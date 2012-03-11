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

package com.openexchange.ajax.jslob;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.jslob.actions.AllJSlobRequest;
import com.openexchange.ajax.jslob.actions.AllJSlobResponse;
import com.openexchange.ajax.jslob.actions.GetJSlobRequest;
import com.openexchange.ajax.jslob.actions.GetJSlobResponse;
import com.openexchange.ajax.jslob.actions.SetJSlobRequest;
import com.openexchange.ajax.jslob.actions.UpdateJSlobRequest;
import com.openexchange.ajax.jslob.actions.UpdateJSlobResponse;
import com.openexchange.jslob.JSlob;
import com.openexchange.test.json.JSONAssertion;

/**
 * {@link JSlobTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobTest extends AbstractAJAXSession {

    /**
     * Initializes a new {@link JSlobTest}.
     */
    public JSlobTest() {
        super(JSlobTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleAllRequest() {
        try {
            final AllJSlobRequest request = new AllJSlobRequest();
            final AllJSlobResponse response = client.execute(request);

            final List<JSlob> list = response.getJSlobs();
            if (!list.isEmpty()) {
                for (final JSlob jSlob : list) {
                    assertNotNull(jSlob.getJsonObject());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSetRequest() {
        final String id = "test.id";
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("string", "A sample string.");
            jsonObject.put("array", new JSONArray("[12,34,56,78,90]"));
            jsonObject.put("object", new JSONObject("{value: 1234, other: \"A string\"}"));

            final SetJSlobRequest setRequest = new SetJSlobRequest().setId(id).setJslob(new JSlob(jsonObject));
            client.execute(setRequest);

            final GetJSlobRequest getRequest = new GetJSlobRequest().setId(id);
            final GetJSlobResponse getResponse = client.execute(getRequest);
            
            final JSlob jslob = getResponse.getJSlob();
            assertNotNull("JSlob is null.", jslob);
            
            final JSONObject jsonObject2 = jslob.getJsonObject();
            assertNotNull("JSON data is null.", jsonObject2);
            
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("string") && JSONAssertion.equals(jsonObject.get("string"), jsonObject2.get("string")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("array") && JSONAssertion.equals(jsonObject.get("array"), jsonObject2.get("array")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("object") && JSONAssertion.equals(jsonObject.get("object"), jsonObject2.get("object")));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                final SetJSlobRequest request = new SetJSlobRequest().setId(id).setJslob(null);
                client.execute(request);
            } catch (final Exception e) {
                System.err.println("Couldn't delete test JSlob");
                e.printStackTrace();
            }
        }
    }

    public void testUpdateRequest() {
        final String id = "test.id";
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("string", "A sample string.");
            jsonObject.put("array", new JSONArray("[12,34,56,78,90]"));
            jsonObject.put("object", new JSONObject("{value: 1234, other: \"A string\"}"));

            final SetJSlobRequest setRequest = new SetJSlobRequest().setId(id).setJslob(new JSlob(jsonObject));
            client.execute(setRequest);

            Object value = new JSONArray("[24,54,58,69,345]");
            jsonObject.put("array", value);
            UpdateJSlobRequest updateRequest = new UpdateJSlobRequest().setId(id).setPathAndValue("array", value);
            UpdateJSlobResponse updateResponse = client.execute(updateRequest);
            JSlob jslob = updateResponse.getJSlob();

            assertNotNull("JSlob is null.", jslob);
            JSONObject jsonObject2 = jslob.getJsonObject();
            assertNotNull("JSON data is null.", jsonObject2);
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("string") && JSONAssertion.equals(jsonObject.get("string"), jsonObject2.get("string")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("array") && JSONAssertion.equals(jsonObject.get("array"), jsonObject2.get("array")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("object") && JSONAssertion.equals(jsonObject.get("object"), jsonObject2.get("object")));

            value = Integer.valueOf(5678);
            jsonObject.put("object", new JSONObject("{value: "+value+", other: \"A string\"}"));
            updateRequest = new UpdateJSlobRequest().setId(id).setPathAndValue("object/value", value);
            updateResponse = client.execute(updateRequest);
            jslob = updateResponse.getJSlob();

            assertNotNull("JSlob is null.", jslob);
            jsonObject2 = jslob.getJsonObject();
            assertNotNull("JSON data is null.", jsonObject2);
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("string") && JSONAssertion.equals(jsonObject.get("string"), jsonObject2.get("string")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("array") && JSONAssertion.equals(jsonObject.get("array"), jsonObject2.get("array")));
            assertTrue("Retrieved JSON data is not equal to provided one.", jsonObject2.hasAndNotNull("object") && JSONAssertion.equals(jsonObject.get("object"), jsonObject2.get("object")));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                final SetJSlobRequest request = new SetJSlobRequest().setId(id).setJslob(null);
                client.execute(request);
            } catch (final Exception e) {
                System.err.println("Couldn't delete test JSlob");
                e.printStackTrace();
            }
        }
    }

}
