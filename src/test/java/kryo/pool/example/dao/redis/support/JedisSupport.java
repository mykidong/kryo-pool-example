package kryo.pool.example.dao.redis.support;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisShardInfo;

public class JedisSupport {

	private static Logger log = LoggerFactory.getLogger(JedisSupport.class);

	public static List<JedisShardInfo> buildShardInfo(List<String> shards,
			int timeout) {
		List<JedisShardInfo> shardsList = new ArrayList<JedisShardInfo>();
		for (String shard : shards) {
			log.info("shard: [" + shard + "]");

			String[] token = shard.split(":");
			String host = token[0];
			int port = Integer.valueOf(token[1]);

			JedisShardInfo si = new JedisShardInfo(host, port, timeout);
			shardsList.add(si);
		}
		return shardsList;
	}

}
