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

package com.openexchange.ajax.find;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.find.common.Bug32060Test;
import com.openexchange.ajax.find.contacts.Bug33447Test;
import com.openexchange.ajax.find.contacts.Bug33576Test;
import com.openexchange.ajax.find.contacts.ExcludeContextAdminTest;
import com.openexchange.ajax.find.drive.BasicDriveTest;
import com.openexchange.ajax.find.mail.BasicMailTest;
import com.openexchange.ajax.find.mail.Bug35442Test;
import com.openexchange.ajax.find.mail.Bug36522Test;
import com.openexchange.ajax.find.mail.Bug39105Test;
import com.openexchange.ajax.find.mail.Bug42970Test;
import com.openexchange.ajax.find.tasks.FindTasksAutocompleteTests;
import com.openexchange.ajax.find.tasks.FindTasksQueryTests;
import com.openexchange.ajax.find.tasks.FindTasksTestsFilterCombinations;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link FindTestSuite}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    com.openexchange.ajax.find.calendar.QueryTest.class,
    //disable AutoCompleteTest for now
    //com.openexchange.ajax.find.calendar.AutocompleteTest.class,
    com.openexchange.ajax.find.contacts.QueryTest.class,
    com.openexchange.ajax.find.contacts.AutocompleteTest.class,
    BasicMailTest.class,
    BasicDriveTest.class,
    FindTasksTestsFilterCombinations.class,
    FindTasksQueryTests.class,
    FindTasksAutocompleteTests.class,
    Bug32060Test.class,
    ExcludeContextAdminTest.class,
    Bug33447Test.class,
    Bug33576Test.class,
    Bug36522Test.class,
    Bug35442Test.class,
    Bug39105Test.class,
    Bug42970Test.class,
})
public final class FindTestSuite {
}
