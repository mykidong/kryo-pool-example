package kryo.pool.example.dao.redis;

import java.util.HashMap;
import java.util.Map;

import kryo.pool.example.api.dao.ItemDao;
import kryo.pool.example.dao.redis.support.BaseRedisDao;
import kryo.pool.support.KryoClassRegistrator;
import kryo.pool.support.KryoContext;
import kryo.pool.support.DefaultKryoContext;
import redis.clients.jedis.ShardedJedis;

import com.esotericsoftware.kryo.Kryo;


public class RedisItemDao extends BaseRedisDao implements ItemDao {
	
	private KryoContext kryoContext = DefaultKryoContext.newKryoContextFactory(new KryoClassRegistrator(){
		@Override
		public void register(Kryo kryo) {
			kryo.register(HashMap.class);	
		}		
	});

	@Override
	public void addItem(String key, Map<String, Object> value) {
		ShardedJedis jedis = this.shardedJedisPool.getResource();		
		
		jedis.set(key.getBytes(), kryoContext.serialze(value));		
		
		// expiration: 2h.
		jedis.expire(key.getBytes(), 60 * 60 * 2);
		
		this.shardedJedisPool.returnResource(jedis);
	}

	@Override
	public Map<String, Object> getItem(String key) {
		
		ShardedJedis jedis = this.shardedJedisPool.getResource();
		
		byte[] valueBytes = jedis.get(key.getBytes());
		
		this.shardedJedisPool.returnResource(jedis);	
		
		Object obj = (valueBytes != null) ? kryoContext.deserialze(HashMap.class, valueBytes) : null;
		
		return (obj != null) ? (Map<String, Object>)obj : null;
	}
}
