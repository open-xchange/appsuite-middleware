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

package com.openexchange.ajax.appointment.bugtests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for appointment bug tests.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    Bug13214Test.class,
    Bug13027Test.class,
    Bug13501Test.class,
    Bug13942Test.class,
    Bug13826Test.class,
    Bug13625Test.class,
    Bug13447Test.class,
    Bug13505Test.class,
    Bug13960Test.class,
    Bug12509Test.class,
    Bug14357Test.class,
    Bug13788Test.class,
    Bug14679Test.class,
    Bug15074Test.class,
    Bug15585Test.class,
    Bug15590Test.class,
    Bug15903Test.class,
    Bug15937Test.class,
    Bug15986Test.class,
    Bug16292Test.class,
    Bug16151Test.class,
    Bug16089Test.class,
    Bug16107Test.class,
    Bug16441Test.class,
    Bug16476Test.class,
    Bug16249Test.class,
    Bug16579Test.class,
    Bug17175Test.class,
    Bug17264Test.class,
    Bug17535Test.class,
    Bug18336Test.class,
    Bug13090Test.class,
    Bug17327Test.class,
    Bug18558Test.class,
    Bug19489Test.class,
    Bug19109Test.class,
    //Bug20980Test_DateOnMissingDSTHour.class,
    Bug21264Test.class,
    Bug21614Test.class,
    Bug21620Test.class,
    Bug24502Test.class,
    Bug26842Test.class,
    Bug26350Test.class,
    Bug29268Test.class,
    Bug29133Test.class,
    Bug29146Test.class,
    Bug29566Test.class,
    Bug30118Test.class,
    Bug30142Test.class,
    Bug30414Test.class,
    Bug31810Test.class,
    Bug31779Test.class,
    Bug31963Test.class,
    Bug32278Test.class,
    Bug32385Test.class,
    Bug33242Test.class,
    Bug35610Test.class,
    Bug35687Test.class,
    Bug35355Test.class,
    Bug37198Test.class,
    Bug37668Test.class,
    Bug38079Test.class,
    WeirdRecurrencePatternTest.class, // Is also a bug test. Related to 37668 and 38079.
    Bug39571Test.class,
    Bug38404Test.class,
    Bug41794Test.class,
    Bug42018Test.class,
    Bug41995Test.class,
    Bug42018Test.class,
    Bug42775Test.class,
    Bug44002Test.class,
    Bug47012Test.class,
    Bug48149Test.class,
    Bug48165Test.class,
    Bug51918Test.class,
    Bug53073Test.class,
    Bug55690Test.class,
    Bug56359Test.class,
    Bug56589Test.class,
})
public class AppointmentBugTestSuite {

}
