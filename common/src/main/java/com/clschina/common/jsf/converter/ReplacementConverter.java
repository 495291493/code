package com.clschina.common.jsf.converter;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.util.CommonUtil;


/**
 * @version 1.0
 */

public class ReplacementConverter implements StateHolder, Converter {
    private boolean transientFlag = false;
    private String[] sources;
    private String[] targets;
    protected static final Log log = LogFactory.getLog(ReplacementConverter.class);


    public ReplacementConverter(){
    }

  

    /**
     * getAsObject
     *
     * @param facesContext FacesContext
     * @param uIComponent UIComponent
     * @param string String
     * @return Object
     * @todo Implement this javax.faces.convert.Converter method
     */
    public Object getAsObject(FacesContext facesContext,
                              UIComponent uIComponent, String string) {
        return string;
    }


    /**
     * getAsString
     *
     * @param facesContext FacesContext
     * @param uIComponent UIComponent
     * @param object Object
     * @return String
     * @todo Implement this javax.faces.convert.Converter method
     */
    public String getAsString(FacesContext facesContext,
                              UIComponent uIComponent, Object object) {
        if (log.isTraceEnabled()){
            log.trace("begin getAsString " + object);
        }
        if(object == null){
        	return null;
        }
        String s = object.toString();
        for(int i=0; sources != null && i < sources.length; i++){
        	if(sources[i] != null){
        		String t = null;
        		if(i < targets.length){
        			t = targets[i];
        		}
        		if(t == null){
        			t = "";
        		}
        		s = CommonUtil.replace(s, sources[i], t);
        	}
        }

        if (log.isTraceEnabled()){
            log.trace("end getAsString " + s);
        }
        return s;
    }



    /**
     * saveState
     *
     * @param facesContext FacesContext
     * @return Object
     * @todo Implement this javax.faces.component.StateHolder method
     */
    public Object saveState(FacesContext facesContext) {
        Object values[] = new Object[3];
        values[0] = sources;
        values[1] = targets;
        return ((Object) (values));
    }


    /**
     * restoreState
     *
     * @param facesContext FacesContext
     * @param object Object
     * @todo Implement this javax.faces.component.StateHolder method
     */
    public void restoreState(FacesContext facesContext, Object state) {
        Object values[] = (Object[]) state;
        sources = (String[]) values[0];
        targets = (String[]) values[1];
    }


    /**
     * isTransient
     *
     * @return boolean
     */
    public boolean isTransient() {
        return transientFlag;
    }


    /**
     * setTransient
     *
     * @param boolean0 boolean
     */
    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }



	/**
	 * @return the sources
	 */
	public String[] getSources() {
		return sources;
	}



	/**
	 * @param sources the sources to set
	 */
	public void setSources(String[] sources) {
		this.sources = sources;
	}



	/**
	 * @return the targets
	 */
	public String[] getTargets() {
		return targets;
	}



	/**
	 * @param targets the targets to set
	 */
	public void setTargets(String[] targets) {
		this.targets = targets;
	}



}
