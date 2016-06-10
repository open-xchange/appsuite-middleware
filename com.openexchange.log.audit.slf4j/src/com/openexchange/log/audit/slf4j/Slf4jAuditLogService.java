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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.log.audit.slf4j;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.java.Strings;
import com.openexchange.log.audit.Attribute;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;


/**
 * {@link Slf4jAuditLogService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Slf4jAuditLogService implements AuditLogService {

    private Logger createLogger(Configuration configuration) throws OXException {
        org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(AuditLogService.class);

        // Check if a dedicated file location is specified
        String fileLocation = configuration.getFileLocation();
        if (Strings.isEmpty(fileLocation)) {
            return slf4jLogger;
        }

        ch.qos.logback.classic.Logger templateLogger = (ch.qos.logback.classic.Logger) slf4jLogger;
        LoggerContext context = templateLogger.getLoggerContext();

        String filePattern = fileLocation;

        ExtendedPatternLayoutEncoder encoder = new ExtendedPatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(configuration.getFileLayoutPattern());

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(Integer.toString(configuration.getFileLimit()));

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(filePattern + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(configuration.getFileLimit());

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(context);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setFile(filePattern);
        rollingFileAppender.setName("Slf4jAuditLogAppender");
        rollingFileAppender.setPrudent(false);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);

        rollingPolicy.setParent(rollingFileAppender);

        encoder.start();
        rollingPolicy.start();
        rollingFileAppender.start();

        List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
        if (null != statuses && false == statuses.isEmpty()) {
            for (Status status : statuses) {
                if (status instanceof ErrorStatus) {
                    ErrorStatus errorStatus = (ErrorStatus) status;
                    Throwable throwable = errorStatus.getThrowable();
                    if (null == throwable) {
                        class FastThrowable extends Throwable {

                            private static final long serialVersionUID = -6877996474956999361L;

                            FastThrowable(String msg) {
                                super(msg);
                            }

                            @Override
                            public synchronized Throwable fillInStackTrace() {
                                return this;
                            }
                        }
                        throwable = new FastThrowable(errorStatus.getMessage());
                    }
                    throw new OXException(OXExceptionConstants.CODE_DEFAULT, OXExceptionStrings.MESSAGE, throwable, new Object[0]).setLogMessage(throwable.getMessage());
                }
            }
        }

        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("Slf4jAuditLogger");
        {
            ch.qos.logback.classic.Level l;
            Slf4jLogLevel iLevel = configuration.getLevel();
            switch (iLevel) {
                case DEBUG:
                    l = ch.qos.logback.classic.Level.DEBUG;
                    break;
                case ERROR:
                    l = ch.qos.logback.classic.Level.ERROR;
                    break;
                case INFO:
                    l = ch.qos.logback.classic.Level.INFO;
                    break;
                case TRACE:
                    l = ch.qos.logback.classic.Level.TRACE;
                    break;
                case WARN:
                    l = ch.qos.logback.classic.Level.WARN;
                    break;
                default:
                    l = ch.qos.logback.classic.Level.ALL;
                    break;
            }
            logbackLogger.setLevel(l);
        }
        logbackLogger.setAdditive(false);
        logbackLogger.addAppender(rollingFileAppender);

        return logbackLogger;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /** The logger to use */
    private final org.slf4j.Logger logger;

    /** The date formatter */
    private final DateFormatter dateFormatter;

    /** The log level */
    private final Slf4jLogLevel level;

    /** The attribute delimiter */
    private final String delimiter;

    /** Whether to include attribute names */
    private final boolean includeAttributeNames;

    /**
     * Initializes a new {@link Slf4jAuditLogService}.
     *
     * @throws OXException If initialization fails
     */
    public Slf4jAuditLogService(Configuration configuration) throws OXException {
        super();
        logger = createLogger(configuration);
        dateFormatter = configuration.getDateFormatter();
        level = configuration.getLevel();
        delimiter = null == configuration.getDelimiter() ? "" : configuration.getDelimiter();
        includeAttributeNames = configuration.isIncludeAttributeNames();
    }

    @Override
    public void log(String eventId, Attribute<?>... attributes) {
        String message = compileMessage(eventId, attributes);
        if (null != message) {
            switch (level) {
                case DEBUG:
                    logger.debug(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case TRACE:
                    logger.trace(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
                default:
                    logger.info(message);
                    break;
            }
        }
    }

    /**
     * Compiles the log message.
     *
     * @param eventId The event identifier
     * @param attributes The associated attributes
     * @return The compiled log message
     */
    private String compileMessage(String eventId, Attribute<?>[] attributes) {
        int length = null == attributes ? 0 : attributes.length;
        if (length == 0) {
            return eventId;
        }

        StringBuilder sb = new StringBuilder(length << 5);
        sb.append(eventId);

        for (Attribute<?> attribute : attributes) {
            if (null == attribute) {
                // An associated attribute is null. Discard the log event.
                return null;
            }

            // Delimiter
            sb.append(delimiter);

            // Attribute name
            if (includeAttributeNames) {
                sb.append(attribute.getName()).append('=');
            }

            // Attribute value
            if (attribute.isDate()) {
                sb.append(dateFormatter.format((Date) attribute.getValue()));
            } else {
                Object value = attribute.getValue();
                sb.append(null == value ? "null" : value.toString());
            }
        }
        return sb.toString();
    }

}
