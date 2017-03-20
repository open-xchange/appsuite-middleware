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

package com.openexchange.sessionstorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link SessionStorageConfiguration} - Provides configuration settings for session storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class SessionStorageConfiguration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionStorageConfiguration.class);

    private static volatile SessionStorageConfiguration instance;

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static SessionStorageConfiguration getInstance() {
        return instance;
    }

    /**
     * Initializes the session storage configuration instance.
     *
     * @param configService The service to use
     * @param providers The providers listing
     */
    public static void initInstance(ConfigurationService configService, ServiceListing<SessionStorageParameterNamesProvider> providers) {
        synchronized (SessionStorageConfiguration.class) {
            instance = new SessionStorageConfiguration(configService, providers);
        }
    }

    /**
     * Releases the session storage configuration instance.
     */
    public static void releaseInstance() {
        synchronized (SessionStorageConfiguration.class) {
            instance = null;
        }
    }

    // --------------------------------------------------------------------------------------------------------------- //

    private final List<String> configuredRemoteParameterNames;
    private final ServiceListing<SessionStorageParameterNamesProvider> providers;

    /**
     * Initializes a new {@link SessionStorageConfiguration}.
     */
    private SessionStorageConfiguration(ConfigurationService configService, ServiceListing<SessionStorageParameterNamesProvider> providers) {
        super();
        this.providers = providers;
        configuredRemoteParameterNames = init(configService);
    }

    /**
     * Initializes this session storage configuration instance.
     *
     * @param configService The configuration service to use
     */
    private static List<String> init(ConfigurationService configService) {
        String tmp = configService.getProperty("com.openexchange.sessiond.remoteParameterNames");
        if (Strings.isEmpty(tmp)) {
            return Collections.emptyList();
        }

        Set<String> names = new TreeSet<String>();
        int length = tmp.length();

        int prev = 0;
        int pos;
        while (prev < length && (pos = tmp.indexOf(':', prev)) >= 0) {
            if (pos > 0) {
                names.add(tmp.substring(prev, pos));
            }
            prev = pos + 1;
        }
        if (prev < length) {
            names.add(tmp.substring(prev));
        }

        return ImmutableList.copyOf(names);
    }

    /**
     * Gets the names of such parameters that are supposed to be taken over from session to stored session representation.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The parameter names
     */
    public Collection<String> getRemoteParameterNames(int userId, int contextId) {
        Iterator<SessionStorageParameterNamesProvider> iter = providers.iterator();
        if (false == iter.hasNext()) {
            return configuredRemoteParameterNames;
        }

        Set<String> names = null;
        do {
            SessionStorageParameterNamesProvider provider = iter.next();
            try {
                List<String> parameterNames = provider.getParameterNames(userId, contextId);
                if (null != parameterNames && !parameterNames.isEmpty()) {
                    if (null == names) {
                        names = new TreeSet<String>(configuredRemoteParameterNames);
                    }
                    names.addAll(parameterNames);
                }
            } catch (Exception e) {
                LOG.warn("Failed to retrieve remote parameter names from provider '{}'", provider.getClass().getName(), e);
            }
        } while (iter.hasNext());
        return null == names ? configuredRemoteParameterNames : Collections.unmodifiableSet(names);
    }

}
