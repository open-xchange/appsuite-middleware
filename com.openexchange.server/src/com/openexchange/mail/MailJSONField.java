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
     *
     * @since v7.8.0
     */
    ORIGINAL_ID("original_id"),
    /**
     * The original folder ID
     *
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
    /**
     * The date of a mail message. As configured, either the internal received date or mail's sent date (as given by <code>"Date"</code> header).
     */
    DATE("date"),
    /**
     * If the E-Mail is Guard-encrypted
     */
    SECURITY("security"),
    /**
     * If the E-Mail contains encryption or signatures
     */
    SECURITY_INFO("security_info"),
    /**
     * A message's text preview
     *
     * @since v7.10.0
     */
    TEXT_PREVIEW("text_preview"),
    /**
     * The message's authenticity results.
     */
    AUTHENTICITY("authenticity"),
    /**
     * A preview of the message's authenticity results.
     */
    AUTHENTICITY_PREVIEW("authenticity_preview")

    ;

    private final String key;

    private MailJSONField(String jsonKey) {
        key = jsonKey;
    }

    /**
     * @return The JSON key
     */
    public String getKey() {
        return key;
    }

}
