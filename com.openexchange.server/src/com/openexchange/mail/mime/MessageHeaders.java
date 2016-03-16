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

package com.openexchange.mail.mime;

/**
 * {@link MessageHeaders} - Various constants for MIME message headers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageHeaders {

    /**
     * Prevent instantiation
     */
    private MessageHeaders() {
        super();
    }

    /** "From" */
    public static final String HDR_FROM = "From".intern();

    public static final HeaderName FROM = HeaderName.valueOf(HDR_FROM);

    /** "To" */
    public static final String HDR_TO = "To".intern();

    public static final HeaderName TO = HeaderName.valueOf(HDR_TO);

    /** "Cc" */
    public static final String HDR_CC = "Cc".intern();

    public static final HeaderName CC = HeaderName.valueOf(HDR_CC);

    /** "Bcc" */
    public static final String HDR_BCC = "Bcc".intern();

    public static final HeaderName BCC = HeaderName.valueOf(HDR_BCC);

    /** "Date" */
    public static final String HDR_DATE = "Date".intern();

    public static final HeaderName DATE = HeaderName.valueOf(HDR_DATE);

    /** "Reply-To" */
    public static final String HDR_REPLY_TO = "Reply-To".intern();

    public static final HeaderName REPLY_TO = HeaderName.valueOf(HDR_REPLY_TO);

    /** "Subject" */
    public static final String HDR_SUBJECT = "Subject".intern();

    public static final HeaderName SUBJECT = HeaderName.valueOf(HDR_SUBJECT);

    /** "Message-ID" */
    public static final String HDR_MESSAGE_ID = "Message-ID".intern();

    public static final HeaderName MESSAGE_ID = HeaderName.valueOf(HDR_MESSAGE_ID);

    /** "In-Reply-To" */
    public static final String HDR_IN_REPLY_TO = "In-Reply-To".intern();

    public static final HeaderName IN_REPLY_TO = HeaderName.valueOf(HDR_IN_REPLY_TO);

    /** "References" */
    public static final String HDR_REFERENCES = "References".intern();

    public static final HeaderName REFERENCES = HeaderName.valueOf(HDR_REFERENCES);

    /** "X-Priority" */
    public static final String HDR_X_PRIORITY = "X-Priority".intern();

    public static final HeaderName X_PRIORITY = HeaderName.valueOf(HDR_X_PRIORITY);

    /** "Importance" */
    public static final String HDR_IMPORTANCE = "Importance".intern();

    public static final HeaderName IMPORTANCE = HeaderName.valueOf(HDR_IMPORTANCE);

    /** "Disposition-Notification-To" */
    public static final String HDR_DISP_NOT_TO = "Disposition-Notification-To".intern();

    public static final HeaderName DISP_NOT_TO = HeaderName.valueOf(HDR_DISP_NOT_TO);

    /** "Content-Disposition" */
    public static final String HDR_CONTENT_DISPOSITION = "Content-Disposition".intern();

    public static final HeaderName CONTENT_DISPOSITION = HeaderName.valueOf(HDR_CONTENT_DISPOSITION);

    /** "Content-Type" */
    public static final String HDR_CONTENT_TYPE = "Content-Type".intern();

    public static final HeaderName CONTENT_TYPE = HeaderName.valueOf(HDR_CONTENT_TYPE);

    /** "MIME-Version" */
    public static final String HDR_MIME_VERSION = "MIME-Version".intern();

    public static final HeaderName MIME_VERSION = HeaderName.valueOf(HDR_MIME_VERSION);

    public static final String HDR_DISP_TO = HDR_DISP_NOT_TO;

    public static final HeaderName DISP_TO = HeaderName.valueOf(HDR_DISP_TO);

    /** "Organization" */
    public static final String HDR_ORGANIZATION = "Organization".intern();

    public static final HeaderName ORGANIZATION = HeaderName.valueOf(HDR_ORGANIZATION);

    /** "X-Mailer" */
    public static final String HDR_X_MAILER = "X-Mailer".intern();

    public static final HeaderName X_MAILER = HeaderName.valueOf(HDR_X_MAILER);

    /** "X-Originating-Client" */
    public static final String HDR_X_ORIGINATING_CLIENT = "X-Originating-Client".intern();

    public static final HeaderName X_ORIGINATING_CLIENT = HeaderName.valueOf(HDR_X_ORIGINATING_CLIENT);

    /** "X-OXMsgref" */
    public static final String HDR_X_OXMSGREF = "X-OXMsgref".intern();

    public static final HeaderName X_OXMSGREF = HeaderName.valueOf(HDR_X_OXMSGREF);

    public static final String HDR_ADDR_DELIM = ",";

    /** "X-Spam-Flag" */
    public static final String HDR_X_SPAM_FLAG = "X-Spam-Flag".intern();

    public static final HeaderName X_SPAM_FLAG = HeaderName.valueOf(HDR_X_SPAM_FLAG);

    /** "Content-ID" */
    public static final String HDR_CONTENT_ID = "Content-ID".intern();

    public static final HeaderName CONTENT_ID = HeaderName.valueOf(HDR_CONTENT_ID);

    /** "Content-Transfer-Encoding" */
    public static final String HDR_CONTENT_TRANSFER_ENC = "Content-Transfer-Encoding".intern();

    public static final HeaderName CONTENT_TRANSFER_ENC = HeaderName.valueOf(HDR_CONTENT_TRANSFER_ENC);

    /** "Content-Disposition" */
    public static final String HDR_DISPOSITION = "Content-Disposition".intern();

    public static final HeaderName DISPOSITION = HeaderName.valueOf(HDR_DISPOSITION);

    /** "X-OX-Marker" */
    public static final String HDR_X_OX_MARKER = "X-OX-Marker".intern();

    public static final HeaderName X_OX_MARKER = HeaderName.valueOf(HDR_X_OX_MARKER);

    /** "Received" */
    public static final String HDR_RECEIVED = "Received".intern();

    public static final HeaderName RECEIVED = HeaderName.valueOf(HDR_RECEIVED);

    /** "Return-Path" */
    public static final String HDR_RETURN_PATH = "Return-Path".intern();

    public static final HeaderName RETURN_PATH = HeaderName.valueOf(HDR_RETURN_PATH);

    /** "X-OX-VCard-Attached" */
    public static final String HDR_X_OX_VCARD = "X-OX-VCard-Attached".intern();

    public static final HeaderName X_OX_VCARD = HeaderName.valueOf(HDR_X_OX_VCARD);

    /** "X-OX-Notification" */
    public static final String HDR_X_OX_NOTIFICATION = "X-OX-Notification".intern();

    public static final HeaderName X_OX_NOTIFICATION = HeaderName.valueOf(HDR_X_OX_NOTIFICATION);

    /** "X-Part-Id" */
    public static final String HDR_X_PART_ID = "X-Part-Id".intern();

    public static final HeaderName X_PART_ID = HeaderName.valueOf(HDR_X_PART_ID);

}
