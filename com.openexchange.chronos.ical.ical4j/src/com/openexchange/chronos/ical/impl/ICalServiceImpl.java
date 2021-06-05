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

package com.openexchange.chronos.ical.impl;

import static com.openexchange.chronos.ical.impl.ICalUtils.getParametersOrDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ICalUtilities;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.ical.StreamedCalendarExport;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.exception.OXException;

/**
 * {@link ICalServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICalServiceImpl implements ICalService {

    private final ICalMapper mapper;

    private final ICalUtilitiesImpl iCalUtilities;

    /**
     * Initializes a new {@link ICalServiceImpl}.
     */
    public ICalServiceImpl() {
        super();
        this.mapper = new ICalMapper();
        this.iCalUtilities = new ICalUtilitiesImpl(mapper);
    }

    @Override
    public CalendarExport exportICal(ICalParameters parameters) {
        ICalParameters iCalParameters = getParametersOrDefault(parameters);
        List<OXException> warnings = new ArrayList<OXException>();
        return new CalendarExportImpl(mapper, iCalParameters, warnings);
    }

    @Override
    public ImportedCalendar importICal(InputStream iCalFile, ICalParameters parameters) throws OXException {
        ICalParameters iCalParameters = getParametersOrDefault(parameters);
        ImportedCalendar calendar = ICalUtils.importCalendar(iCalFile, mapper, iCalParameters);
        return calendar;
    }

    @Override
    public ICalParameters initParameters() {
        return getParametersOrDefault(null);
    }

    @Override
    public ICalUtilities getUtilities() {
        return iCalUtilities;
    }

    @Override
    public StreamedCalendarExport getStreamedExport(OutputStream outputStream, ICalParameters parameters) throws IOException {
        ICalParameters iCalParameters = getParametersOrDefault(parameters);
        return new StreamedCalendarExportImpl(iCalUtilities, iCalParameters, outputStream);
    }

}
