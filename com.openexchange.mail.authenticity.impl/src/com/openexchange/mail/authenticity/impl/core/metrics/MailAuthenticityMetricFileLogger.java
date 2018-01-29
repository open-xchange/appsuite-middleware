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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link MailAuthenticityMetricFileLogger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@SuppressWarnings("unchecked")
public class MailAuthenticityMetricFileLogger implements MailAuthenticityMetricLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAuthenticityMetricFileLogger.class);

    /**
     * Initialises a new {@link MailAuthenticityMetricFileLogger}.
     */
    public MailAuthenticityMetricFileLogger() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger#log(java.util.List, com.openexchange.mail.dataobjects.MailAuthenticityResult)
     */
    @Override
    public void log(String mailId, List<String> rawHeaders, MailAuthenticityResult overallResult) {
        LeanConfigurationService leanConfigService = Services.getService(LeanConfigurationService.class);
        if (!leanConfigService.getBooleanProperty(MailAuthenticityProperty.LOG_METRICS)) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MailId: {}, {}, {}, {}", mailId, serialiseCodes(overallResult), serialiseRawHeaders(rawHeaders), serialiseTechnicalNames(overallResult));
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
