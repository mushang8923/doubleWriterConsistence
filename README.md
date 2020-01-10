> 传统企业中为了解决高并发大流量的问题，通常使用缓存+数据库的方式来支撑高QPS的访问，虽然能解决读QPS的问题，但是同时也引入了新的问题，例如：缓存与数据库的数据不一致的情况；本博文参考网上相关的博文，详细的整理下缓存数据库数据一致性的问题，并且给出基于Java的代码解决方案

**本文参考中华石杉的教程，感谢大神的分享** 

关于缓存数据库数据一致性的解决方案，网上有很多，但是大都是偏向理论的，且大多数使用分布式锁来实现的，分布式锁也是一种解决方式，但是无疑增加了代码逻辑的复杂性，本博文主要是使用JVM自带的缓存队列+线程池来解决数据一致性的问题，并且针对数据一致性的解决方案通过代码来体现出来，让读者能不仅对数据一致性的原因以及解决方案有更深层次的理解，并且也能落实到代码上


### 谈谈缓存与数据库数据一致性的问题
系统中引入了缓存，这里我们使用Redis,我们会先将数据缓存在Redis中，当外部请求数据时，我们都是先从Redis中查询，如果查询到了直接返回给请求，如果查询不到，则再到数据中进行查询，返回给请求，同时再将数据写入到Redis中。具体的业务逻辑如下图：

![image](https://images.gitee.com/uploads/images/2019/0819/190729_4fca5af6_5042409.png)

- 用户发起请求
- 系统先从缓存中查询是否有相关的数据，如果存在则直接返回给用户
- 如果缓存中不存在，则到数据库中查询，查询到的结果再返回给用户，同时写入到缓存中

**优点**：

这样做的好处是，如果缓存中有数据了，就直接返回，减少了数据库的访问压力，同时也提高了请求响应的数据（少了与数据库之间的交互）

**缺点**:

虽然读数据的性能提升了，但是给数据更新造成了新的麻烦，在高并发的场景中很容易就造成了缓存与数据库数据一致性的问题

一般情况下，我们写数据有两种方式：

- 先更新缓存，再更新数据库

针对这种方案，我们来分析下：

1. 先写缓存，如果写入缓存失败，直接返回，无影响
2. 写入缓存之后，再来写数据库，测试数据库写入失败，如果不清除缓存中的数据，就会造成缓存与数据库中的数据不一致
3. 如果增加清除缓存中的数据，那么清除数据失败怎么处理

- 先更新数据库，再更新缓存

我们再来分析这种方案：

1. 先更新数据库，如果更新数据库失败，直接返回，无影响
2. 写入数据库成功之后 ，再来更新缓存中的数据，如果更新失败，则此时缓存中的数据与数据库中的数据就会不一致，需要添加重试机制，增加代码量，并且业务逻辑复杂化，
3. 就算增加了重试机制，如果重试也失败了，该如何处理

以上的两种方案都是有缺陷的，那么我们该如何处理呢，我们一步步来分析：

以上两种方案都是如果一方的更新失败了，都会造成数据不一致的情况，那么需要想办法来处理，就算一方失败了，也不会出现数据不一致的情况。

怎么处理呢？这里我们先这样处理：

- 先删除缓存中的数据，然后再去更新数据库，最后更新缓存中的数据

1. 写请求过来，我们先删除缓存中的数据，
2. 删除成功之后，我们再更新数据库中的数据，此时如果更新数据库中的数据失败，则整个写请求失败，直接返回，数据没有发生变化，此时读请求过来，发现缓存中没有对应的数据，则会从数据库中读取数据，同时将数据写入到缓存中，此时缓存中的数据和数据库中的数据都是一样的， 不存在数据一致性的问题
3. 更新数据库中的数据之后 ，再来更新缓存中的数据，此时更新缓存中的数据失败，直接返回，数据库中的数据是最新的数据，开始读请求过来，发现缓存中没有对应的数据，则会从数据库中读取数据，同时将数据写入到缓存中，此时缓存中的数据和数据库中的数据都是一样的， 不存在数据一致性的问题
4. 更新缓存成功，此时缓存中的数据和数据库的数据是一致的，不存在数据一致性的问题

具体的业务逻辑见下图：
![image](https://images.gitee.com/uploads/images/2019/0819/190729_1b0f19f3_5042409.png)

乍一看，这种方案完美的解决了数据一致性的问题，我们不妨再来将业务场景复杂点，并发量再大一点，比如说每秒的读QPS为1w+，这是我们再来分析下上述方案的业务逻辑：

1. 用户写请求过来，我们还是先删除缓存，然后再更新数据库
2. 在更新数据库的过程中，此时更新还没有完成，数据库的值依旧是原来的旧值，这时一个读请求过来
3. 发现缓存中没有值，就会到数据库中去查询数据，然后写入到缓存中，此时数据库还没有更新结束，读请求获取的数据依旧是原来的旧数据
4. 这时数据库更新完成，但是更新缓存失败，此时缓存中是用的之前的旧数据与数据库中的新数据就会出现数据不一致的情况，数据一致性的问题又出现了

具体的业务逻辑如下图：
- [ ] 业务流程图

由此可见，上述的方案也是存在问题的，尤其是并发量很大的情况下，这类现象出现的几率就很大；对于这种情况我们该如何处理呢？

**分析**：

我们仔细分析上述的情况，可以发现，读请求和写请求是并行的，这是导致数据一致性的根本原因，并行的请求会导致数据一致性的问题，那么解决此类问题的思路就有了——**将请求串行**！

具体的业务逻辑如下：

1. 写请求过来，将写请求缓存到缓存队列中，并且开始执行写请求的具体操作（删除缓存中的数据，更新数据库，更新缓存）
2. 如果在更新数据库过程中，又来了个读请求，将读请求再次存入到缓存队列中，等待队列前的写请求执行完成，才会执行读请求
3. 之前的写请求删除缓存失败，直接返回，此时数据库中的数据是旧值，并且与缓存中的数据是一致的，不会出现缓存一致性的问题
4. 写请求删除缓存成功，则更新数据库，如果更新数据库失败，则直接返回，写请求结束，此时数据库中的值依旧是旧值，读请求过来后，发现缓存中没有数据， 则会直接向数据库中请求，同时将数据写入到缓存中，此时也不会出现数据一致性的问题
5. 更新数据成功之后，再更新缓存，如果此时更新缓存失败，则缓存中没有数据，数据库中是新值 ，写请求结束，此时读请求还是一样，发现缓存中没有数据，同样会从数据库中读取数据，并且存入到缓存中，其实这里不管更新缓存成功还是失败， 都不会出现数据一致性的问题

具体的业务逻辑如下图：
![image](https://images.gitee.com/uploads/images/2019/0819/190729_2d7313bb_5042409.png)

上述的解决方案是将异步请求串行化，这样做的好处呢就是队列上的工作线程完成之后上一个操作数据库的修改之后，才会执行下一个操作。

上述的解决方案中还有个可以优化的地方，如果在修改数据库更新缓存的过程中，不断有读请求过来怎么处理，队列中都一次防止每次的读请求么，不是的，存放大多的队列只会占用队列的资源，我们这里可以判断过滤下读请求，直接返回，提示用户刷新下页面，重新请求数据，这个过程足够队列中写操作执行完成了，读请求再次请求过来时，可以直接返回缓存即可

### 注意点
1. 读请求长时间阻塞

方案中对读请求做了异步化，当从缓存中读取不到数据，则将该读请求写入缓存队列中，此时一定要注意读请求超时的问题，系统设计要做到每个读请求都在必须要在超时时间内完返回请求结果。

在该方案中，如果大量的写请求进入，存放到缓存队列中，这样后来的读请求就会发现在缓存中读取不到数据，也进入到缓存队列中，我们这里来做个简单的假设，如果一个写请求需要50ms的时间，当队列中存在4个写请求的话,就会由200ms的读请求延迟，一般读请求200ms的延迟，用户是可以接受的。

一次类推，如果一下子来了500个写请求，在单机的基础上，则需要25000ms，这个时间相当长了，所以这时我们需要分布式来解决这个问题，将访问的压力分打给其他的服务实例，比如一个单机，20个队列，每个队列中的写操作需要耗时50ms，则大概需要25个单机就可以hold住每秒500个写请求了，当然这个缓存队列的配置还需要跟服务器的内存和实际压测过程中的情况去调节缓存队列中的核心线程数和最大线程数。

当然上面只是一个大概数据的估算，在实际生产环境中一般呈现二八定律的，按照个比率来估算每秒的写请求也是可以的

总结下：当写请求大量的请求过来的时候，如果此时又有大量的读请求的话，单机版本的可能会造成读请求时间过长，我们这里是通过分布式服务的方式来分担写请求的访问压力，通过分担的方式加快写请求的操作，这样读请求返回的时间就快了

2. 读请求并发量较高

还有一个场景就是大量的读请求过来，这个场景和上述的场景比较像，比如说每秒有500个写请求过来，按照上述的方案，会先删除缓存，此时关于这个缓存会有大量的读请求过来，我们按照读写比例20:1的比率来算，就是一个写请求对应20个读请求，那么500个写请求就会由1w个读请求，此时如果还是使用单机的话，1w个读请求（都是缓存被删除的）,此时单机版本肯定是玩不转的了，我们还是需要水平横向扩展，通过增加服务处理的实例，来分担QPS的压力，但是对于缓存执行更新的操作，还是需要通过Nginx服务器来路由到相同的服务实例上

4. 热点数据的路由问题，导致请求倾斜

准确来说这个场景和秒杀比较像，但是场景比较像但是解决的方案则是不一样的，秒杀有秒杀自己的一套解决方案，这里主要是热点数据的QPS非常高，我们前面通过Nginx服务器会将对于该热点数据的请求全部路由到相同的服务实例上，就会造成该服务实例的压力会很大，这个时候需要根据情况来处理

以上就是我们本次缓存数据库双写一致性的解决方案，该方案能解决一部分的问题，但是在实际的生产场景中，还是需要考虑该方案一些注意的要点，结合自己的业务场景来调整该方案，通过模拟、极限压测等方式来优化，落地一套相对比较完善的数据一致性的解决方案

### 代码实现

以上我们详细的分析了数据一致性的解决方案的原理和需要注意的地方，下面我们来通过Java代码来实现该方案

上面的方案中我们已经讨论过，通过JVM待在的缓存队列来缓存读写的请求，并且将所有的请求异步串行化，这里我们使用SpringBoot框架来进行代码实现

1. 首先我们先在系统启动的时候，初始化线程池和缓存队列

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Package com.amos.common.config
     * @ClassName ServletListenerRegistrationConfig
     * @Description 在容器启动的时候，注册自定义的Listener
     * 1. 在监听器中初始化线程池
     * @Author Amos
     * @Modifier
     * @Date 2019/7/14 16:41
     * @Version 1.0
     **/
    @Configuration
    public class ServletListenerRegistrationConfig {
    
        /**
         * 注册自定义的Bean
         * 并且设置监听器，该监听器初始化线程池
         *
         * @return
         */
        @Bean
        public ServletListenerRegistrationBean registrationBean() {
            ServletListenerRegistrationBean servletListenerRegistrationBean = new ServletListenerRegistrationBean();
            servletListenerRegistrationBean.setListener(new InitThreadLocalPoolListen());
            return servletListenerRegistrationBean;
        }
    }
```
2. 按照标配，我们使用线程池来存储线程，当然使用线程池有很多的好处，主要如下：

- 降低资源消耗

可以利用重复已创建的线程降低线程创建和销毁的消耗 

- 提高响应速度

当任务到达时，任务可以不需要等到线程创建就能立即执行

- 提高线程的可管理性

使用线程池可以进行统一分配、调优和监控

我们新建一个类主要是用来创建线程池，一般系统中线程池都是单例的，而且必须是线程安全的, 单例的线程安全有很多中，本博文这里使用静态内部类的方是来实现单例模式（任性，可以空间换时间）

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Package com.amos.common.thread
     * @ClassName RequestThreadPool
     * @Description 请求线程池
     * 1. 使用线程池来管理线程，该线程池必须是单例的
     * 2. 线程池初始化成功后，创建缓存队列，并且和线程池进行绑定
     * @Author Amos
     * @Modifier
     * @Date 2019/7/14 16:47
     * @Version 1.0
     **/
    @Component
    public class RequestThreadPool {
        /**
         * 核心线程数
         */
        @Value("${request.queue.corePoolSize:10}")
        private Integer corePoolSize;
        /**
         * 线程池最大线程数
         */
        @Value("${request.queue.maximumPoolSize:20}")
        private Integer maximumPoolSize;
    
        /**
         * 线程最大存活时间
         */
        @Value("${request.queue.keepAliveTime:60}")
        private Long keepAliveTime;
    
        /**
         * 初始化线程池 这里我们不使用Executors.newFixedThreadPool()方式，该种方式不推荐使用，
         * 主要是因为默认允许的队列的长度是Integer.MAX_VALUE,可能会造成OOM
         * 第一个参数：corePoolSize: 线程中核心线程数的最大值（能同时运行的最大的线程数）
         * 第二个参数：maximumPoolSize: 线程池中线程数的最大值
         * 第三个参数：keepAliveTime: 线程存活时间
         * 第四个参数：unit：时间单位
         * 第五个参数：BlockingQueue: 用于缓存任务的队列 这里使用 ArrayBlockingQueue 这个是有界队列
         */
        private ExecutorService threadPool = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize,
                this.keepAliveTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue(this.corePoolSize));
    
    
        /**
         * 构造器私有化，这样就不能通过new来创建实例对象
         * <p>
         * 类实例化的时候 ，初始化队列的大小，并且绑定队列和线程池以及队列与线程的关系
         * <p>
         * 初始化指定数量的队列
         */
        private RequestThreadPool() {
            /**
             *缓存队列集合来管理所有的缓存队列
             */
            RequestQueue requestQueue = RequestQueue.getInstance();
            for (int i = 0; i < this.corePoolSize; i++) {
                /**
                 * 缓存队列使用Request 接口来作为泛型，将可以将队列的类型添加定义，同时也可以通过多态的特性来实现子类的扩展
                 * 目前Request只是定义，业务可以之后实现
                 */
                ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<>(this.corePoolSize);
                requestQueue.add(queue);
                // 线程池和缓存队列通过线程来绑定
                // 每个线程对应一个队列
                this.threadPool.submit(new RequestThread(queue));
            }
        }
    
        /**
         * 使用静态内部类来实现单例的模式（绝对的线程安全）
         */
        private static class Singleton {
            /**
             * 私有的静态变量，确保该变量不会被外部调用
             */
            private static RequestThreadPool requestThreadPool;
    
            /**
             * 静态代码块在类初始化时执行一次
             */
            static {
                requestThreadPool = new RequestThreadPool();
            }
    
            /**
             * 静态内部类对外提供实例的获取方法
             *
             * @return
             */
            public static RequestThreadPool getInstance() {
                return requestThreadPool;
            }
        }
    
        /**
         * 请求线程池类对外提供获取实例的方法 由于外部类没有RequestThreadPool的实例对象，所以除了该方法，外部类无法创建额外的RequestThreadPool对象
         *
         * @return
         */
        public static RequestThreadPool getInstance() {
            return Singleton.getInstance();
        }
    
    
    }
```

我将整个代码贴出来，方便大家查看，该类的主要用途是系统启动的时候初始化线程池，并且创建缓存队列，将队列和线程池进行绑定

该类中的构造器是`private`修饰的，这样处理的目的主要是为了不让线程池创建之后再创建多余的实例对象，其次也是为了方便在构造器中完成线程池与缓存队列之间的绑定

既然构造器被私有化了，我们就得提供一个供外部获取实例的方法，这里我们使用了静态内部类是实现单例模式，让线程池的实例保持一个。为什么要使用静态内部了呢？

-  外部内加载的时候，不需要立即加载内部类，内部类不被加载，就不会初始化，故而不占用内存
-  当getInstance被调用时，才会去初始化实例，第一次调用getInstance会导致虚拟机加载实例，这种方法不仅能确保线程的安全，也能保证单例的唯一性

线程池存储的线程主要是用来处理外部过来的请求，所以缓存对列主要用来对请求进行处理，而且请求队列必须也是单例的

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: rabbitmq
     * @ClassName: RequestQueue
     * @Package: com.amos.common.request
     * @author: zhuqb
     * @Description: 请求的队列
     * <p/>
     * 1. 这里需要使用单例模式来确保请求的队列的对象只有一个
     * @date: 2019/7/15 0015 下午 14:18
     * @Version: V1.0
     */
    public class RequestQueue {
        /**
         * 构造器私有化，这样就不能通过new来创建实例对象
         * 这里构造器私有化 这点跟枚举一样的，所以我们也可以通过枚举来实现单例模式，详见以后的博文
         */
        private RequestQueue() {
        }
    
        /**
         * 内存队列
         */
        private List<ArrayBlockingQueue<Request>> queues = new ArrayList<ArrayBlockingQueue<Request>>();
    
        /**
         * 私有的静态内部类来实现单例
         */
        private static class Singleton {
            private static RequestQueue queue;
    
            static {
                queue = new RequestQueue();
            }
    
            private static RequestQueue getInstance() {
                return queue;
            }
        }
    
        /**
         * 获取 RequestQueue 对象
         *
         * @return
         */
        public static RequestQueue getInstance() {
            return Singleton.getInstance();
        }
    
        /**
         * 向容器中添加队列
         *
         * @param queue
         */
        public void add(ArrayBlockingQueue<Request> queue) {
            this.queues.add(queue);
        }
    
    }
```



线程池和缓存队列通过线程来绑定，一个线程对应一个缓存队列，在线程里来处理缓存队列中的逻辑

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: rabbitmq
     * @ClassName: RequestThread
     * @Package: com.amos.common.thread
     * @author: zhuqb
     * @Description: 执行请求的工作线程
     * <p/>
     * 线程和队列进行绑定，然后再线程中处理对应的业务逻辑
     * @date: 2019/7/15 0015 下午 14:34
     * @Version: V1.0
     */
    public class RequestThread implements Callable<Boolean> {
        /**
         * 队列
         */
        private ArrayBlockingQueue<Request> queue;
    
        public RequestThread(ArrayBlockingQueue<Request> queue) {
            this.queue = queue;
        }
    
        /**
         * 方法中执行具体的业务逻辑
         * TODO 这里我们先搭建整理的框架，后面在慢慢处理缓存队列
         *
         * @return
         * @throws Exception
         */
        @Override
        public Boolean call() throws Exception {
            return true;
        }
    }
```

然后再监听器中获取线程时的实例对象来完成线程池的启动初始化
```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Package com.amos.common.listener
     * @ClassName InitThreadLocalPoolListen
     * @Description 系统初始化监听器 初始队列
     * @Author Amos
     * @Modifier
     * @Date 2019/7/14 16:44
     * @Version 1.0
     **/
    public class InitThreadLocalPoolListen implements ServletContextListener {
        /**
         * 系统初始化队列
         *
         * @param sce
         */
        @Override
        public void contextInitialized(ServletContextEvent sce) {
            RequestThreadPool.getInstance();
        }
    
        /**
         * 监听器销毁执行的逻辑
         *
         * @param sce
         */
        @Override
        public void contextDestroyed(ServletContextEvent sce) {
    
        }
    }
```
至此，我们框架的代码算是搭建完成了，下面我们以商品购买库存减一的功能来实现缓存与数据库双写一致性性解决方案的代码实现

**代码逻辑如下**：

> 1. 需要集成redis和mysql数据库操作
> 2. 需要一个处理redis的请求和处理数据库的请求业务逻辑代码
> 3. 在缓存队列的线程中执行基于缓存和数据库双写一致性的代码

接下来我们开始搭建环境，关于Redis的环境搭建可以参考我以前的博文[《Redis教程（一）——Redis安装》](https://www.jianshu.com/p/a0bd76adcc1d)，然后我们在SpringBoot中来集成操作Redis的功能，

- 集成Redis和mysql的数据库操作

在pom文件中添加springBoot整合redis的依赖

```xml
    <!-- 添加SpringBoot集成Redis的依赖-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>2.1.6.RELEASE</version>
    </dependency>
```

然后编写Redis的操作功能类
```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Package com.amos.common.util
     * @ClassName RedisUtils
     * @Description redis的操作类
     * @Author Amos
     * @Modifier
     * @Date 2019/8/18 0:13
     * @Version 1.0
     **/
    @Component
    public class RedisUtils {
        public final Log logger = LogFactory.getLog(this.getClass());
        @Autowired
        private RedisTemplate<String, Object> redisTemplate;
    
        /**
         * 指定键缓存实效的时间
         *
         * @param key        指定的键
         * @param expireTime 超时时间 毫秒
         * @return
         */
        public boolean expire(String key, long expireTime) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");
    
            if (expireTime < 0) {
                throw new RabbitMQException("超时时间不能小于0");
            }
            try {
                return this.redisTemplate.expire(key, expireTime, TimeUnit.MICROSECONDS);
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
    
        /**
         * 判断是否有指定的key
         *
         * @param key 指定的键
         * @return
         */
        public boolean hasKey(String key) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");
    
            try {
                return this.redisTemplate.hasKey(key);
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
    
        /**
         * 保存键值
         *
         * @param key   保存的键
         * @param value 保存的值
         * @return
         */
        public boolean save(String key, Object value) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");
    
            try {
                this.redisTemplate.opsForValue().set(key, value);
                return Boolean.TRUE;
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
    
        /**
         * 删除key
         *
         * @param key
         * @return
         */
        public boolean del(String key) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "指定的键不能为空");
            try {
                return this.redisTemplate.delete(key);
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }
        }
    
        /**
         * 保存有实效时间的键值对
         *
         * @param key
         * @param value
         * @param expireTime 实效时间 单位毫秒
         * @return
         */
        public boolean save(String key, Object value, long expireTime) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(key, "键值不能为空");
    
            try {
                this.redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MICROSECONDS);
                return Boolean.TRUE;
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return Boolean.FALSE;
            }
    
        }
    }
```
至此，我们的集成Redis就已经完成了，可以自己写个测试类测试下

我们再来整合mybatis

我们在pom文件中添加mybatis的依赖
```xml
    <!-- 集成Mybatis -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.0.1</version>
    </dependency>
```
接着在创建创建如下的目录结构：
![image](https://s2.ax1x.com/2019/08/18/mlkQi9.png)

在application.yml文件中添加
```yml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    username: root
    password: 123
    url: jdbc:mysql://localhost:3306/amos?useUnicode=true&characterEncoding=utf8&useSSL=false
mybatis:
  config-location: classpath:/mybatis/config/mybatis-config.xml
  mapper-locations: classpath:/mybatis/mapper/*.xml
```
在 classpath:/mybatis/config/mybatis-config.xml中添加
```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE configuration
            PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
            "http://mybatis.org/dtd/mybatis-3-config.dtd">
    <configuration>
        <settings>
            <setting name="mapUnderscoreToCamelCase" value="true"/>
        </settings>
        <typeAliases>
            <package name="com.amos.doublewriterconsistence.entity"/>
        </typeAliases>
    </configuration>
```
其余的配置主要是常见的mybatis的Mapper和Entity,这里我就不详细列举出来了，可以参考我的Gitee的源码,我里面添加了详细的注释，方便阅读
[doubleWriterConsistence](https://gitee.com/amos_zhu/doubleWriterConsistence)

至此SpringBoot集成Mybatis的框架也完成了，大家可以自己编写测试类进行测试功能是否正常，同时我们也完成了代码逻辑中的第一点，接下来是我们此次代码的重点，着重讲解下，如果使用缓存队列来实现一致性的功能代码

- 新增业务处理缓存和数据库的业务逻辑代码

这里我们先将设计业务逻辑的代码编写出来
1. 库存的操作方法

统一提供库存的方法: 从数据库中查询，更新数据库，从缓存中查询，删除缓存数据，保存缓存数据
```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: double-writer-consistence
     * @ClassName: InventoryServiceImpl
     * @Package: com.amos.doublewriterconsistence.service
     * @author: amos
     * @Description:
     * @date: 2019/8/19 0019 下午 14:23
     * @Version: V1.0
     */
    @Service
    public class InventoryServiceImpl implements InventoryService {
        public final Logger logger = LoggerFactory.getLogger(this.getClass());
    
        @Autowired
        InventoryMapper inventoryMapper;
    
        @Autowired
        RedisUtils redisUtils;
    
        /**
         * 删除库存的缓存
         *
         * @param key
         * @return
         */
        @Override
        public Boolean removeInventoryCache(String key) {
            this.logger.info("移除库存：{} 的缓存", key);
            key = InventoryKeyUtils.getInventoryKey(key);
            return this.redisUtils.del(key);
        }
    
        /**
         * 更新数据库库存记录
         *
         * @param inventory
         */
        @Override
        public void updateInventory(Inventory inventory) {
            this.logger.info("更新库存：{} 的库存记录", inventory.getId());
            this.inventoryMapper.update(inventory);
        }
    
        /**
         * 保存库存的缓存记录
         *
         * @param inventory
         * @return
         */
        @Override
        public Boolean saveInventoryCache(Inventory inventory) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(inventory);
            String key = InventoryKeyUtils.getInventoryKey(inventory.getId());
            this.logger.info("保存缓存数据的Key：{}", key);
            return this.redisUtils.save(key, inventory);
        }
    
        /**
         * 获取指定key的缓存值
         *
         * @param key
         * @return
         */
        @Override
        public Inventory getInventoryCache(String key) {
            key = InventoryKeyUtils.getInventoryKey(key);
            Object object = this.redisUtils.get(key);
            return JSONObject.parseObject(JSONObject.toJSONString(object), Inventory.class);
        }
    
        /**
         * 根据id查询库存记录
         *
         * @param id
         * @return
         */
        @Override
        public Inventory selectById(String id) {
            return this.inventoryMapper.selectById(id);
        }
    
        /**
         * 设置空值在缓存中的失效时间
         *
         * @param inventoryKey 键值
         * @param expireTime   失效时间
         */
        @Override
        public void saveNullForCache(String inventoryKey, long expireTime) {
            AmExcepitonEnum.NOT_NULL.assertNotEmpty(inventoryKey);
            String key = InventoryKeyUtils.getInventoryKey(inventoryKey);
            this.logger.info("保存空值，Key：{}", key);
            this.redisUtils.save(key, "", expireTime);
        }
    }
```

2. 读取数据的代码逻辑 

一个读请求过来，我们需要从数据库中读取对应的缓存记录，并且将该数据保存到缓存中，由于我们需要将所有的请求都是通过缓存队列来处理的，所以缓存的操作类应该实现Request接口，在定义好的方法中实现缓存读取的操作

我们在缓存读取的操作类中添加了`isForceFresh`字段来为过滤多重读请求提供支持
```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: rabbitmq
     * @ClassName: InventoryCacheRequestImpl
     * @Package: com.amos.common.request.impl
     * @author: amos
     * @Description: 处理缓存的业务请求
     * 缓存这边我们需要在数据库中查询出对应的数据，然后将数据写入到缓存中
     * 由此我们需要获取库存的id，根据id获取库存的数据
     * 然后将库存数据写入到缓存中 数据中的key是库存ID的标识，value是查询出来的缓存数据
     * @date: 2019/8/19 0019 上午 8:59
     * @Version: V1.0
     */
    public class InventoryCacheRequest implements Request {
        public final Logger logger = LoggerFactory.getLogger(this.getClass());
        /**
         * 库存的id
         */
        private String inventoryId;
        private InventoryService inventoryService;
        /**
         * 是否需要更新缓存
         * 数据更新该值是false
         */
        private Boolean isForceFresh;
    
        public InventoryCacheRequest(String inventoryId, InventoryService inventoryService, Boolean isForceFresh) {
            this.inventoryId = inventoryId;
            this.inventoryService = inventoryService;
            this.isForceFresh = isForceFresh;
        }
    
        /**
         * 1. 根据id到数据库中查询对应的库存数据
         * 2. 查询到了则将数据保存到缓存中
         * 3. 如果查询不到的话则将对应的空数据保存到缓存中，并且设置失效时间
         * 这里的查询不到数据也保存到缓存中，主要是为了防止恶意请求，以防通过不断的循环一个查找不到记录的id来不断的请求数据库，给数据库造成了访问压力，占用系统的资源
         * 同时，也给缓存数据设置失效时间，方便数据发生变化时，及时提供变更后的数据
         */
        @Override
        public void process() {
            // 首先从数据库中查询对应的库存数据
            Inventory inventory = this.inventoryService.selectById(this.inventoryId);
            this.logger.info("库存缓存操作——查询数据库数据:" + JSONObject.toJSONString(inventory));
            if (StringUtils.isEmpty(inventory)) {
                // 查询不到数据的话，对应的key存储空字符串，并且设置失效时间
                this.inventoryService.saveNullForCache(InventoryKeyUtils.getInventoryKey(this.inventoryId), 1000);
            } else {
                this.logger.info("库存缓存操作——保存缓存数据:" + JSONObject.toJSONString(inventory));
                this.inventoryService.saveInventoryCache(inventory);
            }
        }
    
    
        @Override
        public String getInventoryId() {
            return this.inventoryId;
        }
    
        @Override
        public Boolean isForceRefresh() {
            return this.isForceFresh;
        }
    }
```

3. 更新数据的代码逻辑

提高数据更新的请求操作,数据更新过程中，我们需要先删除缓存中的数据，然后再更新数据库中的数据
```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Package com.amos.consumer.service.impl
     * @ClassName InventoryServiceImpl
     * @Description 数据更新操作
     * 1. 先删除缓存中的数据
     * 2. 再更新数据库中的数据
     * @Author Amos
     * @Modifier
     * @Date 2019/8/18 22:16
     * @Version 1.0
     **/
    public class InventoryDBRequest implements Request {
    
        public final Logger logger = LoggerFactory.getLogger(this.getClass());
        private Inventory inventory;
    
        private InventoryService inventoryService;
    
        /**
         * 构造器
         *
         * @param inventory
         * @param inventoryService
         */
        public InventoryDBRequest(Inventory inventory, InventoryService inventoryService) {
            this.inventory = inventory;
            this.inventoryService = inventoryService;
        }
    
        /**
         * 库存数据库操作
         * 1. 先删除缓存中对应的数据
         * 2. 更新数据库中的数据
         */
        @Override
        public void process() {
            this.logger.info("数据库操作——移除缓存中的数据");
            // 首先删除缓存中的数据
            this.inventoryService.removeInventoryCache(this.inventory.getId());
            // 为了测试 所以这里操作时间长点
            try {
                this.logger.info("数据库操作——等待3秒操作");
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 再更新数据库中的数据
            this.logger.info("数据库操作——更新数据库中的数据");
            this.inventoryService.updateInventory(this.inventory);
        }
    
        /**
         * 接口返回库存记录的ID
         *
         * @return
         */
        @Override
        public String getInventoryId() {
            return this.inventory.getId();
        }
    
        /**
         * 始终不更新
         *
         * @return
         */
        @Override
        public Boolean isForceRefresh() {
            return Boolean.FALSE;
        }
    }
```
- 在队列中整合缓存和数据库的业务逻辑处理

上面我们已经完成了读数据和数据更新的功能，现在我们需要在后台的队列处理中，进行相关的业务处理，业务的处理我们已经定义了process公用方法了，现在主要的逻辑是在数据更新过程中如何过滤多次的读请求

还记得我们上面的`isForceFresh`字段么，这里我们主要是根据这个字段来判断是否是重复的读请求，下面是代码，代码中有详细的注释说明，方便阅读
```java
     /**
     * 方法中执行具体的业务逻辑
     *
     * @return
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        try {
            while (true) {
                // ArrayBlockingQueue take方法 获取队列排在首位的对象，如果队列为空或者队列满了，则会被阻塞住
                Request request = this.queue.take();
                Boolean forceFresh = request.isForceRefresh();
                // 如果需要更新的话
                if (!forceFresh) {
                    RequestQueue requestQueue = RequestQueue.getInstance();
                    Map<String, Boolean> tagMap = requestQueue.getTagMap();
                    // 如果是请求缓存中的数据
                    if (request instanceof InventoryCacheRequest) {
                        Boolean tag = tagMap.get(request.getInventoryId());
                        // 如果tag为空 则说明读取缓存的操作
                        if (null == tag) {
                            tagMap.put(request.getInventoryId(), Boolean.FALSE);
                        }
                        // tag为不为空，并且为true时，说明上一个请求是更新数据库的
                        // 那么此时我们需要将标志位修改为False
                        if (tag != null && tag) {
                            tagMap.put(request.getInventoryId(), Boolean.FALSE);
                        }

                        // tag不为空，并且为false时，说明前面已经有数据库+缓存的请求了，
                        // 那么这个请求应该是读请求，可以直接过滤掉了，不要添加到队列中
                        if (tag != null && !tag) {
                            return Boolean.TRUE;
                        }

                    } else if (request instanceof InventoryDBRequest) {
                        // 如果是更新数据库的操作
                        tagMap.put(request.getInventoryId(), Boolean.TRUE);
                    }
                }
                // 执行请求处理
                this.logger.info("缓存队列执行+++++++++++++++++，{}", request.getInventoryId());
                request.process();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }
```
至此,处理业务逻辑的代码我们已经完成了，这里需要注意一点，所有的请求我们都需要打入到缓存队列中来执行下，所以同一库存，我们需要他打入到同一缓存队列中进行处理，如何来实现这个这功能呢？ 这里我们使用hash值取模的方式来实现

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: double-writer-consistence
     * @ClassName: RequestAsyncProcessServiceImpl
     * @Package: com.amos.doublewriterconsistence.service.impl
     * @author: amos
     * @Description:
     * @date: 2019/8/19 0019 下午 15:23
     * @Version: V1.0
     */
    @Service
    public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {
    
        public final Logger logger = LoggerFactory.getLogger(this.getClass());
    
        /**
         * 路由到指定的缓存队列中
         * doubleWriterConsistence
         *
         * @param request
         */
        @Override
        public void route(Request request) {
            try {
                // 做请求的路由，根据每个请求的商品id，路由到对应的内存队列中去
                ArrayBlockingQueue<Request> queue = this.getRoutingQueue(request.getInventoryId());
                // 将请求放入对应的队列中，完成路由操作
                queue.put(request);
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }
    
        /**
         * 根据库存记录路由到指定的缓存队列
         *
         * @param key
         * @return
         */
        private ArrayBlockingQueue<Request> getRoutingQueue(String key) {
            RequestQueue requestQueue = RequestQueue.getInstance();
            int h;
            int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >> 16);
            // 对hash值取模，将hash值路由到指定的内存队列中，比如内存队列大小8
            // 用内存队列的数量对hash值取模之后，结果一定是在0~7之间
            // 所以任何一个商品id都会被固定路由到同样的一个内存队列中去的
            int index = (requestQueue.size() - 1) & hash;
            this.logger.info("路由的缓存队列为：{}", index);
            return requestQueue.getQueue(index);
        }
    }
```

到这里，我们就已经完成了相关代码的开发，接下来需要我们进行编写测试代码来测试下功能是否正常

```java
    /**
     * Copyright © 2018 五月工作室. All rights reserved.
     *
     * @Project: double-writer-consistence
     * @ClassName: InventoryController
     * @Package: com.amos.doublewriterconsistence.web
     * @author: amos
     * @Description: 主要测试
     * 1. 所有的请求是否从缓存队列中走
     * 2. 通过延迟数据的操作，看看读请求是否有等待
     * 3，读请求通过之后，相同的读请求是否直接返回
     * 4. 读请求的数据是否从缓存中获取
     * @date: 2019/8/19 0019 下午 15:31
     * @Version: V1.0
     */
    @RestController
    @RequestMapping(value = "/inventory")
    public class InventoryController {
        public final Logger logger = LoggerFactory.getLogger(this.getClass());
    
        @Autowired
        InventoryService inventoryService;
    
        @Autowired
        RequestAsyncProcessService requestAsyncProcessService;
    
        /**
         * 更新库存的数据记录
         * 1. 将更新数据的记录路由到指定的队列中
         * 2. 后台不断的将从队列中取值去处理
         *
         * @param inventory
         * @return
         */
        @PostMapping(value = "/updateInventory")
        public Result updateInventory(@RequestBody Inventory inventory) {
            try {
                Request request = new InventoryDBRequest(inventory, this.inventoryService);
                this.requestAsyncProcessService.route(request);
                return ResultWapper.success();
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                this.logger.error(e.getMessage());
                return ResultWapper.error(e.getMessage());
            }
        }
    
        /**
         * 获取库存记录
         * 如果在在一定时间内获取不到数据，则直接从数据库中获取，并且数据写入到缓存中
         *
         * @param id
         * @return
         */
        @GetMapping(value = "/getInventory/{id}")
        public Result getInventory(@PathVariable("id") String id) {
            this.logger.info("获取库存记录：{}", id);
            Inventory inventory = null;
            try {
                Request request = new InventoryCacheRequest(id, this.inventoryService, Boolean.FALSE);
                this.requestAsyncProcessService.route(request);
                long startTime = System.currentTimeMillis();
                long waitTime = 0L;
                // 不断循环从缓存中获取数据
                // 如果在在一定时间内获取不到数据，则直接从数据库中获取，并且数据写入到缓存中
                while (true) {
                    if (waitTime > 3000) {
                        break;
                    }
                    inventory = this.inventoryService.getInventoryCache(id);
                    if (null != inventory) {
                        this.logger.info("从缓存中获取到数据");
                        return ResultWapper.success(inventory);
                    } else {
                        Thread.sleep(20);
                        waitTime = System.currentTimeMillis() - startTime;
                    }
    
                }
    
                // 直接从数据库中获取数据
                inventory = this.inventoryService.selectById(id);
                if (null != inventory) {
                    request = new InventoryCacheRequest(id, this.inventoryService, Boolean.TRUE);
                    this.requestAsyncProcessService.route(request);
                    return ResultWapper.success(inventory);
                }
                return ResultWapper.error("查询不到数据");
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                this.logger.error(e.getMessage());
                return ResultWapper.error(e.getMessage());
            }
        }
    }
```
上面详细的代码可以参考我的Gitee——[doubleWriterConsistence](https://gitee.com/amos_zhu/doubleWriterConsistence)

