/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.audit.configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.audit.services.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * Unit tests for {@link AuditConfiguration}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class })
public class AuditConfigurationTest {

    /**
     * Mock for the {@link ConfigurationService}
     */
    @Mock
    private ConfigurationService configurationService;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = OXException.class)
    public void testGetFileAccessLogging_ServicesNotAvailable_ThrowException() throws OXException {
        AuditConfiguration.getFileAccessLogging();
    }

    @Test
    public void testGetFileAccessLogging_ServiceAvailableAndConfiguredTrue_ReturnTrue() throws OXException {
        PowerMockito.mockStatic(Services.class);
        Mockito.when(Services.optService(ConfigurationService.class)).thenReturn(configurationService);
        Mockito.when(this.configurationService.getProperty("com.openexchange.audit.logging.FileAccessLogging.enabled", "true")).thenReturn("true");

        boolean fileAccessLogging = AuditConfiguration.getFileAccessLogging();
        Assert.assertTrue(fileAccessLogging);
    }

    @Test
    public void testGetFileAccessLogging_ServiceAvailableButNothingConfigured_ReturnFalse() throws OXException {
        PowerMockito.mockStatic(Services.class);
        Mockito.when(Services.optService(ConfigurationService.class)).thenReturn(configurationService);

        boolean fileAccessLogging = AuditConfiguration.getFileAccessLogging();
        Assert.assertFalse(fileAccessLogging);
    }
}
