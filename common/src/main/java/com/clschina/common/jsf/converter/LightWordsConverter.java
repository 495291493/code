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

public class LightWordsConverter implements StateHolder, Converter {
    private boolean transientFlag = false;
    private String keywords;
    private String lightStyleClass;
    private int reservedLength;
    private final int MINLENGTH = 5;
    protected static final Log log = LogFactory.getLog(LightWordsConverter.class);


    public LightWordsConverter(){
    }

    public String getKeywords(){
        return keywords;
    }
    public void setKeywords(String keywords){
        this.keywords = keywords;
    }
    public String getLightStyleClass(){
        return lightStyleClass;
    }
    public void setLightStyleClass(String lightStyleClass){
        this.lightStyleClass = lightStyleClass;
    }
    public int getReservedLength(){
        return reservedLength;
    }
    public void setReservedLength(int reservedLength){
        this.reservedLength = reservedLength;
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
        if (keywords == null || keywords.trim().length() == 0){
            if (reservedLength > MINLENGTH) {
                if (object.toString().length() > reservedLength) {
                    if (log.isTraceEnabled()){
                        log.trace("light words isnot needed, only truncating.");
                        log.trace("begin getAsString");
                    }
                    return object.toString().substring(0, reservedLength - 2) +
                            "...";
                }
            }
            if (log.isTraceEnabled()){
                log.trace("light words & truncating arenot needed.");
                log.trace("begin getAsString");
            }
            return object.toString();
        }
        String str = object.toString();

        String[] keys = keywords.split(" ");
        boolean cuted = false;
        if (reservedLength > MINLENGTH && str.length() > reservedLength){
            int position = -1;
            for (int i = 0; i < keys.length; i++) {
                int tmp;
                tmp = str.indexOf(keys[i]);
                //if position < 0 use tmp.
                position = (position < 0 ? tmp : position);
                position = (position > tmp && tmp >= 0 ? tmp : position);
            }
            if (position < 0) {
                str = str.substring(0, reservedLength - 2);
                cuted = true;
            }else{
                int start = position - (reservedLength / 3);
                if (start <= 0){
                    start = 0;
                }else{
                    if ((str.length() - start) < reservedLength){
                        start = str.length() - reservedLength;
                    }
                    start = (start < 0 ? 0 : start);
                }
                str = str.substring(start, start + reservedLength - 2);
                cuted = true;
            }
        }
        String additionBefore, additionAfter;
        if (lightStyleClass == null){
            additionBefore = "<font style='color:#FF0000; font-weight:bold'>";
            additionAfter = "</font>";
        }else{
            additionBefore = "<font class='" + lightStyleClass + "'>";
            additionAfter = "</font>";
        }
        for (int i = 0; i < keys.length; i++) {
            str = CommonUtil.replace(str, keys[i],
                               additionBefore + keys[i] + additionAfter);
        }
        if (cuted){
            str += "...";
        }
        if (log.isTraceEnabled()){
            log.trace("end getAsString ");
        }
        return str;
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
        values[0] = keywords;
        values[1] = lightStyleClass;
        values[2] = new Integer(reservedLength);
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
        keywords = (String) values[0];
        lightStyleClass = (String) values[1];
        reservedLength = ((Integer) values[2]).intValue();
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



}
