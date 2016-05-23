package com.clschina.common.backingbean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;


/**
 * 用来和控件binding
 * 例如 <h:dataTable binding="backingBeanForBinding.comp">
 *      <h:column>#{backingBeanForBinding.comp.rowIndex}</h:column>
 *  来显示当前是第几行
 *  
 *  如果dataTable是在某个循环中，则需要在dataTable里  binding="#{backingBeanForBinding.compMap[loop.id]}"
 *  例如：
 *  <c:forEach var="item" value="....">
 *     <h:dataTable binding="#{backingBeanForBinding.compMap[item.id]" ...>
 *          <h:column>#{backingBeanForBinding.compMap[item.id].rowIndex</h:column>
 *         ...
 *       </h:dataTable>
 *  </c:forEach>
 * @author gexiangdong
 *
 */

public class BackingBeanForBinding implements Serializable {

	private static final long serialVersionUID = -1974406741986466872L;
	private UIComponent comp;
	private UIComponent comp2;
	private UIComponent comp3;
	private UIComponent comp4;
	private UIComponent comp5;

	private Map<String, UIComponent> map;
	
	public  Map<String, UIComponent> getCompMap(){
	    if(map == null){
	        map = new HashMap<String, UIComponent>();
	    }
	    return map;
	}
	
	
	/**
	 * @return the comp
	 */
	public UIComponent getComp() {
		return comp;
	}

	/**
	 * @param comp the comp to set
	 */
	public void setComp(UIComponent comp) {
		this.comp = comp;
	}

	/**
	 * @return the comp2
	 */
	public UIComponent getComp2() {
		return comp2;
	}

	/**
	 * @param comp2 the comp2 to set
	 */
	public void setComp2(UIComponent comp2) {
		this.comp2 = comp2;
	}

	/**
	 * @return the comp3
	 */
	public UIComponent getComp3() {
		return comp3;
	}

	/**
	 * @param comp3 the comp3 to set
	 */
	public void setComp3(UIComponent comp3) {
		this.comp3 = comp3;
	}

	/**
	 * @return the comp4
	 */
	public UIComponent getComp4() {
		return comp4;
	}

	/**
	 * @param comp4 the comp4 to set
	 */
	public void setComp4(UIComponent comp4) {
		this.comp4 = comp4;
	}

	/**
	 * @return the comp5
	 */
	public UIComponent getComp5() {
		return comp5;
	}

	/**
	 * @param comp5 the comp5 to set
	 */
	public void setComp5(UIComponent comp5) {
		this.comp5 = comp5;
	}
	
}
