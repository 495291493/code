package com.clschina.common.backingbean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.bean.ResultInfo;
import com.clschina.common.util.CommonUtil;
import com.clschina.common.util.ContextUtil;
import com.clschina.common.util.DomUtil;

/**
 * 提供标准的信息画面的BackingBean
 * 
 * @author gexiangdong
 * 
 */
public class InformationBackingBean {
	/**
	 * InfomationBackingBean配置的名称
	 */
	private static final String BACKINGBEAN = "information";

	private final static Log log = LogFactory
			.getLog(InformationBackingBean.class);

	private String message;
	private String title;
	private List<Link> urls = new ArrayList<Link>();

	private String redirectUrl; // 自动跳转到的页面URL
	private String redirectUrlName; // 自动跳转到的页面名称
	private int redirectDelay; // 自动跳转到的页面，演示时间

	private static HashMap<String, PredefinedLink[]> predefinedLinks;
	private boolean showToppageLink = true;
	
	private boolean error = false; //是否出错

	public InformationBackingBean() {
		redirectDelay = 5;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * 显示信息画面的，信息详细内容，不可省略
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	private void setStandardMessage(String patternItem, String param) {
		ResourceBundle rb = ResourceBundle
				.getBundle("com.clschina.bundle.Common");
		String pattern = CommonUtil.getResourceString(rb, patternItem);
		Object[] args = new String[1];
		args[0] = param;
		String message = MessageFormat.format(pattern, args);
		setMessage(message);
	}

	/**
	 * 设置删除×××成功的信息，只需要传递主语***即可
	 * 
	 * @param param
	 */
	public void setDeletedMessage(String param) {
		setStandardMessage("deletedmessage", param);
	}

	/**
	 * 设置新增×××成功的信息，只需要传递主语***即可
	 * 
	 * @param param
	 */
	public void setAddedMessage(String param) {
		setStandardMessage("addedmessage", param);
	}

	/**
	 * 设置更新×××成功的信息，只需要传递主语***即可
	 * 
	 * @param param
	 */
	public void setUpdatedMessage(String param) {
		setStandardMessage("updatedmessage", param);
	}

	/**
	 * 自动跳转延时，省略则用系统默认时间
	 * 
	 * @return
	 */
	public int getRedirectDelay() {
		return redirectDelay;
	}

	public void setRedirectDelay(int redirectDelay) {
		this.redirectDelay = redirectDelay;
	}

	/**
	 * 自动跳转到的地址，如果不设置，显示信息画面不会出现自动跳转
	 * 
	 * @return
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		if (redirectUrl.startsWith("/")) {
			try {
				redirectUrl = ThreadLocalManager.getRequest().getContextPath()
						+ redirectUrl;
			} catch (Exception e) {
				log.error("cannot get contextpath", e);
			}
		}
		this.redirectUrl = redirectUrl;
	}

	/**
	 * 自动跳转画面的名称，如果不设置，则直接显示地址
	 * 
	 * @return
	 */
	public String getRedirectUrlName() {
		return redirectUrlName;
	}

	public void setRedirectUrlName(String redirectUrlName) {
		this.redirectUrlName = redirectUrlName;
	}

	/**
	 * 是否显示 返回首页的链接
	 * 
	 * @return
	 */
	public boolean isShowToppageLink() {
		return showToppageLink;
	}

	public void setShowToppageLink(boolean showToppageLink) {
		this.showToppageLink = showToppageLink;
	}

	/**
	 * 信息画面标题，可以省略
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 页面中所有要出现的链接，每个链接诶用一个InfomationBackingLink形式出现，包含name, url两属性，会被按照如下方式描画：
	 * <a href="{link.url}">点击这里，跳转到{link.name}。</a>
	 * 
	 * @return
	 */
	public List<Link> getUrls() {
		return urls;
	}

	/**
	 * 设置显示信息画面的自动跳转属性，此方法使用系统默认的延时，如果要单独设置延时请用setAutoRedirect(String url,
	 * String name, int delay) @see setAutoRedirect(String url, String name, int
	 * delay)
	 * 
	 * @param url
	 *            自动跳转到的URL
	 * @param name
	 *            页面名称，如果设置会在画面上显示
	 */
	public void setAutoRedirect(String url, String name) {
		setRedirectUrl(url);
		setRedirectUrlName(name);
		sendRedirect(url);
	}

	/**
	 * 设置显示信息画面的自动跳转属性
	 * 
	 * @param url
	 *            自动跳转到的URL
	 * @param name
	 *            页面名称，如果设置会在画面上显示
	 * @param delay
	 *            跳转延时，单位秒。 经过X秒后会自动跳转
	 */
	public void setAutoRedirect(String url, String name, int delay) {
		setRedirectUrl(url);
		setRedirectUrlName(name);
		setRedirectDelay(delay);
	}

	/**
	 * 清除前面设置过的所有可能跳转画面的设置。
	 * 
	 */
	public void clearAllUrls() {
		this.urls.clear();
	}

	/**
	 * 增加一个链接
	 * 
	 * @param url
	 * @param name
	 */
	public void addUrl(String url, String name) {
		addUrl(generateLinkUrl(url, name));
	}

	/**
	 * 增加一个链接
	 * 
	 * @param link
	 */
	public void addUrl(InformationBackingBean.Link link) {
		urls.add(link);
	}

	/**
	 * 从配置文件中加载链接(WEB-INF/information-navigation.xml)
	 * 开发状态，修改information-navigation.xml，不需要重启服务，每次都重新载入xml，方便测试。
	 * 部署后，则不每次都重新载入，以便提升性能。部署后，修改information-navigation.xml需要重启服务。
	 * 
	 * @param id
	 *            String 需要加载的ID
	 * @param params
	 *            Map url中用到的参数
	 */
	public void loadLinks(String id, Map<String, String> params) {
		if(log.isTraceEnabled()){
			log.trace("loadLinks(" + id + ", " + params + ")");
		}
		if (predefinedLinks == null) {
			if(log.isTraceEnabled()){
				log.trace("predefinedLinks is null");
			}
			initConfigurationFile();
		} else {
			// 开发状态，每次都重新载入xml，方便测试。
			// 部署后，则不每次都重新载入，以便提升性能。部署后，修改information-navigation.xml需要重启服务。
			Application app = ContextUtil.getApplication();
			if (ProjectStage.Development.equals(app.getProjectStage())) {
				initConfigurationFile();
			}

		}
		if(log.isTraceEnabled()){
			log.trace("predefinedLinks has " + predefinedLinks.size() + "records");
		}
		if (predefinedLinks != null) {
			PredefinedLink[] lks = predefinedLinks.get(id);
			if(log.isTraceEnabled()){
				log.trace(id + " not found in xml.");
			}
			if (lks != null) {
				for (int i = 0; i < lks.length; i++) {
					addUrl(lks[i].getUrl(params), lks[i].getName());
					if (lks[i].autoRedirect && !this.isError()) {
						// 自动跳转页面
						String url = lks[i].getUrl(params);
						this.setRedirectUrlName(lks[i].name);
						this.setRedirectUrl(url);
						sendRedirect(url);
					}
				}
			}
		}
	}
	private void sendRedirect(String url){
		if(log.isTraceEnabled()){
			log.trace("autoredirect url found " + url + "");
		}
		if(url.startsWith("/")){
			url = ThreadLocalManager.getRequest().getContextPath() + url;
		}
		HttpServletResponse resp = ThreadLocalManager.getResponse();
//		System.out.println("auto redirect " + url);
		try {
			resp.sendRedirect(url);
			if(log.isTraceEnabled()){
				log.trace("sendRedirect Response to " + url);
			}
//			System.out.println("sendredirect to " + url);
			resp.flushBuffer();
		} catch (IOException e) {
			if(log.isErrorEnabled()){
			    log.error("Error occured.", e);
			}
		}		
	}
	private synchronized void initConfigurationFile() {
		if (predefinedLinks != null) {
			return;
		}
		InputStream is = null;
		try {
			String f = ContextUtil.getServletContext().getRealPath(
					"/WEB-INF/information-navigation.xml");
			is = new FileInputStream(f);
		} catch (Exception e) {
			log.error("Error while find infomation-navigation.xml.", e);
			return;
		}
		predefinedLinks = new HashMap<String, PredefinedLink[]>();
		// 读取配置文件

		Element root = DomUtil.loadDocument(is);


		NodeList nodes = root.getElementsByTagName("action");
		for (int loop = 0; loop < nodes.getLength(); loop++) {
			Node node = nodes.item(loop);
			if (node != null) {
				String id = DomUtil.getNodeAttribute(node, "id");
				if (id == null) {
					if(log.isWarnEnabled()){
						log.warn("information-navigation.xml found action without ID, omit it.");
					}
					continue;
				}
				id = id.trim();
				if (predefinedLinks.get(id) != null) {
					if(log.isWarnEnabled()){
						log.warn("information-navigation.xml found duplicated action ID, '"
									+ id
									+ "', the previous one will be overwrite.");
					}
				}
				NodeList linkNodes = node.getChildNodes();
				ArrayList<PredefinedLink> links = new ArrayList<PredefinedLink>();
				for (int j = 0; j < linkNodes.getLength(); j++) {
					Node lNode = linkNodes.item(j);
					if (lNode != null && lNode.getNodeName().equals("link")) {
						PredefinedLink pl = new PredefinedLink();
						HashMap<String, String> baseParameters = new HashMap<String, String>();

						for (int i = 0; i < lNode.getAttributes().getLength(); i++) {
							Node n = lNode.getAttributes().item(i);
							String k = n.getNodeName();
							String v = n.getNodeValue();
							if ("name".equalsIgnoreCase(k)) {
								pl.name = v;
							} else if ("url".equalsIgnoreCase(k)) {
								pl.url = v;
							} else if ("autoredirect".equalsIgnoreCase(k)) {
								if ("true".equalsIgnoreCase(v)) {
									pl.autoRedirect = true;
								}
							}

						}
						pl.baseParams = baseParameters;
						links.add(pl);
					}
				}
				predefinedLinks.put(id, (PredefinedLink[]) links
						.toArray(new PredefinedLink[0]));
			}
		}
		try {
			is.close();
		} catch (IOException e) {

		}
	}

	/**
	 * 查找InfomationBacking在faces-config.xml配置的backingbean的实例
	 * 
	 * @return
	 */
	public static InformationBackingBean getBackingBean() {
		Object o = ContextUtil.lookupBean(BACKINGBEAN);
		if (o instanceof InformationBackingBean) {
			return (InformationBackingBean) o;
		} else {
			throw new NullPointerException(
					"无法找到BackingBean '" + BACKINGBEAN + "'，请检查配置文件。");
		}
	}

	/**
	 * 查找backingbean实例，并设置message
	 * 
	 * @param message
	 * @return
	 */
	public static InformationBackingBean getBackingBean(String message) {
		InformationBackingBean info = getBackingBean();
		info.setMessage(message);
		return info;
	}

	/**
	 * 查找backingbean实例，并设置message
	 * 
	 * @param message
	 * @return
	 */
	public static InformationBackingBean getBackingBean(String message,
			InformationBackingBean.Link link) {
		InformationBackingBean info = getBackingBean();
		info.setMessage(message);
		info.addUrl(link);
		return info;
	}

	/**
	 * 生成一个链接类
	 * 
	 * @param url
	 *            地址
	 * @param name
	 *            名称
	 * @return
	 */
	public static Link generateLinkUrl(String url, String name) {
		return new InformationBackingBean.Link(url, name);
	}

	public class PredefinedLink {
		Map<String, String> baseParams;
		Map<String, String> paramsConverter;
		String name;
		String url;
		boolean autoRedirect = false;

		String getName() {
			return name;
		}

		String getName(ResourceBundle rb) {
			return CommonUtil.getResourceString(rb, name);
		}

		String getUrl(Map<String, String> params) {
			if (url == null) {
				if (log.isInfoEnabled()) {
					log.info("information-navigation.xml 配置错误，" + name
							+ " 没有发现链接。");
				}
				return null;
			}
			String returnedUrl = url;
			for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
				String k = it.next();
				returnedUrl = CommonUtil.replace(returnedUrl, "{" + k + "}",
						params.get(k));
			}
			if (log.isTraceEnabled()) {
				log.trace("PredefinedLink: url from xml=" + url + "  params="
						+ params + "   finily url=" + returnedUrl);
			}
			return returnedUrl;
		}
	}

	public static class Link {
		private String url, name;

		public Link(String url, String name) {
			if (url == null) {
				throw new NullPointerException("url cannot be null");
			}
			if (name == null) {
				name = url;
			}
			setUrl(url);
			setName(name);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			if (url.startsWith("/")) {
				try {
					String contextPath = ThreadLocalManager.getRequest()
							.getContextPath();
					if (!url.startsWith(contextPath)) {
						url = contextPath + url;
					}
				} catch (Exception e) {
					log.error("cannot get contextpath", e);
				}
			}
			this.url = url;
		}

	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
		if(this.error){
			//出错则延长自动跳转的时间
			this.setRedirectDelay(3600); 
		}
	}
	/**
	 * 
	 * @param link 跳转链接
	 * @param result 返回结果对象
	 * @return
	 */
	public static void msgSkip(String link,ResultInfo result){
		msgSkip(link, result,null);
	}
	/**
	 * @param link 跳转链接
	 * @param result 返回结果对象
	 * @param params 传参
	 */
	public static void msgSkip(String link,ResultInfo result,HashMap<String, String> params){
		InformationBackingBean info = InformationBackingBean.getBackingBean();
		if(params == null){
			params = new HashMap<String, String>();
		}
		info.loadLinks(link, params);
		if(result != null){
			info.setMessage(result.getMsg());		
			info.setError(!result.isSuccess());
		}else{
			info.setError(true);
		}
	}
	/**
	 * 失败信息直接跳转
	 * @param link 跳转链接
	 * @param result 返回结果对象
	 * @return
	 */
	public static void errmsgSkip(String link,String errmsg){
		InformationBackingBean info = InformationBackingBean.getBackingBean();
		HashMap<String, String> params = new HashMap<String, String>();
		info.loadLinks(link, params);		
		info.setError(true);
		info.setMessage(errmsg);	
	}
}
