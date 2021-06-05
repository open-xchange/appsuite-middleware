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

package com.openexchange.imap.debug;

import java.io.File;
import java.util.Date;
import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.imap.services.Services;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.java.Strings;
import com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.util.FileSize;

/**
 * {@link IMAPDebugLoggerGenerator} - Generates an appropriate debug logger for a given IMAP session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
@SuppressWarnings("deprecation")
public class IMAPDebugLoggerGenerator {

    private static final IMAPDebugLoggerGenerator INSTANCE = new IMAPDebugLoggerGenerator();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static IMAPDebugLoggerGenerator getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link IMAPDebugLoggerGenerator}.
     */
    private IMAPDebugLoggerGenerator() {
        super();
    }

    /**
     * Establishes logging for given IMAP session.
     *
     * @param imapSession The IMAP session to enable logging for
     * @param server The IMAP server
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalStateException If logging cannot be established for given IMAP session
     */
    public void establishLoggerFor(javax.mail.Session imapSession, String server, int userId, int contextId) {
        imapSession.setDebugOut(DevNullPrintStream.getInstance()); // Swallow superfluous JavaMail debug logging: "setDebug: JavaMail version x.y.z"
        imapSession.setDebug(true);
        imapSession.setDebugOut(new LoggerCallingPrintStream(imapSession, server, userId, contextId));
    }

    /**
     * Generates an appropriate logger for a given IMAP session.
     *
     * @param imapSession The IMAP session to enable logging for
     * @param server The IMAP server
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The logger
     * @throws IllegalStateException If logging cannot be established for given IMAP session
     */
    static org.slf4j.Logger generateLoggerFor(javax.mail.Session imapSession, String server, int userId, int contextId) {
        ConfigViewFactory configViewFactory = Services.optService(ConfigViewFactory.class);
        if (configViewFactory == null) {
            throw new IllegalStateException("No such service: " + ConfigViewFactory.class.getName());
        }

        try {
            ConfigView view = configViewFactory.getView(userId, contextId);

            String filePath = ConfigViews.getDefinedStringPropertyFrom("com.openexchange.imap.debugLog.file.path", "/var/log/open-xchange", view).trim();
            {
                StringBuilder filePathBuilder = new StringBuilder(filePath);
                if (!filePath.endsWith("/")) {
                    // Ensure ending slash character
                    filePathBuilder.append('/');
                }
                filePath = filePathBuilder.append("imaptrace_").append(contextId).append('_').append(userId).append('_').append(server).toString();
            }
            {
                File f = new File(filePath);
                if (f.exists()) {
                    if (!f.isDirectory()) {
                        throw new IllegalStateException("Failed to create directory: " + filePath + ". There is already a file with such a pathname.");
                    }
                } else {
                    if (!f.mkdir()) {
                        throw new IllegalStateException("Failed to create directory: " + filePath);
                    }
                }
            }
            filePath = new StringBuilder(filePath).append('/').toString();

            int fileSize = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.debugLog.file.size", 2097152, view);
            int fileCount = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.debugLog.file.count", 99, view);
            String layoutPattern = Strings.unquote(ConfigViews.getDefinedStringPropertyFrom("com.openexchange.imap.debugLog.file.pattern", "%message%n", view).trim());

            LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

            /*-
             * From https://jira.qos.ch/browse/LOGBACK-1359 to avoid possible ConcurrentModificationException when invoking logback
             * configurations concurrently:
             *
             * context .putObject(CoreConstants.FA_FILENAME_COLLISION_MAP, null);
             * context .putObject(CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP, null);
             *
             * or serialize logger configuration
             */

            String sHashCode = toPositiveString(imapSession.hashCode());
            StringBuilder filePatternBase = new StringBuilder(filePath).append("imaptrace_").append(ISO8601Utils.format(new Date(), false)).append('_').append(sHashCode).append(".log");
            int reslen = filePatternBase.length();
            String filePattern = filePatternBase.append(".0").toString();
            filePatternBase.setLength(reslen);

            synchronized (INSTANCE) {
                ExtendedPatternLayoutEncoder encoder = new ExtendedPatternLayoutEncoder();
                encoder.setContext(context);
                encoder.setPattern(layoutPattern);

                SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
                triggeringPolicy.setContext(context);
                triggeringPolicy.setMaxFileSize(FileSize.valueOf(Integer.toString(fileSize)));

                FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
                rollingPolicy.setContext(context);
                rollingPolicy.setFileNamePattern(filePatternBase.append(".%i").toString());
                filePatternBase = null;
                rollingPolicy.setMinIndex(1);
                rollingPolicy.setMaxIndex(fileCount);

                RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
                rollingFileAppender.setAppend(true);
                rollingFileAppender.setContext(context);
                rollingFileAppender.setEncoder(encoder);
                rollingFileAppender.setFile(filePattern);
                rollingFileAppender.setName(new StringBuilder("IMAPDebugLogAppender_").append(contextId).append('_').append(userId).append('_').append(server).append('_').append(sHashCode).toString());
                rollingFileAppender.setPrudent(false);
                rollingFileAppender.setRollingPolicy(rollingPolicy);
                rollingFileAppender.setTriggeringPolicy(triggeringPolicy);

                rollingPolicy.setParent(rollingFileAppender);

                encoder.start();
                triggeringPolicy.start();
                rollingPolicy.start();
                rollingFileAppender.start();

                List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
                if (null != statuses && false == statuses.isEmpty()) {
                    for (Status status : statuses) {
                        if (rollingFileAppender.equals(status.getOrigin()) && (status instanceof ErrorStatus)) {
                            ErrorStatus errorStatus = (ErrorStatus) status;
                            Throwable throwable = errorStatus.getThrowable();
                            if (null == throwable) {
                                class FastThrowable extends Throwable {

                                    private static final long serialVersionUID = -3677996474956999361L;

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
                            throw createException(server, userId, contextId, throwable);
                        }
                    }
                }

                ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(new StringBuilder("IMAPDebugLogger_").append(contextId).append('_').append(userId).append('_').append(server).append('_').append(sHashCode).toString());
                logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
                logbackLogger.setAdditive(false);
                logbackLogger.addAppender(rollingFileAppender);
                return logbackLogger;
            }
        } catch (OXException e) {
            Throwable cause = e.getCause();
            throw createException(server, userId, contextId, cause == null ? e : cause);
        }
    }

    private static IllegalStateException createException(String server, int userId, int contextId, Throwable throwable) {
        return new IllegalStateException("Failed to generate IMAP debug logger for server '" + server + "' of user " + userId + " in context " + contextId, throwable);
    }

    static String toPositiveString(int i) {
        String str = Integer.toString(i);
        return str.charAt(0)  == '-' ? str.substring(1) : str;
    }

}
