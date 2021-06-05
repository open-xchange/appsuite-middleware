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

package com.openexchange.ajax.find;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.find.common.Bug32060Test;
import com.openexchange.ajax.find.contacts.Bug33447Test;
import com.openexchange.ajax.find.contacts.Bug33576Test;
import com.openexchange.ajax.find.contacts.ExcludeContextAdminTest;
import com.openexchange.ajax.find.drive.BasicDriveTest;
import com.openexchange.ajax.find.drive.FolderNameFacetTest;
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
    com.openexchange.ajax.find.calendar.AutocompleteTest.class,
    com.openexchange.ajax.find.contacts.QueryTest.class,
    com.openexchange.ajax.find.contacts.AutoCompleteTest.class,
    com.openexchange.ajax.find.contacts.AutoCompleteShowDepartmentsTest.class,
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
    FolderNameFacetTest.class,
})
public final class FindTestSuite {
}
