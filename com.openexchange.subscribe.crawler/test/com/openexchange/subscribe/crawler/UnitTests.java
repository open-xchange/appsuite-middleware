package com.openexchange.subscribe.crawler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.subscribe.xing.ContactSanitationTest;


/**
 * Unit tests for the bundle com.openexchange.subscribe.crawler
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
@RunWith(Suite.class)
@SuiteClasses({
    ContactSanitationTest.class,
})
public class UnitTests {

    public UnitTests() {
    }
}