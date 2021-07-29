package com.caiweitao.data.test.cache;

import com.caiweitao.data.cache.game.GameCache;
import com.caiweitao.data.db.dao.BaseDao;
import com.caiweitao.data.test.Equip;
import com.caiweitao.data.test.db.DaoFactory;

/**
 * @author 蔡伟涛
 * @Date 2021年5月24日
 * @Description 
 */
public class EquipCache extends GameCache<String, Equip> {

	public EquipCache(String name) {
		super(name);
	}

	@Override
	public Equip without(String playerId) {
		return getDao().selectByKey(playerId);
	}

	@Override
	public BaseDao<String, Equip> getDao() {
		return DaoFactory.equipDao;
	}

}