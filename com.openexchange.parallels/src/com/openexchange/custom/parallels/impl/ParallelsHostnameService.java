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



package com.openexchange.custom.parallels.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.osgi.ParallelsServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;

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

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ParallelsHostnameService.class);

    @Override
    public String getHostname(final int userId, final int contextId) {
        if (contextId > 0) {
            final ContextService service = ParallelsServiceRegistry.getServiceRegistry().getService(ContextService.class);
            String hostname = null;
            Context ctx;
            try {
                ctx = service.getContext(contextId);
                final String[] login_mappings = ctx.getLoginInfo();
                final ConfigurationService configservice = ParallelsServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);

                // load suffix for branding string dynamically in loginmappings
                final String suffix_branded = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_SUFFIX);
                // for debugging purposes
                if(LOG.isDebugEnabled()){
                    LOG.debug("getHostname: Loaded loginmappings "+Arrays.toString(login_mappings)+" for context "+contextId);
                }
                boolean found_host = false;
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
                            if(URL_.length!=2){
                                LOG.error("getHostname: Could not split up branded host "+login_mapping+" login mapping for context "+contextId);
                            }else{
                                hostname = URL_[1];
                                if(LOG.isDebugEnabled()){
                                    LOG.debug("getHostname: Successfully resolved HOST to "+hostname+" for branded context "+contextId);
                                }
                                found_host = true;
                            }
                        }
                    }
                }
                if(!found_host){
                    // now host was provisioned, load fallback from configuration
                    hostname = configservice.getProperty(ParallelsOptions.PROPERTY_BRANDING_FALLBACKHOST);
                    // use systems getHostname() if no fallbackhost is set
                    if( null == hostname || hostname.length() == 0 ) {
                        try {
                            hostname = InetAddress.getLocalHost().getCanonicalHostName();
                        } catch (UnknownHostException e) { }
                    }
                    if( null == hostname || hostname.length() == 0 ) {
                        LOG.warn("getHostname: Unable to determine any hostname for context "+contextId);
                    }
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }

            return hostname;
        }else{
            LOG.error("getHostname: Got context with id "+contextId+", dont generating any hostname");
            return null;
        }


    }

}
