
package com.openexchange.ajax.infostore.apiclient;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    UploadActionTest.class,
    RestoreTest.class
})
public class InfostoreApiClientSuite  {
    // empty
}
