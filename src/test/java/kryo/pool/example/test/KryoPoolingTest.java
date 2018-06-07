package kryo.pool.example.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kryo.pool.example.api.dao.ItemDao;
import kryo.pool.support.KryoClassRegistrator;
import kryo.pool.support.KryoContext;
import kryo.pool.support.DefaultKryoContext;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.esotericsoftware.kryo.Kryo;

public class KryoPoolingTest {
	
	
	@Test
	public void checkKryoThreadSafetyWithKryoPool()
	{		
		// kryo pool factory context.
		KryoContext kryoContext = DefaultKryoContext.newKryoContextFactory(new KryoClassRegistrator(){
			@Override
			public void register(Kryo kryo) {
				kryo.register(ArrayList.class);	
				kryo.register(HashMap.class);	
			}		
		});
				
		// run multiple threads.
		runExecutor(new KryoWorkerThread(kryoContext));	
	}
	
	
	private static class KryoWorkerThread implements Runnable
	{
		private int MAX = 1000;

		private ObjectMapper mapper = new ObjectMapper();
		
		private KryoContext kryoContext;
		
		public KryoWorkerThread(KryoContext kryoContext)
		{
			this.kryoContext = kryoContext;
		}
	
		@Override
		public void run() {			
			
			for(int i = 0; i < MAX; i++)
			{
				// ================ serialization ===========================				
				List<Map<String, Object>> list = new ArrayList<>();
				for(int k = 0; k < 3; k++)
				{
					Map<String, Object> map = new HashMap<>();					
					map.put("any-prop1", "any-value1-" + k);
					map.put("any-prop2", "any-value2-" + k);
					map.put("any-prop3", "any-value3-" + k);
					
					list.add(map);
				}
				
				// serialize list.
				byte[] listBytes = kryoContext.serialze(list);
				
				List<User> userList = new ArrayList<>();
				for(int k = 0; k < 3; k++)
				{
					User user = new User();
					user.setName("any-name" + k);
					user.setAge(50 + k);
					user.setAddress("any-address..." + k);
					
					userList.add(user);
				}
				
				// serialize user list.
				byte[] userListBytes = kryoContext.serialze(userList);
				
				
				Map<String, Object> map = new HashMap<>();				
				map.put("any-prop1", "any-value1");
				map.put("any-prop2", "any-value2");
				map.put("any-prop3", "any-value3");
				
				// serialize map.
				byte[] mapBytes = kryoContext.serialze(map);
			
				User user = new User();
				user.setName("any-name");
				user.setAge(50);
				user.setAddress("any-address...");
				
				// serialize user object.
				byte[] userBytes = kryoContext.serialze(user);

				
				// ================ de-serialization ===========================
				// deserialize list.
				List<Map<String, Object>> retList = (List<Map<String, Object>>)kryoContext.deserialze(ArrayList.class, listBytes);
				
				// deserialize user list.
				List<User> retUserList = (List<User>)kryoContext.deserialze(ArrayList.class, userListBytes);
				
				// deserialize map.
				Map<String, Object> retMap = (Map<String, Object>)kryoContext.deserialze(HashMap.class, mapBytes);
				
				// deserialize user object.
				User retUser = (User)kryoContext.deserialze(User.class, userBytes);
				
				
				try {				
					System.out.println("retList: [" + mapper.writeValueAsString(retList) + "]");
					
					System.out.println("retUserList: [" + mapper.writeValueAsString(retUserList) + "]");
					
					System.out.println("retMap: [" + mapper.writeValueAsString(retMap) + "]");
					
					System.out.println("retUser: [" + mapper.writeValueAsString(retUser) + "]");
				} catch (Exception e) {					
					e.printStackTrace();
				} 		
			}			
		}		
	}
	
	private static class User
	{
		private String name;
		
		private int age;
		
		private String address;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}		
	}
	
	private void runExecutor(Runnable r)
	{
		ExecutorService executor = Executors.newFixedThreadPool(20);
		
	    for (int i = 0; i < 40; i++) {	        
	        executor.execute(r);
	    }
	    
	    executor.shutdown();
	    
	    while (!executor.isTerminated()) {
	    }
	    System.out.println("all threads finished...");
	}
	
	
	@Test
	public void redisExampleTest()
	{
		String[] configLocations = new String[]{
				"classpath:/META-INF/spring/kryo-pool-example/spring-application-context-test.xml",
				"classpath:/META-INF/spring/kryo-pool-example/spring-dao-redis-context-test.xml"
		       };
		
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(configLocations);
		
		ItemDao itemDao = applicationContext.getBean("redis.itemDao", ItemDao.class);
		
		// run multiple threads.
		runExecutor(new ItemWorkerThread(itemDao));	
	}
	
	private static class ItemWorkerThread implements Runnable
	{
		private int MAX = 1000;
		
		private ItemDao itemDao;

		public ItemWorkerThread(ItemDao itemDao)
		{			
			this.itemDao = itemDao;
		}
		
		@Override
		public void run() {
			for(int i = 0; i < MAX; i++)
			{
				String key = "item:item-id-" + Thread.currentThread().getId() + "-" + i;

				Map<String, Object> map = new HashMap<String, Object>();			
				map.put("itemTitle", "any-item-title" + i);				
				map.put("price", 10000 + i);
			
				this.itemDao.addItem(key, map);	
			}
			
			for(int i = 0; i < MAX; i++)
			{
				String key = "item:item-id-" + Thread.currentThread().getId() + "-" + i;
				
				Map<String, Object> retValue = this.itemDao.getItem(key);
				Assert.assertTrue(retValue != null);
			
				try {
					String json = new ObjectMapper().writeValueAsString(retValue);
					System.out.println("json: [" + json + "]");
				} catch (Exception e) {					
					e.printStackTrace();
				} 				
			}			
		}		
	}
}
