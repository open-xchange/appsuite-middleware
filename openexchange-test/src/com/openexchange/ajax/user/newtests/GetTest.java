package com.openexchange.ajax.user.newtests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.newactions.GetRequest;
import com.openexchange.ajax.user.newactions.GetResponse;

public class GetTest extends AbstractAJAXSession {
	
	private static final Log LOG = LogFactory.getLog(GetTest.class);
	
	public GetTest(final String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGet() throws Exception {
	    
	    final int id = 2;
	    
	    final GetRequest getRequest = new GetRequest(id);
	    final GetResponse getResponse = Executor.execute(client, getRequest);
	    
	    final JSONObject user = (JSONObject) getResponse.getData();
	    
	    assertTrue("No ID", user.hasAndNotNull("id"));
	    assertTrue("Wrong ID", user.getInt("id") == id);
	    
	    assertTrue("No aliases", user.hasAndNotNull("aliases"));
	    
	    System.out.println(user);
	}

}