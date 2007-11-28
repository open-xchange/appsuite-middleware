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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONObject;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.test.OXTestToolkit;

/**
 * Tests the CSV imports and exports by using the servlets.
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVImportExportServletTest extends AbstractImportExportServletTest {

	
	public CSVImportExportServletTest(String name) {
		super(name);
	}

	public void testCSVRoundtrip() throws Exception{
		//preparations
		final String insertedCSV = IMPORTED_CSV;
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		try {
		
			//test: import
			InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
			WebConversation webconv = getWebConversation();
			WebRequest req = new PostMethodWebRequest(
					getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
					);
			((PostMethodWebRequest)req).setMimeEncoded(true);
			req.selectFile("file", "contacts.csv", is, format.getMimeType());
			WebResponse webRes = webconv.getResource(req);
			
			JSONObject response = extractFromCallback( webRes.getText() );
			
			//test: export
			webconv =  getWebConversation();
			req = new GetMethodWebRequest( getCSVColumnUrl(EXPORT_SERVLET, folderId, format) );
			webRes = webconv.sendRequest(req);
			is = webRes.getInputStream();
			String resultingCSV = OXTestToolkit.readStreamAsString(is);
			//finally: checking
			CSVParser parser1 = new CSVParser(insertedCSV);
			CSVParser parser2 = new CSVParser(resultingCSV);
			List<List<String>> res1 = parser1.parse();
			List<List<String>> res2 = parser2.parse();
			assertEquals("input == output ? "+res1+" "+res2 , res1, res2);
		} finally {
			//clean up
			removeFolder(folderId);
		}
	}

	public void testCSVBrokenFile() throws Exception{
		//preparations
		final String insertedCSV = "bla1\nbla2,bla3";
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		try {
			//test: import
			InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
			WebConversation webconv = getWebConversation();
			WebRequest req = new PostMethodWebRequest(
					getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
					);
			((PostMethodWebRequest)req).setMimeEncoded(true);
			req.selectFile("file", "contacts.csv", is, format.getMimeType());
			WebResponse webRes = webconv.getResource(req);
			JSONObject response = extractFromCallback( webRes.getText() );
			assertEquals("Must contain error.", "I_E-1000", response.optString("code"));
		} finally {
			removeFolder(folderId);
		}
	}
	
	public void testUnknownCSVFormat() throws Exception{
		//preparations
		final String insertedCSV = "bla\nbla\nbla";
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-contact-roundtrip-" + System.currentTimeMillis(),FolderObject.CONTACT);
		
		try {
			//test: import
			InputStream is = new ByteArrayInputStream(insertedCSV.getBytes());
			WebConversation webconv = getWebConversation();
			WebRequest req = new PostMethodWebRequest(
					getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
					);
			((PostMethodWebRequest)req).setMimeEncoded(true);
			req.selectFile("file", "contacts.csv", is, format.getMimeType());
			WebResponse webRes = webconv.getResource(req);
			JSONObject response = extractFromCallback( webRes.getText() );
			assertEquals("Must contain error ", "I_E-0804", response.optString("code"));
		} finally {
			removeFolder(folderId);
		}
	}
	
	public void testEmptyFileUploaded() throws Exception{
		InputStream is = new ByteArrayInputStream("".getBytes());
		WebConversation webconv = getWebConversation();
		final Format format = Format.CSV;
		final int folderId = createFolder("csv-empty-file-" + System.currentTimeMillis(),FolderObject.CONTACT);
		try {
			WebRequest req = new PostMethodWebRequest(
					getCSVColumnUrl(IMPORT_SERVLET, folderId, format)
					);
			((PostMethodWebRequest)req).setMimeEncoded(true);
			req.selectFile("file", "empty.vcs", is, format.getMimeType());
			WebResponse webRes = webconv.getResource(req);
			JSONObject response = extractFromCallback( webRes.getText() );
			assertEquals("Must contain error ", "I_E-1303", response.optString("code"));
		} finally {
			removeFolder(folderId);
		}
	}
}
