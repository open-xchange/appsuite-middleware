package com.openexchange.webdav.xml.appointment;


import com.openexchange.webdav.xml.appointment.recurrence.Bug7915Test;
import com.openexchange.webdav.xml.appointment.recurrence.Bug8447Test;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AppointmentBugTestSuite extends TestSuite{
	
	public static Test suite(){
		TestSuite tests = new TestSuite();
		tests.addTestSuite( Bug4395Test.class );
		tests.addTestSuite( Bug5933Test.class );
        tests.addTestSuite( Bug6056Test.class );
		tests.addTestSuite( Bug6535Test.class );
		tests.addTestSuite( Bug7915Test.class );
		tests.addTestSuite( Bug8123Test.class );
		tests.addTestSuite( Bug8196Test.class );
		tests.addTestSuite( Bug8447Test.class );
		tests.addTestSuite( Bug8453Test.class );
		tests.addTestSuite( Bug8447Test.class );
		tests.addTestSuite( Bug6455Test.class );
		
		return tests;
	}
}