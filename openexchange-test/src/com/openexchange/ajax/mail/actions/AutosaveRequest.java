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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link AutosaveRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AutosaveRequest extends AbstractMailRequest<MailReferenceResponse> {

    private boolean failOnError;

    private JSONObject mail;

    public AutosaveRequest(JSONObject mail) {
        this(mail, true);
    }

    public AutosaveRequest(JSONObject mail, boolean failOnError) {
        super();
        this.mail = mail;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new LinkedList<Parameter>();
        list.add(new URLParameter(Mail.PARAMETER_ACTION, Mail.ACTION_AUTOSAVE));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return mail;
    }

    @Override
    public AbstractAJAXParser<? extends MailReferenceResponse> getParser() {
        return new AbstractAJAXParser<MailReferenceResponse>(failOnError) {

            @Override
            protected MailReferenceResponse createResponse(Response response) throws JSONException {
                return new MailReferenceResponse(response);
            }
        };
    }

}
