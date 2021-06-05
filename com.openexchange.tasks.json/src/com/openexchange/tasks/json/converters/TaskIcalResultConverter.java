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

package com.openexchange.tasks.json.converters;

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
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link TaskIcalResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TaskIcalResultConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskIcalResultConverter.class);

    private final ServiceLookup services;
    private final Pattern emptyRDate;

    /**
     * Initializes a new {@link TaskIcalResultConverter}.
     */
    public TaskIcalResultConverter(final ServiceLookup services) {
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
        if (resultObject instanceof Task) {
            final Task task = (Task) resultObject;
            result.setResultObject(toIcal(Collections.singletonList(task), session), "native");
        } else {
            if (resultObject instanceof List) {
                result.setResultObject(toIcal((List<Task>) resultObject, session), "native");
            } else {
                final Collection<Task> tasks = (Collection<Task>) resultObject;
                result.setResultObject(toIcal(new ArrayList<Task>(tasks), session), "native");
            }
        }
    }

    private String toIcal(final List<Task> tasks, final ServerSession session) throws OXException {
        final ICalEmitter emitter = services.getService(ICalEmitter.class);
        if (null == emitter) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalEmitter.class.getName());
        }
        final List<ConversionError> errors = new LinkedList<ConversionError>();
        final List<ConversionWarning> warnings = new LinkedList<ConversionWarning>();
        final String icalText = emitter.writeTasks(tasks, errors, warnings, session.getContext());
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
