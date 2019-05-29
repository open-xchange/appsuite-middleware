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

package com.openexchange.gdpr.dataexport;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link DataExportExceptionMessage} - The The GDPR data export error messages.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DataExportExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link DataExportExceptionMessage}.
     */
    private DataExportExceptionMessage() {
        super();
    }

    // There is already a running data export.
    public final static String TASK_ALREADY_RUNNING_MSG = "There is already a running data export";

    // There is no data export.
    public static final String NO_SUCH_TASK_MSG = "There is no data export";

    // The data export is not yet finished. Please try again later.
    public static final String TASK_NOT_COMPLETED_MSG = "The data export is not yet finished. Please try again later.";

    // The data export failed.
    public static final String TASK_FAILED_MSG = "The data export failed";

    // A data export has already been requested
    public static final String DUPLICATE_TASK_MSG = "A data export has already been requested";

    // There is no data export or it has already been completed
    public static final String CANCEL_TASK_FAILED_MSG = "There is no data export or it has already been completed";

    // The data export has been aborted, but he/she tries to retrieve its results
    public static final String TASK_ABORTED_MSG = "The data export has been aborted";

    // User requested a package number that does not exist
    public static final String NO_SUCH_RESULT_FILE_MSG = "No such package";

    // User requested to delete a completed data export task, but task is not yet completed
    public static final String DELETE_TASK_FAILED_MSG = "There is no data export or it is not yet completed";;

}
