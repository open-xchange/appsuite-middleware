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
            } catch (Exception e) {
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
