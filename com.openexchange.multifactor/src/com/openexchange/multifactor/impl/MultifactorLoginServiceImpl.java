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

package com.openexchange.multifactor.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.multifactor.ChallengeAnswer;
import com.openexchange.multifactor.DefaultMultifactorDevice;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorProviderStrategy;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.SingleMultifactorProviderStrategy;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link MultifactorLoginServiceImpl}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorLoginServiceImpl implements MultifactorLoginService {

    private static final Object PARAM_AUTH_PROVIDER_NAME = "providerName";
    private static final Object PARAM_AUTH_DEVICE_ID = "deviceId";

    private final MultifactorProviderRegistry     registry;
    private final MultifactorAuthenticatorFactory multifactorAuthFactory;
    private final LeanConfigurationService        leanConfigService;

    /**
     * Initializes a new {@link MultifactorLoginServiceImpl}.
     *
     * @param registry The {@link MultifactorProviderRegistry} to use
     * @param multifactorAuthFactory The {@link MultifactorAuthenticatorFactory} to use
     * @param leanConfigService The {@link LeanConfigurationService} to use
     */
    public MultifactorLoginServiceImpl(MultifactorProviderRegistry registry, MultifactorAuthenticatorFactory multifactorAuthFactory, LeanConfigurationService leanConfigService) {
        super();
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.multifactorAuthFactory = Objects.requireNonNull(multifactorAuthFactory, "multifactorAuthenticatorFactory must not be null");
        this.leanConfigService = Objects.requireNonNull(leanConfigService, "LeanConfigurationService must not be null");
    }

    /**
     * Get Map of parameters from the request
     *
     * @param request The request to get parameters from
     * @return The requests's parameters as a map
     */
    private Map<String, Object> getParameters(LoginRequest request) {
        if (request == null) {
            return null;
        }
        final Map<String, String[]> requestParameter = request.getRequestParameter();
        final Map<String, Object> parameters = new HashMap<String, Object>();
        for (final Entry<String, String[]> parameter : requestParameter.entrySet()) {
            final String key = parameter.getKey();
            final String value = parameter.getValue().length > 0 ? parameter.getValue()[0] : null;
            if (value != null) {
                parameters.put(key, value);
            }
        }
        return parameters;
    }

    /**
     * Check multifactor authentication done recently. Compares last verified time in the session with configured
     * recentAuthenticationTime.
     *
     * @param session The user session
     * @return <code>true</code> if authentication within configured timeframe, <code>false</code> otherwise
     */
    private boolean checkCurrent(Session session) {
        Object lastValidated = session.getParameter(Session.MULTIFACTOR_LAST_VERIFIED);
        if (lastValidated == null || !(lastValidated instanceof Long)) {
            return false;
        }

        int validMinutes = leanConfigService.getIntProperty(session.getUserId(), session.getContextId(), MultifactorProperties.recentAuthenticationTime);
        if (validMinutes == 0) {  // No restriction in valid time
            return true;
        }

        Calendar lastVer = Calendar.getInstance();
        lastVer.setTime(new Date((Long) lastValidated));
        lastVer.add(Calendar.MINUTE, validMinutes);

        return lastVer.after(Calendar.getInstance());

    }

    @Override
    public boolean checkMultiFactorAuthentication(int userId, int contextId, Locale locale, LoginRequest request) throws OXException {
        final MultifactorRequest multifactorRequest = new MultifactorRequest(contextId, userId, request.getServerName(), locale);
        final Collection<MultifactorProvider> multifactorProviders = registry.getProviders(multifactorRequest);
        if (multifactorProviders != null && !multifactorProviders.isEmpty()) {
            DefaultMultifactorDevice device = new DefaultMultifactorDevice();
            device.setProviderName((String) getParameters(request).get(PARAM_AUTH_PROVIDER_NAME));
            device.setId((String) getParameters(request).get(PARAM_AUTH_DEVICE_ID));
            final MultifactorProviderStrategy multifactorStrategy = new SingleMultifactorProviderStrategy(multifactorAuthFactory, device, new ChallengeAnswer(getParameters(request)));
            try {
                return multifactorStrategy.requireAuthentication(multifactorProviders, multifactorRequest);
            } catch (final OXException ex) {
                // Failed to auth
                throw MultifactorExceptionCodes.INVALID_AUTHENTICATION_FACTOR.create(ex);
            }
        }
        return false;
    }

    @Override
    public boolean requiresMultifactor(int userId, int contextId) throws OXException {
        final MultifactorRequest request = new MultifactorRequest(contextId, userId, null, null);
        final Collection<MultifactorProvider> multifactorProviders = registry.getProviders(request);
        for (MultifactorProvider provider : multifactorProviders) {
            if (!provider.getDevices(request).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkRecentMultifactorAuthentication(Session session) throws OXException {
        if (Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_PARAMETER)) &&
            !Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_AUTHENTICATED))) {

            throw MultifactorExceptionCodes.ACTION_REQUIRES_AUTHENTICATION.create();
        }

        if (Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_PARAMETER)) &&
            Boolean.TRUE.equals(session.getParameter(Session.MULTIFACTOR_AUTHENTICATED))
            && !checkCurrent(session)) {

            throw MultifactorExceptionCodes.ACTION_REQUIRES_REAUTHENTICATION.create();
        }
    }
}
