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

package com.openexchange.dav.caldav.bugs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link CalDAVBugSuite}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(ParallelSuite.class)
@SuiteClasses({ // @formatter:off
    Bug21794Test.class,
    Bug22094Test.class,
    Bug22352Test.class,
    Bug22338Test.class,
    Bug22395Test.class,
    Bug22451Test.class,
    Bug22723Test.class,
    Bug23067Test.class,
    Bug23167Test.class,
    Bug23181Test.class,
    Bug32897Test.class,
    Bug23610Test.class,
    Bug23612Test.class,
    Bug24682Test.class,
    Bug25783Test.class,
    Bug25672Test.class,
    Bug26957Test.class,
    Bug27224Test.class,
    Bug27309Test.class,
    Bug28490Test.class,
    Bug28734Test.class,
    Bug29554Test.class,
    Bug29728Test.class,
    Bug25160Test.class,
    Bug30359Test.class,
    Bug31453Test.class,
    Bug31490Test.class,
    Bug37112Test.class,
    Bug37887Test.class,
    Bug39098Test.class,
    Bug39819Test.class,
    Bug40298Test.class,
    Bug40657Test.class,
    Bug42104Test.class,
    Bug43297Test.class,
    Bug43376Test.class,
    Bug43521Test.class,
    Bug43782Test.class,
    Bug44131Test.class,
    Bug44144Test.class,
    Bug44167Test.class,
    Bug44309Test.class,
    Bug44304Test.class,
    Bug46811Test.class,
    Bug47121Test.class,
    Bug48856Test.class,
    Bug44109Test.class,
    Bug48917Test.class,
    Bug48241Test.class,
    Bug45028Test.class,
    Bug48828Test.class,
    Bug48242Test.class,
    Bug26293Test.class,
    Bug51462Test.class,
    Bug51768Test.class,
    Bug52255Test.class,
    Bug52095Test.class,
    Bug53479Test.class,
    Bug54192Test.class,
    Bug55068Test.class,
    Bug55653Test.class,
    Bug55916Test.class,
    Bug57203Test.class,
    Bug57313Test.class,
    Bug57858Test.class,
    Bug58154Test.class,
    Bug60193Test.class,
    Bug60589Test.class,
    Bug61998Test.class,
    Bug62008Test.class,
    Bug62737Test.class,
    Bug63360Test.class,
    Bug63818Test.class,
    Bug63962Test.class,
    Bug64086Test.class,
    Bug63885Test.class,
    Bug64809Test.class,
    Bug64836Test.class,
    Bug64937Test.class,
    Bug65943Test.class,
    Bug66234Test.class,
    Bug66837Test.class,
    Bug66988Test.class,
    Bug67329Test.class,
    Bug67509Test.class,
    Bug67580Test.class,
    Bug67667Test.class,
    Bug68516Test.class,
    MWB47Test.class,
    MWB456Test.class,
    Bug68847Test.class,
    MWB713Test.class,
    MWB1037Test.class,
    MWB1096Test.class,
}) // @formatter:on
public final class CalDAVBugSuite {

}
