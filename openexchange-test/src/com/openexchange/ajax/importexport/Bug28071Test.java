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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportParser;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.java.Charsets;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link Bug28071Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug28071Test extends ManagedAppointmentTest {

    public Bug28071Test() {
        super();
    }

    @Test
    public void testIgnoreUIDs() throws Exception {
        String iCal = prepareICal();
        /*
         * import initial iCal
         */
        ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertFalse("Initial import failed", icalResponse.hasError());
        assertNotNull("Should have processed 3 appointments", icalResponse.getImports());
        assertEquals("Should have processed 3 appointments", 3, icalResponse.getImports().length);
        for (int i = 0; i < 3; i++) {
            assertFalse("Expected no error", icalResponse.getImports()[i].hasError());
        }
        /*
         * import a second time, expecting UID conflicts
         */
        icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertTrue("Expected UID conflict", icalResponse.hasError());
        /*
         * import a second time ignoring UIDs
         */
        icalResponse = getClient().execute(new IgnoringUIDsImportRequest(iCal, folder.getObjectID()));
        assertFalse("Import ignoring UIDs failed", icalResponse.hasError());
        assertNotNull("Should have processed 3 appointments", icalResponse.getImports());
        assertEquals("Should have processed 3 appointments", 3, icalResponse.getImports().length);
        for (int i = 0; i < 3; i++) {
            assertFalse("Expected no error", icalResponse.getImports()[i].hasError());
        }
    }

    private static final class IgnoringUIDsImportRequest extends AbstractImportRequest<ICalImportResponse> {

        public IgnoringUIDsImportRequest(String iCal, int folderID) {
            super(Action.ICal, folderID, new ByteArrayInputStream(Charsets.getBytes(iCal, Charsets.UTF_8)));
        }

        @Override
        public AbstractAJAXParser<? extends ICalImportResponse> getParser() {
            return new ICalImportParser(true);
        }

        @Override
        public Parameter[] getParameters() {
            Parameter[] parameters = super.getParameters();
            return Arrays.add(parameters, new Parameter("ignoreUIDs", "true"));
        }

    }

    private static String prepareICal() {
        return ICAL.replaceAll("\\$\\{UID1\\}", UUID.randomUUID().toString()).replaceAll("\\$\\{UID2\\}", UUID.randomUUID().toString()).replaceAll("\\$\\{UID3\\}", UUID.randomUUID().toString());
    }

    private static final String ICAL = "BEGIN:VCALENDAR\nPRODID:-//afklm/Manage My Booking/NONSGML v1.0//EN\nVERSION:2.0\nCALSCALE:GREGORIAN\nMETHOD:PUBLISH\n" + "BEGIN:VEVENT\nDTSTAMP:20130806T090731Z\nDTSTART:20130806T182000\nDTEND:20130806T194500\n" + "SUMMARY:Flug KL1888 N\u00fcrnberg - Amsterdam\\, Abflug 18:20 Ortszeit\nUID:${UID1}\n" + "LOCATION:N\u00fcrnberg (N\u00fcrnberg)\n" + "DESCRIPTION:Ihr Flug KL1888 von N\u00fcrnberg (N\u00fcrnberg) nach Amsterdam (Schiphol) wird um 18:20 Ortszeit starten\\n\\n" + "Meine Buchung verwalten auf KLM.com: https://www.klm.com/travel/DE_DE/index.htm#tab=db_mmb\\n\\n" + "Bei Fragen zu Ihrer Reservierung kontaktieren Sie bitte das KLM-Servicecenter: " + "http://www.klm.com/travel/DE_DE/customer_support/customer_support/contact/contact_popup.htm\\n\\n" + "Haftungsausschluss: Bei den genannten Zeiten handelt es sich um Ortszeiten. Abflugzeiten k\u00f6nnen " + "\u00c4nderungen unterliegen. \u00dcberpr\u00fcfen Sie vor Ihrem Flug immer die tats\u00e4chliche Abflugzeit.\n" + "URL:https://www.klm.com/travel/DE_DE/index.htm#tab=db_mmb\nTRANSP:TRANSPARENT\nEND:VEVENT\nBEGIN:VEVENT\n" + "DTSTAMP:20130806T090731Z\nDTSTART:20130808T163000\nDTEND:20130808T174000\n" + "SUMMARY:Flug KL1887 Amsterdam - N\u00fcrnberg\\, Abflug 16:30 Ortszeit\nUID:${UID2}\n" + "LOCATION:Amsterdam (Schiphol)\nDESCRIPTION:Ihr Flug KL1887 von Amsterdam (Schiphol) nach N\u00fcrnberg (N\u00fcrnberg) wird " + "um 16:30 Ortszeit starten\\n\\nMeine Buchung verwalten auf KLM.com: https://www.klm.com/travel/DE_DE/index.htm#tab=db_mmb\\n\\n" + "Check-in: https://www.klm.com/travel/DE_DE/prepare_for_travel/checkin_options/internet_checkin/ici_jffp_app.htm\\n\\n" + "Bei Fragen zu Ihrer Reservierung kontaktieren Sie bitte das KLM-Servicecenter: " + "http://www.klm.com/travel/DE_DE/customer_support/customer_support/contact/contact_popup.htm\\n\\n" + "Haftungsausschluss: Bei den genannten Zeiten handelt es sich um Ortszeiten. Abflugzeiten k\u00f6nnen \u00c4nderungen unterliegen. " + "\u00dcberpr\u00fcfen Sie vor Ihrem Flug immer die tats\u00e4chliche Abflugzeit.\n" + "URL:https://www.klm.com/travel/DE_DE/index.htm#tab=db_mmb\nTRANSP:TRANSPARENT\nEND:VEVENT\nBEGIN:VEVENT\n" + "DTSTAMP:20130806T090731Z\nDTSTART:20130807T103000\nDTEND:20130807T103000\n" + "SUMMARY:Check-in f\u00fcr Flug KL1887 Amsterdam - N\u00fcrnberg ist jetzt ge\u00f6ffnet\nUID:${UID3}\n" + "LOCATION:Amsterdam (Schiphol)\nDESCRIPTION:Sie k\u00f6nnen jetzt f\u00fcr Ihren Flug von Amsterdam nach N\u00fcrnberg " + "einchecken.\\n\\nOnline einchecken: " + "https://www.klm.com/travel/DE_DE/prepare_for_travel/checkin_options/internet_checkin/ici_jffp_app.htm.\\n\\n" + "Haftungsausschluss: Bei den genannten Zeiten handelt es sich um Ortszeiten. Abflugzeiten k\u00f6nnen \u00c4nderungen unterliegen. " + "\u00dcberpr\u00fcfen Sie vor Ihrem Flug immer die tats\u00e4chliche Abflugzeit.\n" + "URL:https://www.klm.com/travel/DE_DE/index.htm#tab=db_mmb\nTRANSP:TRANSPARENT\nBEGIN:VALARM\nTRIGGER:PT0S\nACTION:DISPLAY\n" + "DESCRIPTION:Check-in f\u00fcr Flug KL1887 Amsterdam - N\u00fcrnberg ist jetzt ge\u00f6ffnet\nEND:VALARM\nEND:VEVENT\n" + "END:VCALENDAR\n";

}
