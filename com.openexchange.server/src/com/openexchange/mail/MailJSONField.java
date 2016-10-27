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

package com.openexchange.mail;

import com.openexchange.ajax.fields.CommonFields;

/**
 * {@link MailJSONField} - An enumeration of mail JSON fields as defined in <a href=
 * "http://www.open-xchange.com/wiki/index.php?title=HTTP_API#Module_.22mail.22" >HTTP API's mail section</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailJSONField {

    /**
     * Attachment's file name
     */
    ATTACHMENT_FILE_NAME("filename"),
    /**
     * Attachment's unique disk file name
     */
    ATTACHMENT_UNIQUE_DISK_FILE_NAME("disk"),
    /**
     * Headers
     */
    HEADERS("headers"),
    /**
     * Mail truncated: true, false
     */
    TRUNCATED("truncated"),
    /**
     * Nested messages
     */
    NESTED_MESSAGES("nested_msgs"),
    /**
     * Value
     */
    VALUE("value"),
    /**
     * Thread level
     */
    THREAD_LEVEL("level"),
    /**
     * Flags
     */
    FLAGS("flags"),
    /**
     * From
     */
    FROM("from"),
    /**
     * To
     */
    RECIPIENT_TO("to"),
    /**
     * Cc
     */
    RECIPIENT_CC("cc"),
    /**
     * Bcc
     */
    RECIPIENT_BCC("bcc"),
    /**
     * Subject
     */
    SUBJECT("subject"),
    /**
     * Sent date
     */
    SENT_DATE("sent_date"),
    /**
     * Received date
     */
    RECEIVED_DATE("received_date"),
    /**
     * Size
     */
    SIZE("size"),
    /**
     * Header <code>Content-Type</code>
     */
    CONTENT_TYPE("content_type"),
    /**
     * Header <code>Content-ID</code>
     */
    CID("cid"),
    /**
     * Content
     */
    CONTENT("content"),
    /**
     * Attachments
     */
    ATTACHMENTS("attachments"),
    /**
     * Whether a message has attachments
     */
    HAS_ATTACHMENTS("attachment"),
    /**
     * Disposition
     */
    DISPOSITION("disp"),
    /**
     * User
     */
    USER("user"),
    /**
     * Header <code>Disposition-Notification-To</code>
     */
    DISPOSITION_NOTIFICATION_TO("disp_notification_to"),
    /**
     * Priority
     */
    PRIORITY("priority"),
    /**
     * Message reference
     */
    MSGREF("msgref"),
    /**
     * Color label
     */
    COLOR_LABEL(CommonFields.COLORLABEL),
    /**
     * Infostore IDs
     */
    INFOSTORE_IDS("infostore_ids"),
    /**
     * Data sources
     */
    DATASOURCES("datasources"),
    /**
     * VCard
     */
    VCARD("vcard"),
    /**
     * Total count
     */
    TOTAL("total"),
    /**
     * New count
     */
    NEW("new"),
    /**
     * Unread count
     */
    UNREAD("unread"),
    /**
     * Deleted count
     */
    DELETED("deleted"),
    /**
     * Folder
     */
    FOLDER("folder"),
    /**
     * Flag \SEEN
     */
    SEEN("seen"),
    /**
     * modified
     */
    MODIFIED("modified"),
    /**
     * Account name
     */
    ACCOUNT_NAME("account_name"),
    /**
     * Account identifier
     */
    ACCOUNT_ID("account_id"),
    /**
     * The original mail ID.
     * @since v7.8.0
     */
    ORIGINAL_ID("original_id"),
    /**
     * The original folder ID
     * @since v7.8.0
     */
    ORIGINAL_FOLDER_ID("original_folder_id"),
    /**
     * Flag \ANSWERED
     */
    ANSWERED("answered"),
    /**
     * Flag \FORWARDED
     */
    FORWARDED("forwarded"),
    /**
     * Flag \DRAFT
     */
    DRAFT("draft"),
    /**
     * Flag \FLAGGED
     */
    FLAGGED("flagged"),
    ;


    private final String key;

    private MailJSONField(final String jsonKey) {
        key = jsonKey;
    }

    /**
     * @return The JSON key
     */
    public String getKey() {
        return key;
    }

}
