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

package com.openexchange.dav.carddav.bugs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link CardDAVBugSuite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(ParallelSuite.class)
@SuiteClasses({ // @formatter:off
    Bug20665Test.class,
    Bug21079Test.class,
    Bug21177Test.class,
    Bug21235Test.class,
    Bug21240Test.class,
    Bug21354Test.class,
    Bug21374Test.class,
    Bug23046Test.class,
    Bug23078Test.class,
    Bug28672Test.class,
    Bug30449Test.class,
    Bug38550Test.class,
    Bug40471Test.class,
    Bug46641Test.class,
    Bug47921Test.class,
    Bug48661Test.class,
    Bug48687Test.class,
    Bug48463Test.class,
    Bug54026Test.class,
    Bug58220Test.class,
    Bug61859Test.class,
    //Bug61873Test.class Disabled as long as the bug is not fixed (See also MW-1166)
    Bug68510Test.class,
    MWB346Test.class,
    MWB459Test.class,
    MWB661Test.class,
    MWB833Test.class,
    MWB915Test.class,
    MWB1024Test.class,
}) // @formatter:on
public final class CardDAVBugSuite {

}
