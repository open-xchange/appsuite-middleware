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
