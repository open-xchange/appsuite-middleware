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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.authenticity.impl.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.mail.internet.InternetAddress;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.InterruptibleCharSequence.InterruptedRuntimeException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.parsers.DKIMMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.impl.core.parsers.DMARCMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.impl.core.parsers.SPFMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCPolicy;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCProperty;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;

/**
 * {@link StandardAuthenticationResultsValidator} - The standard parser and validator for <code>Authentication-Results</code> headers and <code>From</code> header of an E-Mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class StandardAuthenticationResultsValidator implements AuthenticationResultsValidator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StandardAuthenticationResultsValidator.class);

    private final Comparator<String> mailAuthComparator;
    private final Map<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegistry;

    /**
     * Initializes a new {@link StandardAuthenticationResultsValidator}.
     */
    protected StandardAuthenticationResultsValidator() {
        super();
        mailAuthComparator = new MailAuthenticityMechanismComparator();
        mechanismParsersRegistry = initialiseMechanismRegistry();
    }

    /**
     * Initializes the mechanism registry.
     *
     * @return An {@link ImmutableMap} with the mechanism implementations
     */
    protected ImmutableMap<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> initialiseMechanismRegistry() {
        ImmutableMap.Builder<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegistry = ImmutableMap.builder();
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DMARC, new DMARCMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DKIM, new DKIMMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.SPF, new SPFMailAuthenticityMechanismParser());
        return mechanismParsersRegistry.build();
    }

    @Override
    public MailAuthenticityResult parseHeaders(List<String> authenticationHeaders, InternetAddress from, List<AllowedAuthServId> allowedAuthServIds) throws OXException {
        MailAuthenticityResult overallResult = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        List<MailAuthenticityMechanismResult> results = new LinkedList<>();
        List<Map<String, String>> unconsideredResults = new LinkedList<>();

        try {
            Thread currentThread = Thread.currentThread();
            for (Iterator<String> authHeaderIterator = authenticationHeaders.iterator(); !currentThread.isInterrupted() && authHeaderIterator.hasNext();) {
                String authenticationHeader = authHeaderIterator.next();
                List<String> elements = StringUtil.splitElements(InterruptibleCharSequence.valueOf(authenticationHeader));

                // The first property of the header MUST always be the domain (i.e. the authserv-id)
                // See https://tools.ietf.org/html/rfc7601 for the formal definition
                String authServId = elements.get(0);
                if (!isAuthServIdValid(authServId, allowedAuthServIds)) {
                    // Not a configured authserver-id, ignore
                    continue;
                }
                // Remove the authserv-id
                elements = elements.subList(1, elements.size());

                // Extract the domain from the 'From' header
                try {
                    overallResult.addAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN, extractDomain(from));
                    overallResult.addAttribute(MailAuthenticityResultKey.TRUSTED_SENDER, from.getAddress());
                } catch (IllegalArgumentException e) {
                    // Malformed 'From' header, return with 'Not Analyzed' result
                    LOGGER.debug("An error occurred while trying to extract a valid domain from the 'From' header", e);
                    overallResult.setStatus(MailAuthenticityStatus.NOT_ANALYZED);
                    return overallResult;
                }

                parseMechanisms(elements, results, unconsideredResults, overallResult, currentThread);
            }
        } catch (InterruptedRuntimeException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create();
        }

        determineOverallResult(overallResult, results);

        overallResult.addAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, results);
        overallResult.addAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, unconsideredResults);

        // Set overall status to 'neutral'. This implies that:
        //  a) no auth-servId match was found, hence no mechanisms was parsed, or
        //  b) we only came across unknown mechanisms, which we don't take
        //     into consideration when we determine the overall result.
        //  c) no mechanism result of any known mechanisms was present.
        //  d) the overall status was set to internal status of 'none'
        if (results.isEmpty() || overallResult.getStatus().equals(MailAuthenticityStatus.NONE)) {
            overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
        }

        return overallResult;
    }

    /**
     * Determines whether the specified authServId is valid
     *
     * @param authServId The authserv-id to check
     * @return <code>true</code> if the string is valid; <code>false</code> otherwise
     */
    protected boolean isAuthServIdValid(String authServId, List<AllowedAuthServId> allowedAuthServIds) {
        if (Strings.isEmpty(authServId)) {
            LOGGER.warn("The authserv-id is missing from the 'Authentication-Results'");
            return false;
        }

        // Cleanse the optional version
        {
            String[] tokens = Strings.splitBy(authServId, ' ', true);
            authServId = tokens.length == 0 ? authServId : tokens[0];
        }

        // Regex and wild-card checks...
        for (AllowedAuthServId allowedAuthServId : allowedAuthServIds) {
            if (allowedAuthServId.allows(authServId)) {
                return true;
            }
        }
        LOGGER.debug("The authserv-id '{}' is not in the list of allowed authserv ids (see server property '{}'). Server misconfiguration?", authServId, MailAuthenticityProperty.AUTHSERV_ID);
        return false;
    }

    /**
     * Extracts the domain from the specified internet address
     *
     * @param adr The address as string
     * @return The domain of the sender
     * @throws IllegalArgumentException if the address is either empty or <code>null</code
     */
    protected String extractDomain(InternetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("The address can be neither empty nor null");
        }
        String adr = address.getAddress();
        int index = adr.indexOf('@');
        return adr.substring(index + 1);
    }

    /**
     * Extracts the outcome of the specified value.
     *
     * @param value the value to extract the outcome from
     * @return The extracted outcome
     */
    protected String extractOutcome(String value) {
        int index = value.indexOf(' ');
        return (index < 0) ? value : value.substring(0, index);
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
        if (beginIndex >= endIndex) {
            return null;
        }
        value = Strings.unquote(value);
        return value.substring(beginIndex + 1, endIndex);
    }

    /**
     * Parses the mechanisms (known and unknown) and adds the results to their respective {@link List}s
     *
     * @param elements A {@link List} with the elements of a single <code>Authentication-Results</code> header.
     * @param results A {@link List} with the results of the known mechanisms
     * @param unconsideredResults A {@link List} with the unconsidered results
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param currentThread The current thread
     */
    protected void parseMechanisms(List<String> elements, List<MailAuthenticityMechanismResult> results, List<Map<String, String>> unconsideredResults, MailAuthenticityResult overallResult, Thread currentThread) {
        Collections.sort(elements, mailAuthComparator);
        for (Iterator<String> iterator = elements.iterator(); !currentThread.isInterrupted() && iterator.hasNext();) {
            String element = iterator.next();
            if (Strings.isEmpty(element)) {
                continue;
            }
            Map<String, String> attributes = StringUtil.parseMap(InterruptibleCharSequence.valueOf(element));

            DefaultMailAuthenticityMechanism mechanism = DefaultMailAuthenticityMechanism.extractMechanism(attributes);
            if (mechanism == null) {
                // Unknown or not parsable mechanism
                Map<String, String> unknownMechs = parseUnknownMechs(element);
                if (unknownMechs != null && !unknownMechs.isEmpty()) {
                    unconsideredResults.add(unknownMechs);
                }
                continue;
            }
            BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult> mechanismParser = mechanismParsersRegistry.get(mechanism);
            if (mechanismParser == null) {
                // Not a valid mechanism, skip but add to the overall result
                Map<String, String> unknownMechs = parseUnknownMechs(element);
                if (unknownMechs != null && !unknownMechs.isEmpty()) {
                    unconsideredResults.add(unknownMechs);
                }
                continue;
            }
            results.add(mechanismParser.apply(attributes, overallResult));
        }
    }

    /**
     * Parses the unknown mechanism's attributes and returns those as a {@link Map}
     *
     * @param element The element to parse
     * @return A {@link Map} with the parsed attributes of the unknown mechanism
     */
    protected Map<String, String> parseUnknownMechs(final String element) {
        List<MailAuthenticityAttribute> attributes = StringUtil.parseList(InterruptibleCharSequence.valueOf(element));
        if (attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> unknownResults = new HashMap<>();
        // First element is always the mechanism
        MailAuthenticityAttribute mechanism = attributes.get(0);
        unknownResults.put("mechanism", mechanism.getKey());
        unknownResults.put("result", extractOutcome(mechanism.getValue()));

        for (int index = 1; index < attributes.size(); index++) {
            MailAuthenticityAttribute attribute = attributes.get(index);
            unknownResults.put(attribute.getKey(), attribute.getValue());
        }

        if (!unknownResults.containsKey("reason")) {
            String reason = extractComment(mechanism.getValue());
            if (Strings.isNotEmpty(reason)) {
                unknownResults.put("reason", reason);
            }
        }

        return unknownResults;
    }

    /**
     * Determines the most reliably authenticated sender domain and whether there is a mismatch between
     * it and the domain from the 'From' header.
     * Sets the {@link MailAuthenticityResultKey#DOMAIN_MISMATCH} to the attributes of the overall result.
     *
     * @param overallResult The overall result
     * @param bestOfDMARC The best DMARC result
     * @param bestOfDKIM The best DKIM result
     * @param bestOfSPF The best SPF result
     */
    private void determineReliableAuthenticatedSender(MailAuthenticityResult overallResult, MailAuthenticityMechanismResult bestOfDMARC, MailAuthenticityMechanismResult bestOfDKIM, MailAuthenticityMechanismResult bestOfSPF) {
        String fromDomain = overallResult.getAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN, String.class);
        for (MailAuthenticityMechanismResult result : Arrays.asList(bestOfDMARC, bestOfDKIM, bestOfSPF)) { // check in authenticity-descending order
            if (result == null) {
                continue;
            }
            if (result.getResult().convert() != MailAuthenticityStatus.PASS && result.getResult().convert() != MailAuthenticityStatus.NEUTRAL) {
                continue;
            }
            String domain = result.getDomain();
            if (domain == null) {
                continue;
            }
            overallResult.addAttribute(MailAuthenticityResultKey.FROM_DOMAIN, domain);
            overallResult.addAttribute(MailAuthenticityResultKey.DOMAIN_MECH, result.getMechanism().getTechnicalName());
            overallResult.addAttribute(MailAuthenticityResultKey.DOMAIN_MISMATCH, !domain.equalsIgnoreCase(fromDomain));
            break;
        }
    }

    /**
     * Separates the results into different containers according to their type
     *
     * @param results All the {@link MailAuthenticityMechanismResult}s
     * @return The results separated by SPF, DKIM and DMARC
     */
    protected SeparatedResults separateAndClearResults(List<MailAuthenticityMechanismResult> results) {
        // Declare containers for SPF, DKIM and DMARC
        List<MailAuthenticityMechanismResult> spfResults = null;
        List<MailAuthenticityMechanismResult> dkimResults = null;
        List<MailAuthenticityMechanismResult> dmarcResults = null;

        for (MailAuthenticityMechanismResult result : results) {
            DefaultMailAuthenticityMechanism mechanism = (DefaultMailAuthenticityMechanism) result.getMechanism();
            switch (mechanism) {
                case DMARC:
                    if (null == dmarcResults) {
                        dmarcResults = new LinkedList<>();
                    }
                    dmarcResults.add(result);
                    break;
                case DKIM:
                    if (null == dkimResults) {
                        dkimResults = new LinkedList<>();
                    }
                    dkimResults.add(result);
                    break;
                case SPF:
                    if (null == spfResults) {
                        spfResults = new LinkedList<>();
                    }
                    spfResults.add(result);
                    break;
            }
        }

        // Remove everything from the initial list
        results.clear();

        return new SeparatedResults(spfResults, dkimResults, dmarcResults);
    }

    /**
     * Picks the best {@link MailAuthenticityMechanismResult} from the specified {@link List} of results.
     *
     * @param results The {@link List} with the {@link MailAuthenticityMechanismResult}s
     * @return The best {@link MailAuthenticityMechanismResult} according to their natural ordering,
     *         or <code>null</code> if the {@link List} is empty, or the first (and only) element
     *         if the {@link List} is a singleton
     */
    protected MailAuthenticityMechanismResult pickBestResult(List<MailAuthenticityMechanismResult> results) {
        int size = results.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return results.get(0);
        }

        MailAuthenticityMechanismResult bestResult = null;
        for (MailAuthenticityMechanismResult result : results) {
            if (null == bestResult) {
                bestResult = result;
                continue;
            }
            if (result.getResult().getCode() < bestResult.getResult().getCode()) {
                bestResult = result;
            } else if (result.getResult().getCode() == bestResult.getResult().getCode() && result.isDomainMatch()) {
                bestResult = result;
            }
        }

        return bestResult;
    }

    ///////////////////////////////// HELPER CLASSES /////////////////////////////////

    /**
     * {@link MailAuthenticityMechanismComparator} - Compares the {@link DefaultMailAuthenticityMechanism}s
     * according to their ordinal value
     *
     * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
     */
    public static class MailAuthenticityMechanismComparator implements Comparator<String> {

        /**
         * Initializes a new {@link MailAuthenticityHandlerImpl.MailAuthenticityMechanismComparator}.
         */
        MailAuthenticityMechanismComparator() {
            super();
        }

        @Override
        public int compare(String o1, String o2) {
            String[] s1 = o1.split("=");
            String[] s2 = o2.split("=");
            DefaultMailAuthenticityMechanism mam1 = null;
            DefaultMailAuthenticityMechanism mam2 = null;
            if (s1.length > 0) {
                mam1 = DefaultMailAuthenticityMechanism.parse(s1[0]);
            }
            if (s2.length > 0) {
                mam2 = DefaultMailAuthenticityMechanism.parse(s2[0]);
            }
            if (mam1 != null && mam2 != null) {
                return mam1.compareTo(mam2);
            }
            if (mam1 == null && mam2 == null) {
                return 0;
            } else if (mam1 == null) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /** Simple helper class to wrap containers for SPF, DKIM and DMARC results */
    public static class SeparatedResults {

        private final List<MailAuthenticityMechanismResult> spfResults;
        private final List<MailAuthenticityMechanismResult> dkimResults;
        private final List<MailAuthenticityMechanismResult> dmarcResults;

        /**
         * Initializes a new {@link SeparatedResults}.
         *
         * @param spfResults The container for SPF results
         * @param dkimResults The container for DKIM results
         * @param dmarcResults The container for DMARC results
         */
        SeparatedResults(List<MailAuthenticityMechanismResult> spfResults, List<MailAuthenticityMechanismResult> dkimResults, List<MailAuthenticityMechanismResult> dmarcResults) {
            super();
            this.spfResults = null == spfResults ? Collections.emptyList() : spfResults;
            this.dkimResults = null == dkimResults ? Collections.emptyList() : dkimResults;
            this.dmarcResults = null == dmarcResults ? Collections.emptyList() : dmarcResults;
        }

        /**
         * Gets the SPF results
         *
         * @return The SPF results
         */
        List<MailAuthenticityMechanismResult> getSpfResults() {
            return spfResults;
        }

        /**
         * Gets the DKIM results
         *
         * @return The DKIM results
         */
        List<MailAuthenticityMechanismResult> getDkimResults() {
            return dkimResults;
        }

        /**
         * Gets the DMAR results
         *
         * @return The DMAR results
         */
        List<MailAuthenticityMechanismResult> getDmarcResults() {
            return dmarcResults;
        }
    }

    /**
     * <p>Determine the overall result from the extracted results. The overall result
     * will be determined by checking the results of DMARC, DKIM and SPF (in that order).</p>
     *
     * <p>If multiple results of a specific mechanism are present, then the best result
     * will be picked for that particular mechanism (according to their natural
     * {@link Enum} ordering) and the rest results of that mechanism will be discarded.</p>
     *
     * <p>If the DMARC mechanism result is 'PASS' then the overall status is marked as 'PASS' or
     * if is 'FAIL' then the overall status is determined according to the 'policy' attribute
     * of DMARC, i.e. for 'reject' the overall status is marked as 'FAIL, for 'quarantine'
     * is marked as 'SUSPICIOUS'and for 'none' is marked as 'NEUTRAL'. For the
     * 'reject' and 'quarantine' cases and no further action is performed, while for 'none'
     * the results of the rest mechanisms are checked.</p>
     *
     * <p>If the DMARC mechanism is other than 'PASS' or 'FAIL' then DKIM and SPF are checked and
     * the overall status is determined in respect to their results.</p>
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param results A {@link List} with the results of the known mechanisms
     */
    protected void determineOverallResult(MailAuthenticityResult overallResult, List<MailAuthenticityMechanismResult> results) {
        // Separate results
        SeparatedResults separatedResults = separateAndClearResults(results);

        // Pick the best results for all mechanisms
        MailAuthenticityMechanismResult bestOfDMARC = pickBestResult(separatedResults.getDmarcResults());
        MailAuthenticityMechanismResult bestOfDKIM = pickBestResult(separatedResults.getDkimResults());
        MailAuthenticityMechanismResult bestOfSPF = pickBestResult(separatedResults.getSpfResults());
        separatedResults = null; // Might help GC

        // Re-add best ones to results
        if (bestOfDMARC != null) {
            results.add(bestOfDMARC);
        }
        if (bestOfDKIM != null) {
            results.add(bestOfDKIM);
        }
        if (bestOfSPF != null) {
            results.add(bestOfSPF);
        }
        if (false == checkDMARCV2(bestOfDMARC, overallResult)) {
            checkSPFV2(overallResult, bestOfSPF, checkDKIMV2(bestOfDKIM));
        }
        determineReliableAuthenticatedSender(overallResult, bestOfDMARC, bestOfDKIM, bestOfSPF);
    }

    /**
     * <p>Checks the DMARC status of the specified result and sets the overall result accordingly.</p>
     * 
     * <p>If the DMARC status yields a 'pass', then the overall status will be set to {@link MailAuthenticityStatus#PASS}.
     * In case of a {@link DMARCResult#FAIL} or {@link DMARCResult#PERMERROR}, then the policy of the DMARC will be examined
     * and that will dictate the value of the overall result.</p>
     * 
     * <p>In any other case the overall result will be set to {@link MailAuthenticityStatus#NEUTRAL}</p>
     * 
     * @param dmarc The DMARC result
     * @param overallResult The overall result
     * @return <code>true</code> in case the DMARC result exists; <code>false</code> otherwise
     */
    private boolean checkDMARCV2(MailAuthenticityMechanismResult dmarc, MailAuthenticityResult overallResult) {
        if (dmarc == null) {
            return false;
        }
        DMARCResult result = (DMARCResult) dmarc.getResult();
        switch (result) {
            case FAIL:
            case PERMERROR:
                checkDMARCPolicyV2(overallResult, dmarc);
                break;
            case PASS:
                overallResult.setStatus(MailAuthenticityStatus.PASS);
                break;
            case NONE:
            case TEMPERROR:
            default:
                overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
        }
        return true;
    }

    /**
     * <p>Checks the DMARC policy of the specified DMARC result and sets the
     * <code>overallStatus</code> accordingly.</p>
     * 
     * <p>If the policy is empty, none existent or set to 'none', then the overall
     * status is set to {@link MailAuthenticityStatus#NEUTRAL}</p>. otherwise the
     * value of the later will be dictated by the policy as follows:
     * <ul>
     * <li><code>quarantine</code> --> {@link MailAuthenticityStatus#SUSPICIOUS}</li>
     * <li><code>reject</code> --> {@link MailAuthenticityStatus#FAIL}</li>
     * </ul>
     * 
     * @param overallResult The overall result
     * @param dmarcResult The DMARC result
     */
    private void checkDMARCPolicyV2(MailAuthenticityResult overallResult, MailAuthenticityMechanismResult dmarcResult) {
        String policy = dmarcResult.getProperties().get(DMARCProperty.POLICY);
        if (Strings.isEmpty(policy)) {
            overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
            return;
        }
        MailAuthenticityStatus overallStatus = MailAuthenticityStatus.NEUTRAL;
        try {
            DMARCPolicy p = DMARCPolicy.valueOf(policy);
            switch (p) {
                case quarantine:
                    overallStatus = MailAuthenticityStatus.SUSPICIOUS;
                    break;
                case reject:
                    overallStatus = MailAuthenticityStatus.FAIL;
                    break;
                case none:
                default:
                    overallStatus = MailAuthenticityStatus.NEUTRAL;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.debug("An invalid DMARC policy was specified '" + policy + "'");
        }
        overallResult.setStatus(overallStatus);
    }

    /**
     * Checks the DKIM result of the specified mechanism.
     * 
     * @param dkimResult The DKIM result
     * @return <code>true</code> if DKIM passed, <code>null</code> if missing and
     *         <code>false</code> in any other case.
     */
    private Boolean checkDKIMV2(MailAuthenticityMechanismResult dkimResult) {
        if (dkimResult == null) {
            return null;
        }
        DKIMResult result = (DKIMResult) dkimResult.getResult();
        switch (result) {
            case PASS:
                return true;
            case FAIL:
            case PERMFAIL:
                return false;
            case NONE:
            case NEUTRAL:
            case TEMPERROR:
                return null;
            case POLICY:
            default:
                return true;
        }
    }

    /**
     * <p>Checks the SPF result of the specified mechanism.</p>
     * 
     * <p>If the result does not exist, nothing will happen. Otherwise, if the <code>spfResult</code> is
     * {@link SPFResult#PERMERROR} and the <code>dkimStatus</code> is either missing or failed, then the
     * <code>overallStatus</code> is marked as {@link MailAuthenticityStatus#SUSPICIOUS}. In any other case
     * no overall status will be set or overriden.</p>
     * 
     * @param overallResult The overall result
     * @param spfResult The SPF result
     * @param dkimStatus The DKIM status
     */
    private void checkSPFV2(MailAuthenticityResult overallResult, MailAuthenticityMechanismResult spfResult, Boolean dkimStatus) {
        if (spfResult == null) {
            return;
        }
        SPFResult result = (SPFResult) spfResult.getResult();
        switch (result) {
            case SOFTFAIL:
                if (dkimStatus == null) {
                    // Retain NEUTRAL
                    return;
                }
            case FAIL:
            case PERMERROR:
                if (dkimStatus == null || false == dkimStatus.booleanValue()) {
                    overallResult.setStatus(MailAuthenticityStatus.SUSPICIOUS);
                }
                return;
            case NEUTRAL:
            case TEMPERROR:
            case NONE:
            case PASS:
            case POLICY:
            default:
                return;
        }
    }
}
