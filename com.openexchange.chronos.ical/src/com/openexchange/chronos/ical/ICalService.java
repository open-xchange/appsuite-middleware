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

package com.openexchange.chronos.ical;

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
     */
    StreamedCalendarExport getStreamedExport(OutputStream outputStream, ICalParameters parameters);
}
