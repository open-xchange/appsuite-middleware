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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractListParser;
import com.openexchange.file.storage.File;

/**
 * {@link ListInfostoreRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ListInfostoreRequest extends AbstractInfostoreRequest<ListInfostoreResponse> {

    private final List<ListItem> items;

    private final int columns[];

    public ListInfostoreRequest(final int columns[]) {
        this(new LinkedList<ListItem>(), columns);
    }

    public ListInfostoreRequest(final List<ListItem> items, final int columns[]) {
        this(items, columns, true);
    }

    public ListInfostoreRequest(final List<ListItem> items, final int columns[], boolean failOnError) {
        super();
        this.items = items;
        this.columns = columns;
        setFailOnError(failOnError);
    }

    public void addItem(ListItem item) {
        items.add(item);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns), new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST)
        };
    }

    @Override
    public ListInfostoreParser getParser() {
        return new ListInfostoreParser(getFailOnError(), columns);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONArray jItems = new JSONArray();
        for (ListItem item : items) {
            jItems.put(item.toJSON());
        }
        return jItems;
    }

    public static final class ListInfostoreParser extends AbstractListParser<ListInfostoreResponse> {

        public ListInfostoreParser(boolean failOnError, int[] columns) {
            super(failOnError, columns);
        }

        @Override
        protected ListInfostoreResponse instantiateResponse(Response response) {
            return new ListInfostoreResponse(response);
        }

    }

    public static final class ListItem {

        private final String folderId;

        private final String id;

        private final String version;

        public ListItem(File metadata) {
            this(metadata.getFolderId(), metadata.getId(), metadata.getVersion());
        }

        public ListItem(String folderId, String id) {
            this(folderId, id, null);
        }

        public ListItem(String folderId, String id, String version) {
            super();
            this.folderId = folderId;
            this.id = id;
            this.version = version;
        }

        public String getFolderId() {
            return folderId;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject jItem = new JSONObject();
            jItem.put("folder", folderId);
            jItem.put("id", id);
            jItem.put("version", version);
            return jItem;
        }
    }

}
