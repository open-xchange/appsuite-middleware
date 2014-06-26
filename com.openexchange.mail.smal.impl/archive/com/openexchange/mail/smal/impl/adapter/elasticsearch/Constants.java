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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailJSONField;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Constants {

    /**
     * Initializes a new {@link Constants}.
     */
    private Constants() {
        super();
    }

    /**
     * The max. chunk size for text fillers.
     */
    public static final int MAX_FILLER_CHUNK = 25;

    /**
     * The max. number of concurrent filler tasks: {@link Runtime#availableProcessors()}.
     */
    public static final int MAX_NUM_CONCURRENT_FILLER_TASKS = Runtime.getRuntime().availableProcessors();

    /**
     * The timeout in milliseconds applied when performing a request to ElasticSearch server.
     */
    public static final long TIMEOUT_MILLIS = 60000;

    /**
     * The name for the cluster.
     */
    public static final String CLUSTER_NAME = "mail_cluster";

    /**
     * The name for mail index.
     */
    public static final String INDEX_NAME_PREFIX = "mail_index_";

    /**
     * The type for mail index.
     */
    public static final String INDEX_TYPE = "mail";

    // ------------------- INDEXABLE FIELDS ----------------------

    public static final MailFields INDEXABLE_FIELDS = new MailFields(
        MailField.ACCOUNT_NAME,
        MailField.ID,
        MailField.FOLDER_ID,
        MailField.FROM,
        MailField.TO,
        MailField.CC,
        MailField.BCC,
        MailField.FLAGS,
        MailField.SIZE,
        MailField.SUBJECT,
        MailField.RECEIVED_DATE,
        MailField.SENT_DATE);

    // ------------------- FIELD NAMES ----------------------

    public static final String FIELD_TIMESTAMP = "timestamp";

    public static final String FIELD_UUID = "uuid";

    public static final String FIELD_USER_ID = "user";

    public static final String FIELD_ACCOUNT_ID = MailJSONField.ACCOUNT_NAME.getKey();

    public static final String FIELD_ID = "id";

    public static final String FIELD_FULL_NAME = MailJSONField.FOLDER.getKey();

    public static final String FIELD_BODY = MailJSONField.CONTENT.getKey();

    public static final String FIELD_FROM = MailJSONField.FROM.getKey();

    public static final String FIELD_TO = MailJSONField.RECIPIENT_TO.getKey();

    public static final String FIELD_CC = MailJSONField.RECIPIENT_CC.getKey();

    public static final String FIELD_BCC = MailJSONField.RECIPIENT_BCC.getKey();

    public static final String FIELD_SUBJECT = MailJSONField.SUBJECT.getKey();

    public static final String FIELD_RECEIVED_DATE = MailJSONField.RECEIVED_DATE.getKey();

    public static final String FIELD_SENT_DATE = MailJSONField.SENT_DATE.getKey();

    public static final String FIELD_SIZE = MailJSONField.SIZE.getKey();

    public static final String FIELD_FLAG_ANSWERED = "flag_answered";

    public static final String FIELD_FLAG_DELETED = "flag_deleted";

    public static final String FIELD_FLAG_DRAFT = "flag_draft";

    public static final String FIELD_FLAG_FLAGGED = "flag_flagged";

    public static final String FIELD_FLAG_RECENT = "flag_recent";

    public static final String FIELD_FLAG_SEEN = "flag_seen";

    public static final String FIELD_FLAG_USER = "flag_user";

    public static final String FIELD_FLAG_SPAM = "flag_spam";

    public static final String FIELD_FLAG_FORWARDED = "flag_forwarded";

    public static final String FIELD_FLAG_READ_ACK = "flag_read_ack";

}
