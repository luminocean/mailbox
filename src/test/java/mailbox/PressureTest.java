package mailbox;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PressureTest {
	private static MailBox mailbox;
	
	@BeforeClass
	public static void setUpBeforeClass(){
		mailbox = new MailBox();
		mailbox.destroy();
		
		// 给大V准备10000个关注者
		System.out.println("setting a test user with 1,000 followers...");
		for(int i=0; i<1000; i++){
			mailbox.follow(i+"", "bigv100");
		}
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test(timeout=10000)
	public void testPublish() {
		// 大V频繁发布事件
		// 10s内给1000个关注者发布1000次消息，即在1000个关注者的规模下每秒可发100次
		System.out.println("Let the test user publish 1,000 events to 1,000 users in 10s...");
		for(int i=0; i<1000; i++){
			mailbox.publish("bigv100", "event10086");
		}
		
		List<String> box = mailbox.view("999");
		assertEquals("event10086", box.get(0));
	}
}
