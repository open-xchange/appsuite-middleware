/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.test.mock.objects.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyListener;
import com.openexchange.config.WildcardFilter;
import com.openexchange.test.mock.main.util.MockDefaultValues;
import com.openexchange.test.mock.objects.AbstractMock;


/**
 * Mock for {@link ConfigurationService}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ConfigurationServiceMock<T extends ConfigurationService> extends AbstractMock {

    /**
     * The {@link ConfigurationService} that will be mocked with this class
     */
    private T configurationService;

    /**
     * {@link PropertyListener} used within the configuration service
     */
    private PropertyListener propertyListener;

    /**
     * {@link Properties}
     */
    private Properties properties;

    /**
     * Initializes a new {@link ConfigurationServiceMock}.
     */
    public ConfigurationServiceMock() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get() {
        return (T) this.configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMocks() {
        this.configurationService = (T) PowerMockito.mock(ConfigurationService.class);
        this.propertyListener = PowerMockito.mock(PropertyListener.class);
        this.properties = PowerMockito.mock(Properties.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeMembers() {
        // nothing to do yet
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void defineMockSpecificBehaviour() {
        try {
            PowerMockito.when(this.configurationService.getDirectory(anyString())).thenReturn(
                folder.newFolder(MockDefaultValues.DEFAULT_FOLDERNAME));
            File file = folder.newFile("tokenlogin-secrets");
            PowerMockito.when(this.configurationService.getFileByName("tokenlogin-secrets")).thenReturn(file);
        } catch (IOException ioException) {
            LOG.warn("Not able to create a new folder and/or file", ioException);
        }

        Map<String, Object> defaultMap = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "value");
        defaultMap.put("default", map);

        PowerMockito.when(this.configurationService.getYaml(anyString())).thenReturn(defaultMap);
        PowerMockito.when(this.configurationService.getBoolProperty(anyString(), anyBoolean())).thenReturn(true);
        PowerMockito.when(this.configurationService.getFilterFromProperty(anyString())).thenReturn(new WildcardFilter("Filter"));
        PowerMockito.when(this.configurationService.getIntProperty(anyString(), anyInt())).thenReturn(
            MockDefaultValues.DEFAULT_INT_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.getPropertiesInFolder(anyString())).thenReturn(this.properties);
        PowerMockito.when(this.configurationService.getProperty(anyString())).thenReturn(MockDefaultValues.DEFAULT_STRING_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.getPropertiesInFolder(anyString())).thenReturn(this.properties);
        PowerMockito.when(this.configurationService.getProperty(anyString(), eq(this.propertyListener))).thenReturn(
            MockDefaultValues.DEFAULT_STRING_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.getProperty(anyString(), anyString())).thenReturn(
            MockDefaultValues.DEFAULT_STRING_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.getProperty(anyString(), anyString(), eq(this.propertyListener))).thenReturn(
            MockDefaultValues.DEFAULT_STRING_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.getText(anyString())).thenReturn(MockDefaultValues.DEFAULT_STRING_PROPERTY_VALUE);
        PowerMockito.when(this.configurationService.size()).thenReturn(MockDefaultValues.DEFAULT_INT_PROPERTY_VALUE);
    }
}
