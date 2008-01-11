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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportRequest;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.VersitDefinition.Writer;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Prevent instantiation.
     */
    private Tools() {
        super();
    }

    public static CSVImportResponse importCSV(final AJAXClient client,
        final CSVImportRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (CSVImportResponse) Executor.execute(client, request);
    }

    public static ICalImportResponse importICal(final AJAXClient client,
        final ICalImportRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (ICalImportResponse) Executor.execute(client, request);
    }

    public static VCardImportResponse importVCard(final AJAXClient client,
        final VCardImportRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (VCardImportResponse) Executor.execute(client, request);
    }

    public static OutlookCSVImportResponse importOutlookCSV(final AJAXClient client,
        final OutlookCSVImportRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (OutlookCSVImportResponse) Executor.execute(client, request);
    }

    public static InputStream toICal(final AJAXClient client,
        final AppointmentObject appointment) throws AjaxException, IOException,
        SAXException, JSONException, ConverterException {
        final VersitDefinition definition = ICalendar.vEvent2;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OXContainerConverter conv = new OXContainerConverter(
            client.getValues().getTimeZone(), client.getValues()
            .getDefaultAddress());
        final VersitObject versit = conv.convertAppointment(appointment);
        final Writer writer = definition.getWriter(baos, "UTF-8");
        final VersitObject container = OXContainerConverter.newCalendar("2.0");
        definition.writeProperties(writer, container);
        definition.write(writer, versit);
        definition.writeEnd(writer, container);
        baos.flush();
        writer.flush();
        conv.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
