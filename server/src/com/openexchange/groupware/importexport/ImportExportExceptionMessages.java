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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport;

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link ImportExportExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ImportExportExceptionMessages implements LocalizableStrings {

    public static final String CANNOT_EXPORT_MSG = "Could not export the folder %1$s in the format %2$s.";

    public static final String LOADING_CONTACTS_FAILED_MSG = "Could not load contacts";

    public static final String UTF8_ENCODE_FAILED_MSG = "Could not encode as UTF-8";

    public static final String SQL_PROBLEM_MSG = "Invalid SQL Query: %s";

    public static final String LOADING_FOLDER_FAILED_MSG = "Could not load folder %s";

    public static final String ICAL_SERVICE_MISSING_MSG = "The necessary iCal emitter serivce is missing.";

    public static final String NUMBER_FAILED_MSG = "Parsing %1$s to a number failed.";

    public static final String ICAL_CONVERSION_FAILED_MSG = "Conversion to iCal failed.";

    public static final String VCARD_CONVERSION_FAILED_MSG = "Conversion to vCard failed.";

    private ImportExportExceptionMessages() {
        super();
    }
}
