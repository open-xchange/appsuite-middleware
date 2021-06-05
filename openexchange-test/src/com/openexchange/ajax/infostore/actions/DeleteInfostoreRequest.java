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

package com.openexchange.ajax.infostore.actions;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class DeleteInfostoreRequest extends AbstractInfostoreRequest<DeleteInfostoreResponse> {

    private List<String> ids, folders;

    private Date timestamp;
    private Boolean hardDelete;

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setTimestamp(Date timestamps) {
        this.timestamp = timestamps;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setHardDelete(Boolean hardDelete) {
        this.hardDelete = hardDelete;
    }

    public Boolean isHardDelete() {
        return hardDelete;
    }

    public DeleteInfostoreRequest(List<String> ids, List<String> folders, Date timestamp) {
        this();
        setIds(ids);
        setFolders(folders);
        setTimestamp(timestamp);
    }

    public DeleteInfostoreRequest() {
        super();
        setIds(new LinkedList<String>());
        setFolders(new LinkedList<String>());
    }

    public DeleteInfostoreRequest(String id, String folder, Date timestamp) {
        this();
        setIds(Arrays.asList(id));
        setFolders(Arrays.asList(folder));
        setTimestamp(timestamp);
    }

    @Override
    public Object getBody() throws JSONException {
        return writeFolderAndIDList(getIds(), getFolders());
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        Params params = new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE, AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(getTimestamp().getTime()));
        if (null != hardDelete) {
            params.add("hardDelete", String.valueOf(hardDelete));
        }
        return params.toArray();
    }

    @Override
    public AbstractAJAXParser<? extends DeleteInfostoreResponse> getParser() {
        return new AbstractAJAXParser<DeleteInfostoreResponse>(getFailOnError()) {

            @Override
            protected DeleteInfostoreResponse createResponse(final Response response) throws JSONException {
                return new DeleteInfostoreResponse(response);
            }
        };
    }

}
