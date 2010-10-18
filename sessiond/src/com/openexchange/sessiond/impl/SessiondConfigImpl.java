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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;

/**
 * SessionConfig
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class SessiondConfigImpl extends AbstractConfigWrapper implements SessiondConfigInterface {

    private static final Log LOG = LogFactory.getLog(SessiondConfigImpl.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private int sessionContainerTimeout = 4200000;
    private int numberOfSessionContainers = 8;
    private int maxSession = 5000;
    private int maxSessionPerUser;
    private int sessionDefaultLifeTime = 3600000;
    private int randomTokenTimeout = 60000;

    public SessiondConfigImpl(final ConfigurationService conf) {
        sessionContainerTimeout = parseProperty(conf, "com.openexchange.sessiond.sessionContainerTimeout", sessionContainerTimeout);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.sessionContainerTimeout=" + sessionContainerTimeout);
        }

        numberOfSessionContainers = parseProperty(conf, "com.openexchange.sessiond.numberOfSessionContainers", numberOfSessionContainers);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.numberOfSessionContainers=" + numberOfSessionContainers);
        }

        maxSession = parseProperty(conf, "com.openexchange.sessiond.maxSession", maxSession);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.maxSession=" + maxSession);
        }

        maxSessionPerUser = 0;
        maxSessionPerUser = parseProperty(conf, "com.openexchange.sessiond.maxSessionPerUser", maxSessionPerUser);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.maxSessionPerUser=" + maxSessionPerUser);
        }

        sessionDefaultLifeTime = parseProperty(conf, "com.openexchange.sessiond.sessionDefaultLifeTime", sessionDefaultLifeTime);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.sessionDefaultLifeTime=" + sessionDefaultLifeTime);
        }

        randomTokenTimeout = parseProperty(conf, "com.openexchange.sessiond.randomTokenTimeout", randomTokenTimeout);
        if (DEBUG) {
            LOG.debug("Sessiond property: com.openexchange.sessiond.randomTokenTimeout=" + randomTokenTimeout);
        }
    }

    public int getSessionContainerTimeout() {
        return sessionContainerTimeout;
    }

    public int getNumberOfSessionContainers() {
        return numberOfSessionContainers;
    }

    public int getMaxSessions() {
        return maxSession;
    }

    public int getMaxSessionsPerUser() {
        return maxSessionPerUser;
    }

    public int getLifeTime() {
        return sessionDefaultLifeTime;
    }

    public int getRandomTokenTimeout() {
        return randomTokenTimeout;
    }
}
