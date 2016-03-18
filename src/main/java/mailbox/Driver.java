package mailbox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Driver {
	public static void main(String[] args) {
		// 建立哨兵集合
		Set<String> sentinels = new HashSet<>();
		sentinels.add("127.0.0.1:26379"); // 只用了一个sentinel来监视集群
		// 连接高可用集群
		MailBox mailbox = new MailBox("mymaster", sentinels);
		// 先清空邮箱
		mailbox.destroy();
		
		// 关注大v
		mailbox.follow("001", "bigv100");
		mailbox.follow("001", "bigv200");

		// 大v发内容了
		mailbox.publish("bigv100", "I'm bigv100 & "+ System.currentTimeMillis());
		mailbox.publish("bigv200", "I'm bigv200 & "+ System.currentTimeMillis());
		
		// 输出用户001的邮箱内容
		List<String> box = mailbox.view("001");
		for(String p: box){
			System.out.println(p);
		}
		System.out.println("-----");
		
		// 取关，这将会从该用户的邮箱里删去这个大v相关的信息
		mailbox.unfollow("001", "bigv100");
		
		// 输出变更后的邮箱内容
		box = mailbox.view("001");
		for(String p: box){
			System.out.println(p);
		}
	}
}
