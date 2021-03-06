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

package com.openexchange.ajax.mail.authenticity;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.junit.Assert;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AuthenticationResult;
import com.openexchange.testing.httpclient.models.AuthenticationResult.StatusEnum;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MechanismResult;
import com.openexchange.testing.httpclient.modules.ImageApi;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link MailAuthenticityTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailAuthenticityTest extends AbstractConfigAwareAPIClientSession {

    private static final String FOLDER = "default0%2FINBOX";
    private static final String SUBFOLDER = "authenticity";
    private static final String PASS_ALL = "passAll.eml";
    private static final String PISHING = "pishing.eml";
    private static final String NONE = "none.eml";
    private static final String TRUSTED = "trusted.eml";
    private static final String[] MAIL_NAMES = new String[] { PASS_ALL, PISHING, NONE, TRUSTED };
    private MailApi api;
    private final Map<String, MailDestinationData> IMPORTED_EMAILS = new HashMap<>();
    private ImageApi imageApi;

    private static final String DKIM = "dkim";
    private static final String SPF = "spf";
    private static final String DMARC = "dmarc";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Setup configurations ----------------------------------
        // general config
        CONFIG.put(MailAuthenticityProperty.ENABLED.getFQPropertyName(), Boolean.TRUE.toString());
        CONFIG.put(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName(), "open-xchange.authenticity.test");

        // trusted domain config
        CONFIG.put("com.openexchange.mail.authenticity.trusted.config", "support@open-xchange.com:1");

        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR) + SUBFOLDER;
        String imgName = "ox.jpg";
        File f = new File(testMailDir, imgName);
        if (f.exists()) {
            CONFIG.put("com.openexchange.mail.authenticity.trusted.image.1", f.getAbsolutePath());
        }
        super.setUpConfiguration();

        // Setup client and import mails ------------------------
        api = new MailApi(getApiClient());
        imageApi = new ImageApi(getApiClient());

        for (String name : MAIL_NAMES) {
            f = new File(testMailDir, name);
            Assert.assertTrue(f.exists());
            MailImportResponse response = api.importMail(FOLDER, f, null, Boolean.TRUE);
            List<MailDestinationData> data = checkResponse(response);
            // data size should always be 1
            Assert.assertEquals(1, data.size());
            IMPORTED_EMAILS.put(name, data.get(0));
        }

    }

    @Test
    public void testBasicFunctionality() throws ApiException {
        /*
         * Test pass all
         */
        MailResponse resp = api.getMail(FOLDER, IMPORTED_EMAILS.get(PASS_ALL).getId(), null, I(0), null, Boolean.FALSE, null, null, null, null, null, null, null, null);
        MailData mail = checkResponse(resp);
        AuthenticationResult authenticationResult = mail.getAuthenticity();
        Assert.assertNotNull(authenticationResult);
        List<MechanismResult> mailAuthenticityMechanismResults = authenticationResult.getUnconsideredResults();
        Assert.assertEquals(3, mailAuthenticityMechanismResults.size());

        boolean spf = false, dmarc = false, dkim = false;
        for (MechanismResult result : mailAuthenticityMechanismResults) {
            switch (result.getMechanism()) {
                case DKIM:
                    dkim = true;
                    break;
                case DMARC:
                    dmarc = true;
                    break;
                case SPF:
                    spf = true;
                    break;
            }
            Assert.assertEquals("pass", result.getResult());
        }

        Assert.assertTrue(spf && dmarc && dkim);
        Assert.assertEquals(StatusEnum.PASS, authenticationResult.getStatus());

        /*
         * Test pishing
         */
        resp = api.getMail(FOLDER, IMPORTED_EMAILS.get(PISHING).getId(), null, I(0), null, Boolean.FALSE, null, null, null, null, null, null, null, null);
        mail = checkResponse(resp);
        authenticationResult = mail.getAuthenticity();
        Assert.assertNotNull(authenticationResult);
        mailAuthenticityMechanismResults = authenticationResult.getUnconsideredResults();
        Assert.assertFalse(mailAuthenticityMechanismResults.isEmpty());

        spf = false;
        dmarc = false;
        dkim = false;
        for (MechanismResult result : mailAuthenticityMechanismResults) {
            switch (result.getMechanism()) {
                case DKIM:
                    dkim = true;
                    break;
                case DMARC:
                    dmarc = true;
                    break;
                case SPF:
                    spf = true;
                    break;
            }
            Assert.assertEquals("fail", result.getResult());
        }

        Assert.assertTrue(spf || dmarc || dkim);
        Assert.assertEquals(StatusEnum.FAIL, authenticationResult.getStatus());

        /*
         * Test none
         */
        resp = api.getMail(FOLDER, IMPORTED_EMAILS.get(NONE).getId(), null, I(0), null, Boolean.FALSE, null, null, null, null, null, null, null, null);
        mail = checkResponse(resp);
        authenticationResult = mail.getAuthenticity();
        Assert.assertNotNull(authenticationResult);
        mailAuthenticityMechanismResults = authenticationResult.getUnconsideredResults();
        Assert.assertFalse(mailAuthenticityMechanismResults.isEmpty());

        spf = false;
        dmarc = false;
        dkim = false;
        for (MechanismResult result : mailAuthenticityMechanismResults) {
            switch (result.getMechanism()) {
                case DKIM:
                    dkim = true;
                    break;
                case DMARC:
                    dmarc = true;
                    break;
                case SPF:
                    spf = true;
                    break;
            }
            Assert.assertEquals("none", result.getResult());
        }

        Assert.assertTrue(spf && dmarc && dkim);
        Assert.assertEquals(StatusEnum.NEUTRAL, authenticationResult.getStatus());
    }

    @Test
    public void testTrustedDomain() throws ApiException {
        /*
         * Test trusted domain
         */
        MailResponse resp = api.getMail(FOLDER, IMPORTED_EMAILS.get(TRUSTED).getId(), null, I(0), null, Boolean.FALSE, null, null, null, null, null, null, null, null);
        MailData mail = checkResponse(resp);
        AuthenticationResult authenticationResult = mail.getAuthenticity();
        Assert.assertNotNull(authenticationResult);
        Assert.assertEquals(StatusEnum.PASS, authenticationResult.getStatus());
        Assert.assertNotNull(authenticationResult.getTrusted());
        Assert.assertTrue(authenticationResult.getTrusted().booleanValue());
        Assert.assertNotNull(authenticationResult.getImage());

        byte[] trustedMailPicture = imageApi.getTrustedMailPicture(authenticationResult.getImage());
        Assert.assertNotNull(trustedMailPicture);
        Assert.assertNotEquals(0, trustedMailPicture.length);
    }

    private MailData checkResponse(MailResponse resp) {
        Assert.assertNull(resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private List<MailDestinationData> checkResponse(MailImportResponse response) {
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        return response.getData();
    }

    // -------------------------   prepare config --------------------------------------

    private static final Map<String, String> CONFIG = new HashMap<String, String>();

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }

    @Override
    protected String getReloadables() {
        return "ConfigReloader,TrustedMailAuthenticityHandler";
    }

}
