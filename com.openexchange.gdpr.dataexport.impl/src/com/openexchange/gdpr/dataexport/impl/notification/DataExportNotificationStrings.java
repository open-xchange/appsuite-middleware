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
