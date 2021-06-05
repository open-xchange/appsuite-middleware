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

package com.openexchange.ajax.passwordchange.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link PasswordChangeUpdateRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class PasswordChangeUpdateRequest extends AbstractPasswordChangeResourceRequest<PasswordChangeUpdateResponse> {

    private final boolean failOnError;

    private final String newPassword;

    private final String oldPassword;

    /**
     * Initializes a new {@link PasswordChangeUpdateRequest}
     *
     * @param newPassword
     *            The new password
     * @param oldPassword
     *            The old password
     * @param failOnError
     *            <code>true</code> to fail on error; otherwise
     *            <code>false</code>
     */
    public PasswordChangeUpdateRequest(final String newPassword, final String oldPassword, final boolean failOnError) {
        super();
        this.failOnError = failOnError;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put("new_password", null == newPassword ? JSONObject.NULL : newPassword);
        retval.put("old_password", null == oldPassword ? JSONObject.NULL : oldPassword);
        return retval;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public ResourceUpdateParser getParser() {
        return new ResourceUpdateParser(failOnError);
    }

    private static final class ResourceUpdateParser extends AbstractAJAXParser<PasswordChangeUpdateResponse> {

        /**
         * Default constructor.
         */
        ResourceUpdateParser(final boolean failOnError) {
            super(failOnError);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected PasswordChangeUpdateResponse createResponse(final Response response) throws JSONException {
            return new PasswordChangeUpdateResponse(response);
        }
    }

}
