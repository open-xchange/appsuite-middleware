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
