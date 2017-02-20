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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.properties.MailFilterConfigurationService;
import com.openexchange.mailfilter.properties.MailFilterProperty;
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
     */
    public MailFilterConfigurationServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#reloadConfiguration(com.openexchange.config.ConfigurationService)
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.Reloadable#getInterests()
     */
    @Override
    public Interests getInterests() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public String getProperty(MailFilterProperty property) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public String getProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property.getFQPropertyName(), userId, contextId, String.class, property.getDefaultValue(String.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getIntProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public int getIntProperty(MailFilterProperty property) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getIntProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public int getIntProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property.getFQPropertyName(), userId, contextId, int.class, property.getDefaultValue(Integer.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getBooleanProperty(com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public boolean getBooleanProperty(MailFilterProperty property) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mailfilter.properties.MailFilterConfigurationService#getBooleanProperty(int, int, com.openexchange.mailfilter.properties.MailFilterProperty)
     */
    @Override
    public boolean getBooleanProperty(int userId, int contextId, MailFilterProperty property) {
        return getProperty(property.getFQPropertyName(), userId, contextId, boolean.class, property.getDefaultValue(Boolean.class));
    }

    //////////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Get the value T of specified property for the specified user in the specified context and coerce it to the specified type T
     *
     * @param property The property's name
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param coerceTo The type T to coerce the value of the property
     * @return The value T of the property
     * @throws OXException If an error is occurred while getting the property
     */
    private <T> T getProperty(String property, int userId, int contextId, Class<T> coerceTo, T defaultValue) {
        if (contextId == 0) {
            return defaultValue;
        }
        if (contextId < 0) {
            contextId = -contextId;
            userId = -1;
        }

        try {
            ConfigViewFactory factory = getService(ConfigViewFactory.class);
            ConfigView view = factory.getView(userId, contextId);

            ComposedConfigProperty<T> p = view.property(property, coerceTo);
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
