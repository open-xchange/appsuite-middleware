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

package com.openexchange.ajax.importexport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * This suite is meant to be used with a running OX.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    ICalTestSuite.class,
    VCardTestSuite.class,
    Bug9475Test.class,

    //CSV
    CSVImportExportServletTest.class,
    Bug18482Test_ByteOrderMarkOnUtf8.class,
    Bug20516Test.class,
    Bug32200Test.class,
    Bug33748Test.class,
    Bug32994Test.class,
    Bug34499Test.class,
    Bug36687Test.class,
    JPCSVImportTest.class,
    Bug67638Test.class,
    
    // Overall bug tests.
    Bug6825Test.class,
    Bug9209Test.class,
    DistributionListExportTest.class,

})
public final class ImportExportServerSuite {
    // empty
}
