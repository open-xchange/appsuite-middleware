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

package com.openexchange.groupware.importexport.importers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.Expectations;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractContactTest;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.server.services.ServerServiceRegistry;
import junit.framework.JUnit4TestAdapter;


/**
 * Part of bugfix 15231
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CsvDoesDifferentLanguages extends AbstractContactTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CsvDoesDifferentLanguages.class);
    }

    @Before
    public void TearUp() throws OXException {
        folderId = createTestFolder(FolderObject.CONTACT, sessObj, ctx, "csvContactTestFolder");
    }

    private final String dutch =
        "Voornaam,Achternaam,Weergavenaam,Bijnaam," +
        "Eerste e-mail,Tweede e-mail,Telefoon werk,Telefoon thuis,Faxnummer,Piepernummer,Mobiel nummer," +
        "Adres,Adres 2,Woonplaats,Provincie,Postcode,Land," +
        "Werkadres,Werkadres 2,Werkplaats,Werkprovincie,Werkpostcode,Werkland," +
        "Werktitel,Afdeling,Organisatie," +
        "Webpagina 1,Webpagina 2," +
        "Geboortejaar,Geboortemaand,Geboortedag," +
        "Overig 1,Overig 2,Overig 3,Overig 4,Aantekeningen,\n"
        +
        "Vorname1,Nachname1,,," +
        "email1@open-xchange.com,email2@open-xchange.com,phone_work1,phone_home1,fax,beeper,mobile," +
        "home_street1,home_street2,home_city,home_state,555,home_country," +
        "business_street1,business_street2,business_city,business_state,666,business_country," +
        "job_title,department,company," +
        "website1,website2," +
        "1981,2,1," +
        "add1,add2,add3,add4,notes,\n";

    private CalendarCollectionService oldInstance;

    private void assertBasicFields(String message, Contact c) {
        Expectations expectations = new Expectations();
        expectations.put(Contact.GIVEN_NAME, "Vorname1");
        expectations.put(Contact.SUR_NAME, "Nachname1");
        expectations.put(Contact.EMAIL1, "email1@open-xchange.com");
        expectations.put(Contact.EMAIL2, "email2@open-xchange.com");
        expectations.put(Contact.TELEPHONE_BUSINESS1, "phone_work1");
        expectations.put(Contact.TELEPHONE_HOME1, "phone_home1");
        expectations.put(Contact.STREET_HOME, "home_street1");
        expectations.put(Contact.CITY_HOME, "home_city");
        expectations.put(Contact.STATE_HOME, "home_state");
        expectations.put(Contact.COUNTRY_HOME, "home_country");
        expectations.put(Contact.POSTAL_CODE_HOME, "555");

        expectations.put(Contact.COMPANY, "company");
        expectations.put(Contact.DEPARTMENT, "department");
        expectations.put(Contact.STREET_BUSINESS, "business_street1");
        expectations.put(Contact.CITY_BUSINESS, "business_city");
        expectations.put(Contact.STATE_BUSINESS, "business_state");
        expectations.put(Contact.COUNTRY_BUSINESS, "business_country");
        expectations.put(Contact.POSTAL_CODE_BUSINESS, "666");

        expectations.put(Contact.URL, "website1");
        expectations.put(Contact.NOTE, "notes");
        expectations.verify(message, c);
    }

    @Test
    public void testBug15231WithDutch() throws Throwable{
        Contact c = makeContact(dutch);
        assertBasicFields("Dutch", c);
    }


    @Before
    public void setUp() throws Exception {
        AbstractContactTest.initialize();
        Class<CalendarCollectionService> myClass = CalendarCollectionService.class;

        this.oldInstance = ServerServiceRegistry.getInstance().getService(myClass);
        ServerServiceRegistry.getInstance().addService(myClass, new MockCalendarCollectionService());
    }

    @After
    public void tearDown() throws Exception {
        if(oldInstance != null) {
            ServerServiceRegistry.getInstance().addService(CalendarCollectionService.class, oldInstance);
        }
    }

    private Contact makeContact(String csv) throws Throwable {
        CSVParser parser = new CSVParser();
        List<List<String>> list = parser.parse(csv);
        assertEquals("Should have one header + one one data row",2, list.size());

        List<String> header = list.get(0);
        List<String> data = list.get(1);

        CSVContactImporter importer = new TestCSVContactImporter();

        boolean[] atLeastOneFieldInserted = new boolean[]{false};
        ContactSwitcher conSet = importer.getContactSwitcher();
        ImportResult result = new ImportResult();

        assertTrue("The importer should consider itself", importer.checkFields(header));
        return importer.convertCsvToContact(header, data, conSet, 1, result , atLeastOneFieldInserted );
    }

}
