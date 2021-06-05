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

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.ChallengeAnswer;
import com.openexchange.multifactor.MultifactorAuthenticator;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.json.converter.MultifactorDevicesResultConverter;
import com.openexchange.server.ServiceLookup;

/**
 * {@link FinishMultifactorRegistrationAction} - Finishes the registration process of a new {@link MultifactorDevice}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class FinishMultifactorRegistrationAction extends AbstractMultifactorAction {

    /**
     * Initializes a new {@link FinishMultifactorRegistrationAction}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     * @throws OXException
     */
    public FinishMultifactorRegistrationAction(ServiceLookup serviceLookup) throws OXException {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXMultifactorRequest request) throws OXException {
        final MultifactorProvider provider = requireProvider(request.getProviderName());
        String deviceId = request.getDeviceID();
        ChallengeAnswer answer = new ChallengeAnswer(request.requireJsonBody().asMap());

        final MultifactorAuthenticator authenticator = serviceLookup.getServiceSafe(MultifactorAuthenticatorFactory.class).createAuthenticator(provider);
        MultifactorDevice result = authenticator.finishRegistration(request.getMultifactorRequest(), deviceId, answer);
        return new AJAXRequestResult(result, MultifactorDevicesResultConverter.INPUT_FORMAT);
    }
}
