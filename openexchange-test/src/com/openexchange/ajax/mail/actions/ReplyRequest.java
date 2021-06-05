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
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link ReplyRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ReplyRequest extends AbstractMailRequest<ReplyResponse> {

    public enum ViewOption {
        TEXT("text"), HTML("html");

        private String str;

        ViewOption(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return this.str;
        }

        public static ViewOption getBy(String str) {
            for (ViewOption option : values()) {
                if (str.equals(option.toString())) {
                    return option;
                }
            }
            return null;
        }
    }

    protected boolean failOnError;

    protected ViewOption view;

    protected String folderID;

    protected String mailID;

    protected String csid;

    public ReplyRequest() {

    }

    public ReplyRequest(String[] folderAndID) {
        this(folderAndID[0], folderAndID[1]);
    }

    public ReplyRequest(String folderID, String mailID) {
        this.folderID = folderID;
        this.mailID = mailID;
    }

    public ViewOption getView() {
        return view;
    }

    public void setView(ViewOption view) {
        this.view = view;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    public String getAction() {
        return Mail.ACTION_REPLY;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, getAction()));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, folderID));
        list.add(new Parameter(Mail.PARAMETER_ID, mailID));
        if (getView() != null) {
            list.add(new Parameter(Mail.PARAMETER_VIEW, getView().toString()));
        }

        if (Strings.isNotEmpty(csid)) {
            list.add(new Parameter(Mail.PARAMETER_CSID, csid));
        }

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ReplyResponse> getParser() {
        return new AbstractAJAXParser<ReplyResponse>(failOnError) {

            @Override
            protected ReplyResponse createResponse(final Response response) throws JSONException {
                return new ReplyResponse(response);
            }
        };
    }

}
