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

package com.openexchange.contact.vcard;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * {@link UnitTests}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AddressTest.class,
    DistributionListTest.class,
    BasicTest.class,
    RoundtripTest.class,
    UpdateTest.class,
    WarningsTest.class,
    ColorLabelTest.class,
    EmptyTest.class,
    ImportIteratorTest.class,
    RemoveImageTest.class,
    Bug13557Test.class,
    Bug14349Test.class,
    Bug14350Test.class,
    Bug15008Test.class,
    Bug15229Test.class,
    Bug15241Test.class,
    Bug18226Test.class,
    Bug21656Test.class,
    Bug55090Test.class,
    Bug6823Test.class,
    Bug6962Test.class,
    Bug7106Test.class,
    Bug7248Test.class,
    Bug7249Test.class,
    Bug7250Test.class,
    Bug7719Test.class,
    MWB768Test.class,
    MWB1133Test.class,
})
public class UnitTests {

}
