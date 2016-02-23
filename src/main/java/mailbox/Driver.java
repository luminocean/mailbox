package mailbox;

import java.util.List;

import redis.clients.jedis.Jedis;

public class Driver {
	public static void main(String[] args) {
		MailBox mailbox = new MailBox();
		// 关注大v
		mailbox.follow("001", "bigv100");
		mailbox.follow("001", "bigv200");

		// 大v发内容了
		mailbox.publish("bigv100", System.currentTimeMillis()+"");
		mailbox.publish("bigv200", System.currentTimeMillis()+"");
		
		// 输出用户001的邮箱内容
		List<String> box = mailbox.view("001");
		for(String p: box){
			System.out.println(p);
		}
		System.out.println("-----");
		
		// 取关
		mailbox.unfollow("001", "bigv100");
		
		// 输出变更后的邮箱内容
		box = mailbox.view("001");
		for(String p: box){
			System.out.println(p);
		}
	}
}
