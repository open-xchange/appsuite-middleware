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
import static org.junit.Assert.assertNull;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
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
import com.openexchange.testing.httpclient.models.Result;
import com.openexchange.testing.httpclient.models.Result.ResultEnum;

/**
 * {@link ApplyMailFilterTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class ApplyMailFilterTest extends AbstractMailFilterTest {

    /**
     * Tests the apply mailfilter action
     *
     * @throws ApiException
     */
    @Test
    public void testApplyMailfilterAction() throws ApiException {

        // Create a new mail folder
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(ApplyMailFilterTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        folder.setModule("mail");
        body.setFolder(folder);
        FolderUpdateResponse createFolder = folderApi.createFolder("default0/INBOX", getSessionId(), body, "0", null, null);
        assertNull(createFolder.getErrorDesc(), createFolder.getError());
        String folderId = rememberFolder(createFolder.getData());

        // Add two mails to this folder (one matches the sieve rule)
        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR);
        File f = new File(testMailDir, "mailfilter1.eml");
        Assert.assertTrue(f.exists());
        MailImportResponse importResponse = mailApi.importMail(getSessionId(), folderId, f, null, true);
        Assert.assertNull(importResponse.getErrorDesc(), importResponse.getError());
        f = new File(testMailDir, "mailfilter2.eml");
        Assert.assertTrue(f.exists());
        importResponse = mailApi.importMail(getSessionId(), folderId, f, null, true);
        Assert.assertNull(importResponse.getErrorDesc(), importResponse.getError());
        List<MailDestinationData> data = importResponse.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());
        String matchingMailId = data.get(0).getId();
        // Add a new sieve rule and remember its id
        MailFilterCreationResponse response = mailfilterapi.createRuleV2(getSessionId(), getRule(), null);
        Assert.assertNull(response.getErrorDesc(), response.getError());
        Integer ruleId = rememberSieveRule(response.getData());
        // apply the rule to the new folder
        MailFilterApplyResponse applyPredefinedRule = mailfilterapi.applyPredefinedRule(getSessionId(), ruleId, null, folderId);
        // Check result is ok
        Assert.assertNull(applyPredefinedRule.getErrorDesc(), applyPredefinedRule.getError());
        Assert.assertNotNull(applyPredefinedRule.getData());
        Assert.assertEquals("Invalid size of results", 1, applyPredefinedRule.getData().size());
        for (Result result : applyPredefinedRule.getData()) {
            Assert.assertEquals(ResultEnum.OK, result.getResult());
        }
        // Check one mail is properly marked as deleted
        MailsResponse allMails = mailApi.getAllMails(getSessionId(), folderId, "600", null, FALSE, FALSE, null, null, I(0), I(5), I(5), null);
        Assert.assertNull(allMails.getErrorDesc(), allMails.getError());
        Assert.assertNotNull(allMails.getData());
        Assert.assertEquals("Unexpected amount of mails returned.", 1, allMails.getData().size());
        Assert.assertNotEquals(matchingMailId, allMails.getData().get(0).get(0));
    }

    private MailFilterRulev2 getRule() {
        MailFilterRulev2 rule = new MailFilterRulev2();
        MailFilterTestv2 test = new MailFilterTestv2();
        test.setHeader("to");
        test.id("to");
        test.values(Collections.singletonList("person3@invalid.com"));
        test.comparison("contains");
        rule.test(test);
        MailFilterAction action = new MailFilterAction();
        action.setId("discard");
        rule.actioncmds(Collections.singletonList(action));
        return rule;
    }

}
