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

package com.openexchange.ajax.contact;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.TimeZone;
import org.json.JSONObject;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.URLParameter;

/**
 * Tests if bug 6335 appears again in tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug6335Test extends ContactTest {

    /**
     * @param name
     */
    public Bug6335Test(final String name) {
        super(name);
    }


	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testBug6335() throws Exception {

		final Contact contactObj = new Contact();
		contactObj.setSurName("\u001f");
		contactObj.setParentFolderID(contactFolderId);

		//final int objectId = insertContact(getWebConversation(), contactObj, getHostName(), getSessionId());
		final WebConversation webCon = getWebConversation();
		String host = getHostName();
		host = appendPrefix(host);

		final StringWriter stringWriter = new StringWriter();
		final JSONObject jsonObj = new JSONObject();
		final ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
		contactWriter.writeContact(contactObj, jsonObj, null);

		stringWriter.write(jsonObj.toString());
		stringWriter.flush();

		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, getSessionId());
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

		WebRequest req = null;
		WebResponse resp = null;

		JSONObject jResponse = null;

		final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes(com.openexchange.java.Charsets.UTF_8));

		req = new PutMethodWebRequest(host + CONTACT_URL + parameter.getURLParameters(), bais, "text/javascript");
		resp = webCon.getResponse(req);

		jResponse = new JSONObject(resp.getText());

		assertEquals(200, resp.getResponseCode());

		final Response response = Response.parse(jResponse.toString());


        assertTrue("Invalid character was not detected.", response.hasError());
        //final OXException.Code code = OXException.Code.INVALID_DATA;
        final OXException exc = response.getException();
        assertEquals("Wrong exception message.", Category.CATEGORY_USER_INPUT, exc.getCategory());
        assertEquals("Wrong exception message.", 168, exc.getCode());
	}
}
