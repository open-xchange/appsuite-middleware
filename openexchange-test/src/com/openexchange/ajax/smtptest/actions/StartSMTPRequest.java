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

package com.openexchange.ajax.smtptest.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Params;

/**
 * 
 * {@link StartSMTPRequest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class StartSMTPRequest implements AJAXRequest<SMTPInitResponse> {

    private final boolean updateAccount;

    private int updateNoReplyForContext = -1;

    private boolean failOnError = true;
    
    private String noReplyAddress;

    public StartSMTPRequest() {
        this(true);
    }

    public StartSMTPRequest(boolean updateAccount) {
        this(updateAccount, -1);
    }

    public StartSMTPRequest(boolean updateAccount, int updateNoReplyForContext) {
        this(updateAccount, updateNoReplyForContext, "no-reply@ox.io");
    }

    public StartSMTPRequest(boolean updateAccount, int updateNoReplyForContext, String noReplyAddress) {
        super();
        this.updateAccount = updateAccount;
        this.updateNoReplyForContext = updateNoReplyForContext;
        this.noReplyAddress = noReplyAddress;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void setUpdateNoReplyForContext(int updateNoReplyForContext) {
        this.updateNoReplyForContext = updateNoReplyForContext;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/smtpserver/test";
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Params(AJAXServlet.PARAMETER_ACTION, "startSMTP", "updateAccount", Boolean.toString(updateAccount), "updateNoReplyForContext", Integer.toString(updateNoReplyForContext), "noReplyAdress", noReplyAddress).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends SMTPInitResponse> getParser() {
        return new AbstractAJAXParser<SMTPInitResponse>(failOnError) {

            @Override
            protected SMTPInitResponse createResponse(Response response) throws JSONException {
                return new SMTPInitResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

}
