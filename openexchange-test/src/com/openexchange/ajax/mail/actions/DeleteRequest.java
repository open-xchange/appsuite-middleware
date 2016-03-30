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

package com.openexchange.ajax.mail.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.TestMail;

/**
 * {@link DeleteRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteRequest extends AbstractMailRequest<DeleteResponse> {

    private final String[][] folderAndMailIds;

    private final boolean hardDelete;

    private boolean failOnError = true;

    public DeleteRequest(String folder, String id, boolean hardDelete) {
        this(new String[][] {{ folder, id }}, hardDelete);
    }

    public DeleteRequest(String[] folderAndMailId, boolean hardDelete) {
        this(new String[][] { folderAndMailId }, hardDelete);
    }

    public DeleteRequest(final String[][] folderAndMailIds) {
        this(folderAndMailIds, false);
    }

    public DeleteRequest(final String[][] folderAndMailIds, final boolean hardDelete) {
        this.folderAndMailIds = folderAndMailIds;
        this.hardDelete = hardDelete;
    }

    public DeleteRequest(final TestMail mail, final boolean hardDelete) {
        this.folderAndMailIds = new String[][] { mail.getFolderAndId() };
        this.hardDelete = hardDelete;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE), new Parameter("harddelete", hardDelete ? "1" : "0") };
    }

    @Override
    public AbstractAJAXParser<DeleteResponse> getParser() {
        return new AbstractAJAXParser<DeleteResponse>(failOnError) {

            @Override
            protected DeleteResponse createResponse(final Response response) {
                return new DeleteResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (final String[] folderAndObject : folderAndMailIds) {
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, folderAndObject[0]);
            json.put(DataFields.ID, folderAndObject[1]);
            array.put(json);
        }
        return array;
    }

    public DeleteRequest ignoreError() {
        failOnError = false;
        return this;
    }

    public DeleteRequest failOnError() {
        failOnError = true;
        return this;
    }
}
