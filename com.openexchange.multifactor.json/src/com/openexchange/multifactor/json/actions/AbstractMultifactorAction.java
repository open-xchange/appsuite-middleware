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

package com.openexchange.multifactor.json.actions;

import java.util.Objects;
import java.util.Optional;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link AbstractMultifactorAction} - a multifactor action
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public abstract class AbstractMultifactorAction implements AJAXActionService {

    protected final ServiceLookup serviceLookup;
    protected final UserService   userService;

    /**
     * Initializes a new {@link AbstractMultifactorAction}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     * @throws OXException in case {@link UserService} is not available
     */
    public AbstractMultifactorAction(ServiceLookup serviceLookup) throws OXException {
        this.serviceLookup = Objects.requireNonNull(serviceLookup, "serviceLookup must not be null");
        this.userService = serviceLookup.getServiceSafe(UserService.class);
    }

    /**
     * Creates a {@link AJAXMultifactorRequest} from the given parameters.
     *
     * @param requestData The {@link AJAXRequestData} sent with the request
     * @param session The {@link ServerSession} to create the {@link AJAXMultifactorRequest} from
     * @return The {@link AJAXMultifactorRequest}
     * @throws OXException
     */
    private AJAXMultifactorRequest createaMultifactorRequest(AJAXRequestData requestData, ServerSession session) {
        session = Objects.requireNonNull(session, "session must not be null");
        final User user = session.getUser();
        if (user == null) {
            return new AJAXMultifactorRequest(requestData, session, null);
        }
        return new AJAXMultifactorRequest(requestData, session, user.getLocale());
    }

    /**
     * Gets the provider with the given name
     *
     * @param providerName The name of the provider
     * @return An {@link Optional} with the provider, or an empty {@link Optional} if no such provider was found
     * @throws OXException
     */
    Optional<MultifactorProvider> getProvider(String providerName) throws OXException {
        return serviceLookup.getServiceSafe(MultifactorProviderRegistry.class).getProvider(providerName);
    }

    /**
     * Gets the provider with the given name, or throws an Exception if the given provider was not found
     *
     * @param providerName The name of the provider
     * @return The provider with the given name
     * @throws OXException
     */
    MultifactorProvider requireProvider(String providerName) throws OXException {
        final Optional<MultifactorProvider> provider = getProvider(providerName);
        if (provider.isPresent()) {
            return provider.get();
        }
        throw MultifactorExceptionCodes.UNKNOWN_PROVIDER.create(providerName);
    }

    /**
     * Gets the provider with name, specified in the given {@link MultifactorDevice}, or throws an exception if the provider was not found
     *
     * @param device The device specifying the provider
     * @return The provider with the specified name
     * @throws OXException
     */
    MultifactorProvider requireProvider(MultifactorDevice device) throws OXException {
       return requireProvider(device.getProviderName());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.ajax.requesthandler.AJAXActionService#perform(com.openexchange.ajax.requesthandler.AJAXRequestData, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return doPerform(createaMultifactorRequest(requestData, session));
    }

    /**
     * Performs the multifactor action
     *
     * @param request The request data used to perform the action
     * @return The result of the action
     * @throws OXException
     */
    protected abstract AJAXRequestResult doPerform(AJAXMultifactorRequest request) throws OXException;
}
