
package com.openexchange.ajax.infostore;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.openexchange.ajax.infostore.test.AnotherCreateAndDeleteInfostoreTest;
import com.openexchange.ajax.infostore.test.Bug27722Test;
import com.openexchange.ajax.infostore.test.Bug32004Test;
import com.openexchange.ajax.infostore.test.CreateAndDeleteInfostoreTest;
import com.openexchange.ajax.infostore.test.InfostoreObjectCountTest;
import com.openexchange.ajax.infostore.test.TrashTest;
import com.openexchange.ajax.infostore.test.ZipDocumentsTest;

public class InfostoreAJAXSuite extends TestSuite {

    public static Test suite() {

        final TestSuite tests = new TestSuite("com.openexchange.ajax.infostore.InfostoreAJAXSuite");
        tests.addTestSuite(AllTest.class);
        tests.addTestSuite(DeleteTest.class);
        tests.addTestSuite(GetTest.class);
        tests.addTestSuite(ListTest.class);
        tests.addTestSuite(NewTest.class);
        tests.addTestSuite(UpdatesTest.class);
        tests.addTestSuite(UpdateTest.class);
        tests.addTestSuite(VersionsTest.class);
        tests.addTestSuite(DetachTest.class);
        tests.addTestSuite(DocumentTest.class);
        tests.addTestSuite(CopyTest.class);
        tests.addTestSuite(LockTest.class);
        tests.addTestSuite(SaveAsTest.class);
        tests.addTestSuite(SearchTest.class);
        tests.addTestSuite(CreateAndDeleteInfostoreTest.class);
        tests.addTestSuite(AnotherCreateAndDeleteInfostoreTest.class);
        tests.addTestSuite(InfostoreObjectCountTest.class);
        tests.addTestSuite(DeleteMultipleFilesTest.class);
        tests.addTestSuite(ZipDocumentsTest.class);
        tests.addTestSuite(Bug27722Test.class);
        tests.addTestSuite(TrashTest.class);
        tests.addTestSuite(Bug32004Test.class);

        return tests;
    }
}
