
package com.openexchange.ajax.importexport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    VCardTestSuite.class,
    ICalTestSuite.class,
    CSVImportExportServletTest.class

})
public class AllImportExportTests  {

}
