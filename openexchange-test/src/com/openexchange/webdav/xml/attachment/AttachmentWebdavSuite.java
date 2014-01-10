package com.openexchange.webdav.xml.attachment;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AttachmentWebdavSuite extends TestSuite{

    public static Test suite(){
        final TestSuite tests = new TestSuite("com.openexchange.webdav.xml.attachment.AttachmentWebdavSuite");
        tests.addTestSuite( DeleteTest.class );
        tests.addTestSuite( ListTest.class );
        tests.addTestSuite( NewTest.class );

        return tests;
    }
}
