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

package com.openexchange.importexport.importers;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link Bug54797}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
@RunWith(PowerMockRunner.class)
public class Bug54797 {
    
    private CSVContactImporter csvContactImporter;
    
    @Mock
    private ServiceLookup serviceLookup;
    
    private int day = 23;
    private int month = 6;
    private int year = 1974;
    
    private final boolean[] atLeastOneFieldInserted = new boolean[10];
    
    private ImportResult result;
    
    private Locale[] locales;
    
    private final List<String> fields = new LinkedList<String>(Arrays.asList(new String[] {
        "First Name","Middle Name","Last Name","Title","Suffix","Initials","Web Page","Gender","Birthday","Anniversary","Location","Language","Internet Free Busy","Notes","E-mail Address","E-mail 2 Address",
        "E-mail 3 Address","Primary Phone","Home Phone","Home Phone 2","Mobile Phone","Pager","Home Fax","Home Address","Home Street","Home Street 2","Home Street 3","Home Address PO Box","Home City",
        "Home State","Home Postal Code","Home Country","Spouse","Children","Manager's Name","Assistant's Name","Referred By","Company Main Phone","Business Phone","Business Phone 2","Business Fax",
        "Assistant's Phone","Company","Job Title","Department","Office Location","Organizational ID Number","Profession","Account","Business Address","Business Street","Business Street 2","Business Street 3",
        "Business Address PO Box","Business City","Business State","Business Postal Code","Business Country","Other Phone","Other Fax","Other Address","Other Street","Other Street 2","Other Street 3","Other Address PO Box",
        "Other City","Other State","Other Postal Code","Other Country","Callback","Car Phone","ISDN","Radio Phone","TTY/TDD Phone","Telex","User 1","User 2","User 3","User 4","Keywords","Mileage","Hobby","Billing Information","Directory Server",
        "Sensitivity","Priority","Private","Categories"
    }));
    
    private final List<String> csvIt = new LinkedList<String>(Arrays.asList(new String[] {
        "","","","","","","","",day+"/"+month+"/"+year,"","","","","","videomail@libero.it","","","","","","","","","","",
        "","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",
        "","","","","","","","","","","","","","","","","","","","","Normal","","Importato 19/07/17"
    }));
    
    private final List<String> csvUs = new LinkedList<String>(Arrays.asList(new String[] {
        "","","","","","","","",month+"/"+day+"/"+year,"","","","","","videomail@libero.it","","","","","","","","","","",
        "","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",
        "","","","","","","","","","","","","","","","","","","","","Normal","","Importato 19/07/17"
    }));
    
    private final Properties properties = new Properties();
    
    private final String propertiesString = "encoding=cp1252\n"
        +"anniversary=Anniversary\n"
        +"assistant_name=Assistant's Name\n"
        +"birthday=Birthday\n"
        +"categories=Categories\n"
        +"cellular_telephone1=Mobile Phone\n"
        +"city_business=Business City\n"
        +"city_home=Home City\n"
        +"city_other=Other City\n"
        +"commercial_register=Organizational ID Number\n"
        +"company=Company\n"
        +"country_business=Business Country\n"
        +"country_home=Home Country\n"
        +"country_other=Other Country\n"
        +"department=Department\n"
        +"email1=E-mail Address\n"
        +"email2=E-mail 2 Address\n"
        +"email3=E-mail 3 Address\n"
        +"fax_business=Business Fax\n"
        +"fax_home=Home Fax\n"
        +"fax_other=Other Fax\n"
        +"first_name=First Name\n"
        +"last_name=Last Name\n"
        +"manager_name=Manager's Name\n"
        +"note=Notes\n"
        +"number_of_children=Children\n"
        +"position=Job Title\n"
        +"postal_code_business=Business Postal Code\n"
        +"postal_code_home=Home Postal Code\n"
        +"postal_code_other=Other Postal Code\n"
        +"private_flag=Sensitivity\n"
        +"profession=Profession\n"
        +"second_name=Middle Name\n"
        +"spouse_name=Spouse\n"
        +"state_business=Business State\n"
        +"state_home=Home State\n"
        +"state_other=Other State\n"
        +"street_business=Business Street\n"
        +"street_other=Other Street\n"
        +"suffix=Suffix\n"
        +"telephone_assistant=Assistant's Phone\n"
        +"telephone_business1=Business Phone\n"
        +"telephone_business2=Business Phone 2\n"
        +"telephone_callback=Callback\n"
        +"telephone_car=Car Phone\n"
        +"telephone_company=Company Main Phone\n"
        +"telephone_home1=Home Phone\n"
        +"telephone_home2=Home Phone 2\n"
        +"telephone_isdn=ISDN\n"
        +"telephone_other=Other Phone\n"
        +"telephone_pager=Pager\n"
        +"telephone_primary=Primary Phone\n"
        +"telephone_radio=Radio Phone\n"
        +"telephone_telex=Telex\n"
        +"telephone_ttytdd=TTY/TDD Phone\n"
        +"title=Title\n"
        +"url=Web Page\n";
        
    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);

        Arrays.fill(atLeastOneFieldInserted, false);
        
        csvContactImporter = new CSVContactImporter(serviceLookup);
        
        locales = SimpleDateFormat.getAvailableLocales();
        
        result = new ImportResult();
        result.setFolder("27944");
        
        properties.load(new StringReader(propertiesString));
        
        MockUtils.injectValueIntoPrivateField(csvContactImporter, "currentMapper", new PropertyDrivenMapper(properties, "test.properties"));
    }    
    
    @Test
    public void testLocalizedCsvImportForbirthdayField() throws OXException {
        for (int i = 0; i < locales.length; i++) {     
            Calendar c = Calendar.getInstance();
            c.set(year, month-1, day, 1, 0, 0);
            ContactSwitcher contactSwitcher = csvContactImporter.getContactSwitcher(locales[i]);
            
            Contact contact = csvContactImporter.convertCsvToContact(fields, csvIt, contactSwitcher, 1, result, atLeastOneFieldInserted);           
            assertEquals(c.getTime().toString(), contact.getBirthday().toString());
            
            contact = csvContactImporter.convertCsvToContact(fields, csvUs, contactSwitcher, 1, result, atLeastOneFieldInserted);           
            assertEquals(c.getTime().toString(), contact.getBirthday().toString());
        }
    }
    

}
