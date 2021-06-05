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

package com.openexchange.ajax.mail.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link MoveMailToCategoryRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MoveMailToCategoryRequest extends AbstractMailCategoriesRequest<MoveMailToCategoryResponse> {

    private static final String PARAMETER_CATEGORY_ID = "category_id";
    private static final String ACTION_MOVE = "move";

    private final String categoryId;
    private List<JSONObject> body;

    /**
     * Initializes a new {@link MoveMailToCategoryRequest}.
     */
    public MoveMailToCategoryRequest(String categoryId) {
        super();
        this.categoryId = categoryId;
        this.body = new ArrayList<>();
    }

    public void addMail(String id, String folder) throws JSONException {
        JSONObject obj = new JSONObject(2);
        obj.put("id", id);
        obj.put("folder_id", folder);
        body.add(obj);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(PARAMETER_ACTION, ACTION_MOVE));
        list.add(new Parameter(PARAMETER_CATEGORY_ID, categoryId));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends MoveMailToCategoryResponse> getParser() {
        return new AbstractAJAXParser<MoveMailToCategoryResponse>(true) {

            @Override
            protected MoveMailToCategoryResponse createResponse(final Response response) {
                return new MoveMailToCategoryResponse(response);
            }
        };
    }

    @Override
    public Object getBody() {
        return new JSONArray(body);
    }

}
