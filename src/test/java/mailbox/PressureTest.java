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
		System.out.println("setting a test user with 10,000 followers...");
		for(int i=0; i<10000; i++){
			mailbox.follow(i+"", "bigv100");
		}
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test(timeout=50000)
	public void testPublish() {
		// 大V频繁发布事件
		// 50s内给10000个关注者发布500次消息，即1s内可以发布10次
		System.out.println("Let the test user publish 500 events to 10,000 users in 50s...");
		for(int i=0; i<500; i++){
			mailbox.publish("bigv100", "event10086");
		}
		
		List<String> box = mailbox.view("999");
		assertEquals("event10086", box.get(0));
	}
}
