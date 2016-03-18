package mailbox;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MailBoxTest {	
	private static MailBox mailbox;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mailbox = new MailBox();
	}

	@Before
	public void setUp() throws Exception {
		mailbox.destroy();
		
		// 用户001和002都关注了用户bigv100和bigv200
		mailbox.follow("001", "bigv100");
		mailbox.follow("001", "bigv200");
		
		mailbox.follow("002", "bigv100");
		mailbox.follow("002", "bigv200");
	}

	@Test
	public void testPublish() {
		mailbox.publish("bigv100", "event5617");
		List<String> events = mailbox.view("001");
		assertEquals(1, events.size());
		assertEquals("event5617", events.get(0));
	}
	
	@Test
	public void testPublishOrder() {
		mailbox.publish("bigv100", "event5617");
		mailbox.publish("bigv200", "event7764");
		
		List<String> events = mailbox.view("001");
		assertEquals(2, events.size());
		// 事件应该按照倒序排列
		assertEquals("event7764", events.get(0));
		assertEquals("event5617", events.get(1));
	}
	
	@Test
	public void testUnfollow() {
		mailbox.publish("bigv100", "event5617");
		mailbox.publish("bigv200", "event7764");
		
		mailbox.unfollow("001", "bigv200");
		List<String> events = mailbox.view("001");
		// 被取关用户的事件应该移除取关者的邮箱
		assertEquals(1, events.size());
		assertEquals("event5617", events.get(0));
	}
	
	@Test
	public void testLeaders() {
		// 关注测试
		Set<String> leaders = new HashSet<>();
		leaders.add("bigv100");
		leaders.add("bigv200");
		
		Set<String> result = new HashSet<>();
		result.addAll(mailbox.leaders("001"));
		assertEquals(leaders, result);
		
		// 取关测试
		mailbox.unfollow("001", "bigv100");
		leaders.remove("bigv100");
		
		result.clear();
		result.addAll(mailbox.leaders("001"));
		assertEquals(leaders, result);
	}
	
	@Test
	public void testFollowers() {
		// 关注测试
		Set<String> followers = new HashSet<>();
		followers.add("001");
		followers.add("002");
		
		Set<String> result = new HashSet<>();
		result.addAll(mailbox.followers("bigv100"));
		assertEquals(followers, result);
		
		// 取关测试
		mailbox.unfollow("001", "bigv100");
		followers.remove("001");
		result.clear();
		result.addAll(mailbox.followers("bigv100"));
		assertEquals(followers, result);
	}
}
