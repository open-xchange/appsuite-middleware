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

package com.openexchange.ajax.mail.filter.tests.bug;

import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
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

    private AJAXClient client;

    /**
     * Initializes a new {@link Bug31253Test}.
     * 
     * @param name
     */
    public Bug31253Test(String name) {
        super(name);
    }

    @Override
    public void tearDown() throws Exception {
        if (folder != null) {
            client.execute(new DeleteRequest(EnumAPI.OX_NEW, folder));
        }

        super.tearDown();
    }

    public void testBug31253() throws Exception {
        client = getClient();
        folder = Create.createPrivateFolder("Test for Bug31253", FolderObject.MAIL, client.getValues().getUserId());
        folder.setFullName(client.getValues().getInboxFolder() + "/Test for Bug31253");

        final InsertResponse folderInsertResponse = client.execute(new InsertRequest(EnumAPI.OX_NEW, folder));
        folderInsertResponse.fillObject(folder);

        final Rule rule = new Rule();
        rule.setName("Test rule for Bug31253");
        rule.setActive(true);
        Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), "Multiline subject with\nOK foobar for Bug 31253", "Multiline text with \nOK barfoo for Bug 31253");
        rule.addAction(vacation);

        final ContainsComparison conComp = new ContainsComparison();
        rule.setTest(new HeaderTest(conComp, new String[] { "Subject" }, new String[] { "31253" }));
        final int id = mailFilterAPI.createRule(rule);
        rule.setId(id);
        rule.setPosition(0);

        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("One rule was expected", 1, rules.size());

        final Rule loadRule = rules.get(0);
        assertRule(rule, loadRule);
    }
}
