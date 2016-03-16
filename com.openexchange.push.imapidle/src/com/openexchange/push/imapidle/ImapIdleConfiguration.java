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

package com.openexchange.push.imapidle;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.push.imapidle.ImapIdlePushListener.PushMode;
import com.openexchange.push.imapidle.locking.DbImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.HzImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
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

            String tmp = configService.getProperty("com.openexchange.push.imapidle.clusterLock", "hz").trim();
            if ("hz".equalsIgnoreCase(tmp)) {
                clusterLock = new HzImapIdleClusterLock("imapidle-2", services);
            } else if ("db".equalsIgnoreCase(tmp)) {
                clusterLock = new DbImapIdleClusterLock(services);
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

}
