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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVExportRequest;
import com.openexchange.ajax.importexport.actions.CSVExportResponse;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.importexport.csv.CSVParser;

/**
 * Tests the CSV imports and exports (rewritten from webdav + servlet to test manager).
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVImportExportServletTest extends AbstractManagedContactTest  {
	String CSV = "Given name,Email 1, Display name\n Prinz, tobias.prinz@open-xchange.com, Tobias Prinz\nLaguna, francisco.laguna@open-xchange.com, Francisco Laguna\n";
	private ContactField field;

	public CSVImportExportServletTest(final String name) {
		super(name);
	}

	public Map<ContactField, Integer> getPositions(List<List<String>> csv){
		HashMap<ContactField, Integer> result = new HashMap<ContactField, Integer>();
		List<String> headers = csv.get(0);
		for(int i = 0; i < headers.size(); i++) {
			field = ContactField.getByDisplayName(headers.get(i));
			if (field != null) {
				result.put(field, i);
			}
		}
		return result;
	}

	public void notestCSVRoundtrip() throws Exception{
		client.execute(new CSVImportRequest(folderID, new ByteArrayInputStream(CSV.getBytes())));
		CSVExportResponse exportResponse = client.execute(new CSVExportRequest(folderID));

		CSVParser parser = new CSVParser();
		List<List<String>> expected = parser.parse(CSV);
		List<List<String>> actual  = parser.parse((String) exportResponse.getData());
		Map<ContactField, Integer> positions = getPositions(actual);

		for(int i = 1; i <= 2; i++) {
			assertEquals("Mismatch of given name in row #"+i, expected.get(i).get(0), actual.get(i).get(positions.get(ContactField.GIVEN_NAME)));
			assertEquals("Mismatch of email 1 in row #"+i, expected.get(i).get(1), actual.get(i).get(positions.get(ContactField.EMAIL1)));
			assertEquals("Mismatch of display name in row #"+i, expected.get(i).get(2), actual.get(i).get(positions.get(ContactField.DISPLAY_NAME)));
		}
	}


	public void testUnknownFile() throws Exception{
		final String insertedCSV = "bla1\nbla2,bla3";

		CSVImportResponse importResponse = client.execute(new CSVImportRequest(folderID, new ByteArrayInputStream(insertedCSV.getBytes()), false));
		assertEquals("Unexpected error code: " + importResponse.getException(), "I_E-0804", importResponse.getException().getErrorCode());
	}

	public void testEmptyFileUploaded() throws Exception{
		final InputStream is = new ByteArrayInputStream("Given name,Email 1, Display name".getBytes());
		CSVImportResponse importResponse = client.execute(new CSVImportRequest(folderID, is, false));
		assertEquals("Unexpected error code: " + importResponse.getException(), "I_E-1315", importResponse.getException().getErrorCode());
	}

	public void notestDoubleImport() throws Exception{
		client.execute(new CSVImportRequest(folderID, new ByteArrayInputStream(CSV.getBytes())));
		client.execute(new CSVImportRequest(folderID, new ByteArrayInputStream(CSV.getBytes())));
		CSVExportResponse exportResponse = client.execute(new CSVExportRequest(folderID));

		CSVParser parser = new CSVParser();
		List<List<String>> expected = parser.parse(CSV);
		assertEquals(3, expected.size());
	}
}
