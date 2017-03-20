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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for all task tests.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    TasksTest.class,
    TaskAttachmentTests.class,
    
    // Now several single function tests.
    InsertTest.class,
    CharsetTest.class,
    TruncationTest.class,
    FloatTest.class,
    AllTest.class,
    ListTest.class,
    UpdatesTest.class,
    TaskRecurrenceTest.class,
    ConfirmTest.class,
    AllAliasTest.class,
    ListAliasTest.class,
    TaskDurationAndCostsTest.class,
    DeleteMultipleTaskTest.class,
    DateTimeTest.class,
    
    // Nodes
    LastModifiedUTCTest.class,
    
    // And finally bug tests.
    Bug6335Test.class,
    Bug7276Test.class,
    Bug7380Test.class,
    Bug7377Test.class,
    Bug8935Test.class,
    Bug9252Test.class,
    Bug10071Test.class,
    Bug10119Test.class,
    Bug10400Test.class,
    Bug11075Test.class,
    Bug11190Test.class,
    Bug11195Test.class,
    Bug11397Test.class,
    Bug11619Test.class,
    Bug11650Test.class,
    Bug11659Test.class,
    Bug11848Test.class,
    Bug12364Test.class,
    Bug12727Test.class,
    Bug12926Test.class,
    Bug13173Test.class,
    Bug14002Test.class,
    Bug15291Test.class,
    Bug15580Test.class,
    Bug15897Test.class,
    Bug15937Test.class,
    Bug16006Test.class,
    Bug18204Test.class,
    Bug20008Test.class,
    Bug21026Test.class,
    Bug22305Test.class,
    Bug23444Test.class,
    Bug26217Test.class,
    Bug27840Test.class,
    Bug28089Test.class,
    Bug30015Test.class,
    Bug32044Test.class,
    Bug33258Test.class,
    Bug35992Test.class,
    Bug36943Test.class,
    Bug37002Test.class,
    Bug37424Test.class,
    Bug37927Test.class,
    Bug38782Test.class,
    Bug50739Test.class,
})
public final class TaskTestSuite {
}
