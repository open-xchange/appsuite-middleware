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

package com.openexchange.sessiond.impl;

import static com.openexchange.sessiond.SessiondProperty.SESSIOND_AUTOLOGIN;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;

/**
 * SessionConfig
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class SessiondConfigImpl implements SessiondConfigInterface {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessiondConfigImpl.class));
    private static final boolean DEBUG = LOG.isDebugEnabled();
    private static final long SHORT_CONTAINER_LIFE_TIME = 6L * 60L * 1000L;
    private static final long LONG_CONTAINER_LIFE_TIME = 60L * 60L * 1000L;

    private int maxSession = 50000;
    private int maxSessionsPerUser = 100;
    private int maxSessionsPerClient = 0;
    private long sessionShortLifeTime = 60L * 60L * 1000L;
    private long randomTokenTimeout = 30000L;
    private long longLifeTime = 7L * 24L * 60L * 60L * 1000L;
    private boolean autoLogin = false;
    private boolean asyncPutToSessionStorage = false;
    private String obfuscationKey = "auw948cz,spdfgibcsp9e8ri+<#qawcghgifzign7c6gnrns9oysoeivn";

    public SessiondConfigImpl(final ConfigurationService conf) {
        maxSession = conf.getIntProperty("com.openexchange.sessiond.maxSession", maxSession);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.maxSession=" + maxSession);
        }

        maxSessionsPerUser = conf.getIntProperty("com.openexchange.sessiond.maxSessionPerUser", maxSessionsPerUser);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.maxSessionPerUser=" + maxSessionsPerUser);
        }

        maxSessionsPerClient = conf.getIntProperty("com.openexchange.sessiond.maxSessionPerClient", maxSessionsPerClient);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.maxSessionPerClient=" + maxSessionsPerClient);
        }

        sessionShortLifeTime = conf.getIntProperty("com.openexchange.sessiond.sessionDefaultLifeTime", (int) sessionShortLifeTime);
        String tmp = conf.getProperty("com.openexchange.sessiond.sessionLongLifeTime", "1W");
        longLifeTime = ConfigTools.parseTimespan(tmp);
        if (sessionShortLifeTime < SHORT_CONTAINER_LIFE_TIME) {
            sessionShortLifeTime = SHORT_CONTAINER_LIFE_TIME;
        }
        if (longLifeTime < LONG_CONTAINER_LIFE_TIME) {
            longLifeTime = LONG_CONTAINER_LIFE_TIME;
        }
        if (longLifeTime < sessionShortLifeTime) {
            longLifeTime = sessionShortLifeTime;
        }
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.sessionDefaultLifeTime=" + sessionShortLifeTime);
        }
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.sessionLongLifeTime=" + longLifeTime);
        }

        tmp = conf.getProperty("com.openexchange.sessiond.randomTokenTimeout", "30000");
        randomTokenTimeout = ConfigTools.parseTimespan(tmp);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.randomTokenTimeout=" + randomTokenTimeout);
        }

        tmp = conf.getProperty(SESSIOND_AUTOLOGIN.getPropertyName(), SESSIOND_AUTOLOGIN.getDefaultValue());
        autoLogin = Boolean.parseBoolean(tmp.trim());

        tmp = conf.getProperty("com.openexchange.sessiond.asyncPutToSessionStorage", "false");
        asyncPutToSessionStorage = Boolean.parseBoolean(tmp.trim());

        obfuscationKey = conf.getProperty("com.openexchange.sessiond.encryptionKey", obfuscationKey);
    }

    @Override
    public boolean isAsyncPutToSessionStorage() {
        return asyncPutToSessionStorage;
    }

    @Override
    public long getSessionContainerTimeout() {
        return SHORT_CONTAINER_LIFE_TIME;
    }

    @Override
    public long getNumberOfSessionContainers() {
        return sessionShortLifeTime / SHORT_CONTAINER_LIFE_TIME;
    }

    @Override
    public int getMaxSessions() {
        return maxSession;
    }

    @Override
    public int getMaxSessionsPerUser() {
        return maxSessionsPerUser;
    }

    @Override
    public int getMaxSessionsPerClient() {
        return maxSessionsPerClient;
    }

    @Override
    public long getLifeTime() {
        return sessionShortLifeTime;
    }

    @Override
    public long getRandomTokenTimeout() {
        return randomTokenTimeout;
    }

    @Override
    public long getLongLifeTime() {
        return longLifeTime;
    }

    @Override
    public long getNumberOfLongTermSessionContainers() {
        long retval = (longLifeTime - sessionShortLifeTime) / LONG_CONTAINER_LIFE_TIME;
        return (retval < 1) ? 1 : retval;
    }

    @Override
    public boolean isAutoLogin() {
        return autoLogin;
    }

    @Override
    public long getLongTermSessionContainerTimeout() {
        return LONG_CONTAINER_LIFE_TIME;
    }

    @Override
    public String getObfuscationKey() {
        return obfuscationKey;
    }

}
