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

package com.openexchange.ajax.mail.filter.tests;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.mail.filter.api.dao.Rule;
import com.openexchange.ajax.mail.filter.api.dao.action.AbstractAction;
import com.openexchange.ajax.mail.filter.api.dao.action.Stop;
import com.openexchange.ajax.mail.filter.api.dao.comparison.IsComparison;
import com.openexchange.ajax.mail.filter.api.dao.test.HeaderTest;

public class NewTest extends AbstractMailFilterTest {

    public NewTest(String name) {
        super(name);
    }

    public void testNew() throws Exception {
        final AJAXSession ajaxSession = getSession();

        String forUser = null;

        deleteAllExistingRules(forUser, ajaxSession);

        final Rule rule = new Rule();
        rule.setName("testNew");
        rule.setActioncmds(new AbstractAction[] { new Stop() });

        final IsComparison isComp = new IsComparison();
        rule.setTest(new HeaderTest(isComp, new String[] { "testheader" }, new String[] { "testvalue"} ));

        final String id = insertRule(rule, forUser, ajaxSession);

        final String[] idArray = getIdArray(forUser, ajaxSession);

        assertEquals("one rules expected", 1, idArray.length);

        final Rule loadRule = loadRules(forUser, id, ajaxSession);
        compareRule(rule, loadRule);

        deleteRule(id, forUser, ajaxSession);
    }

    public void testNewWithTwoEntries() throws Exception {
        final AJAXSession ajaxSession = getSession();

        String forUser = null;

        deleteAllExistingRules(forUser, ajaxSession);

        final Rule rule1 = new Rule();
        rule1.setName("testNewWithTwoEntries1");
        rule1.setActioncmds(new AbstractAction[] { new Stop() });

        IsComparison isComp = new IsComparison();
        rule1.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test"} ));

        final String id1 = insertRule(rule1, forUser, ajaxSession);
        rule1.setId(id1);
        
        final Rule rule2 = new Rule();
        rule2.setName("testNewWithTwoEntries2");
        rule2.setActioncmds(new AbstractAction[] { new Stop() });

        isComp = new IsComparison();
        rule2.setTest(new HeaderTest(isComp, new String[] { "test" }, new String[] { "test"} ));

        final String id2 = insertRule(rule2, forUser, ajaxSession);

        final String[] idArray = getIdArray(forUser, ajaxSession);

        assertEquals("two rules expected", 2, idArray.length);

        Rule loadRule = loadRules(forUser, id1, ajaxSession);
        rule1.setPosition(0);
        compareRule(rule1, loadRule);

        loadRule = loadRules(forUser, id2, ajaxSession);
        rule2.setPosition(1);
        compareRule(rule2, loadRule);

        deleteRule(id1, forUser, ajaxSession);
        deleteRule(id2, forUser, ajaxSession);
    }
}
