package com.openexchange.webdav.xml.folder;


import junit.framework.Test;
import junit.framework.TestSuite;

public class FolderWebdavSuite extends TestSuite{

    public static Test suite(){
        final TestSuite tests = new TestSuite("com.openexchange.webdav.xml.folder.FolderWebdavSuite");
        tests.addTestSuite( DeleteTest.class );
        tests.addTestSuite( ListTest.class );
        tests.addTestSuite( NewTest.class );
        tests.addTestSuite( UpdateTest.class );

        return tests;
    }
}
