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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
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
    public Bug46589Test(String name) {
        super(name);
    }

    /**
     * Insert a single rule in position 0 when list is empty
     */
    public void testBug46589_0() throws Exception {
        // Create the rule
        final Rule expected;
        {
            expected = new Rule();
            expected.setPosition(0);
            expected.setActive(true);
            expected.setName("testNew");
            expected.setActions(Collections.<Action<? extends ActionArgument>> singletonList(new Stop()));
            final Comparison<IsComparisonArgument> isComp = new IsComparison();
            expected.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue" }));

            int id = mailFilterAPI.createRule(expected);
            expected.setId(id);
        }

        // Assert
        getAndAssert(Collections.singletonList(expected));
    }

    /**
     * Insert multiple rules in positions when list is filled
     */
    public void testBug46589_1() throws Exception {
        // Create 5 rules and insert them
        LinkedList<Rule> expectedRules = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            Rule rule = new Rule();
            rule.setName("testBug46589_1_" + i);
            rule.setActive(true);
            List<Action<? extends ActionArgument>> actions = new ArrayList<>(2);
            actions.add(new Keep());
            actions.add(new Stop());
            rule.setActions(actions);
            rule.setTest(new TrueTest());

            int id = mailFilterAPI.createRule(rule);
            rule.setId(id);
            rule.setPosition(i);
            expectedRules.add(rule);
        }

        // Insert a rule in position 3
        Rule rule = new Rule();
        rule.setName("testBug46589_1_" + 5);
        rule.setActive(true);
        rule.setPosition(3);
        List<Action<? extends ActionArgument>> actions = new ArrayList<>(2);
        actions.add(new Keep());
        actions.add(new Stop());
        rule.setActions(actions);
        rule.setTest(new TrueTest());

        int id = mailFilterAPI.createRule(rule);
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
