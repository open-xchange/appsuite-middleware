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



package com.openexchange.custom.parallels.impl;

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;

/**
 *
 * This service rewrites the hostname for the direct links which are sent via email to
 * appointments participants.
 *
 *
 *
 * @author Manuel Kraft
 *
 */
public final class ParallelsHostnameService implements HostnameService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsHostnameService.class);

    @Override
    public String getHostname(int userId, int contextId) {
        return getHostname(contextId, false);
    }

    @Override
    public String getGuestHostname(int userId, int contextId) {
        return getHostname(contextId, true);
    }

    private String getHostname(final int contextId, boolean isGuest) {
        if (contextId <= 0) {
            LOG.error("getHostname: Got context with id {}, dont generating any hostname", I(contextId));
            return null;
        }

        final ContextService service = Services.getService(ContextService.class);
        String hostname = null;
        String guestHostname = null;
        Context ctx;
        try {
            ctx = service.getContext(contextId);
            final String[] login_mappings = ctx.getLoginInfo();
            final ConfigurationService configservice = Services.getService(ConfigurationService.class);

            // load suffix for branding string dynamically in login-mappings
            final String suffix_branded = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_SUFFIX);
            LOG.debug("getHostname: Loaded loginmappings {} for context {}", Arrays.toString(login_mappings), I(contextId));
            if ( null != suffix_branded && suffix_branded.length() != 0) {
                for (final String login_mapping : login_mappings) {
                    if (login_mapping.startsWith(suffix_branded)){
                        /**
                         *
                         *  We found our mapping which contains the branded URL!
                         *
                         *  Now split up the string to get the URL part
                         *
                         */
                        final String[] URL_ = login_mapping.split("\\|\\|"); // perhaps replace with substring(start,end) if would be faster
                        if (3 <= URL_.length) {
                            hostname = URL_[2];
                            LOG.debug("getHostname: Successfully resolved HOST to {} for branded context {}", hostname, I(contextId));
                            if (4 <= URL_.length) {
                                guestHostname = URL_[3];
                                LOG.debug("getHostname: Successfully resolved guest HOST to {} for branded context {}", guestHostname, I(contextId));
                            }
                        } else {
                            LOG.error("getHostname: Could not split up branded host {} login mapping for context {}", login_mapping, I(contextId));
                        }
                    }
                }
            }
            if (null == hostname){
                // now host was provisioned, load fallback from configuration
                hostname = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_FALLBACKHOST);
                // use systems getHostname() if no fallbackhost is set
                if ( null == hostname || hostname.length() == 0 ) {
                    try {
                        hostname = InetAddress.getLocalHost().getCanonicalHostName();
                    } catch (UnknownHostException e) { }
                }
                if ( null == hostname || hostname.length() == 0 ) {
                    LOG.warn("getHostname: Unable to determine any hostname for context {}", I(contextId));
                }
            }
            if (null == guestHostname) {
                // no guest host was provisioned, load fallback from configuration
                guestHostname = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_GUESTFALLBACKHOST);
                // use systems getHostname() if no fallbackhost is set
                if (Strings.isEmpty(guestHostname)) {
                    guestHostname = hostname;
                    LOG.debug("getHostname: no guest host configured, falling back to HOST {} for branded context {}", guestHostname, I(contextId));
                }
            }
        } catch (OXException e) {
            LOG.error("", e);
        }

        return isGuest ? guestHostname : hostname;
    }

}
