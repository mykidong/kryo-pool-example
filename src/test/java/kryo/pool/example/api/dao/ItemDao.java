package kryo.pool.example.api.dao;

import java.util.Map;

public interface ItemDao {
	
	public void addItem(String key, Map<String, Object> value);
	
	public Map<String, Object> getItem(String key);

}
