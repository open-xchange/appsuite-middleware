package com.openexchange.oidc.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.oidc.impl.tests.OIDCAuthenticationFailedHandlerTest;
import com.openexchange.oidc.impl.tests.OIDCPasswordGrantAuthenticationTest;
import com.openexchange.oidc.impl.tests.OIDCSessionInspectorServiceTest;
import com.openexchange.oidc.impl.tests.OIDCWebSSoProviderImplTest;
import com.openexchange.oidc.spi.AbstractOIDCBackendConfigTest;
import com.openexchange.oidc.spi.OIDCBackendTest;
import com.openexchange.oidc.tools.OIDCToolsTest;

@RunWith(Suite.class)
@SuiteClasses({
    OIDCToolsTest.class,
    OIDCBackendTest.class,
    OIDCWebSSoProviderImplTest.class,
    OIDCSessionInspectorServiceTest.class,
    OIDCPasswordGrantAuthenticationTest.class,
    OIDCAuthenticationFailedHandlerTest.class,
    AbstractOIDCBackendConfigTest.class
})
public class UnitTests {

    public UnitTests() {
    }
}
