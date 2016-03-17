package mailbox;

import java.util.List;

import redis.clients.jedis.Jedis;

public class Driver {
	public static void main(String[] args) {
		MailBox mailbox = new MailBox();
		mailbox.destroy();
		
		// 关注大v
		mailbox.follow("001", "bigv100");
		mailbox.follow("001", "bigv200");
		
		for(String leader: mailbox.leaders("001")){
			System.out.print(leader+" ");
		}
		System.out.println();
		
		for(String follower: mailbox.followers("bigv100")){
			System.out.print(follower+" ");
		}
		System.out.println();

		// 大v发内容了
		mailbox.publish("bigv100", "I'm bigv100 & "+ System.currentTimeMillis());
		mailbox.publish("bigv200", "I'm bigv200 & "+ System.currentTimeMillis());
		
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
