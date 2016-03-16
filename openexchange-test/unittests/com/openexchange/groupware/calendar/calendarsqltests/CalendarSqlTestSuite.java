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

package com.openexchange.groupware.calendar.calendarsqltests;

import com.openexchange.groupware.calendar.calendarsqltests.untiltests.UntilTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class CalendarSqlTestSuite {

    public static Test suite() {
        TestSuite tests = new TestSuite();

        tests.addTestSuite(FullTimeSeries.class);
        tests.addTestSuite(Bug9950Test.class);
        tests.addTestSuite(Bug5557Test.class);
        tests.addTestSuite(Bug4778Test.class);
        tests.addTestSuite(Bug13358Test.class);
        tests.addTestSuite(Bug13121Test.class);
        tests.addTestSuite(Bug13068Test.class);
        tests.addTestSuite(Bug12923Test.class);
        tests.addTestSuite(Bug12681Test.class);
        tests.addTestSuite(Bug12662Test.class);
        tests.addTestSuite(Bug12659Test.class);
        tests.addTestSuite(Bug12601Test.class);
        tests.addTestSuite(Bug12571Test.class);
        tests.addTestSuite(Bug12509Test.class);
        tests.addTestSuite(Bug12496Test.class);
        tests.addTestSuite(Bug12489Test.class);
        tests.addTestSuite(Bug12466Test.class);
        tests.addTestSuite(Bug12413Test.class);
        tests.addTestSuite(Bug12377Test.class);
        tests.addTestSuite(Bug12269Test.class);
        tests.addTestSuite(Bug12072Test.class);
        tests.addTestSuite(Bug11865Test.class);
        tests.addTestSuite(Bug11803Test.class);
        tests.addTestSuite(Bug11730Test.class);
        tests.addTestSuite(Bug11708Test.class);
        tests.addTestSuite(Bug11695Test.class);
        tests.addTestSuite(Bug11453Test.class);
        tests.addTestSuite(Bug11424Test.class);
        tests.addTestSuite(Bug11316Test.class);
        tests.addTestSuite(Bug11307Test.class);
        tests.addTestSuite(Bug11148Test.class);
        tests.addTestSuite(Bug11059Test.class);
        tests.addTestSuite(Bug11051Test.class);
        tests.addTestSuite(Bug10806Test.class);
        tests.addTestSuite(Bug10154Test.class);
        tests.addTestSuite(Node1077Test.class);
        tests.addTestSuite(ParticipantsAgreeViaDifferentLoadMethods.class);
        tests.addTestSuite(Bug13995And14922Test.class);
        tests.addTestSuite(Bug13446Test.class);
        tests.addTestSuite(Bug11210Test.class);
        tests.addTestSuite(Bug13226Test.class);
        tests.addTestSuite(Bug14625Test.class);
        tests.addTestSuite(Bug15155Test.class);
        tests.addTestSuite(Bug15031Test.class);
        tests.addTestSuite(Bug16540Test.class);
        tests.addTestSuite(Bug24682Test.class);
        tests.addTestSuite(Bug29339Test.class);
        tests.addTestSuite(Bug30361Test.class);

        tests.addTestSuite(UserStory1906Test.class);

        tests.addTest(UntilTestSuite.suite());

        return tests;
    }
}
