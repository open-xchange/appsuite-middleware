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

package com.openexchange.mail.authentication.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.mail.authentication.MailAuthenticationHandler;
import com.openexchange.mail.authentication.MailAuthenticationResult;
import com.openexchange.mail.authentication.MailAuthenticationStatus;
import com.openexchange.mail.authentication.mechanism.MailAuthenticationMechanism;
import com.openexchange.mail.authentication.mechanism.MailAuthenticationMechanismResult;
import com.openexchange.mail.authentication.mechanism.dkim.DKIMAuthMechResult;
import com.openexchange.mail.authentication.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authentication.mechanism.dkim.DKIMResultHeader;
import com.openexchange.mail.authentication.mechanism.dmarc.DMARCAuthMechResult;
import com.openexchange.mail.authentication.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authentication.mechanism.spf.SPFAuthMechResult;
import com.openexchange.mail.authentication.mechanism.spf.SPFResult;
import com.openexchange.mail.authentication.mechanism.spf.SPFResultHeader;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link MailAuthenticationHandlerImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticationHandlerImpl implements MailAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailAuthenticationHandlerImpl.class);

    /**
     * Regex for checking whether each part of the domain is not longer than 63 characters,
     * and allow internationalised domain names using the punycode notation
     */
    //FIXME: Allow wildcards or regexes in the pattern
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("\\b((?=[a-z0-9-]{1,63}\\.)(xn--)?[a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,63}\\b");

    private static final MailAuthenticationMechanismComparator MAIL_AUTH_COMPARATOR = new MailAuthenticationMechanismComparator();

    private final Map<MailAuthenticationMechanism, Function<Map<String, String>, MailAuthenticationMechanismResult>> mechanismParsersRegitry;

    /**
     * Initialises a new {@link MailAuthenticationHandlerImpl}.
     */
    public MailAuthenticationHandlerImpl() {
        super();
        mechanismParsersRegitry = new HashMap<>(4);
        mechanismParsersRegitry.put(MailAuthenticationMechanism.DMARC, (line) -> {
            String value = line.get(MailAuthenticationMechanism.DMARC.name().toLowerCase());
            DMARCResult dmarcResult = DMARCResult.valueOf(value.toUpperCase());
            String domain = line.get("header.i");
            return new DMARCAuthMechResult(domain, dmarcResult);
        });
        mechanismParsersRegitry.put(MailAuthenticationMechanism.DKIM, (line) -> {
            String value = line.get(MailAuthenticationMechanism.DKIM.name().toLowerCase());
            DKIMResult dkimResult = DKIMResult.valueOf(value.toUpperCase());
            String domain = line.get(DKIMResultHeader.HEADER_I);
            return new DKIMAuthMechResult(domain, dkimResult);
        });
        mechanismParsersRegitry.put(MailAuthenticationMechanism.SPF, (line) -> {
            String value = line.get(MailAuthenticationMechanism.SPF.name().toLowerCase());
            SPFResult spfResult = SPFResult.valueOf(value.toUpperCase());
            String domain = line.get(SPFResultHeader.SMTP_MAILFROM);
            return new SPFAuthMechResult(domain, spfResult);
        });
    }

    public static void main(String[] args) {
        String[] authHeaders = { "mx.xyz.com; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=VvWVD9kg; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=0WC5u+VZ; dkim=pass header.i=@open-xchange.com header.s=201705 header.b=doOaQjgp; spf=pass (xyz.com: domain of jane.doe@open-xchange.com designates 1.2.3.4 as permitted sender) smtp.mailfrom=jane.doe@open-xchange.com; dmarc=pass (p=NONE sp=NONE dis=NONE) header.from=open-xchange.com" };
        MailAuthenticationHandlerImpl m = new MailAuthenticationHandlerImpl();
        MailAuthenticationResult r = m.parseHeaders(authHeaders);
        System.err.println(r);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authentication.MailAuthenticationHandler#handle(com.openexchange.mail.dataobjects.MailPart)
     */
    @Override
    public MailAuthenticationResult handle(MailPart mailPart) {
        //TODO: Perform preliminary configuration checks, 
        //      like whether the feature is enabled or 
        //      the core engine shall be used.
        //      Maybe move those checks to a higher layer in the framework.

        HeaderCollection headerCollection = mailPart.getHeaders();
        String[] authHeaders = headerCollection.getHeader(MailAuthenticationHandler.AUTH_RESULTS_HEADER);
        if (authHeaders == null || authHeaders.length == 0) {
            // TODO: Pass on to custom handlers; return null for now
            return null;
        }

        return parseHeaders(authHeaders);
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Parses the specified authentication headers
     * 
     * @param authHeaders
     * @return
     */
    private MailAuthenticationResult parseHeaders(String[] authHeaders) {
        MailAuthenticationResult result = new MailAuthenticationResult();
        result.setStatus(MailAuthenticationStatus.NOT_ANALYZED);

        // There can only be one, if there are more only the first one is relevant
        String[] split = authHeaders[0].split("; ");
        if (split.length == 0) {
            // Huh? Invalid/Malformed authentication results header, set to 'neutral'
            result.setStatus(MailAuthenticationStatus.NEUTRAL);
            return result;
        }

        // The first property of the header MUST always be the domain
        String domain = cleanseVersion(split[0]);
        if (!isValidDomain(domain)) {
            // Not a valid domain, thus we return with 'neutral' status
            result.setStatus(MailAuthenticationStatus.NEUTRAL);
            return result;
        }
        result.setDomain(domain);

        List<String> extractedMechanismResults = extractMechanismResults(Arrays.asList(Arrays.copyOfRange(split, 1, split.length)));
        parseMechanismResults(extractedMechanismResults, result);

        return result;
    }

    /**
     * @param extractedMechanismResults
     * @param result
     */
    private void parseMechanismResults(List<String> extractedMechanismResults, MailAuthenticationResult result) {
        // Sort by ordinal
        Collections.sort(extractedMechanismResults, MAIL_AUTH_COMPARATOR);
        for (String extractedMechanism : extractedMechanismResults) {
            String[] s = extractedMechanism.split("=");
            if (s.length == 0) {
                continue;
            }
            MailAuthenticationMechanism mechanism = convert(s[0]);
            if (mechanism == null) {
                continue;
            }
            result.addResult(mechanismParsersRegitry.get(mechanism).apply(parseMechanismResult(extractedMechanism)));
        }
    }

    /**
     * Extracts the supported mechanism results from the specified string array
     * 
     * @param authResults The authentication results
     * @return An unmodifiable {@link List} with the supported mechanism results
     */
    private List<String> extractMechanismResults(List<String> authResults) {
        List<String> list = new ArrayList<>();
        Iterator<String> authResultsIterator = authResults.iterator();
        while (authResultsIterator.hasNext()) {
            String authResult = authResultsIterator.next();
            for (MailAuthenticationMechanism mechanism : MailAuthenticationMechanism.values()) {
                if (authResult.startsWith(mechanism.name().toLowerCase())) {
                    list.add(authResult);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * Removes the optional version (if present) from the specified domain.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.2">RFC 7601, Section 2.2</a>
     * @param domain The domain
     * @return The cleansed domain
     */
    private String cleanseVersion(String domain) {
        String[] split = domain.split(" ");
        return split.length == 0 ? domain : split[0];
    }

    /**
     * Determines whether the specified string denotes a valid domain name
     * 
     * @param domain Domain candidate
     * @return <code>true</code> if the string denotes a valid domain, <code>false</code> otherwise
     */
    private boolean isValidDomain(String domain) {
        return DOMAIN_PATTERN.matcher(domain).matches();
    }

    ///////////////////////////////// HELPER CLASSES /////////////////////////////////

    /**
     * {@link MailAuthenticationMechanismComparator} - Compares the {@link MailAuthenticationMechanism}s
     * according to their ordinal value
     *
     * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
     */
    private static class MailAuthenticationMechanismComparator implements Comparator<String> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(String o1, String o2) {
            String[] s1 = o1.split("=");
            String[] s2 = o2.split("=");
            MailAuthenticationMechanism mam1 = null;
            MailAuthenticationMechanism mam2 = null;
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

    /**
     * Parses the specified mechanism result line to a {@link Map}.
     * 
     * @param mechanismResult The mechanism result header line
     * @return A {@link Map} with key/value pairs of the header line's attributes
     */
    private Map<String, String> parseMechanismResult(String mechanismResult) {
        String[] s = mechanismResult.split(" ");
        Map<String, String> resMap = new HashMap<>();
        for (String p : s) {
            String[] pair = p.split("=");
            if (pair.length != 2) {
                continue;
            }
            // TODO: Include the possible 'key-less' reason included in a parentheses?
            //       e.g. spf=pass (xyz.com: domain of jane.doe@open-xchange.com designates 1.2.3.4 as permitted sender)
            resMap.put(pair[0], pair[1]);
        }
        return resMap;
    }

    /**
     * Converts the specified string to a {@link MailAuthenticationMechanism}
     * 
     * @param s The string to convert
     * @return the converted {@link MailAuthenticationMechanism}
     */
    private static MailAuthenticationMechanism convert(String s) {
        try {
            return MailAuthenticationMechanism.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.debug("Unknown mail authentication mechanism '{}'", s);
        }
        return null;
    }
}
