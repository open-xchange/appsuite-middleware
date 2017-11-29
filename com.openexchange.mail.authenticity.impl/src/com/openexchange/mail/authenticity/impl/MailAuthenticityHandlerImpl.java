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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.authenticity.DefaultMailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResultHeader;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResultHeader;
import com.openexchange.mail.authenticity.mechanism.spf.SPFAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResultHeader;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * <p>{@link MailAuthenticityHandlerImpl} - The default core implementation of the {@link MailAuthenticityHandler}</p>
 * 
 * <p>This handler considers only the {@link DefaultMailAuthenticityMechanism#DMARC}, {@link DefaultMailAuthenticityMechanism#DKIM} and
 * {@link DefaultMailAuthenticityMechanism#SPF} in that particular order.</p>
 * 
 * <p>The default overall status of the {@link MailAuthenticityResult} is the {@link MailAuthenticityStatus#NEUTRAL}. If there are none of the above mentioned
 * mechnisms in the e-mail <code>Authentication-Results</code>, then that status applies. Unknown mechanisms and ptypes are ignored from the evaluation
 * but their raw data is included in the overall result's attributes under the {@link DefaultMailAuthenticityResultKey#UNKNOWN_AUTH_MECH_RESULTS} key.</p>
 * 
 * <p>In case there are multiple <code>Authentication-Results</code> in the e-mail's headers, then all of them are evaluated (top to bottom). Their mechanisms are sorted
 * by their predefined ordinal (DMARC > DKIM > SPF) and evaluated in that order.</p>
 * 
 * <p><code>Authentication-Results</code> headers with an invalid/unknown/absent <code>authserv-id</code> are simply ignored and <u>NOT</u> included in the result.</p>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityHandlerImpl implements MailAuthenticityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAuthenticityHandlerImpl.class);

    private static final MailAuthenticityMechanismComparator MAIL_AUTH_COMPARATOR = new MailAuthenticityMechanismComparator();

    private final Map<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegitry;

    /** The required mail fields of this handler */
    private static final Collection<MailField> REQUIRED_MAIL_FIELDS;
    static {
        Collection<MailField> m = new ArrayList<>();
        m.add(MailField.AUTHENTICATION_RESULTS);
        REQUIRED_MAIL_FIELDS = Collections.<MailField> unmodifiableCollection(m);
    }

    /** The required headers of this handler */
    private static final Collection<String> REQUIRED_HEADERS = Collections.emptyList();

    /** The ranking of this handler */
    private final int ranking;
    private final Cache<UserAndContext, List<AllowedAuthServId>> authServIdsCache;
    private final TrustedDomainAuthenticityHandler trustedDomainHandler;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link MailAuthenticityHandlerImpl} with priority 0.
     *
     * @throws IlegalArgumentException if the authServId is either <code>null</code> or empty
     */
    public MailAuthenticityHandlerImpl(ServiceLookup services) {
        this(0, services);
    }

    /**
     * Initialises a new {@link MailAuthenticityHandlerImpl}.1
     *
     * @param ranking The ranking of this handler; a higher value means higher priority;
     * @throws IlegalArgumentException if the authServId is either <code>null</code> or empty
     */
    public MailAuthenticityHandlerImpl(int ranking, ServiceLookup services) {
        super();
        this.services = services;
        this.trustedDomainHandler = services.getService(TrustedDomainAuthenticityHandler.class);
        this.ranking = ranking;
        this.authServIdsCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
        mechanismParsersRegitry = new HashMap<>(4);
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.DMARC, (line, overallResult) -> {
            String value = line.get(DefaultMailAuthenticityMechanism.DMARC.name().toLowerCase());
            DMARCResult dmarcResult = DMARCResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = line.get(DMARCResultHeader.HEADER_FROM);
            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // In case of a DMARC result != "none", set overall result to the DMARC result and continue with the next mechanism
            MailAuthenticityStatus mailAuthStatus = convert(dmarcResult);
            if (!mailAuthStatus.equals(MailAuthenticityStatus.NONE) && !domainMismatch) {
                overallResult.setStatus(mailAuthStatus);
            }

            DMARCAuthMechResult result = new DMARCAuthMechResult(cleanseDomain(domain), dmarcResult);
            result.setReason(extractComment(value));
            return result;
        });
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.DKIM, (line, overallResult) -> {
            String value = line.get(DefaultMailAuthenticityMechanism.DKIM.name().toLowerCase());
            DKIMResult dkimResult = DKIMResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = cleanseDomain(line.get(DKIMResultHeader.HEADER_I));
            if (Strings.isEmpty(domain)) {
                domain = cleanseDomain(line.get(DKIMResultHeader.HEADER_D));
            }

            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // In case of a DKIM result != "none", set overall result to the DKIM result and continue with the next mechanism
            MailAuthenticityStatus mailAuthStatus = convert(dkimResult);
            if (overallResult.getStatus().equals(MailAuthenticityStatus.NEUTRAL) && !mailAuthStatus.equals(MailAuthenticityStatus.NONE) && !domainMismatch) {
                overallResult.setStatus(mailAuthStatus);
            }

            DKIMAuthMechResult result = new DKIMAuthMechResult(cleanseDomain(domain), dkimResult);
            String reason = extractComment(value);
            if (Strings.isEmpty(reason)) {
                reason = line.get(DKIMResultHeader.REASON);
            }
            result.setReason(reason);
            return result;
        });
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.SPF, (line, overallResult) -> {
            String value = line.get(DefaultMailAuthenticityMechanism.SPF.name().toLowerCase());
            SPFResult spfResult = SPFResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = cleanseDomain(line.get(SPFResultHeader.SMTP_MAILFROM));
            if (Strings.isEmpty(domain)) {
                domain = cleanseDomain(line.get(SPFResultHeader.SMTP_HELO));
            }

            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // Set the overall result only if it's 'none'
            MailAuthenticityStatus mailAuthStatus = convert(spfResult);
            if (!domainMismatch && (overallResult.getStatus().equals(MailAuthenticityStatus.NEUTRAL) || (!overallResult.getStatus().equals(MailAuthenticityStatus.PASS) && mailAuthStatus.equals(MailAuthenticityStatus.PASS)))) {
                overallResult.setStatus(mailAuthStatus);
            }

            SPFAuthMechResult result = new SPFAuthMechResult(cleanseDomain(domain), spfResult);
            result.setReason(extractComment(value));
            return result;
        });
    }

    /**
     * Clears the authserv-ids cache.
     */
    public void invalidateAuthServIdsCache() {
        authServIdsCache.invalidateAll();
    }

    @Override
    public void handle(Session session, MailMessage mailMessage) throws OXException {
        HeaderCollection headerCollection = mailMessage.getHeaders();
        String[] authHeaders = headerCollection.getHeader(MailAuthenticityHandler.AUTH_RESULTS_HEADER);
        if (authHeaders == null || authHeaders.length == 0) {
            // TODO: Pass on to custom handlers; return neutral status for now
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            return;
        }

        String[] fromHeaders = headerCollection.getHeader(MessageHeaders.HDR_FROM);
        if (fromHeaders == null || fromHeaders.length == 0) {
            // TODO: Pass on to custom handlers; return neutral status for now
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            return;
        }

        List<AllowedAuthServId> allowedAuthServIds = getAllowedAuthServIds(session);
        mailMessage.setAuthenticityResult(parseHeaders(authHeaders, fromHeaders[0], allowedAuthServIds));

        if (trustedDomainHandler != null) {
            trustedDomainHandler.handle(session, mailMessage);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#getRequiredFields()
     */
    @Override
    public Collection<MailField> getRequiredFields() {
        return REQUIRED_MAIL_FIELDS;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#getRequiredHeaders()
     */
    @Override
    public Collection<String> getRequiredHeaders() {
        return REQUIRED_HEADERS;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#isEnabled(com.openexchange.session.Session)
     */
    @Override
    public boolean isEnabled(Session session) {
        LeanConfigurationService leanConfigService = services.getService(LeanConfigurationService.class);
        return leanConfigService.getBooleanProperty(session.getUserId(), session.getContextId(), MailAuthenticityProperty.enabled);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#getRanking()
     */
    @Override
    public int getRanking() {
        return ranking;
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Parses the specified <code>Authentication-Results</code> headers
     *
     * @param authHeaders The <code>Authentication-Results</code> headers to parse
     * @return The {@link MailAuthenticityResult}
     */
    private MailAuthenticityResult parseHeaders(String[] authHeaders, String fromHeader, List<AllowedAuthServId> allowedAuthServIds) {
        List<String> authHeadersList = extractValidAuthenticationResults(authHeaders, allowedAuthServIds);
        // No valid auth header was extracted, thus we return with neutral status
        if (authHeadersList.isEmpty()) {
            return MailAuthenticityResult.NEUTRAL_RESULT;
        }

        MailAuthenticityResult result = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        try {
            String domain = extractDomain(fromHeader);
            result.addAttribute(DefaultMailAuthenticityResultKey.FROM_DOMAIN, domain);
        } catch (Exception e) {
            // Malformed from header, be strict and return with failed result
            result.setStatus(MailAuthenticityStatus.FAIL);
            return result;
        }

        // Parse the mechanism results
        parseMechanismResults(authHeadersList, result);

        return result;
    }

    /**
     * Extracts the appropriate <code>Authentication-Results</code> header from the specified array
     * and ignores all headers that do not have a valid <code>authserv-id</code>.
     *
     * @param authHeaders The array with the authentication results headers
     * @return A {@link List} with the appropriate <code>Authentication-Results</code> headers or an empty {@link List} if none can be extracted
     */
    private List<String> extractValidAuthenticationResults(String[] authHeaders, List<AllowedAuthServId> allowedAuthServIds) {
        List<String> authHeadersList = new ArrayList<>(authHeaders.length);
        for (String header : authHeaders) {
            List<String> split = splitElements(header.trim());
            if (split.isEmpty()) {
                // Huh? Invalid/Malformed authenticity results header, ignore
                continue;
            }

            // The first property of the header MUST always be the domain (i.e. the authserv-id)
            // See https://tools.ietf.org/html/rfc7601 for the formal definition
            String authServId = split.get(0);
            if (!isAuthServIdValid(authServId, allowedAuthServIds)) {
                // Not a configured authserver-id, ignore
                continue;
            }
            authHeadersList.add(new String(header));
        }
        return authHeadersList;
    }

    /**
     * Extracts the domain of the sender from the specified <code>From</code> header and returns it
     *
     * @param fromHeader The from header
     * @return The domain of the sender
     * @throws IllegalArgumentException if the specified header does not contain any valid parsable Internet address
     */
    private String extractDomain(String fromHeader) {
        try {
            InternetAddress ia = new InternetAddress(fromHeader, true);
            String address = ia.getAddress();
            int index = address.indexOf('@');
            return address.substring(index + 1);
        } catch (AddressException e) {
            throw new IllegalArgumentException("The specified header does not contain any valid parsable internet addresses", e);
        }
    }

    /**
     * Parses all known mechanism results from the specified authResults {@link List}
     *
     * @param authHeadersList A {@link List} with all authentication results
     * @param result The {@link MailAuthenticityResult}
     */
    private void parseMechanismResults(List<String> authHeadersList, MailAuthenticityResult result) {
        Iterator<String> authResultsIterator = authHeadersList.iterator();
        List<MailAuthenticityMechanismResult> results = new ArrayList<>();
        List<String> unknownResults = new ArrayList<>();
        StringBuilder unknownAuthElements = new StringBuilder(128);
        while (authResultsIterator.hasNext()) {
            String authResult = authResultsIterator.next();
            List<String> authHeader = splitElements(authResult);
            // Sort by ordinal
            Collections.sort(authHeader, MAIL_AUTH_COMPARATOR);
            for (int index = 0; index < authHeader.size(); index++) {
                parseAuthHeaderElement(authHeader.get(index), unknownAuthElements, results, result);
            }
            if (unknownAuthElements.length() > 0) {
                unknownAuthElements.setLength(unknownAuthElements.length() - 2);
                unknownResults.add(unknownAuthElements.toString().trim());
                unknownAuthElements.setLength(0);
            }
            authResultsIterator.remove();
        }
        result.addAttribute(DefaultMailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, results);
        result.addAttribute(DefaultMailAuthenticityResultKey.UNKNOWN_AUTH_MECH_RESULTS, unknownResults);

        // Add the remaining attributes as is to the result
        if (!authHeadersList.isEmpty()) {
            unknownResults.addAll(authHeadersList);
        }
    }

    /**
     * Parses the specified authentication header element and adds it to it's appropriate list
     * 
     * @param authHeaderElement The authentication header element
     * @param unknownAuthElements The unknown authentication elements
     * @param results The {@link MailAuthenticityMechanismResult}s
     * @param result The overall {@link MailAuthenticityResult}
     */
    private void parseAuthHeaderElement(String authHeaderElement, StringBuilder unknownAuthElements, List<MailAuthenticityMechanismResult> results, MailAuthenticityResult result) {
        Map<String, String> attributes = parseLine(authHeaderElement);
        DefaultMailAuthenticityMechanism mechanism = getMechanism(attributes);
        if (mechanism == null) {
            // Unknown or not parsable mechanism
            unknownAuthElements.append(authHeaderElement).append("; ");
            return;
        }
        BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult> mechanismParser = mechanismParsersRegitry.get(mechanism);
        if (mechanismParser == null) {
            // Not a valid mechanism, skip but add to the overall result
            unknownAuthElements.append(authHeaderElement).append("; ");
            return;
        }
        MailAuthenticityMechanismResult mailAuthMechResult = mechanismParser.apply(attributes, result);
        results.add(mailAuthMechResult);
    }

    /**
     * Gets the mechanism from the specified attributes
     *
     * @param attributes The attributes
     * @return The {@link DefaultMailAuthenticityMechanism} or <code>null</code> if none exists
     */
    private DefaultMailAuthenticityMechanism getMechanism(Map<String, String> attributes) {
        for (DefaultMailAuthenticityMechanism mechanism : DefaultMailAuthenticityMechanism.values()) {
            if (attributes.containsKey(mechanism.name().toLowerCase())) {
                return mechanism;
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
     * Determines whether the specified authServId is valid
     *
     * @param authServId The authserv-id to check
     * @return <code>true</code> if the string is valid; <code>false</code> otherwise
     */
    private boolean isAuthServIdValid(String authServId, List<AllowedAuthServId> allowedAuthServIds) {
        if (Strings.isEmpty(authServId)) {
            return false;
        }
        String[] split = authServId.split(" ");

        // Cleanse the optional version
        authServId = split.length == 0 ? authServId : split[0];

        // Regex and wildcard checks...
        for (AllowedAuthServId allowedAuthServId : allowedAuthServIds) {
            if (allowedAuthServId.allows(authServId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the specified line to a {@link Map}.
     *
     * @param line The line to parse
     * @return A {@link Map} with the key/value pairs of the line
     */
    private Map<String, String> parseLine(String line) {
        Map<String, String> pairs = new HashMap<>();
        // No pairs; return as a singleton Map with the line being both the key and the value
        if (!line.contains("=")) {
            pairs.put(line, line);
            return pairs;
        }

        StringBuilder keyBuffer = new StringBuilder(32);
        StringBuilder valueBuffer = new StringBuilder(64);
        String key = null;
        boolean valueMode = false;
        boolean backtracking = false;
        int backtrackIndex = 0;
        for (int index = 0; index < line.length();) {
            char c = line.charAt(index);
            switch (c) {
                case '=':
                    if (valueMode) {
                        // A key found while in value mode, so we backtrack
                        backtracking = true;
                        valueMode = false;
                        index--;
                    } else {
                        // Retain the key and switch to value mode
                        key = keyBuffer.toString();
                        keyBuffer.setLength(0);
                        valueMode = true;
                        index++;
                    }
                    break;
                case ' ':
                    if (!valueMode) {
                        //Remove the key from the value buffer
                        valueBuffer.setLength(valueBuffer.length() - backtrackIndex);
                        pairs.put(key, valueBuffer.toString().trim());
                        // Retain the new key (and reverse if that key came from backtracking)
                        key = backtracking ? keyBuffer.reverse().toString() : keyBuffer.toString();
                        // Reset counters
                        keyBuffer.setLength(0);
                        valueBuffer.setLength(0);
                        // Skip to the value of the retained new key (position after the '=' sign)
                        index += backtrackIndex + 2;
                        backtrackIndex = 0;
                        backtracking = false;
                        valueMode = true;
                        break;
                    }
                    // while in value mode spaces are considered as literals, hence fall-through to 'default'
                default:
                    if (valueMode) {
                        // While in value mode append all literals to the value buffer
                        valueBuffer.append(c);
                        index++;
                    } else {
                        // While in key mode append all key literals to the key buffer...
                        keyBuffer.append(c);
                        if (backtracking) {
                            // ... and if we are backtracking, update the counters
                            index--;
                            backtrackIndex++;
                        } else {
                            // ... if we are not backtracking and we are in key mode, go forth
                            index++;
                        }
                    }
            }
        }
        // Add the last pair
        if (valueBuffer.length() > 0) {
            pairs.put(key, valueBuffer.toString());
        }
        return pairs;
    }

    /**
     * Splits the parametrised header to single elements using the semicolon (';')
     * as the split character
     *
     * @param header The header to split
     * @return A {@link List} with the split elements
     */
    private List<String> splitElements(String header) {
        List<String> split = new ArrayList<>();
        boolean openQuotes = false;
        StringBuilder lineBuffer = new StringBuilder(128);
        for (int index = 0; index < header.length(); index++) {
            char c = header.charAt(index);
            switch (c) {
                case '"':
                    openQuotes = !openQuotes;
                    lineBuffer.append(c);
                    break;
                case ';':
                    if (!openQuotes) {
                        split.add(lineBuffer.toString().trim());
                        lineBuffer.setLength(0);
                        break;
                    }
                default:
                    lineBuffer.append(c);
            }
        }
        // Add last one
        if (lineBuffer.length() > 0) {
            split.add(lineBuffer.toString().trim());
        }
        return split;
    }

    /**
     * Converts the specified string to a {@link DefaultMailAuthenticityMechanism}
     *
     * @param s The string to convert
     * @return the converted {@link DefaultMailAuthenticityMechanism}
     */
    private static DefaultMailAuthenticityMechanism convert(String s) {
        int index = s.indexOf(' ');
        if (index > 0) {
            s.substring(0, index);
        }
        try {
            return DefaultMailAuthenticityMechanism.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unknown mail authenticity mechanism '{}'", s);
        }
        return null;
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
     * Extracts an optional comment that may reside within parentheses
     *
     * @param value The value to extract the comment form
     * @return The optional extracted comment; <code>null</code> if no comment was found
     */
    private String extractComment(String value) {
        int beginIndex = value.indexOf('(');
        if (beginIndex < 0) {
            return null;
        }
        int endIndex = value.indexOf(')');
        if (endIndex < 0) {
            return null;
        }
        return value.substring(beginIndex + 1, endIndex);
    }

    /**
     * Converts the specified {@link AuthenticityMechanismResult} to {@link MailAuthenticityStatus}
     *
     * @param mechanismResult The {@link AuthenticityMechanismResult} to convert
     * @return The converted {@link MailAuthenticityStatus}. The status {@link MailAuthenticityStatus#NEUTRAL} might
     *         also get returned if the specified {@link AuthenticityMechanismResult} does not map to a valid {@link MailAuthenticityStatus}.
     *         The status {@link MailAuthenticityStatus#NONE} will be returned if the specified {@link AuthenticityMechanismResult} is <code>null</code>
     */
    private MailAuthenticityStatus convert(AuthenticityMechanismResult authMechResult) {
        if (authMechResult == null) {
            return MailAuthenticityStatus.NONE;
        }
        MailAuthenticityStatus mailAuthenticityStatus = MailAuthenticityStatus.NEUTRAL;
        try {
            mailAuthenticityStatus = MailAuthenticityStatus.valueOf(authMechResult.getTechnicalName().toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unknown mail authenticity status '{}'", authMechResult.getTechnicalName(), e);
        }
        return mailAuthenticityStatus;
    }

    /**
     * Checks whether there is a domain mismatch between the domain extracted from the <code>From</code> header
     * and the domain extracted from the authenticity mechanism. If there is a mismatch the overall status
     * is set to {@link MailAuthenticityStatus#NEUTRAL} and <code>true</code> is returned. Otherwise
     * no further action is performed and <code>false</code> is returned.
     * 
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param domain The domain extracted from the mechanism result
     * @return <code>true</code> if there is a mismatch between the domains, <code>false</code> otherwise
     */
    private boolean checkDomainMismatch(MailAuthenticityResult overallResult, String domain) {
        String fromDomain = overallResult.getAttribute(DefaultMailAuthenticityResultKey.FROM_DOMAIN, String.class);
        boolean domainMismatch = !fromDomain.equals(domain);
        if (domainMismatch) {
            overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
        }
        return domainMismatch;
    }

    /**
     * Gets the allowed authserv-ids for session-associated user.
     *
     * @param session The session
     * @return The allowed authserv-ids
     * @throws OXException If authserv-ids are missing
     */
    private List<AllowedAuthServId> getAllowedAuthServIds(Session session) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        List<AllowedAuthServId> authServIds = authServIdsCache.getIfPresent(key);
        return authServIds == null ? getAllowedAuthServIds(userId, contextId, key) : authServIds;
    }

    /**
     * Gets the allowed authserv-ids for the specified user in the specified context
     * and caches them for future use.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param key The cache key
     * @return The allowed authserv-ids
     * @throws OXException If authserv-ids are missing
     */
    private List<AllowedAuthServId> getAllowedAuthServIds(int userId, int contextId, UserAndContext key) throws OXException {
        // This is not thread-safe, but does not need to
        LeanConfigurationService leanConfigService = services.getService(LeanConfigurationService.class);
        String sAuthServIds = leanConfigService.getProperty(userId, contextId, MailAuthenticityProperty.authServId);
        if (Strings.isEmpty(sAuthServIds)) {
            throw MailAuthenticityExceptionCodes.INVALID_AUTHSERV_IDS.create();
        }

        List<String> tokens = Arrays.asList(Strings.splitByComma(sAuthServIds));
        if (tokens == null || tokens.isEmpty() || tokens.contains("")) {
            throw MailAuthenticityExceptionCodes.INVALID_AUTHSERV_IDS.create();
        }

        List<AllowedAuthServId> authServIds = AllowedAuthServId.allowedAuthServIdsFor(tokens);
        if (authServIds == null || authServIds.isEmpty()) {
            throw MailAuthenticityExceptionCodes.INVALID_AUTHSERV_IDS.create();
        }

        authServIdsCache.put(key, authServIds);
        return authServIds;
    }

    ///////////////////////////////// HELPER CLASSES /////////////////////////////////

    /**
     * {@link MailAuthenticityMechanismComparator} - Compares the {@link DefaultMailAuthenticityMechanism}s
     * according to their ordinal value
     *
     * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
     */
    private static class MailAuthenticityMechanismComparator implements Comparator<String> {

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(String o1, String o2) {
            String[] s1 = o1.split("=");
            String[] s2 = o2.split("=");
            DefaultMailAuthenticityMechanism mam1 = null;
            DefaultMailAuthenticityMechanism mam2 = null;
            if (s1.length > 0) {
                mam1 = convert(s1[0]);
            }
            if (s2.length > 0) {
                mam2 = convert(s2[0]);
            }
            if (mam1 != null && mam2 != null) {
                return mam1.compareTo(mam2);
            }
            if (mam1 == null && mam2 == null) {
                return 0;
            } else if (mam1 == null) {
                return 1;
            } else if (mam2 == null) {
                return -1;
            }
            return 0;
        }
    }
}
