package com.sendtomoon.dubbo.registry;
/*package com.pingan.dubbo.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.rpc.RpcException;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.health.ServiceHealth;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.ImmutableQueryOptions;
import com.pab.framework.google.common.base.Optional;
import com.pingan.dubbo.registry.ConsulRegistry.ConsulClient;
import com.pingan.dubbo.registry.ConsulRegistry.ServiceHealthWrapper;

public class ConsulRegistry extends FailbackRegistry
{
	  private static final Logger logger = LoggerFactory.getLogger(ConsulRegistry.class);
	  private static final int DEFAULT_CHECK_THRESHOLD = 5;
	  private static final String CONSUL_NAME_SEPARATOR = "-";
	  private final ScheduledExecutorService expireExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("DubboRegistryExpireTimer", true));
	  private final ScheduledFuture<?> expireFuture;
	  private final int checkPeriod;
	  private final int checkFailedNum;
	  private final AtomicInteger failNum = new AtomicInteger(0);

	  private final AtomicReference<ConsulClient> currentClient = new AtomicReference();

	  private final ConcurrentMap<String, ConcurrentHashMap<String, ServiceHealthWrapper>> serviceHealthCaches = new ConcurrentHashMap();

	  private final String TSF_ACL_TOKEN = ConfigUtils.getProperty("tsf_token", "");

	  private final String TSF_INSTANCE_ID = ConfigUtils.getProperty("tsf_instance_id", "");

	  private final String TSF_NAMESPACE_ID = ConfigUtils.getProperty("tsf_namespace_id", "");

	  public ConsulRegistry(URL url) {
	    super(url);

	    if (url.isAnyHost()) {
	      throw new IllegalStateException("registry address == null");
	    }

	    this.checkFailedNum = url.getParameter("threshold", 5);
	    try
	    {
	      String tsfConsulIp = ConfigUtils.getProperty("tsf_consul_ip");
	      Integer tsfConsulPort = Integer.valueOf(ConfigUtils.getProperty("tsf_consul_port"));
	      if ((StringUtils.isNotEmpty(tsfConsulIp)) && (tsfConsulPort != null)) {
	        this.currentClient.set(new ConsulClient(tsfConsulIp, tsfConsulPort.intValue()));
	        url.setAddress(tsfConsulIp + ":" + tsfConsulPort);
	      }
	    } catch (Exception ex) {
	      logger.warn("get tsf consul ip port error, ex:", ex);
	    }

	    if (this.currentClient.get() == null) {
	      this.currentClient.set(new ConsulClient(url.getAddress()));
	    }

	    this.checkPeriod = url.getParameter("session", 60000);
	    this.expireFuture = this.expireExecutor.scheduleWithFixedDelay(new Runnable()
	    {
	      public void run() {
	        try {
	          ConsulRegistry.this.check();
	        } catch (Throwable t) {
	          ConsulRegistry.logger.error("Unexpected exception occur at defer expire time, cause: " + t.getMessage(), t);
	        }
	      }
	    }
	    , this.checkPeriod / 2, this.checkPeriod / 2, TimeUnit.MILLISECONDS);
	  }

	  protected void doRegister(URL url)
	  {
	    if ("consumers".equals(url.getParameter("category"))) {
	      return;
	    }

	    AgentClient agent = getConsulClient().agentClient();
	    String serviceId = toServiceId(url);
	    logger.info("registry service id:" + serviceId);
	    long ttl = url.getParameter("dynamic", true) ? this.checkPeriod / 1000 : 9223372036854775807L;
	    try {
	      ImmutableRegCheck check = ImmutableRegCheck.builder().ttl(String.format("%ss", new Object[] { Long.valueOf(ttl) })).build();
	      ImmutableRegistration.Builder builder = ImmutableRegistration.builder();
	      builder.id(serviceId).name(toServiceName(url)).address(url.getHost()).port(url.getPort()).check(check);
	      agent.register(builder.build());
	      KeyValueClient keyValueClient = getConsulClient().keyValueClient();
	      keyValueClient.putValue(toServiceKey(url), URL.encode(url.toFullString()));
	      agent.pass(serviceId);
	      logger.info("register service:" + toServiceName(url) + " service key:" + toServiceKey(url));
	    }
	    catch (ConsulException e) {
	      this.failNum.incrementAndGet();
	      throw new RpcException("Failed register at consul, cause: " + e.getMessage(), e);
	    } catch (Throwable t) {
	      logger.error(t.getMessage(), t);
	      throw new RpcException("Unexpected exception occur at register, cause: " + t.getMessage(), t);
	    }
	  }

	  protected void doUnregister(URL url)
	  {
	    if ("consumers".equals(url.getParameter("category"))) {
	      return;
	    }
	    AgentClient agent = getConsulClient().agentClient();
	    agent.deregister(toServiceId(url));
	    KeyValueClient keyValueClient = getConsulClient().keyValueClient();
	    keyValueClient.deleteKey(toServiceKey(url));
	  }

	  protected void doSubscribe(URL url, NotifyListener listener)
	  {
	    if (url.getParameter("category").contains("providers"))
	    {
	      RpcException exception = null;
	      try {
	        ConcurrentMap cacheConcurrentMap = (ConcurrentMap)this.serviceHealthCaches.get(url.toFullString());
	        if (cacheConcurrentMap == null) {
	          this.serviceHealthCaches.putIfAbsent(url.toFullString(), new ConcurrentHashMap());
	          cacheConcurrentMap = (ConcurrentMap)this.serviceHealthCaches.get(url.toFullString());
	        }

	        String path = toCategoryPath(url);
	        ServiceHealthWrapper serviceHealthCache = (ServiceHealthWrapper)cacheConcurrentMap.get(path);
	        if (serviceHealthCache == null) {
	          serviceHealthCache = new ServiceHealthWrapper(url, path, listener);
	          serviceHealthCache.start();
	          cacheConcurrentMap.putIfAbsent(path, serviceHealthCache);
	        }
	      }
	      catch (Throwable t) {
	        exception = new RpcException("Failed to subscribe service from consul registry. registry: " + getConsulClient().getAddress() + ", service: " + url + ", cause: " + t.getMessage(), t);
	      }
	      if (exception != null) {
	        this.failNum.incrementAndGet();
	        throw exception;
	      }
	    }
	  }

	  protected void doUnsubscribe(URL url, NotifyListener listener)
	  {
	    ConcurrentMap cacheConcurrentMap = (ConcurrentMap)this.serviceHealthCaches.get(url.toFullString());
	    for (Map.Entry entry : cacheConcurrentMap.entrySet()) {
	      ServiceHealthWrapper serviceHealthCache = (ServiceHealthWrapper)entry.getValue();
	      if (serviceHealthCache != null) {
	        try {
	          serviceHealthCache.stop();
	        } catch (Throwable t) {
	          logger.error(t.getMessage(), t);
	        }
	      }
	    }
	    this.serviceHealthCaches.remove(url.toFullString());
	  }

	  public ConsulClient getConsulClient() {
	    return (ConsulClient)this.currentClient.get();
	  }

	  public boolean isAvailable()
	  {
	    if (isAvailable((ConsulClient)this.currentClient.get())) {
	      return true;
	    }
	    return false;
	  }

	  protected void recover() throws Exception
	  {
	    for (Map.Entry entry : this.serviceHealthCaches.entrySet()) {
	      for (Map.Entry wrapperEntry : ((ConcurrentHashMap)entry.getValue()).entrySet()) {
	        ((ServiceHealthWrapper)wrapperEntry.getValue()).stop();
	      }
	    }
	    this.serviceHealthCaches.clear();
	    super.recover();
	  }

	  private String toServicePath(URL url) {
	    String serviceInterface = url.getServiceInterface();
	    return serviceInterface.substring(serviceInterface.lastIndexOf(".") + 1);
	  }

	  private String toCategoryPath(URL url) {
	    return toServicePath(url);
	  }

	  private String toServiceName(URL url) {
	    return toServiceName(toCategoryPath(url));
	  }

	  private String toServiceName(String path) {
	    int i = path.indexOf("/");
	    return path.substring(i + 1).replaceAll("/", "-").replaceAll("\\.", "-");
	  }

	  private String toServiceId(URL url)
	  {
	    return (this.TSF_INSTANCE_ID + "/" + url.toIdentityString()).replaceAll("/", "-");
	  }

	  private String toServiceKey(URL url)
	  {
	    return this.TSF_NAMESPACE_ID + "/" + toCategoryPath(url) + "/" + url.getAddress();
	  }

	  private String toServiceKey(String categoryPath, String host, Integer port) {
	    return this.TSF_NAMESPACE_ID + "/" + categoryPath + "/" + host + ":" + Integer.toString(port.intValue());
	  }

	  protected void check() {
	    logger.info("consul agent ttl check start");
	    AgentClient agent = getConsulClient().agentClient();
	    for (URL url : new HashSet(getRegistered())) {
	      logger.info("consul agent ttl check, url=" + url.toFullString());
	      if (!url.toFullString().startsWith("consumer:")) {
	        String key = toServiceId(url);
	        try {
	          logger.info("consul agent ttl check, service id:" + key);
	          agent.pass(key);
	        } catch (Throwable t) {
	          logger.error(t.getMessage(), t);
	        }
	      } else {
	        logger.info("no check pass consumer");
	      }
	    }
	  }

	  public List<URL> lookup(URL url)
	  {
	    if (url == null) {
	      throw new IllegalArgumentException("lookup url == null");
	    }
	    HealthClient healthClient = getConsulClient().healthClient();
	    List nodes = (List)healthClient.getHealthyServiceInstances(toServiceName(url)).getResponse();
	    KeyValueClient keyValueClient = getConsulClient().keyValueClient();
	    List providers = new ArrayList();
	    for (ServiceHealth node : nodes) {
	      String host = node.getService().getAddress();
	      int port = node.getService().getPort();
	      String path = toCategoryPath(url);
	      providers.add(keyValueClient.getValueAsString(toServiceKey(path, host, Integer.valueOf(port))).get());
	    }
	    return toUrlsWithoutEmpty(url, providers);
	  }

	  private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
	    List urls = new ArrayList();
	    if ((providers != null) && (providers.size() > 0)) {
	      for (String provider : providers) {
	        provider = URL.decode(provider);
	        if (provider.contains("://")) {
	          URL url = URL.valueOf(provider);
	          if (UrlUtils.isMatch(consumer, url)) {
	            urls.add(url);
	          }
	        }
	      }
	    }
	    return urls;
	  }

	  private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
	    List urls = toUrlsWithoutEmpty(consumer, providers);
	    if ((urls == null) || (urls.isEmpty())) {
	      int i = path.lastIndexOf(47);
	      String category = i < 0 ? path : path.substring(i + 1);
	      URL empty = consumer.setProtocol("empty").addParameter("category", category);
	      urls.add(empty);
	    }
	    return urls;
	  }

	  boolean isAvailable(ConsulClient consulClient) {
	    try {
	      consulClient.getConsul().agentClient().ping();
	    } catch (ConsulException e) {
	      return false;
	    }
	    return true;
	  }

	  public String getValue(String key) {
	    KeyValueClient keyValueClient = ((ConsulClient)this.currentClient.get()).keyValueClient();
	    if (keyValueClient.getValueAsString(key).isPresent()) {
	      return (String)keyValueClient.getValueAsString(key).get();
	    }
	    return null;
	  }

	  public void reDo() {
	    try {
	      recover();
	    } catch (Throwable t) {
	      logger.error(t.getMessage(), t);
	    }
	  }

	  class Notifyer
	    implements ConsulCache.Listener<ServiceHealthKey, ServiceHealth>
	  {
	    private String path;
	    private ConsulRegistry.ServiceHealthWrapper wrapper;
	    private boolean hasNotify = false;
	    private List<String> urls = new ArrayList();
	    private Lock lock = new ReentrantLock();

	    public Notifyer(String path, ConsulRegistry.ServiceHealthWrapper wrapper) {
	      this.path = path;
	      this.wrapper = wrapper;
	    }

	    public String getPath() {
	      return this.path;
	    }

	    public boolean isHasNotify() {
	      return this.hasNotify;
	    }

	    public List<String> getUrls() {
	      synchronized (this.urls) {
	        return this.urls;
	      }
	    }

	    public void setHasNotify(boolean hasNotify) {
	      this.hasNotify = hasNotify;
	    }

	    public void notify(Map<ServiceHealthKey, ServiceHealth> map)
	    {
	      ConsulRegistry.logger.info("serviceHealthCache.addListener notify");
	      KeyValueClient keyValueClient;
	      synchronized (this.urls) {
	        this.urls.clear();
	        keyValueClient = ConsulRegistry.this.getConsulClient().keyValueClient();
	        for (Map.Entry entry : map.entrySet()) {
	          String host = ((ServiceHealth)entry.getValue()).getService().getAddress();
	          int port = ((ServiceHealth)entry.getValue()).getService().getPort();
	          String key = ConsulRegistry.this.toServiceKey(this.path, host, Integer.valueOf(port));
	          Optional val = keyValueClient.getValue(key);
	          if (val.isPresent()) {
	            this.urls.add(URL.decode((String)((Value)val.get()).getValueAsString().get()));
	          }
	          ConsulRegistry.logger.info("subscribe service:" + ConsulRegistry.this.toServiceName(this.path) + " service key:" + key + " adress: " + host + ":" + port);
	        }
	      }
	      this.wrapper.trigger(this, this.urls);
	    }
	  }

	  class ServiceHealthWrapper
	  {
	    private URL url;
	    private NotifyListener listener;
	    private ServiceHealthCache serviceHealthCache;
	    private ConsulRegistry.Notifyer currentNotifyer = null;
	    private boolean needSwitch = false;
	    private AtomicLong initCount = new AtomicLong(0L);

	    public ServiceHealthWrapper(URL url, String path, NotifyListener listener) {
	      this.url = url;
	      this.listener = listener;
	      ServiceHealthCache serviceHealthCache = ServiceHealthCache.newCache(ConsulRegistry.this.getConsulClient().healthClient(), ConsulRegistry.this
	        .toServiceName(path), 
	        true, ImmutableQueryOptions.BLANK, 10);
	      serviceHealthCache.addListener(new ConsulRegistry.Notifyer(ConsulRegistry.this, path, this));
	      this.serviceHealthCache = serviceHealthCache;
	    }

	    public void trigger(ConsulRegistry.Notifyer notifyer, List<String> urls) {
	      synchronized (this) {
	        this.initCount.incrementAndGet();
	        if (this.serviceHealthCache != null) {
	          if (urls.isEmpty()) {
	            this.needSwitch = true;
	          } else {
	            this.needSwitch = false;
	            this.currentNotifyer = null;
	            ConsulRegistry.this.notify(this.url, this.listener, ConsulRegistry.access$300(ConsulRegistry.this, this.url, notifyer.getPath(), urls));
	          }
	        }
	        if ((this.needSwitch) && 
	          (this.currentNotifyer != null) && (!this.currentNotifyer.getUrls().isEmpty()) && 
	          (notifyer == this.currentNotifyer))
	          ConsulRegistry.this.notify(this.url, this.listener, ConsulRegistry.access$300(ConsulRegistry.this, this.url, notifyer.getPath(), this.currentNotifyer.getUrls()));
	      }
	    }

	    public boolean hasNotify()
	    {
	      return this.initCount.get() > 0L;
	    }

	    public void start() {
	      try {
	        this.serviceHealthCache.start();
	        int waitSecond = ConsulRegistry.this.checkPeriod * ConsulRegistry.this.checkFailedNum / 1000;
	        this.serviceHealthCache.awaitInitialized(waitSecond, TimeUnit.SECONDS);
	        if (!hasNotify()) {
	          stop();
	          throw new RpcException("Failed to wait consul notify in " + waitSecond + " seconds");
	        }
	      } catch (Throwable t) {
	        stop();
	        throw new RpcException(t.getMessage(), t);
	      }
	    }

	    public void stop() {
	      try {
	        this.serviceHealthCache.stop();
	      } catch (Throwable t) {
	        throw new RpcException(t.getMessage(), t);
	      }
	    }
	  }

	  class ConsulClient
	  {
	    private String host;
	    private int port;
	    private Consul consul = null;

	    public ConsulClient(String host, int port) {
	      this.host = host;
	      this.port = port;
	      this.consul = Consul.builder().withHostAndPort(HostAndPort.fromString(getAddress()))
	        .withConnectTimeoutMillis(2000L)
	        .withReadTimeoutMillis(20000L).withPing(false).withAclToken(ConsulRegistry.this.TSF_ACL_TOKEN).build();
	    }

	    public ConsulClient(String address) {
	      String addr = address;
	      int i = addr.indexOf(':');
	      if (i > 0) {
	        this.host = addr.substring(0, i);
	        this.port = Integer.parseInt(addr.substring(i + 1));
	      } else {
	        this.host = addr;
	      }
	      this.consul = Consul.builder().withHostAndPort(HostAndPort.fromString(getAddress()))
	        .withConnectTimeoutMillis(2000L)
	        .withReadTimeoutMillis(20000L).withPing(false).withAclToken(ConsulRegistry.this.TSF_ACL_TOKEN).build();
	    }

	    public String getHost() {
	      return this.host;
	    }

	    public void setHost(String host) {
	      this.host = host;
	    }

	    public int getPort() {
	      return this.port;
	    }

	    public void setPort(int port) {
	      this.port = port;
	    }

	    public Consul getConsul() {
	      return this.consul;
	    }

	    public String getAddress() {
	      return this.host + ":" + Integer.toString(this.port);
	    }

	    public AgentClient agentClient() {
	      return this.consul.agentClient();
	    }

	    public CatalogClient catalogClient() {
	      return this.consul.catalogClient();
	    }

	    public KeyValueClient keyValueClient() {
	      return this.consul.keyValueClient();
	    }

	    public HealthClient healthClient() {
	      return this.consul.healthClient();
	    }
	  }
	}
*/