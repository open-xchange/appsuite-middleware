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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.mail.filter.json.v2.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.mail.filter.json.v2.mapper.ActionCommandRuleFieldMapper;

/**
 * {@link Bug58952Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class Bug58952Test {

    private ActionCommandRuleFieldMapper mapper;

    /**
     * Initialises a new {@link Bug58952Test}.
     */
    public Bug58952Test() {
        super();
    }

    @Before
    public void setUp() {
        mapper = new ActionCommandRuleFieldMapper(null);
    }

    /**
     * Test the compatibility work-around, i.e. if two action commands exist (redirect and keep), check that
     * they are merged into redirect and the copy flag is set.
     */
    @Test
    public void testWithKeep() throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject redirect = new JSONObject();
        redirect.put("id", "redirect");
        redirect.put("to", "foobar@ox.io");

        JSONObject keep = new JSONObject();
        keep.put("id", "keep");
        jsonArray.put(redirect);
        // Mix 'em up
        for (int i = 0; i < 4; i++) {
            JSONObject command = new JSONObject();
            command.put("id", "someothercommand");
            jsonArray.put(command);
        }
        jsonArray.put(keep);

        int oldLength = jsonArray.length();
        invoke(ActionCommandRuleFieldMapper.class, "applyWorkaroundFor58952", JSONArray.class, mapper, jsonArray);

        assertTrue("Action commands did not merge", jsonArray.length() == oldLength - 1);
        JSONObject merged = jsonArray.getJSONObject(0);
        assertTrue("The copy flag is not set", merged.hasAndNotNull("copy"));
        assertTrue("The copy flag is not set", merged.optBoolean("copy", false));
    }

    /**
     * Tests that the copy flag is correctly parsed.
     */
    @Test
    public void testWithCopy() throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject redirect = new JSONObject();
        redirect.put("id", "redirect");
        redirect.put("to", "foobar@ox.io");
        redirect.put("copy", true);
        jsonArray.put(redirect);

        invoke(ActionCommandRuleFieldMapper.class, "applyWorkaroundFor58952", JSONArray.class, mapper, jsonArray);

        assertTrue("Action commands did not merge", jsonArray.length() == 1);
        JSONObject merged = jsonArray.getJSONObject(0);
        assertTrue("The copy flag is not set", merged.hasAndNotNull("copy"));
        assertTrue("The copy flag is not set", merged.optBoolean("copy", false));
    }

    /**
     * Tests that the redirect command is correctly parsed and not copy flag is present
     */
    @Test
    public void testRedirectOnly() throws Exception {
        JSONArray jsonArray = new JSONArray();
        JSONObject redirect = new JSONObject();
        redirect.put("id", "redirect");
        redirect.put("to", "foobar@ox.io");
        jsonArray.put(redirect);

        invoke(ActionCommandRuleFieldMapper.class, "applyWorkaroundFor58952", JSONArray.class, mapper, jsonArray);

        assertTrue("Action commands did not merge", jsonArray.length() == 1);
        JSONObject merged = jsonArray.getJSONObject(0);
        assertFalse("The copy flag is set", merged.hasAndNotNull("copy"));
    }

    /**
     * Invokes the specified method from the specified target class with the specified arguments
     * 
     * @param targetClass The target class
     * @param methodName The method name
     * @param argClasses The argument class
     * @param targetObject The target class instance
     * @param argObjects The arguments of the method to invoke
     * @return The result of the invoked method
     * @throws Exception if an error is occurred
     */
    private Object invoke(Class<?> targetClass, String methodName, Class<?> argClasses, Object targetObject, Object... argObjects) throws Exception {
        Method method = targetClass.getDeclaredMethod(methodName, argClasses);
        method.setAccessible(true);
        return method.invoke(targetObject, argObjects);
    }
}
