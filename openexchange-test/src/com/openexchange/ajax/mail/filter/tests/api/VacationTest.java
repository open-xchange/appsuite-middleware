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

package com.openexchange.ajax.mail.filter.tests.api;

import java.util.Collections;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link VacationTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class VacationTest extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link VacationTest}.
     * 
     * @param name The test case's name
     */
    public VacationTest(String name) {
        super(name);
    }

    /**
     * Test a basic vacation notice with multiple addresses
     */
    public void testNewVacationWithMultipleAddresses() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(13, new String[] { "root@localhost", "billg@microsoft.com" }, "Betreff", "Text\\u000aText");
            expected.setActioncmds(new AbstractAction[] { vacation });
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a basic vacation notice with single address
     */
    public void testNewVacationWithSingleAddress() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Vacation Notice");
            expected.setFlags(new String[] { "vacation" });
            expected.setActive(false);

            Vacation vacation = new Vacation(1, new String[] { "dsfa" }, "123", "123");
            expected.setActioncmds(new AbstractAction[] { vacation });
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
        }

        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Test a vacation notice without subject
     */
    public void testNewVacationWithoutSubject() throws Exception {
        Rule expected;
        {
            expected = new Rule();
            expected.setName("Abwesenheitsbenachrichtigung");
            expected.setActive(true);
            expected.setFlags(new String[] { "vacation" });

            Vacation vacation = new Vacation(7, new String[] { client.getValues().getDefaultAddress() }, null, "I'm out of office");
            expected.setActioncmds(new AbstractAction[] { vacation });
            expected.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
        }

        getAndAssert(Collections.singletonList(expected));
    }
}
