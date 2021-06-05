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
 * {@link TrainRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class TrainRequest extends AbstractMailCategoriesRequest<TrainResponse> {

    private static final String ACTION_TRAIN = "train";
    private static final String PARAMETER_CATEGORY_ID = "category_id";
    private static final String PARAMETER_APPLY_FOR_FUTURE = "apply-for-future-ones";
    private static final String PARAMETER_APPLY_FOR_EXISTING = "apply-for-existing";

    private String categoryId;
    private boolean future = true;
    private boolean past = false;
    private List<String> mails = new ArrayList<>();

    /**
     * Initializes a new {@link TrainRequest}.
     */
    public TrainRequest(String categoryId, boolean applyToFutureOnes, boolean applyToExistingOnes) {
        super();
        this.future = applyToFutureOnes;
        this.past = applyToExistingOnes;
        this.categoryId = categoryId;
    }

    /**
     * Initializes a new {@link TrainRequest}.
     */
    public TrainRequest(String categoryId) {
        super();
        this.categoryId = categoryId;
    }

    public void addAddress(String emailAddress) {
        mails.add(emailAddress);
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(PARAMETER_ACTION, ACTION_TRAIN));
        list.add(new Parameter(PARAMETER_CATEGORY_ID, categoryId));
        list.add(new Parameter(PARAMETER_APPLY_FOR_FUTURE, future));
        list.add(new Parameter(PARAMETER_APPLY_FOR_EXISTING, past));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends TrainResponse> getParser() {
        return new AbstractAJAXParser<TrainResponse>(true) {

            @Override
            protected TrainResponse createResponse(final Response response) {
                return new TrainResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws JSONException {
        JSONObject obj = new JSONObject(1);
        JSONArray array = new JSONArray(mails);
        obj.put("from", array);
        return obj;
    }

}
