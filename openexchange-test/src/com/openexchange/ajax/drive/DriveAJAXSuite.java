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

package com.openexchange.ajax.drive;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.drive.apiclient.test.TrashTests;
import com.openexchange.ajax.drive.test.Bug67685Test;
import com.openexchange.ajax.drive.test.DeleteLinkTest;
import com.openexchange.ajax.drive.test.GetLinkTest;
import com.openexchange.ajax.drive.test.MWB1058Test;
import com.openexchange.ajax.drive.test.MWB358Test;
import com.openexchange.ajax.drive.test.QuotaForSyncTest;
import com.openexchange.ajax.drive.test.ResumableChecksumTest;
import com.openexchange.ajax.drive.test.UpdateLinkTest;
import com.openexchange.ajax.drive.updater.UpdaterXMLTest;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link DriveAJAXSuite}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    GetLinkTest.class,
    UpdateLinkTest.class,
    DeleteLinkTest.class,
    UpdaterXMLTest.class,
    QuotaForSyncTest.class,
    Bug67685Test.class,
    TrashTests.class,
    ResumableChecksumTest.class,
    MWB358Test.class,
    MWB1058Test.class,
})
public class DriveAJAXSuite {

}
