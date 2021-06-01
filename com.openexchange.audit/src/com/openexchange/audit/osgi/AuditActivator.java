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

package com.openexchange.audit.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.audit.impl.AuditEventHandler;
import com.openexchange.audit.services.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * The activator for <i>com.openexchange.audit</i> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuditActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link AuditActivator}.
     */
    public AuditActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[] { "com/openexchange/groupware/*", FileStorageEventConstants.ALL_TOPICS });
        registerService(EventHandler.class, new AuditEventHandler(getService(UserService.class)), serviceProperties);
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
