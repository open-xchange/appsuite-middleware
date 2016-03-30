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

package liquibase.ext.logging.slf4j;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.core.AbstractLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the Liquibase Logger that sends log output to SLF4J.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class Slf4jLogger extends AbstractLogger {
    private static final int PRIORITY = 5;

    private Logger logger;
    private String changeLogName;
    private String changeSetName;

    /**
     * Takes the given logger name argument and associates it with a SLF4J logger.
     *
     * @param name The name of the logger.
     */
    @Override
    public void setName(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogLevel(String logLevel, String logFile) {
        super.setLogLevel(logLevel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
        changeLogName = (databaseChangeLog == null) ? null : databaseChangeLog.getFilePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeSet(ChangeSet changeSet) {
        changeSetName = (changeSet == null) ? null : changeSet.toString(false);
    }

    /**
     * Logs an severe message. Calls SLF4J {@link Logger#error(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void severe(String message) {
        this.logger.error("{}", buildMessage(message));
    }

    /**
     * Logs a severe message. Calls SLF4J {@link Logger#error(String, Throwable)}.
     *
     * @param message The message to log
     * @param throwable The exception to log.
     */
    @Override
    public void severe(String message, Throwable throwable) {
        this.logger.error("{}", buildMessage(message), throwable);
    }

    /**
     * Logs a warning message. Calls SLF4J {@link Logger#warn(String)}
     *
     * @param message The message to log.
     */
    @Override
    public void warning(String message) {
        this.logger.warn("{}", buildMessage(message));
    }

    /**
     * Logs a warning message. Calls SLF4J {@link Logger#warn(String, Throwable)}.
     *
     * @param message The message to log.
     * @param throwable The exception to log.
     */
    @Override
    public void warning(String message, Throwable throwable) {
        this.logger.warn("{}", buildMessage(message), throwable);
    }

    /**
     * Log an info message. Calls SLF4J {@link Logger#info(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void info(String message) {
        this.logger.info("{}", buildMessage(message));
    }

    /**
     * Log an info message. Calls SLF4J {@link Logger#info(String, Throwable)}.
     *
     * @param message The message to log.
     * @param throwable The exception to log.
     */
    @Override
    public void info(String message, Throwable throwable) {
        this.logger.info("{}", buildMessage(message), throwable);
    }

    /**
     * Log a debug message. Calls SLF4J {@link Logger#debug(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void debug(String message) {
        this.logger.debug("{}", buildMessage(message));
    }

    /**
     * Log a debug message. Calls SLF4J {@link Logger#debug(String, Throwable)}.
     *
     * @param message The message to log.
     * @param throwable The exception to log.
     */
    @Override
    public void debug(String message, Throwable throwable) {
        this.logger.debug("{}", buildMessage(message), throwable);
    }

    /**
     * Gets the logger priority for this logger. The priority is used by Liquibase to determine which logger to use.
     * The logger with the highest priority will be used. This implementation's priority is set to 5. Remove loggers
     * with higher priority numbers if needed.
     *
     * @return An integer (5)
     */
    @Override
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * Build a log message with optional data if it exists.
     *
     * @param message The basic log message before optional data.
     * @return the complete log message to print to the logger.
     */
    protected Object buildMessage(final String message) {
        final String changeLogName = this.changeLogName;
        final String changeSetName = this.changeSetName;
        return new Object() {

            @Override
            public String toString() {
                StringBuilder msg = new StringBuilder(256);
                if(changeLogName != null) {
                    msg.append(changeLogName).append(": ");
                }
                if(changeSetName != null) {
                    msg.append(changeSetName.replace(changeLogName + "::", "")).append(": ");
                }
                msg.append(message);
                return msg.toString();
            }
        };
    }
}
