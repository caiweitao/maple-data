package com.gzyouai.hummingbird.data.test.cache;

import com.gzyouai.hummingbird.data.domain.MarkableMap;

/**
 * @author 蔡伟涛
 * @Date 2021年4月30日
 * @Description 
 */
public class Items extends MarkableMap<Integer, Item,Integer>{

	public Items(Integer playerId) {
		super(playerId);
	}

	@Override
	public String toString() {
		return "Items [getMap()=" + getMap() + "]";
	}

	
}
