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
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.ajax.user.UserTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.URLParameter;

public class UserTest extends AbstractAJAXTest {

    public final static int[] CONTACT_FIELDS = {
        DataObject.OBJECT_ID,
        Contact.INTERNAL_USERID,
        Contact.EMAIL1,
        Contact.GIVEN_NAME,
        Contact.SUR_NAME,
        Contact.DISPLAY_NAME
    };

    public UserTest(final String name) {
        super(name);
    }

    private static final String USER_URL = "/ajax/contacts";

    public void testSearch() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getWebConversation(), getHostName(), "*", getSessionId());
        assertTrue("user array size > 0", users.length > 0);
    }

    public void testList() throws Exception {
        com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getWebConversation(), getHostName(), "*", getSessionId());
        assertTrue("user array size > 0", users.length > 0);

        final int[] id = new int[users.length];
        for (int a = 0; a < id.length; a++) {
            id[a] = users[a].getId();
        }

        users = listUser(getWebConversation(), id, PROTOCOL + getHostName(), getSessionId());
        assertTrue("user array size > 0", users.length > 0);
    }

    public void testSearchUsers() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getWebConversation(), getHostName(), "*", getSessionId());
        assertTrue("user array size > 0", users.length > 0);
    }

    public void testGet() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getWebConversation(), getHostName(), "*", getSessionId());
        assertTrue("user array size > 0", users.length > 0);
        loadUser(getWebConversation(), users[0].getId(), getHostName(), getSessionId());
    }

    public static UserImpl4Test[] listUser(final WebConversation webCon, final int[] id, final String host, final String session) throws Exception {

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);

        final StringBuffer stringBuffer = new StringBuffer();
        for (int a = 0; a < CONTACT_FIELDS.length; a++) {
            if (a > 0) {
                stringBuffer.append(',');
            }
            stringBuffer.append(CONTACT_FIELDS[a]);
        }

        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, stringBuffer.toString());

        final JSONArray requestArray = new JSONArray();
        for (int a = 0; a < id.length; a++) {
            final JSONObject jData = new JSONObject();
            jData.put(DataFields.ID, id[a]);
            jData.put(AJAXServlet.PARAMETER_FOLDERID, FolderObject.SYSTEM_LDAP_FOLDER_ID);
            requestArray.put(jData);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(requestArray.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(host + USER_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals(200, resp.getResponseCode());

        final Response response = ResponseParser.parse(resp.getText());

        if (response.hasError()) {
            fail("json error: " + response.getErrorMessage());
        }

        assertNotNull("timestamp", response.getTimestamp());

        final JSONArray jsonArray = (JSONArray)response.getData();
        final UserImpl4Test[] user = new UserImpl4Test[jsonArray.length()];
        for (int a = 0; a < user.length; a++) {
            final JSONArray jsonContactArray = jsonArray.getJSONArray(a);
            user[a] = new UserImpl4Test();
            user[a].setId(jsonContactArray.getInt(1));
            user[a].setMail(jsonContactArray.getString(2));
        }

        return user;
    }

    public static User loadUser(WebConversation webCon, int userId, String host, String session) throws OXException, IOException, SAXException, JSONException {
        return UserTools.getUser(webCon, host, session, userId);
    }
}
