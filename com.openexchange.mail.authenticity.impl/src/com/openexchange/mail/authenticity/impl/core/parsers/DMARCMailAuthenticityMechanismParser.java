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
        } catch (IllegalArgumentException e) {
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
