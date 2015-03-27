# kryo-pool-example
Because Kryo instance is not thread safe, in multithreads environment, kryo pooling should be used(https://github.com/EsotericSoftware/kryo#pooling-kryo-instances).

This example shows the usage of kryo pooling and how to serialize/deserialize java objects in redis in multithreads env.

## Kryo Pooling Context
To get kryo pool with class registration:

    import kryo.pool.support.KryoClassRegistrator;
    import kryo.pool.support.KryoContext;

    // kryo pool context.
    KryoContext kryoContext = KryoContext.newKryoContextFactory(new KryoClassRegistrator(){
    	@Override
    	public void register(Kryo kryo) {
    		kryo.register(ArrayList.class);	
    		kryo.register(HashMap.class);	
    	}		
    });
  

To serialize java objects:

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
  
  	
Default Buffer size is 100KB, buffer size can be changed while serializing:
    
    int moreBuffer = 200 * 1024;
    byte[] listBytes = kryoContext.serialze(list, moreBuffer);
    


To deserialize:

    // deserialize list.
  	List<Map<String, Object>> retList = 
  	      (List<Map<String, Object>>)kryoContext.deserialze(ArrayList.class, listBytes);
  	      
  	// deserialize user list.
	List<User> retUserList = (List<User>)kryoContext.deserialze(ArrayList.class, userListBytes);
  	
  	// deserialize map.
  	Map<String, Object> retMap = 
  	    (Map<String, Object>)kryoContext.deserialze(HashMap.class, mapBytes);
  		
  	// deserialize user object.
  	User retUser = (User)kryoContext.deserialze(User.class, userBytes);


To check kryo thread safety with kro pooling, run the following with multiple threads:

    mvn -e -Dtest=KryoPoolingTest#checkKryoThreadSafetyWithKryoPool test;
    

## Redis Example
Assumed that all your applications which have access to redis are written in java, java obects can be serialized in kryo, and it can be put/get into/from redis.

Before run the redis example, edit redis hosts in application-context-test.properties in test resource:

    # redis hosts.
    redis.hosts=redis001:6379,redis002:6379,redis003:6379,redis004:6379



To run this redis example:

    mvn -e -Dtest=KryoPoolingTest#redisExampleTest test;
