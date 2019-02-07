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

package com.openexchange.ajax.mail.authenticity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.junit.Assert;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AuthenticationResult;
import com.openexchange.testing.httpclient.models.AuthenticationResult.StatusEnum;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsCleanUpResponse;
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
    private Long timestamp = 0l;
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

        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR) + SUBFOLDER;
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
            MailImportResponse response = api.importMail(getApiClient().getSession(), FOLDER, f, null, true);
            List<MailDestinationData> data = checkResponse(response);
            // data size should always be 1
            Assert.assertEquals(1, data.size());
            IMPORTED_EMAILS.put(name, data.get(0));
            timestamp = response.getTimestamp();
        }

    }

    @Override
    public void tearDown() throws Exception {
        try {
            List<MailListElement> body = new ArrayList<>();
            for (MailDestinationData dest : IMPORTED_EMAILS.values()) {
                MailListElement mailListElement = new MailListElement();
                mailListElement.setFolder(dest.getFolderId());
                mailListElement.setId(dest.getId());
                body.add(mailListElement);
            }
            MailsCleanUpResponse deleteMails = api.deleteMails(getApiClient().getSession(), body, timestamp);
            Assert.assertNull(deleteMails.getErrorDesc(), deleteMails.getError());
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testBasicFunctionality() throws ApiException {
        /*
         * Test pass all
         */
        MailResponse resp = api.getMail(getApiClient().getSession(), FOLDER, IMPORTED_EMAILS.get(PASS_ALL).getId(), null, 0, null, false, null, null, null, null, null, null, null, null);
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
        resp = api.getMail(getApiClient().getSession(), FOLDER, IMPORTED_EMAILS.get(PISHING).getId(), null, 0, null, false, null, null, null, null, null, null, null, null);
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
        resp = api.getMail(getApiClient().getSession(), FOLDER, IMPORTED_EMAILS.get(NONE).getId(), null, 0, null, false, null, null, null, null, null, null, null, null);
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
        MailResponse resp = api.getMail(getApiClient().getSession(), FOLDER, IMPORTED_EMAILS.get(TRUSTED).getId(), null, 0, null, false, null, null, null, null, null, null, null, null);
        MailData mail = checkResponse(resp);
        AuthenticationResult authenticationResult = mail.getAuthenticity();
        Assert.assertNotNull(authenticationResult);
        Assert.assertEquals(StatusEnum.PASS, authenticationResult.getStatus());
        Assert.assertNotNull(authenticationResult.getTrusted());
        Assert.assertTrue(authenticationResult.getTrusted());
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
