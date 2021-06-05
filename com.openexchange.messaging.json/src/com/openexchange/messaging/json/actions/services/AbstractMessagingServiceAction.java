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

package com.openexchange.messaging.json.actions.services;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.json.MessagingServiceWriter;
import com.openexchange.messaging.json.Services;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Common superclass of actions for accessing the known messaging services. Subclasses must implement
 * {@link #doIt(AJAXRequestData, ServerSession)}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMessagingServiceAction implements AJAXActionService{

    protected MessagingServiceRegistry registry;

    public AbstractMessagingServiceAction(final MessagingServiceRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            return doIt(requestData, session);
        } catch (JSONException x) {
            throw MessagingExceptionCodes.JSON_ERROR.create(x, x.getMessage());
        }
    }

    protected abstract AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException;

    protected final MessagingServiceWriter getWriter(final ServerSession session) {
        I18nServiceRegistry registry = Services.optService(I18nServiceRegistry.class);
        if (registry == null) {
            return  new MessagingServiceWriter(Translator.EMPTY);
        }

        I18nService service = registry.getI18nService(session.getUser().getLocale());
        return new MessagingServiceWriter(null == service ? Translator.EMPTY : new I18nTranslator(service));
    }
}
