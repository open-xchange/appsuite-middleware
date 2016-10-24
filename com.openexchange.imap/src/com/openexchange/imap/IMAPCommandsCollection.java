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

package com.openexchange.imap;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.java.Strings.asciiLowerCase;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.mime.utils.MimeStorageUtility.getFetchProfile;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.event.FolderEvent;
import org.apache.commons.lang.RandomStringUtils;
import com.openexchange.exception.OXException;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.command.IMAPNumArgSplitter;
import com.openexchange.imap.command.MessageFetchIMAPCommand;
import com.openexchange.imap.dataobjects.ExtendedIMAPFolder;
import com.openexchange.imap.dataobjects.IMAPMailPart;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.util.IMAPUpdateableData;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;
import com.openexchange.version.Version;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.RFC822DATA;
import com.sun.mail.imap.protocol.UID;
import gnu.trove.TLongCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link IMAPCommandsCollection} - A collection of simple IMAP commands.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPCommandsCollection {

    /**
     * The constant for all.
     */
    protected static final String[] ARGS_ALL = new String[] { "1:*" };

    private static final String STR_UID = "UID";

    private static final String STR_FETCH = "FETCH";

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPCommandsCollection.class);

    private static final Field FIELD_IMAPFOLDER_EXISTS;
    private static final Field FIELD_IMAPFOLDER_ATTRIBUTES;

    static {
        Field existsField = null;
        Field attributesField = null;
        try {
            existsField = IMAPFolder.class.getDeclaredField("exists");
            existsField.setAccessible(true);

            attributesField = IMAPFolder.class.getDeclaredField("attributes");
            attributesField.setAccessible(true);
        } catch (Exception e) {
            LOG.error("", e);
        }
        FIELD_IMAPFOLDER_EXISTS = existsField;
        FIELD_IMAPFOLDER_ATTRIBUTES = attributesField;
    }

    /**
     * Prevent instantiation.
     */
    private IMAPCommandsCollection() {
        super();
    }

    /**
     * The IMAP capabilities.
     */
    public static final class Capabilities {

        private final Set<String> set;

        Capabilities(final Collection<String> col) {
            super();
            set = java.util.Collections.unmodifiableSet(new HashSet<String>(col));
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(set.size() * 8);
            for (final String cap : set) {
                sb.append(' ').append(cap);
            }
            sb.deleteCharAt(0);
            return sb.toString();
        }

        /**
         * Gets the capabilities.
         *
         * @return The capabilities
         */
        public Set<String> getSet() {
            return set;
        }

    }

    /**
     * Gets the IMAP capabilities from the IMAP store associated with given connected IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @return The IMAP capabilities as an unmodifiable {@link Map}
     * @throws MessagingException If a messaging error occurs
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getCapabilities(final IMAPFolder imapFolder) throws MessagingException {
        return ((Map<String, String>) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return java.util.Collections.unmodifiableMap(new HashMap<String, String>(p.getCapabilities()));
            }
        }));
    }

    /**
     * Retrieves the ACL for specified full name.
     *
     * @param fullName The full name
     * @param imapFolder The IMAP folder providing the protocol to use
     * @return The ACL
     * @throws MessagingException If ACL cannot be returned
     */
    public static ACL[] getACL(final String fullName, IMAPFolder imapFolder) throws MessagingException {
        return ((ACL[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                return protocol.getACL(fullName);
            }
        }));
    }

    /**
     * Lists subfolders under specified full name.
     *
     * @param fullName The full name
     * @param separator The separator character
     * @param imapFolder The IMAP folder providing the protocol to use
     * @return The available subfolders
     * @throws MessagingException If subfolders cannot be returned
     */
    public static ListInfo[] listSubfolders(final String fullName, final char separator, IMAPFolder imapFolder) throws MessagingException {
        ListInfo[] listInfos = ((ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                return protocol.list("", new StringBuilder(fullName).append(separator).append("%").toString());
            }
        }));
        return null == listInfos ? new ListInfo[0] : listInfos;
    }

    /**
     * Gets the LIST info for specified full name.
     *
     * @param fullName The full name
     * @param imapFolder The IMAP folder providing the protocol to use
     * @return The LIST info or <code>null</code> (if no such mailbox exists)
     * @throws MessagingException If LIST info cannot be returned
     */
    public static ListInfo getListInfo(final String fullName, IMAPFolder imapFolder) throws MessagingException {
        ListInfo[] listInfos = ((ListInfo[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                return protocol.list("", fullName);
            }
        }));
        if (null == listInfos || listInfos.length == 0) {
            return null;
        }
        return listInfos[0];
    }

    /**
     * Checks existence for specified full name.
     *
     * @param fullName The full name
     * @param imapFolder The IMAP folder providing the protocol to use
     * @return <code>true</code> if existing; otherwise <code>false</code>
     * @throws MessagingException If exists status cannot be returned
     */
    public static boolean exists(final String fullName, IMAPFolder imapFolder) throws MessagingException {
        ListInfo listInfo = getListInfo(fullName, imapFolder);
        return listInfo != null && fullName.equals(listInfo.name);
    }

    private static final Random RANDOM = new SecureRandom();

    /**
     * Gets a random string to use for a mailbox probe.
     *
     * @return The random string
     */
    static String getRandomProbe() {
        return RandomStringUtils.random(32, 97, 122, false, false, null, RANDOM);
    }

    /**
     * Checks if IMAP root folder allows subfolder creation.
     *
     * @param rootFolder The IMAP root folder
     * @param namespacePerUser <code>true</code> to assume a namespace per user; otherwise globally
     * @return <code>true</code> if IMAP root folder allows subfolder creation; otherwise <code>false</code>
     * @throws MessagingException If checking IMAP root folder for subfolder creation fails
     */
    public static Boolean canCreateSubfolder(final DefaultFolder rootFolder, final boolean namespacePerUser) throws MessagingException {
        return ((Boolean) rootFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                // Ensure a unique name is used to probe with
                String fname = getRandomProbe();

                // Encode the mailbox name as per RFC2060
                String mboxName = prepareStringArgument(fname);
                final String login = ((IMAPStore) rootFolder.getStore()).getUser();
                if (namespacePerUser) {
                    LOG.debug("Trying to probe IMAP server {} on behalf of {} for root subfolder capability with mbox name: {}", p.getHost(), login, mboxName);
                } else {
                    LOG.debug("Trying to probe IMAP server {} for root subfolder capability with mbox name: {}", p.getHost(), mboxName);
                }

                StringBuilder sb = new StringBuilder(48);
                boolean created = false;
                try {
                    // Perform CREATE command
                    Response[] r = performCommand(p, sb.append("CREATE ").append(mboxName).toString());
                    Response response = r[r.length - 1];
                    if (response.isOK()) {
                        // Well, CREATE command succeeded. Is folder really on root level...?
                        created = true;
                        boolean retval = true;

                        // Query the folder
                        ListInfo[] li = p.list("", fname);
                        if (li != null) {
                            boolean found = false;
                            for (int i = li.length; !found && i-- > 0;) {
                                found = fname.equals(li[i].name);
                            }
                            if (!found) {
                                if (namespacePerUser) {
                                    LOG.info("Probe of IMAP server {} on behalf of {} for root subfolder capability with mbox name {} failed as test folder was not created at expected position. Thus assuming no root subfolder capability", p.getHost(), login, mboxName);
                                } else {
                                    LOG.info("Probe of IMAP server {} for root subfolder capability with mbox name {} failed as test folder was not created at expected position. Thus assuming no root subfolder capability", p.getHost(), mboxName);
                                }
                            }
                            retval = found;
                        }

                        // Return result
                        if (retval) {
                            if (namespacePerUser) {
                                LOG.info("Probe of IMAP server {} on behalf of {} for root subfolder capability with mbox name {} succeeded. Thus assuming root subfolder capability", p.getHost(), login, mboxName);
                            } else {
                                LOG.info("Probe of IMAP server {} for root subfolder capability with mbox name {} succeeded. Thus assuming root subfolder capability", p.getHost(), mboxName);
                            }
                        }
                        return Boolean.valueOf(retval);
                    }

                    // No "OK" response from IMAP server
                    if (response.isNO()) {
                        // Examine "NO" response for possible "over quota" or "already exists" nature
                        String rest = response.getRest();
                        if (MimeMailException.isOverQuotaException(rest) || MimeMailException.isAlreadyExistsException(rest)) {
                            // Creating folder failed due to exceeded quota or because such a folder already exists. Thus assume "true".
                            if (namespacePerUser) {
                                LOG.info("Probe of IMAP server {} on behalf of {} for root subfolder capability with mbox name {} succeeded. Thus assuming root subfolder capability", p.getHost(), login, mboxName);
                            } else {
                                LOG.info("Probe of IMAP server {} for root subfolder capability with mbox name {} succeeded. Thus assuming root subfolder capability", p.getHost(), mboxName);
                            }
                            return Boolean.TRUE;
                        }

                        // Failed...
                        if (namespacePerUser) {
                            LOG.info("Probe of IMAP server {} on behalf of {} for root subfolder capability with mbox name {} failed (\"NO {}\"). Thus assuming no root subfolder capability", p.getHost(), login, mboxName, rest);
                        } else {
                            LOG.info("Probe of IMAP server {} for root subfolder capability with mbox name {} failed (\"NO {}\"). Thus assuming no root subfolder capability", p.getHost(), mboxName, rest);
                        }
                    }
                    return Boolean.FALSE;
                } finally {
                    if (created) {
                        // Delete probe folder
                        sb.setLength(0);
                        performCommand(p, sb.append("DELETE ").append(mboxName).toString());
                    }
                }
            }
        }));
    }

    /**
     * Checks if IMAP server supports specified folder type.
     * <p>
     * This method is useful to detect if MBox format is enabled. If enabled a mail folder can only either hold subfolders or hold messages.
     * Furthermore the folder type on creation is determined by the folder name. If folder name ends with the server-defined folder
     * separator character its type is HOLDS-FOLDERS; otherwise its type is HOLDS-MESSAGES.
     * <p>
     * Note that there's currently an unresolved problem concerning deletion of mail folders. Since the trash folder is created to hold only
     * messages, no backup of the deleted folder can be copied to trash folder; meaning the folder in question (including messages, its
     * subfolders, and subfolders' contents) is irretrievably lost.
     *
     * @param imapFolder An IMAP folder
     * @param type The folder type to check
     * @return <code>true</code> if IMAP server supports specified folder type; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    public static boolean supportsFolderType(final IMAPFolder imapFolder, final int type, final String fullnamePrefix) throws MessagingException {
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String fullName;
                if (null == fullnamePrefix || fullnamePrefix.length() == 0) {
                    fullName = Long.toString(System.currentTimeMillis());
                } else {
                    fullName = new StringBuilder(64).append(fullnamePrefix).append(Long.toString(System.currentTimeMillis())).toString();
                }
                try {
                    Boolean retval = Boolean.TRUE;
                    boolean delete = false;
                    try {
                        if ((type & Folder.HOLDS_MESSAGES) == 0) {
                            // Only holds folders
                            final char separator = getSeparator(p);
                            p.create(fullName + separator);
                            delete = true;
                        } else {
                            p.create(fullName);
                            delete = true;
                            /*
                             * Some IMAP servers do not allow creation of folders that can contain messages AND subfolders. Verify that
                             * created folder may also contain subfolders.
                             */
                            if ((type & Folder.HOLDS_FOLDERS) != 0) {
                                final ListInfo[] li = p.list("", fullName);
                                if (li != null && !li[0].hasInferiors) {
                                    /*
                                     * The new folder doesn't support inferiors.
                                     */
                                    retval = Boolean.FALSE;
                                }
                            }
                        }
                    } finally {
                        if (delete) {
                            p.delete(fullName);
                        }
                    }
                    return retval;
                } catch (final CommandFailedException e) {
                    /*
                     * Either creation or deletion of temporary folder failed. Assume maildir folder format.
                     */
                    LOG.debug("Either creation or deletion of temporary folder failed. Assume maildir folder format.", e);
                    return Boolean.valueOf((((type & Folder.HOLDS_MESSAGES) > 0)) && ((type & Folder.HOLDS_FOLDERS) > 0));
                }
            }
        }))).booleanValue();
    }

    /**
     * Gets the separator character of the IMAP server associated with specified IMAP folder.
     *
     * @param imapFolder The IMAP folder to obtain IMAP protocol
     * @return The separator character
     * @throws MessagingException If a messaging error occurs
     */
    public static char getSeparator(final IMAPFolder imapFolder) throws MessagingException {
        return ((Character) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                return Character.valueOf(getSeparator(p));
            }
        }))).charValue();
    }

    protected static char getSeparator(final IMAPProtocol p) throws ProtocolException {
        final String dummyFullname = Long.toString(System.currentTimeMillis());
        final ListInfo[] li;
        if (p.isREV1()) {
            li = p.list(dummyFullname, "");
        } else {
            li = p.list("", dummyFullname);
        }
        if (li != null) {
            return li[0].separator;
        }
        return MailProperties.getInstance().getDefaultSeparator();
    }

    /**
     * Gets total, recent, and unseen counts from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The total, recent, and unseen counts wrapped in an <code>int</code> array
     * @throws MessagingException If determining counts fails
     */
    public static int[] getStatus(final IMAPFolder imapFolder) throws MessagingException {
        return getStatus(imapFolder.getFullName(), imapFolder);
    }

    /**
     * Gets total, recent, and unseen counts from given IMAP folder
     *
     * @param fullName The full name of the folder whose STATUS shall be returned
     * @param imapFolder An IMAP folder providing connected {@link IMAPProtocol protocol}
     * @return The total, recent, and unseen counts wrapped in an <code>int</code> array
     * @throws MessagingException If determining counts fails
     */
    public static int[] getStatus(final String fullName, final IMAPFolder imapFolder) throws MessagingException {
        return (int[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                args.writeString(BASE64MailboxEncoder.encode(fullName));
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
                final Response[] r = performCommand(protocol, "STATUS", args);
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
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                /*
                 * Dispatch remaining untagged responses
                 */
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return new int[] { total, recent, unseen };
            }
        });
    }

    /**
     * Gets ACL list from denoted IMAP folder.
     *
     * @param fullName The full name of the folder whose ACL list shall be returned
     * @param imapFolder An IMAP folder providing connected {@link IMAPProtocol protocol}
     * @param checkCapabilities Whether to check for needed capability
     * @return The ACL list
     * @throws MessagingException If determining counts fails
     */
    @SuppressWarnings("unchecked")
    public static List<ACL> getAcl(final String fullName, final IMAPFolder imapFolder, final boolean checkCapabilities) throws MessagingException {
        return (List<ACL>) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                if (checkCapabilities && !protocol.hasCapability("ACL")) {
                    throw new com.sun.mail.iap.BadCommandException("ACL not supported");
                }
                /*
                 * Arguments...
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(fullName));
                /*
                 * Execute
                 */
                final Response[] r = performCommand(protocol, "GETACL", args);
                final Response response = r[r.length-1];
                /*
                 * Grab all ACL responses
                 */
                final List<ACL> list;
                if (response.isOK()) {
                    list = new ArrayList<ACL>(r.length);
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("ACL")) {
                            /*
                             * Read name of mailbox and throw away
                             */
                            ir.readAtomString();
                            String name = null;
                            while ((name = ir.readAtomString()) != null) {
                                final String rights = ir.readAtomString();
                                if (rights == null) {
                                    break;
                                }
                                final ACL acl = new ACL(name, new Rights(rights));
                                list.add(acl);
                            }
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("GETACL " + fullName));
                    list = java.util.Collections.emptyList();
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return list;
            }
        });
    }

    /**
     * Gets MYRIGHTS from denoted IMAP folder.
     *
     * @param fullName The full name of the folder whose MYRIGHTS shall be returned
     * @param imapFolder An IMAP folder providing connected {@link IMAPProtocol protocol}
     * @param checkCapabilities Whether to check for needed capability
     * @return The MYRIGHTS
     * @throws MessagingException If determining counts fails
     */
    public static Rights getMyRights(final String fullName, final IMAPFolder imapFolder, final boolean checkCapabilities) throws MessagingException {
        return (Rights) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                if (checkCapabilities && !protocol.hasCapability("ACL")) {
                    throw new com.sun.mail.iap.BadCommandException("ACL not supported");
                }
                /*
                 * Arguments...
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(fullName));
                /*
                 * Execute
                 */
                final String command = "MYRIGHTS";
                final Response[] r = performCommand(protocol, command, args);
                final Response response = r[r.length-1];
                /*
                 * Grab all responses
                 */
                Rights rights = null;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals(command)) {
                            /*
                             * Read name of mailbox and throw away
                             */
                            ir.readAtomString();
                            final String rs = ir.readAtomString();
                            if (rights == null) {
                                rights = new Rights(rs);
                            }
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("MYRIGHTS " + fullName));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return rights;
            }
        });
    }

    /**
     * Gets unread/unseen message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The unread message count
     * @throws MessagingException If determining counts fails
     */
    public static int getUnread(final IMAPFolder imapFolder) throws MessagingException {
        return getUnread(imapFolder, false);
    }

    /**
     * Gets unread/unseen message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The unread message count
     * @throws MessagingException If determining counts fails
     */
    public static int getUnread(final IMAPFolder imapFolder, final boolean ignoreDeleted) throws MessagingException {
        return ((Integer) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                /*
                 * If ignoreDeleted is true, perform via "SEARCH UNSEEN NOT DELETED" command
                 */
                if (ignoreDeleted) {
                    String command = COMMAND_SEARCH_UNSEEN_NOT_DELETED;
                    Response[] r = performCommand(protocol, command);
                    int unread = 0;
                    Response response = r[r.length - 1];
                    if (response.isOK()) {
                        for (int i = 0, len = r.length - 1; i < len; i++) {
                            if (r[i] instanceof IMAPResponse) {
                                final IMAPResponse ir = (IMAPResponse) r[i];
                                /*
                                 * The SEARCH response from the server contains a listing of message sequence numbers corresponding to those
                                 * messages that match the searching criteria.
                                 */
                                if (ir.keyEquals(COMMAND_SEARCH)) {
                                    while (ir.readAtomString() != null) {
                                        unread++;
                                    }
                                }
                            }
                            r[i] = null;
                        }
                        notifyResponseHandlers(r, protocol);
                    } else if (response.isBAD()) {
                        if (ImapUtility.isInvalidMessageset(response)) {
                            return new int[0];
                        }
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        protocol.handleResult(response);
                    }
                    return Integer.valueOf(unread);
                }
                /*
                 * Perform via STATUS command
                 */
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
                final String[] items = { "UNSEEN" };
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, "STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int unread = -1;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            final int status = parseStatusResponse(ir, "UNSEEN")[0];
                            if (status != -1) {
                                unread = status;
                            }
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return Integer.valueOf(unread);
            }
        })).intValue();
    }

    /**
     * Gets recent message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The total/unread message count
     * @return The recent message count
     * @throws MessagingException If determining counts fails
     */
    public static int getRecent(final IMAPStore imapStore, final String fullName) throws MessagingException {
        final DefaultFolder defaultFolder = (DefaultFolder) imapStore.getDefaultFolder();
        return ((Integer) defaultFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                args.writeString(BASE64MailboxEncoder.encode(fullName));
                /*
                 * Item arguments
                 */
                final Argument itemArgs = new Argument();
                final String[] items = { "RECENT" };
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, "STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int unread = -1;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            final int status = parseStatusResponse(ir, "RECENT")[0];
                            if (status != -1) {
                                unread = status;
                            }
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return Integer.valueOf(unread);
            }
        })).intValue();
    }

    /**
     * Gets total message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The total/unread message count
     * @return The total message count
     * @throws MessagingException If determining counts fails
     */
    public static int getTotal(final IMAPStore imapStore, final String fullName) throws MessagingException {
        final DefaultFolder defaultFolder = (DefaultFolder) imapStore.getDefaultFolder();
        return ((Integer) defaultFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                args.writeString(BASE64MailboxEncoder.encode(fullName));
                /*
                 * Item arguments
                 */
                final Argument itemArgs = new Argument();
                final String[] items = { "MESSAGES" };
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, "STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int total = -1;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            final int status = parseStatusResponse(ir, "MESSAGES")[0];
                            if (status != -1) {
                                total = status;
                            }
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return Integer.valueOf(total);
            }
        })).intValue();
    }

    /**
     * Gets total/unread message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The total/unread message count
     * @throws MessagingException If determining counts fails
     */
    public static int[] getTotalAndUnread(final IMAPFolder imapFolder) throws MessagingException {
        return ((int[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                final String[] items = { "MESSAGES", "UNSEEN"};
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, "STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int[] ret = null;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            ret = parseStatusResponse(ir, "MESSAGES", "UNSEEN");
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return ret;
            }
        }));
    }

    /**
     * Gets total/unread message count from given IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @return The total/unread message count
     * @throws MessagingException If determining counts fails
     */
    public static int[] getTotalAndUnread(final IMAPStore imapStore, final String fullName) throws MessagingException {
        // TODO: Main method for acquiring STATUS information
        final DefaultFolder defaultFolder = (DefaultFolder) imapStore.getDefaultFolder();
        return ((int[]) defaultFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                args.writeString(BASE64MailboxEncoder.encode(fullName));
                /*
                 * Item arguments
                 */
                final Argument itemArgs = new Argument();
                final String[] items = { "MESSAGES", "UNSEEN"};
                for (int i = 0, len = items.length; i < len; i++) {
                    itemArgs.writeAtom(items[i]);
                }
                args.writeArgument(itemArgs);
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(protocol, "STATUS", args);
                final Response response = r[r.length - 1];
                /*
                 * Look for STATUS responses
                 */
                int[] ret = null;
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        final IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            ret = parseStatusResponse(ir, "MESSAGES", "UNSEEN");
                            r[i] = null;
                        }
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("STATUS", args));
                }
                notifyResponseHandlers(r, protocol);
                protocol.handleResult(response);
                return ret;
            }
        }));
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
    protected static int[] parseStatusResponse(final Response statusResponse) throws ParsingException {
        if (null == statusResponse) {
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        int cnt = 0;
        {
            final String resp = statusResponse.toString();
            if (com.openexchange.java.Strings.isEmpty(resp)) {
                throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
            }
            int pos = -1;
            while ((pos = resp.indexOf('(', pos+1)) > 0) {
                cnt++;
            }
        }
        if (cnt <= 0) {
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        /*
         * Read until opening parenthesis or EOF
         */
        byte b = 0;
        do {
            b = statusResponse.readByte();
            if (b == '(' && --cnt > 0) {
                b = statusResponse.readByte();
            }
        } while (b != 0 && b != '(');
        if (0 == b || cnt > 0) {
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
     * Parses number of total messages from specified IMAP response whose key is equal to <code>&quot;STATUS&quot;</code>
     * .
     *
     * @param statusResponse The <code>&quot;STATUS&quot;</code> IMAP response to parse.
     * @param counterTypes The counter types; either <code>MESSAGES</code>, <code>RECENT</code> or <code>UNSEEN</code>
     * @return The  number of total messages
     * @throws ParsingException If parsing STATUS response fails
     */
    protected static int[] parseStatusResponse(final Response statusResponse, final String... counterTypes) throws ParsingException {
        if (null == counterTypes || counterTypes.length == 0) {
            return new int[0];
        }
        if (null == statusResponse) {
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        int cnt = 0;
        {
            final String resp = statusResponse.toString();
            if (com.openexchange.java.Strings.isEmpty(resp)) {
                throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
            }
            int pos = -1;
            while ((pos = resp.indexOf('(', pos+1)) > 0) {
                cnt++;
            }
        }
        if (cnt <= 0) {
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        /*
         * Read until opening parenthesis or EOF
         */
        byte b = 0;
        do {
            b = statusResponse.readByte();
            if (b == '(' && --cnt > 0) {
                b = statusResponse.readByte();
            }
        } while (b != 0 && b != '(');
        if (0 == b || cnt > 0) {
            // EOF
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        /*
         * Parse parenthesized list
         */
        final int[] arr = new int[counterTypes.length];
        Arrays.fill(arr, -1);
        do {
            final String attr = statusResponse.readAtom();
            final int pos = find(attr, counterTypes);
            if (pos >= 0) {
                arr[pos] = statusResponse.readNumber();
            }
        } while (statusResponse.readByte() != ')');
        return arr;
    }

    private static int find(final String elem, final String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(elem)) {
                return i;
            }
        }
        return -1;
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

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Encode the mbox as per RFC2060
                 */
                final Argument args = new Argument();
                args.writeString(BASE64MailboxEncoder.encode(imapFolder.getFullName()));
                /*
                 * Perform command
                 */
                final Response[] r = performCommand(p, "GETQUOTAROOT", args);
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
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("GETQUOTAROOT " + imapFolder.getFullName()));
                }
                notifyResponseHandlers(r, p);
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
    protected static Quota parseQuota(final IMAPResponse r) throws ParsingException {
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
        try {
            ((IMAPFolder) store.getDefaultFolder()).doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol p) {
                    final Argument args = new Argument();
                    args.writeString(BASE64MailboxEncoder.encode(folder));
                    performCommand(p, (subscribe ? "SUBSCRIBE" : "UNSUBSCRIBE"), args);
                    return null;
                }
            });
        } catch (final Exception e) {
            LOG.error("", e);
        }
    }

    private static final String COMMAND_LSUB = "LSUB";

    /**
     * Checks folder subscription for the folder denoted by specified full name.
     * <p>
     * This method imitates the behavior from {@link IMAPFolder#isSubscribed() isSubscribde()} that is a namespace folder's subscription
     * status is checked with specified separator character appended to full name.
     *
     * @param fullName The folder's full name
     * @param separator The separator character
     * @param isNamespace <code>true</code> if denoted folder is a namespace folder; otherwise <code>false</code>
     * @param defaultFolder The IMAP store's default folder
     * @return <code>true</code> if folder is subscribed; otherwise <code>false</code>
     * @throws MessagingException If checking folder subscription fails
     */
    public static boolean isSubscribed(final String fullName, final char separator, final boolean isNamespace, final IMAPFolder defaultFolder) throws MessagingException {
        final String lfolder = ((isNamespace || (fullName.length() == 0)) && (separator != '\0')) ? fullName + separator : fullName;
        return ((Boolean) (defaultFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                String command = new StringBuilder().append(COMMAND_LSUB).append(" \"\" ").append(prepareStringArgument(lfolder)).toString();
                Response[] r = performCommand(p, command);
                Response response = r[r.length - 1];
                if (response.isOK()) {
                    int res = -1;
                    final int len = r.length - 1;
                    for (int i = 0; i < len; i++) {
                        if ((r[i] instanceof IMAPResponse) && (res = parseIMAPResponse((IMAPResponse) r[i])) != -1) {
                            return res == 0 ? Boolean.FALSE : Boolean.TRUE;
                        }
                        r[i] = null;
                    }
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                }
                notifyResponseHandlers(r, p);
                p.handleResult(response);
                return Boolean.FALSE;
            }

            private int parseIMAPResponse(final IMAPResponse ir) throws ParsingException {
                if (ir.keyEquals(COMMAND_LSUB)) {
                    final ListInfo li = new ListInfo(ir);
                    if (li.name.equals(fullName)) {
                        return li.canOpen ? 1 : 0;
                    }
                }
                return -1;
            }
        }))).booleanValue();
    }

    public static void renameFolder(final IMAPFolder folder, final char separator, final IMAPFolder renameTo) throws MessagingException {
        final String renameFullname = renameTo.getFullName();
        final Boolean ret = (Boolean) folder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                if (renameTo.getStore() != folder.getStore()) {
                    throw new ProtocolException("Can't rename across Stores");
                }

                /*
                 * Encode the mbox as per RFC2060
                 */
                final String original = prepareStringArgument(folder.getFullName());
                final String newName = prepareStringArgument(renameFullname);
                // Create command
                final String command = new StringBuilder(32).append("RENAME ").append(original).append(' ').append(newName).toString();
                // Issue command
                final Response[] r = performCommand(protocol, command);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    return Boolean.TRUE;
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(response.toString());
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(response.toString());
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    protocol.handleResult(response);
                }
                return Boolean.FALSE;
            }
        });
        if (null == ret) {
            ProtocolException pex = new ProtocolException("IMAP folder \"" + folder.getFullName() + "\" cannot be renamed.");
            throw new MessagingException(pex.getMessage(), pex);
        }

        // Reset fields 'exists' and 'attributes'
        try {
            Field field = FIELD_IMAPFOLDER_EXISTS;
            field.setBoolean(folder, false);

            field = FIELD_IMAPFOLDER_ATTRIBUTES;
            field.set(folder, null);
        } catch (IllegalAccessException e) {
            LOG.error("", e);
        }

        new ExtendedIMAPFolder(folder, separator).triggerNotifyFolderListeners(FolderEvent.RENAMED);
    }

    public static void createFolder(final IMAPFolder newFolder, final char separator, final int type) throws MessagingException {
        createFolder(newFolder, separator, type, true);
    }

    public static void createFolder(final IMAPFolder newFolder, final char separator, final int type, final boolean errorOnUnsupportedType) throws MessagingException {
        createFolder(newFolder, separator, type, errorOnUnsupportedType, null);
    }

    public static void createFolder(final IMAPFolder newFolder, final char separator, final int type, final boolean errorOnUnsupportedType, final Collection<String> specialUses) throws MessagingException {
        final Boolean ret = (Boolean) newFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                final String fullName = newFolder.getFullName();
                // Encode the mbox as per RFC2060
                final String mbox;
                if ((type & Folder.HOLDS_MESSAGES) == 0) {
                    // Only holds folders
                    mbox = prepareStringArgument(fullName + separator);
                } else {
                    mbox = prepareStringArgument(fullName);
                }
                // Create command
                final String command;
                {
                    final StringBuilder cmdBuilder = new StringBuilder(32).append("CREATE ").append(mbox);
                    if (null != specialUses && !specialUses.isEmpty()) {
                        cmdBuilder.append(" (USE (");
                        final Iterator<String> iterator = specialUses.iterator();
                        cmdBuilder.append(iterator.next());
                        while (iterator.hasNext()) {
                            cmdBuilder.append(' ').append(iterator.next());
                        }
                        cmdBuilder.append("))");
                    }
                    command = cmdBuilder.toString();
                }
                // Issue command
                final Response[] r = performCommand(protocol, command);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    /*
                     * Certain IMAP servers do not allow creation of folders that can contain messages AND subfolders.
                     */
                    if ((type & Folder.HOLDS_FOLDERS) != 0) {
                        final ListInfo[] li = protocol.list("", fullName);
                        if (errorOnUnsupportedType && li != null && !li[0].hasInferiors) {
                            protocol.delete(fullName);
                            throw new ProtocolException(new StringBuilder(32).append("Created IMAP folder \"").append(fullName).append(
                                "\" (").append(newFolder.getStore().toString()).append(") should hold folders AND messages, but can only hold messages.").toString());
                        }
                    }
                    return Boolean.TRUE;
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), newFolder)));
                } else if (response.isNO()) {
                    if (response.toString().toLowerCase(Locale.US).indexOf("already exists") > 0) {
                        return Boolean.TRUE;
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), newFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    protocol.handleResult(response);
                }
                return Boolean.FALSE;
            }
        });
        if (null == ret) {
            final ProtocolException pex =
                new ProtocolException(new StringBuilder(64).append("IMAP folder \"").append(newFolder.getFullName()).append(
                    "\" (").append(newFolder.getStore().toString()).append(") cannot be created.").toString());
            throw new MessagingException(pex.getMessage(), pex);
        }
        // Set exists, type, and attributes
        if (newFolder.exists()) {
            new ExtendedIMAPFolder(newFolder, separator).triggerNotifyFolderListeners(FolderEvent.CREATED);
        }
    }

    /**
     * Sets the given SPECIAL-USE flags for specified IMAP folder
     *
     * @param imapFolder The IMAP folder
     * @param specialUses The SPECIAL-USE flags to apply; e.g. <code>"\Draft"</code>, <code>"\Sent"</code>, <code>"\Junk"</code>, or <code>"\Trash"</code>
     * @throws MessagingException If operation fails
     */
    public static void setSpecialUses(final IMAPFolder imapFolder, final Collection<String> specialUses) throws MessagingException {
        if (null == specialUses || specialUses.isEmpty()) {
            return;
        }
        final int type = imapFolder.getType();
        final char sep = imapFolder.getSeparator();
        imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                String fullName = imapFolder.getFullName();

                // Encode the mbox as per RFC2060
                String mbox = (type & Folder.HOLDS_MESSAGES) == 0 ? prepareStringArgument(fullName + sep) : prepareStringArgument(fullName);

                // Craft IMAP command
                String command;
                {
                    StringBuilder cmdBuilder = new StringBuilder(32).append("SETMETADATA ").append(mbox).append(' ');
                    cmdBuilder.append("(");
                    for (String specialUse : specialUses) {
                        cmdBuilder.append("/private/specialuse ");
                        if (null == specialUse) {
                            cmdBuilder.append("NIL");
                        } else {
                            if (specialUse.charAt(0) == '\\') {
                                cmdBuilder.append('"').append(specialUse).append('"');
                            } else {
                                cmdBuilder.append(specialUse);
                            }
                        }
                    }
                    cmdBuilder.append(")");
                    command = cmdBuilder.toString();
                }

                // Issue command
                Response[] r = performCommand(protocol, command);
                Response response = r[r.length - 1];
                if (response.isOK()) {
                    for (int i = 0, len = r.length - 1; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("METADATA")) {
                            r[i] = null;
                        }
                    }
                    return Boolean.TRUE;
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    protocol.handleResult(response);
                }
                return Boolean.FALSE;
            }
        });
    }

    private final static String TEMPL_UID_STORE_FLAGS = "UID STORE %s %sFLAGS (%s)";

    private final static String TEMPL_STORE_FLAGS = "STORE %s %sFLAGS (%s)";

    /** The string constant for color labels */
    static final Object ALL_COLOR_LABELS = "$cl_0 $cl_1 $cl_2 $cl_3 $cl_4 $cl_5 $cl_6 $cl_7 $cl_8 $cl_9 $cl_10" + " cl_0 cl_1 cl_2 cl_3 cl_4 cl_5 cl_6 cl_7 cl_8 cl_9 cl_10";

    /**
     * Clears an sets only known colors in user defined IMAP flag
     * <p>
     * All known color labels:
     * <code>$cl_0&nbsp;$cl_1&nbsp;$cl_2&nbsp;$cl_3&nbsp;$cl_4&nbsp;$cl_5&nbsp;$cl_6&nbsp;$cl_7&nbsp;$cl_8&nbsp;$cl_9&nbsp;$cl_10</code>
     * <code>cl_0&nbsp;cl_1&nbsp;cl_2&nbsp;cl_3&nbsp;cl_4&nbsp;cl_5&nbsp;cl_6&nbsp;cl_7&nbsp;cl_8&nbsp;cl_9&nbsp;cl_10</code>
     *
     * @param imapFolder - the imap folder
     * @param msgUIDs - the message UIDs
     * @param colorLabelFlag - the color id
     * @return <code>true</code> if color could be set successfully; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static void clearAndSetColorLabelSafely(final IMAPFolder imapFolder, final long[] msgUIDs, final String colorLabelFlag) throws MessagingException, OXException {
        // Only set colors allowed in ALL_COLOR_LABELS
        if (!MailMessage.isValidColorLabel(colorLabelFlag)) {
            throw IMAPException.create(IMAPException.Code.FLAG_FAILED, colorLabelFlag, "Unknown color label.");
        }
        clearAllColorLabels(imapFolder, msgUIDs);
        setColorLabel(imapFolder, msgUIDs, colorLabelFlag);
    }

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
    private static boolean clearAllColorLabels(final IMAPFolder imapFolder, final long[] msgUIDs) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args;
                final String format;
                if (null == msgUIDs) {
                    args = (1 == messageCount ? new String[] { "1" } : ARGS_ALL);
                    format = TEMPL_STORE_FLAGS;
                } else {
                    args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false, 160);
                    format = TEMPL_UID_STORE_FLAGS;
                }
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {

                    final String command = String.format(format, args[i], "-", ALL_COLOR_LABELS);
                    r = performCommand(p, command);
                    response = r[r.length - 1];
                    if (response.isOK()) {
                        notifyResponseHandlers(r, p);
                        continue Next;
                    } else if (response.isBAD()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
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
    private static boolean setColorLabel(final IMAPFolder imapFolder, final long[] msgUIDs, final String colorLabelFlag) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args;
                final String format;
                if (null == msgUIDs) {
                    args = (1 == messageCount ? new String[] { "1" } : ARGS_ALL);
                    format = TEMPL_STORE_FLAGS;
                } else {
                    args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false, 32 + colorLabelFlag.length());
                    format = TEMPL_UID_STORE_FLAGS;
                }
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {
                    final String command = String.format(format, args[i], "+", colorLabelFlag);
                    r = performCommand(p, command);
                    response = r[r.length - 1];
                    if (response.isOK()) {
                        notifyResponseHandlers(r, p);
                        continue Next;
                    } else if (response.isBAD()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }
        }))).booleanValue();
    }

    /**
     * Sets/Unsets specified user flags from messages which correspond to given UIDs.
     *
     * @param imapFolder The IMAP folder
     * @param msgUIDs The message UIDs
     * @param flags The flags to set/unset
     * @param set <code>true</code> to set; <code>false</code> to unset flags
     * @return <code>true</code> if everything went fine; otherwise <code>false</code>
     * @throws MessagingException - if an error occurs in underlying protocol
     */
    public static boolean setUserFlags(final IMAPFolder imapFolder, final long[] msgUIDs, final String[] flags, final boolean set) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args;
                final String format;
                if (null == msgUIDs) {
                    args = (1 == messageCount ? new String[] { "1" } : ARGS_ALL);
                    format = TEMPL_STORE_FLAGS;
                } else {
                    args = IMAPNumArgSplitter.splitUIDArg(msgUIDs, false, 160);
                    format = TEMPL_UID_STORE_FLAGS;
                }
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {

                    final String command = String.format(format, args[i], set ? "+" : "-", userFlags2String(flags));
                    r = performCommand(p, command);
                    response = r[r.length - 1];
                    if (response.isOK()) {
                        notifyResponseHandlers(r, p);
                        continue Next;
                    } else if (response.isBAD()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }

        }))).booleanValue();
    }

    static String userFlags2String(final String[] flags) {
        final StringBuilder sb = new StringBuilder(64);
        boolean first = true;
        for (String flag : flags) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(flag);
        }
        return sb.toString();
    }

    private static final String COMMAND_CLOSE = "CLOSE";

    /**
     * Force to send a CLOSE command to IMAP server that is explicitly <b>not</b> handled by JavaMail API. It really does not matter if this
     * command succeeds or breaks up in a <code>MessagingException</code>. Therefore neither a return value is defined nor any exception is
     * thrown.
     *
     * @param f The IMAP folder providing the connected store
     */
    public static void forceCloseCommand(final IMAPFolder f) {
        try {
            f.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    final Response[] r = performCommand(protocol, COMMAND_CLOSE);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    return Boolean.valueOf(response.isOK());
                }

            });
        } catch (final MessagingException e) {
            LOG.trace("", e);
        }
    }

    private static final String COMMAND_NOOP = "NOOP";

    /**
     * Force to send a NOOP command to IMAP server that is explicitly <b>not</b> handled by JavaMail API. It really does not matter if this
     * command succeeds or breaks up in a <code>MessagingException</code>. Therefore neither a return value is defined nor any exception is
     * thrown.
     *
     * @param f The IMAP folder providing the connected store
     */
    public static void forceNoopCommand(final IMAPFolder f) {
        try {
            f.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    final Response[] r = performCommand(protocol, COMMAND_NOOP);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    return Boolean.valueOf(response.isOK());
                }

            });
        } catch (final MessagingException e) {
            LOG.trace("", e);
        }
    }

    /**
     * Tries to propagate specified client IP through a NOOP command; e.g. "A01 NOOP <CLIENT_IP>".
     *
     * @param f The IMAP folder
     * @param clientIP The client IP address
     */
    public static void propagateClientIP(final IMAPFolder f, final String clientIP) {
        try {
            f.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    final Response[] r = performCommand(protocol, new StringBuilder(COMMAND_NOOP).append(' ').append(clientIP).toString());
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    return Boolean.valueOf(response.isOK());
                }

            });
        } catch (final MessagingException e) {
            LOG.trace("", e);
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
    public static int[] getServerSortList(IMAPFolder folder, String sortCrit, int[] toSort) throws MessagingException {
        if (null == toSort) {
            return getServerSortList(folder, sortCrit, RANGE_ALL);
        }

        // Need to build message range argument
        String[] numArgs = IMAPNumArgSplitter.getSeqNumArg(toSort, false, true, -1);
        if (1 == numArgs.length) {
            return getServerSortList(folder, sortCrit, numArgs);
        }

        // The messages to sort do not fit into a single command -- Sort them all
        int[] allSorted = getServerSortList(folder, sortCrit, RANGE_ALL);

        class SeqNumOrdinal implements Comparable<SeqNumOrdinal> {
            final int seqNum;
            final int ordinal;

            SeqNumOrdinal(int seqNum, int ordinal) {
                super();
                this.seqNum = seqNum;
                this.ordinal = ordinal;
            }

            @Override
            public int compareTo(SeqNumOrdinal o) {
                int thisOrdinal = ordinal;
                int otherOrdinal = o.ordinal;
                return thisOrdinal < otherOrdinal ? -1 : (thisOrdinal == otherOrdinal ? 0 : 1);
            }
        }

        int length = toSort.length;
        List<SeqNumOrdinal> list = new ArrayList<SeqNumOrdinal>(length);
        for (int i = 0; i < length; i++) {
            int seqNum = toSort[i];
            list.add(new SeqNumOrdinal(seqNum, getIndexFor(seqNum, allSorted)));
        }
        Collections.sort(list);

        int[] sorted = new int[length];
        for (int i = 0; i < length; i++) {
            sorted[i] = list.get(i).seqNum;
        }
        return sorted;
    }

    private static int getIndexFor(int seqNum, int[] seqNums) {
        for (int i = 0; i < seqNums.length; i++) {
            if (seqNum == seqNums[i]) {
                return i;
            }
        }
        return -1;
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

    /** The <code>"SORT"</code> string constant */
    static final String COMMAND_SORT = "SORT".intern();

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
        Object val = imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                String command = new StringBuilder(numArgument.length() + 16).append("SORT (").append(sortCrit).append(") UTF-8 ").append(numArgument).toString();
                Response[] r = performCommand(p, command);
                Response response = r[r.length - 1];
                TIntList sia = new TIntArrayList(32);
                if (response.isOK()) {
                    for (int i = 0, len = r.length; i < len; i++) {
                        if (!(r[i] instanceof IMAPResponse)) {
                            continue;
                        }
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals(COMMAND_SORT)) {
                            for (String num; (num = ir.readAtomString()) != null && num.length() > 0;) {
                                try {
                                    sia.add(Integer.parseInt(num));
                                } catch (NumberFormatException e) {
                                    LOG.error("", e);
                                    throw wrapException(e, "Invalid Message Number: " + num);
                                }
                            }
                        }
                        r[i] = null;
                    }
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return sia.toArray();
            }
        });
        return ((int[]) val);
    }

    private static final String COMMAND_SEARCH_UNSEEN = "SEARCH UNSEEN";
    private static final String COMMAND_SEARCH_UNSEEN_NOT_DELETED = "SEARCH UNSEEN NOT DELETED";

    private static final String COMMAND_SEARCH = "SEARCH";

    /**
     * Determines all unseen messages in specified folder.
     *
     * @param folder The IMAP folder
     * @param fields The desired fields
     * @param sortField The sort-by field
     * @param fastFetch Whether to perform a fast <code>FETCH</code> or not
     * @param limit The limit
     * @param serverInfo The IMAP server information
     * @return All unseen messages in specified folder
     * @throws MessagingException
     */
    public static Message[] getUnreadMessages(IMAPFolder folder, MailField[] fields, MailSortField sortField, OrderDirection orderDir, boolean fastFetch, int limit, IMAPServerInfo serverInfo, Session session) throws MessagingException {
        return getUnreadMessages(folder, fields, sortField, orderDir, fastFetch, limit, false, serverInfo, session);
    }

    /**
     * Determines all unseen messages in specified folder.
     *
     * @param folder The IMAP folder
     * @param fields The desired fields
     * @param sortField The sort-by field
     * @param fastFetch Whether to perform a fast <code>FETCH</code> or not
     * @param limit The limit
     * @param ignoreDeleted Whether to ignore deleted messages
     * @param serverInfo The IMAP server information
     * @return All unseen messages in specified folder
     * @throws MessagingException
     */
    public static Message[] getUnreadMessages(final IMAPFolder folder, final MailField[] fields, final MailSortField sortField, final OrderDirection orderDir, final boolean fastFetch, final int limit, final boolean ignoreDeleted, final IMAPServerInfo serverInfo, final Session session) throws MessagingException {
        final IMAPFolder imapFolder = folder;
        final Message[] val = (Message[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                String command = ignoreDeleted ? COMMAND_SEARCH_UNSEEN_NOT_DELETED : COMMAND_SEARCH_UNSEEN;
                Response[] r = performCommand(p, command);
                /*
                 * Result is something like: SEARCH 12 20 24
                 */
                int[] newMsgSeqNums;
                {
                    int[] tmp = handleSearchResponses(r, p, command);
                    if (limit > 0 && limit < tmp.length) {
                        try {
                            /*
                             * Sort exceeding list on server
                             */
                            IMAPStore imapStore = (IMAPStore) folder.getStore();
                            if (imapStore.hasCapability("SORT")) {
                                final MailSortField sortBy = sortField == null ? MailSortField.RECEIVED_DATE : sortField;
                                final String sortCriteria = IMAPSort.getSortCritForIMAPCommand(sortBy, orderDir == OrderDirection.DESC, imapStore.hasCapability("SORT=DISPLAY") && IMAPMessageStorage.allowSORTDISPLAY(session, serverInfo.getAccountId()));
                                if (tmp.length > 256) {
                                    /*
                                     * Sort all
                                     */
                                    final TIntSet set = new TIntHashSet(tmp);
                                    tmp = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria);
                                    final TIntList list = new TIntArrayList(limit);
                                    for (int i = 0, k = 0; i < tmp.length && k < limit; i++) {
                                        final int seqNum = tmp[i];
                                        if (set.contains(seqNum)) {
                                            list.add(seqNum);
                                            k++;
                                        }
                                    }
                                    tmp = list.toArray();
                                } else {
                                    /*
                                     * Sort specified sequence numbers
                                     */
                                    tmp = IMAPCommandsCollection.getServerSortList(imapFolder, sortCriteria, tmp);
                                }
                            }
                            /*
                             * Copy to fitting array
                             */
                            final int[] ni = new int[limit];
                            System.arraycopy(tmp, 0, ni, 0, limit);
                            newMsgSeqNums = ni;
                        } catch (final OXException e) {
                            throw wrapException(e, null);
                        } catch (final MessagingException e) {
                            throw wrapException(e, null);
                        } catch (final RuntimeException e) {
                            throw wrapException(e, null);
                        }
                    } else {
                        newMsgSeqNums = tmp;
                    }
                }
                /*
                 * No new messages found
                 */
                if (newMsgSeqNums.length == 0) {
                    return null;
                }
                /*
                 * Fetch messages
                 */
                final Message[] newMsgs;
                try {
                    final MailFields set = new MailFields(fields);
                    final boolean body = set.contains(MailField.BODY) || set.contains(MailField.FULL);
                    final MailField sort = MailField.toField((sortField == null ? MailSortField.RECEIVED_DATE : sortField).getListField());
                    final FetchProfile fp = null == sort ? getFetchProfile(fields, fastFetch) : getFetchProfile(fields, sort, fastFetch);
                    newMsgs = new MessageFetchIMAPCommand(folder, p.isREV1(), newMsgSeqNums, fp, serverInfo, false, false, body).doCommand();
                } catch (final MessagingException e) {
                    throw wrapException(e, null);
                }
                return newMsgs;
            }

            private int[] handleSearchResponses(final Response[] r, final IMAPProtocol p, String command) throws ProtocolException {
                final Response response = r[r.length - 1];
                final TIntList tmp = new TIntArrayList(32);
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
                                    tmp.add(Integer.parseInt(num));
                                } catch (final NumberFormatException e) {
                                    continue;
                                }
                            }
                        }
                        r[i] = null;
                    }
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new int[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        COMMAND_SEARCH_UNSEEN,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        COMMAND_SEARCH_UNSEEN,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return tmp.toArray();
            } // End of handleSearchResponses()

        });
        return val;
    }

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
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                String command = "EXPUNGE";
                final Response[] r = performCommand(p, command);
                final Response response = r[r.length - 1];
                if (response.isOK()) {
                    return Boolean.TRUE;
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return Boolean.FALSE;
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
                IMAPCommandsCollection.uidExpunge(imapFolder, uids);
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
                LOG.warn("UID EXPUNGE failed.\nPerforming fallback actions.", e);
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
     * @throws OXException
     */
    public static boolean isReadOnly(final IMAPFolder f) throws OXException {
        try {
            final Boolean val = (Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {

                /*
                 * (non-Javadoc)
                 * @see com.sun.mail.imap.IMAPFolder$ProtocolCommand#doCommand(com .sun.mail.imap.protocol.IMAPProtocol)
                 */
                @Override
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    /*
                     * Encode the mbox as per RFC2060
                     */
                    final Argument args = new Argument();
                    args.writeString(BASE64MailboxEncoder.encode(f.getFullName()));
                    /*
                     * Perform command
                     */
                    final Response[] r = performCommand(p, "SELECT", args);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    Boolean retval = Boolean.FALSE;
                    if (response.isOK()) { // command successful
                        retval = Boolean.valueOf(response.toString().indexOf("READ-ONLY") != -1);
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging("SELECT", args));
                        p.handleResult(response);
                    }
                    return retval;
                }
            });
            return val.booleanValue();
        } catch (final Exception e) {
            LOG.error("", e);
            throw IMAPException.create(IMAPException.Code.FAILED_READ_ONLY_CHECK, e, new Object[0]);
        }
    }

    /**
     * Checks if IMAP folder denoted by specified full name is allowed to be opened in desired mode.
     *
     * @param f The IMAP folder providing protocol access
     * @param fullName The full name to check
     * @param mode The desired open mode
     * @return <code>true</code> if IMAP folder denoted by specified full name is allowed to be opened in desired mode; otherwise
     *         <code>false</code>
     * @throws OXException If an IMAP error occurs
     */
    public static boolean canBeOpened(final IMAPFolder f, final String fullName, final int mode) throws OXException {
        if ((Folder.READ_ONLY != mode) && (Folder.READ_WRITE != mode)) {
            IMAPException.create(IMAPException.Code.UNKNOWN_FOLDER_MODE, Integer.valueOf(mode));
        }
        try {
            return ((Boolean) f.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                    final Boolean retval;
                    {
                        /*
                         * Encode the mbox as per RFC2060
                         */
                        final Argument args = new Argument();
                        args.writeString(BASE64MailboxEncoder.encode(fullName));
                        /*
                         * Perform command
                         */
                        String command = Folder.READ_ONLY == mode ? "EXAMINE" : "SELECT";
                        final Response[] r = performCommand(p, command, args);
                        /*
                         * Grab last response that should indicate an OK
                         */
                        final Response response = r[r.length - 1];
                        if (response.isOK()) { // command successful
                            retval = Boolean.TRUE;
                        } else {
                            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command + " " + fullName));
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
                    final Response[] r = performCommand(p, "SELECT", args);
                    /*
                     * Grab last response that should indicate an OK
                     */
                    final Response response = r[r.length - 1];
                    p.handleResult(response);
                    return retval;
                }
            })).booleanValue();
        } catch (final Exception e) {
            LOG.error("", e);
            throw IMAPException.create(IMAPException.Code.FAILED_READ_ONLY_CHECK, e, new Object[0]);
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
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        return (long[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                String command = FETCH_FLAGS;
                Response[] r = performCommand(p, command);
                int mlen = r.length - 1;
                Response response = r[mlen];
                long[] retval = null;
                if (response.isOK()) {
                    final Set<Long> set = new TreeSet<Long>();
                    final String flagsItemName = "FLAGS";
                    for (int i = 0; i < mlen; i++) {
                        if (!(r[i] instanceof FetchResponse)) {
                            continue;
                        }
                        final FetchResponse fr = (FetchResponse) r[i];
                        final boolean deleted;
                        {
                            final FLAGS item = getItemOf(FLAGS.class, fr, flagsItemName);
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
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                notifyResponseHandlers(r, p);
                return retval;
            }
        }));
    }

    private static final String TEMPL_FETCH_UID = "FETCH %s (UID)";

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
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        return (long[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                    final String command = String.format(TEMPL_FETCH_UID, args[i]);
                    r = performCommand(p, command);
                    final int len = r.length - 1;
                    response = r[len];
                    if (response.isOK()) {
                        for (int j = 0; j < len; j++) {
                            if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                final UID uidItem = getItemOf(UID.class, (FetchResponse) r[j], STR_UID);
                                uids[index++] = uidItem.uid;
                                r[j] = null;
                            }
                        }
                        notifyResponseHandlers(r, p);
                    } else if (response.isBAD()) {
                        if (ImapUtility.isInvalidMessageset(response)) {
                            return new long[0];
                        }
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
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

    /**
     * Detects the corresponding UIDs from given folder
     *
     * @param imapFolder The IMAP folder
     * @return The corresponding UIDs
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static long[] getUIDs(final IMAPFolder imapFolder) throws MessagingException {
        return getUIDCollection(imapFolder).toArray();
    }

    /**
     * Detects the corresponding UIDs from given folder
     *
     * @param imapFolder The IMAP folder
     * @return The corresponding UIDs
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static TLongCollection getUIDCollection(final IMAPFolder imapFolder) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return new TLongArrayList(0);
        }
        return (TLongList) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                Response[] r = null;
                Response response = null;
                final TLongList uids = new TLongArrayList(messageCount);
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
                final String command = String.format(TEMPL_FETCH_UID, "1:*");
                r = performCommand(p, command);
                final int len = r.length - 1;
                response = r[len];
                if (response.isOK()) {
                    for (int j = 0; j < len; j++) {
                        if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                            uids.add(getItemOf(UID.class, (FetchResponse) r[j], STR_UID).uid);
                            r[j] = null;
                        }
                    }
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new long[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return uids;
            }

        }));
    }

    private static final String TEMPL_UID_FETCH_UID = "UID FETCH %s (UID)";

    /**
     * Checks there is such a message with specified UID in given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @param uid The UID to check
     * @return <code>true</code> if such a message exists; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     */
    public static boolean existsMessage(final IMAPFolder imapFolder, final long uid) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            return false;
        }
        if (uid <= 0) {
            return false;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String command = String.format(TEMPL_UID_FETCH_UID, Long.toString(uid));
                final Response[] r = performCommand(p, command);
                final int len = r.length - 1;
                final Response response = r[len];
                r[len] = null;
                if (response.isOK()) {
                    return Boolean.valueOf(len > 0);
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return Boolean.FALSE;
            }

        }))).booleanValue();
    }

    /**
     * Maps specified UIDs to current corresponding sequence numbers.
     *
     * @param imapFolder The IMAP folder
     * @param uids The UIDs
     * @return The current corresponding sequence numbers
     * @throws MessagingException If a messaging error occurs
     */
    public static int[] uids2SeqNums(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return new int[0];
        }
        final int length = uids.length;
        if (length == 0) {
            /*
             * Empty array...
             */
            return new int[0];
        }
        return (int[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                /*
                 * Execute command
                 */
                final TLongIntHashMap seqNumMap = new TLongIntHashMap(length);
                final String[] args = messageCount == length ? (1 == messageCount ? new String[] { "1" } : ARGS_ALL) : IMAPNumArgSplitter.splitUIDArg(uids, false, 16); // "UID FETCH <uids> (UID)"
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
                    final String command = String.format(TEMPL_UID_FETCH_UID, args[k]);
                    final Response[] r = performCommand(p, command);
                    final int len = r.length - 1;
                    final Response response = r[len];
                    r[len] = null;
                    if (response.isOK() && len > 0) {
                        for (int j = 0; j < len; j++) {
                            if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                final FetchResponse fr = (FetchResponse) r[j];
                                final UID uidItem = getItemOf(UID.class, fr, STR_UID);
                                seqNumMap.put(uidItem.uid, fr.getNumber());
                                r[j] = null;
                            }
                        }
                        notifyResponseHandlers(r, p);
                    } else if (response.isBAD()) {
                        if (ImapUtility.isInvalidMessageset(response)) {
                            return new int[0];
                        }
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        p.handleResult(response);
                    }
                }
                LOG.debug("{}: IMAP resolve fetch >>>UID FETCH ... (UID)<<< for {} messages took {}msec", imapFolder.getFullName(), Integer.valueOf(length), Long.valueOf(System.currentTimeMillis() - start));
                final int[] retval = new int[length];
                for (int i = 0; i < retval.length; i++) {
                    final int seqNum = seqNumMap.get(uids[i]);
                    retval[i] = 0 == seqNum ? -1 : seqNum;
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
    public static TLongIntMap uids2SeqNumsMap(final IMAPFolder imapFolder, final long[] uids) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return new TLongIntHashMap(0);
        }
        return (TLongIntMap) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final TLongIntHashMap uid2seqNum = new TLongIntHashMap(uids.length);
                final String[] args = messageCount == uids.length ? (1 == messageCount ? new String[] { "1" } : ARGS_ALL) : IMAPNumArgSplitter.splitUIDArg(uids, false, 16); // "UID FETCH <uids> (UID)"
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
                    final String command = String.format(TEMPL_UID_FETCH_UID, args[k]);
                    final Response[] r = performCommand(p, command);
                    final int len = r.length - 1;
                    final Response response = r[len];
                    r[len] = null;
                    if (response.isOK()) {
                        for (int j = 0; j < len; j++) {
                            if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                                final FetchResponse fr = (FetchResponse) r[j];
                                final UID uidItem = getItemOf(UID.class, fr, STR_UID);
                                uid2seqNum.put(uidItem.uid, fr.getNumber());
                                r[j] = null;
                            }
                        }
                        notifyResponseHandlers(r, p);
                    } else if (response.isBAD()) {
                        if (ImapUtility.isInvalidMessageset(response)) {
                            return new TLongIntHashMap(0);
                        }
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        p.handleResult(response);
                    }
                }
                LOG.debug("{}: IMAP resolve fetch >>>UID FETCH ... (UID)<<< for {} messages took {}msec", imapFolder.getFullName(), uids.length, (System.currentTimeMillis() - start));
                return uid2seqNum;
            }
        }));
    }

    private static final String COMMAND_FETCH_UIDS = "FETCH 1:* (UID)";

    /**
     * Fetches all UIDs from given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @return All UIDs from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static long[] fetchUIDs(final IMAPFolder imapFolder) throws MessagingException {
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return new long[0];
        }
        return (long[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                String command = COMMAND_FETCH_UIDS;
                final Response[] r = performCommand(p, command);
                final int len = r.length - 1;
                final Response response = r[len];
                final TLongList l = new TLongArrayList(len);
                if (response.isOK()) {
                    for (int j = 0; j < len; j++) {
                        if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                            l.add(getItemOf(UID.class, (FetchResponse) r[j], STR_UID).uid);
                            r[j] = null;
                        }
                    }
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new long[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(IMAPException.Code.PROTOCOL_ERROR, command, ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return l.toArray();
            }
        }));
    }

    private static final String COMMAND_FETCH_UID_FLAGS = "FETCH 1:* (UID FLAGS)";

    /**
     * Fetches all UIDs from given IMAP folder.
     *
     * @param imapFolder The IMAP folder
     * @return All UIDs from given IMAP folder
     * @throws MessagingException If an error occurs in underlying protocol
     */
    public static IMAPUpdateableData[] fetchUIDAndFlags(final IMAPFolder imapFolder) throws MessagingException {
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return new IMAPUpdateableData[0];
        }
        return (IMAPUpdateableData[]) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
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
                String command = COMMAND_FETCH_UID_FLAGS;
                final Response[] r = performCommand(p, command);
                final int len = r.length - 1;
                final Response response = r[len];
                final List<IMAPUpdateableData> l = new ArrayList<IMAPUpdateableData>(len);
                if (response.isOK()) {
                    for (int j = 0; j < len; j++) {
                        if (STR_FETCH.equals(((IMAPResponse) r[j]).getKey())) {
                            final FetchResponse fr = (FetchResponse) r[j];
                            final FLAGS flags = getItemOf(FLAGS.class, fr, "FLAGS");
                            l.add(IMAPUpdateableData.newInstance(
                                getItemOf(UID.class, fr, STR_UID).uid,
                                parseSystemFlags(flags),
                                parseUserFlags(flags)));
                            r[j] = null;
                        }
                    }
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new IMAPUpdateableData[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return l.toArray(new IMAPUpdateableData[l.size()]);
            }
        }));
    }

    /**
     * Parses specified {@link Flags flags} to system flags.
     *
     * @param flags The flags to parse
     * @return The parsed system flags
     */
    protected static int parseSystemFlags(final Flags flags) {
        int retval = 0;
        if (flags.contains(Flags.Flag.ANSWERED)) {
            retval |= MailMessage.FLAG_ANSWERED;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            retval |= MailMessage.FLAG_DELETED;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            retval |= MailMessage.FLAG_DRAFT;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            retval |= MailMessage.FLAG_FLAGGED;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            retval |= MailMessage.FLAG_RECENT;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            retval |= MailMessage.FLAG_SEEN;
        }
        if (flags.contains(Flags.Flag.USER)) {
            retval |= MailMessage.FLAG_USER;
        }
        return retval;
    }

    /**
     * Parses specified {@link Flags flags} to user flags.
     *
     * @param flags The flags to parse
     * @return The parsed user flags
     */
    protected static Set<String> parseUserFlags(final Flags flags) {
        final String[] userFlags = flags.getUserFlags();
        if (userFlags == null) {
            return java.util.Collections.emptySet();
        }
        /*
         * Mark message to contain user flags
         */
        return new HashSet<String>(Arrays.asList(userFlags));
    }

    /**
     * Gets the data of the part associated with given section identifier.
     *
     * @param imapFolder The IMAP folder
     * @param uid The UID
     * @param sectionId The section identifier
     * @param peek Whether to peek or to fetch (\Seen flag set)
     * @return The data or <code>null</code>
     * @throws MessagingException If a messaging error occurs
     */
    public static MailPart getPart(final IMAPFolder imapFolder, final long uid, final String sectionId, final boolean peek) throws MessagingException {
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return null;
        }
        return (MailPart) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                BODYSTRUCTURE bodystructure = null;
                {
                    final Response[] r = p.fetch(uid, "BODYSTRUCTURE");
                    p.notifyResponseHandlers(r);

                    final int mlen = r.length-1;
                    final Response response = r[mlen];
                    if (response.isOK()) {
                        for (int i = 0; null == bodystructure && i < mlen; i++) {
                            bodystructure = getItemOf(BODYSTRUCTURE.class, (FetchResponse) r[i], "BODYSTRUCTURE");
                        }
                    } else if (response.isNO()) {
                        return null;
                    } else {
                        p.handleResult(response);
                        return null;
                    }
                }

                if (null == bodystructure || ignorable(bodystructure).booleanValue()) {
                    return null;
                }

                BodyAndId bid = null;
                try {
                    bid = getBODYSTRUCTURE(sectionId, bodystructure, null, 1, new boolean[1]);
                } catch (final MessagingException e) {
                    final Exception cause = e.getNextException();
                    throw new ProtocolException(e.getMessage(), null == cause ? e : cause);
                }
                if (null == bid) {
                    return null;
                }
                bodystructure = null;

                // Stream or byte-array based?
                boolean streamed = false;
                if (p.isREV1()) {
                    /*-
                     * Would always yield true since hard-coded: properties.put("mail.imap.fetchsize", "65536");
                     *
                    final String property = IMAPSessionProperties.getDefaultSessionProperties().getProperty("mail.imap.fetchsize");
                    if (null != property && Integer.parseInt(property.trim()) > 0) {
                        streamed = true;
                    }
                     *
                     */
                    streamed = true;
                }

                if (streamed) {
                    try {
                        final Message message = imapFolder.getMessageByUID(uid);
                        if (null == message) {
                            return null;
                        }
                        return toMailPart((IMAPMessage) message, sectionId, peek, bid.bodystructure, imapFolder.getFullName(), false);
                    } catch (final Exception e) {
                        // Ignore
                    }
                }

                final ByteArray byteArray;
                if (p.isREV1()) {
                    final BODY b = peek ? p.peekBody(uid, sectionId) : p.fetchBody(uid, sectionId);
                    if (null == b) {
                        return null;
                    }
                    byteArray = b.getByteArray();
                } else {
                    final RFC822DATA rd = p.fetchRFC822(uid, null);
                    if (null == rd) {
                        return null;
                    }
                    byteArray = rd.getByteArray();
                }

                return toMailPart(byteArray, bid.bodystructure, imapFolder.getFullName());
            }

            private Boolean ignorable(BODYSTRUCTURE bodystructure) {
                if (false == bodystructure.isMulti()) {
                    return Boolean.valueOf(isSignedData(bodystructure));
                }

                // Multipart...
                if (isMultipartSigned(bodystructure) || isApplicationSmil(bodystructure)) {
                    return Boolean.TRUE;
                }

                for (BODYSTRUCTURE body : bodystructure.bodies) {
                    Boolean isSigned = ignorable(body);
                    if (null != isSigned) {
                        return isSigned;
                    }
                }

                return Boolean.FALSE;
            }

            private boolean isApplicationSmil(final BODYSTRUCTURE bodystructure) {
                return /*bodystructure.isMulti() &&*/ "related".equals(asciiLowerCase(bodystructure.subtype)) && "application/smil".equals(asciiLowerCase(MimeMessageUtility.decodeEnvelopeHeader(bodystructure.cParams.get("type"))));
            }

            private boolean isSignedData(BODYSTRUCTURE bodystructure) {
                return "application".equals(asciiLowerCase(bodystructure.type)) && "pkcs7-mime".equals(asciiLowerCase(bodystructure.subtype)) && "signed-data".equals(asciiLowerCase(bodystructure.cParams.get("smime-type"))) && "smime.p7m".equals(asciiLowerCase(bodystructure.cParams.get("name")));
            }

            private boolean isMultipartSigned(BODYSTRUCTURE bodystructure) {
                return /*bodystructure.isMulti() &&*/ "signed".equals(asciiLowerCase(bodystructure.subtype)) && "application/pkcs7-signature".equals(asciiLowerCase(bodystructure.cParams.get("protocol")));
            }

        }));
    }

    protected static BodyAndId getBODYSTRUCTURE(final String sectionId, final BODYSTRUCTURE bodystructure, final String prefix, final int partCount, final boolean[] mpDetected) throws MessagingException {
        final String sequenceId = getSequenceId(prefix, partCount);
        boolean candidate = false;
        if (sectionId.equals(sequenceId)) {
            if (!bodystructure.isMulti()) {
                return new BodyAndId(bodystructure, sequenceId);
            }
            candidate = true;
        }
        if (bodystructure.isNested()) {
            /*
             * A message/rfc822
             */
            final BODYSTRUCTURE[] bodies = bodystructure.bodies;
            if (null != bodies) {
                final int count = bodies.length;
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        final BodyAndId bid = getBODYSTRUCTURE(sectionId, bodies[i], sequenceId, i + 1, new boolean[] { false });
                        if (bid != null) {
                            return bid;
                        }
                    }
                }
            }
        } else {
            /*
             * A multipart
             */
            final BODYSTRUCTURE[] bodies = bodystructure.bodies;
            if (null != bodies) {
                final int count = bodies.length;
                if (count > 0) {
                    final String mpId = null == prefix && !mpDetected[0] ? "" : getSequenceId(prefix, partCount);
                    final String mpPrefix;
                    if (mpDetected[0]) {
                        mpPrefix = mpId;
                    } else {
                        mpPrefix = prefix;
                        mpDetected[0] = true;
                    }
                    for (int i = 0; i < count; i++) {
                        final BodyAndId bid = getBODYSTRUCTURE(sectionId, bodies[i], mpPrefix, i + 1, mpDetected);
                        if (bid != null) {
                            return bid;
                        }
                    }
                }
            }
        }
        return candidate ? new BodyAndId(bodystructure, sequenceId) : null;
    }

    /**
     * Gets the data of the part associated with given section identifier.
     *
     * @param imapFolder The IMAP folder
     * @param uid The UID
     * @param contentId The Content-ID value
     * @param peek Whether to peek or to fetch (\Seen flag set)
     * @return The data or <code>null</code>
     * @throws MessagingException If a messaging error occurs
     */
    public static MailPart getPartByContentId(final IMAPFolder imapFolder, final long uid, final String contentId, final boolean peek) throws MessagingException {
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return null;
        }
        return (MailPart) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                BODYSTRUCTURE bodystructure = null;
                {
                    final Response[] r = p.fetch(uid, "BODYSTRUCTURE");
                    p.notifyResponseHandlers(r);

                    final int mlen = r.length-1;
                    final Response response = r[mlen];
                    if (response.isOK()) {
                        for (int i = 0; null == bodystructure && i < mlen; i++) {
                            bodystructure = getItemOf(BODYSTRUCTURE.class, (FetchResponse) r[i], "BODYSTRUCTURE");
                        }
                    } else if (response.isNO()) {
                        return null;
                    } else {
                        p.handleResult(response);
                        return null;
                    }
                }

                BodyAndId bid = null;
                try {
                    bid = getBODYSTRUCTUREByContentId(contentId, bodystructure, null, 1, new boolean[1]);
                } catch (final MessagingException e) {
                    final Exception cause = e.getNextException();
                    throw new ProtocolException(e.getMessage(), null == cause ? e : cause);
                }
                if (null == bid) {
                    return null;
                }
                bodystructure = null;

                // Stream or byte-array based?
                boolean streamed = false;
                if (p.isREV1()) {
                    /*-
                     * Would always yield true since hard-coded: properties.put("mail.imap.fetchsize", "65536");
                     *
                    final String property = IMAPSessionProperties.getDefaultSessionProperties().getProperty("mail.imap.fetchsize");
                    if (null != property && Integer.parseInt(property.trim()) > 0) {
                        streamed = true;
                    }
                     *
                     */
                    streamed = true;
                }

                if (streamed) {
                    try {
                        final Message message = imapFolder.getMessageByUID(uid);
                        if (null == message) {
                            return null;
                        }
                        return toMailPart((IMAPMessage) message, bid.sectionId, peek, bid.bodystructure, imapFolder.getFullName(), false);
                    } catch (final Exception e) {
                        // Ignore
                    }
                }

                final ByteArray byteArray;
                if (p.isREV1()) {
                    final BODY b = peek ? p.peekBody(uid, bid.sectionId) : p.fetchBody(uid, bid.sectionId);
                    if (null == b) {
                        return null;
                    }
                    byteArray = b.getByteArray();
                } else {
                    final RFC822DATA rd = p.fetchRFC822(uid, null);
                    if (null == rd) {
                        return null;
                    }
                    byteArray = rd.getByteArray();
                }

                return toMailPart(byteArray, bid.bodystructure, imapFolder.getFullName());
            }

        }));
    }

    private static final String SUFFIX = "@" + Version.NAME;

    protected static BodyAndId getBODYSTRUCTUREByContentId(final String contentId, final BODYSTRUCTURE bodystructure, final String prefix, final int partCount, final boolean[] mpDetected) throws MessagingException {
        final String sequenceId = getSequenceId(prefix, partCount);
        if (MimeMessageUtility.equalsCID(contentId, bodystructure.id, SUFFIX)) {
            return new BodyAndId(bodystructure, sequenceId);
        }
        if (bodystructure.isNested()) {
            /*
             * A message/rfc822
             */
            final BODYSTRUCTURE[] bodies = bodystructure.bodies;
            if (null != bodies) {
                final int count = bodies.length;
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        final BodyAndId bid = getBODYSTRUCTUREByContentId(contentId, bodies[i], sequenceId, i + 1, new boolean[] { false });
                        if (bid != null) {
                            return bid;
                        }
                    }
                }
            }
        } else {
            /*
             * A multipart
             */
            final BODYSTRUCTURE[] bodies = bodystructure.bodies;
            if (null != bodies) {
                final int count = bodies.length;
                if (count > 0) {
                    final String mpId = null == prefix && !mpDetected[0] ? "" : getSequenceId(prefix, partCount);
                    final String mpPrefix;
                    if (mpDetected[0]) {
                        mpPrefix = mpId;
                    } else {
                        mpPrefix = prefix;
                        mpDetected[0] = true;
                    }
                    for (int i = 0; i < count; i++) {
                        final BodyAndId bid = getBODYSTRUCTUREByContentId(contentId, bodies[i], mpPrefix, i + 1, mpDetected);
                        if (bid != null) {
                            return bid;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static MailPart toMailPart(final IMAPMessage msg, final String sectionId, final boolean peek, final BODYSTRUCTURE bodystructure, final String fullName, final boolean loadContent) throws ProtocolException {
        try {
            final IMAPMailPart ret = new IMAPMailPart(msg, sectionId, peek, bodystructure, fullName, loadContent);
            ret.applyBodyStructure(bodystructure);
            return ret;
        } catch (final IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        } catch (final RuntimeException e) {
            throw new ProtocolException(e.getMessage(), e);
        }
    }

    protected static MailPart toMailPart(final ByteArray byteArray, final BODYSTRUCTURE bodystructure, final String fullName) throws ProtocolException {
        try {
            final IMAPMailPart ret = new IMAPMailPart(byteArray, bodystructure, fullName);
            ret.applyBodyStructure(bodystructure);
            return ret;
        } catch (final IOException e) {
            throw new ProtocolException(e.getMessage(), e);
        } catch (final RuntimeException e) {
            throw new ProtocolException(e.getMessage(), e);
        }
    }

    /**
     * Composes part's sequence ID from given prefix and part's count
     *
     * @param prefix The prefix (may be <code>null</code>)
     * @param partCount The part count
     * @return The sequence ID
     */
    private static String getSequenceId(final String prefix, final int partCount) {
        if (prefix == null) {
            return Integer.toString(partCount);
        }
        return new StringBuilder(prefix).append('.').append(partCount).toString();
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
        if (imapFolder.getMessageCount() <= 0) {
            /*
             * Empty folder...
             */
            return true;
        }
        return ((Boolean) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String[] args = IMAPNumArgSplitter.splitUIDArg(uids, false, 12); // "UID EXPUNGE <uids>"
                Response[] r = null;
                Response response = null;
                Next: for (int i = 0; i < args.length; i++) {
                    final String command = String.format(TEMPL_UID_EXPUNGE, args[i]);
                    r = performCommand(p, command);
                    response = r[r.length - 1];
                    if (response.isOK()) {
                        continue Next;
                    } else if (response.isBAD()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else if (response.isNO()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new CommandFailedException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        p.handleResult(response);
                    }
                }
                return Boolean.TRUE;
            }
        }))).booleanValue();
    }

    // private static final String ATOM_PERMANENTFLAGS = "[PERMANENTFLAGS";

    // static final Pattern PATTERN_USER_FLAGS = Pattern.compile("(?:\\(|\\s)(?:\\\\\\*)(?:\\)|\\s)");

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

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                final String command = new StringBuilder("SELECT ").append(prepareStringArgument(imapFolder.getFullName())).toString();
                final Response[] r = performCommand(p, command);
                final Response response = r[r.length - 1];
                Boolean retval = Boolean.FALSE;
                if (response.isOK()) {
                    final MailboxInfo mi = new MailboxInfo(r);
                    retval = Boolean.valueOf(mi.permanentFlags.contains(Flags.Flag.USER));
                    notifyResponseHandlers(r, p);
                } else if (response.isBAD()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return retval;
            }
        });
        return val.booleanValue();
    }

    private static final String COMMAND_FETCH_OXMARK_RFC = "FETCH 1:* (UID RFC822.HEADER.LINES (" + MessageHeaders.HDR_X_OX_MARKER + "))";

    private static final String COMMAND_FETCH_OXMARK_REV1 =
        "FETCH 1:* (UID BODY.PEEK[HEADER.FIELDS (" + MessageHeaders.HDR_X_OX_MARKER + ")])";

    private static interface HeaderString {

        /**
         * Gets the headers as a {@link String}
         *
         * @param fetchItem The appropriate fetch item representing headers
         * @return The headers as a {@link String} or <code>null</code>
         */
        public String getHeaderString(Item fetchItem);
    }

    private static HeaderString REV1HeaderStream = new HeaderString() {

        @Override
        public String getHeaderString(final Item fetchItem) {
            final ByteArray byteArray = ((BODY) fetchItem).getByteArray();
            if (null == byteArray) {
                return null;
            }
            return Charsets.toAsciiString(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount());
        }
    };

    private static HeaderString RFCHeaderStream = new HeaderString() {

        @Override
        public String getHeaderString(final Item fetchItem) {
            final ByteArray byteArray = ((RFC822DATA) fetchItem).getByteArray();
            if (null == byteArray) {
                return null;
            }
            return Charsets.toAsciiString(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount());
        }
    };

    protected static HeaderString getHeaderStream(final boolean isREV1) {
        if (isREV1) {
            return REV1HeaderStream;
        }
        return RFCHeaderStream;
    }

    /**
     * Searches the message whose {@link MessageHeaders#HDR_X_OX_MARKER} header is set to specified marker.
     *
     * @param marker The marker to lookup
     * @param numOfAppendedMessages The number of appended messages
     * @param imapFolder The IMAP folder in which to search the message
     * @return The matching message's UID or <code>-1</code> if none found
     * @throws MessagingException If marker look-up fails
     */
    public static long[] findMarker(final String marker, final int numOfAppendedMessages, final IMAPFolder imapFolder) throws MessagingException {
        if ((marker == null) || (marker.length() == 0)) {
            return new long[0];
        }
        if (imapFolder.getMessageCount() <= 0) {
            return new long[0];
        }
        return ((long[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                boolean isREV1 = p.isREV1();
                String command = isREV1 ? COMMAND_FETCH_OXMARK_REV1 : COMMAND_FETCH_OXMARK_RFC;
                Response[] r = performCommand(p, command);
                Response response = r[r.length - 1];
                try {
                    if (response.isOK()) {
                        final long[] retval = new long[numOfAppendedMessages];
                        Arrays.fill(retval, -1L);
                        boolean markerFound = false;
                        int numAdded = 0;
                        HeaderString headerStream = null;
                        final HeaderCollection h = new HeaderCollection();
                        final int len = r.length - 1;
                        for (int i = 0; i < len; i++) {
                            if (!(r[i] instanceof FetchResponse)) {
                                continue;
                            }
                            final FetchResponse fetchResponse = (FetchResponse) r[i];
                            if (!markerFound) {
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
                                    final String headerString = headerStream.getHeaderString(headerItem);
                                    if (null != headerString) {
                                        h.load(headerString);
                                    }
                                    curMarker = h.getHeader(MessageHeaders.HDR_X_OX_MARKER, null);
                                }
                                markerFound = (marker.equals(curMarker));
                            }
                            /*
                             * Marker found
                             */
                            if (markerFound) {
                                final UID uidItem = getItemOf(UID.class, fetchResponse, STR_UID);
                                retval[numAdded++] = uidItem.uid;
                                if (numAdded >= numOfAppendedMessages) {
                                    // Break for loop
                                    i = len;
                                }
                            }
                            r[i] = null;
                        }
                        return retval;
                    }

                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                } catch (final Exception e) {
                    throw wrapException(e, null);
                } finally {
                    // p.notifyResponseHandlers(r);
                    p.handleResult(response);
                }
                return new long[0];
            }
        }));
    }

    private static final String COMMAND_FETCH_ENV_UID = "FETCH 1:* (ENVELOPE UID)";

    /**
     * Finds corresponding UIDs of messages whose Message-ID header is contained in given message IDs.
     *
     * @param imapFolder The IMAP folder to search in
     * @param messageIds The message IDs
     * @return The UIDs of matching message or <code>-1</code> if none found
     * @throws MessagingException
     */
    public static long[] messageId2UID(final IMAPFolder imapFolder, final String... messageIds) throws MessagingException {
        if (0 == messageIds.length) {
            return new long[0];
        }
        if (imapFolder.getMessageCount() <= 0) {
            final long[] uids = new long[messageIds.length];
            Arrays.fill(uids, -1);
            return uids;
        }
        return (long[]) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
                String command = COMMAND_FETCH_ENV_UID;
                Response[] r = performCommand(p, command);
                Response response = r[r.length - 1];
                Long retval = Long.valueOf(-1L);
                if (response.isOK()) {
                    final int length = messageIds.length;
                    final TObjectLongMap<String> messageId2Uid = new TObjectLongHashMap<String>(length, 0.5f, -1L);
                    for (int i = 0, len = r.length - 1; i < len; i++) {
                        if (!(r[i] instanceof FetchResponse)) {
                            continue;
                        }
                        final FetchResponse fetchResponse = (FetchResponse) r[i];
                        final String messageId = getItemOf(ENVELOPE.class, fetchResponse, "ENVELOPE").messageId;
                        final long uid = getItemOf(UID.class, fetchResponse, STR_UID).uid;
                        messageId2Uid.putIfAbsent(messageId, uid);
                        r[i] = null;
                    }
                    final long[] uids = new long[length];
                    Arrays.fill(uids, -1);
                    for (int i = 0; i < length; i++) {
                        final long uid = messageId2Uid.get(messageIds[i]);
                        if (uid > 0) {
                            uids[i] = uid;
                        }
                    }
                    notifyResponseHandlers(r, p);
                    return uids;
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new long[0];
                    }
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else if (response.isNO()) {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                } else {
                    LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                    p.handleResult(response);
                }
                return retval;
            }
        });
    }

    private static final Pattern PATTERN_QUOTE_ARG = Pattern.compile("[\\s\\*%\\(\\)\\{\\}\"\\\\]");

    private static final Pattern PATTERN_ESCAPE_ARG = Pattern.compile("[\"\\\\]");

    private final static String REPLPAT_QUOTE = "\"";

    private final static String REPLACEMENT_QUOTE = "\\\\\\\"";

    private final static String REPLPAT_BACKSLASH = "\\\\";

    private final static String REPLACEMENT_BACKSLASH = "\\\\\\\\";

    /**
     * First encodes given full name by using <code>com.sun.mail.imap.protocol.BASE64MailboxEncoder.encode()</code> method. Afterwards
     * encoded string is checked if it needs quoting and escaping of the special characters '"' and '\'.
     *
     * @param fullName The folder full name
     * @return Prepared full name ready for being used in raw IMAP commands
     */
    public static String prepareStringArgument(final String fullName) {
        /*
         * Ensure to have only ASCII characters
         */
        final String lfolder = BASE64MailboxEncoder.encode(fullName);
        /*
         * Determine if quoting (and escaping) has to be done
         */
        final boolean quote = PATTERN_QUOTE_ARG.matcher(lfolder).find() || "NIL".equalsIgnoreCase(lfolder);
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
    protected static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse, final String itemName) throws ProtocolException {
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
    public static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
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
    protected static ProtocolException missingFetchItem(final String itemName) {
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
    protected static ProtocolException wrapException(final Exception e, final String causeMessage) {
        final ProtocolException pe = new ProtocolException(causeMessage == null ? e.getMessage() : causeMessage);
        pe.initCause(e);
        return pe;
    }

    /**
     * Handles specified response(s).
     *
     * @param r The response(s)
     * @param protocol The IMAP protocol
     */
    public static void notifyResponseHandlers(final Response[] r, final IMAPProtocol protocol) {
        final Response[] rs = new Response[1];
        for (int i = 0; i < r.length; i++) {
            final Response response = r[i];
            if (null != response) {
                rs[0] = response;
                try {
                    protocol.notifyResponseHandlers(rs);
                } catch (final java.lang.ArrayIndexOutOfBoundsException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Performs specified command without arguments
     *
     * @param p The IMAP protocol
     * @param command The command
     * @return The responses
     */
    public static Response[] performCommand(final IMAPProtocol p, final String command) {
        return performCommand(p, command, null);
    }

    /**
     * Performs specified command using given arguments
     *
     * @param p The IMAP protocol
     * @param command The command
     * @param args The argument
     * @return The responses
     */
    public static Response[] performCommand(final IMAPProtocol p, final String command, final Argument args) {
        final long start = System.currentTimeMillis();
        final Response[] responses = p.command(command, args);
        final long time = System.currentTimeMillis() - start;
        mailInterfaceMonitor.addUseTime(time);
        return responses;
    }

    static final class BodyAndId {

        final BODYSTRUCTURE bodystructure;
        final String sectionId;

        BodyAndId(final BODYSTRUCTURE bodystructure, final String sectionId) {
            super();
            this.bodystructure = bodystructure;
            this.sectionId = sectionId;
        }

    }
}
