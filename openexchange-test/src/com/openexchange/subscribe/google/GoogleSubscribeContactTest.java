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

package com.openexchange.subscribe.google;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import javax.imageio.ImageIO;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * {@link GoogleSubscribeContactTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.6.1
 */
public class GoogleSubscribeContactTest extends AbstractGoogleSubscribeTest {

    /**
     * Initializes a new {@link GoogleSubscribeContactTest}.
     *
     * @param name
     */
    public GoogleSubscribeContactTest(String name) {
        super(name);
    }

    public void testContacts() {
        Contact[] contacts = getContactManager().allAction(getContactTestFolderID(), Contact.ALL_COLUMNS);

        // represents a full supported contact mapping
        final String testAccount1 = "Herr Paul M\u00fcller Van Dyk Ende";
        boolean testAccount1Success = false;

        // represents a test account with private information only
        final String testAccount2 = "Maria Meier";
        boolean testAccount2Success = false;

        // represents a test account with no primary mail information only
        final String testAccount3 = "No Primary Mail";
        boolean testAccount3Success = false;

        for (Contact c : contacts) {
            if (c.getDisplayName() != null) {
                if (c.getDisplayName().equals(testAccount1)) {
                    // name
                    assertNotNullAndEquals("given name", "Paul", c.getGivenName());
                    assertNotNullAndEquals("middle name", "M\u00fcller", c.getMiddleName());
                    assertNotNullAndEquals("surname", "Van Dyk", c.getSurName());
                    assertNotNullAndEquals("title", "Herr", c.getTitle());
                    assertNotNullAndEquals("suffix", "Ende", c.getSuffix());
                    assertNotNullAndEquals("yomi firstname", "PhoneticPaul", c.getYomiFirstName());
                    assertNotNullAndEquals("yomi lastname", "PhoneticM\u00fcller", c.getYomiLastName());

                    // email
                    assertNotNullAndEquals("email1", "privat@example.com", c.getEmail1());
                    assertNotNullAndEquals("email2", "business@example.com", c.getEmail2());
                    assertNotNullAndEquals("email3", "other@example.com", c.getEmail3());

                    // organisation
                    assertNotNullAndEquals("company", "OX", c.getCompany());
                    assertNotNullAndEquals("job title", "DJ", c.getPosition());

                    assertNotNullAndEquals("note", "Do not edit. Account for testing.", c.getNote());
                    assertNotNullAndEquals("nickname", "MeisterPauli", c.getNickname());

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

                    // location
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

                    final byte[] expectedImage = getBytesOfPaulsImage();
                    final byte[] currentImage = c.getImage1();

                    BufferedImage biExpected = getBufferedImage(expectedImage);
                    BufferedImage biCurrent = getBufferedImage(currentImage);

                    float meanExpected = meanHistogramRGBValue(biExpected);
                    float meanCurrent = meanHistogramRGBValue(biCurrent);

                    assertValueInRange(meanCurrent, meanExpected - 0.5f, meanExpected + 0.5f);
                    testAccount1Success = true;
                } else if (c.getDisplayName().equals(testAccount2)) {
                    assertNotNullAndEquals("given name", "Maria", c.getGivenName());
                    assertNotNullAndEquals("surname", "Meier", c.getSurName());
                    assertFieldIsNull("title", c.getTitle());
                    assertFieldIsNull("middle name", c.getMiddleName());
                    assertFieldIsNull("suffix", c.getSuffix());
                    assertFieldIsNull("yomi firstname", c.getYomiFirstName());
                    assertFieldIsNull("yomi lastname", c.getYomiLastName());

                    // email
                    assertNotNullAndEquals("email1", "mariameier@example.com", c.getEmail1());
                    assertFieldIsNull("email2", c.getEmail2());
                    assertFieldIsNull("email3", c.getEmail3());

                    // organisation
                    assertNotNullAndEquals("note", "Test account. Do not change.", c.getNote());
                    assertFieldIsNull("job title", c.getPosition());

                    assertFieldIsNull("telephone business 1", c.getTelephoneBusiness1());
                    assertNotNullAndEquals("cellular telephone 1", "+4213371337133701", c.getTelephoneHome1());
                    assertFieldIsNull("telephone other", c.getTelephoneOther());
                    assertFieldIsNull("fax business", c.getFaxBusiness());
                    assertFieldIsNull("fax home", c.getFaxHome());
                    assertFieldIsNull("cellular telephone 1", c.getCellularTelephone1());
                    assertFieldIsNull("cellular telephone 2", c.getCellularTelephone2());

                    assertFieldIsNull("birthday", c.getBirthday());

                    // location
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
                    testAccount2Success = true;
                } else if (c.getDisplayName().equals(testAccount3)) {
                    assertNotNullAndEquals("given name", "No", c.getGivenName());
                    assertNotNullAndEquals("surname", "Primary", c.getSurName());
                    assertFieldIsNull("title", c.getTitle());
                    assertFieldIsNull("middle name", c.getMiddleName());
                    assertNotNullAndEquals("suffix", "Mail", c.getSuffix());
                    assertFieldIsNull("yomi firstname", c.getYomiFirstName());
                    assertFieldIsNull("yomi lastname", c.getYomiLastName());

                    // email
                    assertNotNullAndEquals("email1", "noprimarybuthomeaddress@example.com", c.getEmail1());
                    assertFieldIsNull("email2", c.getEmail2());
                    assertFieldIsNull("email3", c.getEmail3());

                    // organisation
                    assertFieldIsNull("company", c.getCompany());
                    assertFieldIsNull("job title", c.getPosition());

                    assertFieldIsNull("note", c.getNote());
                    assertFieldIsNull("nickname", c.getNickname());

                    assertFieldIsNull("telephone business 1", c.getTelephoneBusiness1());
                    assertFieldIsNull("cellular telephone 1", c.getTelephoneHome1());
                    assertFieldIsNull("telephone other", c.getTelephoneOther());
                    assertFieldIsNull("fax business", c.getFaxBusiness());
                    assertFieldIsNull("fax home", c.getFaxHome());
                    assertFieldIsNull("cellular telephone 1", c.getCellularTelephone1());
                    assertFieldIsNull("cellular telephone 2", c.getCellularTelephone2());

                    assertFieldIsNull("birthday", c.getBirthday());

                    // location
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
                    testAccount3Success = true;
                }
            }

            if (testAccount1Success && testAccount2Success && testAccount3Success) {
                return;
            }
        }
        assertTrue("Could not find: " + testAccount1, testAccount1Success);
        assertTrue("Could not find: " + testAccount2, testAccount2Success);
        assertTrue("Could not find: " + testAccount3, testAccount3Success);
    }

    private BufferedImage getBufferedImage(byte[] inc){
        ByteArrayInputStream bais = new ByteArrayInputStream(inc);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            assertFalse("An IOException occured " + e.getMessage(), true);
            return null;
        }
    }
    /**
     * Asserts that a value is in range between rangeMin and rangeMax
     *
     * @param value the value to be tested
     * @param rangeMin the absolute minimum range
     * @param rangeMax the absolute maximum range
     */
    private void assertValueInRange(float value, float rangeMin, float rangeMax) {
        boolean inRange = (rangeMin <= value && value <= rangeMax);
        assertTrue("Range differs too much. Expect values between: " + rangeMin + " and " + rangeMax + "--> Got: " + value, inRange);
    }

    /**
     * Returns the mean value of the RGB histogram of an image
     *
     * @param image the image
     * @return rounded average to second decimal place of RGB histogram
     */
    private float meanHistogramRGBValue(BufferedImage image) {
        int[] hValue = new int[256];
        // fill zero matrix
        for (int i = 0; i < hValue.length; i++) {
            hValue[i] = 0;
        }
        // get colors from image
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int red = new Color(image.getRGB(i, j)).getRed();
                int green = new Color(image.getRGB(i, j)).getGreen();
                int blue = new Color(image.getRGB(i, j)).getBlue();
                hValue[red]++;
                hValue[green]++;
                hValue[blue]++;
            }
        }
        // aggregate frequency of values per color and calculates the average value
        float average = 0;
        for (int colorPos = 0; colorPos < hValue.length; colorPos++) {
            average += (hValue[colorPos] * colorPos) / 3.0f;
        }
        average = average / (image.getWidth() * image.getHeight());
        return Math.round(average * 100.0f) / 100.0f;
    }

    private byte[] getBytesOfPaulsImage() {

        return new byte[] {
            -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 2, 0, 0, 1, 0, 1, 0, 0, -1, -37, 0, 67, 0, 8, 6, 6, 7, 6, 5, 8, 7, 7, 7, 9, 9,
            8, 10, 12, 20, 13, 12, 11, 11, 12, 25, 18, 19, 15, 20, 29, 26, 31, 30, 29, 26, 28, 28, 32, 36, 46, 39, 32, 34, 44, 35, 28, 28,
            40, 55, 41, 44, 48, 49, 52, 52, 52, 31, 39, 57, 61, 56, 50, 60, 46, 51, 52, 50, -1, -37, 0, 67, 1, 9, 9, 9, 12, 11, 12, 24, 13,
            13, 24, 50, 33, 28, 33, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
            50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, -1, -64, 0, 17, 8, 0, 96, 0,
            96, 3, 1, 34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 31, 0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7,
            8, 9, 10, 11, -1, -60, 0, -75, 16, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125, 1, 2, 3, 0, 4, 17, 5, 18, 33, 49, 65, 6,
            19, 81, 97, 7, 34, 113, 20, 50, -127, -111, -95, 8, 35, 66, -79, -63, 21, 82, -47, -16, 36, 51, 98, 114, -126, 9, 10, 22, 23,
            24, 25, 26, 37, 38, 39, 40, 41, 42, 52, 53, 54, 55, 56, 57, 58, 67, 68, 69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90,
            99, 100, 101, 102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120, 121, 122, -125, -124, -123, -122, -121, -120, -119, -118,
            -110, -109, -108, -107, -106, -105, -104, -103, -102, -94, -93, -92, -91, -90, -89, -88, -87, -86, -78, -77, -76, -75, -74,
            -73, -72, -71, -70, -62, -61, -60, -59, -58, -57, -56, -55, -54, -46, -45, -44, -43, -42, -41, -40, -39, -38, -31, -30, -29,
            -28, -27, -26, -25, -24, -23, -22, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -1, -60, 0, 31, 1, 0, 3, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -60, 0, -75, 17, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2,
            119, 0, 1, 2, 3, 17, 4, 5, 33, 49, 6, 18, 65, 81, 7, 97, 113, 19, 34, 50, -127, 8, 20, 66, -111, -95, -79, -63, 9, 35, 51, 82,
            -16, 21, 98, 114, -47, 10, 22, 36, 52, -31, 37, -15, 23, 24, 25, 26, 38, 39, 40, 41, 42, 53, 54, 55, 56, 57, 58, 67, 68, 69,
            70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100, 101, 102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120, 121,
            122, -126, -125, -124, -123, -122, -121, -120, -119, -118, -110, -109, -108, -107, -106, -105, -104, -103, -102, -94, -93, -92,
            -91, -90, -89, -88, -87, -86, -78, -77, -76, -75, -74, -73, -72, -71, -70, -62, -61, -60, -59, -58, -57, -56, -55, -54, -46,
            -45, -44, -43, -42, -41, -40, -39, -38, -30, -29, -28, -27, -26, -25, -24, -23, -22, -14, -13, -12, -11, -10, -9, -8, -7, -6,
            -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -9, 92, -47, -102, 40, -96, 97, -102, 51, 69, 20, 0, 102, -109, 52, 10, 40,
            11, -40, 51, 70, 104, -94, -128, 74, -31, -102, 92, -46, 81, 64, -12, 12, -47, -102, 40, -96, 26, 66, -47, 70, 41, 49, 64,
            -123, -94, -109, 20, -72, -96, 4, 20, 81, 70, 40, 19, 10, 40, -96, 82, 41, 108, 53, -101, 20, -127, -72, -26, -122, 28, -26,
            -112, -32, -47, 114, 27, 20, 63, 52, -20, -45, 2, -127, 74, 40, -72, 43, -114, -49, -46, -116, -3, 43, -27, 79, -8, 92, -2, 34,
            -11, 95, -50, -109, -2, 23, 63, -120, -67, 87, -13, -90, 81, -11, 94, -31, -22, 40, 4, 122, -118, -7, 83, -2, 23, 63, -120,
            -67, 87, -13, -91, -1, 0, -123, -49, -30, 47, 85, -4, -24, 3, -22, -96, 71, -88, -4, -23, 114, 61, -65, 58, -7, 83, -2, 23, 63,
            -120, -67, 87, -13, -93, -2, 23, 63, -120, -67, 87, -13, -96, 71, -43, 121, 30, -44, 18, 61, -85, -27, 79, -8, 92, -2, 34, -11,
            95, -50, -113, -8, 92, -2, 34, -11, 95, -50, -122, 59, -24, 125, 79, 41, -29, -116, 83, 71, -36, -22, 43, -27, -81, -8, 92,
            -34, 34, -57, -16, -2, 116, 127, -62, -26, -15, 15, -5, 63, -99, 77, -119, -79, -11, 24, 24, -55, 36, 126, 116, 36, -125, 61,
            70, 43, -27, -49, -8, 92, 94, 32, 124, 14, 63, 58, -24, -12, -17, -119, -70, -68, -10, -95, -104, 28, -44, 78, 74, 10, -20,
            -17, -61, 97, -99, 119, -53, 20, 120, -66, -10, -12, 31, -107, 27, -113, -96, -4, -85, -23, -49, -8, 80, -38, 79, -4, -10, 31,
            -107, 31, -16, -95, -76, -97, -7, -20, 63, 42, -44, -30, 62, 98, -34, 125, 7, -27, 70, -13, -24, 63, 42, -6, 119, -2, 20, 54,
            -109, -1, 0, 61, -121, -27, 71, -4, 40, 109, 39, -2, 123, 15, -54, -128, 62, 99, -34, 125, 7, -27, 70, -13, -19, -7, 87, -45,
            -97, -16, -95, -76, -97, -7, -20, 63, 42, 63, -31, 67, 105, 63, -13, -40, 126, 84, -45, 3, -26, 48, -25, -37, -14, -93, 121,
            -10, -4, -85, -23, -49, -8, 80, -38, 79, -4, -10, 31, -107, 31, -16, -95, -76, -97, -7, -20, 63, 42, -101, -121, 67, -26, 101,
            115, -19, -7, 82, 23, 111, 111, -54, -66, -103, 31, 1, -12, -111, -1, 0, 45, -123, 7, -32, 62, -109, -38, 97, 64, -36, -76, 62,
            106, -114, 67, -111, -112, 63, 42, -19, 116, -126, 126, -60, -67, 63, 42, -11, -13, -16, 39, 74, 28, -119, -121, 21, -95, 107,
            -16, -98, -58, -34, 61, -127, -58, 5, 115, -42, -125, -110, -78, 61, 108, -85, 23, 12, 60, -7, -92, 122, 54, -49, -10, -115,
            27, 63, -38, 52, -22, 43, 115, -56, 25, -77, -3, -93, 74, 19, -3, -90, -89, 81, 64, 13, -37, -2, -45, 81, -73, -3, -90, -91,
            -94, -128, 27, -77, -3, -93, 75, -73, -3, -93, 75, 69, 22, 11, -24, 70, -55, -109, -9, -102, -109, 103, -5, 77, 82, -32, 81,
            -127, 84, 45, -56, 76, 68, -97, -68, -44, -15, 24, 29, -51, 62, -118, -101, 106, 53, -90, -120, 90, 41, 40, -86, 1, 104, -92,
            -93, 20, 88, 2, -116, -47, 69, 38, -128, 5, 20, 81, 78, -62, -22, 20, 81, 69, 5, 59, 36, 20, 81, 69, 2, -47, -97, -1, -39 };
    }

}
