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

package com.openexchange.imap.command;

import static com.openexchange.imap.command.MailMessageFetchIMAPCommand.getFetchCommand;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.StorageUtility;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.UID;


/**
 * {@link MailMessageFillerIMAPCommand} - Fills a given collection of messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageFillerIMAPCommand extends AbstractIMAPCommand<Void> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageFillerIMAPCommand.class);

    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"

    private final TLongObjectMap<MailMessage> messages;
    private final int length;
    private final long[] uids;
    private final String command;
    private final String[] args;
    private final boolean uid;
    private final String fullname;
    private final int index;

    /**
     * Initializes a new {@link MailMessageFillerIMAPCommand}.
     */
    public MailMessageFillerIMAPCommand(Collection<MailMessage> messages, boolean isRev1, FetchProfile fp, IMAPServerInfo serverInfo, IMAPFolder imapFolder) throws MessagingException {
        super(imapFolder);
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        index = 0;
        length = messages.size();
        // Fill collection
        TLongObjectMap<MailMessage> tm = new TLongObjectHashMap<MailMessage>(length);
        TLongList tuids = new TLongArrayList(length);
        for (MailMessage message : messages) {
            if (null != message) {
                long uid = ((IDMailMessage) message).getUid();
                tm.put(uid, message);
                tuids.add(uid);
            }
        }
        this.messages = tm;
        this.uids = tuids.toArray();
        if (length == messageCount) {
            command = getFetchCommand(isRev1, checkFetchProfile(fp), false, serverInfo);
            args = (1 == length ? new String[] { "1" } : ARGS_ALL);
            uid = false;
        } else {
            command = getFetchCommand(isRev1, checkFetchProfile(fp), false, serverInfo);
            args = IMAPNumArgSplitter.splitUIDArg(uids, false, LENGTH_WITH_UID + command.length());
            uid = true;
        }
        if (0 == length) {
            returnDefaultValue = true;
        }
        fullname = imapFolder.getFullName();
    }

    @Override
    protected boolean addLoopCondition() {
        return (index < length);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(final int argsIndex) {
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        if (uid) {
            sb.append("UID ");
        }
        sb.append("FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (").append(command).append(')');
        return sb.toString();
    }

    @Override
    protected Void getDefaultValue() {
        return null;
    }

    @Override
    protected Void getReturnVal() throws MessagingException {
        return null;
    }

    @Override
    protected boolean handleResponse(final Response currentReponse) throws MessagingException {
        /*
         * Response is null or not a FetchResponse
         */
        if (!FetchResponse.class.isInstance(currentReponse)) {
            return false;
        }
        final FetchResponse fetchResponse = (FetchResponse) currentReponse;
        UID uid = IMAPCommandsCollection.getItemOf(UID.class, fetchResponse);
        if (null != uid) {
            MailMessage message = messages.get(uid.uid);
            if (null != message) {
                try {
                    MailMessageFetchIMAPCommand.handleFetchRespone((IDMailMessage) message, fetchResponse, fullname);
                } catch (OXException e) {
                    /*
                     * Discard corrupt message
                     */
                    LOG.warn("Message #{} discarded", Long.valueOf(uid.uid), e);
                }
            }
            return true;
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------- //

    private static FetchProfile checkFetchProfile(FetchProfile fetchProfile) {
        // Add UID item to FetchProfile if absent
        {
            boolean found = false;
            final Item uid = UIDFolder.FetchProfileItem.UID;
            final Item[] items = fetchProfile.getItems();
            for (int i = 0; !found && i < items.length; i++) {
                final Item cur = items[i];
                if (uid == cur) {
                    found = true;
                }
            }
            if (!found) {
                fetchProfile.add(uid);
            }
        }
        return fetchProfile;
    }

    /**
     * Parses the string argument as a signed decimal <code>long</code>. The characters in the string must all be decimal digits.
     * <p>
     * Note that neither the character <code>L</code> (<code>'&#92;u004C'</code>) nor <code>l</code> (<code>'&#92;u006C'</code>) is
     * permitted to appear at the end of the string as a type indicator, as would be permitted in Java programming language source code.
     *
     * @param s A <code>String</code> containing the <code>long</code> representation to be parsed
     * @return The <code>long</code> represented by the argument in decimal or <code>-1</code> if the string does not contain a parsable
     *         <code>long</code>.
     */
    protected static long parseLong(final String s) {
        return StorageUtility.parseUnsignedLong(s);
    }

}
