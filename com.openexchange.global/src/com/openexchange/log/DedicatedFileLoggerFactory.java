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

package com.openexchange.log;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TriggeringPolicy;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.FileSize;

/**
 * {@link DedicatedFileLoggerFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
@SuppressWarnings("deprecation")
public class DedicatedFileLoggerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DedicatedFileLoggerFactory.class);

    private static final String DEFAULT_PATTERN = "%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"} %-5level [%thread] %class.%method\\(%class{0}.java:%line\\)%n%sanitisedMessage%exception{full}";

    private static final String DEFAULT_LOG_LEVEL = "error";

    /**
     * Creates a new one or re-initialises an already existing file logger
     * via the specified {@link LogConfiguration} instance.
     *
     * @param logConfiguration The {@link LogConfiguration} instance with the configuration
     *            of the dedicated file logger
     * @return The {@link Optional} {@link Logger}
     */
    public static Optional<Logger> createOrReinitializeLogger(LogConfiguration config) {
        return createOrReinitializeLogger(config, null);
    }

    /**
     * Creates a new one or re-initialises an already existing file logger
     * via the specified {@link LogConfiguration} instance.
     *
     * @param logConfiguration The {@link LogConfiguration} instance with the configuration
     *            of the dedicated file logger
     * @return The {@link Optional} {@link Logger}
     */
    public static Optional<Logger> createOrReinitializeLogger(LogConfiguration config, Logger seed) {
        if (false == config.isEnabledDedicatedLogging()) {
            return Optional.empty();
        }

        // Check if a dedicated file location is specified
        String fileLocation = config.getLoggingFileLocation();
        if (Strings.isEmpty(fileLocation)) {
            LOG.warn("File location for dedicated logging for logger '{}' is empty. Disabling logging...", config.getLoggerName());
            return Optional.empty();
        }
        ch.qos.logback.classic.Logger logbackLogger = ((ch.qos.logback.classic.Logger) (seed == null ? org.slf4j.LoggerFactory.getLogger(config.getLoggerName()) : seed));
        logbackLogger.detachAndStopAllAppenders();

        // Grab logger context from standard logger or the seed
        ch.qos.logback.classic.Logger templateLogger = (ch.qos.logback.classic.Logger) (seed == null ? LOG : seed);
        LoggerContext context = templateLogger.getLoggerContext();

        ExtendedPatternLayoutEncoder encoder = new ExtendedPatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(config.getLoggingPattern().orElseGet(() -> DEFAULT_PATTERN));

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(FileSize.valueOf(Integer.toString(config.getLoggingFileLimit())));

        FixedWindowRollingPolicy rollingPolicy = createRollingPpolicy(config, context, fileLocation);
        RollingFileAppender<ILoggingEvent> rollingFileAppender = createRollingFileAppender(config, context, fileLocation, encoder, triggeringPolicy, rollingPolicy);
        rollingPolicy.setParent(rollingFileAppender);

        encoder.start();
        triggeringPolicy.start();
        rollingPolicy.start();
        rollingFileAppender.start();

        if (checkForConfigErrors(context, rollingFileAppender)) {
            return Optional.empty();
        }

        logbackLogger.addAppender(rollingFileAppender);
        logbackLogger.setLevel(convertLevel(config.getLogLevel().orElseGet(() -> DEFAULT_LOG_LEVEL)));
        logbackLogger.setAdditive(false);

        return Optional.of(logbackLogger);
    }

    /**
     * Creates a {@link FixedWindowRollingPolicy} with the specified configuration
     *
     * @param config The {@link LogConfiguration}
     * @param context The {@link LoggerContext}
     * @param filePattern The file pattern
     * @return The {@link FixedWindowRollingPolicy}
     */
    private static FixedWindowRollingPolicy createRollingPpolicy(LogConfiguration config, LoggerContext context, String filePattern) {
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(filePattern + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(config.getLoggingFileCount());
        return rollingPolicy;
    }

    /**
     * Creates a {@link RollingFileAppender} with the specified configuration
     *
     * @param config The {@link LogConfiguration}
     * @param context The {@link LoggerContext}
     * @param filePattern The file pattern
     * @param encoder The encoder
     * @param triggeringPolicy The {@link TriggeringPolicy}
     * @param rollingPolicy The {@link RollingPolicy}
     * @return The {@link RollingFileAppender}
     */
    private static RollingFileAppender<ILoggingEvent> createRollingFileAppender(LogConfiguration config, LoggerContext context, String filePattern, ExtendedPatternLayoutEncoder encoder, SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy, FixedWindowRollingPolicy rollingPolicy) {
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(context);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setFile(filePattern);
        rollingFileAppender.setName(config.getLoggerName() + "Appender");
        rollingFileAppender.setPrudent(false);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);
        return rollingFileAppender;
    }

    /**
     * Converts the specified level to a Logging {@link Level}
     *
     * @param level The level to convert
     * @return The converted {@link Level}
     */
    private static ch.qos.logback.classic.Level convertLevel(String level) {
        switch (Strings.asciiLowerCase(level.trim())) {
            case "all":
                return ch.qos.logback.classic.Level.ALL;
            case "error":
                return ch.qos.logback.classic.Level.ERROR;
            case "warn":
            case "warning":
                return ch.qos.logback.classic.Level.WARN;
            case "info":
                return ch.qos.logback.classic.Level.INFO;
            case "debug":
                return ch.qos.logback.classic.Level.DEBUG;
            case "trace":
                return ch.qos.logback.classic.Level.TRACE;
            default:
                return ch.qos.logback.classic.Level.ERROR;
        }
    }

    /**
     * Checks the logger configuration for any errors. If there are errors present then
     * log those to the standard logger and return <code>true</code> to indicate that.
     *
     * @param context The {@link LoggerContext}
     * @param rollingFileAppender The {@link RollingFileAppender}
     * @return <code>true</code> if illegal configuration is detected; <code>false</code> otherwise
     */
    private static boolean checkForConfigErrors(LoggerContext context, RollingFileAppender<ILoggingEvent> rollingFileAppender) {
        List<ch.qos.logback.core.status.Status> statuses = context.getStatusManager().getCopyOfStatusList();
        if (statuses == null || statuses.isEmpty()) {
            return false;
        }
        for (ch.qos.logback.core.status.Status status : statuses) {
            if (false == rollingFileAppender.equals(status.getOrigin()) || !(status instanceof ch.qos.logback.core.status.ErrorStatus)) {
                continue;
            }

            ch.qos.logback.core.status.ErrorStatus errorStatus = (ch.qos.logback.core.status.ErrorStatus) status;
            Throwable throwable = checkThrowable(errorStatus);
            LOG.warn("Illegal logging configuration. Reason: '{}'. Disabling logging...", throwable.getMessage());
            return true;
        }
        return false;
    }

    /**
     * Checks the {@link ErrorStatus} and extracts its {@link Throwable}
     *
     * @param errorStatus The error status to check
     * @return The {@link Throwable} of the {@link ErrorStatus}
     */
    private static Throwable checkThrowable(ch.qos.logback.core.status.ErrorStatus errorStatus) {
        Throwable throwable = errorStatus.getThrowable();
        if (null != throwable) {
            return throwable;
        }
        return new FastThrowable(errorStatus.getMessage());
    }

    /**
     * {@link FastThrowable}
     */
    private static class FastThrowable extends Throwable {

        private static final long serialVersionUID = -1177996474876999361L;

        FastThrowable(String msg) {
            super(msg);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
