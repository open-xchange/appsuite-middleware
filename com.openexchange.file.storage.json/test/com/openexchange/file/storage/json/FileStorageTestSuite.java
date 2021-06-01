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

package com.openexchange.file.storage.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.file.storage.json.actions.files.AllTest;
import com.openexchange.file.storage.json.actions.files.DeleteTest;
import com.openexchange.file.storage.json.actions.files.DetachTest;
import com.openexchange.file.storage.json.actions.files.FileActionTest;
import com.openexchange.file.storage.json.actions.files.GetTest;
import com.openexchange.file.storage.json.actions.files.InfostoreRequestTest;
import com.openexchange.file.storage.json.actions.files.ListTest;
import com.openexchange.file.storage.json.actions.files.LockActionTest;
import com.openexchange.file.storage.json.actions.files.NewTest;
import com.openexchange.file.storage.json.actions.files.RevertTest;
import com.openexchange.file.storage.json.actions.files.SearchTest;
import com.openexchange.file.storage.json.actions.files.UnlockActionTest;
import com.openexchange.file.storage.json.actions.files.UpdateTest;
import com.openexchange.file.storage.json.actions.files.UpdatesTest;
import com.openexchange.file.storage.json.actions.files.VersionsTest;

/**
 * {@link FileStorageTestSuite}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    // @formatter:off
    FileWriterTest.class, 
    FileSearchTermParserTest.class,
    FileParserTest.class, 
    AllTest.class,
    DeleteTest.class,
    DetachTest.class,
    FileActionTest.class,
    GetTest.class,
    InfostoreRequestTest.class,
    ListTest.class,
    LockActionTest.class,
    NewTest.class,
    RevertTest.class,
    SearchTest.class,
    UnlockActionTest.class,
    UpdatesTest.class,
    UpdateTest.class,
    VersionsTest.class
    // @formatter:on
})
public class FileStorageTestSuite {

}
