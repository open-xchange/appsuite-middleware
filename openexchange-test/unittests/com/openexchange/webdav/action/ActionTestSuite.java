
package com.openexchange.webdav.action;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.webdav.action.ifheader.IgnoreLocksIfHeaderApplyTest;
import com.openexchange.webdav.action.ifheader.StandardIfHeaderApplyTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GetTest.class,
    HeadTest.class,
    PutTest.class,
    DeleteTest.class,
    OptionsTest.class,
    TraceTest.class,
    MoveTest.class,
    CopyTest.class,
    MkcolTest.class,
    PropfindTest.class,
    ProppatchTest.class,
    LockTest.class,
    UnlockTest.class,
    IfMatchTest.class,
    IfTest.class,
    DefaultHeaderTest.class,
    NotExistTest.class,
    MaxUploadSizeActionTest.class,
    StandardIfHeaderApplyTest.class,
    IgnoreLocksIfHeaderApplyTest.class,
    Bug33505Test.class,
    Bug34283Test.class,
    Bug49057Test.class,
})
public class ActionTestSuite {
}
