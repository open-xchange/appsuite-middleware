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

package com.openexchange.dav.carddav.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link CardDAVTestSuite} - Testsuite for the CardDAV interface.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(ParallelSuite.class)
@SuiteClasses({ // @formatter:off
    CurrentUserPrincipalTest.class,
    OptionsTest.class,
    CollectionsTest.class,
    PrincipalPropertiesTest.class,
    AddressbookPropertiesTest.class,
    NewTest.class,
    UpdateTest.class,
    DeleteTest.class,
    MoveTest.class,
    UpgradeTest.class,
    ImageTest.class,
    CookieTest.class,
    BasicTest.class,
    BulkImportTest.class,
    AddressbookQueryTest.class,
    AddressbookQueryPartialRetrievalTest.class,
    AddressbookMultigetPartialRetrievalTest.class,
    ImageURITest.class,
    DistListTest.class,
}) // @formatter:on
public final class CardDAVTestSuite {

}
