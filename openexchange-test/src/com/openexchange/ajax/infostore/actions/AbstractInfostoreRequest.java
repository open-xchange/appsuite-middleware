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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.file.storage.json.actions.files.TestFriendlyInfostoreRequest;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractInfostoreRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    private boolean failOnError;

    public static final String INFOSTORE_URL = "/ajax/infostore";

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public String getServletPath() {
        return INFOSTORE_URL;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public JSONObject writeJSON(File data) throws JSONException {
        return convertToJSON(data, null);
    }

    public JSONObject writeJSON(File data, Field[] fields) throws JSONException {
        return convertToJSON(data,fields);
    }

    public static JSONObject convertToJSON(File data, Field[] fields) throws JSONException{
        FileMetadataWriter writer = new com.openexchange.file.storage.json.FileMetadataWriter(null);
        if (fields == null) {
            return writer.write(new TestFriendlyInfostoreRequest("UTC"), data);
        }

        return writer.writeSpecific(new TestFriendlyInfostoreRequest("UTC"), data, fields, null);
    }

    public JSONArray writeFolderAndIDList(List<String> ids, List<String> folders) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0, length = ids.size(); i < length; i++) {
            JSONObject tuple = new JSONObject();
            tuple.put(AJAXServlet.PARAMETER_ID, ids.get(i));
            tuple.put(AJAXServlet.PARAMETER_FOLDERID, folders.get(i));
            array.put(tuple);
        }
        return array;
    }
}
