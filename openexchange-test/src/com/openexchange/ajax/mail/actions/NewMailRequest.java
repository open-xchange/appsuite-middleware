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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link NewMailRequest} - The request for <code>/ajax/mail?action=new</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NewMailRequest extends AbstractMailRequest<NewMailResponse> {

    private final String folder;
    private final String rfc822;
    private final int flags;
    private final boolean failOnError;

    public NewMailRequest(String folder, String rfc822, int flags) {
        this(folder, rfc822, flags, true);
    }

    public NewMailRequest(String folder, String rfc822, int flags, boolean failOnError) {
        super();
        this.folder = folder;
        this.rfc822 = rfc822;
        this.flags = flags;
        this.failOnError = failOnError;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public Object getBody() {
        return rfc822;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(Mail.PARAMETER_ACTION, Mail.ACTION_NEW));
        list.add(new Parameter(Mail.PARAMETER_SRC, "1"));
        if (folder != null) {
            list.add(new Parameter(Mail.PARAMETER_FOLDERID, folder));
        }
        if (flags >= 0) {
            list.add(new Parameter(Mail.PARAMETER_FLAGS, flags));
        }
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<NewMailResponse> getParser() {
        return new AbstractAJAXParser<NewMailResponse>(failOnError) {

            @Override
            protected NewMailResponse createResponse(final Response response) throws JSONException {
                NewMailResponse retval = new NewMailResponse(response);
                JSONObject json = (JSONObject) response.getData();
                if (json.has(CommonFields.FOLDER_ID)) {
                    retval.setFolder(json.getString(CommonFields.FOLDER_ID));
                }
                if (json.has(CommonFields.ID)) {
                    retval.setId(json.getString(CommonFields.ID));
                }
                return retval;
            }
        };
    }
}
