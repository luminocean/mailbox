package mailbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BasicCommands;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.ScriptingCommands;

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

	/**
	 * 创建Mailbox操作类，与Redis建立连接
	 * @param host Redis服务所在的host地址
	 * @param port Redis服务使用的端口
	 */
	public MailBox(String host, int port){
		client = new Jedis(host, port);
	}
	
	public MailBox(){
		client = new Jedis("localhost", 6379);
	}

	/**
	 * 关注一个用户
	 * @param follower 关注者
	 * @param followee 被关注者
	 */
	public void follow(String follower, String followee) {
		// 在followee的关注者集合中加入这个follower
		client.sadd(String.format("user:%s:followers", followee), follower);
	}
	
	/**
	 * 取关一个用户
	 * @param follower 关注者
	 * @param followee 被关注者
	 */
	public void unfollow(String follower, String followee) {
		client.eval(unfollowScript, 2, follower, followee);
	}
	
	/**
	 * 发出一个事件，该事件会发送到所有关注者的邮箱里
	 * @param followee
	 * @param event
	 */
	public void publish(String followee, String event) {
		client.eval(publishScript, 1, followee, event, mailBoxSize+"");
	}
	
	/**
	 * 获取该用户邮箱内的数据
	 * @param follower
	 * @return
	 */
	public List<String> view(String follower){
		// 将获取的结果set转为list，这个set是有序的
		Set<String> set = client.zrevrange(String.format("user:%s:mailbox", follower), 0, -1);
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}

	/**
	 * 清空所有用户的邮箱
	 * 危险操作，应仅供开发使用
	 */
	public void destroy() {
		client.flushDB();
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
