package core;

import java.util.List;

import redis.clients.jedis.Jedis;

public class Driver {
	public static void main(String[] args) {
		MailBox mailbox = new MailBox();
		mailbox.follow("001", "bigv100");
		mailbox.follow("002", "bigv100");
		mailbox.follow("003", "bigv100");
		
		mailbox.publish("bigv100", "event00124013r1");
		
		Jedis tester = new Jedis("localhost", 6379);
		List<String> box = tester.lrange("user:001:mailbox", 0, -1);
		for(String p: box){
			System.out.println(p);
		}
	}
}
