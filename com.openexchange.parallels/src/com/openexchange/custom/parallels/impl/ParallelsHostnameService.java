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



package com.openexchange.custom.parallels.impl;

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
        return getHostname(userId, contextId, false);
    }

    @Override
    public String getGuestHostname(int userId, int contextId) {
        return getHostname(userId, contextId, true);
    }

    private String getHostname(final int userId, final int contextId, boolean isGuest) {
        if (contextId > 0) {
            final ContextService service = Services.getService(ContextService.class);
            String hostname = null;
            String guestHostname = null;
            Context ctx;
            try {
                ctx = service.getContext(contextId);
                final String[] login_mappings = ctx.getLoginInfo();
                final ConfigurationService configservice = Services.getService(ConfigurationService.class);

                // load suffix for branding string dynamically in loginmappings
                final String suffix_branded = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_SUFFIX);
                LOG.debug("getHostname: Loaded loginmappings {} for context {}", Arrays.toString(login_mappings), contextId);
                if( null != suffix_branded && suffix_branded.length() != 0) {
                    for (final String login_mapping : login_mappings) {
                        if(login_mapping.startsWith(suffix_branded)){
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
                                LOG.debug("getHostname: Successfully resolved HOST to {} for branded context {}", hostname, contextId);
                                if (4 <= URL_.length) {
                                    guestHostname = URL_[3];
                                    LOG.debug("getHostname: Successfully resolved guest HOST to {} for branded context {}", guestHostname, contextId);
                                }
                            } else {
                                LOG.error("getHostname: Could not split up branded host {} login mapping for context {}", login_mapping, contextId);
                            }
                        }
                    }
                }
                if (null == hostname){
                    // now host was provisioned, load fallback from configuration
                    hostname = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_FALLBACKHOST);
                    // use systems getHostname() if no fallbackhost is set
                    if( null == hostname || hostname.length() == 0 ) {
                        try {
                            hostname = InetAddress.getLocalHost().getCanonicalHostName();
                        } catch (UnknownHostException e) { }
                    }
                    if( null == hostname || hostname.length() == 0 ) {
                        LOG.warn("getHostname: Unable to determine any hostname for context {}", contextId);
                    }
                }
                if (null == guestHostname) {
                    // no guest host was provisioned, load fallback from configuration
                    guestHostname = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_GUESTFALLBACKHOST);
                    // use systems getHostname() if no fallbackhost is set
                    if (Strings.isEmpty(guestHostname)) {
                        guestHostname = hostname;
                        LOG.debug("getHostname: no guest host configured, falling back to HOST {} for branded context {}", guestHostname, contextId);
                    }
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }

            return isGuest ? guestHostname : hostname;
        } else {
            LOG.error("getHostname: Got context with id {}, dont generating any hostname", contextId);
            return null;
        }
    }

}
