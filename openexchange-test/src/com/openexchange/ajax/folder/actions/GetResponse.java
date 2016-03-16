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

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.parser.ParsedFolder;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.webdav.xml.fields.FolderFields;

/**
 * {@link GetResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class GetResponse extends AbstractAJAXResponse {

    private FolderObject folder;
    private Folder storageFolder;

    /**
     * Initializes a new {@link GetResponse}
     *
     * @param response The response
     */
    public GetResponse(final Response response) {
        super(response);
    }

    /**
     * @return the folder
     * @throws OXException parsing the folder out of the response fails.
     */
    public FolderObject getFolder() throws OXException, OXException {
        if (hasError()) {
            return null;
        }
        if (null == folder) {
            final FolderObject parsed = new FolderObject();
            final JSONObject data = (JSONObject) getData();
            try {
                if (data.has(FolderFields.ID)) {
                    rearrangeId(data);
                }
                if (data.has(FolderFields.FOLDER_ID)) {
                    final String tmp = data.getString(FolderFields.FOLDER_ID);
                    if (tmp.startsWith(FolderObject.SHARED_PREFIX)) {
                        data.put(FolderFields.FOLDER_ID, Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID));
                    }
                }
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create();
            }
            new FolderParser().parse(parsed, data);// .parse(parsed, (JSONObject) getData());
            fillInFullName(data, parsed);
            this.folder = parsed;
        }
        return folder;
    }

    public Folder getStorageFolder() throws OXException {

        if (hasError()) {
            return null;
        }
        if (null == storageFolder) {
            final JSONObject data = (JSONObject) getData();
            try {
                if (data.has(FolderFields.ID)) {
                    rearrangeId(data);
                }
                if (data.has(FolderFields.FOLDER_ID)) {
                    final String tmp = data.getString(FolderFields.FOLDER_ID);
                    if (tmp.startsWith(FolderObject.SHARED_PREFIX)) {
                        data.put(FolderFields.FOLDER_ID, Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID));
                    }
                }
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create();
            }
            com.openexchange.folder.json.parser.FolderParser parser = new com.openexchange.folder.json.parser.FolderParser(new StaticDiscoveryService());

            ParsedFolder pfolder = parser.parseFolder(data, TimeZone.getDefault());// .parse(parsed, (JSONObject) getData());

            this.storageFolder = pfolder;
        }
        return storageFolder;

    }

    private void fillInFullName(final JSONObject data, final FolderObject parsed) {
        if (data.has("full_name")) {
            parsed.setFullName(data.optString("full_name"));
        }
    }

    private void rearrangeId(final JSONObject data) throws JSONException {
        try {
            Integer.parseInt(data.getString(FolderFields.ID));
        } catch (final NumberFormatException x) {
            final String id = data.getString(FolderFields.ID);
            data.remove(FolderFields.ID);
            data.put("full_name", id);
        }
    }
}
