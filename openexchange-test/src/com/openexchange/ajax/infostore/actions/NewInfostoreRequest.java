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

package com.openexchange.ajax.infostore.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class NewInfostoreRequest extends AbstractInfostoreRequest<NewInfostoreResponse> {

    private DocumentMetadata metadata;
    private final InputStream input;

    public NewInfostoreRequest() {
        this(null, (InputStream) null);
    }

    public NewInfostoreRequest(DocumentMetadata data) {
        this(data, (InputStream) null);
    }

    public NewInfostoreRequest(DocumentMetadata data, File upload) throws FileNotFoundException {
        this(data, new FileInputStream(upload));
    }

    public NewInfostoreRequest(DocumentMetadata data, InputStream input) {
        super();
        this.metadata = data;
        this.input = input;
    }

    public void setMetadata(DocumentMetadata metadata) {
        this.metadata = metadata;
    }

    public DocumentMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String getBody() throws JSONException {
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

        return retVal.toString();
    }

    @Override
    public Method getMethod() {
        return null == input ? Method.PUT : Method.UPLOAD;
    }

    @Override
    public Parameter[] getParameters() throws JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(3);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW));
        if (null != input) {
            tmp.add(new FieldParameter("json", getBody()));
            tmp.add(new FileParameter("file", metadata.getFileName(), input, metadata.getFileMIMEType()));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public NewInfostoreParser getParser() {
        return new NewInfostoreParser(getFailOnError(), null != input);
    }
}
