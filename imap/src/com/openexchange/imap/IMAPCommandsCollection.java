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

package com.openexchange.imap;

import static com.openexchange.imap.sort.IMAPSort.getMessageComparator;
import static com.openexchange.mail.mime.utils.MIMEStorageUtility.getFetchProfile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import com.openexchange.imap.command.FetchIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.tools.Collections.SmartIntArray;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.INTERNALDATE;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link IMAPCommandsCollection} - A collection of simple IMAP commands.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPCommandsCollection {

    private static final String STR_UID = "UID";

    private static final String STR_FETCH = "FETCH";

    private static final String ERR_UID_STORE_NOT_SUPPORTED = "UID STORE not supported";

    private static final String STR_INVALID_SYSTEM_FLAG = "Invalid system flag";

    private static final String ERR_INVALID_SYSTEM_FLAG_DETECTED = "Invalid System Flag detected";

    static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPCommandsCollection.class);

    /**
     * Server does not support %s command.
     */
    private static final String PROTOCOL_ERROR_TEMPL = "Server does not support %s command";

    /**
     * Prevent instantiation.
     */
    private IMAPCommandsCollection() {
        super();
    }

    /**
     * Updates specified IMAP folder's internal <code>total</code> and <code>recent</code> counters through executing an
     * <code>EXAMINE</code> or <code>SELECT</code> command dependent on IMAP folder's open mode.
     * 
     * @param imapFolder The IMAP folder to update
     * @throws MessagingException If a messaging error occurs
     */
    public static void updateIMAPFolder(final IMAPFolder imapFolder) throws MessagingException {
        updateIMAPFolder(imapFolder, imapFolder.getMode());
    }

    /**
     * Updates specified IMAP folder's internal <code>total</code> and <code>recent</code> counters through executing an
     * <code>EXAMINE</code> or <code>SELECT</code> command dependent on specified mode.
     * 
     * @param imapFolder The IMAP folder to update
     * @param mode The mode in which the folder is opened
     * @throws MessagingException If a messaging error occurs
     */
    public static void updateIMAPFolder(final IMAPFolder imapFolder, final int mode) throws MessagingException {
        imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            /*
             * (non-Javadoc)
             * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun .mail.imap.protocol.IMAPProtocol)
             */
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Encode the mbox as per RFC2060
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(imapFolder.getFullName()));
                /*
                 * Perform command
                 */
                final Response[] tmp = mode == Folder.READ_ONLY ? p.command("EXAMINE", args) : p.command("SELECT", args);
                final Response[] r = new Response[tmp.length - 1];
                System.arraycopy(tmp, 0, r, 0, r.length);
                /*
                 * Dispatch responses and thus update folder when handling untagged responses of EXISTS and RECENT
                 */
                p.notifyResponseHandlers(r);
                p.handleResult(tmp[tmp.length - 1]);
                return null;
            }
        });
    }

    /**
     * Checks if IMAP root folder allows subfolder creation.
     * 
     * @param rootFolder The IMAP root folder
     * @return <code>true</code> if IMAP root folder allows subfolder creation; otherwise <code>false</code>
     * @throws MessagingException If checking IMAP root folder for subfolder creation fails
     */
    public static boolean canCreateSubfolder(final DefaultFolder rootFolder) throws MessagingException {
        return ((Boolean) rootFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Encode the mbox as per RFC2060
                 */
                final String mboxName = prepareStringArgument(String.valueOf(System.currentTimeMillis()));
                /*
                 * Perform command: CREATE
                 */
                final StringBuilder sb = new StringBuilder(7 + mboxName.length());
                final Response[] r = p.command(sb.append("CREATE ").append(mboxName).toString(), null);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    sb.setLength(0);
                    p.command(sb.append("DELETE ").append(mboxName).toString(), null);
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        })).booleanValue();
    }

    /**
     * Checks if IMAP folder's prefix allows subfolder creation.
     * 
     * @param prefix The IMAP folder's prefix
     * @param imapFolder The IMAP folder providing the IMAP connection
     * @return <code>true</code> if subfolder are allowed; otherwise <code>false</code>
     * @throws MessagingException If checking IMAP root folder for subfolder creation fails
     */
    public static boolean canCreateSubfolder(final String prefix, final IMAPFolder imapFolder) throws MessagingException {
        return ((Boolean) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Encode the mbox as per RFC2060
                 */
                final String now = String.valueOf(System.currentTimeMillis());
                final StringBuilder sb = new StringBuilder(now.length() + prefix.length() + 16);
                final String mboxName = prepareStringArgument(sb.append(prefix).append(now).toString());
                /*
                 * Perform command: CREATE
                 */
                sb.setLength(0);
                final Response[] r = p.command(sb.append("CREATE ").append(mboxName).toString(), null);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    sb.setLength(0);
                    p.command(sb.append("DELETE ").append(mboxName).toString(), null);
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        })).booleanValue();
    }

    public static int[] getStatus(final IMAPFolder imapFolder) throws MessagingException {
        return (int[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                if (!protocol.isREV1() && !protocol.hasCapability("IMAP4SUNVERSION")) {
                    /*
                     * STATUS is rev1 only, however the non-rev1 SIMS2.0 does support this.
                     */
                    throw new com.sun.mail.iap.BadCommandException("STATUS not supported");
                }
                /*
                 * Encode the mbox as per RFC2060
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(imapFolder.getFullName()));
                /*
                 * Item arguments
                 */
                final Argument itemArgs = new Argument();
                final String[] items = { "MESSAGES", "RECENT", "UNSEEN" };
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = protocol.command("STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int total = -1;
                int recent = -1;
                int unseen = -1;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            final int[] status = parseStatusResponse(ir);
                            if (status[0] != -1) {
                                total = status[0];
                            }
                            if (status[1] != -1) {
                                recent = status[1];
                            }
                            if (status[2] != -1) {
                                unseen = status[2];
                            }
                            r[i] = null;
                        }
                    }
                }
                /*
                 * Dispatch remaining untagged responses
                 */
                protocol.notifyResponseHandlers(r);
                protocol.handleResult(response);
                return new int[] { total, recent, unseen };
            }
        });
    }

    /**
     * Parses number of total, recent and unread messages from specified IMAP response whose key is equal to <code>&quot;STATUS&quot;</code>
     * .
     * 
     * @param statusResponse The <code>&quot;STATUS&quot;</code> IMAP response to parse.
     * @return An array of <code>int</code> with length of <code>3</code> containing number of total (index <code>0</code>), recent (index
     *         <code>1</code>) and unread (index <code>2</code>) messages
     * @throws ParsingException If parsing STATUS response fails
     */
    static int[] parseStatusResponse(final Response statusResponse) throws ParsingException {
        /*
         * Read until opening parenthesis or EOF
         */
        byte b = 0;
        do {
            b = statusResponse.readByte();
        } while (b != 0 && b != '(');
        if (0 == b) {
            // EOF
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        /*
         * Parse parenthesized list
         */
        int total = -1;
        int recent = -1;
        int unseen = -1;
        do {
            final String attr = statusResponse.readAtom();
            if (attr.equalsIgnoreCase("MESSAGES")) {
                total = statusResponse.readNumber();
            } else if (attr.equalsIgnoreCase("RECENT")) {
                recent = statusResponse.readNumber();
            } else if (attr.equalsIgnoreCase("UNSEEN")) {
                unseen = statusResponse.readNumber();
            }
        } while (statusResponse.readByte() != ')');
        return new int[] { total, recent, unseen };
    }

    /**
     * Get the quotas for the quota-root associated with given IMAP folder. Note that many folders may have the same quota-root. Quotas are
     * controlled on the basis of a quota-root, not (necessarily) a folder. The relationship between folders and quota-roots depends on the
     * IMAP server. Some servers might implement a single quota-root for all folders owned by a user. Other servers might implement a
     * separate quota-root for each folder. A single folder can even have multiple quota-roots, perhaps controlling quotas for different
     * resources.
     * 
     * @param imapFolder The IMAP folder whose quotas shall be determined
     * @return The quotas for the quota-root associated with given IMAP folder
     * @throws MessagingException If determining the quotas fails
     */
    public static Quota[] getQuotaRoot(final IMAPFolder imapFolder) throws MessagingException {
        return (Quota[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Encode the mbox as per RFC2060
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(imapFolder.getFullName()));
                /*
                 * Perform command
                 */
                final Response[] r = p.command("GETQUOTAROOT", args);
                final Response response = r[r.length - 1];
                /*
                 * Create map for parsed responses
                 */
                final Map<String, Quota> tab = new HashMap<String, Quota>(2);
                if (response.isOK()) {
                    for (int i = 0; i < r.length; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("QUOTAROOT")) {
                            /*
                             * Read name of mailbox and throw away
                             */
                            ir.readAtomString();
                            /*
                             * For each quota-root add a place holder quota
                             */
                            String root = null;
                            while ((root = ir.readAtomString()) != null) {
                                tab.put(root, new Quota(root));
                            }
                            r[i] = null;
                        } else if (ir.keyEquals("QUOTA")) {
                            final Quota quota = parseQuota(ir);
                            // final Quota q = tab.get(quota.quotaRoot);
                            // if (q != null && q.resources != null) {
                            // should merge resources
                            // }
                            tab.put(quota.quotaRoot, quota);
                            r[i] = null;
                        }
                    }
                }
                /*
                 * Dispatch responses and thus update folder when handling untagged responses of EXISTS and RECENT
                 */
                p.notifyResponseHandlers(r);
                p.handleResult(response);
                /*
                 * Create return value
                 */
                final Quota[] qa = new Quota[tab.size()];
                final Iterator<Quota> iter = tab.values().iterator();
                final int size = tab.size();
                for (int i = 0; i < size; i++) {
                    qa[i] = iter.next();
                }
                return qa;
            }
        });
    }

    /**
     * Parse a QUOTA response.
     * 
     * @param r The IMAP response representing QUOTA response
     * @return The parsed instance of {@link Quota}
     * @throws ParsingException If parsing QUOTA response fails
     */
    static Quota parseQuota(final IMAPResponse r) throws ParsingException {
        final String quotaRoot = r.readAtomString();
        final Quota q = new Quota(quotaRoot);
        r.skipSpaces();
        {
            final byte b = r.readByte();
            if (b != '(') {
                if (b == '\0' || b == '\r' || b == '\n') {
                    /*
                     * Some IMAP servers indicate no resource restriction through a missing parenthesis pair instead of an empty one.
                     */
                    q.resources = new Quota.Resource[0];
                    return q;
                }
                throw new ParsingException("parse error in QUOTA");
            }
        }
        /*
         * quota_list ::= "(" #quota_resource ")"
         */
        final List<Quota.Resource> l = new ArrayList<Quota.Resource>(4);
        while (r.peekByte() != ')') {
            /*
             * quota_resource ::= atom SP number SP number
             */
            final String name = r.readAtom();
            if (name != null) {
                final long usage = r.readLong();
                final long limit = r.readLong();
                /*
                 * Add quota resource
                 */
                l.add(new Quota.Resource(name, usage, limit));
            }
        }
        r.readByte();
        q.resources = l.toArray(new Quota.Resource[l.size()]);
        return q;
    }

    /**
     * The reason for this routine is because the javamail library checks for existence when attempting to alter the subscription on the
     * specified folder. The problem is that we might be subscribed to a folder that either no longer exists (deleted by another IMAP
     * client), or that we simply do not currently have access permissions to (a shared folder that we are no longer permitted to see.)
     * Either way, we need to be able to unsubscribe to that folder if so desired. The current javamail routines will not allow us to do
     * that.
     * <P>
     * (Technically this is rather wrong of them. The IMAP spec makes it very clear that folder subscription should NOT depend upon the
     * existence of said folder. They even demonstrate a case in which it might indeed be valid to be subscribed to a folder that does not
     * appear to exist at a given moment.)
     */
    public static void forceSetSubscribed(final Store store, final String folder, final boolean subscribe) {
        final String lfolder = BASE64MailboxEncoder.encode(folder);
        final String cmd = (subscribe ? "SUBSCRIBE" : "UNSUBSCRIBE");
        try {
            final IMAPFolder f = (IMAPFolder) store.getDefaultFolder();
            // Object val =
            f.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol p) {
                    final Argument args = new Argument();
                    args.writeString(lfolder);
                    // Response[] r =
                    p.command(cmd, args);
                    return null;
                }
            });
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static final String COMMAND_LSUB = "LSUB";

    /**
     * Checks folder subscription for the folder denoted by specified fullname.
     * <p>
     * This method imitates the behavior from {@link IMAPFolder#isSubscribed() isSubscribde()} that is a namespace folder's subscription
     * status is checked with specified separator character appended to fullname.
     * 
     * @param fullname The folder's fullname
     * @param separator The separator character
     * @param isNamespace <code>true</code> if denoted folder is a namespace folder; otherwise <code>false</code>
     * @param defaultFolder The IMAP store's default folder
     * @return <code>true</code> if folder is subscribed; otherwise <code>false</code>
     * @throws MessagingException If checking folder subscription fails
     */
    public static boolean isSubscribed(final String fullname, final char separator, final boolean isNamespace, final IMAPFolder defaultFolder) throws MessagingException {
        final String lfolder = ((isNamespace || (fullname.length() == 0)) && (separator != '\0')) ? fullname + separator : fullname;
        return ((Boolean) (defaultFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(new StringBuilder().append(COMMAND_LSUB).append(" \"\" ").append(
                    prepareStringArgument(lfolder)).toString(), null);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    int res = -1;
                    final int len = r.length - 1;
                    for (int i = 0; i < len; i++) {
                        if ((r[i] instanceof IMAPResponse) && (res = parseIMAPResponse((IMAPResponse) r[i])) != -1) {
                            return res == 0 ? Boolean.FALSE : Boolean.TRUE;
                        }
                        r[i] = null;
                    }
                }
                /*
                 * Dispatch responses and thus update folder when handling untagged responses of EXISTS and RECENT
                 */
                p.notifyResponseHandlers(r);
                p.handleResult(response);
                return Boolean.FALSE;
            }

            private int parseIMAPResponse(final IMAPResponse ir) throws ParsingException {
                if (ir.keyEquals(COMMAND_LSUB)) {
                    final ListInfo li = new ListInfo(ir);
                    if (li.name.equals(fullname)) {
                        return li.canOpen ? 1 : 0;
                    }
                }
                return -1;
            }
        }))).booleanValue();
    }

    private final static String TEMPL_UID_STORE_FLAGS = "UID STORE %s %sFLAGS (%s)";

    private static final String ALL_COLOR_LABELS = "$cl_0 $cl_1 $cl_2 $cl_3 $cl_4 $cl_5 $cl_6 $cl_7 $cl_8 $cl_9 $cl_10" + " cl_0 cl_1 cl_2 cl_3 cl_4 cl_5 cl_6 cl_7 cl_8 cl_9 cl_10";

    /**
     * Clears all set color label (which are stored as user flags) from messages which correspond to given UIDs.
     * <p>
     * All known color labels:
     * <code>$cl_0&nbsp;$cl_1&nbsp;$cl_2&nbsp;$cl_3&nbsp;$cl_4&nbsp;$cl_5&nbsp;$cl_6&nbsp;$cl_7&nbsp;$cl_8&nbsp;$cl_9&nbsp;$cl_10</code>
     * <code>cl_0&nbsp;cl_1&nbsp;cl_2&nbsp;cl_3&nbsp;cl_4&nbsp;cl_5&nbsp;cl_6&nbsp;cl_7&nbsp;cl_8&nbsp;cl_9&nbsp;cl_10</code>
     * 
     * @param imapFolder - the imap folder
     * @param msgUIDs - the message UIDs
     * @return <code>true</code> if everything went fine; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static boolean clearAllColorLabels(final IMAPFolder imapFolder, final long[] msgUIDs) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false, 160);
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {
                    r = p.command(String.format(TEMPL_UID_STORE_FLAGS, args[i], "-", ALL_COLOR_LABELS), null);
                    response = r[r.length - 1];
                    try {
                        if (response.isOK()) {
                            continue Next;
                        } else if (response.isBAD() && (response.getRest() != null) && (response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1)) {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_INVALID_SYSTEM_FLAG_DETECTED));
                        } else {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_UID_STORE_NOT_SUPPORTED));
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }

        }))).booleanValue();
    }

    /**
     * Applies the given color flag as an user flag to the messages corresponding to given UIDS.
     * 
     * @param imapFolder - the imap folder
     * @param msgUIDs - the message UIDs
     * @param colorLabelFlag - the color label
     * @return <code>true</code> if everything went fine; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static boolean setColorLabel(final IMAPFolder imapFolder, final long[] msgUIDs, final String colorLabelFlag) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false, 32 + colorLabelFlag.length());
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {
                    r = p.command(String.format(TEMPL_UID_STORE_FLAGS, args[i], "+", colorLabelFlag), null);
                    response = r[r.length - 1];
                    try {
                        if (response.isOK()) {
                            continue Next;
                        } else if (response.isBAD() && (response.getRest() != null) && (response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1)) {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_INVALID_SYSTEM_FLAG_DETECTED));
                        } else {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_UID_STORE_NOT_SUPPORTED));
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }
        }))).booleanValue();
    }

    private static final String COMMAND_NOOP = "NOOP";

    /**
     * Force to send a NOOP command to IMAP server that is explicitly <b>not</b> handled by JavaMail API. It really does not matter if this
     * command succeeds or breaks up in a <code>MessagingException</code>. Therefore neither a return value is defined nor any exception is
     * thrown.
     */
    public static void forceNoopCommand(final IMAPFolder f) {
        try {
            f.doCommand(new IMAPFolder.ProtocolCommand() {

                /*
                 * (non-Javadoc)
                 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com .sun.mail.imap.protocol.IMAPProtocol)
                 */
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    final Response[] r = protocol.command(COMMAND_NOOP, null);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    if (response.isOK()) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }

            });
        } catch (final MessagingException e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(e.getMessage(), e);
            }
        }
    }

    private static final String[] RANGE_ALL = { "ALL" };

    /**
     * Sorts given messages according to specified sort field and specified sort direction.
     * 
     * @param folder The IMAP folder
     * @param sortCrit The IMAP sort criteria
     * @param toSort The messages' sequence numbers to sort or <code>null</code> to sort all
     * @return An array of <code>int</code> representing sorted messages' sequence numbers
     * @throws MessagingException
     */
    public static int[] getServerSortList(final IMAPFolder folder, final String sortCrit, final int[] toSort) throws MessagingException {
        return getServerSortList(folder, sortCrit, null == toSort ? RANGE_ALL : IMAPNumArgSplitter.getSeqNumArg(toSort, false, false, -1));
    }

    /**
     * Sorts all messages according to specified sort field and specified sort direction.
     * 
     * @param folder The IMAP folder
     * @param sortCrit The IMAP sort criteria
     * @return An array of <code>int</code> representing sorted messages' sequence numbers
     * @throws MessagingException
     */
    public static int[] getServerSortList(final IMAPFolder folder, final String sortCrit) throws MessagingException {
        return getServerSortList(folder, sortCrit, RANGE_ALL);
    }

    private static final String COMMAND_SORT = "SORT";

    /**
     * Executes the IMAP <i>SORT</i> command parameterized with given sort criteria and given sort range.
     * 
     * @param imapFolder The IMAP folder in which the sort is performed
     * @param sortCrit The sort criteria
     * @param mdat The sort range; if <code>null</code> all messages located in given folder are sorted
     * @return An array of <code>int</code> representing sorted messages' sequence numbers
     * @throws MessagingException If IMAP <i>SORT</i> command fails
     */
    public static int[] getServerSortList(final IMAPFolder imapFolder, final String sortCrit, final String[] mdat) throws MessagingException {
        final String numArgument;
        if (mdat == null) {
            numArgument = RANGE_ALL[0];
        } else if (mdat.length == 0) {
            throw new MessagingException("IMAP sort failed: Empty message num argument.");
        } else if (mdat.length > 1) {
            throw new MessagingException("IMAP sort failed: Message num argumet too long.");
        } else {
            numArgument = mdat[0];
        }
        /*
         * Call the IMAPFolder.doCommand() method with inner class definition of ProtocolCommand
         */
        final Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(new StringBuilder(numArgument.length() + 16).append("SORT (").append(sortCrit).append(
                    ") UTF-8 ").append(numArgument).toString(), null);
                final Response response = r[r.length - 1];
                final SmartIntArray sia = new SmartIntArray(32);
                try {
                    if (response.isOK()) {
                        for (int i = 0, len = r.length; i < len; i++) {
                            if (!(r[i] instanceof IMAPResponse)) {
                                continue;
                            }
                            final IMAPResponse ir = (IMAPResponse) r[i];
                            if (ir.keyEquals(COMMAND_SORT)) {
                                String num;
                                while ((num = ir.readAtomString()) != null) {
                                    try {
                                        sia.append(Integer.parseInt(num));
                                    } catch (final NumberFormatException e) {
                                        LOG.error(e.getMessage(), e);
                                        throw wrapException(e, "Invalid Message Number: " + num);
                                    }
                                }
                            }
                            r[i] = null;
                        }
                    } else {
                        throw new ProtocolException(
                            new StringBuilder(String.format(PROTOCOL_ERROR_TEMPL, COMMAND_SORT)).append(": ").append(
                                getResponseType(response)).append(' ').append(response.getRest()).toString());
                    }
                } finally {
                    p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return sia.toArray();
            }
        });
        return ((int[]) val);
    }

    private static final String COMMAND_SEARCH_UNSEEN = "SEARCH UNSEEN";

    private static final String COMMAND_SEARCH = "SEARCH";

    /**
     * Determines all unseen messages in specified folder and sorts them according to given sort criteria.
     * 
     * @param folder The IMAP folder
     * @param fields The desired fields
     * @param sortField The sort-by field
     * @param orderDir The order (ASC or DESC)
     * @param locale The user's locale
     * @return All unseen messages in specified folder
     * @throws MessagingException
     */
    public static Message[] getUnreadMessages(final IMAPFolder folder, final MailField[] fields, final MailSortField sortField, final OrderDirection orderDir, final Locale locale) throws MessagingException {
        final IMAPFolder imapFolder = folder;
        final Message[] val = (Message[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            /*
             * (non-Javadoc)
             * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun .mail.imap.protocol.IMAPProtocol)
             */
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(COMMAND_SEARCH_UNSEEN, null);
                /*
                 * Result is something like: SEARCH 12 20 24
                 */
                int[] newMsgSeqNums = null;
                final Response response = r[r.length - 1];
                {
                    final SmartIntArray tmp = new SmartIntArray(32);
                    try {
                        if (response.isOK()) {
                            for (int i = 0, len = r.length - 1; i < len; i++) {
                                if (!(r[i] instanceof IMAPResponse)) {
                                    r[i] = null;
                                    continue;
                                }
                                final IMAPResponse ir = (IMAPResponse) r[i];
                                /*
                                 * The SEARCH response from the server contains a listing of message sequence numbers corresponding to those
                                 * messages that match the searching criteria.
                                 */
                                if (ir.keyEquals(COMMAND_SEARCH)) {
                                    String num;
                                    while ((num = ir.readAtomString()) != null) {
                                        try {
                                            tmp.append(Integer.parseInt(num));
                                        } catch (final NumberFormatException e) {
                                            continue;
                                        }
                                    }
                                }
                                r[i] = null;
                            }
                        } else {
                            throw new ProtocolException(
                                new StringBuilder(String.format(PROTOCOL_ERROR_TEMPL, COMMAND_SEARCH)).append(": ").append(
                                    getResponseType(response)).append(' ').append(response.getRest()).toString());
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                    newMsgSeqNums = tmp.toArray();
                }
                /*
                 * No new messages found
                 */
                if (newMsgSeqNums.length == 0) {
                    return null;
                }
                /*
                 * Fetch messages and sort them
                 */
                final Message[] newMsgs;
                try {
                    final MailFields set = new MailFields(fields);
                    final boolean body = set.contains(MailField.BODY) || set.contains(MailField.FULL);
                    newMsgs = new FetchIMAPCommand(folder, p.isREV1(), newMsgSeqNums, getFetchProfile(
                        fields,
                        MailField.toField(sortField.getListField()),
                        IMAPConfig.isFastFetch()), false, false, body).doCommand();
                } catch (final MessagingException e) {
                    throw wrapException(e, null);
                }
                final List<Message> msgList = Arrays.asList(newMsgs);
                Collections.sort(msgList, getMessageComparator(sortField, orderDir, locale));
                return msgList.toArray(newMsgs);
            }
        });
        return val;
    }

    private static final String COMMAND_EXPUNGE = "EXPUNGE";

    /**
     * Performs the <code>EXPUNGE</code> command on whole folder referenced by <code>imapFolder</code>.
     * <p>
     * <b>NOTE</b>: The internal message cache of specified instance of {@link IMAPFolder} is left in an inconsistent state cause its kept
     * message references are not marked as expunged. Therefore the folder should be closed afterwards to force a message cache update.
     * 
     * @param imapFolder - the IMAP folder
     * @return <code>true</code> if everything went fine; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static boolean fastExpunge(final IMAPFolder imapFolder) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(COMMAND_EXPUNGE, null);
                final Response response = r[r.length - 1];
                Boolean retval = Boolean.FALSE;
                try {
                    if (response.isOK()) {
                        retval = Boolean.TRUE;
                    } else if (response.isBAD() && (response.getRest() != null) && (response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1)) {
                        throw new ProtocolException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            ERR_INVALID_SYSTEM_FLAG_DETECTED));
                    } else {
                        throw new ProtocolException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            ERR_UID_STORE_NOT_SUPPORTED));
                    }
                } finally {
                    /*
                     * No invocation of notifyResponseHandlers() to avoid sequential (by message) folder cache update
                     */
                    /* p.notifyResponseHandlers(r); */
                    p.handleResult(response);
                }
                return retval;
            }
        }))).booleanValue();
    }

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    /**
     * Performs a <i>UID EXPUNGE</i> on specified UIDs on first try. If <i>UID EXPUNGE</i> fails the fallback action as proposed in RFC 3501
     * is done:
     * <ol>
     * <li>Remember all messages which are marked as \Deleted by now</li>
     * <li>Temporary remove the \Deleted flags from these messages</li>
     * <li>Set \Deleted flag on messages referenced by given UIDs and perform a normal <i>EXPUNGE</i> on folder</li>
     * <li>Restore the \Deleted flags on remaining messages</li>
     * </ol>
     * <b>NOTE</b>: The internal message cache of specified instance of {@link IMAPFolder} is left in an inconsistent state cause its kept
     * message references are not marked as expunged. Therefore the folder should be closed afterwards to force a message cache update.
     * 
     * @param imapFolder The IMAP folder
     * @param uids The UIDs
     * @param supportsUIDPLUS <code>true</code> if IMAP server's capabilities indicate support of UIDPLUS extension; otherwise
     *            <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    public static void uidExpungeWithFallback(final IMAPFolder imapFolder, final long[] uids, final boolean supportsUIDPLUS) throws MessagingException {
        boolean performFallback = !supportsUIDPLUS;
        if (supportsUIDPLUS) {
            try {
                final long start = System.currentTimeMillis();
                IMAPCommandsCollection.uidExpunge(imapFolder, uids);
                MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append(uids.length).append(" messages expunged in ").append(
                        (System.currentTimeMillis() - start)).append("msec").toString());
                }
            } catch (final FolderClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw e;
            } catch (final StoreClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw e;
            } catch (final MessagingException e) {
                if (e.getNextException() instanceof ProtocolException) {
                    final ProtocolException protocolException = (ProtocolException) e.getNextException();
                    final Response response = protocolException.getResponse();
                    if (response != null && response.isBYE()) {
                        /*
                         * The BYE response is always untagged, and indicates that the server is about to close the connection.
                         */
                        throw new StoreClosedException(imapFolder.getStore(), protocolException.getMessage());
                    }
                    final Throwable cause = protocolException.getCause();
                    if (cause instanceof StoreClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw ((StoreClosedException) cause);
                    } else if (cause instanceof FolderClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw ((FolderClosedException) cause);
                    }
                }
                if (LOG.isWarnEnabled()) {
                    LOG.warn(new StringBuilder(64).append("UID EXPUNGE failed: ").append(e.getMessage()).append(
                        ".\nPerforming fallback actions.").toString(), e);
                }
                performFallback = true;
            }
        }
        if (performFallback) {
            /*
             * UID EXPUNGE did not work or not supported; perform fallback actions
             */
            final long[] excUIDs = IMAPCommandsCollection.getDeletedMessages(imapFolder, uids);
            if (excUIDs.length > 0) {
                /*
                 * Temporary remove flag \Deleted, perform expunge & restore flag \Deleted
                 */
                new FlagsIMAPCommand(imapFolder, excUIDs, FLAGS_DELETED, false, true, false).doCommand();
                IMAPCommandsCollection.fastExpunge(imapFolder);
                new FlagsIMAPCommand(imapFolder, excUIDs, FLAGS_DELETED, true, true, false).doCommand();
            } else {
                IMAPCommandsCollection.fastExpunge(imapFolder);
            }
        }
    }

    /**
     * Determines if given folder is marked as read-only when performing a <code>SELECT</code> command on it.
     * 
     * @param folder The IMAP folder
     * @return <code>true</code> is IMAP folder is marked as READ-ONLY; otherwise <code>false</code>
     * @throws IMAPException
     */
    public static boolean isReadOnly(final IMAPFolder f) throws IMAPException {
        try {
            final Boolean val = (Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {

                /*
                 * (non-Javadoc)
                 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com .sun.mail.imap.protocol.IMAPProtocol)
                 */
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    /*
                     * Encode the mbox as per RFC2060
                     */
                    final Argument args = new Argument();
                    args.writeString(BASE64MailboxEncoder.encode(f.getFullName()));
                    /*
                     * Perform command
                     */
                    final Response[] r = p.command("SELECT", args);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    Boolean retval = Boolean.FALSE;
                    if (response.isOK()) { // command successful
                        retval = Boolean.valueOf(response.toString().indexOf("READ-ONLY") != -1);
                    } else {
                        p.handleResult(response);
                    }
                    return retval;
                }
            });
            return val.booleanValue();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IMAPException(IMAPException.Code.FAILED_READ_ONLY_CHECK, e, new Object[0]);
        }
    }

    /**
     * Checks if IMAP folder denoted by specified fullname is allowed to be opened in desired mode.
     * 
     * @param f The IMAP folder providing protocol access
     * @param fullname The fullname to check
     * @param mode The desired open mode
     * @return <code>true</code> if IMAP folder denoted by specified fullname is allowed to be opened in desired mode; otherwise
     *         <code>false</code>
     * @throws IMAPException If an IMAP error occurs
     */
    public static boolean canBeOpened(final IMAPFolder f, final String fullname, final int mode) throws IMAPException {
        if ((Folder.READ_ONLY != mode) && (Folder.READ_WRITE != mode)) {
            throw new IMAPException(IMAPException.Code.UNKNOWN_FOLDER_MODE, Integer.valueOf(mode));
        }
        try {
            return ((Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    final Boolean retval;
                    {
                        /*
                         * Encode the mbox as per RFC2060
                         */
                        final Argument args = new Argument();
                        args.writeString(BASE64MailboxEncoder.encode(fullname));
                        /*
                         * Perform command
                         */
                        final Response[] r = p.command(Folder.READ_ONLY == mode ? "EXAMINE" : "SELECT", args);
                        /*
                         * Grab last response that should indicate an OK
                         */
                        final Response response = r[r.length - 1];
                        if (response.isOK()) { // command successful
                            retval = Boolean.TRUE;
                        } else {
                            retval = Boolean.FALSE;
                        }
                    }
                    /*
                     * Now re-access previous folder to keep it selected
                     */
                    final Argument args = new Argument();
                    args.writeString(BASE64MailboxEncoder.encode(f.getFullName()));
                    /*
                     * Perform command
                     */
                    final Response[] r = p.command("SELECT", args);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    p.handleResult(response);
                    return retval;
                }
            })).booleanValue();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw new IMAPException(IMAPException.Code.FAILED_READ_ONLY_CHECK, e, new Object[0]);
        }
    }

    private static final String FETCH_FLAGS = "FETCH 1:* (FLAGS UID)";

    /**
     * Gets all messages marked as deleted in given IMAP folder.
     * 
     * @param imapFolder The IMAP folder
     * @param filter The filter whose elements are going to be removed from return value
     * @return All messages marked as deleted in given IMAP folder filtered by specified <code>filter</code>
     * @throws MessagingException If a protocol error occurs
     */
    private static long[] getDeletedMessages(final IMAPFolder imapFolder, final long[] filter) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        return (long[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(FETCH_FLAGS, null);
                final Response response = r[r.length - 1];
                long[] retval = null;
                try {
                    if (response.isOK()) {
                        final Set<Long> set = new TreeSet<Long>();
                        final int mlen = r.length - 1;
                        for (int i = 0; i < mlen; i++) {
                            if (!(r[i] instanceof FetchResponse)) {
                                continue;
                            }
                            final FetchResponse fr = (FetchResponse) r[i];
                            final boolean deleted;
                            {
                                final FLAGS item = getItemOf(FLAGS.class, fr, "FLAGS");
                                deleted = item.contains(Flags.Flag.DELETED);
                            }
                            if (deleted) {
                                final UID uidItem = getItemOf(UID.class, fr, STR_UID);
                                set.add(Long.valueOf(uidItem.uid));
                            }
                            r[i] = null;
                        }
                        if ((filter != null) && (filter.length > 0)) {
                            for (int i = 0; i < filter.length; i++) {
                                set.remove(Long.valueOf(filter[i]));
                            }
                        }
                        retval = new long[set.size()];
                        int i = 0;
                        for (final Long l : set) {
                            retval[i++] = l.longValue();
                        }
                    } else {
                        throw new ProtocolException(new StringBuilder("FETCH command failed: ").append(getResponseType(response)).append(
                            ' ').append(response.getRest()).toString());
                    }
                } finally {
                    /*
                     * No invocation of notifyResponseHandlers() to avoid sequential (by message) folder cache update
                     */
                    /* p.notifyResponseHandlers(r); */
                    p.handleResult(response);
                }
                return retval;
            }
        }));
    }

    static String getResponseType(final Response response) {
        if (response.isBAD()) {
            return "BAD";
        }
        if (response.isBYE()) {
            return "BYE";
        }
        if (response.isNO()) {
            return "NO";
        }
        if (response.isOK()) {
            return "OK";
        }
        return "UNKNOWN";
    }

    private static final String TEMPL_FETCH_UID = "FETCH %s (UID)";

    /**
     * Detects the corresponding UIDs to message range according to specified starting/ending sequence numbers.
     * 
     * @param imapFolder The IMAP folder
     * @param startSeqNum The starting sequence number
     * @param endSeqNum The ending sequence number
     * @return The corresponding UIDs
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static long[] seqNums2UID(final IMAPFolder imapFolder, final int startSeqNum, final int endSeqNum) throws MessagingException {
        return seqNums2UID(
            imapFolder,
            new String[] { new StringBuilder(16).append(startSeqNum).append(':').append(endSeqNum).toString() },
            endSeqNum - startSeqNum + 1);
    }

    /**
     * Detects the corresponding UIDs to message range according to specified sequence numbers.
     * 
     * @param imapFolder The IMAP folder
     * @param seqNums The sequence numbers
     * @return The corresponding UIDs
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static long[] seqNums2UID(final IMAPFolder imapFolder, final int[] seqNums) throws MessagingException {
        return seqNums2UID(imapFolder, IMAPNumArgSplitter.splitSeqNumArg(seqNums, true, 12), seqNums.length); // "FETCH <nums> (UID)"
    }

    /**
     * Detects the corresponding UIDs to message range according to specified arguments
     * 
     * @param imapFolder The IMAP folder
     * @param args The arguments
     * @param size The number of messages in folder
     * @return The corresponding UIDs
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static long[] seqNums2UID(final IMAPFolder imapFolder, final String[] args, final int size) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        return (long[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                Response[] r = null;
                Response response = null;
                int index = 0;
                final long[] uids = new long[size];
                for (int i = 0; (i < args.length) && (index < size); i++) {
                    /*-
                     * Arguments:  sequence set
                     * message data item names or macro
                     * 
                     * Responses:  untagged responses: FETCH
                     * 
                     * Result:     OK - fetch completed
                     *             NO - fetch error: can't fetch that data
                     *             BAD - command unknown or arguments invalid
                     */
                    r = p.command(String.format(TEMPL_FETCH_UID, args[i]), null);
                    final int len = r.length - 1;
                    response = r[len];
                    try {
                        if (response.isOK()) {
                            for (int j = 0; j < len; j++) {
                                if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                    final UID uidItem = getItemOf(UID.class, (FetchResponse) r[j], STR_UID);
                                    uids[index++] = uidItem.uid;
                                    r[j] = null;
                                }
                            }
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                if (index < size) {
                    final long[] trim = new long[index];
                    System.arraycopy(uids, 0, trim, 0, trim.length);
                    return trim;
                }
                return uids;
            }

        }));
    }

    private static final String TEMPL_UID_FETCH_UID = "UID FETCH %s (UID)";

    /**
     * Maps specified UIDs to current corresponding sequence numbers.
     * 
     * @param imapFolder The IMAP folder
     * @param uids The UIDs
     * @return The current corresponding sequence numbers
     * @throws MessagingException If a messaging error occurs
     */
    public static int[] uids2SeqNums(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new int[0];
        }
        return (int[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Map<Long, Integer> m = new HashMap<Long, Integer>(uids.length);
                final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, true, 16); // "UID FETCH <uids> (UID)"
                final long start = System.currentTimeMillis();
                for (int k = 0; k < args.length; k++) {
                    /*-
                     * Arguments:  sequence set
                     * message data item names or macro
                     * 
                     * Responses:  untagged responses: FETCH
                     * 
                     * Result:     OK - fetch completed
                     *             NO - fetch error: can't fetch that data
                     *             BAD - command unknown or arguments invalid
                     */
                    final Response[] r = p.command(String.format(TEMPL_UID_FETCH_UID, args[k]), null);
                    final int len = r.length - 1;
                    final Response response = r[len];
                    r[len] = null;
                    try {
                        if (response.isOK() && len > 0) {
                            for (int j = 0; j < len; j++) {
                                if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                    final FetchResponse fr = (FetchResponse) r[j];
                                    final UID uidItem = getItemOf(UID.class, fr, STR_UID);
                                    m.put(Long.valueOf(uidItem.uid), Integer.valueOf(fr.getNumber()));
                                    r[j] = null;
                                }
                            }
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append(imapFolder.getFullName()).append(
                        ": IMAP resolve fetch >>>UID FETCH ... (UID)<<< for ").append(uids.length).append(" messages took ").append(
                        (System.currentTimeMillis() - start)).append("msec").toString());
                }
                final int[] retval = new int[m.size()];
                for (int i = 0; i < retval.length; i++) {
                    final Long key = Long.valueOf(uids[i]);
                    retval[i] = m.containsKey(key) ? m.get(key).intValue() : -1;
                }
                return retval;
            }
        }));
    }

    /**
     * Generates a map resolving specified UIDs to current corresponding sequence numbers.
     * 
     * @param imapFolder The IMAP folder
     * @param uids The UIDs
     * @return A map resolving specified UIDs to current corresponding sequence numbers
     * @throws MessagingException If a messaging error occurs
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, Integer> uids2SeqNumsMap(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return Collections.emptyMap();
        }
        return (Map<Long, Integer>) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Map<Long, Integer> m = new HashMap<Long, Integer>(uids.length);
                final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, true, 16); // "UID FETCH <uids> (UID)"
                final long start = System.currentTimeMillis();
                for (int k = 0; k < args.length; k++) {
                    /*-
                     * Arguments:  sequence set
                     * message data item names or macro
                     * 
                     * Responses:  untagged responses: FETCH
                     * 
                     * Result:     OK - fetch completed
                     *             NO - fetch error: can't fetch that data
                     *             BAD - command unknown or arguments invalid
                     */
                    final Response[] r = p.command(String.format(TEMPL_UID_FETCH_UID, args[k]), null);
                    final int len = r.length - 1;
                    final Response response = r[len];
                    r[len] = null;
                    try {
                        if (response.isOK()) {
                            for (int j = 0; j < len; j++) {
                                if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                    final FetchResponse fr = (FetchResponse) r[j];
                                    final UID uidItem = getItemOf(UID.class, fr, STR_UID);
                                    m.put(Long.valueOf(uidItem.uid), Integer.valueOf(fr.getNumber()));
                                    r[j] = null;
                                }
                            }
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(new StringBuilder(128).append(imapFolder.getFullName()).append(
                        ": IMAP resolve fetch >>>UID FETCH ... (UID)<<< for ").append(uids.length).append(" messages took ").append(
                        (System.currentTimeMillis() - start)).append("msec").toString());
                }
                return m;
            }
        }));
    }

    /**
     * Generates a map resolving corresponding sequence numbers to specified UIDs.
     * 
     * @param imapFolder The IMAP folder
     * @param uids The UIDs
     * @return A map resolving corresponding sequence numbers to specified UIDs
     * @throws MessagingException If a messaging error occurs
     */
    @SuppressWarnings("unchecked")
    public static Map<Integer, Long> seqNums2uidsMap(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return Collections.emptyMap();
        }
        return (Map<Integer, Long>) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Map<Integer, Long> m = new HashMap<Integer, Long>(uids.length);
                final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, true, 16); // "UID FETCH <uids> (UID)"
                for (int k = 0; k < args.length; k++) {
                    /*-
                     * Arguments:  sequence set
                     * message data item names or macro
                     * 
                     * Responses:  untagged responses: FETCH
                     * 
                     * Result:     OK - fetch completed
                     *             NO - fetch error: can't fetch that data
                     *             BAD - command unknown or arguments invalid
                     */
                    final Response[] r = p.command(String.format(TEMPL_UID_FETCH_UID, args[k]), null);
                    final int len = r.length - 1;
                    final Response response = r[len];
                    r[len] = null;
                    try {
                        if (response.isOK()) {
                            for (int j = 0; j < len; j++) {
                                if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                    final FetchResponse fr = (FetchResponse) r[j];
                                    final UID uidItem = getItemOf(UID.class, fr);
                                    if (uidItem != null) {
                                        m.put(Integer.valueOf(fr.getNumber()), Long.valueOf(uidItem.uid));
                                    }
                                    r[j] = null;
                                }
                            }
                        }
                    } finally {
                        p.notifyResponseHandlers(r);
                        p.handleResult(response);
                    }
                }
                return m;
            }
        }));
    }

    private static final String COMMAND_FETCH = "FETCH 1:* (UID INTERNALDATE)";

    /**
     * Fetches all messages from given IMAP folder and pre-fills instances with UID, folder fullname and received date.
     * 
     * @param imapFolder The IMAP folder
     * @param ascending <code>true</code> to order messages by received date in ascending order; otherwise descending
     * @return All messages from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static MailMessage[] fetchAll(final IMAPFolder imapFolder, final boolean ascending) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return new MailMessage[0];
        }
        return (MailMessage[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*-
                 * Arguments:  sequence set
                 * message data item names or macro
                 * 
                 * Responses:  untagged responses: FETCH
                 * 
                 * Result:     OK - fetch completed
                 *             NO - fetch error: can't fetch that data
                 *             BAD - command unknown or arguments invalid
                 */
                final Response[] r = p.command(COMMAND_FETCH, null);
                final int len = r.length - 1;
                final Response response = r[len];
                final List<MailMessage> l = new ArrayList<MailMessage>(len);
                try {
                    if (response.isOK()) {
                        final String fullname = imapFolder.getFullName();
                        for (int j = 0; j < len; j++) {
                            if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                final FetchResponse fr = (FetchResponse) r[j];
                                final MailMessage m = new IDMailMessage(String.valueOf(getItemOf(UID.class, fr, STR_UID).uid), fullname);
                                m.setReceivedDate(getItemOf(INTERNALDATE.class, fr, "INTERNALDATE").getDate());
                                l.add(m);
                                r[j] = null;
                            }
                        }
                    }
                } finally {
                    p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                Collections.sort(l, ascending ? ASC_COMP : DESC_COMP);
                return l.toArray(new MailMessage[l.size()]);
            }

        }));
    }

    /**
     * A {@link Comparator} comparing instances of {@link MailMessage} by their received date in ascending order.
     */
    static final Comparator<MailMessage> ASC_COMP = new Comparator<MailMessage>() {

        public int compare(final MailMessage m1, final MailMessage m2) {
            final Date d1 = m1.getReceivedDate();
            final Date d2 = m2.getReceivedDate();
            final Integer refComp = compareReferences(d1, d2);
            return (refComp == null ? d1.compareTo(d2) : refComp.intValue());
        }
    };

    /**
     * A {@link Comparator} comparing instances of {@link MailMessage} by their received date in descending order.
     */
    static final Comparator<MailMessage> DESC_COMP = new Comparator<MailMessage>() {

        public int compare(final MailMessage m1, final MailMessage m2) {
            final Date d1 = m1.getReceivedDate();
            final Date d2 = m2.getReceivedDate();
            final Integer refComp = compareReferences(d1, d2);
            return (refComp == null ? d1.compareTo(d2) : refComp.intValue()) * (-1);
        }
    };

    /**
     * Compares given object references being <code>null</code>.
     * 
     * @param o1 The first object reference
     * @param o2 The second object reference
     * @return An {@link Integer} of <code>-1</code> if first reference is <code>null</code> but the second is not, an {@link Integer} of
     *         <code>1</code> if first reference is not <code>null</code> but the second is, an {@link Integer} of <code>0</code> if both
     *         references are <code>null</code>, or returns <code>null</code> if both references are not <code>null</code>
     */
    static Integer compareReferences(final Object o1, final Object o2) {
        if ((o1 == null) && (o2 != null)) {
            return Integer.valueOf(-1);
        } else if ((o1 != null) && (o2 == null)) {
            return Integer.valueOf(1);
        } else if ((o1 == null) && (o2 == null)) {
            return Integer.valueOf(0);
        }
        /*
         * Both references are not null
         */
        return null;
    }

    private static final String TEMPL_UID_EXPUNGE = "UID EXPUNGE %s";

    /**
     * <p>
     * Performs the <code>EXPUNGE</code> command on messages identified through given <code>uids</code>.
     * <p>
     * <b>NOTE</b> folder's message cache is left in an inconsistent state cause its kept message references are not marked as expunged.
     * Therefore the folder should be closed afterwards to force message cache update.
     * 
     * @param imapFolder - the imap folder
     * @param uids - the message UIDs
     * @return <code>true</code> if everything went fine; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static boolean uidExpunge(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        if (imapFolder.getMessageCount() == 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, false, 12); // "UID EXPUNGE <uids>"
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {
                    r = p.command(String.format(TEMPL_UID_EXPUNGE, args[i]), null);
                    response = r[r.length - 1];
                    try {
                        if (response.isOK()) {
                            continue Next;
                        } else if (response.isBAD() && (response.getRest() != null) && (response.getRest().indexOf(STR_INVALID_SYSTEM_FLAG) != -1)) {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_INVALID_SYSTEM_FLAG_DETECTED));
                        } else {
                            throw new ProtocolException(IMAPException.getFormattedMessage(
                                IMAPException.Code.PROTOCOL_ERROR,
                                ERR_UID_STORE_NOT_SUPPORTED));
                        }
                    } finally {
                        /*
                         * No invocation of notifyResponseHandlers() to avoid sequential (by message) folder cache update
                         */
                        /* p.notifyResponseHandlers(r); */
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }
        }))).booleanValue();
    }

    private static final String ATOM_PERMANENTFLAGS = "[PERMANENTFLAGS";

    static final Pattern PATTERN_USER_FLAGS = Pattern.compile("(?:\\(|\\s)(?:\\\\\\*)(?:\\)|\\s)");

    /**
     * Applies the IMAPv4 SELECT command on given folder and returns whether its permanent flags supports user-defined flags or not.
     * <p>
     * User flags are supported if untagged <i>PERMANENTFLAGS</i> response contains "\*", e.g.:
     * 
     * <pre>
     * * OK [PERMANENTFLAGS (\Answered \Flagged \Draft \Deleted \Seen \*)]
     * </pre>
     * 
     * @param imapFolder The IMAP folder to check
     * @return <code>true</code> if user flags are supported; otherwise <code>false</code>
     * @throws MessagingException If SELECT command fails
     */
    public static boolean supportsUserDefinedFlags(final IMAPFolder imapFolder) throws MessagingException {
        final Boolean val = (Boolean) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            /*
             * (non-Javadoc)
             * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun .mail.imap.protocol.IMAPProtocol)
             */
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String command = new StringBuilder("SELECT ").append(prepareStringArgument(imapFolder.getFullName())).toString();
                final Response[] r = p.command(command, null);
                final Response response = r[r.length - 1];
                Boolean retval = Boolean.FALSE;
                try {
                    if (response.isOK()) {
                        NextResp: for (int i = 0, len = r.length - 1; i < len; i++) {
                            if (!(r[i] instanceof IMAPResponse)) {
                                continue;
                            }
                            final IMAPResponse ir = (IMAPResponse) r[i];
                            if (ir.isUnTagged() && ir.isOK()) {
                                /*
                                 * " OK [PERMANENTFLAGS (\Deleted \)]"
                                 */
                                ir.skipSpaces();
                                if (ATOM_PERMANENTFLAGS.equals(ir.readAtom('\0'))) {
                                    retval = Boolean.valueOf(PATTERN_USER_FLAGS.matcher(ir.getRest()).find());
                                    break NextResp;
                                }
                            }
                            r[i] = null;
                        }
                    } else {
                        return retval;
                    }
                } finally {
                    p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return retval;
            }
        });
        return val.booleanValue();
    }

    private static final String COMMAND_FETCH_OXMARK_RFC = "FETCH 1:* (UID RFC822.HEADER.LINES (" + MessageHeaders.HDR_X_OX_MARKER + "))";

    private static final String COMMAND_FETCH_OXMARK_REV1 = "FETCH 1:* (UID BODY.PEEK[HEADER.FIELDS (" + MessageHeaders.HDR_X_OX_MARKER + ")])";

    private static interface HeaderString {

        public String getHeaderString(Item fetchItem);
    }

    private static HeaderString REV1HeaderStream = new HeaderString() {

        public String getHeaderString(final Item fetchItem) {
            final ByteArray byteArray = ((BODY) fetchItem).getByteArray();
            try {
                return new String(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount(), "US-ASCII");
            } catch (final UnsupportedEncodingException e) {
                // Cannot occur
                return "";
            }
        }
    };

    private static HeaderString RFCHeaderStream = new HeaderString() {

        public String getHeaderString(final Item fetchItem) {
            final ByteArray byteArray = ((RFC822DATA) fetchItem).getByteArray();
            try {
                return new String(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount(), "US-ASCII");
            } catch (final UnsupportedEncodingException e) {
                // Cannot occur
                return "";
            }
        }
    };

    static HeaderString getHeaderStream(final boolean isREV1) {
        if (isREV1) {
            return REV1HeaderStream;
        }
        return RFCHeaderStream;
    }

    /**
     * Searches the message whose {@link MessageHeaders#HDR_X_OX_MARKER} header is set to specified marker.
     * 
     * @param marker The marker to lookup
     * @param imapFolder The IMAP folder in which to search the message
     * @return The matching message's UID or <code>-1</code> if none found
     * @throws MessagingException If marker look-up fails
     */
    public static long findMarker(final String marker, final IMAPFolder imapFolder) throws MessagingException {
        if ((marker == null) || (marker.length() == 0)) {
            return -1L;
        }
        if (imapFolder.getMessageCount() == 0) {
            return -1L;
        }
        return ((Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final boolean isREV1 = p.isREV1();
                final Response[] r;
                if (isREV1) {
                    r = p.command(COMMAND_FETCH_OXMARK_REV1, null);
                } else {
                    r = p.command(COMMAND_FETCH_OXMARK_RFC, null);
                }
                final Response response = r[r.length - 1];
                try {
                    if (response.isOK()) {
                        HeaderString headerStream = null;
                        final HeaderCollection h = new HeaderCollection();
                        final int len = r.length - 1;
                        for (int i = 0; i < len; i++) {
                            if (!(r[i] instanceof FetchResponse)) {
                                continue;
                            }
                            final FetchResponse fetchResponse = (FetchResponse) r[i];
                            final Item headerItem;
                            if (isREV1) {
                                headerItem = getItemOf(BODY.class, fetchResponse, "HEADER");
                            } else {
                                headerItem = getItemOf(RFC822DATA.class, fetchResponse, "HEADER");
                            }
                            final String curMarker;
                            {
                                if (null == headerStream) {
                                    headerStream = getHeaderStream(isREV1);
                                }
                                if (!h.isEmpty()) {
                                    h.clear();
                                }
                                h.load(headerStream.getHeaderString(headerItem));
                                curMarker = h.getHeader(MessageHeaders.HDR_X_OX_MARKER, null);
                            }
                            if (marker.equals(curMarker)) {
                                final UID uidItem = getItemOf(UID.class, fetchResponse, STR_UID);
                                return Long.valueOf(uidItem.uid);
                            }
                            r[i] = null;
                        }
                    }
                } catch (final MailException e) {
                    throw wrapException(e, null);
                } finally {
                    // p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return Long.valueOf(-1L);
            }
        })).longValue();
    }

    private static final String COMMAND_FETCH_ENV_UID = "FETCH 1:* (ENVELOPE UID)";

    /**
     * Finds corresponding UID of message whose Message-ID header matches given message ID.
     * 
     * @param messageId The message ID
     * @param imapFolder The IMAP folder
     * @return The UID of matching message or <code>-1</code> if none found
     * @throws MessagingException
     */
    public static long messageId2UID(final String messageId, final IMAPFolder imapFolder) throws MessagingException {
        if ((messageId == null) || (messageId.length() == 0)) {
            return -1L;
        }
        if (imapFolder.getMessageCount() == 0) {
            return -1L;
        }
        final Long retval = (Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            /*
             * (non-Javadoc)
             * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com.sun .mail.imap.protocol.IMAPProtocol)
             */
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final Response[] r = p.command(COMMAND_FETCH_ENV_UID, null);
                final Response response = r[r.length - 1];
                final Long retval = Long.valueOf(-1L);
                try {
                    if (response.isOK()) {
                        for (int i = 0, len = r.length - 1; i < len; i++) {
                            if (!(r[i] instanceof FetchResponse)) {
                                continue;
                            }
                            final FetchResponse fetchResponse = (FetchResponse) r[i];
                            if (messageId.equals(getItemOf(ENVELOPE.class, fetchResponse, "ENVELOPE").messageId)) {
                                final UID uidItem = getItemOf(UID.class, fetchResponse, STR_UID);
                                return Long.valueOf(uidItem.uid);
                            }
                            r[i] = null;
                        }
                    }
                } finally {
                    // p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return retval;
            }
        });
        return retval.longValue();
    }

    private static final Pattern PATTERN_QUOTE_ARG = Pattern.compile("[\\s\\*%\\(\\)\\{\\}\"\\\\]");

    private static final Pattern PATTERN_ESCAPE_ARG = Pattern.compile("[\"\\\\]");

    private final static String REPLPAT_QUOTE = "\"";

    private final static String REPLACEMENT_QUOTE = "\\\\\\\"";

    private final static String REPLPAT_BACKSLASH = "\\\\";

    private final static String REPLACEMENT_BACKSLASH = "\\\\\\\\";

    /**
     * First encodes given fullname by using <code>com.sun.mail.imap.protocol.BASE64MailboxEncoder.encode()</code> method. Afterwards
     * encoded string is checked if it needs quoting and escaping of the special characters '"' and '\'.
     * 
     * @param fullname The folder fullname
     * @return Prepared fullname ready for being used in raw IMAP commands
     */
    public static String prepareStringArgument(final String fullname) {
        /*
         * Ensure to have only ASCII characters
         */
        final String lfolder = BASE64MailboxEncoder.encode(fullname);
        /*
         * Determine if quoting (and escaping) has to be done
         */
        final boolean quote = PATTERN_QUOTE_ARG.matcher(lfolder).find();
        final boolean escape = PATTERN_ESCAPE_ARG.matcher(lfolder).find();
        final StringBuilder sb = new StringBuilder(lfolder.length() + 8);
        if (escape) {
            sb.append(lfolder.replaceAll(REPLPAT_BACKSLASH, REPLACEMENT_BACKSLASH).replaceAll(REPLPAT_QUOTE, REPLACEMENT_QUOTE));
        } else {
            sb.append(lfolder);
        }
        if (quote) {
            /*
             * Surround with quotes
             */
            sb.insert(0, '"');
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response; throws an appropriate protocol exception if not present
     * in given <i>FETCH</i> response.
     * 
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @param itemName The item name to generate appropriate error message on absence
     * @return The item associated with given class in specified <i>FETCH</i> response.
     */
    static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse, final String itemName) throws ProtocolException {
        final I retval = getItemOf(clazz, fetchResponse);
        if (null == retval) {
            throw missingFetchItem(itemName);
        }
        return retval;
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     * 
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     * @see #getItemOf(Class, FetchResponse, String)
     */
    static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
        final int len = fetchResponse.getItemCount();
        for (int i = 0; i < len; i++) {
            final Item item = fetchResponse.getItem(i);
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }
        return null;
    }

    /**
     * Generates a new protocol exception according to following template:<br>
     * <code>&quot;Missing &lt;itemName&gt; item in FETCH response.&quot;</code>
     * 
     * @param itemName The item name; e.g. <code>UID</code>, <code>FLAGS</code>, etc.
     * @return A new protocol exception with appropriate message.
     */
    static ProtocolException missingFetchItem(final String itemName) {
        return new ProtocolException(
            new StringBuilder(48).append("Missing ").append(itemName).append(" item in FETCH response.").toString());
    }

    /**
     * Generates a new protocol exception wrapping specified exception.
     * 
     * @param e The exception to wrap
     * @param causeMessage An optional individual error message; leave to <code>null</code> to pass specified exception's message
     * @return A new protocol exception wrapping specified exception.
     */
    static ProtocolException wrapException(final Exception e, final String causeMessage) {
        final ProtocolException pe = new ProtocolException(causeMessage == null ? e.getMessage() : causeMessage);
        pe.initCause(e);
        return pe;
    }
}
