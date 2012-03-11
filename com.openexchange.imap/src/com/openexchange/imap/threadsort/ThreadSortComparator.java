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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap.threadsort;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;

/**
 * {@link ThreadSortComparator} - Sorts according to <small><b><a href="http://tools.ietf.org/html/rfc5256">RFC5256</a></b></small>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadSortComparator {

    /**
     * Initializes a new {@link ThreadSortComparator}.
     */
    private ThreadSortComparator() {
        super();
    }

    public static MailMessage[] doThreadSort(final MailMessage[] mailMessages) {
        /*
         * The REFERENCES threading algorithm threads the searched messages by grouping them together in parent/child relationships based on
         * which messages are replies to others. The parent/child relationships are built using two methods: reconstructing a message's
         * ancestry using the references contained within it; and checking the original (not base) subject of a message to see if it is a
         * reply to (or forward of) another message.
         */

        // 1.) Normalize Message-Id headers
        normalize(mailMessages);
        // 2.) Iterate messages
        final List<ThreadSortMailMessage> roots = new ArrayList<ThreadSortMailMessage>(mailMessages.length);
        final Map<String, ThreadSortMailMessage> referenceMap = new HashMap<String, ThreadSortMailMessage>(mailMessages.length);
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            final String[] references = getMessageReferences(mailMessage);
            if (null == references || references.length == 0) {
                roots.add(new ThreadSortMailMessage(mailMessage));
                continue;
            }
            for (int j = 0; j < references.length; j++) {
                final String reference = references[j];
                ThreadSortMailMessage tsmm = referenceMap.get(reference);
                if (null == tsmm) {
                    tsmm = getByReference(reference, mailMessages);
                    referenceMap.put(reference, tsmm);
                }
            }

        }


        return null;
    }

    private static ThreadSortMailMessage getByReference(final String reference, final MailMessage[] mailMessages) {
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            final String messageId = mailMessage.getFirstHeader("Message-Id");
            if (reference.equalsIgnoreCase(messageId)) {
                return new ThreadSortMailMessage(mailMessage);
            }
        }
        final MailMessage dummy = new DummyMailMessage();
        dummy.addHeader("Message-Id", reference);
        return new ThreadSortMailMessage(dummy);
    }

    private static String[] getMessageReferences(final MailMessage mailMessage) {
        /*
         * If a message contains a References header line, then use the Message IDs in the References header line as the references.
         */
        {
            final String referencesHdr = mailMessage.getFirstHeader("References");
            final String[] references = null == referencesHdr ? new String[0] : unfold(referencesHdr).split("\\s*,\\s*");
            if (references.length > 0) {
                return references;
            }
        }
        /*
         * If a message does not contain a References header line, or the References header line does not contain any valid Message IDs,
         * then use the first (if any) valid Message ID found in the In-Reply-To header line as the only reference (parent) for this
         * message.
         */
        {
            final String inReplyToHdr = mailMessage.getFirstHeader("In-Reply-To");
            final String ref = null == inReplyToHdr ? null : unfold(inReplyToHdr).split("\\s*,\\s*")[0];
            if (null != ref && ref.length() > 0) {
                return new String[] { ref };
            }
        }
        /*
         * If a message does not contain an In-Reply-To header line, or the In-Reply-To header line does not contain a valid Message ID,
         * then the message does not have any references (NIL).
         */
        return new String[0];
    }

    private static void normalize(final MailMessage[] mailMessages) {
        final StringBuilder sb = new StringBuilder(64);
        final Set<String> ids = new HashSet<String>(mailMessages.length);
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            String messageId = mailMessage.getFirstHeader("Message-Id");
            if (null == messageId) {
                sb.setLength(0);
                sb.append(getNewUniqueId());
                sb.append("@xxx.dummy.com");
                messageId = sb.toString();
            }
            mailMessage.removeHeader("Message-Id");
            String normalizeID = normalizeID(messageId, sb);
            if (ids.contains(normalizeID)) {
                sb.setLength(0);
                sb.append(getNewUniqueId());
                sb.append("@xxx.dummy.com");
                normalizeID = sb.toString();
            }
            ids.add(normalizeID);
            mailMessage.addHeader("Message-Id", normalizeID);
        }
    }

    private static final Pattern PATTERN = Pattern.compile("<\"(.*?)\">@(.*)");

    private static String normalizeID(final String messageId, final StringBuilder sb) {
        final Matcher m = PATTERN.matcher(messageId);
        if (m.matches()) {
            sb.setLength(0);
            return sb.append('<').append(m.group(1)).append(">@").append(m.group(2)).toString();
        }
        return messageId;
    }

    private static String getNewUniqueId() {
        final StringBuilder s = new StringBuilder(36).append(UUID.randomUUID());
        s.deleteCharAt(23);
        s.deleteCharAt(18);
        s.deleteCharAt(13);
        s.deleteCharAt(8);
        return s.toString();
    }
}
