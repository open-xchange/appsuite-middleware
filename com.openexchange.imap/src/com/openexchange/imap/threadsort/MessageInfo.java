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

package com.openexchange.imap.threadsort;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.Collection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link MessageInfo} - A message information in thread-sort string;
 * <p>
 * E.g. <code>"${23}"</code> or <code>"${INBOX/110}"</code> or <code>"${0/INBOX/110}"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageInfo {

    /**
     * The dummy message identifier.
     */
    public static final MessageInfo DUMMY = new MessageInfo(-1);

    /**
     * The <code>'/'</code> character which separates folder's full name from mail's ID in a mail path
     */
    private static final char SEPERATOR = '/';

    /**
     * Extracts the message numbers from specified collection
     *
     * @param messageIds The message identifiers
     * @return The message numbers
     */
    public static TIntList toSeqNums(final Collection<MessageInfo> messageIds) {
        final TIntList ret = new TIntArrayList(messageIds.size());
        for (final MessageInfo messageId : messageIds) {
            ret.add(messageId.messageNumber);
        }
        return ret;
    }

    /**
     * Extracts the message numbers from specified collection
     *
     * @param messageIds The message identifiers
     * @return The message numbers
     */
    public static int[] toSeqNumsArray(final Collection<MessageInfo> messageIds) {
        final int[] ret = new int[messageIds.size()];
        int i = 0;
        for (final MessageInfo messageId : messageIds) {
            ret[i++] = messageId.messageNumber;
        }
        return ret;
    }

    /**
     * Gets the message identifier for specified string.
     *
     * @param msgId The message idenfifier's string representation
     * @return The message identifier
     */
    public static MessageInfo valueOf(final String msgId) {
        return valueOf(msgId, 0, msgId.length());
    }

    /**
     * Gets the message identifier for specified string.
     *
     * @param msgId The message idenfifier's string representation
     * @return The message identifier
     */
    public static MessageInfo valueOf(final String msgId, final int off, final int len) {
        if (com.openexchange.java.Strings.isEmpty(msgId)) {
            return null;
        }
        int pos = off;
        if ('{' == msgId.charAt(pos)) {
            pos++;
        }
        final int end = '}' == msgId.charAt(len - 1) ? len - 1 : len;
        final int sepPos = msgId.lastIndexOf(SEPERATOR, end);
        if (sepPos < 0) {
            return new MessageInfo(getUnsignedInteger(msgId.substring(pos, end))).setSlen(len);
        }
        final String firstPart = msgId.substring(pos, sepPos);
        final int firstSep = firstPart.indexOf(SEPERATOR);
        if (firstSep < 0) {
            return new MessageInfo().setFullName(firstPart).setMessageNumber(getUnsignedInteger(msgId.substring(sepPos + 1, end))).setSlen(len);
        }
        final int accId = getUnsignedInteger(firstPart.substring(0, firstSep));
        if (accId < 0) {
            return new MessageInfo().setFullName(firstPart).setMessageNumber(getUnsignedInteger(msgId.substring(sepPos + 1, end))).setSlen(len);
        }
        final MessageInfo messageId = new MessageInfo().setAccountId(accId).setSlen(len);
        return messageId.setFullName(firstPart.substring(firstSep + 1)).setMessageNumber(getUnsignedInteger(msgId.substring(sepPos + 1, end)));
    }

    private int messageNumber;
    private String fullName;
    private int accountId;
    private int slen;

    /**
     * Initializes a new {@link MessageInfo}.
     */
    public MessageInfo() {
        this(-1);
    }

    /**
     * Initializes a new {@link MessageInfo}.
     *
     * @param messageNumber The message number
     */
    public MessageInfo(final int messageNumber) {
        super();
        this.messageNumber = messageNumber;
        accountId = -1;
        slen = -1;
    }

    /**
     * Gets the <code>slen</code>
     *
     * @return The <code>slen</code>
     */
    public int getSlen() {
        return slen;
    }

    /**
     * Sets the <code>slen</code>
     *
     * @param slen The <code>slen</code> to set
     * @return This message identifier
     */
    public MessageInfo setSlen(final int slen) {
        this.slen = slen;
        return this;
    }

    /**
     * Gets the message number
     *
     * @return The message number
     */
    public int getMessageNumber() {
        return messageNumber;
    }

    /**
     * Sets the message number
     *
     * @param messageNumber The message number to set
     * @return This message identifier
     */
    public MessageInfo setMessageNumber(final int messageNumber) {
        this.messageNumber = messageNumber;
        return this;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name
     *
     * @param fullName The full name to set
     * @return This message identifier
     */
    public MessageInfo setFullName(final String fullName) {
        this.fullName = fullName;
        return this;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Sets the account identifier
     *
     * @param accountId The account identifier to set
     * @return This message identifier
     */
    public MessageInfo setAccountId(final int accountId) {
        this.accountId = accountId;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(16).append('{');
        if (accountId >= 0) {
            builder.append(accountId).append(SEPERATOR);
        }
        if (fullName != null) {
            builder.append(fullName).append(SEPERATOR);
        }
        builder.append(messageNumber);
        builder.append('}');
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // result = prime * result + accountId;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + messageNumber;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MessageInfo)) {
            return false;
        }
        final MessageInfo other = (MessageInfo) obj;
        // if (accountId != other.accountId) {
        // return false;
        // }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }
        if (messageNumber != other.messageNumber) {
            return false;
        }
        return true;
    }
}
