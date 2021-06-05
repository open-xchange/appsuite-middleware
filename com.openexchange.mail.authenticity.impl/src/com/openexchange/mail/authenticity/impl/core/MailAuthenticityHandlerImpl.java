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

    private static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailAuthenticityHandlerImpl.class);

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
    public MailAuthenticityHandlerImpl(TrustedMailService trustedMailService, ServiceLookup services, CustomRuleChecker checker) {
        this(0, trustedMailService, services, checker);
    }

    /**
     * Initializes a new {@link MailAuthenticityHandlerImpl}.
     *
     * @param ranking The ranking of this handler; a higher value means higher priority
     * @param services The service look-up
     */
    public MailAuthenticityHandlerImpl(int ranking, TrustedMailService trustedMailService, ServiceLookup services, CustomRuleChecker checker) {
        super();
        this.services = services;
        this.trustedMailService = trustedMailService;
        this.ranking = ranking;
        this.authServIdsCache = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();
        this.requiredMailFields = ImmutableList.of(MailField.FROM);
        this.validator = new StandardAuthenticationResultsValidator();
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
    public void handle(Session session, MailMessage mailMessage) {
        if (mailMessage.containsAuthenticityResult()) {
            // Appears that authenticity results has already been set for specified MailMessage instance
            return;
        }

        HeaderCollection headerCollection = mailMessage.getHeaders();
        String[] authHeaders = headerCollection.getHeader(MessageHeaders.HDR_AUTHENTICATION_RESULTS);
        if (authHeaders == null || authHeaders.length == 0) {
            // No 'Authentication-Results header' - set overall status to 'neutral'
            mailMessage.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
            logMetrics(mailMessage.getMessageId(), Collections.emptyList(), mailMessage.getAuthenticityResult());
            return;
        }

        InternetAddress[] from = mailMessage.getFrom();
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

    @Override
    public Collection<MailField> getRequiredFields() {
        return requiredMailFields;
    }

    @Override
    public Collection<String> getRequiredHeaders() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEnabled(Session session) {
        // MailAuthenticityProperty.enabled is already checked in MailAuthenticityHandlerRegistryImpl
        // No further individual conditions to check
        return true;
    }

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
        String sAuthServIds = leanConfigService.getProperty(userId, contextId, MailAuthenticityProperty.AUTHSERV_ID);
        if (Strings.isEmpty(sAuthServIds)) {
            throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }

        List<String> tokens = Arrays.asList(Strings.splitByComma(sAuthServIds));
        if (tokens == null || tokens.isEmpty() || tokens.contains("")) {
            throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
        }

        List<AllowedAuthServId> authServIds = AllowedAuthServId.allowedAuthServIdsFor(tokens);
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
    private void logMetrics(String mailId, List<String> authHeaders, MailAuthenticityResult overallResult) {
        MailAuthenticityMetricLogger metricLogger = services.getService(MailAuthenticityMetricLogger.class);
        if (metricLogger == null) {
            return;
        }
        metricLogger.log(mailId, authHeaders, overallResult);
    }

}
