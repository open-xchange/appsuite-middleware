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

package com.openexchange.ajax.folder.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.folderstorage.FolderStorage;

/**
 * {@link SubscribeRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SubscribeRequest extends AbstractFolderRequest<SubscribeResponse> {

    private final String parent;

    private final boolean failOnError;

    private final List<String> folderIds;

    private final List<Boolean> flags;

    public SubscribeRequest(final API api, final boolean failOnError) {
        this(api, FolderStorage.ROOT_ID, failOnError);
    }

    public SubscribeRequest(final API api, final String parent, final boolean failOnError) {
        super(api);
        this.parent = parent;
        this.failOnError = failOnError;
        folderIds = new ArrayList<String>(4);
        flags = new ArrayList<Boolean>(4);
    }

    /**
     * Adds specified folder identifier.
     *
     * @param folderId The folder identifier
     * @param subscribe <code>true</code> to subscribe denoted folder to tree; otherwise <code>false</code>
     * @return This request with folder identifier added
     */
    public SubscribeRequest addFolderId(final String folderId, final boolean subscribe) {
        folderIds.add(folderId);
        flags.add(Boolean.valueOf(subscribe));
        return this;
    }

    @Override
    public Object getBody() {
        try {
            final JSONArray jArray = new JSONArray();
            final int size = folderIds.size();
            for (int i = 0; i < size; i++) {
                final JSONObject jObject = new JSONObject();
                jObject.put("id", folderIds.get(i));
                jObject.put("subscribe", flags.get(i).booleanValue());
                jArray.put(jObject);
            }
            return jArray;
        } catch (final JSONException e) {
            return new JSONArray();
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "subscribe"));
        params.add(new Parameter(Folder.PARAMETER_PARENT, parent));
    }

    @Override
    public AbstractAJAXParser<? extends SubscribeResponse> getParser() {
        return new AbstractAJAXParser<SubscribeResponse>(failOnError) {

            @Override
            protected SubscribeResponse createResponse(final Response response) {
                return new SubscribeResponse(response);
            }
        };
    }
}
