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

package com.openexchange.chronos.scheduling.common;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Constants {

    /*
     * ----------------------------- FILE NAMES -----------------------------
     */

    public static final String CANCLE_FILE_NAME = "cancel.ics";

    public static final String INVITE_FILE_NAME = "invite.ics";

    public static final String RESPONSE_FILE_NAME = "response.ics";

    /*
     * ----------------------------- MIME PARTS -----------------------------
     */

    public static final String MIXED = "mixed";

    public static final String MULTIPART_MIXED = "multipart/mixed";

    public static final String ALTERNATIVE = "alternative";

    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    /*
     * ------------------------------- HEADER -------------------------------
     */

    public static final String HEADER_DATE = "Date";

    public static final String HEADER_XPRIORITY = "X-Priority";

    public static final String HEADER_DISPNOTTO = "Disposition-Notification-To";

    public static final String HEADER_ORGANIZATION = "Organization";

    public static final String HEADER_AUTO_SUBMITTED = "Auto-Submitted";

    public static final String HEADER_X_MAILER = "X-Mailer";

    public final static String HEADER_X_OX_REMINDER = "X-OX-Reminder";

    public final static String HEADER_X_OX_MODULE = "X-Open-Xchange-Module";

    public final static String HEADER_X_OX_TYPE = "X-Open-Xchange-Type";

    public final static String HEADER_X_OX_OBJECT = "X-Open-Xchange-Object";

    public final static String HEADER_X_OX_SEQUENCE = "X-Open-Xchange-Sequence";

    public final static String HEADER_X_OX_UID = "X-Open-Xchange-UID";

    public final static String HEADER_X_OX_RECURRENCE_DATE = "X-Open-Xchange-RDATE";

    /*
     * --------------------------- HEADER VALUES ---------------------------
     */

    public static final String VALUE_AUTO_GENERATED = "auto-generated";

    public static final String VALUE_PRIORITYNORM = "3 (normal)";

    public static final String VALUE_X_MAILER = "Open-Xchange Mailer";

    public final static String VALUE_X_OX_MODULE = "Appointments";

    /*
     * ------------------------ ADDITIONAL HEADER -------------------------
     */
    public static final String ADDITIONAL_HEADER_READ_RECEIPT = "readReceipt";

    public static final String ADDITIONAL_HEADER_MAIL_HEADERS = "mailHeaders";

    /*
     * ---------------------------- ADDITIONAL ----------------------------
     */

    public static final String COMMENT = "COMMENT";

}
