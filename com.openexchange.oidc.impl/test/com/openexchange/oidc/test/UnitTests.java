package com.openexchange.oidc.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.oidc.impl.OIDCSessionInspectorServiceTest;
import com.openexchange.oidc.impl.OIDCWebSSoProviderImplTest;
import com.openexchange.oidc.spi.AbstractOIDCBackendTest;
import com.openexchange.oidc.tools.OIDCToolsTest;

@RunWith(Suite.class)
@SuiteClasses({
    OIDCToolsTest.class,
    AbstractOIDCBackendTest.class,
    OIDCWebSSoProviderImplTest.class,
    OIDCSessionInspectorServiceTest.class
})
public class UnitTests {

    public UnitTests() {
    }
}
