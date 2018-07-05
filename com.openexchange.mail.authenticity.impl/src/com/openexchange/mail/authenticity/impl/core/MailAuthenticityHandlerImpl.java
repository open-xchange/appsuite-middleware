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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.InternetAddress;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.AllowedAuthServId;
import com.openexchange.mail.authenticity.MailAuthenticityExceptionCodes;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.MailAuthenticityStatus;
import com.openexchange.mail.authenticity.impl.core.metrics.MailAuthenticityMetricLogger;
import com.openexchange.mail.authenticity.impl.trusted.TrustedMailService;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
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
    private final Collection<MailField> requiredMailFields;
    private final Cache<UserAndContext, List<AllowedAuthServId>> authServIdsCache;
    private final CustomRuleChecker checker;
    private final AuthenticationResultsValidator validator;

    /**
     * Initializes a new {@link MailAuthenticityHandlerImpl} with ranking of <code>0</code> (zero).
     *
     * @param services The service look-up
     */
    public MailAuthenticityHandlerImpl(final TrustedMailService trustedMailService, final ServiceLookup services, CustomRuleChecker checker) {
        this(0, trustedMailService, services, checker);
    }

    /**
     * Initializes a new {@link MailAuthenticityHandlerImpl}.
     *
     * @param ranking The ranking of this handler; a higher value means higher priority
     * @param services The service look-up
     */
    public MailAuthenticityHandlerImpl(final int ranking, final TrustedMailService trustedMailService, final ServiceLookup services, CustomRuleChecker checker) {
        super();
        this.services = services;
        this.trustedMailService = trustedMailService;
        this.ranking = ranking;
        authServIdsCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
        requiredMailFields = ImmutableList.of(MailField.FROM);
        validator = new StandardAuthenticationResultsValidator();
        this.checker = checker;
    }

    /**
     * Clears the authserv-ids cache.
     */
    public void invalidateAuthServIdsCache() {
        authServIdsCache.invalidateAll();
    }

    /**
     * Gets the parser/validator to use.
     *
     * @return The parser/validator instance
     */
    public AuthenticationResultsValidator getValidator() {
        return validator;
    }

    @Override
    public void handle(final Session session, final MailMessage mailMessage) {
        if (mailMessage.containsAuthenticityResult()) {
            // Appears that authenticity results has already been set for specified MailMessage instance
            return;
        }

        final HeaderCollection headerCollection = mailMessage.getHeaders();
        final String[] authHeaders = headerCollection.getHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS);
        if (authHeaders == null || authHeaders.length == 0) {
            // No 'Authentication-Results header' - set overall status to 'none'
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NONE_RESULT);
            logMetrics(mailMessage.getMessageId(), Collections.emptyList(), mailMessage.getAuthenticityResult());
            return;
        }

        final InternetAddress[] from = mailMessage.getFrom();
        if (from == null || from.length == 0) {
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            logMetrics(mailMessage.getMessageId(), Arrays.asList(authHeaders), mailMessage.getAuthenticityResult());
            return;
        }

        List<String> headers = Arrays.asList(authHeaders);
        MailAuthenticityResult authenticityResult = MailAuthenticityResult.NOT_ANALYZED_RESULT;
        try {
            AuthenticationResultsValidator validator = getValidator();
            authenticityResult = validator.parseHeaders(headers, from[0], getAllowedAuthServIds(session));
        } catch (Exception e) {
            LOGGER.error("An error occurred during parsing the '{}' header of mail {} in folder {}", MessageHeaders.HDR_AUTHENTICATION_RESULTS, mailMessage.getMailId(), mailMessage.getFolder(), e);
        }
        mailMessage.setAuthenticityResult(authenticityResult);
        logMetrics(mailMessage.getMessageId(), headers, mailMessage.getAuthenticityResult());

        try {
            checker.check(session, authenticityResult);
        } catch (OXException e) {
            LOGGER.error("An error occurred while checking custom mail authenticity rules for mail {} in folder {}.", mailMessage.getMailId(), mailMessage.getFolder(), e);
        }

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
    private void logMetrics(String mailId, final List<String> authHeaders, MailAuthenticityResult overallResult) {
        MailAuthenticityMetricLogger metricLogger = services.getService(MailAuthenticityMetricLogger.class);
        if (metricLogger == null) {
            return;
        }
        metricLogger.log(mailId, authHeaders, overallResult);
    }

}
