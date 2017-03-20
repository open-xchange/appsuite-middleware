
package com.openexchange.ajax.kata;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    AppointmentRunner.class,
    ContactRunner.class,
    TaskRunner.class,
    FolderRunner.class,

})
public class KataSuite  {

}
