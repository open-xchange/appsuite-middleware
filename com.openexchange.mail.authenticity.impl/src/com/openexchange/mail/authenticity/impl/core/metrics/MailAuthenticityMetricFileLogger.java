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
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link MailAuthenticityMetricFileLogger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class MailAuthenticityMetricFileLogger implements MailAuthenticityMetricLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAuthenticityMetricFileLogger.class);
    private final LeanConfigurationService leanConfigService;

    /**
     * Initialises a new {@link MailAuthenticityMetricFileLogger}.
     */
    public MailAuthenticityMetricFileLogger(LeanConfigurationService leanConfigService) {
        super();
        this.leanConfigService = leanConfigService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger#log(java.util.List, com.openexchange.mail.dataobjects.MailAuthenticityResult)
     */
    @Override
    public void log(String mailId, List<String> rawHeaders, MailAuthenticityResult overallResult) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}", compileLog(mailId, rawHeaders, overallResult));
        }
    }

    /**
     * Compiles the entire log entry
     * 
     * @param mailId The mail identifier
     * @param rawHeaders a {@link List} with the raw headers of the message
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @return A {@link JSONObject} with the log entry
     */
    private JSONObject compileLog(String mailId, List<String> rawHeaders, MailAuthenticityResult overallResult) {
        JSONObject log = new JSONObject();
        try {
            logRawHeaders(log, rawHeaders);
            logMechanisms(log, overallResult);
            log.put(MailAuthenticityMetricLogField.mailId.name(), DigestUtils.sha256Hex(mailId).substring(0, 12));
            log.put(MailAuthenticityMetricLogField.domainMismatch.name(), overallResult.getAttribute(MailAuthenticityResultKey.DOMAN_MISMATCH, Boolean.class));
            log.put(MailAuthenticityMetricLogField.overallResult.name(), overallResult.getAttribute(MailAuthenticityResultKey.STATUS));
            log.put(MailAuthenticityMetricLogField.fromHeader.name(), overallResult.getAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN));
            return log;
        } catch (JSONException e) {
            LOGGER.error("Unable to compile debug log entry for mail with id '{}'", mailId, e);
        }
        return log;
    }

    /**
     * Log the raw headers if enabled.
     * 
     * @param log The log object
     * @param rawHeaders The {@link List} with the raw headers of the message
     * @throws JSONException if a JSON error is occurred
     */
    private void logRawHeaders(JSONObject log, List<String> rawHeaders) throws JSONException {
        if (!leanConfigService.getBooleanProperty(MailAuthenticityProperty.LOG_RAW_HEADERS)) {
            return;
        }
        JSONArray rawHeadersArray = new JSONArray();
        for (String rawHeader : rawHeaders) {
            rawHeadersArray.put(rawHeader);
        }
        log.put(MailAuthenticityMetricLogField.rawHeaders.name(), rawHeadersArray);
    }

    /**
     * Log the mechanisms
     * 
     * @param log The log object
     * @param overallResult The overall result containing the mechanisms
     * @throws JSONException if a JSON error is occurred
     */
    @SuppressWarnings("unchecked")
    private void logMechanisms(JSONObject log, MailAuthenticityResult overallResult) throws JSONException {
        List<MailAuthenticityMechanismResult> results = overallResult.getAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, List.class);
        JSONArray resultsArray = new JSONArray();
        if (null != results) {
            for (MailAuthenticityMechanismResult result : results) {
                logMechanism(resultsArray, result);
            }
        }
        log.put(MailAuthenticityMetricLogField.mechanismResults.name(), resultsArray);
    }

    /**
     * Log a single mechanism
     * 
     * @param resultsArray The array holding all the logged mechanism results
     * @param result The {@link MailAuthenticityMechanismResult}
     * @throws JSONException if a JSON error is occurred
     */
    private void logMechanism(JSONArray resultsArray, MailAuthenticityMechanismResult result) throws JSONException {
        JSONObject resultLog = new JSONObject();
        resultLog.put("result", result.getResult().getTechnicalName());
        for (String k : result.getProperties().keySet()) {
            resultLog.put(k, result.getProperties().get(k));
        }
        resultLog.put("domainMismatch", !result.isDomainMatch());
        resultsArray.put(resultLog);
    }
}
