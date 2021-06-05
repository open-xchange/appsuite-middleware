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

package com.openexchange.ajax.folder.actions;

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonDeleteParser;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * Stores the parameters to delete a folder.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - working with mail folders
 */
public class DeleteRequest extends AbstractFolderRequest<CommonDeleteResponse> {

    private final String[] folderIds;
    private final Date lastModified;
    private final boolean failOnError;

    private Boolean hardDelete;
    private Boolean failOnErrorParam;

    public DeleteRequest(final API api, final String[] folderIds, final Date lastModified, final boolean failOnError) {
        super(api);
        this.folderIds = folderIds;
        this.lastModified = lastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final API api, final String[] folderIds, final Date lastModified) {
        this(api, folderIds, lastModified, true);
    }

    public DeleteRequest(final API api, final String folderId, final Date lastModified) {
        this(api, new String[] { folderId }, lastModified);
    }

    public DeleteRequest(final API api, final int[] folderIds, final Date lastModified) {
        this(api, i2s(folderIds), lastModified);
    }

    public DeleteRequest(final API api, final int[] folderIds, final Date lastModified, boolean failOnError) {
        this(api, i2s(folderIds), lastModified, failOnError);
    }

    public DeleteRequest(final API api, final int folderId, final Date lastModified) {
        this(api, new int[] { folderId }, lastModified);
    }

    public DeleteRequest(final API api, final boolean failOnError, final FolderObject... folder) {
        super(api);
        folderIds = new String[folder.length];
        Date maxLastModified = new Date(Long.MIN_VALUE);
        for (int i = 0; i < folder.length; i++) {
            if (folder[i].containsObjectID()) { // task, appointment or contact folder
                folderIds[i] = Integer.valueOf(folder[i].getObjectID()).toString();
            } else { // mail folder
                folderIds[i] = folder[i].getFullName();
            }
            if (maxLastModified.before(folder[i].getLastModified())) {
                maxLastModified = folder[i].getLastModified();
            }
        }
        lastModified = maxLastModified;
        this.failOnError = failOnError;
    }

    public DeleteRequest(final API api, final FolderObject... folder) {
        this(api, true, folder);
    }

    public DeleteRequest setHardDelete(Boolean hardDelete) {
        this.hardDelete = hardDelete;
        return this;
    }

    public Boolean isHardDelete() {
        return hardDelete;
    }

    public DeleteRequest setFailOnErrorParam(Boolean failOnError) {
        this.failOnErrorParam = failOnError;
        return this;
    }

    public Boolean isFailOnErrorParam() {
        return failOnErrorParam;
    }

    @Override
    public Object getBody() {
        final JSONArray array = new JSONArray();
        for (final String folderId : folderIds) {
            array.put(folderId);
        }
        return array;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE));
        params.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified.getTime()));
        if (null != hardDelete) {
            params.add(new Parameter("hardDelete", String.valueOf(hardDelete)));
        }
        if (null != failOnErrorParam) {
            params.add(new Parameter("failOnError", String.valueOf(failOnErrorParam)));
        }
    }

    @Override
    public CommonDeleteParser getParser() {
        return new CommonDeleteParser(failOnError);
    }
}
