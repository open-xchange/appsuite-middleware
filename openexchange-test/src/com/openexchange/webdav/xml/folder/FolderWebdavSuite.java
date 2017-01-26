
package com.openexchange.webdav.xml.folder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    DeleteTest.class,
    ListTest.class,
    NewTest.class,
    UpdateTest.class,

})
public class FolderWebdavSuite  {


}
