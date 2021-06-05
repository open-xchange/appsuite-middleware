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
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link UpdateMailRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateMailRequest extends AbstractMailRequest<UpdateMailResponse> {

    private String folderID;

    private String mailID;

    private int flags;

    private int color;

    private boolean removeFlags;

    private boolean failOnError;

    private boolean messageId;

    public UpdateMailRequest setMessageId(final boolean messageId) {
        this.messageId = messageId;
        return this;
    }

    public boolean doesFailOnError() {
        return failOnError;
    }

    public void setFailOnError(final boolean failOnError) {
        this.failOnError = failOnError;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setFolderID(final String folderID) {
        this.folderID = folderID;
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(final String mailID) {
        this.mailID = mailID;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public boolean doesRemoveFlags() {
        return removeFlags;
    }

    public void removeFlags() {
        this.removeFlags = true;
    }

    public boolean doesUpdateFlags() {
        return !removeFlags;
    }

    public void updateFlags() {
        this.removeFlags = false;
    }

    /**
     * Initializes a new {@link UpdateMailRequest}.
     */
    public UpdateMailRequest(final String folderID) {
        super();
        this.folderID = folderID;
        flags = -1;
        color = -1;
    }

    /**
     * Initializes a new {@link UpdateMailRequest}.
     */
    public UpdateMailRequest(final String folderID, final String mailID) {
        super();
        this.folderID = folderID;
        this.mailID = mailID;
        flags = -1;
        color = -1;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        if (color >= 0) {
            json.put("color_label", color);
        }
        if (flags >= 0) {
            json.put("flags", flags);
            json.put("value", !removeFlags);
        }
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        final List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, Mail.ACTION_UPDATE));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, folderID));
        if (null != mailID) {
            list.add(new Parameter(messageId ? Mail.PARAMETER_MESSAGE_ID : Mail.PARAMETER_ID, mailID));
        }
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends UpdateMailResponse> getParser() {
        return new AbstractAJAXParser<UpdateMailResponse>(failOnError) {

            @Override
            protected UpdateMailResponse createResponse(final Response response) throws JSONException {
                return new UpdateMailResponse(response);
            }
        };
    }

}
