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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

/**
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class UpdateInfostoreRequest extends AbstractInfostoreRequest<UpdateInfostoreResponse> {

    private DocumentMetadata metadata;
    private File upload;
    private Metadata[] fields;
    private final int id;
    private final Date lastModified;

    public UpdateInfostoreRequest(int id, Date lastModified, File upload) {
        this.id = id;
        this.upload = upload;
        this.lastModified = lastModified;
    }

    public UpdateInfostoreRequest(DocumentMetadata data, Metadata[] fields, File upload, Date lastModified) {
        this.metadata = data;
        this.id = data.getId();
        this.lastModified = lastModified;
        this.upload = upload;
        this.fields = fields;
    }

    public UpdateInfostoreRequest(DocumentMetadata data, Metadata[] fields, Date lastModified) {
        this.metadata = data;
        this.id = data.getId();
        this.lastModified = lastModified;
        this.fields = fields;
    }

    public void setMetadata(DocumentMetadata metadata) {
        this.metadata = metadata;
    }

    public DocumentMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String getBody() throws JSONException {
        return writeJSON(getMetadata(), fields);
    }

    @Override
    public Method getMethod() {
        return null == upload ? Method.PUT : Method.UPLOAD;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> tmp = new ArrayList<Parameter>(3);
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_ID, id));
        tmp.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified));
        if (null != upload) {
            tmp.add(new FieldParameter("json", getBody()));
            tmp.add(new FileParameter("file", upload.getName(), new FileInputStream(upload), metadata.getFileMIMEType()));
        }
        return tmp.toArray(new Parameter[tmp.size()]);
    }

    @Override
    public UpdateInfostoreParser getParser() {
        return new UpdateInfostoreParser(getFailOnError(), null != upload);
    }
}
