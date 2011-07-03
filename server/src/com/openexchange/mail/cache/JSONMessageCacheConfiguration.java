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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mail.cache;

import com.openexchange.config.ConfigurationService;
import com.openexchange.mail.MailException;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link JSONMessageCacheConfiguration} - The configuration for JSON message cache.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONMessageCacheConfiguration {

    private static JSONMessageCacheConfiguration instance;

    /**
     * Gets the configuration for JSON message cache.
     * 
     * @return The configuration for JSON message cache.
     */
    public static JSONMessageCacheConfiguration getInstance() {
        return instance;
    }

    /**
     * Initializes the cache instance.
     * 
     * @return The cache instance
     * @throws MailException If initialization fails
     */
    public static void initInstance() throws MailException {
        synchronized (JSONMessageCache.class) {
            if (null == instance) {
                instance = new JSONMessageCacheConfiguration();
            }
        }
    }

    /**
     * Releases the cache instance.
     */
    public static void releaseInstance() {
        synchronized (JSONMessageCache.class) {
            if (null != instance) {
                instance = null;
            }
        }
    }

    /*-
     * ############################################# Member stuff #############################################
     */

    /**
     * Whether the cache is enabled.
     */
    private boolean enabled;

    /**
     * The shrinker interval in seconds for the superior user map.
     */
    private int shrinkerIntervalUserMap;

    /**
     * The shrinker interval in seconds for folder maps.
     */
    private int shrinkerIntervalFolderMap;

    /**
     * The time-to-live in seconds for folder maps put into user map.
     */
    private int ttlUserMap;

    /**
     * The time-to-live in seconds for an ID-to-JSON mapping put into folder map.
     */
    private int ttlFolderMap;

    /**
     * The max. time in milliseconds to wait for a mail's JSON representation to become available in a folder map.
     */
    private int maxWaitTimeMillis;

    /**
     * Whether to prefetch unseen mails only.
     */
    private boolean unseenOnly;

    /**
     * Initializes a new {@link JSONMessageCacheConfiguration}.
     */
    private JSONMessageCacheConfiguration() {
        super();
    }

    /**
     * Loads the JSON message cache properties.
     * 
     * @throws MailException If loading of properties fails
     */
    public void loadProperties() throws MailException {
        final ConfigurationService configuration;
        try {
            configuration = ServerServiceRegistry.getInstance().getService(ConfigurationService.class, true);
        } catch (final ServiceException e) {
            throw new MailException(e);
        }

        final StringBuilder logBuilder = new StringBuilder(512);
        logBuilder.append("\nLoading JSON message cache properties...\n");

        final String fallbackPrefix = "\". Setting to fallback: ";

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.enabled", "true").trim();
            if ("true".equalsIgnoreCase(tmp)) {
                enabled = true;
                logBuilder.append("\tEnabled: ").append(enabled).append('\n');
            } else if ("false".equalsIgnoreCase(tmp)) {
                enabled = false;
                logBuilder.append("\tEnabled: ").append(enabled).append('\n');
            } else {
                enabled = true;
                logBuilder.append("\tEnabled: Non parseable boolean value \"").append(tmp).append(fallbackPrefix).append(enabled).append(
                    '\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.shrinkerIntervalUserMap", "60").trim();
            try {
                shrinkerIntervalUserMap = Integer.parseInt(tmp);
                logBuilder.append("\tShrinker Interval User Map: ").append(shrinkerIntervalUserMap).append('\n');
            } catch (final NumberFormatException e) {
                shrinkerIntervalUserMap = 60;
                logBuilder.append("\tShrinker Interval User Map: Non parseable value \"").append(tmp).append(fallbackPrefix).append(
                    shrinkerIntervalUserMap).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.shrinkerIntervalFolderMap", "60").trim();
            try {
                shrinkerIntervalFolderMap = Integer.parseInt(tmp);
                logBuilder.append("\tShrinker Interval Folder Map: ").append(shrinkerIntervalFolderMap).append('\n');
            } catch (final NumberFormatException e) {
                shrinkerIntervalFolderMap = 60;
                logBuilder.append("\tShrinker Interval Folder Map: Non parseable value \"").append(tmp).append(fallbackPrefix).append(
                    shrinkerIntervalFolderMap).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.ttlUserMap", "300").trim();
            try {
                ttlUserMap = Integer.parseInt(tmp);
                logBuilder.append("\tTTL User Map: ").append(ttlUserMap).append('\n');
            } catch (final NumberFormatException e) {
                ttlUserMap = 300;
                logBuilder.append("\tTTL User Map: Non parseable value \"").append(tmp).append(fallbackPrefix).append(ttlUserMap).append(
                    '\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.ttlFolderMap", "300").trim();
            try {
                ttlFolderMap = Integer.parseInt(tmp);
                logBuilder.append("\tTTL Folder Map: ").append(ttlFolderMap).append('\n');
            } catch (final NumberFormatException e) {
                ttlFolderMap = 300;
                logBuilder.append("\tTTL Folder Map: Non parseable value \"").append(tmp).append(fallbackPrefix).append(ttlFolderMap).append(
                    '\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.unseenOnly", "false").trim();
            if ("true".equalsIgnoreCase(tmp)) {
                unseenOnly = true;
                logBuilder.append("\tUnseen Only: ").append(unseenOnly).append('\n');
            } else if ("false".equalsIgnoreCase(tmp)) {
                unseenOnly = false;
                logBuilder.append("\tUnseen Only: ").append(unseenOnly).append('\n');
            } else {
                unseenOnly = false;
                logBuilder.append("\tUnseen Only: Non parseable boolean value \"").append(tmp).append(fallbackPrefix).append(unseenOnly).append(
                    '\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.cache.json.maxWaitTimeMillis", "100").trim();
            try {
                maxWaitTimeMillis = Integer.parseInt(tmp);
                logBuilder.append("\tMax. Wait Time Millis: ").append(maxWaitTimeMillis).append('\n');
            } catch (final NumberFormatException e) {
                maxWaitTimeMillis = 100;
                logBuilder.append("\tMax. Wait Time Millis: Non parseable value \"").append(tmp).append(fallbackPrefix).append(
                    maxWaitTimeMillis).append('\n');
            }
        }

        logBuilder.append("JSON message cache properties successfully loaded!");
        final org.apache.commons.logging.Log logger = com.openexchange.exception.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(JSONMessageCacheConfiguration.class));
        if (logger.isInfoEnabled()) {
            logger.info(logBuilder.toString());
        }
    }

    /**
     * Indicates if the JSON mail cache is enabled.
     * 
     * @return <code>true</code> if the JSON mail cache is enabled; otherwise <code>false</code>
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the shrinker interval in seconds for the superior user map.
     * 
     * @return The shrinker interval in seconds for the superior user map
     */
    public int getShrinkerIntervalUserMap() {
        return shrinkerIntervalUserMap;
    }

    /**
     * Gets the shrinker interval in seconds for folder maps.
     * 
     * @return The shrinker interval in seconds for folder maps
     */
    public int getShrinkerIntervalFolderMap() {
        return shrinkerIntervalFolderMap;
    }

    /**
     * Gets the time-to-live in seconds for folder maps put into user map.
     * 
     * @return The time-to-live in seconds for folder maps put into user map
     */
    public int getTTLUserMap() {
        return ttlUserMap;
    }

    /**
     * Gets the time-to-live in seconds for an ID-to-JSON mapping put into folder map.
     * 
     * @return The time-to-live in seconds for an ID-to-JSON mapping put into folder map
     */
    public int getTTLFolderMap() {
        return ttlFolderMap;
    }

    /**
     * Gets the max. time in milliseconds to wait for a mail's JSON representation to become available in a folder map.
     * 
     * @return The max. time in milliseconds to wait for a mail's JSON representation to become available in a folder map
     */
    public int getMaxWaitTimeMillis() {
        return maxWaitTimeMillis;
    }

    /**
     * Whether to prefetch unseen mails only.
     * 
     * @return <code>true</code> if only unseen mails are prefetched; otherwise <code>false</code>
     */
    public boolean isUnseenOnly() {
        return unseenOnly;
    }

}
