
package com.openexchange.filestore.impl.groupware.unified;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.impl.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link UnifiedQuotaUtilsTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class })
public class UnifiedQuotaUtilsTest {

    private static final String COM_OPENEXCHANGE_UNIFIEDQUOTA_ENABLED = "com.openexchange.unifiedquota.enabled";

    @Mock
    ConfigViewFactory mockedConfigViewFactory;

    @Mock
    ConfigView mockedConfigView;

    @Mock
    ComposedConfigProperty<String> mockedComposedConfigProperty;

    @Before
    public void setUP() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Services.class);

        /**
         * Happy path mocking, every test will change only those parts that are not identical with
         * the ideal path.
         */
        setUpHappyPath_isUnifiedQuotaEnabledFor();
    }

    private void setUpHappyPath_isUnifiedQuotaEnabledFor() {
        try {
            PowerMockito.when(Services.optService(ConfigViewFactory.class)).thenReturn(mockedConfigViewFactory);
            PowerMockito.when(mockedConfigViewFactory.getView(0, 0)).thenReturn(mockedConfigView);
            PowerMockito.when(mockedConfigView.property(COM_OPENEXCHANGE_UNIFIEDQUOTA_ENABLED, String.class)).thenReturn(mockedComposedConfigProperty);
            PowerMockito.when(mockedComposedConfigProperty.isDefined()).thenReturn(true);
            PowerMockito.when(mockedComposedConfigProperty.get()).thenReturn("true");
        } catch (OXException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void isUnifiedQuotaEnabledFor_MissingFactoryTest() {
        PowerMockito.when(Services.optService(ConfigViewFactory.class)).thenReturn(null);
        try {
            UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        } catch (OXException e) {
            assertTrue("Wrong exception was thrown, SERVICE_UNAVAILABLE was expected", e.getExceptionCode() == ServiceExceptionCode.SERVICE_UNAVAILABLE);
            return;
        }
        fail("No error was thrown, but expected");
    }

    @Test
    public void isUnifiedQuotaEnabledFor_MissingProperty() throws OXException {
        PowerMockito.when(mockedConfigView.property(COM_OPENEXCHANGE_UNIFIEDQUOTA_ENABLED, String.class)).thenReturn(null);
        boolean unifiedQuotaEnabled = UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        assertTrue("false expected but got true", unifiedQuotaEnabled == false);
    }

    @Test
    public void isUnifiedQuotaEnabledFor_DefinedIsFalseTest() throws OXException {
        PowerMockito.when(mockedComposedConfigProperty.isDefined()).thenReturn(false);
        boolean unifiedQuotaEnabled = UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        assertTrue("false expected but got true", unifiedQuotaEnabled == false);
    }

    @Test
    public void isUnifiedQuotaEnabledFor_EmptyPropertyTest() throws OXException {
        PowerMockito.when(mockedComposedConfigProperty.get()).thenReturn("");
        boolean unifiedQuotaEnabled = UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        assertTrue("false expected but got true", unifiedQuotaEnabled == false);
    }

    @Test
    public void isUnifiedQuotaEnabledFor_WrongPropertyTest() throws OXException {
        PowerMockito.when(mockedComposedConfigProperty.get()).thenReturn("This is so wrong");
        boolean unifiedQuotaEnabled = UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        assertTrue("false expected but got true", unifiedQuotaEnabled == false);
    }

    @Test
    public void isUnifiedQuotaEnabledFor_TruePropertyTest() throws OXException {
        boolean unifiedQuotaEnabled = UnifiedQuotaUtils.isUnifiedQuotaEnabledFor(0, 0);
        assertTrue("true expected but got false", unifiedQuotaEnabled == true);
    }
}
