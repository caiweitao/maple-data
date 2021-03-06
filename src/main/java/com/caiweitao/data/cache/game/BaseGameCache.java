package com.caiweitao.data.cache.game;

import java.util.Collection;

import com.caiweitao.data.exception.CacheSelectException;

/**
 * @author caiweitao
 * @Date 2021年7月7日
 * @Description 
 */
public abstract class BaseGameCache<K,V> {

	protected String name;
	
	public abstract V get(K key);
	
	public abstract void put (K k,V v);

	public abstract Collection<K> keys();
	
	public abstract boolean containKey (K k);
	
	public abstract int size();
	
	public abstract void clear();
	
	public abstract V without (K playerId) throws CacheSelectException;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
