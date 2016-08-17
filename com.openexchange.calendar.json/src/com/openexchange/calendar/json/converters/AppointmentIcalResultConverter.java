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

package com.openexchange.calendar.json.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AppointmentIcalResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AppointmentIcalResultConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentIcalResultConverter.class);

    private final ServiceLookup services;
    private final Pattern emptyRDate;

    /**
     * Initializes a new {@link AppointmentIcalResultConverter}.
     */
    public AppointmentIcalResultConverter(final ServiceLookup services) {
        super();
        this.services = services;
        emptyRDate = Pattern.compile("^RDATE:\\r\\n", Pattern.MULTILINE);
    }

    @Override
    public String getInputFormat() {
        return "appointment";
    }

    @Override
    public String getOutputFormat() {
        return "ical";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final Object resultObject = result.getResultObject();
        if (resultObject instanceof Appointment) {
            final Appointment appointment = (Appointment) resultObject;
            result.setResultObject(toIcal(Collections.singletonList(appointment), session), "native");
        } else {
            if (resultObject instanceof List) {
                result.setResultObject(toIcal((List<Appointment>) resultObject, session), "native");
            } else {
                final Collection<Appointment> appointments = (Collection<Appointment>) resultObject;
                result.setResultObject(toIcal(new ArrayList<Appointment>(appointments), session), "native");
            }
        }
    }

    private String toIcal(final List<Appointment> appointments, final ServerSession session) throws OXException {
        final ICalEmitter emitter = services.getService(ICalEmitter.class);
        if (null == emitter) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalEmitter.class.getName());
        }
        final List<ConversionError> errors = new LinkedList<ConversionError>();
        final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        final String icalText = emitter.writeAppointments(appointments, session.getContext(), errors, warnings);
        log(errors, warnings);
        return removeEmptyRDates(icalText);
    }

    private void log(final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        for(final ConversionError error : errors) {
            LOG.warn(error.getMessage());
        }

        for(final ConversionWarning warning : warnings) {
            LOG.warn(warning.getMessage());
        }
    }

    private String removeEmptyRDates(final String iCal) {
        return emptyRDate.matcher(iCal).replaceAll("");
    }

}
