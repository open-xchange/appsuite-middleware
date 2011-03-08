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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.ajax.folder;

import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link FunctionTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class FunctionTests extends AbstractAJAXSession {

    private AJAXClient client;

    public FunctionTests(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testUnknownAction() throws IOException, JSONException, AjaxException {
        GetResponse response = client.execute(new UnknownActionRequest(API.OX_OLD, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, false));
        assertTrue("JSON response should contain an error message.", response.hasError());
        AbstractOXException exception = response.getException();
        String error = exception.getOrigMessage();
        assertTrue(
            "Error is not the expected one: \"" + error + "\"",
            error.equals("Action \"unknown\" NOT supported via GET on /ajax/folders") || error.equals("Unknown AJAX action: %s."));
    }

    private class UnknownActionRequest extends GetRequest {

        UnknownActionRequest(API api, int folderId, boolean failOnError) {
            super(api, folderId, failOnError);
        }

        @Override
        protected void addParameters(List<Parameter> params) {
            params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "unknown"));
            params.add(new Parameter(AJAXServlet.PARAMETER_ID, getFolderIdentifier()));
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, getColumns()));
        }
    }
}
