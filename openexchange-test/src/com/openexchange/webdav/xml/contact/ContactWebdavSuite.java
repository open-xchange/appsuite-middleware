package com.openexchange.webdav.xml.contact;


import junit.framework.Test;
import junit.framework.TestSuite;

public class ContactWebdavSuite extends TestSuite{

    public static Test suite(){
        final TestSuite tests = new TestSuite("com.openexchange.webdav.xml.contact.ContactWebdavSuite");
        tests.addTestSuite( DeleteTest.class );
        tests.addTestSuite( ListTest.class );
        tests.addTestSuite( NewTest.class );
        tests.addTestSuite( UpdateTest.class );
        tests.addTestSuite( Bug8182Test.class );

        return tests;
    }
}
