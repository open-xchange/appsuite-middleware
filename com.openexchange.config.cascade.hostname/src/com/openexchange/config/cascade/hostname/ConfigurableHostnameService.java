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

package com.openexchange.config.cascade.hostname;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;

/**
 * {@link ConfigurableHostnameService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ConfigurableHostnameService implements HostnameService {

    private final ConfigViewFactory configViews;

    private static final String HOSTNAME_KEY = "com.openexchange.hostname";
    private static final String OLD_GUEST_HOSTNAME_KEY = "com.openexchange.guestHostname";
    private static final String DEFAULT_GUEST_HOSTNAME_KEY = "com.openexchange.share.guestHostname";

    /**
     * Initializes a new {@link ConfigurableHostnameService}.
     *
     * @param configViews A reference to the config view factory
     */
    public ConfigurableHostnameService(final ConfigViewFactory configViews) {
        super();
        this.configViews = configViews;
    }

    @Override
    public String getHostname(final int userId, final int contextId) {
        return getHostname(HOSTNAME_KEY, userId, contextId);
    }

    @Override
    public String getGuestHostname(int userId, int contextId) {
        String hostname = getHostname(OLD_GUEST_HOSTNAME_KEY, userId, contextId);
        if (null != hostname) {
            org.slf4j.LoggerFactory.getLogger(ConfigurableHostnameService.class).warn(
                "Using deprecated definition from \"{}\" as guest hostname. This property will be removed in future versions in favor of the default property, " + 
                "so switch to \"{}\" now.", OLD_GUEST_HOSTNAME_KEY, DEFAULT_GUEST_HOSTNAME_KEY);
            return hostname;
        }        
        return getHostname(DEFAULT_GUEST_HOSTNAME_KEY, userId, contextId);
    }

    private String getHostname(String property, int userId, int contextId) {
        try {
            ConfigView view = configViews.getView(userId, contextId);
            return view.get(property, String.class);
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(ConfigurableHostnameService.class).warn("Error getting value for \"{}\": {}", property, e.getMessage(), e);
            return null;
        }
    }

}
