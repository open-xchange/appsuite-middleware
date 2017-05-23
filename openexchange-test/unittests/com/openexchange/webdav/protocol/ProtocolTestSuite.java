
package com.openexchange.webdav.protocol;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ResourceTest.class,
    CollectionTest.class,
    LockTest.class,
    LockInteractionTest.class,
})
public class ProtocolTestSuite {

}
