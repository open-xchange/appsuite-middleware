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

package com.openexchange.multifactor.json.actions;

import java.util.Objects;
import java.util.Optional;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link AbstractMultifactorAction} - a multifactor action
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
@RestrictedAction(module = RestrictedAction.REQUIRES_FULL_AUTH)
public abstract class AbstractMultifactorAction implements AJAXActionService {

    protected final ServiceLookup serviceLookup;
    protected final UserService userService;

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
        Objects.requireNonNull(session, "session must not be null");
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
