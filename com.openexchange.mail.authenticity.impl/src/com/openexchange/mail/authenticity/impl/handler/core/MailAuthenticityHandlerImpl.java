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

package com.openexchange.mail.authenticity.impl.handler.core;

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
import com.openexchange.mail.authenticity.MailAuthenticityAttribute;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.handler.domain.TrustedDomainService;
import com.openexchange.mail.authenticity.mechanism.AbstractAuthMechResult;
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

    private final Map<DefaultMailAuthenticityMechanism, BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult>> mechanismParsersRegitry;

    private static final MailAuthenticityMechanismComparator MAIL_AUTH_COMPARATOR = new MailAuthenticityMechanismComparator();

    private static final String EMPTY_STRING = "";

    /** The required mail fields of this handler */
    private static final Collection<MailField> REQUIRED_MAIL_FIELDS;
    static {
        Collection<MailField> m = new ArrayList<>();
        m.add(MailField.AUTHENTICATION_OVERALL_RESULT);
        REQUIRED_MAIL_FIELDS = Collections.<MailField> unmodifiableCollection(m);
    }

    /** The required headers of this handler */
    private static final Collection<String> REQUIRED_HEADERS;
    static {
        Collection<String> m = new ArrayList<>();
        m.add(MessageHeaders.HDR_AUTHENTICATION_RESULTS);
        m.add(MessageHeaders.HDR_FROM);
        REQUIRED_HEADERS = Collections.<String> unmodifiableCollection(m);
    }

    /** The ranking of this handler */
    private final int ranking;
    private final Cache<UserAndContext, List<AllowedAuthServId>> authServIdsCache;
    private final TrustedDomainService trustedDomainHandler;
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
        this.trustedDomainHandler = services.getService(TrustedDomainService.class);
        this.ranking = ranking;
        this.authServIdsCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
        mechanismParsersRegitry = new HashMap<>(4);
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.DMARC, (attributes, overallResult) -> {
            String value = attributes.remove(DefaultMailAuthenticityMechanism.DMARC.getTechnicalName());
            DMARCResult dmarcResult = DMARCResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = extractDomain(attributes, DMARCResultHeader.HEADER_FROM);
            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // In case of a DMARC result != "none", set overall result to the DMARC result and continue with the next mechanism
            MailAuthenticityStatus mailAuthStatus = dmarcResult.convert();
            if (!domainMismatch) {
                overallResult.setStatus(mailAuthStatus);
            }

            DMARCAuthMechResult result = new DMARCAuthMechResult(domain, dmarcResult);
            result.setReason(extractComment(value));
            addProperties(attributes, result);
            return result;
        });
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.DKIM, (attributes, overallResult) -> {
            String value = attributes.remove(DefaultMailAuthenticityMechanism.DKIM.getTechnicalName());
            DKIMResult dkimResult = DKIMResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = extractDomain(attributes, DKIMResultHeader.HEADER_I, DKIMResultHeader.HEADER_D);
            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // In case of a DKIM result != "none", set overall result to the DKIM result and continue with the next mechanism
            MailAuthenticityStatus mailAuthStatus = dkimResult.convert();
            if (overallResult.getStatus().equals(MailAuthenticityStatus.NEUTRAL) && !domainMismatch) {
                overallResult.setStatus(mailAuthStatus);
            }

            DKIMAuthMechResult result = new DKIMAuthMechResult(domain, dkimResult);
            String reason = extractComment(value);
            if (Strings.isEmpty(reason)) {
                reason = Strings.unquote(attributes.remove(DKIMResultHeader.REASON));
            }
            result.setReason(reason);
            addProperties(attributes, result);
            return result;
        });
        mechanismParsersRegitry.put(DefaultMailAuthenticityMechanism.SPF, (attributes, overallResult) -> {
            String value = attributes.remove(DefaultMailAuthenticityMechanism.SPF.getTechnicalName());
            SPFResult spfResult = SPFResult.valueOf(extractOutcome(value.toUpperCase()));

            String domain = extractDomain(attributes, SPFResultHeader.SMTP_MAILFROM, SPFResultHeader.SMTP_HELO);
            boolean domainMismatch = checkDomainMismatch(overallResult, domain);

            // Set the overall result only if it's 'none'
            MailAuthenticityStatus mailAuthStatus = spfResult.convert();
            if (!domainMismatch && (overallResult.getStatus().equals(MailAuthenticityStatus.NEUTRAL) || (!overallResult.getStatus().equals(MailAuthenticityStatus.PASS) && mailAuthStatus.equals(MailAuthenticityStatus.PASS)))) {
                overallResult.setStatus(mailAuthStatus);
            }

            SPFAuthMechResult result = new SPFAuthMechResult(domain, spfResult);
            result.setReason(extractComment(value));
            addProperties(attributes, result);
            return result;
        });
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
    public void handle(Session session, MailMessage mailMessage) throws OXException {
        HeaderCollection headerCollection = mailMessage.getHeaders();
        String[] authHeaders = headerCollection.getHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS);
        if (authHeaders == null || authHeaders.length == 0) {
            // Pass on to custom handlers
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            return;
        }

        String[] fromHeaders = headerCollection.getHeader(MessageHeaders.HDR_FROM);
        if (fromHeaders == null || fromHeaders.length == 0) {
            // Pass on to custom handlers
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            return;
        }

        mailMessage.setAuthenticityResult(parseHeaders(Arrays.asList(authHeaders), fromHeaders[0], session));

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
     * Performs the parsing magic of the specified <code>Authentication-Results</code> headers and <code>From</code> header and
     * returns the overall {@link MailAuthenticityResult}
     * 
     * @param authenticationHeaders The <code>Authentication-Results</code> headers
     * @param fromHeader The <code>From</code> header
     * @param session The groupware {@link Session}
     * @return The overall {@link MailAuthenticityResult}
     * @throws OXException if the allowed authserv-ids cannot be retrieved from the configuration
     */
    private MailAuthenticityResult parseHeaders(List<String> authenticationHeaders, String fromHeader, Session session) throws OXException {
        List<AllowedAuthServId> allowedAuthServIds = getAllowedAuthServIds(session);

        MailAuthenticityResult overallResult = new MailAuthenticityResult(MailAuthenticityStatus.NEUTRAL);
        List<MailAuthenticityMechanismResult> results = new ArrayList<>();
        List<Map<String, String>> unknownResults = new ArrayList<>();

        Iterator<String> authHeaderIterator = authenticationHeaders.iterator();
        while (authHeaderIterator.hasNext()) {
            String authenticationHeader = authHeaderIterator.next();
            List<String> elements = StringUtil.splitElements(authenticationHeader);

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
                String address = extractAddress(fromHeader);
                String domain = extractDomain(address);
                overallResult.addAttribute(DefaultMailAuthenticityResultKey.FROM_DOMAIN, domain);
                overallResult.addAttribute(DefaultMailAuthenticityResultKey.TRUSTED_SENDER, address);
            } catch (Exception e) {
                // Malformed from header, be strict and return with failed result
                LOGGER.debug("An error occurred while trying to extract a valid domain from the 'From' header", e);
                overallResult.setStatus(MailAuthenticityStatus.FAIL);
                return overallResult;
            }

            parseMechanisms(elements, results, unknownResults, overallResult);
        }

        overallResult.addAttribute(DefaultMailAuthenticityResultKey.MAIL_AUTH_MECH_RESULTS, results);
        overallResult.addAttribute(DefaultMailAuthenticityResultKey.UNKNOWN_AUTH_MECH_RESULTS, unknownResults);

        return overallResult;
    }

    /**
     * Parses the mechanisms (known and unknown) and adds the results to their respective {@link List}s
     * 
     * @param elements A {@link List} with the elements of a single <code>Authentication-Results</code> header.
     * @param results A {@link List} with the results of the known mechanisms
     * @param unknownResults A {@link List} with the unknown results
     * @param overallResult The overall {@link MailAuthenticityResult}
     */
    private void parseMechanisms(List<String> elements, List<MailAuthenticityMechanismResult> results, List<Map<String, String>> unknownResults, MailAuthenticityResult overallResult) {
        Collections.sort(elements, MAIL_AUTH_COMPARATOR);
        for (String element : elements) {
            Map<String, String> attributes = StringUtil.parseMap(element);

            DefaultMailAuthenticityMechanism mechanism = DefaultMailAuthenticityMechanism.extractMechanism(attributes);
            if (mechanism == null) {
                // Unknown or not parsable mechanism
                unknownResults.add(parseUnknownMechs(element));
                continue;
            }
            BiFunction<Map<String, String>, MailAuthenticityResult, MailAuthenticityMechanismResult> mechanismParser = mechanismParsersRegitry.get(mechanism);
            if (mechanismParser == null) {
                // Not a valid mechanism, skip but add to the overall result
                unknownResults.add(parseUnknownMechs(element));
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
    private Map<String, String> parseUnknownMechs(String element) {
        List<MailAuthenticityAttribute> attributes = StringUtil.parseList(element);
        Map<String, String> unknownResults = new HashMap<>();
        // First element is always the mechanism
        MailAuthenticityAttribute mechanism = attributes.get(0);
        unknownResults.put("mechanism", mechanism.getKey());
        unknownResults.put("result", extractOutcome(mechanism.getValue()));

        for (int index = 1; index < attributes.size(); index++) {
            unknownResults.put(attributes.get(index).getKey(), attributes.get(index).getValue());
        }

        if (!unknownResults.containsKey("reason")) {
            String reason = extractComment(mechanism.getValue());
            if (!Strings.isEmpty(reason)) {
                unknownResults.put("reason", reason);
            }
        }

        return unknownResults;

    }

    /**
     * Extracts the e-mail address of the sender from the specified <code>From</code> header and returns it
     * 
     * @param fromHeader The from header
     * @return The e-mail address as {@link String}
     * @throws IllegalArgumentException if the specified header does not contain any valid parsable Internet address
     */
    private String extractAddress(String fromHeader) {
        try {
            InternetAddress ia = new InternetAddress(fromHeader, true);
            return ia.getAddress();
        } catch (AddressException e) {
            throw new IllegalArgumentException("The specified header does not contain any valid parsable internet addresses", e);
        }
    }

    /**
     * Extracts the domain of the sender from the specified address and returns it
     *
     * @param address The address as string
     * @return The domain of the sender
     * @throws IllegalAccessException if the address is either empty or <code>null</code
     */
    private String extractDomain(String address) {
        if (Strings.isEmpty(address)) {
            throw new IllegalArgumentException("The address can be neither empty nor null");
        }
        int index = address.indexOf('@');
        return address.substring(index + 1);
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
            LOGGER.warn("The authserv-id is missing from the 'Authentication-Results'");
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
        LOGGER.warn("The authserv-id '{}' is not in the allowed authserv ids list (see server property '{}'). Server misconfiguration?", authServId, MailAuthenticityProperty.authServId);
        return false;
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
        value = Strings.unquote(value);
        return value.substring(beginIndex + 1, endIndex);
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
            if (!Strings.isEmpty(value)) {
                return cleanseDomain(value);
            }
        }
        return null;
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
        if (tokens == null || tokens.isEmpty() || tokens.contains(EMPTY_STRING)) {
            throw MailAuthenticityExceptionCodes.INVALID_AUTHSERV_IDS.create();
        }

        List<AllowedAuthServId> authServIds = AllowedAuthServId.allowedAuthServIdsFor(tokens);
        if (authServIds == null || authServIds.isEmpty()) {
            throw MailAuthenticityExceptionCodes.INVALID_AUTHSERV_IDS.create();
        }

        authServIdsCache.put(key, authServIds);
        return authServIds;
    }

    /**
     * Adds the specified attributes to the specified {@link MailAuthenticityMechanismResult}
     * 
     * @param attributes The attributes to add
     * @param mechResult The {@link MailAuthenticityMechanismResult} to add the attributs to
     */
    private void addProperties(Map<String, String> attributes, AbstractAuthMechResult mechResult) {
        for (String key : attributes.keySet()) {
            mechResult.addProperty(key, attributes.get(key));
        }
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
}
