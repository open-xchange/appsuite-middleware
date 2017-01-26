
package com.openexchange.webdav.xml.contact;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    DeleteTest.class,
    ListTest.class,
    NewTest.class,
    UpdateTest.class,
    Bug8182Test.class,

})
public class ContactWebdavSuite  {


}
