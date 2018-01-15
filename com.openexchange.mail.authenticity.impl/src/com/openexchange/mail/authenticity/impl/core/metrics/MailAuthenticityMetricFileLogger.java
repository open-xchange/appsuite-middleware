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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity.impl.core.metrics;

import java.util.List;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

/**
 * {@link MailAuthenticityMetricFileLogger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("unchecked")
public class MailAuthenticityMetricFileLogger implements MailAuthenticityMetricLogger {

    private final ch.qos.logback.classic.Logger metricLogger;

    /**
     * Initialises a new {@link MailAuthenticityMetricFileLogger}.
     */
    public MailAuthenticityMetricFileLogger() {
        super();
        metricLogger = initialiseLogger();
    }

    /**
     * Creates a new logger
     * 
     * @return A new call trace logger
     */
    private Logger initialiseLogger() {
        String logPath = "/var/log/open-xchange";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        return createLogger(createFileAppender(logPath, loggerContext, createPatternLayoutEncoder(loggerContext)));
    }

    /**
     * Creates the {@link PatternLayoutEncoder}. A simple encoder with the pattern '%message%n'
     * 
     * @param loggerContext The {@link LoggerContext}
     * @return The {@link PatternLayoutEncoder}
     */
    private PatternLayoutEncoder createPatternLayoutEncoder(LoggerContext loggerContext) {
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setPattern("%date - %message %n");
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.start();
        return patternLayoutEncoder;
    }

    /**
     * Creates a {@link RollingFileAppender} in the specified file system path, with the specified {@link LoggerContext} and {@link PatternLayoutEncoder}
     * 
     * @param logPath the file system logging path
     * @param loggerContext The {@link LoggerContext}
     * @param patternLayoutEncoder The {@link PatternLayoutEncoder}
     * @return The {@link RollingFileAppender}
     */
    private RollingFileAppender<ILoggingEvent> createFileAppender(String logPath, LoggerContext loggerContext, PatternLayoutEncoder patternLayoutEncoder) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setFile(logPath + "/mailAuthenticityMetrics.log.0");
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.setRollingPolicy(createRollingPolicy(logPath, loggerContext, fileAppender));
        fileAppender.setTriggeringPolicy(createTriggeringPolicy(loggerContext));
        fileAppender.start();
        return fileAppender;
    }

    /**
     * Creates the trace logger
     * 
     * @param fileAppender The {@link RollingFileAppender} for the logger
     * @return the {@link ch.qos.logback.classic.Logger}
     */
    private Logger createLogger(RollingFileAppender<ILoggingEvent> fileAppender) {
        Logger logger = (Logger) LoggerFactory.getLogger("com.openexchange.mail.authenticity.metrics.MailAuthenticityMetricLogger");
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        logger.setAdditive(false);
        return logger;
    }

    /**
     * Creates a {@link SizeBasedTriggeringPolicy} for the logger appender
     * 
     * @param loggerContext the {@link LoggerContext}
     * @return The {@link SizeBasedTriggeringPolicy}
     */
    private SizeBasedTriggeringPolicy<ILoggingEvent> createTriggeringPolicy(LoggerContext loggerContext) {
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(loggerContext);
        triggeringPolicy.setMaxFileSize(new FileSize(2 * FileSize.MB_COEFFICIENT));
        triggeringPolicy.start();
        return triggeringPolicy;
    }

    /**
     * Creates a {@link FixedWindowRollingPolicy} for the file appender
     * 
     * @param logPath the file system logging path
     * @param loggerContext The {@link LoggerContext}
     * @param fileAppender The {@link RollingFileAppender}
     * @return The {@link FixedWindowRollingPolicy}
     */
    private FixedWindowRollingPolicy createRollingPolicy(String logPath, LoggerContext loggerContext, RollingFileAppender<ILoggingEvent> fileAppender) {
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(logPath + "/mailAuthenticityMetrics.log.%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(99);
        rollingPolicy.setParent(fileAppender);
        return rollingPolicy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger#log(java.util.List, com.openexchange.mail.dataobjects.MailAuthenticityResult)
     */
    @Override
    public void log(List<String> rawHeaders, MailAuthenticityResult overallResult) {
        LeanConfigurationService leanConfigService = Services.getService(LeanConfigurationService.class);
        if (!leanConfigService.getBooleanProperty(MailAuthenticityProperty.LOG_METRICS)) {
            return;
        }
        switch (metricLogger.getLevel().toInt()) {
            case Level.DEBUG_INT:
                metricLogger.debug("{}, {}, {}", serialiseCodes(overallResult), serialiseRawHeaders(rawHeaders), serialiseTechnicalNames(overallResult));
                break;
            case Level.INFO_INT:
            default:
                metricLogger.info("{}", serialiseCodes(overallResult));
        }
    }

    /**
     * Serialises the specified {@link MailAuthenticityMechanismResult} for logging. Serialises only the codes
     * of the mechanisms and overall result in a form like: <code>1|1:4|2:3</code>. The first number designates
     * the code for the overall status, the pipe character '|' is used as a separator for key/values, and the
     * colon ':' separates the key from the value. The key/value part designates the mechanism and the result of
     * that mechanism. The previous example can be then translated to:
     * <code>Overall Result: fail, Mechanism Results: dkim=temperror, spf=fail</code>
     * 
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @return The serialised object
     */
    private Object serialiseCodes(MailAuthenticityResult overallResult) {
        StringBuilder serialised = new StringBuilder();
        serialised.append("R:").append(overallResult.getStatus().ordinal()).append("|");
        List<MailAuthenticityMechanismResult> knownResults = (List<MailAuthenticityMechanismResult>) overallResult.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS);
        if (knownResults == null || knownResults.isEmpty()) {
            serialised.setLength(serialised.length() - 1);
            return serialised.toString();
        }
        for (MailAuthenticityMechanismResult mechResult : knownResults) {
            MailAuthenticityMechanism mechanism = mechResult.getMechanism();
            serialised.append(mechanism.getCode()).append(":");
            AuthenticityMechanismResult result = mechResult.getResult();
            serialised.append(result.getCode()).append("|");
        }
        serialised.setLength(serialised.length() - 1);
        return serialised.toString();
    }

    /**
     * Serialises the {@link MailAuthenticityResult} for logging
     * 
     * @param overallResult The {@link MailAuthenticityResult} to serialised
     * @return the serialised object
     */
    private Object serialiseTechnicalNames(MailAuthenticityResult overallResult) {
        StringBuilder serialised = new StringBuilder();
        serialised.append("Overall Result: ").append(overallResult.getStatus().getTechnicalName().toLowerCase()).append(", ");
        List<MailAuthenticityMechanismResult> knownResults = (List<MailAuthenticityMechanismResult>) overallResult.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS);
        if (knownResults == null || knownResults.isEmpty()) {
            serialised.setLength(serialised.length() - 2);
            return serialised.toString();
        }
        serialised.append("Mechanism Results: ");
        for (MailAuthenticityMechanismResult mechResult : knownResults) {
            MailAuthenticityMechanism mechanism = mechResult.getMechanism();
            serialised.append(mechanism.getTechnicalName().toLowerCase()).append("=");
            AuthenticityMechanismResult result = mechResult.getResult();
            serialised.append(result.getTechnicalName().toLowerCase()).append(", ");
        }
        serialised.setLength(serialised.length() - 2);
        return serialised.toString();
    }

    /**
     * Serialises the specified raw headers for logging
     * 
     * @param rawHeaders The {@link List} with the raw headers to serialise
     * @return the serialised object
     */
    private Object serialiseRawHeaders(List<String> rawHeaders) {
        StringBuilder serialiser = new StringBuilder("Raw Headers: ");
        return serialiser.append(rawHeaders).toString();
    }
}
