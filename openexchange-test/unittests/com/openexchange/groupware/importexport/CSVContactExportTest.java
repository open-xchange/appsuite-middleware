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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.importexport.importers.TestCSVContactImporter;
import com.openexchange.importexport.exporters.CSVContactExporter;
import com.openexchange.importexport.exporters.Exporter;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.test.OXTestToolkit;
import junit.framework.JUnit4TestAdapter;


public class CSVContactExportTest extends AbstractContactTest {

	public static Exporter exp = new CSVContactExporter();
	public static String TEST1_RESULT =
		"\"Object id\"," +
		"\"Folder id\"," +
		"\"Given name\"\n";
	public static int[] TEST1_BASE = {
		ContactField.OBJECT_ID.getNumber(),
		ContactField.FOLDER_ID.getNumber(),
		ContactField.GIVEN_NAME.getNumber(),
		7000};
	public static String TEST2_RESULT =
		"Given name, " +
		"Email 1\n" +
		"Prinz, tobias.prinz@open-xchange.com\n" +
		"Laguna, francisco.laguna@open-xchange.com";
	public static int[] TEST2_BASE ={
		ContactField.GIVEN_NAME.getNumber(),
		ContactField.EMAIL1.getNumber()};

	public static String TEST_EMPTY_RESULT =
		"Given name, " +
		"Email 1\n" +
		",\n" +
		",";
	public static int[] TEST_EMPTY_BASE ={
		ContactField.GIVEN_NAME.getNumber(),
		ContactField.EMAIL1.getNumber()};


	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CSVContactExportTest.class);
	}

    @Before
    public void TearUp() throws OXException {
        folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "csvContactTestFolder");
    }

	@Test public void canExport() throws OXException, IOException{
		assertTrue(
			"Can export?" ,
			exp.canExport(sessObj, Format.CSV, Integer.toString(folderId), null));
	}

	@Test public void exportHead() throws OXException, IOException{
		final InputStream is = exp.exportData(sessObj, Format.CSV, String.valueOf( folderId ), TEST1_BASE, null);
		assertEquals("Head only", TEST1_RESULT, OXTestToolkit.readStreamAsString(is) );
	}

	@Test public void exportData() throws NumberFormatException, Exception{
		final CSVContactImporter imp = new TestCSVContactImporter();
		InputStream is;

		//importing prior to export test
		is = new ByteArrayInputStream( TEST2_RESULT.getBytes() );
		final Map <String, Integer>folderMappings = new HashMap<String, Integer>();
		folderMappings.put(Integer.toString(folderId), new Integer(Types.CONTACT) );
		final List<ImportResult> results = imp.importData(sessObj, Format.CSV, is, new LinkedList<String>( folderMappings.keySet()), null);

		//exporting and asserting
		is = exp.exportData(sessObj, Format.CSV, String.valueOf( folderId ),TEST2_BASE, null);
		final CSVParser parser = new CSVParser();
		final String resStr = OXTestToolkit.readStreamAsString(is);
		assertEquals("Two imports", parser.parse(TEST2_RESULT), parser.parse(resStr) );

		//cleaning up
		for(final ImportResult res : results){
		    contactStorage.delete(sessObj, res.getFolder(), res.getObjectId(), res.getDate());
		}
	}
}
