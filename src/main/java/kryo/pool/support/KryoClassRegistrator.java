package kryo.pool.support;

import com.esotericsoftware.kryo.Kryo;

public interface KryoClassRegistrator {
	
	public void register(Kryo kryo);

}
