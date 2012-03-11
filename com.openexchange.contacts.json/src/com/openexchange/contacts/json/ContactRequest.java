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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.json;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactRequest {

    private final AJAXRequestData request;

    private final ServerSession session;

    public ContactRequest(final AJAXRequestData request, final ServerSession session) {
        super();
        this.request = request;
        this.session = session;
    }

    public int getId() throws OXException {
    	if (request.isSet("id")) {
            return request.getParameter("id", int.class);
    	}
    	return session.getUser().getContactId();
    }

    public int getFolder() throws OXException {
        return request.getParameter("folder", int.class);
    }

    public TimeZone getTimeZone() {
        final String timezone = request.getParameter("timezone");
        if (timezone == null) {
            return TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
        } else {
            return TimeZoneUtils.getTimeZone(timezone);
        }
    }

    public ServerSession getSession() {
        return session;
    }

    public int[] getColumns() throws OXException {
        return checkOrInsertLastModified(removeVirtual(RequestTools.getColumnsAsIntArray(request, "columns")));
    }

    public int getSort() throws OXException {
        return RequestTools.getNullableIntParameter(request, "sort");
    }

    public Order getOrder() {
        return OrderFields.parse(request.getParameter("order"));
    }

    public String getCollation() {
        return request.getParameter("collation");
    }

    public int getLeftHandLimit() throws OXException {
        return RequestTools.getNullableIntParameter(request, "left_hand_limit");
    }

    public int getRightHandLimit() throws OXException {
        return RequestTools.getNullableIntParameter(request, "right_hand_limit");
    }

    public int[][] getListRequestData() throws OXException {
        final JSONArray data = (JSONArray) request.getData();
        if (data == null) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create("data");
        }

        return RequestTools.buildObjectIdAndFolderId(data);
    }

    public boolean containsImage() {
        return request.hasUploads();
    }

    public JSONObject getContactJSON(final boolean isUpload) throws OXException {
        if (isUpload) {
            final String jsonField = request.getUploadEvent().getFormField("json");
            try {
                return new JSONObject(jsonField);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, jsonField);
            }
        } else {
            return (JSONObject) request.getData();
        }
    }

    public UploadEvent getUploadEvent() {
        return request.getUploadEvent();
    }

    public int[] getDeleteRequestData() throws OXException {
        final JSONObject json = (JSONObject) request.getData();
        final int[] data = new int[2];
        try {
            data[0] = json.getInt("id");
            data[1] = json.getInt("folder");
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }

        return data;
    }

    public long getTimestamp() throws OXException {
        return request.getParameter("timestamp", long.class);
    }

    public int[] getUserIds() throws OXException {
        final JSONArray json = (JSONArray) request.getData();
        final int userIdArray[] = new int[json.length()];
        for (int i = 0; i < userIdArray.length; i++) {
            try {
                userIdArray[i] = json.getInt(i);
            } catch (final JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
            }
        }

        return userIdArray;
    }

    public int getFolderFromJSON() throws OXException {
        final JSONObject json = (JSONObject) request.getData();
        try {
            return json.getInt("folder_id");
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e, json);
        }
    }

    public Object getData() {
        return request.getData();
    }

    private int[] removeVirtual(final int[] columns) {
        final TIntList helper = new TIntArrayList(columns.length);
        for (final int col : columns) {
            if (col == Contact.LAST_MODIFIED_UTC) {
                // SKIP
            } else if (col == Contact.IMAGE1_URL) {
                helper.add(Contact.IMAGE1);
            } else {
                helper.add(col);
            }

        }
        return helper.toArray();
    }

    private int[] checkOrInsertLastModified(final int[] columns) {
        if (!Arrays.contains(columns, Contact.LAST_MODIFIED)) {
            final int[] newColumns = new int[columns.length + 1];
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
            newColumns[columns.length] = Contact.LAST_MODIFIED;

            return newColumns;
        } else {
            return columns;
        }
    }

    public String getIgnore() {
        return request.getParameter("ignore");
    }

}
