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

package com.openexchange.ajax.mail.filter.tests.bug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Move;
import com.openexchange.ajax.mail.filter.api.dao.comparison.ContainsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug18490Test}
 *
 * Important: This test should work as long as the mail server is cyrus older than 2.3.11 with mailfilter property com.openexchange.mail.filter.useUTF7FolderEncoding=true
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug18490Test extends AbstractMailFilterTest {

    FolderObject folder;

    AJAXClient client;

    /**
     * Initializes a new {@link Bug18490Test}.
     *
     * @param name
     */
    public Bug18490Test() {
        super();
    }

    @Test
    public void testBug18490() throws Exception {
        client = getClient();
        folder = Create.createPrivateFolder("Bug18490 test F\u00f6lder", FolderObject.MAIL, getClient().getValues().getUserId());
        folder.setFullName(getClient().getValues().getInboxFolder() + "/Bug18490 test F\u00f6lder");
        final InsertResponse folderInsertResponse = getClient().execute(new InsertRequest(EnumAPI.OX_NEW, folder));
        folderInsertResponse.fillObject(folder);

        final Rule rule = new Rule();
        rule.setName("Bug18490 test rule");
        rule.setActive(true);
        rule.addAction(new Move(folder.getFullName()));

        final ContainsComparison conComp = new ContainsComparison();
        rule.setTest(new HeaderTest(conComp, new String[] { "Subject" }, new String[] { "Bug18490" }));

        final int id = mailFilterAPI.createRule(rule);
        rememberRule(id);
        rule.setId(id);
        rule.setPosition(0);

        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("One rule was expected", 1, rules.size());

        final Rule loadRule = rules.get(0);
        assertRule(rule, loadRule);

        // Send Mail to myself
        final TestMail testMail = new TestMail();
        testMail.setSubject("Bug18490 testmail");
        testMail.setTo(Arrays.asList(new String[] { getClient().getValues().getSendAddress() }));
        testMail.setFrom(getClient().getValues().getSendAddress());
        testMail.setContentType(MailContentType.PLAIN.toString());
        testMail.setBody("Move me...");
        testMail.sanitize();
        final MailTestManager mtm = new MailTestManager(client, true);
        mtm.send(testMail);

        final List<TestMail> mails = mtm.findAndLoadSimilarMails(testMail, client, folder.getFullName());

        assertFalse("No mail was found", null == mails);
    }

}
