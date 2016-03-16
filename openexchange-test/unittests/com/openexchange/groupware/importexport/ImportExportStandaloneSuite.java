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

package com.openexchange.groupware.importexport;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.openexchange.groupware.importexport.importers.CsvDoesDifferentLanguages;
import com.openexchange.groupware.importexport.mappers.PropertyDrivenMapperTest;

/**
 * This suite is meant for tests without a running OX instance
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class ImportExportStandaloneSuite extends TestSuite {

	public static Test suite(){
		final TestSuite tests = new TestSuite();
		//basics
		tests.addTestSuite( ContactFieldTester.class );
		tests.addTestSuite( ContactSwitcherTester.class );
		tests.addTest( CSVParserTest.suite() );
		//tests.addTestSuite( com.openexchange.groupware.importexport.OXContainerConverterTest.class );
		//tests.addTestSuite( com.openexchange.tools.versit.OXContainerConverterTest.class );
		tests.addTest( SizedInputStreamTest.suite() );

		//CSV
		tests.addTest( CSVContactImportTest.suite() );
		tests.addTest( CSVContactExportTest.suite() );
		tests.addTest( OutlookCSVContactImportTest.suite() );
        tests.addTest(CsvDoesDifferentLanguages.suite());
        tests.addTestSuite(PropertyDrivenMapperTest.class);

		//ICAL
		tests.addTest( ICalImportTest.suite() );

		//separate tests for reported bugs
		tests.addTest( Bug7732Test.suite() );
//		tests.addTest( Bug7470Test.suite() ); //FIXME
		tests.addTest( Bug8475.suite() );
		tests.addTest( Bug8527.suite() );
		tests.addTest( Bug8653.suite() );
		tests.addTest( Bug8654.suite() );
		tests.addTest( Bug8681Suite.suite() );

		return tests;
	}
}
