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
import java.util.Map.Entry;
import java.util.function.BiFunction;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.mechanism.AbstractAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link AbstractMailAuthenticityMechanismParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractMailAuthenticityMechanismParser implements BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult> {

    private final DefaultMailAuthenticityMechanism mechanism;
    private final String[] domainHeaders;

    /**
     * Initializes a new {@link AbstractMailAuthenticityMechanismParser}.
     */
    protected AbstractMailAuthenticityMechanismParser(DefaultMailAuthenticityMechanism mechanism, String... domainHeaders) {
        super();
        this.mechanism = mechanism;
        this.domainHeaders = domainHeaders;
    }

    @Override
    public MailAuthenticityMechanismResult apply(Map<String, String> attributes, MailAuthenticityResult overallResult) {
        String result = attributes.remove(mechanism.getTechnicalName());
        AuthenticityMechanismResult authenticityMechanismResult = parseMechanismResult(extractOutcome(Strings.asciiLowerCase(result)));

        String domain = extractDomain(attributes, domainHeaders);
        boolean domainMatch = checkDomainMatch(overallResult, domain);

        return createResult(domain, authenticityMechanismResult, result, domainMatch, attributes);
    }

    /**
     * Parses the specified value to a valid mechanism result.
     *
     * @param value The value
     * @return The mechanism result
     * @throws IllegalArgumenException if an invalid value is passed as argument
     */
    protected abstract AuthenticityMechanismResult parseMechanismResult(String value);

    /**
     * Creates a new {@link MailAuthenticityMechanismResult} with the specified domain, {@link AuthenticityMechanismResult}, mechanism name
     *
     * @param domain The domain
     * @param mechResult The mechanism result
     * @param mechanismResult The mechanism's result string (may contain a comment in parenthesis); e.g. <code>"pass(p=REJECT)"</code>
     * @param domainMatch Whether there is a domain match
     * @param attributes
     * @return The new {@link MailAuthenticityMechanismResult}
     */
    protected abstract MailAuthenticityMechanismResult createResult(String domain, AuthenticityMechanismResult mechResult, String mechanismResult, boolean domainMatch, Map<String, String> attributes);

    /**
     * Checks whether there is a domain match between the domain extracted from the <code>From</code> header
     * and the domain extracted from the authenticity mechanism. In case of a match <code>true</code> is returned,
     * otherwise <code>false</code>.
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param domain The domain extracted from the mechanism result
     * @return <code>true</code> if there is a match between the domains, <code>false</code> otherwise
     */
    private boolean checkDomainMatch(MailAuthenticityResult overallResult, String domain) {
        String fromDomain = overallResult.getAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN, String.class);
        if (fromDomain == null) {
            return false;
        }
        return fromDomain.equalsIgnoreCase(domain);
    }

    /**
     * Extracts the domain value of the specified key from the specified attributes {@link Map}
     *
     * @param attributes The attributes {@link Map}
     * @param keys The keys
     * @return The cleansed domain if present, <code>null</code> if none exists
     */
    private String extractDomain(Map<String, String> attributes, String... keys) {
        for (String key : keys) {
            String attrValue = attributes.get(key);
            if (Strings.isNotEmpty(attrValue)) {
                return cleanseDomain(attrValue);
            }
        }
        return null;
    }

    /**
     * Removes the optional version (if present) from the specified domain
     * and the preceding "at" symbol ('@') (if present) from the domain.
     *
     * @param domain The domain to cleanse
     * @return The cleansed domain or <code>null</code> if the specified domain is <code>null</code>
     *         or empty in the first place
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     */
    private String cleanseDomain(String domain) {
        if (Strings.isEmpty(domain)) {
            return domain;
        }
        String cleansedDomain = domain;
        // Cleanse the optional version
        if (domain.indexOf(' ') >= 0) {
            String[] split = Strings.splitBy(domain, ' ', true);
            cleansedDomain = split.length == 0 ? domain : split[0];
        }
        // Cleanse the preceding "at" symbol ('@')
        if (domain.indexOf('@') >= 0) {
            int index = cleansedDomain.indexOf('@');
            cleansedDomain = cleansedDomain.substring(index + 1);
        }
        return cleansedDomain;
    }

    /**
     * Extracts the outcome of the specified value.
     *
     * @param value the value to extract the outcome from
     * @return The extracted outcome or <code>null</code> if the value is <code>null</code>
     */
    private String extractOutcome(String value) {
        if (Strings.isEmpty(value)) {
            return null;
        }
        int index = value.indexOf('(');
        String retval = (index < 0) ? value : value.substring(0, index);
        index = retval.indexOf(' ');
        return (index < 0) ? retval : retval.substring(0, index);
    }

    /**
     * Adds the specified attributes to the specified {@link MailAuthenticityMechanismResult}
     *
     * @param attributes The attributes to add
     * @param mechResult The {@link MailAuthenticityMechanismResult} to add the attributes to
     */
    protected void addProperties(Map<String, String> attributes, AbstractAuthMechResult mechResult) {
        for (Entry<String, String> entry : attributes.entrySet()) {
            mechResult.addProperty(entry.getKey(), entry.getValue());
        }
    }

    protected String compileReasonPhrase(AuthenticityMechanismResult mechResult, String phraseFragment, String address) {
        if (Strings.isEmpty(address)) {
            return mechResult.getDisplayName();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(mechResult.getDisplayName());
        builder.append(' ').append(phraseFragment).append(' ');
        builder.append(address);
        return builder.toString();
    }

    /**
     * Extracts an optional comment that may reside within parentheses
     *
     * @param value The value to extract the comment form
     * @return The optional extracted comment; <code>null</code> if no comment was found
     */
    protected String extractComment(String value) {
        int beginIndex = value.indexOf('(');
        if (beginIndex < 0) {
            return null;
        }
        int endIndex = value.indexOf(')');
        if (endIndex < 0) {
            return null;
        }
        value = Strings.unquote(value);
        return value.substring(beginIndex + 1, endIndex);
    }
}
