package com.openexchange.admin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.openexchange.admin.rmi.AdminCoreTest;
import com.openexchange.admin.rmi.GroupTest;
import com.openexchange.admin.rmi.ResourceTest;
import com.openexchange.admin.rmi.TaskMgmtTest;
import com.openexchange.admin.rmi.UserTest;
import com.openexchange.admin.tools.NetUtilTest;
	 
@RunWith(Suite.class)
@Suite.SuiteClasses({
	  AdminCoreTest.class,
	  UserTest.class,
	  GroupTest.class,
	  ResourceTest.class,
	  TaskMgmtTest.class,
	  NetUtilTest.class
})
	
public class AdminTestSuite {
	    // the class remains completely empty, 
	    // being used only as a holder for the above annotations
}
