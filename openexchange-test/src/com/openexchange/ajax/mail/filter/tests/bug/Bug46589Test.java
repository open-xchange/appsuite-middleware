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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.Comparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.comparison.argument.IsComparisonArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link Bug46589Test}. Tests for Bug 46589 - [L3] inserting a mailfilter with position 0 and an empty list fails with BAD_POSITION Exception
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug46589Test extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link Bug46589Test}.
     *
     * @param name Test case's name
     */
    public Bug46589Test() {
        super();
    }

    /**
     * Insert a single rule in position 0 when list is empty
     */
    @Test
    public void testBug46589_0() throws Exception {
        // Create the rule
        final Rule expected;
        {
            expected = new Rule();
            expected.setPosition(0);
            expected.setActive(true);
            expected.setName("testNew");
            expected.addAction(new Stop());
            final Comparison<IsComparisonArgument> isComp = new IsComparison();
            expected.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

            int id = mailFilterAPI.createRule(expected);
            rememberRule(id);
            expected.setId(id);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Insert multiple rules in positions when list is filled
     */
    @Test
    public void testBug46589_1() throws Exception {
        // Create 5 rules and insert them
        LinkedList<Rule> expectedRules = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            Rule rule = new Rule();
            rule.setName("testBug46589_1_" + i);
            rule.setActive(true);
            rule.addAction(new Keep());
            rule.addAction(new Stop());
            rule.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(rule);
            rememberRule(id);
            rule.setId(id);
            rule.setPosition(i);
            expectedRules.add(rule);
        }

        // Insert a rule in position 3
        Rule rule = new Rule();
        rule.setName("testBug46589_1_" + 5);
        rule.setActive(true);
        rule.setPosition(3);
        rule.addAction(new Keep());
        rule.addAction(new Stop());
        rule.setTest(new TrueTest());

        int id = mailFilterAPI.createRule(rule);
        rememberRule(id);
        rule.setId(id);
        rule.setPosition(3);

        // Adjust the expected rules
        List<Rule> finalRules = new ArrayList<>(expectedRules.size() + 1);
        Collections.addAll(finalRules, expectedRules.subList(0, 3).toArray(new Rule[3]));
        finalRules.add(rule);
        for (int index = 3; index < expectedRules.size(); index++) {
            Rule r = expectedRules.get(index);
            r.setPosition(index + 1);
            finalRules.add(r);
        }

        // Assert
        getAndAssert(finalRules);
    }
}
