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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * {@link GoogleContactsTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
@PowerMockIgnore({"javax.imageio.*"})
public class GoogleContactsTest extends AbstractGoogleTest {

    public void testContacts() throws Exception {
        try {
            LinkedList<Contact> contacts = getGoogleContacts();

            assertFalse("Received an empty contact list", contacts.isEmpty());

            //represents a full supported contact mapping
            final String testAccount1 = "Herr Paul Müller Van Dyk Ende";
            boolean testAccount1Success = false;

            //represents a test account with private information only
            final String testAccount2 = "Maria Meier";
            boolean testAccount2Success = false;

            //represents a test account with no primary mail information only
            final String testAccount3 = "No Primary Mail";
            boolean testAccount3Success = false;

            for(Contact c : contacts) {
                if(c.getDisplayName() != null)
                    if(c.getDisplayName().equals(testAccount1)) {
                        //name
                        assertNotNullAndEquals("given name", "Paul", c.getGivenName());
                        assertNotNullAndEquals("middle name", "Müller", c.getMiddleName());
                        assertNotNullAndEquals("surname", "Van Dyk", c.getSurName());
                        assertNotNullAndEquals("title", "Herr", c.getTitle());
                        assertNotNullAndEquals("suffix", "Ende", c.getSuffix());

                        //email
                        assertNotNullAndEquals("email1", "business@example.com", c.getEmail1());
                        assertNotNullAndEquals("email2", "privat@example.com", c.getEmail2());
                        assertNotNullAndEquals("email3", "other@example.com", c.getEmail3());

                        //organisation
                        assertNotNullAndEquals("company", "OX", c.getCompany());
                        assertNotNullAndEquals("job title", "DJ", c.getPosition());

                        assertNotNullAndEquals("telephone business 1", "+4913371337133709", c.getTelephoneBusiness1());
                        assertNotNullAndEquals("telephone home 1", "+4913371337133702", c.getTelephoneHome1());
                        assertNotNullAndEquals("telephone other", "+4913371337133708", c.getTelephoneOther());
                        assertNotNullAndEquals("fax business", "+4913371337133704", c.getFaxBusiness());
                        assertNotNullAndEquals("fax home", "+4913371337133705", c.getFaxHome());
                        assertNotNullAndEquals("cellular telephone 1", "+4913371337133701", c.getCellularTelephone1());
                        assertNotNullAndEquals("cellular telephone 2", "+4913371337133710", c.getCellularTelephone2());

                        Calendar cal = Calendar.getInstance(TimeZones.UTC);
                        cal.clear();
                        cal.set(Calendar.DAY_OF_MONTH, 21);
                        cal.set(Calendar.MONTH, 8 - 1);
                        cal.set(Calendar.YEAR, 1967);
                        assertNotNullAndEquals("birthday", cal.getTime(), c.getBirthday());

                        //location
                        assertNotNullAndEquals("street business", "Paulstr. 85", c.getStreetBusiness());
                        assertNotNullAndEquals("postal code business", "57462", c.getPostalCodeBusiness());
                        assertNotNullAndEquals("city business", "Olpe", c.getCityBusiness());
                        assertNotNullAndEquals("country business", "Deutschland", c.getCountryBusiness());
                        assertNotNullAndEquals("street home", "Dykstr. 47", c.getStreetHome());
                        assertNotNullAndEquals("postal code home", "54260", c.getPostalCodeHome());
                        assertNotNullAndEquals("city home", "Utzutz", c.getCityHome());
                        assertNotNullAndEquals("country home", "Deutschland", c.getCountryHome());
                        assertNotNullAndEquals("street other", "Otherstreet 39", c.getStreetOther());
                        assertNotNullAndEquals("postal code other", "26723", c.getPostalCodeOther());
                        assertNotNullAndEquals("city other", "Emden", c.getCityOther());
                        assertNotNullAndEquals("country other", "Deutschland", c.getCountryOther());

                        assertNotNullAndEquals("instant messenger", "dyksenexample458@example.com (SKYPE)", c.getInstantMessenger1());

                        final String expectedImage = Arrays.toString(getBytesOfPaulsImage());
                        final String actualImage = Arrays.toString(c.getImage1());

                        assertEquals("Image does not equals", expectedImage, actualImage);
                        assertNotNullAndEquals("content type", "image/jpeg", c.getImageContentType());

                        testAccount1Success = true;
                    }
                    else if(c.getDisplayName().equals(testAccount2)) {
                        assertNotNullAndEquals("given name", "Maria", c.getGivenName());
                        assertNotNullAndEquals("surname", "Meier", c.getSurName());
                        assertFieldIsNull("title", c.getTitle());
                        assertFieldIsNull("middle name", c.getMiddleName());
                        assertFieldIsNull("suffix", c.getSuffix());

                        //email
                        assertNotNullAndEquals("email1", "mariameier@example.com", c.getEmail1());
                        //TODO: currently here will apper the mariameier@example again, because it is the primary mail and home mail...
                        assertFieldIsNull("email2", c.getEmail2());
                        assertFieldIsNull("email3", c.getEmail3());

                        //organisation
                        assertFieldIsNull("company", c.getCompany());
                        assertFieldIsNull("job title", c.getPosition());

                        assertFieldIsNull("telephone business 1", c.getTelephoneBusiness1());
                        assertNotNullAndEquals("cellular telephone 1", "+4213371337133701", c.getTelephoneHome1());
                        assertFieldIsNull("telephone other", c.getTelephoneOther());
                        assertFieldIsNull("fax business", c.getFaxBusiness());
                        assertFieldIsNull("fax home",  c.getFaxHome());
                        assertFieldIsNull("cellular telephone 1", c.getCellularTelephone1());
                        assertFieldIsNull("cellular telephone 2", c.getCellularTelephone2());

                        assertFieldIsNull("birthday", c.getBirthday());

                        //location
                        assertFieldIsNull("street business", c.getStreetBusiness());
                        assertFieldIsNull("postal code business", c.getPostalCodeBusiness());
                        assertFieldIsNull("city business", c.getCityBusiness());
                        assertFieldIsNull("country business", c.getCountryBusiness());
                        assertFieldIsNull("street home", c.getStreetHome());
                        assertFieldIsNull("postal code home", c.getPostalCodeHome());
                        assertFieldIsNull("city home", c.getCityHome());
                        assertFieldIsNull("country home", c.getCountryHome());
                        assertFieldIsNull("street other", c.getStreetOther());
                        assertFieldIsNull("postal code other", c.getPostalCodeOther());
                        assertFieldIsNull("city other", c.getCityOther());
                        assertFieldIsNull("country other", c.getCountryOther());

                        assertFieldIsNull("instant messenger", c.getInstantMessenger1());

                        assertFieldIsNull("Image does not equals", c.getImage1());
                        assertFieldIsNull("content type", c.getImageContentType());
                        testAccount2Success = true;
                    } else if(c.getDisplayName().equals(testAccount3)) {
                        assertNotNullAndEquals("given name", "No", c.getGivenName());
                        assertNotNullAndEquals("surname", "Primary", c.getSurName());
                        assertFieldIsNull("title", c.getTitle());
                        assertFieldIsNull("middle name", c.getMiddleName());
                        assertNotNullAndEquals("suffix", "Mail", c.getSuffix());

                        //email
                        assertNotNullAndEquals("email1",  "noprimarybuthomeaddress@example.com", c.getEmail1());
                        assertFieldIsNull("email2", c.getEmail2());
                        assertFieldIsNull("email3", c.getEmail3());

                        //organisation
                        assertFieldIsNull("company", c.getCompany());
                        assertFieldIsNull("job title", c.getPosition());

                        assertFieldIsNull("telephone business 1", c.getTelephoneBusiness1());
                        assertFieldIsNull("cellular telephone 1", c.getTelephoneHome1());
                        assertFieldIsNull("telephone other", c.getTelephoneOther());
                        assertFieldIsNull("fax business", c.getFaxBusiness());
                        assertFieldIsNull("fax home",  c.getFaxHome());
                        assertFieldIsNull("cellular telephone 1", c.getCellularTelephone1());
                        assertFieldIsNull("cellular telephone 2", c.getCellularTelephone2());

                        assertFieldIsNull("birthday", c.getBirthday());

                        //location
                        assertFieldIsNull("street business", c.getStreetBusiness());
                        assertFieldIsNull("postal code business", c.getPostalCodeBusiness());
                        assertFieldIsNull("city business", c.getCityBusiness());
                        assertFieldIsNull("country business", c.getCountryBusiness());
                        assertFieldIsNull("street home", c.getStreetHome());
                        assertFieldIsNull("postal code home", c.getPostalCodeHome());
                        assertFieldIsNull("city home", c.getCityHome());
                        assertFieldIsNull("country home", c.getCountryHome());
                        assertFieldIsNull("street other", c.getStreetOther());
                        assertFieldIsNull("postal code other", c.getPostalCodeOther());
                        assertFieldIsNull("city other", c.getCityOther());
                        assertFieldIsNull("country other", c.getCountryOther());

                        assertFieldIsNull("instant messenger", c.getInstantMessenger1());

                        assertFieldIsNull("Image does not equals", c.getImage1());
                        assertFieldIsNull("content type", c.getImageContentType());
                        testAccount2Success = true;
                    }

                if(testAccount1Success && testAccount2Success && testAccount3Success) {
                    return;
                }
            }
            assertTrue("Could not find: " + testAccount1 ,testAccount1Success);
            assertTrue("Could not find: " + testAccount2 ,testAccount2Success);
            assertTrue("Could not find: " + testAccount3 ,testAccount3Success);
        } catch (OXException e) {
            assertFalse(e.getMessage(), true);
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    private byte[] getBytesOfPaulsImage(){
        return new byte[]{ -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, -1, -37, 0, -124, 0, 5, 3, 4, 8, 8, 7, 8,
            8, 8, 8, 8, 8, 8, 8, 7, 7, 7, 8, 7, 7, 8, 8, 7, 8, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 10, 16, 12, 7, 8, 14, 9, 7, 7, 12, 21, 12,
            14, 17, 17, 31, 19, 31, 7, 12, 22, 24, 22, 30, 24, 16, 30, 31, 18, 1, 5, 5, 5, 8, 7, 8, 15, 9, 8, 14, 18, 12, 12, 13, 20, 20, 18,
            18, 18, 18, 20, 18, 18, 20, 18, 18, 18, 18, 30, 18, 18, 18, 20, 30, 18, 18, 30, 18, 18, 20, 18, 20, 30, 30, 30, 18, 30, 30, 30, 30,
            20, 30, 20, 30, 20, 20, 20, 30, 30, 30, 30, 20, 22, 20, -1, -64, 0, 17, 8, 0, 96, 0, 96, 3, 1, 34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0,
            29, 0, 1, 1, 0, 3, 1, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 8, 9, 3, 4, 6, 7, 5, -1, -60, 0, 48, 16, 0, 2, 0, 2, 7, 7, 3, 3, 5, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 2, 17, 3, 5, 19, 24, 81, -111, -47, 8, 18, 33, 83, 97, 98, -110, 4, 49, 50, 6, 65, 113, 7, 20, 66, 67, -95, -1,
            -60, 0, 26, 1, 0, 3, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3,
                4, 6, 5, -1, -60, 0, 32, 17, 1, 0,
            2, 1, 4, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 17, 3, 2, 18, 33, 49, 4, 5, 6, 34, 65, -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17,
            0, 63, 0, -37, -71, -119, -128, 10, 38, 38, 3, 2, 38, 71, 16, 64, 82, 46, -119, -119, -112, 5, -120, -117, 89, -106, 100, 3,
            85, 65, 49, 48, 0, 76, 66, -127, 34, 72, 9, 67, 36, -117, 32, 36, 64, -123, -112, 20, -124, 40, 66, 100, -114, -98, 116, -111,
            -56, -112, -57, -61, -120, -115, 113, -103, -116, 114, 98, -74, 45, 90, -106, 26, 94, 39, -90, -15, -27, 4, 9, 22, 16, -79, 22,
            -49, 123, -16, 55,
            -65, 7, 59, -81, 53, 95, 99, 15, -109, -48, -105, -102, -81, -79, -121, -55, -24, 82, -99, 18, -33, 88, -96, -94, 88, -93, -99, -73,
            -102, -81, -79, -121, -55, -24, 91, -51, 87, -40, -61, -28, -12, 0, -24, -118, -119, 98, -77, 46, -14, -23, -103, -50, -37, -51, 87,
            -40, -61, -28, -12, 23, -102, -81, -79, -121, -55, -24, 56, 39, 68, -73, -105, 64, -30, 93, 14, 118, -34, 106, -66, -58, 31, 39, -96,
            123, 77, 87, -40, -61, -28, -12, 20, -86, -8, 116, 51, -44, -66, 28, 36, 97, 15, -57, -35, 28, -10, 123, 77, 87, -46, -2, 62, 79, 65,
            121, -102, -9, -73, -55, -24, 68, -62, 54, -70, 11, 10, -108, -37, 107, 50, -48, -45, 41, -5, -87, 28, -7, 123, 74, 87, -111, 73, 112,
            -14, 122, 31, 120, -6, 115, -11, -50, -76, -91, -96, 81, 68, -100, -52, 57, 114, 70, 56, -71, 123, 94, -69, -41, -22, -14, -11, 108,
            -57, 23, 45, 89, -76, -117, 5, -112, -76, 120, 44, -115, -7, -70, 77, 89, -51, 89, 49, 116, -102, -77, -102, -78, 102, -59, -68, 118,
            -126, -38, -68, 22, 66, -47, -32, -78, 55, -22, -23, 85, 103, 53, 100, -59, -46, 106, -50, 106, -55, -116, 52, 26, -43, -32, -78, 22,
            -81, -90, 70, -4, -35, 38, -84, -26, -84, -104, -70, 77, 89, -51, 89, 49, -60, -122, -125, 42, 87, -45, 33, 106, -6, 100, 111, -51,
            -46, 106, -50, 106, -55, -117, -92, -43, -100, -43, -109, 34, -50, 122, 104, 85, 29, 43, -23, -111, 35, -91, -117, -90, 70, -5, 45,
            -110, -86, -59, -3, -85, -3, 36, 91, 37, -43, -97, 106, 84, 51, -99, 124, 52, 71, -45, -45, 57, -87, -91, -111, -6, -89, -46, 49, 63,
            -37, 67, -19, -111, -77, 17, 108, -99, 86, -82, 42, -107, 112, 63, -75, 85, -20, -11, -24, -24, 96, -36, 81, -87, 35, 67, -53, -59,
            57, 35, 108, 58, -113, -116, 123, 92, 94, 22, 77, -7, 58, 126, -31, 101, -36, -59, -105, 115, 51, 6, -21, -106, 121, -70, 46, -26,
            85, 69, -35, 17, -104, 2, 97, 103, -35, 16, -77, -18, -120, -91, 0, -62, -49, -71, -106, -49, -71, -103, 0, -95, 124, 60, 35, -94,
            -101, -7, 68, 75, 46, -24, -113, -111, 36, 73, 34, -54, 34, -33, 26, 42, 6, -33, -54, 35, -46, 26, 20, -66, -20, -11, 4, 109, -27, 81,
            -11, -30, 25, 2, 2, -62, -126, 6, -126, -120, 19, 33, 69, 48, 4, 0, 29, 15, -48, 16, 10, -41, -85, 108, 66, -128, 6,
                -98, 37, -1, -39 };
    }
}
