/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        return new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns),
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST)
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
