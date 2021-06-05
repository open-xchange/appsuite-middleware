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

package com.openexchange.ajax.mail.filter.tests.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
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
    public ReorderTest() {
        super();
    }

    /**
     * Test the reorder API call
     */
    @Test
    public void testReorder() throws Exception {
        Random r = new Random(System.currentTimeMillis());

        // Create 10 rules
        LinkedList<Rule> expectedRules = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Rule rule = new Rule();
            rule.setName("testReorder" + i);
            rule.setActive(true);
            List<Action<? extends ActionArgument>> actions = new ArrayList<>(2);
            actions.add(new Keep());
            actions.add(new Stop());
            rule.setActions(actions);
            rule.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(rule);
            rememberRule(id);
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
