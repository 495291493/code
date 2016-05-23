package com.clschina.common.db;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class GiftDataCache<T> {
	private static Log log = LogFactory.getLog(GiftDataCache.class);

	protected List<T> list;
	protected Date lastRefersh;
	
	private long refershPeriod = 1800000; //默认至少3小时刷新一次。
	private long norefershPeriod = 10000; //10秒内，一定不刷新。
	
	/**
	 * 手动刷新缓存
	 */
	public void refersh(){
		if(lastRefersh != null && System.currentTimeMillis() - lastRefersh.getTime() <= norefershPeriod){
			//不够最短刷新间隔
			return;
		}
		list = loadData();
		lastRefersh = new Date();
	}
	
	/**
	 * 取数据，需要覆盖此方法，实现从数据库读取数据，并返回List
	 * @return
	 */
	protected abstract List<T> loadData();
	
	/**
	 * 取得缓存数据
	 * @return
	 */
	public List<T> getData(){
		if(list == null){
			if(log.isTraceEnabled()){
				log.trace("数据为空，刷新。");
			}
			refersh();
		}else if(lastRefersh == null || System.currentTimeMillis() - lastRefersh.getTime() > refershPeriod){
			if(log.isTraceEnabled()){
				log.trace("数据超过保存时限，刷新。");
			}
			refersh();
		}
		return list;
	}
	
	/**
	 * 清空缓存
	 */
	public void clearCache(){
		list = null;
		lastRefersh = null;
	}

	/**
	 * @return the 强制刷新时间间隔，单位毫秒
	 */
	public long getRefershPeriod() {
		return refershPeriod;
	}

	/**
	 * @param refershPeriod 强制刷新时间间隔，单位毫秒
	 */
	public void setRefershPeriod(long refershPeriod) {
		this.refershPeriod = refershPeriod;
	}
	
	/**
	 * 返回最后刷新时间
	 * @return
	 */
	public Date getLastRefershDate(){
		return lastRefersh;
	}
	
	protected abstract Entry<String, Object> element2Entry(T t);
	
	public Map<String, Object> getCacheMap(){
		return new AbstractMap<String, Object>(){

			@Override
			public Set<Entry<String, Object>> entrySet() {
				Set<Entry<String, Object>> set = new HashSet<Entry<String, Object>>();
				if(list == null){
					refersh();
				}
				if(list != null){
					for(int i=0; i<list.size(); i++){
						set.add(element2Entry(list.get(i)));
					}
				}
				return set;
			}

			/* (non-Javadoc)
			 * @see java.util.AbstractMap#get(java.lang.Object)
			 */
			@Override
			public Object get(Object key) {
				Object v = super.get(key);
				if(v == null){
					refersh();
				}
				v = super.get(key);
				return v;
			}
		};
	}
}
