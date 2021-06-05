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

package com.openexchange.push.imapidle;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.push.imapidle.ImapIdlePushListener.PushMode;
import com.openexchange.push.imapidle.locking.DbImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.HzImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.LocalImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.NoOpImapIdleClusterLock;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ImapIdleConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class ImapIdleConfiguration {

    private ImapIdleClusterLock clusterLock;
    private String fullName;
    private int accountId;
    private PushMode pushMode;
    private long delay;
    private boolean checkPeriodic;

    /**
     * Initializes a new {@link ImapIdleConfiguration}.
     */
    public ImapIdleConfiguration() {
        super();
    }

    public void init(ServiceLookup services) {
        ConfigurationService configService = services.getService(ConfigurationService.class);

        {
            fullName = configService.getProperty("com.openexchange.push.imapidle.folder", "INBOX").trim();
        }

        {
            String propName = "com.openexchange.push.imapidle.delay";
            String defaultDelay = "5000";

            String tmp = configService.getProperty(propName, defaultDelay).trim();
            try {
                delay = Long.parseLong(tmp);
            } catch (NumberFormatException e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(ImapIdleConfiguration.class);
                logger.warn("Property value set for \"{}\" is not a number: {}. Using fall-back value {} instead.", propName, tmp, defaultDelay);
                delay = 5000L;
            }
        }

        {
            String tmp = configService.getProperty("com.openexchange.push.imapidle.clusterLock.validateSessionExistence", "false").trim();
            boolean validateSessionExistence = Boolean.parseBoolean(tmp);

            tmp = configService.getProperty("com.openexchange.push.imapidle.clusterLock", "hz").trim();
            if ("hz".equalsIgnoreCase(tmp)) {
                clusterLock = new HzImapIdleClusterLock("imapidle-2", validateSessionExistence, services);
            } else if ("db".equalsIgnoreCase(tmp)) {
                clusterLock = new DbImapIdleClusterLock(validateSessionExistence, services);
            } else if ("local".equalsIgnoreCase(tmp)) {
                clusterLock = new LocalImapIdleClusterLock(validateSessionExistence, services);
            } else {
                clusterLock = new NoOpImapIdleClusterLock();
            }
        }

        {
            int tmp = Strings.parsePositiveInt(configService.getProperty("com.openexchange.push.imapidle.accountId", "0").trim());
            accountId = tmp < 0 ? 0 : accountId;
        }

        {
            PushMode tmp = PushMode.fromIdentifier(configService.getProperty("com.openexchange.push.imapidle.pushMode", "always").trim());
            pushMode = null == tmp ? PushMode.ALWAYS : tmp;
        }

        {
            String tmp = configService.getProperty("com.openexchange.push.imapidle.checkPeriodic", "false").trim();
            checkPeriodic = Boolean.parseBoolean(tmp);
        }
    }

    /**
     * Gets the cluster lock
     *
     * @return The cluster lock
     */
    public ImapIdleClusterLock getClusterLock() {
        return clusterLock;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the push mode
     *
     * @return The push mode
     */
    public PushMode getPushMode() {
        return pushMode;
    }

    /**
     * Gets the delay.
     *
     * @return The delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Signals whether existence of expired IMAP IDLE listeners should happen periodically or through waiting take.
     *
     * @return <code>true</code> for periodic; otherwise <code>false</code>
     */
    public boolean isCheckPeriodic() {
        return checkPeriodic;
    }

}
