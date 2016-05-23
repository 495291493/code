package com.clschina.common.redmin.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clschina.common.log.EMailLogAppender;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;

import junit.framework.TestCase;

public class RedminAPITest extends TestCase {
	private static Log log = LogFactory.getLog(RedminAPITest.class);

	public void testShowIssueList() throws Exception{
		log.error("测试Readmine Issue Log Appender. " + new Date(), new RuntimeException());
		
		String uri = "http://zb.clschina.com/redmine/";
	    String apiAccessKey = "84b55e2434516e2909fbf0db6fada056ad7ec5da";
	    String projectKey = "sfb-wx";
	    Integer queryId = null; // any

	    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
	    //RedmineManager mgr = RedmineManagerFactory.createWithUserAuth(uri, login, password);

	    IssueManager issueManager = mgr.getIssueManager();
//	    List<Issue> issues = issueManager.getIssues(projectKey, queryId);
	    HashMap<String, String> map = new HashMap<String, String>();
	    //map.put("projectkey", projectKey);
	    map.put("created_on", "2015-05-28");
	    map.put("status_id", "o");
	    map.put("cf_1", "helloworld");
	    map.put("author_id", "me");
	    
	    List<Issue> issues = issueManager.getIssues(map);
	    for (Issue issue : issues) {
	        System.out.println(issue.toString() );
	        issue.setNotes("issue notes..... " + (new Date()).toString());
	        issueManager.update(issue);
	        
	        
	    }

	    // Create issue
	   /*
	    projectKey = "sandbox";
	    Project project = mgr.getProjectManager().getProjectByKey(projectKey);
	    Issue issueToCreate = IssueFactory.create(project.getId(), "redmine api test2-1");
	    CustomField cf = CustomFieldFactory.create(1, "issuemd5", "helloworld");
	    issueToCreate.addCustomField(cf);

	    Issue createdIssue = issueManager.createIssue(issueToCreate);
	    System.out.print("NEW ISSUE ID: " + createdIssue.getId());
	     */
	    // Get issue by ID:
	    //Issue issue = issueManager.getIssueById(123);
	}

}
