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
     * Initialises a new {@link AbstractMailAuthenticityMechanismParser}.
     */
    public AbstractMailAuthenticityMechanismParser(DefaultMailAuthenticityMechanism mechanism, String... domainHeaders) {
        super();
        this.mechanism = mechanism;
        this.domainHeaders = domainHeaders;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
     */
    @Override
    public MailAuthenticityMechanismResult apply(Map<String, String> attributes, MailAuthenticityResult overallResult) {
        String value = attributes.remove(mechanism.getTechnicalName());
        AuthenticityMechanismResult authenticityMechanismResult = parseMechanismResult(extractOutcome(value.toUpperCase()));

        String domain = extractDomain(attributes, domainHeaders);
        boolean domainMatch = checkDomainMatch(overallResult, domain);

        return createResult(domain, authenticityMechanismResult, value, domainMatch, attributes);
    }

    /**
     * Parses the specified value to a valid {@link AuthenticityMechanismResult} via the {@link Enum#valueOf(Class, String)}
     * method of the corresponding {@link AuthenticityMechanismResult}
     *
     * @param value The value
     * @return The {@link AuthenticityMechanismResult}
     * @throws IllegalArgumenException if an invalid value is passed as argument
     */
    abstract AuthenticityMechanismResult parseMechanismResult(String value);

    /**
     * Creates a new {@link MailAuthenticityMechanismResult} with the specified domain, {@link AuthenticityMechanismResult}, mechanism name
     *
     * @param domain The domain
     * @param mechResult the {@link AuthenticityMechanismResult}
     * @param mechanismName The mechanism's name (may contain a comment in parenthesis)
     * @param domainMatch Whether there is a domain match
     * @param attributes
     * @return The new {@link MailAuthenticityMechanismResult}
     */
    abstract MailAuthenticityMechanismResult createResult(String domain, AuthenticityMechanismResult mechResult, String mechanismName, boolean domainMatch, Map<String, String> attributes);

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
            String value = attributes.get(key);
            if (Strings.isNotEmpty(value)) {
                return cleanseDomain(value);
            }
        }
        return null;
    }

    /**
     * Removes the optional version (if present) from the specified domain
     * and the preceding "at" symbol ('@') (if present) from the domain.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     * @param domain The domain to cleanse
     * @return The cleansed domain or <code>null</code> if the specified domain is <code>null</code>
     *         or empty in the first place
     */
    private String cleanseDomain(String domain) {
        if (Strings.isEmpty(domain)) {
            return domain;
        }
        String[] split = domain.split(" ");
        // Cleanse the optional version
        domain = split.length == 0 ? domain : split[0];
        int index = domain.indexOf('@');
        return index < 0 ? domain : domain.substring(index + 1);
    }

    /**
     * Extracts the outcome of the specified value.
     *
     * @param value the value to extract the outcome from
     * @return The extracted outcome
     */
    private String extractOutcome(String value) {
        int index = value.indexOf(' ');
        return (index < 0) ? value : value.substring(0, index);
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
