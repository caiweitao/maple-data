package com.caiweitao.data.test.cache;

import com.caiweitao.data.cache.game.GameCache;
import com.caiweitao.data.db.mysql.dao.BaseDao;
import com.caiweitao.data.test.db.DaoFactory;

/**
 * @author caiweitao
 * @Date 2021年4月28日
 * @Description 
 */
public class PlayerCache extends GameCache<Integer, Player> {

	public PlayerCache(String name) {
		super(name);
	}

	@Override
	public Player without(Integer playerId) {
		return getDao().selectByKey(playerId);
	}

	@Override
	public BaseDao<Integer,Player> getDao() {
		return DaoFactory.playerDao;
	}
}
