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

import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVExportRequest;
import com.openexchange.ajax.importexport.actions.CSVExportResponse;
import com.openexchange.ajax.importexport.actions.VCardExportRequest;
import com.openexchange.ajax.importexport.actions.VCardExportResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.importexport.csv.CSVParser;

public class DistributionListExportTest extends AbstractManagedContactTest {

	public DistributionListExportTest(String name) {
		super(name);
	}

	public void testCsvDistributionListsAreExported () throws OXException, IOException, JSONException {
		Contact list = generateContact("Distribution list");
		list.setDistributionList( new DistributionListEntryObject[]{
				new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
		});
		manager.newAction(list);
		CSVExportResponse csvExportResponse = client.execute(new CSVExportRequest(folderID, true));
		String csvStr = (String) csvExportResponse.getData();

		CSVParser csvParser = new CSVParser(csvStr);
		List<List<String>> csv = csvParser.parse();
		assertEquals("Should only contain the header line but no content", 2, csv.size() );
	}

	public void testCsvDistributionListsAreNotExported () throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list");
        list.setDistributionList( new DistributionListEntryObject[]{
                new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        manager.newAction(list);
        CSVExportResponse csvExportResponse = client.execute(new CSVExportRequest(folderID, false));
        String csvStr = (String) csvExportResponse.getData();

        CSVParser csvParser = new CSVParser(csvStr);
        List<List<String>> csv = csvParser.parse();
        assertEquals("Should only contain the header line but no content", 1, csv.size() );
    }

    public void testVCardDistributionListsAreNotExportedByDefault() throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list is not present");
        list.setDistributionList( new DistributionListEntryObject[]{
                new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        manager.newAction(list);
        VCardExportResponse vcardExportResponse = client.execute(new VCardExportRequest(folderID, false));
        String vcard = (String) vcardExportResponse.getData();

        assertFalse("Should not contain name of contact in list", vcard.contains("my displayname"));
        assertFalse("Should not contain e-mail of contact in list", vcard.contains("myemail@adress.invalid"));
        assertFalse("Should not contain name of distribution list", vcard.contains("Distribution list is not present"));

    }

    public void testVCardDistributionListsAreNotExported() throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list is not present");
        list.setDistributionList( new DistributionListEntryObject[]{
                new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        manager.newAction(list);
        VCardExportResponse vcardExportResponse = client.execute(new VCardExportRequest(folderID, Boolean.FALSE, false));
        String vcard = (String) vcardExportResponse.getData();

        assertFalse("Should not contain name of contact in list", vcard.contains("my displayname"));
        assertFalse("Should not contain e-mail of contact in list", vcard.contains("myemail@adress.invalid"));
        assertFalse("Should not contain name of distribution list", vcard.contains("Distribution list is not present"));

    }

    public void testVCardDistributionListsAreExported () throws OXException, IOException, JSONException {
        Contact list = generateContact("Distribution list");
        list.setDistributionList( new DistributionListEntryObject[]{
                new DistributionListEntryObject("my displayname", "myemail@adress.invalid", 0)
        });
        manager.newAction(list);
        VCardExportResponse vcardExportResponse = client.execute(new VCardExportRequest(folderID, Boolean.TRUE, true));
        String vcard = (String) vcardExportResponse.getData();

        assertTrue("Should contain name of contact in list", vcard.contains("my displayname"));
        assertTrue("Should contain e-mail of contact in list", vcard.contains("myemail@adress.invalid"));
        assertTrue("Should contain name of distribution list", vcard.contains("Distribution list"));

    }

}
