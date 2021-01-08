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

package com.openexchange.ajax.mail.filter.apiclient;

import static com.openexchange.java.Autoboxing.I;
import static java.lang.Boolean.FALSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailFilterAction;
import com.openexchange.testing.httpclient.models.MailFilterApplyResponse;
import com.openexchange.testing.httpclient.models.MailFilterCreationResponse;
import com.openexchange.testing.httpclient.models.MailFilterRulev2;
import com.openexchange.testing.httpclient.models.MailFilterTestv2;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.models.Result.ResultEnum;

/**
 * {@link ApplyMailFilterTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ApplyMailFilterTest extends AbstractMailFilterTest {

    /**
     * Initializes a new {@link ApplyMailFilterTest}.
     */
    public ApplyMailFilterTest() {
        super();
    }

    /**
     * Tests the apply mail-filter action
     */
    @Test
    public void testApplyMailfilterAction() throws ApiException {
        // Create a new mail folder
        String folderId = createFolder();

        // Add two mails to this folder (one matches the sieve rule)
        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR);
        importMail("mailfilter1.eml", testMailDir, folderId);
        String matchingMailId = importMail("mailfilter2.eml", testMailDir, folderId);

        // Add a new sieve rule and remember its id
        Integer ruleId = addNewRule();

        // Check amount of mails are 2
        getAllMailsAssertAmount(folderId, 2);

        // Apply the rule to the new folder
        applyPredefinedRule(folderId, ruleId);

        // Check one mail is properly marked as deleted
        MailsResponse allMails = getAllMailsAssertAmount(folderId, 1);
        Assert.assertNotEquals(matchingMailId, allMails.getData().get(0).get(0));
    }

    /////////////////////////////////// HELPERS //////////////////////////////////

    /**
     * Creates a new folder
     *
     * @return The folder identifier
     * @throws ApiException if an error is occurred
     */
    private String createFolder() throws ApiException {
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(ApplyMailFilterTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        folder.setModule("mail");

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        FolderUpdateResponse createFolder = folderApi.createFolder("default0/INBOX", body, "0", null, null, null);
        assertNull(createFolder.getErrorDesc(), createFolder.getError());
        return rememberFolder(createFolder.getData());
    }

    /**
     * Imports the specified .eml asset from the specified asset directory
     * to the folder with the specified identifier
     *
     * @param asset The asset name
     * @param assetDir The asset directory
     * @param folderId The folder identifier
     * @return The imported mail identifier
     * @throws ApiException if an error is occurred
     */
    private String importMail(String asset, String assetDir, String folderId) throws ApiException {
        File f = new File(assetDir, asset);
        Assert.assertTrue(f.exists());

        MailImportResponse importResponse = mailApi.importMail(folderId, f, null, Boolean.TRUE);
        Assert.assertNull(importResponse.getErrorDesc(), importResponse.getError());

        List<MailDestinationData> data = importResponse.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());

        return data.get(0).getId();
    }

    /**
     * Gets all mails from the specified folder and asserts that the expected amount
     * is equals the actual amount
     *
     * @param folderId The folder identifier
     * @param expectedAmount The expected amount
     * @return The MailsResponse
     * @throws ApiException if an API error is occurred
     */
    private MailsResponse getAllMailsAssertAmount(String folderId, int expectedAmount) throws ApiException {
        MailsResponse allMails = mailApi.getAllMails(folderId, "600", null, FALSE, FALSE, null, null, I(0), I(5), I(5), null);
        Assert.assertNull(allMails.getErrorDesc(), allMails.getError());
        Assert.assertNotNull(allMails.getData());
        Assert.assertEquals("Unexpected amount of mails returned.", expectedAmount, allMails.getData().size());

        return allMails;
    }

    /**
     * Applies the predefined rule to the specified folder
     *
     * @param folderId The folder id to apply the rule
     * @param ruleId The rule id to apply to the folder
     * @throws ApiException if an error is occurred
     */
    private void applyPredefinedRule(String folderId, Integer ruleId) throws ApiException {
        MailFilterApplyResponse applyPredefinedRule = mailfilterapi.applyPredefinedRule(ruleId, null, folderId);

        // Check result is ok
        Assert.assertNull(applyPredefinedRule.getErrorDesc(), applyPredefinedRule.getError());
        Assert.assertNotNull(applyPredefinedRule.getData());
        applyPredefinedRule.getData().stream().forEach(result -> assertNull("Expected no error but found: " + result.getErrors(), result.getErrors()));

        Assert.assertEquals("Invalid size of results: " + applyPredefinedRule.getData().toString(), 1, applyPredefinedRule.getData().size());
        applyPredefinedRule.getData().stream().forEach(m -> assertEquals(ResultEnum.OK, m.getResult()));
    }

    /**
     * Adds a new sieve rule
     * 
     * @return The rule's id
     * @throws ApiException if an error is occurred
     */
    private Integer addNewRule() throws ApiException {
        MailFilterCreationResponse response = mailfilterapi.createRuleV2(getRule(), null);
        Assert.assertNull(response.getErrorDesc(), response.getError());
        return rememberSieveRule(response.getData());
    }

    /**
     * Creates a rule
     *
     * @return The created rule
     */
    private MailFilterRulev2 getRule() {
        //@formatter:off
        MailFilterTestv2 test = new MailFilterTestv2();
        test.id("to")
            .values(ImmutableList.of("person3@invalid.com"))
            .comparison("contains")
            .setHeader("to");
        //@formatter:on

        MailFilterAction action = new MailFilterAction();
        action.setId("discard");

        return new MailFilterRulev2().test(test).actioncmds(ImmutableList.of(action));
    }
}
