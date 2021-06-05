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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.LinkedList;
import org.junit.Test;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.Keep;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.test.TrueTest;
import com.openexchange.ajax.mail.filter.tests.AbstractMailFilterTest;
import com.openexchange.exception.OXException;

/**
 * {@link Bug46714Test}. Test for Bug 46714 - IOOBE at GeneralMailFilterGroup.getOrderedRules
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug46714Test extends AbstractMailFilterTest {

    /**
     * Initialises a new {@link Bug46714Test}.
     *
     * @param name the test's name
     */
    public Bug46714Test() {
        super();
    }

    /**
     * Insert 5 rules and try to reorder with an array of 6
     */
    @Test
    public void testBug46714() throws Exception {
        // Create 5 rules and insert them
        LinkedList<Rule> expectedRules = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            Rule rule = new Rule();
            rule.setName("testBug46714_1_" + i);
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

        int reorder[] = new int[] { 1, 1, 1, 1, 1, 1 };

        // Reorder
        try {
            // We are expecting an exception so we disable the failOnError
            mailFilterAPI.setFailOnError(false);
            mailFilterAPI.reorder(reorder);
            fail("Expected an exception");
        } catch (Exception e) {
            assertTrue("The exception is not an OXException", e instanceof OXException);
            OXException oxe = (OXException) e;
            assertEquals("The exception code does not match", 28, oxe.getCode());
            assertEquals("The exception prefix does not match", "MAIL_FILTER", oxe.getPrefix());
        }
    }
}
