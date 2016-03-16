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

package com.openexchange.ajax.task;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for all task tests.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TaskTestSuite {

    /**
     * Prevent instantiation
     */
    private TaskTestSuite() {
        super();
    }

    /**
     * Generates the task test suite.
     * @return the task tests suite.
     */
    public static Test suite() {
        final TestSuite tests = new TestSuite("com.openexchange.ajax.task.TaskTestSuite");
        // First the function tests.
        tests.addTestSuite(TasksTest.class);
        tests.addTestSuite(TaskAttachmentTests.class);

        // Now several single function tests.
        tests.addTestSuite(InsertTest.class);
        tests.addTestSuite(CharsetTest.class);
        tests.addTestSuite(TruncationTest.class);
        tests.addTestSuite(FloatTest.class);
        tests.addTestSuite(AllTest.class);
        tests.addTestSuite(ListTest.class);
        tests.addTestSuite(UpdatesTest.class);
        tests.addTestSuite(TaskRecurrenceTest.class);
        tests.addTestSuite(ConfirmTest.class);
        tests.addTestSuite(AllAliasTest.class);
        tests.addTestSuite(ListAliasTest.class);
        tests.addTestSuite(TaskDurationAndCostsTest.class);
        tests.addTestSuite(DeleteMultipleTaskTest.class);
        tests.addTest(new JUnit4TestAdapter(DateTimeTest.class));

        // Nodes
        tests.addTestSuite(LastModifiedUTCTest.class);

        // And finally bug tests.
        tests.addTestSuite(Bug6335Test.class);
        tests.addTestSuite(Bug7276Test.class);
        tests.addTestSuite(Bug7380Test.class);
        tests.addTestSuite(Bug7377Test.class);
        tests.addTestSuite(Bug8935Test.class);
        tests.addTestSuite(Bug9252Test.class);
        tests.addTestSuite(Bug10071Test.class);
        tests.addTestSuite(Bug10119Test.class);
        tests.addTestSuite(Bug10400Test.class);
        tests.addTestSuite(Bug11075Test.class);
        tests.addTestSuite(Bug11190Test.class);
        tests.addTestSuite(Bug11195Test.class);
        tests.addTestSuite(Bug11397Test.class);
        tests.addTestSuite(Bug11619Test.class);
        tests.addTestSuite(Bug11650Test.class);
        tests.addTestSuite(Bug11659Test.class);
        tests.addTestSuite(Bug11848Test.class);
        tests.addTestSuite(Bug12364Test.class);
        tests.addTestSuite(Bug12727Test.class);
        tests.addTestSuite(Bug12926Test.class);
        tests.addTestSuite(Bug13173Test.class);
        tests.addTestSuite(Bug14002Test.class);
        tests.addTest(new JUnit4TestAdapter(Bug15291Test.class));
        tests.addTestSuite(Bug15580Test.class);
        tests.addTestSuite(Bug15897Test.class);
        tests.addTestSuite(Bug15937Test.class);
        tests.addTestSuite(Bug16006Test.class);
        tests.addTestSuite(Bug18204Test.class);
        tests.addTestSuite(Bug20008Test.class);
        tests.addTestSuite(Bug21026Test.class);
        tests.addTestSuite(Bug22305Test.class);
        tests.addTestSuite(Bug23444Test.class);
        tests.addTest(new JUnit4TestAdapter(Bug26217Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug27840Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug28089Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug30015Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug32044Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug33258Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug35992Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug36943Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug37002Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug37424Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug37927Test.class));
        tests.addTest(new JUnit4TestAdapter(Bug38782Test.class));
        return tests;
    }
}
