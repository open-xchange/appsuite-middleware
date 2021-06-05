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
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ArchiveRequest}
 *
 */
public class ArchiveRequest extends AbstractMailRequest<ArchiveResponse> {

    private final String sourceFolderID;
    private final boolean failOnError = true;
    private final String[] mailIDs;
    private Boolean useDefaultName;
    private Boolean createIfAbsent;

    public ArchiveRequest(String[] mailIDs, String sourceFolderID) {
        this.mailIDs = mailIDs;
        this.sourceFolderID = sourceFolderID;
    }

    /**
     * Sets the createIfAbsent
     *
     * @param createIfAbsent The createIfAbsent to set
     */
    public void setCreateIfAbsent(boolean createIfAbsent) {
        this.createIfAbsent = Boolean.valueOf(createIfAbsent);
    }

    /**
     * Sets the useDefaultName
     *
     * @param useDefaultName The useDefaultName to set
     */
    public void setUseDefaultName(boolean useDefaultName) {
        this.useDefaultName = Boolean.valueOf(useDefaultName);
    }

    @Override
    public Object getBody() throws JSONException {
        int length = mailIDs.length;
        JSONArray jso = new JSONArray(length);
        for (int i = 0; i < length; i++) {
            jso.put(mailIDs[i]);
        }
        return jso;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<Parameter>();

        list.add(new Parameter(Mail.PARAMETER_ACTION, "archive"));
        list.add(new Parameter(Mail.PARAMETER_FOLDERID, sourceFolderID));
        if (null != useDefaultName) {
            list.add(new Parameter("useDefaultName", useDefaultName.toString()));
        }
        if (null != createIfAbsent) {
            list.add(new Parameter("createIfAbsent", createIfAbsent.toString()));
        }

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ArchiveResponse> getParser() {
        return new AbstractAJAXParser<ArchiveResponse>(failOnError) {

            @Override
            protected ArchiveResponse createResponse(final Response response) throws JSONException {
                return new ArchiveResponse(response);
            }
        };
    }

}
