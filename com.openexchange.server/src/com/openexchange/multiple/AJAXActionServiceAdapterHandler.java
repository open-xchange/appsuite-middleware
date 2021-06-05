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

package com.openexchange.multiple;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * Gleamed from the UserMultipleHandler
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXActionServiceAdapterHandler implements MultipleHandler, MultipleHandlerFactoryService {

    private AJAXActionServiceFactory factory = null;

    private AJAXRequestResult result;

    private final String module;

    public AJAXActionServiceAdapterHandler(final AJAXActionServiceFactory factory, final String module) {
        this.factory = factory;
        this.module = module;
    }

    @Override
    public void close() {
        result = null;
    }

    @Override
    public Date getTimestamp() {
        if (null == result) {
            return null;
        }
        final Date timestamp = result.getTimestamp();
        return null == timestamp ? null : new Date(timestamp.getTime());
    }

    @Override
    public Collection<OXException> getWarnings() {
        if (null == result) {
            return Collections.<OXException> emptySet();
        }
        return result.getWarnings();
    }

    @Override
    public Object performRequest(final String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws JSONException, OXException {
        final AJAXActionService actionService = factory.createActionService(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        final AJAXRequestData request = new AJAXRequestData();
        request.setSecure(secure);
        request.setHostname(jsonObject.getString(HOSTNAME));
        request.setRoute(jsonObject.getString(ROUTE));
        request.setRemoteAddress(jsonObject.getString(REMOTE_ADDRESS));
        for (final Entry<String, Object> entry : jsonObject.entrySet()) {
            final String key = entry.getKey();
            if (DATA.equals(key)) {
                request.setData(entry.getValue());
            } else {
                request.putParameter(key, entry.getValue().toString());
            }
        }
        try {
            result = actionService.perform(request, session);
        } catch (IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            request.cleanUploads();
        }
        return result.getResultObject();
    }

    @Override
    public MultipleHandler createMultipleHandler() {
        return this;
    }

    @Override
    public String getSupportedModule() {
        return module;
    }

}
