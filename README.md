# mailbox
mailbox是一个基于Redis的大规模消息分发存储工具。

### 1. 需求与解决方案

使用场景：某微博网站有一个具有千万关注者的大V发布了一个微博。
现在要求其任何一个关注者在登陆有都能看到该大V的该条微博。

本工具的解决方案是，为每一个用户维护一个固定大小的『邮箱』，当该大V发布了新的微博，这个事件会发送给其每一个关注者的邮箱。当该关注者上线后，只要访问该邮箱即可（按时间顺序）获取其关注的各个用户的微博。

当然，实际操作中不会真的把微博内容发给所有人，而应该只是把该微博的id广播出去。不过本工具并不做此假设，任何字符串均可。

### 2. Redis的启动方式

直接执行编译后的redis-server即可启动单个redis服务器。

另外，还提供了[redis.sh](./src/main/resources/redis/redis.sh)脚本用于启动一个主从备份的Redis实例。
执行`redis.sh start`可以启动一个主Redis进程、一个从Redis进程以及一个哨兵进程(sentinel)用于监控主进程的状态。如果主进程失效，则该哨兵进程会自动把从进程切换为主进程继续提供服务。

### 3. 运行端口信息

redis官方默认的启动端口是`6379`，因此直接启动redis时使用的这个端口。

使用附带的`redis.sh`脚本启动时，主服务器使用`6379`端口，从服务器使用`6378`端口，哨兵进程使用`26379`端口。

### 4. mailbox使用方式

启动Redis后，new一个MailBox对象即可使用。

但是在高可用的需求下，如果连接的redis实例失效，mailbox将无法访问该redis实例。
即使sentinel进程发现了这个失效问题并及时切换到了从redis实例，mailbox也无从得知新的redis实例的地址是什么。

因此更好的方式是，在创建mailbox时传入一个sentinel进程的地址集合(`Set<String>`)，这样mailbox可以通过查询sentinel来获知主redis进程是什么，从而可以在redis实例失效时及时访问到新的且可用的redis实例。

### API

#### 构造函数

`MailBox()`
简易构造函数，使用默认值创建Mailbox对象

`MailBox(String host, int port)`
创建Mailbox对象，直接与Redis建立连接

`MailBox(String masterName, Set<String> sentinels)`
以高可用方式连接Redis 在此方式下，至少有两个Redis实例互为主备，且至少有一个哨兵进程(sentinel)用于监视Redis进程的状态

#### 实例方法

`void follow(String follower, String leader)`
关注一个用户

`void unfollow(String follower, String leader)`
取关一个用户

`void publish(String leader, String event)`
发出一个事件，该事件会发送到所有关注者的邮箱里

`List<String> view(String follower)`
获取该用户邮箱内的数据

`List<String> followers(String leader)`
返回一个用户的所有关注者

`List<String> leaders(String follower)`
返回一个用户关注的所有其他用户

`void close()`
关闭该mailbox，断开所有连接

`void destroy()`
清空所有用户的邮箱 危险操作，应仅供开发使用
