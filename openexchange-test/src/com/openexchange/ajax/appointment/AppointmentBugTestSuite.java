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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.appointment;

import com.openexchange.ajax.appointment.recurrence.Bug12212Test;
import com.openexchange.ajax.appointment.recurrence.Bug12495Test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for appointment bug tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AppointmentBugTestSuite extends TestSuite{

    private AppointmentBugTestSuite() {
        super();
    }

	/**
	 * @return the suite.
	 */
	public static Test suite() {
		final TestSuite tests = new TestSuite();
		tests.addTestSuite(Bug4392Test.class);
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
        tests.addTestSuite(Bug13214Test.class);
        tests.addTestSuite(Bug13027Test.class);
        tests.addTestSuite(Bug13501Test.class);
        tests.addTestSuite(Bug13942Test.class);
		return tests;
	}
}
