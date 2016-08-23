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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.advertisement.actions.GetConfigRequest;
import com.openexchange.ajax.advertisement.actions.GetConfigResponse;
import com.openexchange.ajax.advertisement.actions.SetConfigRequest;
import com.openexchange.ajax.advertisement.actions.SetConfigResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractConfigAwareAjaxSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;

/**
 * {@link AdvertisementTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
@RunWith(Parameterized.class)
public class AdvertisementTest extends AbstractConfigAwareAjaxSession {

    private String taxonomyTypes = "groupware_premium";
    private static final String BASIC_AUTH_LOGIN = "rest";
    private static final String BASIC_AUTH_PASSWORD = "secret1";
    private Context old;
    private static final String reloadables = "AdvertisementPackageServiceImpl";
    private static final String DEFAULT = "default";

    @Parameter(value = 0)
    public String packageScheme;

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
            result.put("com.openexchange.advertisement.taxonomy.types", taxonomyTypes);
        }
        return result;
    }

    @Before
    public void before() throws Exception {
        setUp();
        setUpConfiguration(client, false);

        switch (packageScheme) {
            case "Global":
                //nothing to do
                break;
            case "AccessCombinations":
                //nothing to do
                break;
            case "TaxonomyTypes":
                // Add taxonomy types
                Context ctx = new Context(client.getValues().getContextId());
                ctx.setUserAttribute("taxonomy", "types", taxonomyTypes);
                Credentials credentials = new Credentials(AJAXConfig.getProperty(Property.OX_ADMIN_MASTER), AJAXConfig.getProperty(Property.OX_ADMIN_MASTER_PWD));
                OXContextInterface ctxInterface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
                old = ctxInterface.getData(ctx, credentials);
                ctxInterface.change(ctx, credentials);
                break;
            default:
                fail("Unknown package scheme.");
        }
    }

    @After
    public void after() throws Exception {

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
                    Credentials credentials = new Credentials(AJAXConfig.getProperty(Property.OX_ADMIN_MASTER), AJAXConfig.getProperty(Property.OX_ADMIN_MASTER_PWD));
                    OXContextInterface ctxInterface = (OXContextInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXContextInterface.RMI_NAME);
                    ctxInterface.change(old, credentials);
                }
                break;
        }
    }

    @Test
    public void configByUserIdTest() throws OXException, IOException, JSONException {
        String data = "{\"data\":\"Preview\"}";
        try{
            SetConfigRequest set = SetConfigRequest.createPreview(  null, 
                                                                    String.valueOf(client.getValues().getUserId()), 
                                                                    String.valueOf(client.getValues().getContextId()), 
                                                                    data, 
                                                                    BASIC_AUTH_LOGIN, 
                                                                    BASIC_AUTH_PASSWORD);
            SetConfigResponse setResponse = client.execute(set);
            assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());
            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = client.execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(data));

            AJAXClient client2 = new AJAXClient(User.User2);
            GetConfigResponse response2 = client2.execute(req);
            assertTrue("Expecting a response with an error.", response2.hasError());
        } finally {
            SetConfigRequest set = SetConfigRequest.createPreview(  null, 
                String.valueOf(client.getValues().getUserId()), 
                String.valueOf(client.getValues().getContextId()), 
                null, 
                BASIC_AUTH_LOGIN, 
                BASIC_AUTH_PASSWORD);
            client.execute(set);
        }
    }
    
    @Test
    public void configByResellerAndPackageTest() throws OXException, IOException, JSONException {
        String data = "{\"data\":\"Package\"}";
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
        try{
            SetConfigRequest set = SetConfigRequest.create(DEFAULT, pack, data, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            SetConfigResponse setResponse = client.execute(set);
            assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());
            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = client.execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(data));
        } finally {
            SetConfigRequest set = SetConfigRequest.create(DEFAULT, pack, null, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            client.execute(set);
        }
    }

    @Test
    public void removeConfigTest() throws OXException, IOException, JSONException {
        String data = "{\"data\":\"Delete\"}";
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
        // create configuration
        SetConfigRequest set = SetConfigRequest.create(DEFAULT, pack, data, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
        SetConfigResponse setResponse = client.execute(set);
        assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());
        // Check if configuration is available
        GetConfigRequest req = new GetConfigRequest();
        GetConfigResponse response = client.execute(req);
        assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
        assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(data));
        // Remove configuration again
        set = SetConfigRequest.create(DEFAULT, pack, null, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
        client.execute(set);
        // Check if configuration is gone
        GetConfigResponse response2 = client.execute(req);
        assertTrue("Expecting a response with an error.", response2.hasError());
    }

    @Test
    public void removePreviewTest() throws OXException, IOException, JSONException {

        String data = "{\"data\":\"Fallback\"}";
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
        try {
            SetConfigRequest set = SetConfigRequest.create(DEFAULT, pack, data, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            SetConfigResponse setResponse = client.execute(set);
            assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());
            GetConfigRequest req = new GetConfigRequest();
            GetConfigResponse response = client.execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(data));

            // Create Preview 
            String dataPreview = "{\"data\":\"Preview\"}";
            set = SetConfigRequest.createPreview(null, String.valueOf(client.getValues().getUserId()), String.valueOf(client.getValues().getContextId()), dataPreview, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            setResponse = client.execute(set);
            assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());

            // Check if preview configuration is available
            req = new GetConfigRequest();
            response = client.execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(dataPreview));

            // Remove preview configuration
            set = SetConfigRequest.createPreview(null, String.valueOf(client.getValues().getUserId()), String.valueOf(client.getValues().getContextId()), null, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            setResponse = client.execute(set);
            assertTrue("Setting failed: " + setResponse.getErrorMessage(), !setResponse.hasError());

            // Check if configuration is back to the old one again
            response = client.execute(req);
            assertTrue("Response has errors: " + response.getErrorMessage(), !response.hasError());
            assertTrue("The server returned the wrong configuration.", response.getData().toString().equals(data));

        } finally {
            SetConfigRequest set = SetConfigRequest.create(DEFAULT, pack, null, BASIC_AUTH_LOGIN, BASIC_AUTH_PASSWORD);
            client.execute(set);
        }
    }

    @Override
    protected String getReloadables() {
        return reloadables;
    }
}
