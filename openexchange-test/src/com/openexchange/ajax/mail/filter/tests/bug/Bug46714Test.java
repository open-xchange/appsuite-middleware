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

import java.util.LinkedList;
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
    public Bug46714Test(String name) {
        super(name);
    }

    /**
     * Insert 5 rules and try to reorder with an array of 6
     */
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
