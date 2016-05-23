package com.clschina.common.datasync.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.filter.MultipartRequestWrapper;

/**
 * 接收上传文件
 * 
 * @author ch
 * 
 */
public class DataReceiverServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * 日志
	 */
	private static final Log log = LogFactory.getLog(DataReceiverServlet.class);

	public void init() {
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		// 保存上传文件的路径
		File rootFolder = ThreadLocalManager.getInstance().getFilesFolder();

		MultipartRequestWrapper mReq = (MultipartRequestWrapper) request;//
		FileItem[] fileItem = mReq.getFileItems("file");// 上传的文件
		String[] sql = mReq.getParameterValues("sql");

		if (fileItem != null && fileItem.length > 0) {
			for (int i = 0; i < fileItem.length; i++) {
				FileItem fi = fileItem[i];
				// 获取 field 的 name 或 id
				String fileName = fi.getName();

				// 文件名中文处理
				//fileName = new String(fileName.getBytes(), "utf-8");

				// 把上传数据写入本地磁盘
				File file = new File(rootFolder, fileName);
				File parentFolder = file.getParentFile();
				if (!parentFolder.exists()) {
					parentFolder.mkdirs();
				}
				if(log.isTraceEnabled()){
					log.trace("will save file to + " + file.getPath());
				}
				try {
					fi.write(file);
				} catch (Exception e) {
					log.error(
							"上传接收文件出错：" + fileName + "; save to "
									+ file.getPath(), e);
				}

				if (log.isTraceEnabled()) {
					log.trace("----------------[" + fileName + "]["
							+ file.getPath() + "]");
				}
			}

		}
		if(log.isTraceEnabled()){
			log.trace("sql = " + (sql == null ? "null" : sql.length + " sql(s).") + "   " + request.getParameter("sql"));
		}
		if (sql != null) {
			Session session = (Session) ThreadLocalManager
					.getValue(ThreadLocalManager.HIBERNATE_SESSION);
			Transaction trans = session.beginTransaction();
			trans.begin();
			for (int i = 0; i < sql.length; i++) {
				SQLQuery q = session.createSQLQuery(sql[i]);
				int rowsEffected = q.executeUpdate();
				if (log.isTraceEnabled()) {
					log.trace("#" + i + " " + sql + ";  " + rowsEffected
							+ " rows effected.");
				}
			}
			trans.commit();
		}
		out.println("ok");
	}
}
