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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportRequest;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import org.json.JSONArray;

/**
 * This test verifies if the problem described in bug 9209 does not appear
 * anymore.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug9209Test extends AbstractAJAXSession {

    /**
     * Test byte array.
     */
    private static final byte[] TEST_BYTES = new byte[] { 'a', 'b', 'c' };

    /**
     * Default constructor.
     * @param name name of the test.
     */
    public Bug9209Test(final String name) {
        super(name);
    }

    /**
     * Verifies if bug 9209 appears again for CSV files.
     * @throws Throwable if an exception occurs.
     */
    public void test9209CSV() throws Throwable {
        final AJAXClient client = getClient();
        final CSVImportResponse iResponse = Tools.importCSV(client,
            new CSVImportRequest(client.getValues().getPrivateContactFolder(),
            new ByteArrayInputStream(TEST_BYTES), false));
        assertTrue("CSV importer does not give an error.", iResponse.hasError());
    }

    /**
     * Verifies if bug 9209 appears again for iCal files.
     * @throws Throwable if an exception occurs.
     */
    public void test9209ICal() throws Throwable {
        final AJAXClient client = getClient();
        final ICalImportResponse iResponse = Tools.importICal(client,
            new ICalImportRequest(client.getValues().getPrivateAppointmentFolder(),
            new ByteArrayInputStream(TEST_BYTES), false));
        //the last version of the ical4j parser does not fail on weird inputs, but does nothing
        assertEquals("Response data should be empty", 0, ((JSONArray)iResponse.getData()).length());
        assertNull("No conflicts should be found", iResponse.getConflicts());
        assertFalse("ICal importer should not give an error.", iResponse.hasError());
    }

    /**
     * Verifies if bug 9209 appears again for vCard files.
     * @throws Throwable if an exception occurs.
     */
    public void test9209VCard() throws Throwable {
        final AJAXClient client = getClient();
        final VCardImportResponse iResponse = Tools.importVCard(client,
            new VCardImportRequest(client.getValues().getPrivateContactFolder(),
            new ByteArrayInputStream(TEST_BYTES), false));
        assertTrue("VCard importer does not give an error.", iResponse.hasError());
    }

    /**
     * Verifies if bug 9209 appears again for Outlook CSV files.
     * @throws Throwable if an exception occurs.
     */
    public void test9209OutlookCSV() throws Throwable {
        final AJAXClient client = getClient();
        final OutlookCSVImportResponse iResponse = Tools.importOutlookCSV(client,
            new OutlookCSVImportRequest(client.getValues().getPrivateContactFolder(),
            new ByteArrayInputStream(TEST_BYTES), false));
        assertTrue("Outlook CSV importer does not give an error.", iResponse.hasError());
    }
}
