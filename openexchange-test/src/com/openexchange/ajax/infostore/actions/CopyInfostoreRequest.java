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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.file.storage.File;

/**
 * {@link CopyInfostoreRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class CopyInfostoreRequest extends AbstractInfostoreRequest<CopyInfostoreResponse> {

    private String id;
    private String folderId;
    private com.openexchange.file.storage.File metadata;
    private String version;

    public CopyInfostoreRequest(String id, String folderId, File file) {
        this(id, folderId, file, null);
    }
    
    public CopyInfostoreRequest(String id, String folderId, File file, String version) {
        super();
        this.id = id;
        this.folderId = folderId;
        this.metadata = file;
        this.version = version;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(4);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_COPY));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, new Date()));
        if (version != null) {
            tmp.add(new Parameter(AJAXServlet.PARAMETER_VERSION, this.version));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends CopyInfostoreResponse> getParser() {
        return new CopyInfostoreParser(getFailOnError());
    }

    @Override
    public String getBody() throws JSONException {
        JSONObject jFile = prepareJFile();
        return jFile.toString();
    }

    public void setMetadata(com.openexchange.file.storage.File metadata) {
        this.metadata = metadata;
    }

    public com.openexchange.file.storage.File getMetadata() {
        return metadata;
    }

    private JSONObject prepareJFile() throws JSONException {
        final JSONObject originalObject = new JSONObject(writeJSON(getMetadata()));
        final JSONObject retVal = new JSONObject();
        final Set<String> set = originalObject.keySet();

        for (String string : set) {
            final Object test = originalObject.get(string);
            if (test != JSONObject.NULL) {
                if (test instanceof JSONArray) {
                    if (((JSONArray) test).length() > 0) {
                        retVal.put(string, test);
                    }
                } else {
                    retVal.put(string, test);
                }
            }
        }
        return retVal;
    }

}
