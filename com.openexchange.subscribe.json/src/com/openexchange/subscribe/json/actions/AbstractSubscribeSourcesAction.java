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

package com.openexchange.subscribe.json.actions;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.json.osgi.Services;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public abstract class AbstractSubscribeSourcesAction implements AJAXActionService {

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractSubscribeSourcesAction}.
     */
    protected AbstractSubscribeSourcesAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Performs given subscribe request.
     *
     * @param subscribeRequest The request
     * @return The request result
     * @throws OXException If a Open-Xchange error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException, JSONException;

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            return perform(new SubscribeRequest(requestData, session));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    protected SubscriptionSourceDiscoveryService getAvailableSources(final ServerSession session) throws OXException {
        return services.getService(SubscriptionSourceDiscoveryService.class).filter(session.getUserId(), session.getContextId());
    }

    protected Translator createTranslator(final ServerSession session) {
        I18nServiceRegistry registry = Services.getService(I18nServiceRegistry.class);
        if (registry == null) {
            return Translator.EMPTY;
        }
        I18nService service = registry.getI18nService(session.getUser().getLocale());
        return null == service ? Translator.EMPTY : new I18nTranslator(service);
    }

}
