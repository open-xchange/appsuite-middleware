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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.sessiond.impl.usertype.UserTypeSessiondConfigInterface;

/**
 * {@link SessiondConfigImpl} - The default {@link UserTypeSessiondConfigInterface} implementation.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SessiondConfigImpl implements SessiondConfigInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessiondConfigImpl.class);

    private static final long SHORT_CONTAINER_LIFE_TIME = 6L * 60L * 1000L; // 6 minutes
    private static final long LONG_CONTAINER_LIFE_TIME = 60L * 60L * 1000L; // 1 hour

    private final int maxSession;
    private final int maxSessionsPerClient;
    private final long sessionShortLifeTime;
    private final long randomTokenTimeout;
    private final long longLifeTime;
    private final boolean asyncPutToSessionStorage;
    private final String obfuscationKey;
    private final boolean removeFromSessionStorageOnTimeout;

    /**
     * Initializes a new {@link SessiondConfigImpl}.
     *
     * @param conf The configuration service
     */
    public SessiondConfigImpl(ConfigurationService conf) {
        super();
        maxSession = conf.getIntProperty("com.openexchange.sessiond.maxSession", 50000);
        LOG.debug("Sessiond property: com.openexchange.sessiond.maxSession={}", I(maxSession));

        maxSessionsPerClient = conf.getIntProperty("com.openexchange.sessiond.maxSessionPerClient", 0);
        LOG.debug("Sessiond property: com.openexchange.sessiond.maxSessionPerClient={}", I(maxSessionsPerClient));

        long sessionShortLifeTime = conf.getIntProperty("com.openexchange.sessiond.sessionDefaultLifeTime", ((int) (60L * 60L * 1000L)));
        String tmp = conf.getProperty("com.openexchange.sessiond.sessionLongLifeTime", "1W");
        long longLifeTime = ConfigTools.parseTimespan(tmp);
        if (sessionShortLifeTime < SHORT_CONTAINER_LIFE_TIME) {
            sessionShortLifeTime = SHORT_CONTAINER_LIFE_TIME;
        }
        if (longLifeTime < LONG_CONTAINER_LIFE_TIME) {
            longLifeTime = LONG_CONTAINER_LIFE_TIME;
        }
        if (longLifeTime < sessionShortLifeTime) {
            longLifeTime = sessionShortLifeTime;
        }
        this.sessionShortLifeTime = sessionShortLifeTime;
        this.longLifeTime = longLifeTime;
        LOG.debug("Sessiond property: com.openexchange.sessiond.sessionDefaultLifeTime={}", L(this.sessionShortLifeTime));
        LOG.debug("Sessiond property: com.openexchange.sessiond.sessionLongLifeTime={}", L(this.longLifeTime));

        tmp = conf.getProperty("com.openexchange.sessiond.randomTokenTimeout", Integer.toString(30000));
        randomTokenTimeout = ConfigTools.parseTimespan(tmp);
        LOG.debug("Sessiond property: com.openexchange.sessiond.randomTokenTimeout={}", L(randomTokenTimeout));

        tmp = conf.getProperty("com.openexchange.sessiond.asyncPutToSessionStorage", "true");
        asyncPutToSessionStorage = Boolean.parseBoolean(tmp.trim());

        obfuscationKey = conf.getProperty("com.openexchange.sessiond.encryptionKey", "auw948cz,spdfgibcsp9e8ri+<#qawcghgifzign7c6gnrns9oysoeivn");
        removeFromSessionStorageOnTimeout = conf.getBoolProperty("com.openexchange.sessiond.removeFromSessionStorageOnTimeout", false);
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
    public int getNumberOfSessionContainers() {
        return (int) (sessionShortLifeTime / SHORT_CONTAINER_LIFE_TIME);
    }

    @Override
    public int getMaxSessions() {
        return maxSession;
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
    public int getNumberOfLongTermSessionContainers() {
        long retval = (longLifeTime - sessionShortLifeTime) / LONG_CONTAINER_LIFE_TIME;
        return (int) ((retval < 1) ? 1 : retval);
    }

    @Override
    public long getLongTermSessionContainerTimeout() {
        return LONG_CONTAINER_LIFE_TIME;
    }

    @Override
    public String getObfuscationKey() {
        return obfuscationKey;
    }

    @Override
    public boolean isRemoveFromSessionStorageOnTimeout() {
        return removeFromSessionStorageOnTimeout;
    }

}
