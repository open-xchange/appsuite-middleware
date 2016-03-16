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

package com.openexchange.ajax.mail.netsol.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.mail.FolderAndID;
import com.openexchange.ajax.mail.actions.AbstractMailRequest;

/**
 * {@link NetsolDeleteRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class NetsolDeleteRequest implements AJAXRequest<NetsolDeleteRequest.NetsolDeleteResponse> {

    private final FolderAndID[] mailPaths;

    private final boolean hardDelete;

    /**
     * Initializes a new {@link NetsolDeleteRequest}
     *
     * @param mailPaths
     *            The mail paths of messages to delete
     */
    public NetsolDeleteRequest(final FolderAndID[] mailPaths, final boolean hardDelete) {
        super();
        this.mailPaths = mailPaths;
        this.hardDelete = hardDelete;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < mailPaths.length; i++) {
            final JSONObject jo = new JSONObject();
            jo.put(AJAXServlet.PARAMETER_FOLDERID, mailPaths[i].folderId);
            jo.put(AJAXServlet.PARAMETER_ID, mailPaths[i].id);
            array.put(jo);
        }
        return array;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE),
                new Parameter("harddelete", hardDelete ? "1" : "0") };
    }

    @Override
    public DeleteParser getParser() {
        return new DeleteParser(true);
    }

    @Override
    public String getServletPath() {
        return AbstractMailRequest.MAIL_URL;
    }

    final static class DeleteParser extends AbstractAJAXParser<NetsolDeleteResponse> {

        /**
         * Initializes a new {@link DeleteParser}
         *
         * @param failOnError
         *            <code>true</code> if fail on error; otherwise
         *            <code>false</code>
         */
        DeleteParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected NetsolDeleteResponse createResponse(final Response response) {
            return new NetsolDeleteResponse(response);
        }

    }

    public final static class NetsolDeleteResponse extends AbstractAJAXResponse {

        /**
         * Initializes a new {@link NetsolDeleteResponse}
         *
         * @param response
         *            The response
         */
        public NetsolDeleteResponse(final Response response) {
            super(response);
        }

        /**
         * @return JSON array containing failed
         */
        public JSONArray getFailed() {
            return (JSONArray) getData();
        }
    }
}
