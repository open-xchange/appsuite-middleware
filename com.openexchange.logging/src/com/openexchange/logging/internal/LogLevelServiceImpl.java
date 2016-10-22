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

package com.openexchange.logging.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.logging.LogLevelService;

/**
 * {@link LogLevelServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class LogLevelServiceImpl implements LogLevelService {

    /** The logger */
    protected static Logger LOG = LoggerFactory.getLogger(LogLevelServiceImpl.class);

    private final ConcurrentHashMap<String, ch.qos.logback.classic.Level> changedLogLevels = new ConcurrentHashMap<>();

    @Override
    public boolean set(String className, Level logLevel) {
        if (Strings.isEmpty(className)) {
            LOG.info("Class name is empty. Abort change.");
            return false;
        }
        ch.qos.logback.classic.Logger lLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(className);

        if (lLogger == null) {
            LOG.warn("Not able to check (and set) the log level for class {} to {} as no logger is defined.", className, logLevel.getName());
            return false;
        }

        ch.qos.logback.classic.Level effectiveLevel = lLogger.getEffectiveLevel();
        if (effectiveLevel == null) {
            LOG.warn("No log level found for class {}. Skipping change as we are not able to reset previous status.", className);
            return false;
        }

        ch.qos.logback.classic.Level newLevel = asSlf4jLevel(logLevel);
        lLogger.setLevel(newLevel);
        ch.qos.logback.classic.Level existing = this.changedLogLevels.putIfAbsent(className, effectiveLevel);
        if (null != existing) {
            LOG.warn("Not able to check (and set) the log level for class {} to {}. Another thread set log level {} concurrently", className, logLevel.getName(), existing.levelStr);
            return false;
        }

        LOG.info("Configured log level for class {} as {}.", className, logLevel.getName());
        return true;
    }

    private static ch.qos.logback.classic.Level asSlf4jLevel(java.util.logging.Level lbLevel) {
        if (lbLevel == null) {
            throw new IllegalArgumentException("Unexpected level [null]");
        }

        int levelInt = lbLevel.intValue();
        if (levelInt == java.util.logging.Level.ALL.intValue()) {
            return ch.qos.logback.classic.Level.ALL;
        } else if (levelInt == java.util.logging.Level.FINEST.intValue()) {
            return ch.qos.logback.classic.Level.TRACE;
        } else if (levelInt == java.util.logging.Level.FINE.intValue()) {
            return ch.qos.logback.classic.Level.DEBUG;
        } else if (levelInt == java.util.logging.Level.INFO.intValue()) {
            return ch.qos.logback.classic.Level.INFO;
        } else if (levelInt == java.util.logging.Level.WARNING.intValue()) {
            return ch.qos.logback.classic.Level.WARN;
        } else if (levelInt == java.util.logging.Level.SEVERE.intValue()) {
            return ch.qos.logback.classic.Level.ERROR;
        } else if (levelInt == java.util.logging.Level.OFF.intValue()) {
            return ch.qos.logback.classic.Level.OFF;
        } else {
            throw new IllegalArgumentException("Unexpected level [" + lbLevel + "]");
        }
    }

    @Override
    public void reset(String className) {
        if (Strings.isEmpty(className)) {
            LOG.info("Class name is empty. Abort change.");
            return;
        }

        ch.qos.logback.classic.Level level = this.changedLogLevels.remove(className);
        if (level == null) {
            LOG.warn("Cannot reset log level for class {}. No previous change found.", className);
            return;
        }
        ch.qos.logback.classic.Logger lLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(className);
        if (lLogger != null) {
            lLogger.setLevel(level);
        }
    }
}
