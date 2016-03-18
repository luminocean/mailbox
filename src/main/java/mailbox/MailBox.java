package mailbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

/**
 * 消息邮箱操作主类
 * @author luminocean
 *
 */
public class MailBox {
	private static final Logger logger = LoggerFactory.getLogger(MailBox.class);
	
	// 每个用户的邮箱大小
	private static int mailBoxSize = 3;
	// pubish使用的脚本源码
	private static String publishScript;
	// 取关使用的脚本源码
	private static String unfollowScript;
	// 获取源码
	{
		publishScript = readScript("publish.lua");
		unfollowScript = readScript("unfollow.lua");
	}
	
	// redis操作接口
	private Jedis client;
	// sentinel池
	private JedisSentinelPool sentinelPool;

	/**
	 * 创建Mailbox对象，直接与Redis建立连接
	 * @param host Redis服务所在的host地址
	 * @param port Redis服务使用的端口
	 */
	public MailBox(String host, int port){
		client = new Jedis(host, port);
	}
	
	/**
	 * 简易构造函数，使用默认值创建Mailbox对象
	 */
	public MailBox(){
		client = new Jedis("localhost", 6379);
	}
	
	/**
	 * 以高可用方式连接Redis
	 * 在此方式下，至少有两个Redis实例互为主备，且至少有一个哨兵进程(sentinel)用于监视Redis进程的状态
	 * @param masterName 主Redis名称，这个值可以在sentinel的配置脚本中找到
	 * @param sentinels sentinels的地址集合，每一个地址为『ip:port』格式的字符串
	 */
	public MailBox(String masterName, Set<String> sentinels){
		sentinelPool = new JedisSentinelPool(masterName, sentinels);
		client = sentinelPool.getResource();
	}

	/**
	 * 关注一个用户
	 * @param follower 关注者
	 * @param leader 被关注者
	 */
	public void follow(String follower, String leader) {
		// 在leader的关注者集合中加入这个follower
		client.sadd(String.format("user:%s:followers", leader), follower);
		// 同样在follower的leader集合中加入该leader
		client.sadd(String.format("user:%s:leaders", follower), leader);
	}
	
	/**
	 * 取关一个用户
	 * @param follower 关注者
	 * @param leader 被关注者
	 */
	public void unfollow(String follower, String leader) {
		client.eval(unfollowScript, 2, follower, leader);
	}
	
	/**
	 * 发出一个事件，该事件会发送到所有关注者的邮箱里
	 * @param leader 被关注者
	 * @param event 消息内容
	 */
	public void publish(String leader, String event) {
		client.eval(publishScript, 1, leader, event, mailBoxSize+"");
	}
	
	/**
	 * 获取该用户邮箱内的数据
	 * @param follower 用户
	 * @return 邮箱内的所有数据列表
	 */
	public List<String> view(String follower){
		// 将获取的结果set转为list，这个set是有序的
		Set<String> set = client.zrevrange(String.format("user:%s:mailbox", follower), 0, -1);
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}
	
	/**
	 * 返回一个用户关注的所有其他用户
	 * @param follower 用户
	 * @return 关注的所有用户列表
	 */
	public List<String> leaders(String follower){
		Set<String> members = client.smembers(String.format("user:%s:leaders", follower));
		List<String> list = new ArrayList<>(members.size());
		list.addAll(members);
		return list;
	}
	
	/**
	 * 返回一个用户的所有关注者
	 * @param leader 用户
	 * @return 该用户的所有关注者
	 */
	public List<String> followers(String leader){
		Set<String> members = client.smembers(String.format("user:%s:followers", leader));
		List<String> list = new ArrayList<>(members.size());
		list.addAll(members);
		return list;
	}

	/**
	 * 清空所有用户的邮箱
	 * 危险操作，应仅供开发使用
	 */
	public void destroy() {
		client.flushDB();
	}
	
	/**
	 * 关闭该mailbox，断开所有连接
	 */
	public void close(){
		if(sentinelPool != null) sentinelPool.close();
		if(client != null) client.close();
	}
	
	private static String readScript(String filePath){
		// 读取classpath下脚本
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				MailBox.class.getClassLoader().getResourceAsStream(filePath)));
		StringBuilder scriptBuilder = new StringBuilder();
		String buf = null;
		try {
			while( (buf = reader.readLine()) != null ){
				scriptBuilder.append(buf);
			}
		} catch (IOException e) {
			logger.error("读取" + filePath + "失败", e);
		}
		return scriptBuilder.toString();
	}
}
