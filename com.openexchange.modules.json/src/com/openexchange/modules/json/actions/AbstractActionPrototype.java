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

package com.openexchange.modules.json.actions;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.modules.json.ModelParser;
import com.openexchange.modules.json.ModelWriter;
import com.openexchange.modules.model.Model;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractActionPrototype}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractActionPrototype<T extends Model<T>> implements AJAXActionService{

    private final ModelParser<T> parser;
    private final ModelWriter<T> writer;

    public AbstractActionPrototype(final ModelParser<T> parser, final ModelWriter<T> writer) {
        super();
        this.parser = parser;
        this.writer = writer;
    }


    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        final RequestPrototype<T> req = createRequest(requestData, parser, session);
        try {
            return perform(req);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }


    protected abstract AJAXRequestResult perform(RequestPrototype<T> req) throws JSONException, OXException;


    protected RequestPrototype<T> createRequest(final AJAXRequestData request, final ModelParser<T> modelParser, final ServerSession session) {
        return new RequestPrototype<T>(request, modelParser, session);
    }

    protected AJAXRequestResult result(final T thing) throws JSONException {
        return new AJAXRequestResult(writer.write(thing));
    }

    protected AJAXRequestResult result(final List<T> things) throws JSONException {
        final JSONArray array = new JSONArray();

        for (final T thing : things) {
            array.put(writer.write(thing));
        }

        return new AJAXRequestResult(array);
    }

}
