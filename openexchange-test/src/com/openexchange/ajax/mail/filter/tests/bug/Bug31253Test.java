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
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.comparison.ContainsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug31253Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug31253Test extends AbstractMailFilterTest {

    private FolderObject folder;

    /**
     * Initializes a new {@link Bug31253Test}.
     *
     * @param name
     */
    public Bug31253Test() {
        super();
    }

    @Test
    public void testBug31253() throws Exception {
        folder = Create.createPrivateFolder("Test for Bug31253", FolderObject.MAIL, getClient().getValues().getUserId());
        folder.setFullName(getClient().getValues().getInboxFolder() + "/Test for Bug31253");

        final InsertResponse folderInsertResponse = getClient().execute(new InsertRequest(EnumAPI.OX_NEW, folder));
        folderInsertResponse.fillObject(folder);

        final Rule rule = new Rule();
        rule.setName("Test rule for Bug31253");
        rule.setActive(true);
        Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), "Multiline subject with\nOK foobar for Bug 31253", "Multiline text with \nOK barfoo for Bug 31253");
        rule.addAction(vacation);

        final ContainsComparison conComp = new ContainsComparison();
        rule.setTest(new HeaderTest(conComp, new String[] { "Subject" }, new String[] { "31253" }));
        final int id = mailFilterAPI.createRule(rule);
        rememberRule(id);
        rule.setId(id);
        rule.setPosition(0);

        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("One rule was expected", 1, rules.size());

        final Rule loadRule = rules.get(0);
        assertRule(rule, loadRule);
    }
}
