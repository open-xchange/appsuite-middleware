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

package com.openexchange.configjump.generic;

import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.configjump.ConfigJumpService;

/**
 * This class maintains the service registrations.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Services {

    private final BundleContext context;
    private final Lock registrationLock = new ReentrantLock();
    private ServiceRegistration<ConfigJumpService> configJump;

    /**
     * Default constructor.
     */
    public Services(final BundleContext context) {
        super();
        this.context = context;
    }

    public void registerService(final Properties props) {
        final String url = props.getProperty("URL");
        if (null == url) {
            org.slf4j.LoggerFactory.getLogger(Services.class).error("Missing URL property in configjump.properties.");
            return;
        }
        registrationLock.lock();
        try {
            if (null == configJump) {
                configJump = context.registerService(ConfigJumpService.class, new GenericImpl(url), null);
            }
        } finally {
            registrationLock.unlock();
        }
    }

    public void unregisterService() {
        registrationLock.lock();
        try {
            final ServiceRegistration<ConfigJumpService> configJump = this.configJump;
            if (null != configJump) {
                configJump.unregister();
                this.configJump = null;
            }
        } finally {
            registrationLock.unlock();
        }
    }
}
