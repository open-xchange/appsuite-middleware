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

package com.openexchange.gdpr.dataexport.impl.notification;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link DataExportNotificationStrings} - The localizable strings for composing the notification mail about successful, failed or aborted data export.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportNotificationStrings implements LocalizableStrings {

    private DataExportNotificationStrings() {
        super();
    }

    // The user salutation; e.g. "Dear John Doe,"
    public static final String SALUTATION = "Dear %1$s,";

    // The content of the E-Mail informing about successful data export. E.g. "The data archive that you have requested on 09/01/2019 is now ready for download. You can download the archive until 5th of September 2019."
    public static final String CONTENT_SUCCESS_WITH_EXPIRATION = "The data archive that you have requested on %1$s is now ready for download. You can download the archive until %2$s.";

    // The content of the E-Mail informing about successful data export. E.g. "The data archive that you have requested on 09/01/2019 is now ready for download. You can download the archive until 5th of September 2019."
    public static final String CONTENT_SUCCESS_WITHOUT_EXPIRATION = "The data archive that you have requested on %1$s is now ready for download.";

    // The content of the E-Mail informing about failed data export. E.g. "Unfortunately the data export that you have requested on 09/01/2019 has failed. Please try again."
    public static final String CONTENT_FAILURE = "Unfortunately the data export that you have requested on %1$s has failed. Please try again.";

    // The content of the E-Mail informing about aborted data export. E.g. "The data export that you have requested on 09/01/2019 has been canceled."
    public static final String CONTENT_ABORTED = "The data export that you have requested on %1$s has been canceled.";

    // The subject of the E-Mail for a successful data export result
    public static final String SUBJECT_SUCCESS = "Your personal data archive is ready for download";

    // The subject of the E-Mail for a failed data export result
    public static final String SUBJECT_FAILURE = "Your personal data export failed";

    // The subject of the E-Mail for an aborted data export result
    public static final String SUBJECT_ABORTED = "Your personal data export has been canceled";

    // The label of the button linking to downloadable archives.
    public static final String VIEW_ARCHIVES = "Download archives";

    // The personal part for the no-reply address
    public static final String NO_REPLY_PERSONAL = "Service for dowloading your personal data";

    // The info footer provided for every data export notification E-Mail
    public static final String INFO = "You are receiving this email because you have requested a personal data download recently.";

}
