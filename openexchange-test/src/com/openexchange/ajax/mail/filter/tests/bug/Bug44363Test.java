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
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Discard;
import com.openexchange.ajax.mail.filter.api.dao.action.Vacation;
import com.openexchange.ajax.mail.filter.api.dao.comparison.ContainsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;

/**
 * {@link Bug44363Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug44363Test extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link Bug44363Test}.
     *
     * @param name
     */
    public Bug44363Test() {
        super();
    }

    /**
     * Test case for bug 44363
     * <ul>
     * <li>Create a vacation rule</li>
     * <li>Create some other arbitrary rule</li>
     * <li>Deactivate the other rule</li>
     * <li>Deactivate the vacation rule</li>
     * </ul>
     * Assert there are still two rules present
     *
     * @throws Exception if an error is occurred
     */
    @Test
    public void testBug44363() throws Exception {
        // Create a vacation rule with a single lined dot '.' character
        Rule vacationRule;
        {
            vacationRule = new Rule();
            vacationRule.setName("Vacation Notice");
            vacationRule.setActive(true);
            Vacation vacation = new Vacation(7, Collections.singletonList("foo@invalid.tld"), "Vacation Notice for Bug 44363", "Multiline text with\n\n.\n\n a single lined dot character for bug 44363");
            vacationRule.addAction(vacation);
            final ContainsComparison conComp = new ContainsComparison();
            vacationRule.setTest(new HeaderTest(conComp, new String[] { "Subject" }, new String[] { "Vacation for 44363" }));
            int vacationId = mailFilterAPI.createRule(vacationRule);
            rememberRule(vacationId);
            vacationRule.setId(vacationId);
        }

        // Create some other rule
        Rule otherRule;
        {
            otherRule = new Rule();
            otherRule.setName("Some Rule for Bug 44363");
            otherRule.setActive(true);
            otherRule.addAction(new Discard());
            ContainsComparison conComp = new ContainsComparison();
            otherRule.setTest(new HeaderTest(conComp, new String[] { "Subject" }, new String[] { "Bug 44363" }));
            int otherId = mailFilterAPI.createRule(otherRule);
            rememberRule(otherId);
            otherRule.setId(otherId);
        }

        // Assert we have 2 rules
        List<Rule> rules = mailFilterAPI.listRules();
        assertEquals("Two rules were expected", 2, rules.size());

        // Deactivate the other rule
        otherRule.setActive(false);
        mailFilterAPI.updateRule(otherRule);

        // Deactivate the vacation notice
        vacationRule.setActive(false);
        mailFilterAPI.updateRule(vacationRule);

        // Assert that we still have two rules
        rules = mailFilterAPI.listRules();
        assertEquals("Two rules were expected", 2, rules.size());
    }
}
