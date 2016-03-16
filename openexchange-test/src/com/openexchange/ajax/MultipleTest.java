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

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.URLParameter;

public class MultipleTest extends AbstractAJAXTest {

    public MultipleTest(final String name) {
        super(name);
    }

    private static final String MULTIPLE_URL = "/ajax/multiple";

    public void testMultiple() throws Exception {
        final int folderId = FolderTest.getStandardContactFolder(getWebConversation(), getHostName(), getSessionId()).getObjectID();

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(Multiple.MODULE, Multiple.MODULE_CONTACT);
        jsonObj.put(Multiple.PARAMETER_ACTION, Multiple.ACTION_NEW);

        Contact contactObj = new Contact();
        contactObj.setSurName("testMultiple1");
        contactObj.setParentFolderID(folderId);

        JSONObject jsonDataObj = new JSONObject();
        ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
        contactWriter.writeContact(contactObj, jsonDataObj, null);

        jsonObj.put("data", jsonDataObj);
        jsonArray.put(jsonObj);

        jsonObj = new JSONObject();
        jsonObj.put(Multiple.MODULE, Multiple.MODULE_CONTACT);
        jsonObj.put(Multiple.PARAMETER_ACTION, Multiple.ACTION_NEW);

        contactObj = new Contact();
        contactObj.setSurName("testMultiple2");
        contactObj.setParentFolderID(folderId);

        jsonDataObj = new JSONObject();
        contactWriter = new ContactWriter(TimeZone.getDefault());
        contactWriter.writeContact(contactObj, jsonDataObj, null);

        jsonObj.put("data", jsonDataObj);
        jsonArray.put(jsonObj);

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + getHostName() + MULTIPLE_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = getWebConversation().getResponse(req);

        assertEquals(200, resp.getResponseCode());
        jsonArray = new JSONArray(resp.getText());
        for (int a = 0; a < jsonArray.length(); a++) {
            final Response response = Response.parse(jsonArray.getJSONObject(a).toString());

            if (response.hasError()) {
                fail("json error: " + response.getErrorMessage());
            }
        }
    }
}
