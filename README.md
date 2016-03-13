# mailbox
mailbox是一个基于Redis的大规模消息分发存储工具。

### 需求与解决方案

使用场景：某微博网站有一个具有千万关注者的大V发布了一个微博。
现在要求其任何一个关注者在登陆有都能看到该大V的该条微博。

本工具的解决方案是，为每一个用户维护一个固定大小的『邮箱』，当该大V发布了新的微博，这个事件会发送给其每一个关注者的邮箱。当该关注者上线后，只要访问该邮箱即可（按时间顺序）获取其关注的各个用户的微博。

当然，实际操作中不会真的把微博内容发给所有人，而应该只是把该微博的id广播出去。不过本工具并不做此假设，任何字符串均可。

### 使用方式

启动Redis后，new一个MailBox对象即可使用。

### Redis的启动方式

直接执行编译后的redis-server即可启动单个redis服务器。
另外，还提供了[redis.sh](./src/main/resources/redis/redis.sh)脚本用于启动一个主从备份的Redis实例。
具体而言，执行`redis.sh start`可以启动一个主Redis进程和一个从Redis进程，以及一个哨兵进程用于监控主进程的状态。如果主进程失效，则该哨兵进程会自动把从进程切换为主进程继续提供服务。

### API设计

`void follow(String follower, String publisher)`
关注一个用户

`void unfollow(String follower, String publisher)`
取关一个用户

`void publish(String publisher, String event)`
发出一个事件，该事件会发送到所有关注者的邮箱里

`List<String> view(String follower)`
获取该用户邮箱内的数据

`void destroy()``
清空所有用户的邮箱 危险操作，应仅供开发使用
