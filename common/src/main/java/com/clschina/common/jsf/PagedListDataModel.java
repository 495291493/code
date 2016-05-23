package com.clschina.common.jsf;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A special type of JSF DataModel to allow a datatable and datascroller
 * to page through a large set of data without having to hold the entire
 * set of data in memory at once.
 * <p>
 * Any time a managed bean wants to avoid holding an entire dataset,
 * the managed bean should declare an inner class which extends this
 * class and implements the fetchData method. This method is called
 * as needed when the table requires data that isn't available in the
 * current data page held by this object.
 * <p>
 * This does require the managed bean (and in general the business
 * method that the managed bean uses) to provide the data wrapped in
 * a DataPage object that provides info on the full size of the dataset.
 */
public abstract class PagedListDataModel<T> extends DataModel<Object> implements Serializable{ 
	/**
	 * 
	 */
	private static final long serialVersionUID = 2199300814009938034L;
	private static final Log log = LogFactory.getLog(PagedListDataModel.class);
    private int pageSize;
    private int rowIndex;
    private DataPage<T> page;
    private HashMap<String, Object[]> cache = new HashMap<String, Object[]>();
    
    /**
     * Create a datamodel that pages through the data showing the specified
     * number of rows on each page.
     */
    public PagedListDataModel(int pageSize) {
        super();
        this.pageSize = pageSize;
        this.rowIndex = -1;
        this.page = null;
    }

    /**
     * Not used in this class; data is fetched via a callback to the
     * fetchData method rather than by explicitly assigning a list.
     */
    @Override
    public void setWrappedData(Object o) {
        throw new UnsupportedOperationException("setWrappedData");
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Specify what the "current row" within the dataset is. Note that
     * the UIData component will repeatedly call this method followed
     * by getRowData to obtain the objects to render in the table.
     */
    @Override
    public void setRowIndex(int index) {
        rowIndex = index;
    }

    /**
     * Return the total number of rows of data available (not just the
     * number of rows in the current page!).
     */
    @Override
    public int getRowCount() {
        return getPage().getDatasetSize();
    }
    
    /**
     * Return a DataPage object; if one is not currently available then
     * fetch one. Note that this doesn't ensure that the datapage
     * returned includes the current rowIndex row; see getRowData.
     */
    private DataPage<T> getPage() {
        if (page != null){
            return page;
        }
        
        int rowIndex = getRowIndex();
        int startRow = rowIndex;
        if (rowIndex == -1) {
            // even when no row is selected, we still need a page
            // object so that we know the amount of data available.
           startRow = 0;
        }

        // invoke method on enclosing class
        page = fetchPageFromCache(startRow, pageSize);
        return page;
    }

    /**
     * Return the object corresponding to the current rowIndex.
     * If the DataPage object currently cached doesn't include that
     * index then fetchPage is called to retrieve the appropriate page.
     */
    @Override
    public Object getRowData(){
        if (rowIndex < 0) {
            throw new IllegalArgumentException(
                "Invalid rowIndex for PagedListDataModel; not within page");
        }

        // ensure page exists; if rowIndex is beyond dataset size, then 
        // we should still get back a DataPage object with the dataset size
        // in it...
        if (page == null) {
            page = fetchPageFromCache(rowIndex, pageSize);
        }

//        if (rowIndex == page.getStartRow()){
//        	if(log.isTraceEnabled()){
//        		log.trace("rowIndex == page.getStartRow(), will retrieve data...");
//        	}
//            page = fetchPageFromCache(rowIndex, pageSize);
//        }

        int datasetSize = page.getDatasetSize();
        int startRow = page.getStartRow();
        int nRows =page.getData() == null ? 0 : page.getData().size();
        int endRow = startRow + nRows;
        
        if (rowIndex >= datasetSize) {
            throw new IllegalArgumentException("Invalid rowIndex");
        }
        if(log.isTraceEnabled()){
        	log.trace("rowIndex=" + rowIndex + "; startRow=" + startRow + "; endRow=" + endRow + "; rows=" + nRows + "; datasetSize=" + datasetSize);
        }
        if (rowIndex < startRow) {
            page = fetchPageFromCache(rowIndex, pageSize);
            startRow = page.getStartRow();
        } else if (rowIndex >= endRow) {
            page = fetchPageFromCache(rowIndex, pageSize);
            startRow = page.getStartRow();
        }
        
        return page.getData() == null ? null : page.getData().get(rowIndex - startRow);
    }

    @Override
    public Object getWrappedData() {
        return page.getData();
    }

    /**
     * Return true if the rowIndex value is currently set to a
     * value that matches some element in the dataset. Note that
     * it may match a row that is not in the currently cached 
     * DataPage; if so then when getRowData is called the
     * required DataPage will be fetched by calling fetchData.
     */
    @Override
    public boolean isRowAvailable() {
        DataPage<T> page = getPage();
        if (page == null){
            return false;
        }
        
        int rowIndex = getRowIndex();
        if (rowIndex < 0) {
            return false;
        } else if (rowIndex >= page.getDatasetSize()) {
            return false;
        } else {
            return true;
        }
    }
    
    public void clearCache(){
        cache.clear();
    }
    
    @SuppressWarnings("unchecked")
	private DataPage<T> fetchPageFromCache(int startRow, int pageSize){
     	if(log.isTraceEnabled()){
			log.trace("fetchPageFromCache(" + startRow + "," + pageSize + ")");
		}
       	DataPage<T> dp = null;
       	String key = startRow + "-" + pageSize;
    	Object[] ary = cache.get(key);
    	if(ary != null){
    		Date d = (Date) ary[0];
    		if(System.currentTimeMillis() - d.getTime() <= 60000){
    			//未超过1分钟，使用缓存数据。
    			if(log.isTraceEnabled()){
    				log.trace("使用缓存的DataPage");
    			}
    			dp = (DataPage<T>) ary[1];
    		}else{
    			if(log.isTraceEnabled()){
    				log.trace("缓存数据过期，" + d.getTime() + " vs " + System.currentTimeMillis());
    			}
    		}
    	}
    	
    	FacesContext context = FacesContext.getCurrentInstance();
    	if(log.isTraceEnabled()){
    		log.trace("fetchPageFromCache ... " + context.getCurrentPhaseId());
    	}
//    	if(!(context.getCurrentPhaseId().equals(PhaseId.UPDATE_MODEL_VALUES) || 
//    			context.getCurrentPhaseId().equals(PhaseId.RENDER_RESPONSE) || 
//    	    			context.getCurrentPhaseId().equals(PhaseId.PROCESS_VALIDATIONS)
//    			)){
//    		if(log.isTraceEnabled()){
//    			log.trace("not UPDATE_MODEL_VALUES or RENDER_RESPONSE or PROCESS_VALIDATIONS, return null instead.");
//    		}
//    		return new DataPage<T>(0, startRow, null){};
//    	}
    	if(dp == null){
    		dp = fetchPage(startRow, pageSize);
    		if(log.isTraceEnabled()){
				log.trace("缓存未找到，从数据库中取。");
			}
    		ary = new Object[2];
    		ary[0] = new Date();
    		ary[1] = dp;
    		cache.put(key, ary);
    	}
    	return dp;
    }

    /**
     * Method which must be implemented in cooperation with the
     * managed bean class to fetch data on demand.
     */
    public abstract DataPage<T> fetchPage(int startRow, int pageSize);
    
}

