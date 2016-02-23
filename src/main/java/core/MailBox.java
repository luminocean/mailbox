package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	public static enum MODE { STAND_ALONE, CLUSTER };
	
	// 每个用户的邮箱大小
	private static int mailBoxSize = 3;
	// pubish使用的脚本源码
	private static String publishScript;
	
	{
		// 读取publish脚本
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				this.getClass().getClassLoader().getResourceAsStream("publish.lua")));
		StringBuilder scriptBuilder = new StringBuilder();
		String buf = null;
		try {
			while( (buf = reader.readLine()) != null ){
				scriptBuilder.append(buf);
			}
		} catch (IOException e) {
			logger.error("读取publish.lua脚本失败", e);
		}
		publishScript = scriptBuilder.toString();
	}
	
	// redis操作接口
	private JedisCommands client;
	
	/**
	 * 创建Mailbox操作类，与Redis建立连接
	 * @param host Redis服务所在的host地址
	 * @param port Redis服务使用的端口
	 * @param mode Redis服务运行模式，是单机还是集群的模式
	 */
	public MailBox(String host, int port, MODE mode){
		// 集群模式
		if( mode == MODE.CLUSTER ){
			Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
			jedisClusterNodes.add(new HostAndPort(host, port));
			client = new JedisCluster(jedisClusterNodes);
		}
		// 单机模式
		else{
			client = new Jedis(host, port);
		}
	}
	
	public MailBox(){
		this("localhost", 6379, MODE.STAND_ALONE);
	}

	/**
	 * 关注一个用户
	 * @param follower
	 * @param publisher
	 */
	public void follow(String follower, String publisher) {
		// 在publisher的关注者集合中加入这个follower
		client.sadd(String.format("user:%s:followers", publisher), follower);
	}
	
	/**
	 * 取关一个用户
	 * @param follower
	 * @param publisher
	 */
	public void unfollow(String follower, String publisher) {
		client.srem(String.format("user:%s:followers", publisher), follower);
	}
	
	/**
	 * 发出一个事件，该事件会发送到所有关注者的邮箱里
	 * @param publisher
	 * @param event
	 */
	public void publish(String publisher, String event) {
		((ScriptingCommands)client).eval(publishScript, 1, publisher, event, mailBoxSize+"");
	}

	/**
	 * 重新加载一个用户的邮箱内容
	 * 主要用于该用户关注或取关别的用户时刷新自己的邮箱使用
	 * @param follower
	 * @param events
	 */
	public void reload(String follower, List<String> events) {
		// TODO 尚未实现
	}
	
	/**
	 * 获取该用户邮箱内的数据
	 * @param follower
	 * @return
	 */
	public List<String> view(String follower){
		// TODO 尚未实现
		return null;
	}

	/**
	 * 清空所有用户的邮箱
	 * 危险操作，应仅供开发使用
	 */
	public void destroy() {
		((BasicCommands)client).flushDB();
	}
}
