/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.importexport.importers;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.importexport.formats.csv.ContactFieldMapper;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link Bug43229Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.8.1
 */
@RunWith(PowerMockRunner.class)
public class Bug43229Test {

    @Mock
    private ServiceLookup serviceLookup;

    private CSVContactImporter csvContactImporter;
    private ContactFieldMapper whmailMapper;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        /*
         * init a set of mappers
         */
        LinkedList<ContactFieldMapper> mappers = new LinkedList<ContactFieldMapper>();
        Properties googleProperties = new Properties();
        googleProperties.load(new StringReader(
            "encoding=UTF-16LE\n" +
            "display_name=Name\n" +
            "first_name=Given Name\n" +
            "second_name=Additional Name\n" +
            "last_name=Family Name\n"
        ));
        mappers.add(new PropertyDrivenMapper(googleProperties, "google.properties"));
        Properties oxProperties = new Properties();
        oxProperties.load(new StringReader(
            "encoding=UTF-8\n" +
            "last_name=Sur name\n" +
            "anniversary=Anniversary\n" +
            "assistant_name=Assistant's name\n" +
            "birthday=Birthday\n" +
            "branches=Branches\n"
        ));
        mappers.add(new PropertyDrivenMapper(oxProperties, "open-xchange.properties"));
        Properties whmailProperties = new Properties();
        whmailProperties.load(new StringReader(
            "encoding=utf8\n" +
            "cellular_telephone1=Mobile phone\n" +
            "company=Company\n" +
            "email1=Email\n" +
            "email2=Email2\n" +
            "email3=Email3\n" +
            "fax_business=Fax\n" +
            "fax_home=Fax (home)\n" +
            "first_name=First name\n" +
            "last_name=Last name\n" +
            "note=Comment\n" +
            "telephone_business1=Work phone\n" +
            "telephone_home1=Home phone\n"
        ));
        whmailMapper = new PropertyDrivenMapper(whmailProperties, "whmail2.en_EN.properties");
        mappers.add(whmailMapper);
        /*
         * prepare contact importer & inject mappers
         */
        csvContactImporter = new CSVContactImporter(serviceLookup);
        MockUtils.injectValueIntoPrivateField(csvContactImporter, "mappers", mappers);
    }

     @Test
     public void testContinueOnParseException() throws Exception {
        byte[] csv = {
            34, 70, 105, 114, 115, 116, 32, 110, 97, 109, 101, 34, 44, 34, 76, 97, 115, 116, 32, 110, 97, 109, 101, 34, 44, 34, 69, 109,
            97, 105, 108, 34, 13, 10, 34, 84, 101, 115, 116, 34, 44, 34, 34, 44, 34, 116, 101, 115, 116, 64, 100, 111, 109, 97, 105, 110,
            46, 116, 108, 100, 34, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
        List<List<String>> parsedValues = csvContactImporter.parse(Streams.newByteArrayInputStream(csv), -1);
        Assert.assertNotNull("Nothing parsed", parsedValues);
        Assert.assertEquals("Unexpected number of rows parsed", 2, parsedValues.size());
        Assert.assertEquals("Unexpected parsed first row", Arrays.asList("First name", "Last name", "Email"), parsedValues.get(0));
        Assert.assertEquals("Unexpected parsed second row", Arrays.asList("Test", "", "test@domain.tld"), parsedValues.get(1));
        ContactFieldMapper mapper = csvContactImporter.getCurrentMapper();
        Assert.assertNotNull("No mapper chosen", mapper);
        Assert.assertEquals("Unexpected mapper chosen", whmailMapper, mapper);
    }

}
