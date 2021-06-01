/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.httpclient.HttpMethod;
import org.json.JSONObject;
import com.openexchange.test.common.test.json.JSONAssertion;

/**
 * Yet another OX testing framework. This one is for simple cases, when the full blown framework would be too much.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractSimpleClientTest {

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
        super();
    }

    public SimpleOXClient createClient() throws Exception {
        Properties properties = getAJAXProperties();
        String host = properties.getProperty("hostname");
        boolean secure = "https".equalsIgnoreCase(properties.getProperty("protocol"));
        return currentClient = new SimpleOXClient(host, secure);
    }

    public SimpleOXClient as(String user) throws Exception {
        if (authenticatedClients.containsKey(user)) {
            currentClient = authenticatedClients.get(user);
        } else {
            createClient();
            String[] credentials = credentials(user);
            currentClient.login(credentials[0], credentials[1]);
            authenticatedClients.put(user, currentClient);
        }
        return currentClient;
    }

    public SimpleOXClient asUser(String user) throws Exception {
        return as(user);
    }

    public JSONObject raw(String action, Object... parameters) throws Exception {
        return rawResponse = currentModule.raw(action, parameters);
    }

    public JSONObject rawGeneral(String module, String action, Object... parameters) throws Exception {
        return rawResponse = currentClient.raw(module, action, parameters);
    }

    public HttpMethod rawMethod(String module, String action, Object... parameters) throws Exception {
        return currentClient.rawMethod(module, action, parameters);
    }

    public SimpleResponse call(String action, Object... parameters) throws Exception {
        return lastResponse = currentModule.call(action, parameters);
    }

    public SimpleResponse callGeneral(String module, String action, Object... parameters) throws Exception {
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
        return new String[] { properties.getProperty(user) + "@" + properties.getProperty("contextName"), properties.getProperty("password")
        };
    }

    protected Properties getAJAXProperties() throws Exception {
        if (ajaxProperties != null) {
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
