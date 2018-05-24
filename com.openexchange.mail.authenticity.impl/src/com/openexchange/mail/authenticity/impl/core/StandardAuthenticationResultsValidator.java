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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
        final ImmutableMap.Builder<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegistry = ImmutableMap.builder();
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DMARC, new DMARCMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DKIM, new DKIMMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.SPF, new SPFMailAuthenticityMechanismParser());
        return mechanismParsersRegistry.build();
    }

    @Override
    public MailAuthenticityResult parseHeaders(List<String> authenticationHeaders, InternetAddress from, List<AllowedAuthServId> allowedAuthServIds) throws OXException {
        MailAuthenticityResult overallResult = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        List<MailAuthenticityMechanismResult> results = new ArrayList<>();
        List<Map<String, String>> unconsideredResults = new ArrayList<>();

        try {
            final Thread currentThread = Thread.currentThread();
            for (Iterator<String> authHeaderIterator = authenticationHeaders.iterator(); !currentThread.isInterrupted() && authHeaderIterator.hasNext();) {
                final String authenticationHeader = authHeaderIterator.next();
                List<String> elements = StringUtil.splitElements(InterruptibleCharSequence.valueOf(authenticationHeader));

                // The first property of the header MUST always be the domain (i.e. the authserv-id)
                // See https://tools.ietf.org/html/rfc7601 for the formal definition
                final String authServId = elements.get(0);
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
                } catch (final IllegalArgumentException e) {
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

        // Set overall status to 'none'. This implies that:
        //  a) no auth-servId match was found, hence no mechanisms was parsed, or
        //  b) we only came across unknown mechanisms, which we don't take 
        //     into consideration when we determine the overall result. 
        if (results.isEmpty()) {
            overallResult.setStatus(MailAuthenticityStatus.NONE);
        }

        determineDomainMismatch(overallResult);

        return overallResult;
    }

    /**
     * Determines whether the specified authServId is valid
     *
     * @param authServId The authserv-id to check
     * @return <code>true</code> if the string is valid; <code>false</code> otherwise
     */
    protected boolean isAuthServIdValid(String authServId, final List<AllowedAuthServId> allowedAuthServIds) {
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
    protected String extractDomain(final InternetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("The address can be neither empty nor null");
        }
        final String adr = address.getAddress();
        final int index = adr.indexOf('@');
        return adr.substring(index + 1);
    }

    /**
     * Extracts the outcome of the specified value.
     *
     * @param value the value to extract the outcome from
     * @return The extracted outcome
     */
    protected String extractOutcome(final String value) {
        final int index = value.indexOf(' ');
        return (index < 0) ? value : value.substring(0, index);
    }

    /**
     * Extracts an optional comment that may reside within parentheses
     *
     * @param value The value to extract the comment form
     * @return The optional extracted comment; <code>null</code> if no comment was found
     */
    protected String extractComment(String value) {
        final int beginIndex = value.indexOf('(');
        if (beginIndex < 0) {
            return null;
        }
        final int endIndex = value.indexOf(')');
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
            final Map<String, String> attributes = StringUtil.parseMap(InterruptibleCharSequence.valueOf(element));

            final DefaultMailAuthenticityMechanism mechanism = DefaultMailAuthenticityMechanism.extractMechanism(attributes);
            if (mechanism == null) {
                // Unknown or not parsable mechanism
                unconsideredResults.add(parseUnknownMechs(element));
                continue;
            }
            final BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult> mechanismParser = mechanismParsersRegistry.get(mechanism);
            if (mechanismParser == null) {
                // Not a valid mechanism, skip but add to the overall result
                unconsideredResults.add(parseUnknownMechs(element));
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
        final List<MailAuthenticityAttribute> attributes = StringUtil.parseList(InterruptibleCharSequence.valueOf(element));
        final Map<String, String> unknownResults = new HashMap<>();
        // First element is always the mechanism
        final MailAuthenticityAttribute mechanism = attributes.get(0);
        unknownResults.put("mechanism", mechanism.getKey());
        unknownResults.put("result", extractOutcome(mechanism.getValue()));

        for (int index = 1; index < attributes.size(); index++) {
            final MailAuthenticityAttribute attribute = attributes.get(index);
            unknownResults.put(attribute.getKey(), attribute.getValue());
        }

        if (!unknownResults.containsKey("reason")) {
            final String reason = extractComment(mechanism.getValue());
            if (!Strings.isEmpty(reason)) {
                unknownResults.put("reason", reason);
            }
        }

        return unknownResults;
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
     * if is 'FAIL' then the overall status is marked as 'FAIL, and no further action is performed.</p>
     *
     * <p>If the DMARC mechanism is other than 'PASS' or 'FAIL' then DKIM and SPF are checked and
     * the overall status is determined in respect to their results.</p>
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param results A {@link List} with the results of the known mechanisms
     */
    protected void determineOverallResult(final MailAuthenticityResult overallResult, final List<MailAuthenticityMechanismResult> results) {
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

        if (bestOfDMARC != null) {
            // If DMARC passes we set the overall status to PASS
            if (DMARCResult.PASS.equals(bestOfDMARC.getResult()) && bestOfDMARC.isDomainMatch()) {
                overallResult.setStatus(MailAuthenticityStatus.PASS);
                overallResult.addAttribute(MailAuthenticityResultKey.FROM_DOMAIN, bestOfDMARC.getDomain());
                return;
            } else if (DMARCResult.FAIL.equals(bestOfDMARC.getResult())) {
                overallResult.setStatus(MailAuthenticityStatus.FAIL);
                return;
            } else if (DMARCResult.NONE.equals(bestOfDMARC.getResult())) {
                overallResult.setStatus(MailAuthenticityStatus.NONE);
            }
        }

        // The DMARC status was NEUTRAL or none existing, check for DKIM
        boolean dkimFailed = dkimFailed(overallResult, bestOfDKIM);

        // Continue with SPF
        checkSPF(overallResult, bestOfSPF, dkimFailed);
    }

    /**
     * <p>Check the DKIM best of result and set the overall status.</p>
     *
     * <p>If the DKIM status is either 'PERMFAIL' or 'FAIL' then the DKIM mechanism is
     * considered to have failed, thus the overall status is set accordingly to 'FAIL'.</p>
     *
     * <p>If the DKIM status is set to 'PASS', then the overall status will be set to
     * either 'PASS' or 'NEUTRAL' depending on whether there is a domain match ('PASS'
     * in case of a domain match).</p>
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param bestOfDKIM The best of DKIM {@link MailAuthenticityMechanismResult}
     * @return <code>true</code> if DKIM failed, <code>false</code> otherwise
     */
    protected boolean dkimFailed(final MailAuthenticityResult overallResult, MailAuthenticityMechanismResult bestOfDKIM) {
        if (bestOfDKIM == null) {
            return false;
        }
        boolean dkimFailed = false;
        final DKIMResult dkimResult = (DKIMResult) bestOfDKIM.getResult();
        switch (dkimResult) {
            case PERMFAIL:
            case FAIL:
                dkimFailed = true;
                break;
            case PASS:
                if (bestOfDKIM.isDomainMatch()) {
                    overallResult.setStatus(MailAuthenticityStatus.PASS);
                    overallResult.addAttribute(MailAuthenticityResultKey.FROM_DOMAIN, bestOfDKIM.getDomain());
                } else {
                    overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
                }

                break;
            case NONE:
                if (MailAuthenticityStatus.NONE.equals(overallResult.getStatus())) {
                    // Keep the 'none' status as DMARC set it to 'none' as well
                    break;
                }
            default:
                overallResult.setStatus(bestOfDKIM.getResult().convert());
        }

        return dkimFailed;
    }

    /**
     * <p>Check the SPF best of result and set the overall status.</p>
     *
     * <p>If the SPF status is either 'PERMERROR' or 'FAIL' then the SPF mechanism is
     * considered to have failed, thus the overall status is set accordingly to 'FAIL'.</p>
     *
     * <p>If the SPF status is either 'SOFTFAIL', or 'TEMPERROR', or 'NONE', or 'NEUTRAL'
     * then the overall status is set to either 'NEUTRAL' or 'FAIL' depending on whether
     * there is a domain match ('NEUTRAL' in case of a domain match).</p>
     *
     * <p>On the other hand, if the SPF status is set to 'PASS' then the overall status
     * is set to either 'NEUTRAL' or 'FAIL' depending on whether DKIM failed and there is
     * a domain match ('NEUTRAL' in case of a domain match), or to either 'PASS' or 'NEUTRAL'
     * if DKIM passed and there is a domain match ('PASS' in case of a domain match).</p>
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param bestOfSPF The best of SPF {@link MailAuthenticityMechanismResult}
     * @param dkimFailed the status of DKIM
     */
    protected void checkSPF(final MailAuthenticityResult overallResult, MailAuthenticityMechanismResult bestOfSPF, boolean dkimFailed) {
        if (bestOfSPF == null) {
            return;
        }
        final SPFResult spfResult = (SPFResult) bestOfSPF.getResult();
        switch (spfResult) {
            case SOFTFAIL:
            case TEMPERROR:
            case NONE:
                if (MailAuthenticityStatus.NONE.equals(overallResult.getStatus())) {
                    // Keep the 'none' status as DKIM and/or DMARC set it to 'none' as well
                    break;
                }
            case NEUTRAL:
                // Handle as neutral or fail, depending on the domain match
                if (dkimFailed) {
                    overallResult.setStatus(bestOfSPF.isDomainMatch() ? MailAuthenticityStatus.NEUTRAL : MailAuthenticityStatus.FAIL);
                }
                break;
            case PASS:
                // Pass
                if (dkimFailed) {
                    overallResult.setStatus(bestOfSPF.isDomainMatch() ? MailAuthenticityStatus.NEUTRAL : MailAuthenticityStatus.FAIL);
                } else if (MailAuthenticityStatus.PASS != overallResult.getStatus()) {
                    if (bestOfSPF.isDomainMatch()) {
                        overallResult.setStatus(MailAuthenticityStatus.PASS);
                        overallResult.addAttribute(MailAuthenticityResultKey.FROM_DOMAIN, bestOfSPF.getDomain());
                    } else {
                        overallResult.setStatus(MailAuthenticityStatus.NEUTRAL);
                    }
                }
                break;
            case PERMERROR:
            case FAIL:
                // Handle as fail
                overallResult.setStatus(MailAuthenticityStatus.FAIL);
                break;
            case POLICY:
            default:
                // Override
                overallResult.setStatus(bestOfSPF.getResult().convert());
        }
    }

    /**
     * Separates the results into different containers according to their type
     *
     * @param results All the {@link MailAuthenticityMechanismResult}s
     * @return The results separated by SPF, DKIM and DMARC
     */
    protected SeparatedResults separateAndClearResults(final List<MailAuthenticityMechanismResult> results) {
        // Declare containers for SPF, DKIM and DMARC
        List<MailAuthenticityMechanismResult> spfResults = null;
        List<MailAuthenticityMechanismResult> dkimResults = null;
        List<MailAuthenticityMechanismResult> dmarcResults = null;

        for (final MailAuthenticityMechanismResult result : results) {
            final DefaultMailAuthenticityMechanism mechanism = (DefaultMailAuthenticityMechanism) result.getMechanism();
            switch (mechanism) {
                case DMARC:
                    if (null == dmarcResults) {
                        dmarcResults = new ArrayList<>();
                    }
                    dmarcResults.add(result);
                    break;
                case DKIM:
                    if (null == dkimResults) {
                        dkimResults = new ArrayList<>();
                    }
                    dkimResults.add(result);
                    break;
                case SPF:
                    if (null == spfResults) {
                        spfResults = new ArrayList<>();
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
            } else {
                if (result.getResult().getCode() < bestResult.getResult().getCode()) {
                    bestResult = result;
                } else if (result.getResult().getCode() == bestResult.getResult().getCode() && result.isDomainMatch()) {
                    bestResult = result;
                }
            }
        }

        return bestResult;
    }

    /**
     * Determines whether there is a domain mismatch between the dominant mechanism's domain and the domain from the 'From' header.
     * Sets the {@link MailAuthenticityResultKey#DOMAN_MISMATCH} to the attributes of the overall result.
     * 
     * @param overallResult The overall result
     */
    private void determineDomainMismatch(MailAuthenticityResult overallResult) {
        boolean domainMismatch = true;
        String mechDomain = (String) overallResult.getAttribute(MailAuthenticityResultKey.FROM_DOMAIN);
        if (Strings.isNotEmpty(mechDomain)) {
            domainMismatch = !mechDomain.equals((String) overallResult.getAttribute(MailAuthenticityResultKey.FROM_HEADER_DOMAIN));
        }
        overallResult.addAttribute(MailAuthenticityResultKey.DOMAN_MISMATCH, domainMismatch);
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
        public int compare(final String o1, final String o2) {
            final String[] s1 = o1.split("=");
            final String[] s2 = o2.split("=");
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

}
