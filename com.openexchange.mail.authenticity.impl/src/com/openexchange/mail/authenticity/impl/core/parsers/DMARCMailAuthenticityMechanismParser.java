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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCProperty;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResultHeader;

/**
 * {@link DMARCMailAuthenticityMechanismParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DMARCMailAuthenticityMechanismParser extends AbstractMailAuthenticityMechanismParser {

    /**
     * Initialises a new {@link DMARCMailAuthenticityMechanismParser}.
     */
    public DMARCMailAuthenticityMechanismParser() {
        super(DefaultMailAuthenticityMechanism.DMARC, DMARCResultHeader.HEADER_FROM);
    }

    @Override
    protected AuthenticityMechanismResult parseMechanismResult(String value) {
        try {
            DMARCResult dmarcResult = DMARCResult.dmarcResultFor(value);
            return dmarcResult == null ? DMARCResult.FAIL : dmarcResult;
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            return DMARCResult.FAIL;
        }
    }

    @Override
    protected MailAuthenticityMechanismResult createResult(String domain, AuthenticityMechanismResult mechResult, String mechanismResult, boolean domainMatch, Map<String, String> attributes) {
        DMARCAuthMechResult result = new DMARCAuthMechResult(domain, (DMARCResult) mechResult);
        result.setReason(mechResult.getDisplayName());
        result.setDomainMatch(domainMatch);
        result.addProperty(DMARCProperty.FROM_DOMAIN, result.getDomain());
        result.addProperty(DMARCProperty.POLICY, extractPolicy(mechanismResult));
        return result;
    }

    private static final Pattern REGEX_POLICY = Pattern.compile("([a-zA-Z]+(\\s*)=(\\s*)[a-zA-Z]+\\s?)+");

    /**
     * Extracts the optional policy of the DMARC mechanism
     *
     * @param mechComment The mechanism comment
     * @return the policy if present, otherwise an empty string
     */
    private String extractPolicy(String mechComment) {
        Matcher m = REGEX_POLICY.matcher(mechComment);
        if (m.find()) {
            for (String pair : Strings.splitByWhitespaces(m.group())) {
                String[] split = Strings.splitBy(pair, '=', true);
                if (split.length == 2 && "p".equalsIgnoreCase(split[0])) {
                    return Strings.asciiLowerCase(split[1]);
                }
            }
        }
        return "";
    }

}
