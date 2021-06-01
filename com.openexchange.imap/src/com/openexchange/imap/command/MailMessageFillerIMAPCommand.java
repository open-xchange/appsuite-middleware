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

package com.openexchange.imap.command;

import static com.openexchange.imap.command.MailMessageFetchIMAPCommand.getFetchCommand;
import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPServerInfo;
import com.openexchange.mail.PreviewMode;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.StorageUtility;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.UID;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * {@link MailMessageFillerIMAPCommand} - Fills a given collection of messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageFillerIMAPCommand extends AbstractIMAPCommand<Void> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MailMessageFillerIMAPCommand.class);

    private static final int LENGTH_WITH_UID = 13; // "UID FETCH <nums> (<command>)"

    private final TLongObjectMap<MailMessage> messages;
    private final int accountId;
    private final int length;
    private final long[] uids;
    private final String command;
    private final String[] args;
    private final boolean uid;
    private final String fullname;
    private final int index;
    private final boolean examineHasAttachmentUserFlags;

    /**
     * Initializes a new {@link MailMessageFillerIMAPCommand}.
     *
     * @param messages The messages to fill
     * @param isRev1 Whether IMAP server has <i>IMAP4rev1</i> capability or not
     * @param fp The fetch profile to use
     * @param serverInfo The IMAP server information deduced from configuration
     * @param examineHasAttachmentUserFlags Whether has-attachment user flags should be considered
     * @param previewMode Whether target IMAP server supports any preview> capability
     * @param imapFolder The IMAP folder providing connected protocol
     * @throws MessagingException If initialization fails
     */
    public MailMessageFillerIMAPCommand(Collection<MailMessage> messages, boolean isRev1, FetchProfile fp, IMAPServerInfo serverInfo, boolean examineHasAttachmentUserFlags, PreviewMode previewMode, IMAPFolder imapFolder) throws MessagingException {
        super(imapFolder);
        this.examineHasAttachmentUserFlags = examineHasAttachmentUserFlags;
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            returnDefaultValue = true;
        }
        accountId = null == serverInfo ? 0 : serverInfo.getAccountId();
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
            command = getFetchCommand(isRev1, checkFetchProfile(fp), false, serverInfo, previewMode);
            args = (1 == length ? ARGS_FIRST : ARGS_ALL);
            uid = false;
        } else {
            command = getFetchCommand(isRev1, checkFetchProfile(fp), false, serverInfo, previewMode);
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
    protected String getCommand(int argsIndex) {
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
    protected boolean handleResponse(Response currentReponse) throws MessagingException {
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
                    IDMailMessage idm = (IDMailMessage) message;
                    idm.setAccountId(accountId);
                    MailMessageFetchIMAPCommand.handleFetchRespone(idm, fetchResponse, fullname, examineHasAttachmentUserFlags);
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
    protected static long parseLong(String s) {
        return StorageUtility.parseUnsignedLong(s);
    }

}
