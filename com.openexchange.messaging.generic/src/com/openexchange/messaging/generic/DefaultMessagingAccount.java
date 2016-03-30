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

package com.openexchange.messaging.generic;

import java.util.Map;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.ServiceAware;
import com.openexchange.messaging.generic.services.MessagingGenericServiceRegistry;
import com.openexchange.messaging.registry.MessagingServiceRegistry;

/**
 * {@link DefaultMessagingAccount} - The default {@link MessagingAccount} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class DefaultMessagingAccount implements MessagingAccount, ServiceAware {

    private static final long serialVersionUID = -8295765793020470243L;

    private Map<String, Object> configuration;

    private String displayName;

    private int id;

    private String serviceId;

    private int[] staticRootPermissions;

    private transient MessagingService messagingService;

    /**
     * Initializes a new {@link DefaultMessagingAccount}.
     */
    public DefaultMessagingAccount() {
        super();
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public MessagingService getMessagingService() {
        MessagingService messagingService = this.messagingService;
        if (null == messagingService && null != serviceId) {
            // Try to obtain from registry
            final MessagingServiceRegistry registry = MessagingGenericServiceRegistry.getService(MessagingServiceRegistry.class);
            if (null == registry) {
                return null;
            }
            try {
                messagingService = registry.getMessagingService(serviceId, -1, -1);
            } catch (final Exception e) {
                messagingService = null;
            }
            if (null != messagingService) {
                this.messagingService = messagingService;
            }
        }
        return messagingService;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the service identifier
     *
     * @param serviceId The service identifier to set
     * @return This account with service identifier applied
     */
    public DefaultMessagingAccount setServiceId(final String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration to set
     */
    public void setConfiguration(final Map<String, Object> configuration) {
        this.configuration = configuration; //Collections.unmodifiableMap(configuration);
    }

    /**
     * Sets the display name.
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Sets the ID.
     *
     * @param id The ID to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the messaging service.
     *
     * @param messagingService The messaging service to set
     */
    public void setMessagingService(final MessagingService messagingService) {
        this.messagingService = messagingService;
        serviceId = null == messagingService ? null : messagingService.getId();
    }

}
