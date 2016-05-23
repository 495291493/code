package com.clschina.common.jsf;

import java.io.Serializable;
import java.util.List;

/**
 * 将需要的页的数据封装到一个DataPage中去， 这个类表示了我们需要的一页的数据，<br>
 * 里面包含有三个元素：datasetSize，startRow，和一个用于表示具体数据的List。<br>
 * datasetSize表示了这个记录集的总条数，查询数据的时候，使用同样的条件取count即可，<br>
 * startRow表示该页的起始行在数据库中所有记录集中的位置
 */
public class DataPage<T> implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -754112760637076642L;
	private int datasetSize;
    private int startRow;
    private List<T> data;
    
    /**
     * 
     * @param datasetSize 数据库中记录的总数（注意，不是data.size()）
     * @param startRow    data在数据库中的位置
     * @param data
     */
    public DataPage(int datasetSize, int startRow, List<T> data) {
        this.datasetSize = datasetSize;
        this.startRow = startRow;
        this.data = data;
     }
    
    /**
     * Return the number of items in the full dataset.
     */
    public int getDatasetSize() {
      return datasetSize;
    }

    /**
     * Return the offset within the full dataset of the first
     * element in the list held by this object.
     */
    public int getStartRow() {
      return startRow;
    }

    /**
     * Return the list of objects held by this object, which
     * is a continuous subset of the full dataset.
     */
    public List<T> getData() {
      return data;
    }
    
}
