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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.data.conversion.ical;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ConversionWarningMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ConversionWarningMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link ConversionWarningMessage}.
     */
    private ConversionWarningMessage() {
        super();
    }
    
    // Unable to convert task status "%1$s".
    public final static String INVALID_STATUS_MSG = "Unable to convert task status \"%1$s\".";

    // Unable to convert task priority %1$d.
    public final static String INVALID_PRIORITY_MSG = "Unable to convert task priority %d.";
    
    // Can not create recurrence rule: %s
    public final static String CANT_CREATE_RRULE_MSG = "Can not create recurrence rule: %s";

    // Invalid session given to implementation "%1$s".
    public final static String INVALID_SESSION_MSG = "Invalid session given to implementation \"%1$s\".";

    // Can't generate uid.
    public final static String CANT_GENERATE_UID_MSG = "Can not generate uid.";

    // Problem writing to stream.
    public final static String WRITE_PROBLEM_MSG = "Problem writing to stream.";

    // Validation of calendar failed.
    public final static String VALIDATION_MSG = "Validation of calendar failed.";

    // Can not resolve user: %d
    public final static String CANT_RESOLVE_USER_MSG = "Can not resolve user: %d";

    // Parsing error parsing ical: %s
    public final static String PARSE_EXCEPTION_MSG = "Parsing error parsing ical: %s";

    // Unknown Class: %1$s
    public final static String UNKNOWN_CLASS_MSG = "Unknown Class: %1$s";

    // Cowardly refusing to convert confidential classified objects.
    public final static String CLASS_CONFIDENTIAL_MSG = "Cowardly refusing to convert confidential classified objects.";

    // Missing DTStart in appointment
    public final static String MISSING_DTSTART_MSG = "Missing DTSTART";

    // Can not resolve resource: %d
    public final static String CANT_RESOLVE_RESOURCE_MSG = "Can not resolve resource: %1$s";

    // Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.
    public final static String PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS_MSG = "Private Appointments can not have attendees. Removing attendees and accepting appointment anyway.";

    // Not supported recurrence pattern: BYMONTH
    public final static String BYMONTH_NOT_SUPPORTED_MSG = "Not supported recurrence pattern: BYMONTH";

    // This does not look like an iCal file. Please check the file.
    public final static String DOES_NOT_LOOK_LIKE_ICAL_FILE_MSG = "This does not look like an iCal file. Please check the file.";
    
    // Empty "CLASS" element.
    public final static String EMPTY_CLASS_MSG = "Empty \"CLASS\" element.";

    // Insufficient information for parsing/writing this element.
    public final static String INSUFFICIENT_INFORMATION_MSG = "Insufficient information for parsing/writing this element.";

    // An error occurred: %1$s
    public final static String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

}
