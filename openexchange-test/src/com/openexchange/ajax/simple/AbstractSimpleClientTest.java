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

package com.openexchange.ajax.simple;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethod;
import org.json.JSONObject;
import com.openexchange.test.json.JSONAssertion;


/**
 * Yet another OX testing framework. This one is for simple cases, when the full blown framework would be too much.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractSimpleClientTest extends TestCase {

    protected static final String USER1 = "login";
    protected static final String USER2 = "seconduser";
    protected static final String USER3 = "thirdlogin";
    protected static final String USER4 = "fourthlogin";

    protected Properties ajaxProperties;

    protected SimpleOXClient currentClient;
    protected SimpleOXModule currentModule;
    protected SimpleResponse lastResponse;


    private final Map<String, SimpleOXClient> authenticatedClients = new HashMap<String, SimpleOXClient>();
    protected JSONObject rawResponse;

    public AbstractSimpleClientTest() {

    }

    public AbstractSimpleClientTest(String name) {
        super(name);
    }

    public SimpleOXClient createClient() throws Exception{
        Properties properties = getAJAXProperties();
        String host = properties.getProperty("hostname");
        boolean secure = "https".equalsIgnoreCase(properties.getProperty("protocol"));
        return currentClient = new SimpleOXClient(host, secure);
    }

    public SimpleOXClient as(String user) throws Exception {
        if(authenticatedClients.containsKey(user)) {
            currentClient = authenticatedClients.get(user);
        } else {
            createClient();
            String[] credentials = credentials(user);
            currentClient.login(credentials[0], credentials[1]);
            authenticatedClients.put(user, currentClient);
        }
        return currentClient;
    }

    public SimpleOXClient asUser(String user) throws Exception{
        return as(user);
    }

    public JSONObject raw(String action, Object...parameters) throws Exception {
        return rawResponse = currentModule.raw(action, parameters);
    }

    public JSONObject rawGeneral(String module, String action, Object...parameters) throws Exception {
        return rawResponse = currentClient.raw(module, action, parameters);
    }

    public HttpMethod rawMethod(String module, String action, Object...parameters) throws Exception {
        return currentClient.rawMethod(module, action, parameters);
    }

    public SimpleResponse call(String action, Object...parameters) throws Exception {
        return lastResponse = currentModule.call(action, parameters);
    }

    public SimpleResponse callGeneral(String module, String action, Object...parameters) throws Exception {
        return lastResponse = currentClient.call(module, action, parameters);
    }

    public SimpleOXModule module(String module) {
        return currentModule = currentClient.getModule(module);
    }

    public SimpleOXModule inModule(String module) {
        return module(module);
    }

    public static void assertNoError(SimpleResponse response) {
        assertFalse(response.getError(), response.hasError());
    }

    public void assertNoError() {
        assertNoError(lastResponse);
    }

    public static void assertError(SimpleResponse response) {
        assertTrue(response.hasError());
    }

    public void assertError() {
        assertError(lastResponse);
    }

    public Map<String, Object> details() {
        return lastResponse.getObjectData();
    }

    public List<List<Object>> list() {
        return lastResponse.getListData();
    }

    public void assertDataIs(Object expected) {
        assertEquals(expected, lastResponse.getData());
    }

    public String[] credentials(String user) throws Exception {
        Properties properties = getAJAXProperties();
        return new String[] {
            properties.getProperty(user) + "@" + properties.getProperty("contextName"),
            properties.getProperty("password")
        };
    }

    protected Properties getAJAXProperties() throws Exception {
        if(ajaxProperties != null) {
            return ajaxProperties;
        }
        String testPropFile = System.getProperty("test.propfile", "conf/test.properties");
        Properties testProperties = load(testPropFile);
        return ajaxProperties = load(testProperties.getProperty("ajaxPropertiesFile"));
    }

    private Properties load(String testPropFile) throws FileNotFoundException, IOException {
        InputStream is = null;
        try {
            Properties properties = new Properties();
            properties.load(is = new FileInputStream(testPropFile));
            return properties;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public void assertRaw(JSONAssertion assertion) {
        JSONAssertion.assertValidates(assertion, rawResponse);
    }

}
