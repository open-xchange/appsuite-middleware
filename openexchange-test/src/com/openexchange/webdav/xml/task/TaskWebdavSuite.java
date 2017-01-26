
package com.openexchange.webdav.xml.task;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    ConfirmTest.class,
    DeleteTest.class,
    ListTest.class,
    NewTest.class,
    UpdateTest.class,
    
    Bug10991Test.class,

})
public class TaskWebdavSuite  {


}
