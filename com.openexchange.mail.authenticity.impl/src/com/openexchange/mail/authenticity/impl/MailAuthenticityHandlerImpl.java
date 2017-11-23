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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.DefaultMailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanism;
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
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityHandlerImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityHandlerImpl implements MailAuthenticityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAuthenticityHandlerImpl.class);

    private static final MailAuthenticityMechanismComparator MAIL_AUTH_COMPARATOR = new MailAuthenticityMechanismComparator();

    private final Map<MailAuthenticityMechanism, Function<Map<String, String>, MailAuthenticityMechanismResult>> mechanismParsersRegitry;

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

    private final String configuredAuthServId;

    /**
     * Initialises a new {@link MailAuthenticityHandlerImpl} with priority 0.
     */
    public MailAuthenticityHandlerImpl() {
        this(0);
    }

    /**
     * Initialises a new {@link MailAuthenticityHandlerImpl}.
     *
     * @param ranking The ranking of this handler; a higher value means higher priority;
     */
    public MailAuthenticityHandlerImpl(int ranking) {
        super();
        this.ranking = ranking;
        this.configuredAuthServId = ""; //TODO: Fetch from the config service
        mechanismParsersRegitry = new HashMap<>(4);
        mechanismParsersRegitry.put(MailAuthenticityMechanism.DMARC, (line) -> {
            String value = line.get(MailAuthenticityMechanism.DMARC.name().toLowerCase());
            DMARCResult dmarcResult = DMARCResult.valueOf(extractOutcome(value.toUpperCase()));
            String domain = line.get(DMARCResultHeader.HEADER_FROM);
            DMARCAuthMechResult result = new DMARCAuthMechResult(cleanseDomain(domain), dmarcResult);
            result.setReason(extractComment(value));
            return result;
        });
        mechanismParsersRegitry.put(MailAuthenticityMechanism.DKIM, (line) -> {
            String value = line.get(MailAuthenticityMechanism.DKIM.name().toLowerCase());
            DKIMResult dkimResult = DKIMResult.valueOf(extractOutcome(value.toUpperCase()));
            String domain = line.get(DKIMResultHeader.HEADER_I);
            if (Strings.isEmpty(domain)) {
                domain = line.get(DKIMResultHeader.HEADER_D);
            }
            DKIMAuthMechResult result = new DKIMAuthMechResult(cleanseDomain(domain), dkimResult);
            String reason = extractComment(value);
            if (Strings.isEmpty(reason)) {
                reason = line.get(DKIMResultHeader.REASON);
            }
            result.setReason(reason);
            return result;
        });
        mechanismParsersRegitry.put(MailAuthenticityMechanism.SPF, (line) -> {
            String value = line.get(MailAuthenticityMechanism.SPF.name().toLowerCase());
            SPFResult spfResult = SPFResult.valueOf(extractOutcome(value.toUpperCase()));
            String domain = line.get(SPFResultHeader.SMTP_MAILFROM);
            if (Strings.isEmpty(domain)) {
                domain = line.get(SPFResultHeader.SMTP_HELO);
            }
            SPFAuthMechResult result = new SPFAuthMechResult(cleanseDomain(domain), spfResult);
            result.setReason(extractComment(value));
            return result;
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#handle(com.openexchange.mail.dataobjects.MailPart)
     */
    @Override
    public void handle(Session session, MailMessage mailMessage) {
        //TODO: Perform preliminary configuration checks,
        //      like whether the feature is enabled or
        //      the core engine shall be used.
        //      Maybe move those checks to a higher layer in the framework.

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

        mailMessage.setAuthenticityResult(parseHeaders(authHeaders, fromHeaders[0]));
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
        // TODO Implement
        return true;
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
     * Parses the specified authenticity headers
     *
     * @param authHeaders The authenticity headers to parse
     * @return The {@link MailAuthenticityResult}
     */
    private MailAuthenticityResult parseHeaders(String[] authHeaders, String fromHeader) {
        String authHeader = extractAuthenticationResultHeader(authHeaders);
        // No valid auth header was extracted, thus we return with neutral status
        if (Strings.isEmpty(authHeader)) {
            return MailAuthenticityResult.NEUTRAL_RESULT;
        }

        MailAuthenticityResult result = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        try {
            String domain = parseDomain(fromHeader);
            result.addAttribute(DefaultMailAuthenticityResultKey.DOMAIN, domain);
        } catch (Exception e) {
            // Malformed from header, be strict and return with failed result
            result.setStatus(MailAuthenticityStatus.FAIL);
            return result;
        }

        // Get all attributes except the domain
        List<String> split = splitLines(authHeader);
        List<String> authResultLines = new ArrayList<>(split.size() - 1);
        authResultLines.addAll(split.subList(1, split.size()));
        // Extract and parse the known mechanisms
        List<String> extractedMechanismResults = extractMechanismResults(authResultLines);
        parseMechanismResults(extractedMechanismResults, result);

        //TODO: add the remaining attributes as is to the result

        return result;
    }

    /**
     * Extracts the appropriate <code>Authentication-Results</code> header from the specified array.
     * 
     * @param authHeaders The array with the authentication results headers
     * @return the appropriate <code>Authentication-Results</code> header or <code>null</code> if none can be extracted
     */
    private String extractAuthenticationResultHeader(String[] authHeaders) {
        String authHeader = null;
        for (String header : authHeaders) {
            List<String> split = splitLines(header);
            if (split.isEmpty()) {
                //Huh? Invalid/Malformed authenticity results header, ignore
                continue;
            }

            // The first property of the header MUST always be the domain (i.e. the authserv-id)
            // See https://tools.ietf.org/html/rfc7601 for the formal definition
            String authServId = split.get(0);
            if (!isAuthServIdValid(authServId)) {
                // Not a configured authserver-id, ignore
                continue;
            }
            authHeader = header;
        }
        return authHeader;
    }

    /**
     * Parses the specified from header and returns the domain of the sender
     * 
     * @param fromHeader The from header
     * @return The domain of the sender
     * @throws IllegalArgumentException if the specified header does not contain any valid parsable internet address
     */
    private String parseDomain(String fromHeader) {
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
     * @param extractedMechanismResults
     * @param result
     */
    private void parseMechanismResults(List<String> extractedMechanismResults, MailAuthenticityResult result) {
        // Sort by ordinal
        Collections.sort(extractedMechanismResults, MAIL_AUTH_COMPARATOR);
        List<MailAuthenticityMechanismResult> results = new ArrayList<>();
        Set<MailAuthenticityMechanism> mechanisms = new HashSet<>();
        for (String extractedMechanism : extractedMechanismResults) {
            String[] s = extractedMechanism.split("=");
            if (s.length == 0) {
                continue;
            }
            MailAuthenticityMechanism mechanism = convert(s[0]);
            if (mechanism == null) {
                continue;
            }
            MailAuthenticityMechanismResult mailAuthMechResult = mechanismParsersRegitry.get(mechanism).apply(parseLine(extractedMechanism));
            try {
                result.setStatus(MailAuthenticityStatus.valueOf(mailAuthMechResult.getResult().getTechnicalName().toUpperCase()));
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Unknown mail authenticity status '{}'", mailAuthMechResult.getResult().getTechnicalName(), e);
                result.setStatus(MailAuthenticityStatus.NEUTRAL);
            }
            mechanisms.add(mechanism);
            results.add(mailAuthMechResult);
        }

        result.addAttribute(DefaultMailAuthenticityResultKey.MAIL_AUTH_MECHS, mechanisms);
        result.addAttribute(DefaultMailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, results);
    }

    /**
     * Extracts the supported mechanism results from the specified string array
     *
     * @param authResults The authenticity results
     * @return An unmodifiable {@link List} with the supported mechanism results
     */
    private List<String> extractMechanismResults(List<String> authResults) {
        List<String> list = new ArrayList<>();
        Iterator<String> authResultsIterator = authResults.iterator();
        while (authResultsIterator.hasNext()) {
            String authResult = authResultsIterator.next();
            for (MailAuthenticityMechanism mechanism : MailAuthenticityMechanism.values()) {
                if (authResult.startsWith(mechanism.name().toLowerCase())) {
                    list.add(authResult);
                    authResultsIterator.remove();
                    break;
                }
            }
        }
        return list;
    }

    /**
     * Removes the optional version (if present) from the specified domain
     * and the preceding "at" symbol ('@') (if present) from the domain.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     * @param domain The domain to cleanse
     * @return The cleansed domain
     */
    private String cleanseDomain(String domain) {
        String[] split = domain.split(" ");
        // Cleanse the optional version
        domain = split.length == 0 ? domain : split[0];
        // TODO: Consider wildcards and regexes...
        //       e.g. mx[0-9]?.open-xchnge.com
        int index = domain.indexOf('@');
        return index < 0 ? domain : domain.substring(index + 1);
    }

    /**
     * Determines whether the specified authServId is valid
     * 
     * @param authServId The authserv-id to check
     * @return <code>true</code> if the string is valid; <code>false</code> otherwise
     */
    private boolean isAuthServIdValid(String authServId) {
        String[] split = authServId.split(" ");
        // Cleanse the optional version
        authServId = split.length == 0 ? authServId : split[0];

        if (Strings.isEmpty(authServId)) {
            return false;
        }
        // TODO: Regex and wildcard checks...
        if (configuredAuthServId.equals(authServId)) {
            return true;
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
     * Splits the parametrised header to lines
     *
     * @param header The header to split
     * @return A {@link List} with the split lines
     */
    private List<String> splitLines(String header) {
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
     * Converts the specified string to a {@link MailAuthenticityMechanism}
     *
     * @param s The string to convert
     * @return the converted {@link MailAuthenticityMechanism}
     */
    private static MailAuthenticityMechanism convert(String s) {
        int index = s.indexOf(' ');
        if (index > 0) {
            s.substring(0, index);
        }
        try {
            return MailAuthenticityMechanism.valueOf(s.toUpperCase());
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

    ///////////////////////////////// HELPER CLASSES /////////////////////////////////

    /**
     * {@link MailAuthenticityMechanismComparator} - Compares the {@link MailAuthenticityMechanism}s
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
            MailAuthenticityMechanism mam1 = null;
            MailAuthenticityMechanism mam2 = null;
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
