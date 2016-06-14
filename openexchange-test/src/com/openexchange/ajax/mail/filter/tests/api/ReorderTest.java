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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link ReorderTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ReorderTest extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link ReorderTest}.
     * 
     * @param name
     */
    public ReorderTest(String name) {
        super(name);
    }

    /**
     * Test the reorder API call
     */
    public void testReorder() throws Exception {
        Random r = new Random(System.currentTimeMillis());

        // Create 10 rules
        LinkedList<Rule> expectedRules = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Rule rule = new Rule();
            rule.setName("testReorder" + i);
            rule.setActive(true);
            rule.setActionCommands(new Action[] { new Keep(), new Stop() });
            rule.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(rule);
            rule.setId(id);
            rule.setPosition(i);
            expectedRules.add(rule);
        }

        // Initialise the alreadyPicked array
        int reorderSize = 5;
        int[] alreadyPicked = new int[reorderSize];
        for (int i = 0; i < alreadyPicked.length; i++) {
            alreadyPicked[i] = -1;
        }

        int[] reorder = new int[reorderSize];
        // Reorder 5 random rules
        for (int i = 4; i >= 0; i--) {
            int ri = -1;
            do {
                ri = r.nextInt(10 - reorderSize) + reorderSize;
            } while (Arrays.binarySearch(alreadyPicked, ri) >= 0);
            alreadyPicked[i] = ri;

            // Remove the rule from the expected list
            Rule removedRule = expectedRules.remove(ri);
            // Added to the reorder list
            reorder[i] = removedRule.getId();
            // And add the removed rule  at the first position of the expected
            expectedRules.addFirst(removedRule);
        }

        // Refresh the positions
        int i = 0;
        for (Rule rule : expectedRules) {
            rule.setPosition(i++);
        }

        // Reorder
        mailFilterAPI.reorder(reorder);

        // Get and assert
        getAndAssert(expectedRules);
    }
}
