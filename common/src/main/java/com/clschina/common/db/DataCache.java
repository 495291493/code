package com.clschina.common.db;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.StringUtil;


public abstract class DataCache<T> implements Serializable{
	private static final long serialVersionUID = 8340880533733124338L;

	private static Log log = LogFactory.getLog(DataCache.class);

	protected List<T> list;
	protected Date lastRefersh;
	private boolean returnIdIfNotExists = false;

    protected abstract Entry<String, String> element2Entry(T t);
    
    private long refershPeriod = 3600000; //默认至少1小时刷新一次。
	private long norefershPeriod = 10000; //10秒内，一定不刷新。
	
	/**
	 * 手动刷新缓存
	 */
	public  synchronized void refersh(){
		if(lastRefersh != null && System.currentTimeMillis() - lastRefersh.getTime() <= norefershPeriod){
			//不够最短刷新间隔
			return;
		}
		List<T> l = loadData();
		list = l;
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
	
 
    public boolean isReturnIdIfNotExists() {
        return returnIdIfNotExists;
    }

    public void setReturnIdIfNotExists(boolean returnIdIfNotExists) {
        this.returnIdIfNotExists = returnIdIfNotExists;
    }
    
	public Map<String, String> getCacheMap(){
		return new DataMap<String, String>(){
            private static final long serialVersionUID = -8192660269865244690L;


            @Override
			public Set<Entry<String, String>> entrySet() {
				Set<Entry<String, String>> set = new HashSet<Entry<String, String>>(); 
				List<T> list = getData();
				if(list != null){
					for(int i=0; i<list.size(); i++){
						set.add(element2Entry(list.get(i)));
					}
				}
				return set;
			}

			 
			@Override
			public String get(Object key) {
				String v = super.get(key);
				if(v == null){
					refersh();
				}
				v = super.get(key);
				if(returnIdIfNotExists && StringUtil.isNullOrEmpty(v)){
				    v = "[" + key.toString() + "]";
				}
				return v;
			}
		};
	}
	

}

/**
 * 供getCacheMap用
 *
 * @param <T>
 * @param <T1>
 */
abstract class DataMap<T, T1> extends AbstractMap<T, T> implements Serializable{
    private static final long serialVersionUID = -69837093743406725L;
    
}
