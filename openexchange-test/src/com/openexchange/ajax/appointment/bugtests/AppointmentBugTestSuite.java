/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    Bug16194Test.class,
    Bug16211Test.class,
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
    Bug18455Test.class,
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
    Bug33697Test.class,
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
    Bug56589Test.class

})
public class AppointmentBugTestSuite {

}
