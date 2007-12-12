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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
 * MailJSONField
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
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
	SEEN("seen");

	private final String key;

	private MailJSONField(final String jsonKey) {
		this.key = jsonKey;
	}

	/**
	 * @return The JSON key
	 */
	public String getKey() {
		return key;
	}

}
