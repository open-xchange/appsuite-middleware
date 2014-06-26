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

package com.openexchange.mail.smal.impl.adapter.solrj;

/**
 * {@link SolrConstants} - Provides constants.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SolrConstants {

    public static final String FIELD_TIMESTAMP = "timestamp";

    public static final String FIELD_UUID = "uuid";

    public static final String FIELD_CONTEXT = "context";

    public static final String FIELD_USER = "user";

    public static final String FIELD_ACCOUNT = "account";

    public static final String FIELD_FULL_NAME = "full_name";

    public static final String FIELD_ID = "id";

    public static final String FIELD_FROM_PERSONAL = "from_personal";

    public static final String FIELD_FROM_ADDR = "from_addr";

    public static final String FIELD_FROM_PLAIN = "from_plain";

    public static final String FIELD_SENDER_PERSONAL = "sender_personal";

    public static final String FIELD_SENDER_ADDR = "sender_addr";

    public static final String FIELD_SENDER_PLAIN = "sender_plain";

    public static final String FIELD_TO_PERSONAL = "to_personal";

    public static final String FIELD_TO_ADDR = "to_addr";

    public static final String FIELD_TO_PLAIN = "to_plain";

    public static final String FIELD_CC_PERSONAL = "cc_personal";

    public static final String FIELD_CC_ADDR = "cc_addr";

    public static final String FIELD_CC_PLAIN = "cc_plain";

    public static final String FIELD_BCC_PERSONAL = "bcc_personal";

    public static final String FIELD_BCC_ADDR = "bcc_addr";

    public static final String FIELD_BCC_PLAIN = "bcc_plain";

    public static final String FIELD_ATTACHMENT = "attachment";

    public static final String FIELD_COLOR_LABEL = "color_label";

    public static final String FIELD_SIZE = "size";

    public static final String FIELD_RECEIVED_DATE = "received_date";

    public static final String FIELD_SENT_DATE = "sent_date";

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

    public static final String FIELD_USER_FLAGS = "user_flags";

    public static final String FIELD_SUBJECT_PREFIX = "subject_";

    public static final String FIELD_SUBJECT_PLAIN = "subject_plain";

    public static final String FIELD_CONTENT_PREFIX = "content_";

    public static final String FIELD_CONTENT_FLAG = "content_flag";

    /*-
     * --------------------------- TEXT FILLER CONSTANTS ---------------------
     */

    public static final int MAX_NUM_CONCURRENT_FILLER_TASKS = Runtime.getRuntime().availableProcessors() << 1;

    public static final int MAX_FILLER_CHUNK = 75;

}
