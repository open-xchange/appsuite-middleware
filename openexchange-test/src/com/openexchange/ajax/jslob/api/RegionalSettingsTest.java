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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.jslob.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.ConfigBody;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.JSlobData;
import com.openexchange.testing.httpclient.models.JSlobsResponse;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.JSlobApi;

/**
 * {@link RegionalSettingsTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class RegionalSettingsTest extends AbstractAPIClientSession {

    private static final String ID = "localeData";
    private static final String CORE = "io.ox/core";
    private JSlobApi jslobApi;
    private ConfigApi configApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        jslobApi = new JSlobApi(getApiClient());
        configApi = new ConfigApi(getApiClient());
    }

    @Test
    public void testCustomRegionalSettingsViaJSLob() throws ApiException {
        // Get jslob and check that user has no custom region format
        Map<String, Object> map = getCoreJSLob();
        assertFalse(map.containsKey(ID));

        // Adjust regional settings
        HashMap<String, Object> custom = new HashMap<>();
        custom.put("number", "1 234,56");
        CommonResponse response = jslobApi.setJSlob(getSessionId(), Collections.singletonMap(ID, custom), CORE, null);
        assertNull(response.getErrorDesc(), response.getError());

        // Check again
        map = getCoreJSLob();
        assertTrue("Couldn't find region information", map.containsKey(ID));
        if (map.get(ID) instanceof Map == false) {
            fail("Unknown format");
        }
        @SuppressWarnings("unchecked") Map<String, Object> region_settings = (Map<String, Object>) map.get(ID);
        assertTrue(region_settings.containsKey("number"));

        // Remove entry by setting region_format to a simple string (NULL doesn't work, because of the api client)
        response = jslobApi.setJSlob(getSessionId(), Collections.singletonMap(ID, "NOT_NULL"), CORE, null);
        assertNull(response.getErrorDesc(), response.getError());

        // Get jslob and check that user has no custom region format
        map = getCoreJSLob();
        assertFalse(map.containsKey(ID));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCoreJSLob() throws ApiException {
        JSlobsResponse resp = jslobApi.getJSlobList(getSessionId(), Collections.singletonList(CORE), null);
        assertNull(resp.getErrorDesc(), resp.getError());
        assertNotNull("", resp.getData());
        List<JSlobData> list = resp.getData();
        assertEquals(1, list.size());
        JSlobData jSlobData = list.get(0);
        if (jSlobData.getTree() instanceof Map) {
            return (Map<String, Object>) jSlobData.getTree();
        }
        fail("Unable to parse result");
        return null;
    }

    @Test
    public void testCustomRegionalSettingsViaConfigTree() throws ApiException, JSONException {
        // Remove settings first
        putRegionSettingToConfigTree(null);
        // Get config tree entry and check that user has no custom region format
        getSettingsFromConfigTree(false);
        // Adjust regional settings
        HashMap<String, Object> custom = new HashMap<>();
        custom.put("number", "1 234,56");
        putRegionSettingToConfigTree(custom);
        // Check again
        Map<String, Object> region_settings = getSettingsFromConfigTree(true);
        assertTrue(region_settings.containsKey("number"));
        // Remove entry again
        putRegionSettingToConfigTree(null);
        // Check again
        getSettingsFromConfigTree(false);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getSettingsFromConfigTree(boolean exists) throws ApiException {
        ConfigResponse resp = configApi.getConfigNode(ID, getSessionId());
        assertNull(resp.getErrorDesc(), resp.getError());
        String data = resp.getData().toString();
        if (exists == false) {
            assertTrue("Unexpectedly found custom settings", JSONObject.NULL.toString().equals(data));
            return null;
        }
        assertFalse("Missing custom settings", JSONObject.NULL.toString().equals(data));
        return (Map<String, Object>) resp.getData();
    }

    private void putRegionSettingToConfigTree(Map<String, Object> settings) throws ApiException, JSONException {
        if (settings == null) {
            // use simple string instead of null, because of bad test client support
            CommonResponse resp = configApi.putConfigNode(ID, getSessionId(), new ConfigBody().data("DELETE"));
            assertNull(resp.getErrorDesc(), resp.getError());
            return;
        }
        JSONObject json = new JSONObject();
        for (Entry<String, Object> entry : settings.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }

        CommonResponse resp = configApi.putConfigNode(ID, getSessionId(), new ConfigBody().data(json.toString()));
        assertNull(resp.getErrorDesc(), resp.getError());
    }

}
