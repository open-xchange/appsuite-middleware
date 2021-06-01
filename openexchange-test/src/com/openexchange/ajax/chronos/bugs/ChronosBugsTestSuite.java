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

package com.openexchange.ajax.chronos.bugs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link ChronosBugsTestSuite}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    // @formatter:off
    Bug10154Test.class,
    Bug10733Test.class,
    Bug10836Test.class,
    Bug11250Test.class,
    Bug12099Test.class,
    Bug12432Test.class,
    Bug12444Test.class,
    Bug12610Test.class,
    Bug12842Test.class,
    Bug13090Test.class,
    Bug13214Test.class,
    Bug13447Test.class,
    Bug13501Test.class,
    Bug13505Test.class,
    Bug13625Test.class,
    Bug13788Test.class,
    Bug13942Test.class,
    Bug14357Test.class,
    Bug14679Test.class,
    Bug15074Test.class,
    Bug15585Test.class,
    Bug58814Test.class,
    Bug64836Test.class,
    Bug66144Test.class,
    Bug68699Test.class,
    MWB2Test.class,
    MWB104Test.class,
    MWB864Test.class,
    MWB999Test.class,
    MWB1014Test.class,
    MWB1040Test.class,
    MWB1077Test.class,
    // @formatter:on
})
public class ChronosBugsTestSuite {

}
