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

package com.openexchange.importexport.importers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.server.ServiceLookup;


/**
 * {@link CSVContactImporterTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
@RunWith(PowerMockRunner.class)
public class CSVContactImporterTest {

    @Mock
    private ServiceLookup serviceLookup;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testisValid_emptyWithHyphen_returnFalse() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("0-0-00");

        Assert.assertFalse(validDate);
    }

    @Test
    public void testisValid_emptyWithHyphenAndTwoNumbers_returnFalse() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("00-00-00");

        Assert.assertFalse(validDate);
    }

    @Test
    public void testisValid_emptyWithDot_returnFalse() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("0.0.00");

        Assert.assertFalse(validDate);
    }

    @Test
    public void testisValid_emptyWithDotAndTwoNumbers_returnFalse() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("00.00.00");

        Assert.assertFalse(validDate);
    }

    @Test
    public void testisValid_validDateFormatTwoYearCharacters_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1.1.88");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validDateFormatFourYearCharacters_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1.1.1988");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validDateFormatFullDate_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("11.11.1988");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validDateFormatTwoNumbersAll_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("01.01.88");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validUsDateFormatTwoNumbersAll_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1988-11-11");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validUsDateFormatOneNumber_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1988-1-1");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validSlashDateFormatTwoNumbersAll4_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("11/11/88");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validSlashDateFormatOneNumber_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1/1/1988");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testisValid_validSlashDateFormat_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("1/1/88");

        Assert.assertTrue(validDate);
    }

    @Test
    public void testIsValid_validString_returnTrue() {
        CSVContactImporter csvContactImporter = new CSVContactImporter(serviceLookup);

        boolean validDate = csvContactImporter.isValid("Evaluate this value to be imported");

        Assert.assertTrue(validDate);
    }

}
