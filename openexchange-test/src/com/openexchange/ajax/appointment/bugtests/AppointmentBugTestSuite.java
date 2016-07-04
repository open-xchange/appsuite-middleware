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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for appointment bug tests.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AppointmentBugTestSuite extends TestSuite {

    private AppointmentBugTestSuite() {
        super();
    }

    /**
     * @return the suite.
     */
    public static Test suite() {
        final TestSuite tests = new TestSuite();
        /*tests.addTestSuite(Bug4392Test.class);
        tests.addTestSuite(Bug4541Test.class);
        tests.addTestSuite(Bug6055Test.class);
        tests.addTestSuite(Bug8317Test.class);
        tests.addTestSuite(Bug8724Test.class);
        tests.addTestSuite(Bug8836Test.class);
        tests.addTestSuite(Bug9089Test.class);
        tests.addTestSuite(Bug10154Test.class);
        tests.addTestSuite(Bug10733Test.class);
        tests.addTestSuite(Bug10836Test.class);
        tests.addTestSuite(Bug11250Test.class);
        tests.addTestSuite(Bug11865Test.class);
        tests.addTestSuite(Bug12099Test.class);
        tests.addTestSuite(Bug12326Test.class);
        tests.addTestSuite(Bug12372Test.class);
        tests.addTestSuite(Bug12444Test.class);
        tests.addTestSuite(Bug12264Test.class);
        tests.addTestSuite(Bug12463Test.class);
        tests.addTestSuite(Bug12212Test.class);
        tests.addTestSuite(Bug12495Test.class);
        tests.addTestSuite(Bug12610Test.class);
        tests.addTestSuite(Bug12432Test.class);
        tests.addTestSuite(Bug12842Test.class);
        */tests.addTestSuite(Bug13214Test.class);
        tests.addTestSuite(Bug13027Test.class);
        tests.addTestSuite(Bug13501Test.class);
        tests.addTestSuite(Bug13942Test.class);
        tests.addTestSuite(Bug13826Test.class);
        tests.addTestSuite(Bug13625Test.class);
        tests.addTestSuite(Bug13447Test.class);
        tests.addTestSuite(Bug13505Test.class);
        tests.addTestSuite(Bug13960Test.class);
        tests.addTestSuite(Bug12509Test.class);
        tests.addTestSuite(Bug14357Test.class);
        tests.addTestSuite(Bug13788Test.class);
        tests.addTestSuite(Bug14679Test.class);
        tests.addTestSuite(Bug15074Test.class);
        tests.addTestSuite(Bug15585Test.class);
        tests.addTestSuite(Bug15590Test.class);
        tests.addTestSuite(Bug15903Test.class);
        tests.addTestSuite(Bug15937Test.class);
        tests.addTestSuite(Bug15986Test.class);
        tests.addTestSuite(Bug16292Test.class);
        tests.addTestSuite(Bug16151Test.class);
        tests.addTestSuite(Bug16194Test.class);
        tests.addTestSuite(Bug16211Test.class);
        tests.addTestSuite(Bug16089Test.class);
        tests.addTestSuite(Bug16107Test.class);
        tests.addTestSuite(Bug16441Test.class);
        tests.addTestSuite(Bug16476Test.class);
        tests.addTestSuite(Bug16249Test.class);
        tests.addTestSuite(Bug16579Test.class);
        tests.addTestSuite(Bug17175Test.class);
        tests.addTestSuite(Bug17264Test.class);
        tests.addTestSuite(Bug17535Test.class);
        tests.addTestSuite(Bug18336Test.class);
        tests.addTestSuite(Bug13090Test.class);
        tests.addTestSuite(Bug17327Test.class);
        tests.addTestSuite(Bug18455Test.class);
        tests.addTestSuite(Bug18558Test.class);
        tests.addTestSuite(Bug19489Test.class);
        tests.addTestSuite(Bug19109Test.class);
        //tests.addTestSuite(Bug20980Test_DateOnMissingDSTHour.class);
        tests.addTestSuite(Bug21264Test.class);
        tests.addTestSuite(Bug21614Test.class);
        tests.addTestSuite(Bug21620Test.class);
        tests.addTestSuite(Bug24502Test.class);
        tests.addTestSuite(Bug26842Test.class);
        tests.addTestSuite(Bug26350Test.class);
        tests.addTestSuite(Bug29268Test.class);
        tests.addTestSuite(Bug29133Test.class);
        tests.addTestSuite(Bug29146Test.class);
        tests.addTestSuite(Bug29566Test.class);
        tests.addTestSuite(Bug30118Test.class);
        tests.addTestSuite(Bug30142Test.class);
        tests.addTestSuite(Bug30414Test.class);
        tests.addTestSuite(Bug31810Test.class);
        tests.addTestSuite(Bug31779Test.class);
        tests.addTestSuite(Bug31963Test.class);
        tests.addTestSuite(Bug32278Test.class);
        tests.addTestSuite(Bug32385Test.class);
        tests.addTestSuite(Bug33242Test.class);
        tests.addTestSuite(Bug33697Test.class);
        tests.addTestSuite(Bug35610Test.class);
        tests.addTestSuite(Bug35687Test.class);
        tests.addTestSuite(Bug35355Test.class);
        tests.addTestSuite(Bug37198Test.class);
        tests.addTestSuite(Bug37668Test.class);
        tests.addTestSuite(Bug38079Test.class);
        tests.addTestSuite(WeirdRecurrencePatternTest.class); // Is also a bug test. Related to 37668 and 38079.
        tests.addTestSuite(Bug39571Test.class);
        tests.addTestSuite(Bug38404Test.class);
        tests.addTestSuite(Bug41794Test.class);
        tests.addTestSuite(Bug42018Test.class);
        tests.addTestSuite(Bug41995Test.class);
        tests.addTestSuite(Bug42018Test.class);
        tests.addTestSuite(Bug42775Test.class);
        tests.addTestSuite(Bug44002Test.class);
        tests.addTestSuite(Bug47012Test.class);
        return tests;
    }
}
