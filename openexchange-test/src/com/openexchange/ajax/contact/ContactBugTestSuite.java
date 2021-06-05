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

package com.openexchange.ajax.contact;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    Bug4409Test.class,
    Bug6335Test.class,
    Bug12716Test.class,
    Bug13931Test.class,
    Bug13960Test.class,
    Bug15317Test.class,
    Bug15315Test.class,
    Bug15937Test.class,
    Bug16515Test.class,
    Bug16618Test.class,
    Bug17513Test.class,
    Bug13915FileAsViaJSON.class,
    Bug18608Test_SpecialCharsInEmailTest.class,
    Bug19827Test.class,
    Bug25300Test.class,
    Bug28185Test.class,
    Bug31993Test.class,
    Bug34075Test.class,
    Bug32635Test.class,
    Bug35059Test.class,
    Bug42225Test.class,
    Bug46654Test.class,
    Bug55703Test.class,
})
public final class ContactBugTestSuite  {

}
