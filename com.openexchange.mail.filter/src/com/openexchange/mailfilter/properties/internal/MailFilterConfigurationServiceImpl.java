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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mailfilter.properties.internal;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.properties.MailFilterConfigurationService;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.mailfilter.properties.PasswordSource;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailFilterConfigurationServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailFilterConfigurationServiceImpl implements MailFilterConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailFilterConfigurationServiceImpl.class);

    private ServiceLookup services;

    /**
     * Initialises a new {@link MailFilterConfigurationServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @throws OXException if initialisation fails
     */
    public MailFilterConfigurationServiceImpl(ServiceLookup services) throws OXException {
        super();
        this.services = services;
        checkConfigfile();
    }

    /**
     * This method checks for a valid properties' file and throws and exception if none is there or one of the properties is missing
     * 
     * @throws OXException If the properties' file is invalid
     */
    private void checkConfigfile() throws OXException {
        final ConfigurationService config = getService(ConfigurationService.class);
        try {
            Properties file = ConfigurationServices.loadPropertiesFrom(config.getFileByName("mailfilter.properties"));
            if (file.isEmpty()) {
                throw MailFilterExceptionCode.NO_PROPERTIES_FILE_FOUND.create();
            }
            for (final MailFilterProperty property : MailFilterProperty.values()) {
                if (null == file.getProperty(property.getFQPropertyName())) {
                    throw MailFilterExceptionCode.PROPERTY_NOT_FOUND.create(property.getFQPropertyName());
                }
            }
            try {
                Integer.parseInt(file.getProperty(MailFilterProperty.connectionTimeout.getFQPropertyName()));
            } catch (final NumberFormatException e) {
                throw MailFilterExceptionCode.PROPERTY_ERROR.create("Property " + MailFilterProperty.connectionTimeout.getFQPropertyName() + " is not an integer value", e);
            }
        } catch (IOException e) {
            throw MailFilterExceptionCode.IO_ERROR.create(e.getMessage(), e);
        }

        // Check password source
        final String passwordSrc = config.getProperty(MailFilterProperty.passwordSource.getFQPropertyName());
        if (passwordSrc == null) {
            throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }
        PasswordSource passwordSource = PasswordSource.passwordSourceFor(passwordSrc);
        switch (passwordSource) {
            case GLOBAL:
                final String masterpassword = config.getProperty(MailFilterProperty.masterPassword.getFQPropertyName());
                if (masterpassword.length() == 0) {
                    throw MailFilterExceptionCode.NO_MASTERPASSWORD_SET.create();
                }
                break;
            case SESSION:
                break;
            default:
                throw MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.create();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#reloadConfiguration(com.openexchange.config.ConfigurationService)
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            checkConfigfile();
        } catch (OXException e) {
            LOGGER.error("Error while reloading 'mailfilter.properties': {}", e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#getInterests()
     */
    @Override
    public Interests getInterests() {
        String[] configFileNames = new String[MailFilterProperty.values().length];
        int index = 0;
        for (MailFilterProperty mailFilterProperty : MailFilterProperty.values()) {
            configFileNames[index++] = mailFilterProperty.getFQPropertyName();
        }
        return DefaultInterests.builder().configFileNames("mailfilter.properties").configFileNames(configFileNames).build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public String getProperty(MailFilterProperty property) {
        return getProperty(property, -1, -1, String.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public String getProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property, userId, contextId, String.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getIntProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public int getIntProperty(MailFilterProperty property) {
        return getProperty(property, -1, -1, Integer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getIntProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public int getIntProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property, userId, contextId, Integer.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getBooleanProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public boolean getBooleanProperty(MailFilterProperty property) {
        return getProperty(property, -1, -1, Boolean.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getBooleanProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public boolean getBooleanProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property, userId, contextId, Boolean.class);
    }

    //////////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Get the value T of specified property for the specified user in the specified context and coerce it to the specified type T
     *
     * @param property The {@link MailFilterProperty}
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property from the config cascade or the default value
     * @throws OXException If an error is occurred while getting the property
     */
    private <T> T getProperty(MailFilterProperty property, int userId, int contextId, Class<T> coerceTo) {
        T defaultValue = property.getDefaultValue(coerceTo);
        try {
            ConfigViewFactory factory = getService(ConfigViewFactory.class);
            ConfigView view = factory.getView(userId, contextId);

            ComposedConfigProperty<T> p = view.property(property.getFQPropertyName(), coerceTo);
            if (!p.isDefined()) {
                return defaultValue;
            }

            return p.get();
        } catch (OXException e) {
            LOGGER.error("Error getting '{}' property for user '{}' in context '{}'. Returning the default value of '{}'", property, userId, contextId, defaultValue, e);
        }
        return defaultValue;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The requested service
     * @throws OXException If the service is not available
     */
    private <S extends Object> S getService(final Class<? extends S> clazz) throws OXException {
        final S service = services.getService(clazz);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getSimpleName());
        }
        return service;
    }
}
