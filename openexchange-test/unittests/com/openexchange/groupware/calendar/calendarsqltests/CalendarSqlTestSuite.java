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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    FullTimeSeries.class,
    Bug9950Test.class,
    Bug5557Test.class,
    Bug4778Test.class,
    Bug13358Test.class,
    Bug13121Test.class,
    Bug13068Test.class,
    Bug12923Test.class,
    Bug12681Test.class,
    Bug12662Test.class,
    Bug12659Test.class,
    Bug12601Test.class,
    Bug12571Test.class,
    Bug12509Test.class,
    Bug12496Test.class,
    Bug12489Test.class,
    Bug12466Test.class,
    Bug12413Test.class,
    Bug12377Test.class,
    Bug12269Test.class,
    Bug12072Test.class,
    Bug11865Test.class,
    Bug11803Test.class,
    Bug11730Test.class,
    Bug11708Test.class,
    Bug11695Test.class,
    Bug11453Test.class,
    Bug11424Test.class,
    Bug11316Test.class,
    Bug11307Test.class,
    Bug11148Test.class,
    Bug11059Test.class,
    Bug11051Test.class,
    Bug10806Test.class,
    Bug10154Test.class,
    Node1077Test.class,
    ParticipantsAgreeViaDifferentLoadMethods.class,
    Bug13995And14922Test.class,
    Bug13446Test.class,
    Bug11210Test.class,
    Bug13226Test.class,
    Bug14625Test.class,
    Bug15155Test.class,
    Bug15031Test.class,
    Bug16540Test.class,
    Bug24682Test.class,
    Bug29339Test.class,
    Bug30361Test.class,
    UserStory1906Test.class,
})
public class CalendarSqlTestSuite {

}
