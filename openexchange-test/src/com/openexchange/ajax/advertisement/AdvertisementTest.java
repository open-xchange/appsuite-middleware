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

package com.openexchange.ajax.advertisement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.URI;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.advertisement.actions.GetConfigRequest;
import com.openexchange.ajax.advertisement.actions.GetConfigResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractConfigAwareAjaxSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link AdvertisementTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
@RunWith(Parameterized.class)
public class AdvertisementTest extends AbstractConfigAwareAjaxSession {

    private String taxonomyTypes = "groupware_premium";
    private Context old;
    private static final String reloadables = "AdvertisementPackageServiceImpl";
    private static final String DEFAULT = "default";
    private Executor executor;
    private URI baseRestUri;

    @Parameter(value = 0)
    public String packageScheme;
    private static Random random;

    @Parameters(name = "packageScheme={0}")
    public static Iterable<? extends Object[]> params() {
        ArrayList<String[]> result = new ArrayList<>();
        result.add(new String[] { "Global" });
        result.add(new String[] { "AccessCombinations" });
        result.add(new String[] { "TaxonomyTypes" });
        return result;
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        if (packageScheme == null) {
            fail("No package scheme defined!");
        }
        Map<String, String> result = new HashMap<>();
        result.put("com.openexchange.advertisement.default.packageScheme", packageScheme);
        if (packageScheme == "TaxonomyTypes") {
            result.put("com.openexchange.advertisement.default.taxonomy.types", taxonomyTypes);
        }
        return result;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        random = new Random();
        setUpConfiguration(getClient(), false);

        switch (packageScheme) {
            case "Global":
                //nothing to do
                break;
            case "AccessCombinations":
                //nothing to do
                break;
            case "TaxonomyTypes":
                // Add taxonomy types
                Context ctx = new Context(getClient().getValues().getContextId());
                ctx.setUserAttribute("taxonomy", "types", taxonomyTypes);
                TestUser oxAdminMaster = TestContextPool.getOxAdminMaster();
                Credentials credentials = new Credentials(oxAdminMaster.getUser(), oxAdminMaster.getPassword());
                OXContextInterface ctxInterface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
                old = ctxInterface.getData(ctx, credentials);
                ctxInterface.change(ctx, credentials);
                break;
            default:
                fail("Unknown package scheme.");
        }

        TestUser restUser = TestContextPool.getRestUser();
        String protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
        String host = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        int port = 8009;

        executor = Executor.newInstance().auth(new HttpHost(host, port), restUser.getUser(), restUser.getPassword()).authPreemptive(new HttpHost(host, port));
        baseRestUri = new URIBuilder().setScheme(protocol).setHost(host).setPort(port).setPath("/advertisement/v1/config").build();
        LoggerFactory.getLogger(AdvertisementTest.class).info("Protocol: {}; host: {}, baseUri: {}, restCredentials: {}:{}", protocol, host, baseRestUri, restUser.getUser(), restUser.getPassword());
    }

    @After
    public void after() throws Exception {
        try {
            switch (packageScheme) {
                case "Global":
                    //nothing to do
                    break;
                case "AccessCombinations":
                    //nothing to do
                    break;
                case "TaxonomyTypes":
                    // Change to old taxonomy types
                    if (old != null) {
                        TestUser oxAdminMaster = TestContextPool.getOxAdminMaster();
                        Credentials credentials = new Credentials(oxAdminMaster.getUser(), oxAdminMaster.getPassword());
                        OXContextInterface ctxInterface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
                        ctxInterface.change(old, credentials);
                    }
                    break;
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void configByUserIdTest() throws Exception {
        URI restURI = restURI("user").setParameter("contextId", String.valueOf(getClient().getValues().getContextId())).setParameter("userId", String.valueOf(getClient().getValues().getUserId())).build();

        JSONValue adConfig = generateAdConfig();
        try {
            HttpResponse setResponse = executor.execute(Request.Put(restURI).body(new StringEntity(adConfig.toString(), ContentType.APPLICATION_JSON))).returnResponse();
            StatusLine statusLine = setResponse.getStatusLine();
            assertTrue("Unexpected status: " + statusLine.toString(), Arrays.contains(new int[] { 200, 201 }, statusLine.getStatusCode()));

            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = getClient().execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", adConfig.equals(response.getData()));

            AJAXClient client2 = new AJAXClient(testContext.acquireUser());
            GetConfigResponse response2 = client2.execute(req);
            assertTrue("Expecting a response with an error.", response2.hasError());
        } catch (Exception e) {
            LoggerFactory.getLogger(AdvertisementTest.class).error("Test failed with error: {}", e.getMessage(), e);
        } finally {
            HttpResponse response = executor.execute(Request.Delete(restURI)).returnResponse();
            assertEquals("Deletion of ad config failed: " + response.getStatusLine(), 204, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void configByResellerAndPackageTest() throws Exception {
        String pack = null;
        switch (packageScheme) {
            case "Global":
                pack = DEFAULT;
                break;
            case "AccessCombinations":
            case "TaxonomyTypes":
                pack = "groupware_premium";
                break;
        }

        URI restURI = restURI("package").setParameter("reseller", DEFAULT).setParameter("package", pack).build();

        JSONValue adConfig = generateAdConfig();
        try {
            HttpResponse setResponse = executor.execute(Request.Put(restURI).body(new StringEntity(adConfig.toString(), ContentType.APPLICATION_JSON))).returnResponse();
            StatusLine statusLine = setResponse.getStatusLine();
            assertTrue("Unexpected status: " + statusLine.toString(), Arrays.contains(new int[] { 200, 201 }, statusLine.getStatusCode()));

            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = getClient().execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", adConfig.equals(response.getData()));
        } catch (Exception e) {
            LoggerFactory.getLogger(AdvertisementTest.class).error("Test failed with error: {}", e.getMessage(), e);
        } finally {
            HttpResponse response = executor.execute(Request.Delete(restURI)).returnResponse();
            assertEquals("Deletion of ad config failed: " + response.getStatusLine(), 204, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void removeConfigTest() throws Exception {
        String pack = null;
        switch (packageScheme) {
            case "Global":
                pack = DEFAULT;
                break;
            case "AccessCombinations":
            case "TaxonomyTypes":
                pack = "groupware_premium";
                break;
        }

        URI restURI = restURI("package").setParameter("reseller", DEFAULT).setParameter("package", pack).build();

        JSONValue adConfig = generateAdConfig();

        // create configuration
        HttpResponse setResponse = executor.execute(Request.Put(restURI).body(new StringEntity(adConfig.toString(), ContentType.APPLICATION_JSON))).returnResponse();
        StatusLine statusLine = setResponse.getStatusLine();
        assertTrue("Unexpected status: " + statusLine.toString(), Arrays.contains(new int[] { 200, 201 }, statusLine.getStatusCode()));
        // Check if configuration is available
        GetConfigRequest req = new GetConfigRequest();
        GetConfigResponse response = getClient().execute(req);
        assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
        assertTrue("The server returned the wrong configuration.", adConfig.equals(response.getData()));
        // Remove configuration again
        HttpResponse deleteResponse = executor.execute(Request.Delete(restURI)).returnResponse();
        assertEquals("Deletion of ad config failed: " + deleteResponse.getStatusLine(), 204, deleteResponse.getStatusLine().getStatusCode());
        // Check if configuration is gone
        GetConfigResponse response2 = getClient().execute(req);
        assertTrue("Expecting a response with an error.", response2.hasError());
    }

    @Test
    public void removePreviewTest() throws Exception {
        String pack = null;
        switch (packageScheme) {
            case "Global":
                pack = DEFAULT;
                break;
            case "AccessCombinations":
            case "TaxonomyTypes":
                pack = "groupware_premium";
                break;
        }

        URI restURI = restURI("package").setParameter("reseller", DEFAULT).setParameter("package", pack).build();

        JSONValue adConfig = generateAdConfig();
        try {
            HttpResponse setResponse = executor.execute(Request.Put(restURI).body(new StringEntity(adConfig.toString(), ContentType.APPLICATION_JSON))).returnResponse();
            StatusLine statusLine = setResponse.getStatusLine();
            assertTrue("Unexpected status: " + statusLine.toString(), Arrays.contains(new int[] { 200, 201 }, statusLine.getStatusCode()));
            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = getClient().execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", adConfig.equals(response.getData()));

            // Create Preview
            JSONValue previewConfig = generateAdConfig();
            URI previewURI = restURI("user").setParameter("contextId", String.valueOf(getClient().getValues().getContextId())).setParameter("userId", String.valueOf(getClient().getValues().getUserId())).build();
            HttpResponse setPreviewResponse = executor.execute(Request.Put(previewURI).body(new StringEntity(adConfig.toString(), ContentType.APPLICATION_JSON))).returnResponse();
            statusLine = setPreviewResponse.getStatusLine();
            assertTrue("Unexpected status: " + statusLine.toString(), Arrays.contains(new int[] { 200, 201 }, statusLine.getStatusCode()));

            // Check if preview configuration is available
            req = new GetConfigRequest();
            response = getClient().execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", previewConfig.equals(response.getData()));

            // Remove preview configuration
            HttpResponse deletePreviewResponse = executor.execute(Request.Delete(previewURI)).returnResponse();
            assertEquals("Deletion of ad config failed: " + deletePreviewResponse.getStatusLine(), 204, deletePreviewResponse.getStatusLine().getStatusCode());

            // Check if configuration is back to the old one again
            response = getClient().execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", adConfig.equals(response.getData()));
        } catch (Exception e) {
            LoggerFactory.getLogger(AdvertisementTest.class).error("Test failed with error: {}", e.getMessage(), e);
        } finally {
            HttpResponse response = executor.execute(Request.Delete(restURI)).returnResponse();
            assertEquals("Deletion of ad config failed: " + response.getStatusLine(), 204, response.getStatusLine().getStatusCode());
        }
    }

    private static JSONValue generateAdConfig() throws JSONException {
        JSONArray config = new JSONArray();
        int num = 0;
        while (num == 0) {
            num = random.nextInt(5);
        }

        /*
         * {
         * "cooldown": 5000,
         * "gpt": {
         * "adUnitPath": "%adunit%"
         * },
         * "reloadAfter": 30000,
         * "space": "io.ox/ads/leaderboard"
         * }
         */
        long customerId = random.nextLong();
        for (int i = 0; i < num; i++) {
            JSONObject adSpace = new JSONObject();
            adSpace.put("cooldown", 5000);
            adSpace.put("reloadAfter", 30000);
            adSpace.put("space", "io.ox/ads/random_" + Integer.toString(random.nextInt()));
            JSONObject gpt = new JSONObject();
            gpt.put("adUnitPath", "/" + Long.toString(customerId) + "/RandomUnit_" + Integer.toString(random.nextInt()));
            adSpace.put("gpt", gpt);
        }

        return config;
    }

    private URIBuilder restURI(String pathSuffix) {
        return new URIBuilder(baseRestUri).setPath(baseRestUri.getPath() + '/' + pathSuffix);
    }

    @Override
    protected String getReloadables() {
        return reloadables;
    }
}
