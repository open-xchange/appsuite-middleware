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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.folder.actions.AbstractFolderRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link NotifyFolderRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NotifyFolderRequest extends AbstractFolderRequest<AbstractAJAXResponse> {

    private final String id;
    private final int[] entities;
    private final boolean failOnError;

    /**
     * Initializes a new {@link NotifyFolderRequest}.
     *
     * @param id The folder identifier
     * @param entities The entities to notify
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    public NotifyFolderRequest(String id, int[] entities, boolean failOnError) {
        super(EnumAPI.OX_NEW);
        this.id = id;
        this.entities = entities;
        this.failOnError = failOnError;
    }

    /**
     * Initializes a new {@link NotifyFolderRequest}.
     *
     * @param id The folder identifier
     * @param entities The entities to notify
     */
    public NotifyFolderRequest(String id, int... entities) {
        this(id, entities, true);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonEntities = new JSONArray();
        for (int entity : entities) {
            jsonEntities.put(entity);
        }
        jsonBody.put("entities", jsonEntities);
        return jsonBody;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "notify"));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
    }

    @Override
    public AbstractAJAXParser<? extends AbstractAJAXResponse> getParser() {
        return new AbstractAJAXParser<AbstractAJAXResponse>(failOnError) {

            @Override
            protected AbstractAJAXResponse createResponse(Response response) throws JSONException {
                return new AbstractAJAXResponse(response) {};
            }
        };
    }

}
