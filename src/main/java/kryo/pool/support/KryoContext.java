package kryo.pool.support;

/**
 * Created by mykidong on 2018-06-07.
 */
public interface KryoContext {

    byte[] serialze(Object obj);

    byte[] serialze(Object obj, int bufferSize);

    Object deserialze(Class clazz, byte[] serialized);
}
