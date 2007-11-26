package com.openexchange.ajax.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;

public class AllTest extends ContactTest {

	private static final Log LOG = LogFactory.getLog(AllTest.class);
	
	public AllTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}	
	
	public void testAll() throws Exception {
		final int cols[] = new int[]{ AppointmentObject.OBJECT_ID };
		
		ContactObject[] contactArray = listContact(getWebConversation(), contactFolderId, cols, PROTOCOL + getHostName(), getSessionId());
	}
}