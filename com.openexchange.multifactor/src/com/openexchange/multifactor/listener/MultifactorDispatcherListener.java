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

package com.openexchange.multifactor.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ActionBoundDispatcherListener;
import com.openexchange.exception.OXException;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MultifactorDispatcherListener}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorDispatcherListener extends ActionBoundDispatcherListener {

    private final MultifactorLoginService mfService;
    private final String                  module;
    private final String                  action;

    /**
     * Initializes a new {@link MultifactorDispatcherListener}.
     *
     * @param serviceLookup a {@link ServiceLookup}
     */
    public MultifactorDispatcherListener(MultifactorLoginService mfService, String module, String action) {
        this.module = Objects.requireNonNull(module, "Module must not be null");
        this.mfService = Objects.requireNonNull(mfService, "Multifactor Login Service not available");
        this.action = action;
    }

    /**
     * Check if multifactor authentication has been done during this current session and since reload
     *
     * @param requestData The {@link AJAXRequestData}
     * @throws OXException
     */
    public void checkMultifactor(AJAXRequestData requestData) throws OXException {
        mfService.checkRecentMultifactorAuthentication(requestData.getSession());
    }

    @Override
    public void onRequestInitialized(AJAXRequestData requestData) throws OXException {
        checkMultifactor(requestData);
    }

    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        /* no-op */
    }

    @Override
    public void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        /* no-op */
    }

    @Override
    public Set<String> getActions() {
        if (action == null || action.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<String>(Arrays.asList(action));
    }

    @Override
    public String getModule() {
        return module;
    }
}
