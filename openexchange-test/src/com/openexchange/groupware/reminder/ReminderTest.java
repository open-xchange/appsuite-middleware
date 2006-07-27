package com.openexchange.groupware.reminder;


import com.openexchange.groupware.Init;
import junit.framework.TestCase;

public class ReminderTest extends TestCase {
	
	private static boolean isInit = false;
	
	public void setUp() throws Exception {
		super.setUp();
		
		if (isInit) {
			return ;
		}
		
		Init.loadSystemProperties();
		Init.loadServerConf();
		Init.initDB();

		isInit = true;
} 
	
	public void testInsert() throws Exception {
		fail("not supported yet!");
	}

	public void testUpdate() throws Exception {
		fail("not supported yet!");
	}
	
	public void testDelete() throws Exception {
		fail("not supported yet!");
	}	

	public void testLoad() throws Exception {
		fail("not supported yet!");
	}	
	
	public void testListReminderByTargetId() throws Exception {
		fail("not supported yet!");
	}

	public void testListReminderBetweenByUserId() throws Exception {
		fail("not supported yet!");
	}
	
	public void testListLastModifiedReminderUserId() throws Exception {
		fail("not supported yet!");
	}	
}

