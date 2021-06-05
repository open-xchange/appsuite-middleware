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

package com.openexchange.file.storage.config.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.config.ConfigFileStorageAccount;
import com.openexchange.file.storage.config.ConfigFileStorageAuthenticator;

/**
 * {@link ConfigFileStorageAccountParser} - Provides configured accounts parsed from a <i>.properties</i> file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class ConfigFileStorageAccountParser {

    private static final ConfigFileStorageAccountParser INSTANCE = new ConfigFileStorageAccountParser();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ConfigFileStorageAccountParser getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator> authenticators;

    private volatile Map<String, Map<String, ConfigFileStorageAccountImpl>> map;

    /**
     * Initializes a new {@link ConfigFileStorageAccountParser}.
     */
    private ConfigFileStorageAccountParser() {
        super();
        authenticators = new ConcurrentHashMap<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator>(4, 0.9f, 1);
        map = Collections.emptyMap();
    }

    /**
     * Gets the authenticators' map.
     *
     * @return The authenticators' map
     */
    public ConcurrentMap<ConfigFileStorageAuthenticator, ConfigFileStorageAuthenticator> getAuthenticators() {
        return authenticators;
    }

    /**
     * Drops formerly parsed properties.
     */
    public void drop() {
        map = Collections.emptyMap();
    }

    /**
     * Retrieves the first configured account matching given account identifier.
     *
     * @param accountId The account identifier
     * @return The first matching account or <code>null</code>
     */
    public ConfigFileStorageAccount get(final String accountId) {
        for (final Map.Entry<String, Map<String, ConfigFileStorageAccountImpl>> entry : map.entrySet()) {
            final Map<String, ConfigFileStorageAccountImpl> accounts = entry.getValue();
            final ConfigFileStorageAccountImpl fileStorageAccount = accounts.get(accountId);
            if (null != fileStorageAccount) {
                // A configured account available
                return fileStorageAccount;
            }
        }
        return null;
    }

    /**
     * Gets the configured accounts for specified service identifier.
     *
     * @param serviceId The service identifier
     * @return The configured accounts
     */
    public Map<String, ConfigFileStorageAccountImpl> getAccountsFor(final String serviceId) {
        return map.get(serviceId);
    }

    /**
     * Parses specified properties to a map associating service identifier with configured file storage accounts.
     *
     * @param properties The properties to parse
     */
    public void parse(final Properties properties) {
        final String prefix = "com.openexchange.file.storage.account.";
        final int prefixLength = prefix.length();
        /*
         * Parse identifiers
         */
        final Set<String> ids = new HashSet<String>();
        final Locale english = Locale.ENGLISH;
        for (final Object key : properties.keySet()) {
            final String propName = ((String) key).toLowerCase(english);
            if (propName.startsWith(prefix)) {
                final String id = propName.substring(prefixLength, propName.indexOf('.', prefixLength));
                ids.add(id);
            }
        }
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigFileStorageAccountParser.class);
        if (ids.isEmpty()) {
            logger.info("Found no pre-configured file storage accounts.");
            return;
        }
        logger.info("Found following pre-configured file storage accounts: {}", new TreeSet<String>(ids));
        /*
         * Get the accounts for identifiers
         */
        final Map<String, Map<String, ConfigFileStorageAccountImpl>> m = new ConcurrentHashMap<String, Map<String, ConfigFileStorageAccountImpl>>();
        for (final String id : ids) {
            try {
                final ConfigFileStorageAccountImpl account = parseAccount(id, properties);
                final String serviceId = account.getServiceId();
                Map<String, ConfigFileStorageAccountImpl> map = m.get(serviceId);
                if (null == map) {
                    map = new ConcurrentHashMap<String, ConfigFileStorageAccountImpl>(2, 0.9f, 1);
                    m.put(serviceId, map);
                }
                map.put(account.getId(), account);
            } catch (OXException e) {
                logger.warn("Configuration for file storage account \"{}\" is invalid", id, e);
            }
        }
        this.map = m;
    }

    private ConfigFileStorageAccountImpl parseAccount(final String id, final Properties properties) throws OXException {
        final StringBuilder sb = new StringBuilder("com.openexchange.file.storage.account.").append(id).append('.');
        final int resetLen = sb.length();
        /*
         * Create account
         */
        final ConfigFileStorageAccountImpl account = new ConfigFileStorageAccountImpl();
        account.setId(id);
        /*
         * Parse display name
         */
        final String displayName = properties.getProperty(sb.append("displayName").toString());
        if (null == displayName) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create("displayName");
        }
        account.setDisplayName(dropQuotes(displayName));
        /*
         * Parse service identifier
         */
        sb.setLength(resetLen);
        final String serviceId = properties.getProperty(sb.append("serviceId").toString());
        if (null == serviceId) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create("serviceId");
        }
        account.setServiceId(dropQuotes(serviceId));
        /*
         * Parse configuration
         */
        sb.setLength(resetLen);
        sb.append("config.");
        final String configPrefix = sb.toString();
        final int configPrefixLen = configPrefix.length();
        final Map<String, Object> configuration = new HashMap<String, Object>();
        for (final Entry<Object, Object> entry : properties.entrySet()) {
            final String value = (String) entry.getValue();
            final String propName = ((String) entry.getKey()).toLowerCase(Locale.ENGLISH);
            if (propName.startsWith(configPrefix) && null != value) {
                configuration.put(propName.substring(configPrefixLen), dropQuotes(value));
            }
        }
        account.setConfiguration(configuration);
        return account;
    }

    private static String dropQuotes(final String value) {
        final int mlen = value.length() - 1;
        if (mlen > 1 && '"' == value.charAt(0) && '"' == value.charAt(mlen)) {
            return value.substring(1, mlen);
        }
        return value;
    }

}
