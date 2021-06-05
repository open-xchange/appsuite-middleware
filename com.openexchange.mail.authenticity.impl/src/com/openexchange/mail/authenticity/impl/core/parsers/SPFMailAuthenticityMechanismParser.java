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

package com.openexchange.mail.authenticity.impl.core.parsers;

import java.util.Map;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityFragmentPhrases;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFProperty;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResultHeader;

/**
 * {@link SPFMailAuthenticityMechanismParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SPFMailAuthenticityMechanismParser extends AbstractMailAuthenticityMechanismParser {

    /**
     * Initialises a new {@link SPFMailAuthenticityMechanismParser}.
     */
    public SPFMailAuthenticityMechanismParser() {
        super(DefaultMailAuthenticityMechanism.SPF, SPFResultHeader.SMTP_MAILFROM, SPFResultHeader.SMTP_HELO);
    }

    @Override
    protected AuthenticityMechanismResult parseMechanismResult(String value) {
        try {
            SPFResult spfResult = SPFResult.spfResultFor(value);
            return spfResult == null ? SPFResult.FAIL : spfResult;
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            return SPFResult.FAIL;
        }
    }

    @Override
    protected MailAuthenticityMechanismResult createResult(String domain, AuthenticityMechanismResult mechResult, String mechanismResult, boolean domainMatch, Map<String, String> attributes) {
        SPFAuthMechResult result = new SPFAuthMechResult(domain, (SPFResult) mechResult);
        result.setDomainMatch(domainMatch);
        result.addProperty(SPFProperty.MAIL_FROM, result.getDomain());
        String reason;
        if (Strings.isNotEmpty(result.getClientIP())) {
            result.addProperty(SPFProperty.CLIENT_IP, result.getClientIP());
            reason = compileReasonPhrase(mechResult, MailAuthenticityFragmentPhrases.WITH_IP, result.getClientIP());
        } else {
            reason = compileReasonPhrase(mechResult, MailAuthenticityFragmentPhrases.WITH_DOMAIN, result.getDomain());
        }
        result.setReason(reason);
        return result;
    }
}
