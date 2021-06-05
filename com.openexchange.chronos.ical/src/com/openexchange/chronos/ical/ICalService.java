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

package com.openexchange.chronos.ical;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ICalService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface ICalService {

    /**
     * Imports an iCalendar file.
     *
     * @param inputStream The input stream carrying the iCalendar data to import
     * @param parameters Further parameters for the iCalendar import, or <code>null</code> to stick with the defaults
     * @return A calendar import providing access to the imported data
     * @throws OXException If importing the iCalendar data fails; non-fatal conversion warnings are accessible within each imported component
     */
    ImportedCalendar importICal(InputStream inputStream, ICalParameters parameters) throws OXException;

    /**
     * Initializes a new {@link CalendarExport} for adding events or other iCalendar components to the export.
     *
     * @param parameters Further parameters for the iCalendar export, or <code>null</code> to stick with the defaults
     * @return The calendar export
     */
    CalendarExport exportICal(ICalParameters parameters);

    /**
     * Initializes a new {@link ICalParameters} instance for use with the iCal service.
     *
     * @return The parameters
     */
    ICalParameters initParameters();

    /**
     * Provides access to additional iCal utilities.
     *
     * @return The iCal utilities
     */
    ICalUtilities getUtilities();

    /**
     * Initializes a {@link StreamedCalendarExport}.
     * 
     * @param outputStream The {@link OutputStream} to write on
     * @param parameters The {@link ICalParameters}
     * @return A {@link StreamedCalendarExport}
     * @throws IOException If writing to the output stream fails
     */
    StreamedCalendarExport getStreamedExport(OutputStream outputStream, ICalParameters parameters) throws IOException;
}
