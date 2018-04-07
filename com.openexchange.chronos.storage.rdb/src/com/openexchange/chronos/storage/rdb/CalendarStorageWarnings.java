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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.ID_COMPARATOR;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class CalendarStorageWarnings {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarStorageWarnings.class);

    private SortedMap<String, List<OXException>> warnings;
    private ProblemSeverity unsupportedDataThreshold;

    /**
     * Initializes a new {@link CalendarStorageWarnings}.
     */
    protected CalendarStorageWarnings() {
        super();
    }

    /**
     * Configures the severity threshold defining which unsupported data errors can be ignored.
     *
     * @param severityThreshold The threshold defining up to which severity unsupported data errors can be ignored, or
     *            <code>null</code> to not ignore any unsupported data error at all
     */
    public void setUnsupportedDataThreshold(ProblemSeverity severityThreshold) {
        this.unsupportedDataThreshold = severityThreshold;
    }

    /**
     * Keeps track of a warning that occurred when processing the data of a specific event.
     *
     * @param eventId The identifier of the event the warning is associated with
     * @param warning The warning
     */
    public void addWarning(String eventId, OXException warning) {
        if (null == warnings) {
            warnings = new TreeMap<String, List<OXException>>(ID_COMPARATOR);
        }
        com.openexchange.tools.arrays.Collections.put(warnings, eventId, warning);
    }

    /**
     * Initializes and keeps track of a {@link CalendarExceptionCodes#IGNORED_INVALID_DATA} warning that occurred when processing the data
     * of a specific event.
     *
     * @param eventId The identifier of the event the warning is associated with
     * @param field The corresponding event field of the invalid data
     * @param severity The problem severity
     * @param message The message providing details of the warning
     * @param cause The optional initial cause
     * @return The added warning
     */
    public OXException addInvalidDataWaring(String eventId, EventField field, ProblemSeverity severity, String message, Throwable cause) {
        OXException warning = CalendarExceptionCodes.IGNORED_INVALID_DATA.create(cause, eventId, field, String.valueOf(severity), message);
        if (0 > ProblemSeverity.NORMAL.compareTo(severity)) {
            LOG.info(warning.getLogMessage());
        } else {
            LOG.debug(warning.getLogMessage());
        }
        addWarning(eventId, warning);
        return warning;
    }

    /**
     * Initializes a new {@link CalendarExceptionCodes#UNSUPPORTED_DATA} error that occurred when processing the data of a specific event.
     * <p/>
     * In case errors up to a certain problem severity can be ignored, an appropriate warning is tracked, otherwise, the error is raised.
     *
     * @param eventId The identifier of the event the error is associated with
     * @param field The corresponding event field of the unsupported data
     * @param severity The problem severity
     * @param message The message providing details of the error
     * @throws {@link CalendarExceptionCodes#UNSUPPORTED_DATA}
     */
    public void addUnsupportedDataError(String eventId, EventField field, ProblemSeverity severity, String message) throws OXException {
        OXException error = CalendarExceptionCodes.UNSUPPORTED_DATA.create(eventId, field, severity, message);
        if (null == unsupportedDataThreshold || 0 > unsupportedDataThreshold.compareTo(severity)) {
            //            error.setCategory(Category.CATEGORY_ERROR);
            throw error;
        }
        addInvalidDataWaring(eventId, field, severity, message, error);
    }

    /**
     * Gets any tracked warnings that occurred when processing the stored data.
     *
     * @return The warnings, mapped to the associated event identifier, or an empty map if there are none
     */
    public Map<String, List<OXException>> getWarnings() {
        return null == warnings ? Collections.<String, List<OXException>> emptyMap() : warnings;
    }

    /**
     * Gets any tracked warnings that occurred when processing the stored data and flushes them, so that subsequent invocations would
     * return an empty map.
     *
     * @return The warnings, mapped to the associated event identifier, or an empty map if there are none
     */
    public Map<String, List<OXException>> getAndFlushWarnings() {
        if (null == warnings) {
            return Collections.emptyMap();
        }
        Map<String, List<OXException>> result = new TreeMap<String, List<OXException>>(warnings);
        warnings = null;
        return result;
    }

}
