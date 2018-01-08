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

package com.openexchange.mail.authenticity.impl.core;

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
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.InterruptibleCharSequence;
import com.openexchange.java.InterruptibleCharSequence.InterruptedRuntimeException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityResultKey;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
import com.openexchange.mail.authenticity.impl.core.parsers.DKIMMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.impl.core.parsers.DMARCMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.impl.core.parsers.SPFMailAuthenticityMechanismParser;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dmarc.DMARCResult;
import com.openexchange.mail.authenticity.mechanism.spf.SPFResult;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;

/**
 * <p>{@link MailAuthAuthenticityHandler}</p>
 *
 * <p>This handler considers only the {@link DefaultMailAuthenticityMechanism#DMARC}, {@link DefaultMailAuthenticityMechanism#DKIM} and
 * {@link DefaultMailAuthenticityMechanism#SPF} in that particular order.</p>
 *
 * <p>The default overall status of the {@link MailAuthenticityResult} is the {@link MailAuthenticityStatus#NEUTRAL}. If there are none of the above mentioned
 * mechanisms in the e-mail's <code>Authentication-Results</code> header, then that status applies. Unknown mechanisms, duplicate known mechanisms and <code>ptypes</code>
 * are ignored from the evaluation but their raw data is included in the overall result's attributes under the
 * {@link DefaultMailAuthenticityResultKey#UNCONSIDERED_AUTH_MECH_RESULTS} key.</p>
 *
 * <p>In case there are multiple <code>Authentication-Results</code> in the e-mail's headers, then all of them are evaluated (top to bottom). Their mechanisms are sorted
 * by their predefined ordinal (DMARC > DKIM > SPF) and evaluated in that order.</p>
 *
 * <p><code>Authentication-Results</code> headers with an invalid/unknown/absent <code>authserv-id</code> are simply ignored and <u>NOT</u> included in the result.</p>
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityHandlerImpl implements MailAuthenticityHandler {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailAuthenticityHandlerImpl.class);

    private final int ranking;
    private final ServiceLookup services;
    private final TrustedMailService trustedMailService;
    private final Comparator<String> mailAuthComparator;
    private final Collection<MailField> requiredMailFields;
    private final Cache<UserAndContext, List<AllowedAuthServId>> authServIdsCache;
    private final Map<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegistry;

    /**
     * Initializes a new {@link MailAuthenticityHandlerImpl} with ranking of <code>0</code> (zero).
     *
     * @param services The service look-up
     */
    public MailAuthenticityHandlerImpl(final TrustedMailService trustedMailService, final ServiceLookup services) {
        this(0, trustedMailService, services);
    }

    /**
     * Initializes a new {@link MailAuthenticityHandlerImpl}.
     *
     * @param ranking The ranking of this handler; a higher value means higher priority
     * @param services The service look-up
     */
    public MailAuthenticityHandlerImpl(final int ranking, final TrustedMailService trustedMailService, final ServiceLookup services) {
        super();
        this.services = services;
        this.trustedMailService = trustedMailService;
        this.ranking = ranking;
        mailAuthComparator = new MailAuthenticityMechanismComparator();
        authServIdsCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
        requiredMailFields = ImmutableList.of(MailField.FROM);
        mechanismParsersRegistry = initialiseMechanismRegistry();
    }

    /**
     * Initialise the mechanism registry
     *
     * @return An {@link ImmutableMap} with the mechanism implementations
     */
    private ImmutableMap<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> initialiseMechanismRegistry() {
        final ImmutableMap.Builder<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegistry = ImmutableMap.builder();
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DMARC, new DMARCMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.DKIM, new DKIMMailAuthenticityMechanismParser());
        mechanismParsersRegistry.put(DefaultMailAuthenticityMechanism.SPF, new SPFMailAuthenticityMechanismParser());

        return mechanismParsersRegistry.build();
    }

    /**
     * Clears the authserv-ids cache.
     */
    public void invalidateAuthServIdsCache() {
        authServIdsCache.invalidateAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#handle(com.openexchange.session.Session, com.openexchange.mail.dataobjects.MailMessage)
     */
    @Override
    public void handle(final Session session, final MailMessage mailMessage) {
        if (mailMessage.containsAuthenticityResult()) {
            // Appears that authenticity results has already been set for specified MailMessage instance
            return;
        }

        final HeaderCollection headerCollection = mailMessage.getHeaders();
        final String[] authHeaders = headerCollection.getHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS);
        if (authHeaders == null || authHeaders.length == 0) {
            // Pass on to custom handlers
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            logMetrics(Collections.emptyList(), mailMessage.getAuthenticityResult());
            return;
        }

        final InternetAddress[] from = mailMessage.getFrom();
        if (from == null || from.length == 0) {
            // Pass on to custom handlers
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            logMetrics(Arrays.asList(authHeaders), mailMessage.getAuthenticityResult());
            return;
        }

        List<String> headers = Arrays.asList(authHeaders);
        MailAuthenticityResult authenticityResult = MailAuthenticityResult.NOT_ANALYZED_RESULT;
        try {
            authenticityResult = parseHeaders(headers, from[0], session);
        } catch (Exception e) {
            LOGGER.error("An error occurred during parsing the 'Authentication-Results' header: {}", e.getMessage(), e);
        }
        mailMessage.setAuthenticityResult(authenticityResult);
        logMetrics(headers, mailMessage.getAuthenticityResult());

        trustedMailService.handle(session, mailMessage);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#getRequiredFields()
     */
    @Override
    public Collection<MailField> getRequiredFields() {
        return requiredMailFields;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#getRequiredHeaders()
     */
    @Override
    public Collection<String> getRequiredHeaders() {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.mail.authenticity.MailAuthenticityHandler#isEnabled(com.openexchange.session.Session)
     */
    @Override
    public boolean isEnabled(final Session session) {
        // MailAuthenticityProperty.enabled is already checked in MailAuthenticityHandlerRegistryImpl
        // No further individual conditions to check
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
     * Performs the parsing magic of the specified <code>Authentication-Results</code> headers and <code>From</code> header and
     * returns the overall {@link MailAuthenticityResult}
     *
     * @param authenticationHeaders The <code>Authentication-Results</code> headers
     * @param fromHeader The <code>From</code> header
     * @param session The groupware {@link Session}
     * @return The overall {@link MailAuthenticityResult}
     * @throws OXException if the allowed authserv-ids cannot be retrieved from the configuration or any other parsing error occurs
     */
    private MailAuthenticityResult parseHeaders(final List<String> authenticationHeaders, final InternetAddress from, final Session session) throws OXException {
        final List<AllowedAuthServId> allowedAuthServIds = getAllowedAuthServIds(session);

        final MailAuthenticityResult overallResult = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        final List<MailAuthenticityMechanismResult> results = new ArrayList<>();
        final List<Map<String, String>> unconsideredResults = new ArrayList<>();

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
                    overallResult.addAttribute(MailAuthenticityResultKey.FROM_DOMAIN, extractDomain(from));
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

        determineOverallResult(overallResult, results, unconsideredResults);

        overallResult.addAttribute(MailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, results);
        overallResult.addAttribute(MailAuthenticityResultKey.UNCONSIDERED_AUTH_MECH_RESULTS, unconsideredResults);

        return overallResult;
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
    private void parseMechanisms(List<String> elements, List<MailAuthenticityMechanismResult> results, List<Map<String, String>> unconsideredResults, MailAuthenticityResult overallResult, Thread currentThread) {
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
     * Determine the overall result from the extracted results
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param results A {@link List} with the results of the known mechanisms
     * @param unconsideredResults A {@link List} with the unknown/unconsidered results
     */
    private void determineOverallResult(final MailAuthenticityResult overallResult, final List<MailAuthenticityMechanismResult> results, final List<Map<String, String>> unconsideredResults) {
        // Separate results
        SeparatedResults separatedResults = separateAndClearResults(results);

        // Pick the best results for all mechanisms
        MailAuthenticityMechanismResult bestOfDMARC = pickBestResult(separatedResults.getDmarcResults(), unconsideredResults);
        MailAuthenticityMechanismResult bestOfDKIM = pickBestResult(separatedResults.getDkimResults(), unconsideredResults);
        MailAuthenticityMechanismResult bestOfSPF = pickBestResult(separatedResults.getSpfResults(), unconsideredResults);
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
                return;
            } else if (DMARCResult.FAIL.equals(bestOfDMARC.getResult())) {
                overallResult.setStatus(MailAuthenticityStatus.FAIL);
                return;
            }
        }

        // The DMARC status was NEUTRAL or none existing, check for DKIM
        boolean dkimFailed = dkmiFailed(overallResult, bestOfDKIM);
        // Continue with SPF
        checkSPF(overallResult, bestOfSPF, dkimFailed);
    }

    /**
     * Separates the results into different containers according to their type
     *
     * @param results All the {@link MailAuthenticityMechanismResult}s
     * @return The results separated by SPF, DKIM and DMARC
     */
    private SeparatedResults separateAndClearResults(final List<MailAuthenticityMechanismResult> results) {
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
     * Check the DKIM best of result and determine the overall status
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param bestOfDKIM The best of DKIM {@link MailAuthenticityMechanismResult}
     * @return <code>true</code> if DKIM failed, <code>false</code> otherwise
     */
    private boolean dkmiFailed(final MailAuthenticityResult overallResult, MailAuthenticityMechanismResult bestOfDKIM) {
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
                overallResult.setStatus(bestOfDKIM.isDomainMatch() ? MailAuthenticityStatus.PASS : MailAuthenticityStatus.NEUTRAL);
                break;
            default:
                overallResult.setStatus(bestOfDKIM.getResult().convert());
        }
        return dkimFailed;
    }

    /**
     * Check the SPF best of result and determine the overall status
     *
     * @param overallResult The overall {@link MailAuthenticityResult}
     * @param bestOfSPF The best of SPF {@link MailAuthenticityMechanismResult}
     * @param dkimFailed the status of DKIM
     */
    private void checkSPF(final MailAuthenticityResult overallResult, MailAuthenticityMechanismResult bestOfSPF, boolean dkimFailed) {
        if (bestOfSPF == null) {
            return;
        }
        final SPFResult spfResult = (SPFResult) bestOfSPF.getResult();
        switch (spfResult) {
            case SOFTFAIL:
            case TEMPERROR:
            case NONE:
            case NEUTRAL:
                // Handle as neutral or fail, depending on the domain match
                overallResult.setStatus(bestOfSPF.isDomainMatch() ? MailAuthenticityStatus.NEUTRAL : MailAuthenticityStatus.FAIL);
                break;
            case PASS:
                // Pass
                if (dkimFailed) {
                    overallResult.setStatus(bestOfSPF.isDomainMatch() ? MailAuthenticityStatus.NEUTRAL : MailAuthenticityStatus.FAIL);
                } else {
                    overallResult.setStatus(bestOfSPF.isDomainMatch() ? MailAuthenticityStatus.PASS : MailAuthenticityStatus.NEUTRAL);
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
     * Picks the best {@link MailAuthenticityMechanismResult} from the specified {@link List} of results
     *
     * @param results The {@link List} with the {@link MailAuthenticityMechanismResult}s
     * @param unconsideredResults The {@link List} with the unconsidered results
     * @return The best {@link MailAuthenticityMechanismResult} according to their natural ordering,
     *         or <code>null</code> if the {@link List} is empty, or the first (and only) element
     *         if the {@link List} is a singleton
     */
    private MailAuthenticityMechanismResult pickBestResult(List<MailAuthenticityMechanismResult> results, List<Map<String, String>> unconsideredResults) {
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

        // Add the rest to unconsidered list and remove from the original
        for (MailAuthenticityMechanismResult result : results) {
            if (result != bestResult) {
                unconsideredResults.add(convert(result));
            }
        }
        return bestResult;
    }

    /**
     * Parses the unknown mechanism's attributes and returns those as a {@link Map}
     *
     * @param element The element to parse
     * @return A {@link Map} with the parsed attributes of the unknown mechanism
     */
    private Map<String, String> parseUnknownMechs(final String element) {
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
     * Converts the specified {@link MailAuthenticityMechanismResult} to a {@link Map}
     *
     * @param result The {@link MailAuthenticityMechanismResult} to convert
     * @return A {@link Map} with the converted {@link MailAuthenticityMechanismResult}
     */
    private Map<String, String> convert(MailAuthenticityMechanismResult result) {
        final Map<String, String> unconsidered = new HashMap<>(6);
        unconsidered.put("mechanism", result.getMechanism().getTechnicalName());
        unconsidered.put("result", result.getResult().getTechnicalName());
        unconsidered.put("domain", result.getDomain());
        String reason = result.getReason();
        if (!Strings.isEmpty(reason)) {
            unconsidered.put("reason", reason);
        }
        return unconsidered;
    }

    /**
     * Extracts the domain from the specified internet address
     * 
     * @param adr The address as string
     * @return The domain of the sender
     * @throws IllegalArgumentException if the address is either empty or <code>null</code
     */
    private String extractDomain(final InternetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("The address can be neither empty nor null");
        }
        final String adr = address.getAddress();
        final int index = adr.indexOf('@');
        return adr.substring(index + 1);
    }

    private static final Pattern SPLIT = Pattern.compile(" ");

    /**
     * Determines whether the specified authServId is valid
     *
     * @param authServId The authserv-id to check
     * @return <code>true</code> if the string is valid; <code>false</code> otherwise
     */
    private boolean isAuthServIdValid(String authServId, final List<AllowedAuthServId> allowedAuthServIds) {
        if (Strings.isEmpty(authServId)) {
            LOGGER.warn("The authserv-id is missing from the 'Authentication-Results'");
            return false;
        }
        final String[] split = SPLIT.split(authServId, 0);

        // Cleanse the optional version
        authServId = split.length == 0 ? authServId : split[0];

        // Regex and wildcard checks...
        for (final AllowedAuthServId allowedAuthServId : allowedAuthServIds) {
            if (allowedAuthServId.allows(authServId)) {
                return true;
            }
        }
        LOGGER.warn("The authserv-id '{}' is not in the allowed authserv ids list (see server property '{}'). Server misconfiguration?", authServId, MailAuthenticityProperty.AUTHSERV_ID);
        return false;
    }

    /**
     * Extracts the outcome of the specified value.
     *
     * @param value the value to extract the outcome from
     * @return The extracted outcome
     */
    private String extractOutcome(final String value) {
        final int index = value.indexOf(' ');
        return (index < 0) ? value : value.substring(0, index);
    }

    /**
     * Extracts an optional comment that may reside within parentheses
     *
     * @param value The value to extract the comment form
     * @return The optional extracted comment; <code>null</code> if no comment was found
     */
    private String extractComment(String value) {
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
     * Gets the allowed authserv-ids for session-associated user.
     *
     * @param session The session
     * @return The allowed authserv-ids
     * @throws OXException If authserv-ids are missing
     */
    private List<AllowedAuthServId> getAllowedAuthServIds(final Session session) throws OXException {
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        final UserAndContext key = UserAndContext.newInstance(userId, contextId);
        final List<AllowedAuthServId> authServIds = authServIdsCache.getIfPresent(key);
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
    private List<AllowedAuthServId> getAllowedAuthServIds(final int userId, final int contextId, final UserAndContext key) throws OXException {
        // This is not thread-safe, but does not need to
        final LeanConfigurationService leanConfigService = services.getService(LeanConfigurationService.class);
        final String sAuthServIds = leanConfigService.getProperty(userId, contextId, MailAuthenticityProperty.AUTHSERV_ID);
        if (Strings.isEmpty(sAuthServIds)) {
            throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }

        final List<String> tokens = Arrays.asList(Strings.splitByComma(sAuthServIds));
        if (tokens == null || tokens.isEmpty() || tokens.contains("")) {
            throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }

        final List<AllowedAuthServId> authServIds = AllowedAuthServId.allowedAuthServIdsFor(tokens);
        if (authServIds == null || authServIds.isEmpty()) {
            throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }

        authServIdsCache.put(key, authServIds);
        return authServIds;
    }

    /**
     * Logs the specified raw headers and overall result with the {@link MailAuthenticityMetricLogger}
     *
     * @param authHeaders the raw headers
     * @param overallResult the overall result
     */
    private void logMetrics(final List<String> authHeaders, MailAuthenticityResult overallResult) {
        MailAuthenticityMetricLogger metricLogger = services.getService(MailAuthenticityMetricLogger.class);
        metricLogger.log(authHeaders, overallResult);
    }

    ///////////////////////////////// HELPER CLASSES /////////////////////////////////

    /**
     * {@link MailAuthenticityMechanismComparator} - Compares the {@link DefaultMailAuthenticityMechanism}s
     * according to their ordinal value
     *
     * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
     */
    private static class MailAuthenticityMechanismComparator implements Comparator<String> {

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
            } else if (mam2 == null) {
                return -1;
            }
            return 0;
        }
    }

    /** Simple helper class to wrap containers for SPF, DKIM and DMARC results */
    private static class SeparatedResults {

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
