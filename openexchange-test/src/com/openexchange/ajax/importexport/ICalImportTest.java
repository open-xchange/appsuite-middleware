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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;

public class ICalImportTest extends AbstractICalTest {

    // Bug 12177    @Test
    @Test
    public void testWarnings() throws JSONException, OXException, IOException, SAXException {
        StringBuilder icalText = new StringBuilder(1500);
        icalText.append("BEGIN:VCALENDAR\n");
        icalText.append("VERSION:2.0").append('\n');
        icalText.append("PRODID:OPEN-XCHANGE").append('\n');

        icalText.append("BEGIN:VEVENT").append('\n');
        icalText.append("CLASS:SUPERCALIFRAGILISTICEXPLIALIDOCIOUS").append('\n');
        icalText.append("DTSTART:20070101T080000Z").append('\n');
        icalText.append("DTEND:20070101T100000Z").append('\n');
        icalText.append("SUMMARY: appointmentWithWarnings ICalImportTest#testWarnings " + System.currentTimeMillis()).append('\n');
        icalText.append("TRANSP:OPAQUE").append('\n');
        icalText.append("END:VEVENT").append('\n');

        icalText.append("END:VCALENDAR");

        final ICalImportRequest request = new ICalImportRequest(appointmentFolderId, new ByteArrayInputStream(icalText.toString().getBytes(com.openexchange.java.Charsets.UTF_8)), false);
        final ICalImportResponse iResponse = getClient().execute(request);
        final ImportResult[] importResult = iResponse.getImports();

        ImportResult resultWithWarnings = null;
        try {
            assertEquals(1, importResult.length);
            resultWithWarnings = importResult[0];

            assertNotNull(resultWithWarnings.getException());

            List<ConversionWarning> warnings = resultWithWarnings.getWarnings();
            assertNotNull(warnings);
            assertEquals(1, warnings.size());

        } finally {
            DeleteRequest delete = new DeleteRequest(Integer.valueOf(resultWithWarnings.getObjectId()), appointmentFolderId, new Date(Long.MAX_VALUE), true);
            getClient().execute(delete);
        }

    }
}
