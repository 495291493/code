package com.clschina.common.autorun.sms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.clschina.common.autorun.AutoRun;
import com.clschina.common.component.ThreadLocalManager;
import com.clschina.common.db.HibernateSessionFactory;
import com.clschina.common.sms.SMSResult;
import com.clschina.common.sms.SMSService;

/**
 * 定时发送短信任务，从scheduled_sms表查询要发送的短信
 * create table scheduled_sms(
 *      id int identity primary key, 
 *      phone nchar(11) not null, 
 *      content nvarchar(200) not null, 
 *      scheduleDate datetime not null, 
 *      sendDate datetime null,
 *      status int default 0 not null
 *   )
 *   scheduleDate为计划发送时间
 *   status=0为待发送，1为已发。
 * @author GeXiangDong
 *
 */
public class ScheduledSms extends AutoRun {
    private final static Log log = LogFactory.getLog(ScheduledSms.class);

    @Override
    public void excuteTask() {
        Date taskStartTime = new Date();
        Session session = (Session) ThreadLocalManager.getValue(ThreadLocalManager.HIBERNATE_SESSION);
        if (session != null && (!session.isOpen() || !session.isConnected())) {
            if (log.isInfoEnabled()) {
                log.info("SESSION isOpen=" + session.isOpen() + "; isConnected=" + session.isConnected());
            }
            session = null;
        }
        if(session == null){
            SessionFactory factory = HibernateSessionFactory.getSessionFactory();
            if(log.isTraceEnabled()){
                log.trace("get session from HibernateSessionFactory " + factory);
            }
            session = factory.openSession();
            ThreadLocalManager.setValue(ThreadLocalManager.HIBERNATE_SESSION, session);
        }
        // 通过批次，查得数据
        String sql = "select id, phone, content from scheduled_sms with (nolock) where status=0 and scheduleDate>=:sdate and scheduleDate<=:edate";
        Query query = session.createSQLQuery(sql);
        Calendar sc = Calendar.getInstance();
        sc.add(Calendar.HOUR, -1);
        Calendar ec = Calendar.getInstance();
        ec.add(Calendar.MINUTE, 40);
        query.setParameter("sdate", sc);
        query.setParameter("edate", ec);
        List<?> list = query.list();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
        if (log.isTraceEnabled()) {
            log.trace("查询" + sdf.format(sc.getTime()) + "～" + sdf.format(ec.getTime()) + "之间的计划发送短信，发现" + list.size()
                    + "条。");
        }
        int successCount = 0;
        int failedCount = 0;
        for (int i = 0; i < list.size(); i++) {
            Object[] row = (Object[]) list.get(i);
            String phone = (String) row[1];
            String content = (String) row[2];
            int smsId = ((Number) row[0]).intValue();
            content = content.trim();
            phone = phone.trim();
            if (phone.length() == 11 && content.length() > 0) {
                SMSResult result = SMSService.getInstance().sms(phone, content, SMSService.DISPATCHER_CLSCHINA_GUANGGAO_HTTP);
                int code = result.getCode();
                if (log.isTraceEnabled()) {
                    log.trace("请求返回结果:[" + code + "]，内容：[" + content + "][" + phone + "]");
                    log.trace("请求返回结果:[" + result.getCode() + "/" + result.getMessage() + "]");
                }
                if (code == SMSResult.CODE_SUCCESS) {
                    successCount++;
                    String updSql = "update scheduled_sms set status=1, sendDate=getdate() where id=:id";
                    Transaction trans = session.beginTransaction();
                    Query q = session.createSQLQuery(updSql);
                    q.setParameter("id", smsId);
                    q.executeUpdate();
                    trans.commit();
                    if(log.isTraceEnabled()){
                        log.trace("update " + smsId + " to success status.");
                    }
                }else {
                    failedCount++;
                }
                try{
                    Thread.sleep(150);
                }catch(Exception e){
                    
                }
            }
        }
        if (successCount > 0 || failedCount > 0) {
            // 发送统计报告
            Date taskEndTime = new Date();
            String msg = "在" + sdf.format(sc.getTime()) + "～" + sdf.format(ec.getTime()) + "之间的有" + list.size() + "条计划发送短信。" + sdf.format(taskStartTime)
                    + "开始发送，耗时" + ((taskEndTime.getTime() - taskStartTime.getTime()) / 60000) + "分钟，发送成功" + successCount + "条，失败" + failedCount + "条。";
            SMSService.getInstance().sms("13501714675", msg, SMSService.DISPATCHER_CLSCHINA_GUANGGAO_HTTP);
        }
        
        session.close();
        ThreadLocalManager.getInstance().clear();
    }
}
