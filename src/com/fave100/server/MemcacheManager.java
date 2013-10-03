package com.fave100.server;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.fave100.server.domain.favelist.FaveItem;
import com.fave100.server.domain.favelist.FaveList;

public class MemcacheManager {

	public static final String SEPARATOR_TOKEN = ":";
	public static final String FAVEITEM_RANK_NAMESPACE = "faveItemRank";
	public static final String MASTER_FAVELIST_NAMESPACE = "masterFaveList";

	private static MemcacheManager _instance;
	private Cache _cache;

	private MemcacheManager(final Cache cache) {
		setCache(cache);
	}

	public static MemcacheManager getInstance() {
		if (_instance == null) {
			try {
				final CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
				_instance = new MemcacheManager(cacheFactory.createCache(Collections.emptyMap()));
			}
			catch (final Exception e) {
				Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
			}
		}
		return _instance;
	}

	public double getFaveItemScore(final String id, final String hashtag) {
		// Need to store in intermediate object, since casting a null results in NPE
		final Object cacheItem = _cache.get(FAVEITEM_RANK_NAMESPACE + SEPARATOR_TOKEN + hashtag.toLowerCase() + SEPARATOR_TOKEN + id);
		return (cacheItem == null) ? 0 : (double)cacheItem;
	}

	/**
	 * Inserts or updates a FaveItem score in Memcache, but does not rerank the item. Use when the rank is already known (i.e. when
	 * initializing Memcache)
	 * 
	 * @param id
	 * @param hashtag
	 * @param score
	 */
	public void putFaveItemScoreNoRerank(final String id, final String hashtag, final double score) {
		_cache.put(FAVEITEM_RANK_NAMESPACE + SEPARATOR_TOKEN + hashtag.toLowerCase() + SEPARATOR_TOKEN + id, score);
	}

	/**
	 * Inserts or updates a FaveItem score in Memcache and reranks the item, inserting it in the top 100 if required.
	 * 
	 * @param id
	 * @param hashtag
	 * @param score
	 */
	public void putFaveItemScore(final String id, final String hashtag, final double score, final FaveItem passedFaveItem) {
		// e.g. {faveItemRank:rock2013:645116, 245}
		_cache.put(FAVEITEM_RANK_NAMESPACE + SEPARATOR_TOKEN + hashtag.toLowerCase() + SEPARATOR_TOKEN + id, score);

		// If it now belongs to master list for hashtag, update master
		final List<FaveItem> master = getMasterFaveList(hashtag);
		if (master == null)
			return;

		double targetScore = 0;
		int rank = master.size() + 1;
		FaveItem existingFave = null;

		// Check for dupe, and rerank
		for (int i = master.size() - 1; i > 0; i--) {
			final String targetId = master.get(i).getId();
			targetScore = getFaveItemScore(targetId, hashtag);
			if (score > targetScore) {
				rank = i;
			}

			if (id.equals(targetId)) {
				existingFave = master.get(i);
			}
		}

		if (rank < FaveList.MAX_FAVES) {
			FaveItem faveItem = existingFave;
			// Fave not in master yet, insert new fave
			if (faveItem == null) {
				faveItem = passedFaveItem;
			}
			// Fave already in master, adjust position
			else {
				master.remove(existingFave);
			}

			if (rank < master.size()) {
				master.add(rank, faveItem);
			}
			else if (score > 0) {
				master.add(faveItem);
			}
			
			// If it was inserted into middle of master, may need to remove last item to fit under limit
			while(master.size() > FaveList.MAX_FAVES) {
				master.remove(master.size()-1);
			}

			putMasterFaveList(hashtag, master);
		}

	}

	public void modifyFaveItemScore(final String id, final String hashtag, final double delta, final FaveItem faveItem) {
		// TODO: Aug 19 2013: Is it possible to increment counter atomically? Not super important, but nice
		final double currentRank = getFaveItemScore(id, hashtag);
		putFaveItemScore(id, hashtag, currentRank + delta, faveItem);
	}

	@SuppressWarnings("unchecked")
	public List<FaveItem> getMasterFaveList(final String hashtag) {
		// Need to store in intermediate object, since casting a null results in NPE
		final Object cacheItem = _cache.get(MASTER_FAVELIST_NAMESPACE + SEPARATOR_TOKEN + hashtag.toLowerCase());
		return cacheItem == null ? null : (List<FaveItem>)cacheItem;
	}

	public void putMasterFaveList(final String hashtag, final List<FaveItem> list) {
		_cache.put(MASTER_FAVELIST_NAMESPACE + SEPARATOR_TOKEN + hashtag.toLowerCase(), list);
	}

	/* Getters and Setters */

	public Cache getCache() {
		return _cache;
	}

	public void setCache(final Cache cache) {
		_cache = cache;
	}

}
