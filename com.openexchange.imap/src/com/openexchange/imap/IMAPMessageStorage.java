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

package com.openexchange.imap;

import static com.openexchange.imap.threader.Threadables.applyThreaderTo;
import static com.openexchange.imap.threader.Threadables.getThreadableFor;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.fold;
import static com.openexchange.mail.mime.utils.MimeStorageUtility.getFetchProfile;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.imap.OperationKey.Type;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.AbstractIMAPCommand;
import com.openexchange.imap.command.BodyFetchIMAPCommand;
import com.openexchange.imap.command.BodystructureFetchIMAPCommand;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.command.MailMessageFetchIMAPCommand;
import com.openexchange.imap.command.MessageFetchIMAPCommand;
import com.openexchange.imap.command.MessageFetchIMAPCommand.FetchProfileModifier;
import com.openexchange.imap.command.MoveIMAPCommand;
import com.openexchange.imap.command.SimpleFetchIMAPCommand;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.threader.Threadable;
import com.openexchange.imap.threader.ThreadableMapping;
import com.openexchange.imap.threader.Threadables;
import com.openexchange.imap.threader.Threadables.ThreadableResult;
import com.openexchange.imap.threader.references.Conversation;
import com.openexchange.imap.threader.references.Conversations;
import com.openexchange.imap.threadsort.MessageInfo;
import com.openexchange.imap.threadsort.ThreadSortNode;
import com.openexchange.imap.threadsort.ThreadSortUtil;
import com.openexchange.imap.util.IMAPSessionStorageAccess;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedByteArrayInputStream;
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.ThreadSortMailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.ExtendedMimeMessage;
import com.openexchange.mail.mime.ManagedMimeMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.text.TextFinder;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.AbstractTrackableTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.collections.PropertizedList;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.version.Version;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link IMAPMessageStorage} - The IMAP implementation of message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPMessageStorage extends IMAPFolderWorker implements IMailMessageStorageExt, IMailMessageStorageBatch, ISimplifiedThreadStructure {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IMAPMessageStorage.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final int READ_ONLY = Folder.READ_ONLY;

    private static final int READ_WRITE = Folder.READ_WRITE;

    /*-
     * Flag constants
     */

    /**
     * This message is a draft. This flag is set by clients to indicate that the message is a draft message.
     */
    private static final Flag DRAFT = Flags.Flag.DRAFT;

    /**
     * This message is marked deleted. Clients set this flag to mark a message as deleted. The expunge operation on a folder removes all
     * messages in that folder that are marked for deletion.
     */
    private static final Flag DELETED = Flags.Flag.DELETED;

    /**
     * The Flags object initialized with the \Draft system flag.
     */
    private static final Flags FLAGS_DRAFT = new Flags(DRAFT);

    /**
     * The Flags object initialized with the \Deleted system flag.
     */
    private static final Flags FLAGS_DELETED = new Flags(DELETED);

    /*-
     * String constants
     */

    private static final char[] STR_MSEC = new char[] { 'm','s','e','c' };

    private static final boolean LOOK_UP_INBOX_ONLY = true;

    private static volatile Boolean useImapThreaderIfSupported;
    /** <b>Only</b> applies to: getThreadSortedMessages(...) in ISimplifiedThreadStructure. Default is <code>false</code> */
    static boolean useImapThreaderIfSupported() {
        Boolean b = useImapThreaderIfSupported;
        if (null == b) {
            synchronized (IMAPMessageStorage.class) {
                b = useImapThreaderIfSupported;
                if (null == b) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    b = Boolean.valueOf(null != service && service.getBoolProperty("com.openexchange.imap.useImapThreaderIfSupported", false));
                    useImapThreaderIfSupported = b;
                }
            }
        }
        return b.booleanValue();
    }

    private static volatile Boolean useReferenceOnlyThreader;
    /** <b>Only</b> applies to: getThreadSortedMessages(...) in ISimplifiedThreadStructure. Default is <code>true</code> */
    static boolean useReferenceOnlyThreader() {
        Boolean b = useReferenceOnlyThreader;
        if (null == b) {
            synchronized (IMAPMessageStorage.class) {
                b = useReferenceOnlyThreader;
                if (null == b) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    b = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.imap.useReferenceOnlyThreader", true));
                    useReferenceOnlyThreader = b;
                }
            }
        }
        return b.booleanValue();
    }

    private static volatile Boolean byEnvelope;
    /** <b>Only</b> applies to: getThreadSortedMessages(...) in ISimplifiedThreadStructure. Default is <code>true</code> */
    static boolean byEnvelope() {
        Boolean b = byEnvelope;
        if (null == b) {
            synchronized (IMAPMessageStorage.class) {
                b = byEnvelope;
                if (null == b) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    b = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.imap.useReferenceOnlyThreaderByEnvelope", true));
                    byEnvelope = b;
                }
            }
        }
        return b.booleanValue();
    }

    /*-
     * Members
     */

    private MailAccount mailAccount;
    private Locale locale;
    private IIMAPProperties imapProperties;
    private final IMAPFolderStorage imapFolderStorage;

    /**
     * Initializes a new {@link IMAPMessageStorage}.
     *
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws OXException If initialization fails
     */
    public IMAPMessageStorage(final AccessedIMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws OXException {
        super(imapStore, imapAccess, session);
        imapFolderStorage = imapAccess.getFolderStorage();
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountStorageService storageService = Services.getService(MailAccountStorageService.class);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final RuntimeException e) {
                throw handleRuntimeException(e);
            }
        }
        return mailAccount;
    }

    private Locale getLocale() throws OXException {
        if (locale == null) {
            try {
                if (session instanceof ServerSession) {
                    locale = ((ServerSession) session).getUser().getLocale();
                } else {
                    final UserService userService = Services.getService(UserService.class);
                    locale = userService.getUser(session.getUserId(), ctx).getLocale();
                }
            } catch (final RuntimeException e) {
                throw handleRuntimeException(e);
            }
        }
        return locale;
    }

    private IIMAPProperties getIMAPProperties() {
        if (null == imapProperties) {
            imapProperties = imapConfig.getIMAPProperties();
        }
        return imapProperties;
    }

    @Override
    public void clearCache() throws OXException {
        IMAPFolderWorker.clearCache(imapFolder);
    }

    @Override
    public MailMessage[] getMessages(final String fullName, final String[] mailIds, final MailField[] mailFields, final String[] headerNames) throws OXException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        return getMessagesInternal(fullName, uids2longs(mailIds), mailFields, headerNames);
    }

    private static String extractPlainText(final String content, final String optMimeType) throws OXException {
        final TextXtractService textXtractService = Services.getService(TextXtractService.class);
        return textXtractService.extractFrom(new UnsynchronizedByteArrayInputStream(content.getBytes(Charsets.UTF_8)), optMimeType);
    }

    @Override
    public String[] getPrimaryContentsLong(final String fullName, final long[] mailIds) throws OXException {
        if (!imapConfig.getImapCapabilities().hasIMAP4rev1()) {
            return super.getPrimaryContentsLong(fullName, mailIds);
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            final BODYSTRUCTURE[] bodystructures = new BodystructureFetchIMAPCommand(imapFolder, mailIds).doCommand();
            final String[] retval = new String[mailIds.length];

            for (int i = 0; i < bodystructures.length; i++) {
                final BODYSTRUCTURE bodystructure = bodystructures[i];
                if (null != bodystructure) {
                    try {
                        retval[i] = handleBODYSTRUCTURE(fullName, mailIds[i], bodystructure, null, 1, new boolean[1]);
                    } catch (final Exception e) {
                        if (DEBUG) {
                            LOG.debug("Ignoring failed handling of BODYSTRUCTURE item: " + e.getMessage(), e);
                        }
                        retval[i] = null;
                    }
                }
            }
            return retval;
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static final Whitelist WHITELIST = Whitelist.relaxed();

    // private static final Pattern PATTERN_CRLF = Pattern.compile("(\r?\n)+");

    private String handleBODYSTRUCTURE(final String fullName, final long mailId, final BODYSTRUCTURE bodystructure, final String prefix, final int partCount, final boolean[] mpDetected) throws OXException {
        try {
            final String type = toLowerCase(bodystructure.type);
            if ("text".equals(type)) {
                final String sequenceId = getSequenceId(prefix, partCount);
                String content;
                {
                    final byte[] bytes = new BodyFetchIMAPCommand(imapFolder, mailId, sequenceId, true).doCommand();
                    final ParameterList cParams = bodystructure.cParams;
                    content = readContent(bytes, null == cParams ? null : cParams.get("charset"), bodystructure.encoding);
                }
                final String subtype = toLowerCase(bodystructure.subtype);
                if ("plain".equals(subtype)) {
                    if (UUEncodedMultiPart.isUUEncoded(content)) {
                        final UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
                        if (uuencodedMP.isUUEncoded()) {
                            content = uuencodedMP.getCleanText();
                        }
                    }
                    return content;
                }
                if (subtype.startsWith("htm")) {
                    try {
                        return new Renderer(new Segment(new Source(content), 0, content.length())).setMaxLineLength(9999).setIncludeHyperlinkURLs(
                            false).toString();
                    } catch (final StackOverflowError s) {
                        LOG.warn("StackOverflowError while parsing html mail. Returning null...");
                        return null;
                    }
                    // content = PATTERN_CRLF.matcher(content).replaceAll("");// .replaceAll("(  )+", "");
                }
                try {
                    return extractPlainText(content, new com.openexchange.java.StringAllocator(type).append('/').append(subtype).toString());
                } catch (final OXException e) {
                    if (!subtype.startsWith("htm")) {
                        final com.openexchange.java.StringAllocator sb =
                            new com.openexchange.java.StringAllocator("Failed extracting plain text from \"text/").append(subtype).append("\" part:\n");
                        sb.append(" context=").append(session.getContextId());
                        sb.append(", user=").append(session.getUserId());
                        sb.append(", account=").append(accountId);
                        sb.append(", full-name=").append(fullName);
                        sb.append(", uid=").append(mailId);
                        sb.append(", sequence-id=").append(sequenceId);
                        LOG.warn(sb.toString());
                        throw e;
                    }
                    /*
                     * Retry with sanitized HTML content
                     */
                    return extractPlainText(Jsoup.clean(content, WHITELIST), new com.openexchange.java.StringAllocator(type).append('/').append(subtype).toString());
                }
            }
            if ("multipart".equals(type)) {
                final String mpId = null == prefix && !mpDetected[0] ? "" : getSequenceId(prefix, partCount);
                final String mpPrefix;
                if (mpDetected[0]) {
                    mpPrefix = mpId;
                } else {
                    mpPrefix = prefix;
                    mpDetected[0] = true;
                }
                final BODYSTRUCTURE[] bodies = bodystructure.bodies;
                final String subtype = toLowerCase(bodystructure.subtype);
                final int count = bodies.length;
                if ("alternative".equals(subtype)) {
                    /*
                     * Prefer HTML text over plain text
                     */
                    String text = null;
                    for (int i = 0; i < count; i++) {
                        final BODYSTRUCTURE bp = bodies[i];
                        final String bpType = toLowerCase(bp.type);
                        final String bpSubtype = toLowerCase(bp.subtype);
                        if ("text".equals(bpType) && "plain".equals(bpSubtype)) {
                            if (text == null) {
                                text = handleBODYSTRUCTURE(fullName, mailId, bp, mpPrefix, i + 1, mpDetected);
                            }
                            continue;
                        } else if ("text".equals(bpType) && bpSubtype.startsWith("htm")) {
                            final String s = handleBODYSTRUCTURE(fullName, mailId, bp, mpPrefix, i + 1, mpDetected);
                            if (s != null) {
                                return s;
                            }
                        } else if ("multipart".equals(bpType)) {
                            final String s = handleBODYSTRUCTURE(fullName, mailId, bp, mpPrefix, i + 1, mpDetected);
                            if (s != null) {
                                return s;
                            }
                        }
                    }
                    return text;
                }
                /*
                 * A regular multipart
                 */
                for (int i = 0; i < count; i++) {
                    final String s = handleBODYSTRUCTURE(fullName, mailId, bodies[i], mpPrefix, i + 1, mpDetected);
                    if (s != null) {
                        return s;
                    }
                }
            }
            final String subtype = toLowerCase(bodystructure.subtype);
            if ("application".equals(type) && (subtype.startsWith("application/ms-tnef") || subtype.startsWith("application/vnd.ms-tnef"))) {
                final String sequenceId = getSequenceId(prefix, partCount);
                final byte[] bytes = new BodyFetchIMAPCommand(imapFolder, mailId, sequenceId, true).doCommand();
                return new TextFinder().handleTNEFStream(Streams.newByteArrayInputStream(bytes));
            }
            return null;
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static String readContent(final byte[] bytes, final String charset, final String encoding) throws IOException {
        if (null == encoding) {
            return MessageUtility.readStream(Streams.newByteArrayInputStream(bytes), charset);
        }
        InputStream in;
        try {
            in = MimeUtility.decode(Streams.newByteArrayInputStream(bytes), encoding);
        } catch (final MessagingException e) {
            in = Streams.newByteArrayInputStream(bytes);
        }
        return MessageUtility.readStream(in, charset);
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
            return String.valueOf(partCount);
        }
        return new com.openexchange.java.StringAllocator(prefix).append('.').append(partCount).toString();
    }

    @Override
    public MailMessage[] getMessagesLong(final String fullName, final long[] mailIds, final MailField[] mailFields) throws OXException {
        if ((mailIds == null) || (mailIds.length == 0)) {
            return EMPTY_RETVAL;
        }
        return getMessagesInternal(fullName, mailIds, mailFields, null);
    }

    private MailMessage[] getMessagesInternal(final String fullName, final long[] uids, final MailField[] mailFields, final String[] headerNames) throws OXException {
        final MailFields fieldSet = new MailFields(mailFields);
        /*
         * Check for field FULL
         */
        if (fieldSet.contains(MailField.FULL) || fieldSet.contains(MailField.BODY)) {
            final MailMessage[] mails = new MailMessage[uids.length];
            for (int j = 0; j < mails.length; j++) {
                try {
                    mails[j] = getMessageLong(fullName, uids[j], false);
                } catch (final OXException e) {
                    e.setCategory(Category.CATEGORY_WARNING);
                    imapAccess.addWarnings(Collections.singletonList(e));
                    mails[j] = null;
                } catch (final Exception e) {
                    final OXException oxe = MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                    oxe.setCategory(Category.CATEGORY_WARNING);
                    imapAccess.addWarnings(Collections.singletonList(oxe));
                    mails[j] = null;
                }
            }
            return mails;
        }
        /*
         * Get messages with given fields filled
         */
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            /*
             * Fetch desired messages by given UIDs. Turn UIDs to corresponding sequence numbers to maintain order cause some IMAP servers
             * ignore the order of UIDs provided in a "UID FETCH" command.
             */
            final MailMessage[] messages;
            final MailField[] fields = fieldSet.toArray();
            if (imapConfig.asMap().containsKey("UIDPLUS")) {
                final TLongObjectHashMap<MailMessage> fetchedMsgs =
                    fetchValidWithFallbackFor(
                        uids,
                        uids.length,
                        getFetchProfile(fields, headerNames, null, null, getIMAPProperties().isFastFetch()),
                        imapConfig.getImapCapabilities().hasIMAP4rev1(),
                        false);
                /*
                 * Fill array
                 */
                messages = new MailMessage[uids.length];
                for (int i = 0; i < uids.length; i++) {
                    messages[i] = fetchedMsgs.get(uids[i]);
                }
            } else {
                final TLongIntMap seqNumsMap = IMAPCommandsCollection.uids2SeqNumsMap(imapFolder, uids);
                final TLongObjectMap<MailMessage> fetchedMsgs =
                    fetchValidWithFallbackFor(
                        seqNumsMap.values(),
                        seqNumsMap.size(),
                        getFetchProfile(fields, headerNames, null, null, getIMAPProperties().isFastFetch()),
                        imapConfig.getImapCapabilities().hasIMAP4rev1(),
                        true);
                /*
                 * Fill array
                 */
                messages = new MailMessage[uids.length];
                for (int i = 0; i < uids.length; i++) {
                    messages[i] = fetchedMsgs.get(seqNumsMap.get(uids[i]));
                }
            }
            /*
             * Check field existence
             */
            MimeMessageConverter.checkFieldExistence(messages, mailFields);
            if (fieldSet.contains(MailField.ACCOUNT_NAME) || fieldSet.contains(MailField.FULL)) {
                return setAccountInfo(messages);
            }
            return messages;
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return new MailMessage[0];
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private TLongObjectHashMap<MailMessage> fetchValidWithFallbackFor(final Object array, final int len, final FetchProfile fetchProfile, final boolean isRev1, final boolean seqnum) throws OXException {
        final String key = new com.openexchange.java.StringAllocator(16).append(accountId).append(".imap.fetch.modifier").toString();
        final FetchProfile fp = fetchProfile;
        int retry = 0;
        while (true) {
            try {
                final FetchProfileModifier modifier = (FetchProfileModifier) session.getParameter(key);
                if (null == modifier) {
                    // session.setParameter(key, FetchIMAPCommand.DEFAULT_PROFILE_MODIFIER);
                    return fetchValidFor(array, len, fp, isRev1, seqnum, false);
                }
                return fetchValidFor(array, len, modifier.modify(fp), isRev1, seqnum, modifier.byContentTypeHeader());
            } catch (final FolderClosedException e) {
                throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
            } catch (final StoreClosedException e) {
                throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
            } catch (final MessagingException e) {
                final Exception nextException = e.getNextException();
                if ((nextException instanceof BadCommandException) || (nextException instanceof CommandFailedException)) {
                    if (DEBUG) {
                        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128).append("Fetch with fetch profile failed: ");
                        for (final Item item : fetchProfile.getItems()) {
                            sb.append(item.getClass().getSimpleName()).append(',');
                        }
                        for (final String name : fetchProfile.getHeaderNames()) {
                            sb.append(name).append(',');
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        LOG.debug(sb.toString(), e);
                    }
                    if (0 == retry) {
                        session.setParameter(key, MessageFetchIMAPCommand.NO_BODYSTRUCTURE_PROFILE_MODIFIER);
                        retry++;
                    } else if (1 == retry) {
                        session.setParameter(key, MessageFetchIMAPCommand.HEADERLESS_PROFILE_MODIFIER);
                        retry++;
                    } else {
                        throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
                    }
                } else {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
                }
            } catch (final ArrayIndexOutOfBoundsException e) {
                /*
                 * May occur while parsing invalid BODYSTRUCTURE response
                 */
                if (DEBUG) {
                    final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128).append("Fetch with fetch profile failed: ");
                    for (final Item item : fetchProfile.getItems()) {
                        sb.append(item.getClass().getSimpleName()).append(',');
                    }
                    for (final String name : fetchProfile.getHeaderNames()) {
                        sb.append(name).append(',');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    LOG.debug(sb.toString(), e);
                }
                if (0 == retry) {
                    session.setParameter(key, MessageFetchIMAPCommand.NO_BODYSTRUCTURE_PROFILE_MODIFIER);
                    retry++;
                } else if (1 == retry) {
                    session.setParameter(key, MessageFetchIMAPCommand.HEADERLESS_PROFILE_MODIFIER);
                    retry++;
                } else {
                    throw handleRuntimeException(e);
                }
            } catch (final RuntimeException e) {
                throw handleRuntimeException(e);
            }
        }
    }

    private TLongObjectHashMap<MailMessage> fetchValidFor(final Object array, final int len, final FetchProfile fetchProfile, final boolean isRev1, final boolean seqnum, final boolean byContentType) throws MessagingException, OXException {
        final TLongObjectHashMap<MailMessage> map = new TLongObjectHashMap<MailMessage>(len);
        // final MailMessage[] tmp = new NewFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, array, fetchProfile, false,
        // false, false).setDetermineAttachmentByHeader(byContentType).doCommand();
        final MailMessageFetchIMAPCommand command;
        if (array instanceof long[]) {
            command =
                new MailMessageFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, (long[]) array, fetchProfile).setDetermineAttachmentByHeader(byContentType);
        } else {
            command =
                new MailMessageFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, (int[]) array, fetchProfile).setDetermineAttachmentByHeader(byContentType);
        }
        final long start = System.currentTimeMillis();
        final MailMessage[] tmp = command.doCommand();
        final long time = System.currentTimeMillis() - start;
        mailInterfaceMonitor.addUseTime(time);
        if (DEBUG) {
            LOG.debug(new com.openexchange.java.StringAllocator(128).append("IMAP fetch for ").append(len).append(" messages took ").append(time).append(STR_MSEC).toString());
        }
        for (final MailMessage mailMessage : tmp) {
            final IDMailMessage idmm = (IDMailMessage) mailMessage;
            if (null != idmm) {
                map.put(seqnum ? idmm.getSeqnum() : idmm.getUid(), idmm);
            }
        }
        return map;
    }

    @Override
    public MailMessage[] getMessagesByMessageID(final String... messageIDs) throws OXException {
        try {
            final int length = messageIDs.length;
            int count = 0;
            final MailMessage[] retval = new MailMessage[length];
            try {
                imapFolder = setAndOpenFolder(imapFolder, "INBOX", READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, "INBOX");
            }
            final long[] uids = IMAPCommandsCollection.messageId2UID(imapFolder, messageIDs);
            for (int i = 0; i < uids.length; i++) {
                final long uid = uids[i];
                if (uid != -1) {
                    retval[i] = new IDMailMessage(String.valueOf(uid), "INBOX");
                    count++;
                }
            }
            if (count == length || LOOK_UP_INBOX_ONLY) {
                return retval;
            }
            /*
             * Look-up other folders
             */
            recursiveMessageIDLookUp((IMAPFolder) imapStore.getDefaultFolder(), messageIDs, retval, count);
            return retval;
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private int recursiveMessageIDLookUp(final IMAPFolder parentFolder, final String[] messageIDs, final MailMessage[] retval, final int countArg) throws OXException, MessagingException {
        int count = countArg;
        final Folder[] folders = parentFolder.list();
        for (int i = 0; count >= 0 && i < folders.length; i++) {
            final String fullName = folders[i].getFullName();
            final IMAPFolder imapFolder = setAndOpenFolder(fullName, READ_ONLY);
            final long[] uids = IMAPCommandsCollection.messageId2UID(imapFolder, messageIDs);
            for (int k = 0; k < uids.length; k++) {
                final long uid = uids[k];
                if (uid != -1) {
                    retval[k] = new IDMailMessage(fullName, Long.toString(uid));
                    count++;
                }
            }
            if (count == messageIDs.length) {
                return -1;
            }
            count = recursiveMessageIDLookUp(imapFolder, messageIDs, retval, count);
        }
        return count;
    }

    @Override
    public MailPart getAttachmentLong(final String fullName, final long msgUID, final String sequenceId) throws OXException {
        if (msgUID < 0 || null == sequenceId) {
            return null;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            /*
             * Try by Content-ID
             */
            try {
                final MailPart part = IMAPCommandsCollection.getPart(imapFolder, msgUID, sequenceId, false);
                if (null != part) {
                    // Appropriate part found -- check for special content
                    final ContentType contentType = part.getContentType();
                    if (!isTNEFMimeType(contentType) && !isUUEncoded(part, contentType)) {
                        return part;
                    }
                }
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(e, Long.valueOf(msgUID), fullName);
                }
                // Ignore
            } catch (final Exception e) {
                // Ignore
            }
            /*
             * Regular look-up
             */
            final MailMessage mail = getMessageLong(fullName, msgUID, false);
            if (null == mail) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(msgUID), fullName);
            }
            final MailPartHandler handler = new MailPartHandler(sequenceId);
            new MailMessageParser().parseMailMessage(mail, handler);
            if (handler.getMailPart() == null) {
                throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, Long.valueOf(msgUID), fullName);
            }
            return handler.getMailPart();
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static boolean isTNEFMimeType(final ContentType contentType) {
        // note that application/ms-tnefx was also observed in the wild
        return contentType != null && (contentType.startsWith("application/ms-tnef") || contentType.startsWith("application/vnd.ms-tnef"));
    }

    private static boolean isUUEncoded(final MailPart part, final ContentType contentType) throws OXException, IOException {
        if (null == part) {
            return false;
        }
        if (!contentType.startsWith("text/plain")) {
            return false;
        }
        return new UUEncodedMultiPart(MimeMessageUtility.readContent(part, contentType)).isUUEncoded();
    }

    @Override
    public MailPart getImageAttachmentLong(final String fullName, final long msgUID, final String contentId) throws OXException {
        if (msgUID < 0 || null == contentId) {
            return null;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            /*
             * Try by Content-ID
             */
            try {
                final MailPart partByContentId = IMAPCommandsCollection.getPartByContentId(imapFolder, msgUID, contentId, false);
                if (null != partByContentId) {
                    return partByContentId;
                }
            } catch (final Exception e) {
                // Ignore
            }
            /*
             * Regular look-up
             */
            final IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
            if (null == msg) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(msgUID), fullName);
            }
            Part p = examinePart(msg, contentId);
            if (null == p) {
                // Retry...
                ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
                msg.writeTo(out);
                final MimeMessage tmp = new MimeMessage(MimeDefaultSession.getDefaultSession(), new UnsynchronizedByteArrayInputStream(out.toByteArray()));
                out = null;
                p = examinePart(tmp, contentId);
                if (null == p) {
                    throw MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND.create(contentId, Long.valueOf(msgUID), fullName);
                }
            }
            return MimeMessageConverter.convertPart(p, false);
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static final String SUFFIX = "@" + Version.NAME;

    private Part examinePart(final Part part, final String contentId) throws OXException {
        try {
            final String ct = toLowerCase(getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_TYPE, part));
            if (ct.startsWith("image/")) {
                final String partContentId = getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_ID, part);
                if (null == partContentId) {
                    /*
                     * Compare with file name
                     */
                    final String realFilename = getRealFilename(part);
                    if (MimeMessageUtility.equalsCID(contentId, realFilename)) {
                        return part;
                    }
                }
                /*
                 * Compare with Content-Id
                 */
                if (MimeMessageUtility.equalsCID(contentId, partContentId, SUFFIX)) {
                    return part;
                }
                /*
                 * Compare with file name
                 */
                final String realFilename = getRealFilename(part);
                if (MimeMessageUtility.equalsCID(contentId, realFilename)) {
                    return part;
                }
            } else if (ct.startsWith("multipart/")) {
                final Multipart m;
                {
                    final Object content = part.getContent();
                    if (content instanceof Multipart) {
                        m = (Multipart) content;
                    } else {
                        m = new MimeMultipart(part.getDataHandler().getDataSource());
                    }
                }
                final int count = m.getCount();
                for (int i = 0; i < count; i++) {
                    final Part p = examinePart(m.getBodyPart(i), contentId);
                    if (null != p) {
                        return p;
                    }
                }
            }
            return null;
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", imapFolder.getFullName()));
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static String getRealFilename(final Part part) throws MessagingException {
        final String fileName = part.getFileName();
        if (fileName != null) {
            return fileName;
        }
        final String hdr = getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_DISPOSITION, part);
        if (hdr == null) {
            return getContentTypeFilename(part);
        }
        try {
            final String retval = new ContentDisposition(hdr).getFilenameParameter();
            if (retval == null) {
                return getContentTypeFilename(part);
            }
            return retval;
        } catch (final OXException e) {
            return getContentTypeFilename(part);
        }
    }

    private static final String PARAM_NAME = "name";

    private static String getContentTypeFilename(final Part part) throws MessagingException {
        final String hdr = getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_TYPE, part);
        if (hdr == null || hdr.length() == 0) {
            return null;
        }
        try {
            return new ContentType(hdr).getParameter(PARAM_NAME);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private static String getFirstHeaderFrom(final String name, final Part part) throws MessagingException {
        if (null == part || null == name) {
            return null;
        }
        final String[] header = part.getHeader(name);
        if (null == header || 0 == header.length) {
            return null;
        }
        return header[0];
    }

    private static final FetchProfile FETCH_PROFILE_ENVELOPE = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
        }
    };

    @Override
    public MailMessage getMessageLong(final String fullName, final long msgUID, final boolean markSeen) throws OXException {
        if (msgUID < 0) {
            return null;
        }
        try {
            final int desiredMode = markSeen ? READ_WRITE : READ_ONLY;
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, desiredMode);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            IMAPMessage msg;
            try {
                final long start = System.currentTimeMillis();
                msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
                imapFolder.fetch(new Message[] {msg}, FETCH_PROFILE_ENVELOPE);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } catch (final java.lang.NullPointerException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                return null;
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                return null;
            } catch (final MessageRemovedException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                return null;
            } catch (final MessagingException e) {
                final Exception cause = e.getNextException();
                if (!(cause instanceof BadCommandException)) {
                    throw e;
                }
                // Hm... Something weird with executed "UID FETCH" command; retry manually...
                final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, new long[] { msgUID });
                if ((null == seqNums) || (0 == seqNums.length)) {
                    LOG.warn("No message with UID '" + msgUID + "' found in folder '" + fullName + '\'', cause);
                    return null;
                }
                final int msgnum = seqNums[0];
                if (msgnum < 1) {
                    /*
                     * message-numbers start at 1
                     */
                    LOG.warn("No message with UID '" + msgUID + "' found in folder '" + fullName + '\'', cause);
                    return null;
                }
                msg = (IMAPMessage) imapFolder.getMessage(msgnum);
            }
            if (msg == null || msg.isExpunged()) {
                // throw new OXException(OXException.Code.MAIL_NOT_FOUND,
                // String.valueOf(msgUID), imapFolder
                // .toString());
                return null;
            }
            msg.setUID(msgUID);
            msg.setPeek(!markSeen);
            final MailMessage mail;
            try {
                mail = MimeMessageConverter.convertMessage(msg, false);
                mail.setFolder(fullName);
                mail.setMailId(Long.toString(msgUID));
                mail.setUnreadMessages(IMAPCommandsCollection.getUnread(imapFolder));
            } catch (final OXException e) {
                if (MimeMailExceptionCode.MESSAGE_REMOVED.equals(e) || MailExceptionCode.MAIL_NOT_FOUND.equals(e) || MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.equals(e)) {
                    /*
                     * Obviously message was removed in the meantime
                     */
                    return null;
                }
                /*
                 * Check for generic messaging error
                 */
                if (MimeMailExceptionCode.MESSAGING_ERROR.equals(e)) {
                    /*-
                     * Detected generic messaging error. This most likely hints to a severe JavaMail problem.
                     *
                     * Perform some debug logs for traceability...
                     */
                    if (DEBUG) {
                        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
                        sb.append("Generic messaging error occurred for mail \"").append(msgUID).append("\" in folder \"");
                        sb.append(fullName).append("\" with login \"").append(imapConfig.getLogin()).append("\" on server \"");
                        sb.append(imapConfig.getServer()).append("\" (user=").append(session.getUserId());
                        sb.append(", context=").append(session.getContextId()).append("): ").append(e.getMessage());
                        LOG.debug(sb.toString(), e);
                    }
                }
                throw e;
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                return null;
            }
            if (!mail.isSeen() && markSeen) {
                mail.setPrevSeen(false);
                if (imapConfig.isSupportsACLs()) {
                    try {
                        if (aclExtension.canKeepSeen(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                            /*
                             * User has \KEEP_SEEN right: Switch \Seen flag
                             */
                            setSeenFlag(fullName, mail, msg);
                        }
                    } catch (final MessagingException e) {
                        imapFolderStorage.removeFromCache(fullName);
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(
                                new com.openexchange.java.StringAllocator("/SEEN flag could not be set on message #").append(mail.getMailId()).append(" in folder ").append(
                                    mail.getFolder()).toString(),
                                e);
                        }
                    }
                } else {
                    setSeenFlag(fullName, mail, msg);
                }
            }
            return setAccountInfo(mail);
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private void setSeenFlag(final String fullName, final MailMessage mail, final IMAPMessage msg) {
        try {
            msg.setFlags(FLAGS_SEEN, true);
            mail.setFlag(MailMessage.FLAG_SEEN, true);
            final int cur = mail.getUnreadMessages();
            mail.setUnreadMessages(cur <= 0 ? 0 : cur - 1);
            imapFolderStorage.decrementUnreadMessageCount(fullName);
        } catch (final Exception e) {
            imapFolderStorage.removeFromCache(fullName);
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    new com.openexchange.java.StringAllocator("/SEEN flag could not be set on message #").append(mail.getMailId()).append(" in folder ").append(
                        mail.getFolder()).toString(),
                    e);
            }
        }
    }

    private static final FetchProfile FETCH_PROFILE_ENVELOPE_UID = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
            add(UIDFolder.FetchProfileItem.UID);
        }
    };

    @Override
    public MailMessage[] searchMessages(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] mailFields) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            } catch (final OXException e) {
                if (IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.equals(e)) {
                    return EMPTY_RETVAL;
                }
                throw e;
            }
            if (imapFolder.getMessageCount() <= 0) {
                return EMPTY_RETVAL;
            }
            final MailFields usedFields = new MailFields();
            // Add desired fields
            usedFields.addAll(mailFields);
            // Add sort field
            final MailSortField effectiveSortField;
            if (null == sortField) {
                effectiveSortField = MailSortField.RECEIVED_DATE;
            } else {
                if (MailSortField.SENT_DATE.equals(sortField)) {
                    final String draftsFullname = imapAccess.getFolderStorage().getDraftsFolder();
                    if (fullName.equals(draftsFullname)) {
                        effectiveSortField = MailSortField.RECEIVED_DATE;
                    } else {
                        effectiveSortField = sortField;
                    }
                } else {
                    effectiveSortField = sortField;
                }
            }
            usedFields.add(MailField.toField(effectiveSortField.getListField()));
            /*
             * Shall a search be performed?
             */
            final int[] filter;
            if (null == searchTerm) {
                // TODO: enable if action=updates shall be performed
                if (!IMAPSessionStorageAccess.hasSessionStorage(accountId, imapFolder, session)) {
                    IMAPSessionStorageAccess.fillSessionStorage(accountId, imapFolder, session);
                }

                final boolean throwInUse = false; // Fake an [INUSE] error
                if (throwInUse) {
                    final CommandFailedException cfe = new CommandFailedException("NO [INUSE] Mailbox already in use");
                    throw new MessagingException(cfe.getMessage(), cfe);
                }

                /*
                 * Check if an all-fetch can be performed to only obtain UIDs of all folder's messages: FETCH 1: (UID)
                 */
                final MailFields mfs = new MailFields(mailFields);
                if (MailSortField.RECEIVED_DATE.equals(effectiveSortField) && onlyLowCostFields(mfs)) {
                    final MailMessage[] mailMessages = performLowCostFetch(fullName, mfs, order, indexRange);
                    imapFolderStorage.updateCacheIfDiffer(fullName, mailMessages.length);
                    return mailMessages;
                }
                /*
                 * Proceed with common handling
                 */
                filter = null;
            } else {
                /*
                 * Preselect message list according to given search pattern
                 */
                filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
                if ((filter == null) || (filter.length == 0)) {
                    return EMPTY_RETVAL;
                }
            }
            int[] sortSeqNums = IMAPSort.sortMessages(imapFolder, filter, effectiveSortField, order, imapConfig);
            if (null != sortSeqNums) {
                /*
                 * Sort was performed on IMAP server
                 */
                if (indexRange != null) {
                    final int fromIndex = indexRange.start;
                    int toIndex = indexRange.end;
                    if (sortSeqNums.length == 0) {
                        return EMPTY_RETVAL;
                    }
                    if ((fromIndex) > sortSeqNums.length) {
                        /*
                         * Return empty iterator if start is out of range
                         */
                        return EMPTY_RETVAL;
                    }
                    /*
                     * Reset end index if out of range
                     */
                    if (toIndex >= sortSeqNums.length) {
                        toIndex = sortSeqNums.length;
                    }
                    final int[] tmp = sortSeqNums;
                    final int retvalLength = toIndex - fromIndex;
                    sortSeqNums = new int[retvalLength];
                    System.arraycopy(tmp, fromIndex, sortSeqNums, 0, retvalLength);
                }
                /*
                 * Fetch (possibly) filtered and sorted sequence numbers
                 */
                final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
                if (body) {
                    final List<MailMessage> list = new ArrayList<MailMessage>(sortSeqNums.length);
                    final Message[] messages = imapFolder.getMessages(sortSeqNums);
                    imapFolder.fetch(messages, FETCH_PROFILE_ENVELOPE_UID);
                    NextMessage: for (final Message msg : messages) {
                        if (msg != null && !msg.isExpunged()) {
                            final IMAPMessage imapMessage = (IMAPMessage) msg;
                            final long msgUID = imapFolder.getUID(msg);
                            imapMessage.setUID(msgUID);
                            imapMessage.setPeek(true);
                            final MailMessage mail;
                            try {
                                mail = MimeMessageConverter.convertMessage(imapMessage, false);
                                mail.setFolder(fullName);
                                mail.setMailId(Long.toString(msgUID));
                                mail.setUnreadMessages(IMAPCommandsCollection.getUnread(imapFolder));
                            } catch (final OXException e) {
                                if (MimeMailExceptionCode.MESSAGE_REMOVED.equals(e) || MailExceptionCode.MAIL_NOT_FOUND.equals(e) || MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.equals(e)) {
                                    /*
                                     * Obviously message was removed in the meantime
                                     */
                                    continue NextMessage;
                                }
                                /*
                                 * Check for generic messaging error
                                 */
                                if (MimeMailExceptionCode.MESSAGING_ERROR.equals(e)) {
                                    /*-
                                     * Detected generic messaging error. This most likely hints to a severe JavaMail problem.
                                     *
                                     * Perform some debug logs for traceability...
                                     */
                                    if (DEBUG) {
                                        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(128);
                                        sb.append("Generic messaging error occurred for mail \"").append(msgUID).append("\" in folder \"");
                                        sb.append(fullName).append("\" with login \"").append(imapConfig.getLogin()).append("\" on server \"");
                                        sb.append(imapConfig.getServer()).append("\" (user=").append(session.getUserId());
                                        sb.append(", context=").append(session.getContextId()).append("): ").append(e.getMessage());
                                        LOG.debug(sb.toString(), e);
                                    }
                                }
                                throw e;
                            } catch (final java.lang.IndexOutOfBoundsException e) {
                                /*
                                 * Obviously message was removed in the meantime
                                 */
                                continue NextMessage;
                            }
                            list.add(mail);
                        }
                    } // for (final Message msg : messages)
                    return setAccountInfo(list.toArray(new MailMessage[0]));
                } // if (body)
                // Body content not requested
                final boolean isRev1 = imapConfig.getImapCapabilities().hasIMAP4rev1();
                final FetchProfile fetchProfile = getFetchProfile(mailFields, getIMAPProperties().isFastFetch());
                final MailMessageFetchIMAPCommand command = new MailMessageFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, sortSeqNums, fetchProfile);

                final long start = System.currentTimeMillis();
                final MailMessage[] tmp = command.doCommand();
                final long time = System.currentTimeMillis() - start;
                mailInterfaceMonitor.addUseTime(time);

                return setAccountInfo(tmp);
            }
            /*
             * Do application sort
             */
            final int size = filter == null ? imapFolder.getMessageCount() : filter.length;
            final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), getIMAPProperties().isFastFetch());
            final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
            final Message[] msgs;
            if (DEBUG) {
                final long start = System.currentTimeMillis();
                if (filter == null) {
                    msgs = new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), fetchProfile, size, body).doCommand();
                } else {
                    msgs = new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), filter, fetchProfile, false, false, body).doCommand();
                }
                final long time = System.currentTimeMillis() - start;
                LOG.debug(new com.openexchange.java.StringAllocator(128).append("IMAP fetch for ").append(size).append(" messages took ").append(time).append("msec").toString());
            } else {
                if (filter == null) {
                    msgs = new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), fetchProfile, size, body).doCommand();
                } else {
                    msgs = new MessageFetchIMAPCommand(imapFolder, imapConfig.getImapCapabilities().hasIMAP4rev1(), filter, fetchProfile, false, false, body).doCommand();
                }
            }
            if ((msgs == null) || (msgs.length == 0)) {
                return new MailMessage[0];
            }
            MailMessage[] mails = convert2Mails(msgs, usedFields.toArray(), body);
            if (usedFields.contains(MailField.ACCOUNT_NAME) || usedFields.contains(MailField.FULL)) {
                setAccountInfo(mails);
            }
            /*
             * Perform sort on temporary list
             */
            {
                final int length = mails.length;
                final List<MailMessage> msgList = new ArrayList<MailMessage>(length);
                for (int i = 0; i < length; i++) {
                    final MailMessage tmp = mails[i];
                    if (null != tmp) {
                        msgList.add(tmp);
                    }
                }
                Collections.sort(msgList, new MailMessageComparator(effectiveSortField, order == OrderDirection.DESC, getLocale()));
                mails = msgList.toArray(new MailMessage[0]);
            }
            /*
             * Get proper sub-array if an index range is specified
             */
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((mails == null) || (msgs.length == 0)) {
                    return EMPTY_RETVAL;
                }
                if ((fromIndex) > mails.length) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= mails.length) {
                    toIndex = mails.length;
                }
                final MailMessage[] tmp = mails;
                final int retvalLength = toIndex - fromIndex;
                mails = new MailMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, mails, 0, retvalLength);
            }

            return mails;
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return new MailMessage[0];
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static final MailMessageComparator COMPARATOR_ASC = new MailMessageComparator(MailSortField.RECEIVED_DATE, false, null);

    private static final MailMessageComparator COMPARATOR_DESC = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(final String fullName, final boolean includeSent, final boolean cache, final IndexRange indexRange, final long max, final MailSortField sortField, final OrderDirection order, final MailField[] mailFields) throws OXException {
        final long timeStamp = DEBUG ? System.currentTimeMillis() : 0L;

        IMAPFolder sentFolder = null;
        try {
            final String sentFullName = imapFolderStorage.getSentFolder();
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            final int messageCount = imapFolder.getMessageCount();
            if (0 >= messageCount || (null != indexRange && (indexRange.end - indexRange.start) < 1)) {
                return Collections.emptyList();
            }
            final int limit = max <= 0 ? -1 : (messageCount <= max ? -1 : (int) max);
            final boolean mergeWithSent = includeSent && !sentFullName.equals(fullName);
            if (mergeWithSent) {
                sentFolder = (IMAPFolder) imapStore.getFolder(sentFullName);
                sentFolder.open(READ_ONLY);
                // addOpenedFolder(sentFolder);
            }
            /*
             * Sort messages by thread reference
             */
            final MailFields usedFields = new MailFields(mailFields);
            usedFields.add(MailField.THREAD_LEVEL);
            usedFields.add(MailField.RECEIVED_DATE);
            usedFields.add(null == sortField ? MailField.RECEIVED_DATE : MailField.toField(sortField.getListField()));
            final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
            boolean merged = false;
            boolean cached = false;
            List<ThreadSortNode> threadList = null;
            if (!body && useImapThreaderIfSupported() && imapConfig.getImapCapabilities().hasThreadReferences()) {
                if (DEBUG) {
                    LOG.debug("\tIMAPMessageStorage.getThreadSortedMessages(): Using IMAP server's THREAD=REFERENCES threader.");
                }
                /*
                 * Parse THREAD response to a list structure and extract sequence numbers
                 */
                final String sortRange = limit <= 0 ? "ALL" : (Integer.toString(messageCount - limit + 1) + ':' + Integer.toString(messageCount));
                final String threadResponse = ThreadSortUtil.getThreadResponse(imapFolder, sortRange);
                threadList = ThreadSortUtil.parseThreadResponse(threadResponse);
                ThreadSortNode.applyFullName(fullName, threadList);
            } else if (useReferenceOnlyThreader()) {
                final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), true);
                final boolean byEnvelope = byEnvelope();
                if (DEBUG) {
                    LOG.debug("\tIMAPMessageStorage.getThreadSortedMessages(): Using built-in by-reference-only threader." + (byEnvelope ? " Preferring ENVELOPE." : ""));
                }
                /*
                 * Do list append
                 */
                Future<List<MailMessage>> messagesFromSentFolder = null;
                if (mergeWithSent) {
                    final IMAPFolder zentFolder = sentFolder;
                    final FetchProfile clonedFetchProfile = cloneFetchProfile(fetchProfile);
                    messagesFromSentFolder = ThreadPools.getThreadPool().submit(new AbstractTask<List<MailMessage>>() {

                        @Override
                        public List<MailMessage> call() throws Exception {
                            return Conversations.messagesFor(zentFolder, limit, clonedFetchProfile, byEnvelope);
                        }
                    });
                }
                // Retrieve from actual folder
                final List<Conversation> conversations = Conversations.conversationsFor(imapFolder, limit, fetchProfile, byEnvelope);
                // Retrieve from sent folder
                if (null != messagesFromSentFolder) {
                    final List<MailMessage> sentMessages = getFrom(messagesFromSentFolder);
                    closeSafe(sentFolder);
                    sentFolder = null;
                    for (final Conversation conversation : conversations) {
                        for (final MailMessage sentMessage : sentMessages) {
                            if (conversation.referencesOrIsReferencedBy(sentMessage)) {
                                conversation.addMessage(sentMessage);
                            }
                        }
                    }
                }
                // Fold it
                Conversations.fold(conversations);
                // Comparator
                final MailMessageComparator threadComparator = COMPARATOR_DESC;
                // Sort
                List<List<MailMessage>> list = new ArrayList<List<MailMessage>>(conversations.size());
                for (final Conversation conversation : conversations) {
                    list.add(conversation.getMessages(threadComparator));
                }
                // Sort root elements
                {
                    final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
                    final Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, order, getLocale());
                    Collections.sort(list, listComparator);
                }
                // Check for index range
                if (null != indexRange) {
                    final int fromIndex = indexRange.start;
                    int toIndex = indexRange.end;
                    final int size = list.size();
                    if ((fromIndex) > size) {
                        // Return empty iterator if start is out of range
                        return Collections.emptyList();
                    }
                    // Reset end index if out of range
                    if (toIndex >= size) {
                        toIndex = size;
                    }
                    list = list.subList(fromIndex, toIndex);
                }
                /*
                 * Apply account identifier
                 */
                setAccountInfo2(list);
                // Return list
                return list;
            } else {
                if (DEBUG) {
                    LOG.debug("\tIMAPMessageStorage.getThreadSortedMessages(): Using built-in JWZ threader (http://www.jwz.org/doc/threading.html).");
                }
                /*
                 * Need to use in-application Threader
                 */
                final boolean logIt = DEBUG;
                final long st = logIt ? System.currentTimeMillis() : 0L;
                if (mergeWithSent) {
                    final Future<ThreadableResult> future;
                    {
                        final IMAPFolder sent = sentFolder;
                        final Props props = LogProperties.optLogProperties(Thread.currentThread());
                        future = ThreadPools.getThreadPool().submit(new AbstractTrackableTask<ThreadableResult>() {

                            @Override
                            public ThreadableResult call() throws Exception {
                                return getThreadableFor(sent, false, cache, limit, accountId, session);
                            }

                            @Override
                            public Props optLogProperties() {
                                return props;
                            }
                        });
                    }
                    final ThreadableResult threadableResult = getThreadableFor(imapFolder, false, cache, limit, accountId, session);
                    final ThreadableResult sentThreadableResult = getFrom(future);
                    Threadable threadable = threadableResult.threadable;
                    Threadables.append(threadable, sentThreadableResult.threadable);
                    // Sort them by thread reference
                    threadable = applyThreaderTo(threadable);
                    threadable = Threadables.filterFullName(sentFullName, threadable);
                    threadList = Threadables.toNodeList(threadable);
                    ThreadSortNode.filterFullName(sentFullName, threadList);
                    cached = threadableResult.cached || sentThreadableResult.cached;
                    // Mark as merged
                    merged = true;
                    if (logIt) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.info("\tIMAPMessageStorage.getThreadSortedMessages(): In-application thread-sort (incl. sent messages) took " + dur + "msec for folder " + fullName);
                    }
                } else {
                    final ThreadableResult threadableResult = getThreadableFor(imapFolder, false, cache, limit, accountId, session);
                    Threadable threadable = threadableResult.threadable;
                    threadable = applyThreaderTo(threadable);
                    threadList = Threadables.toNodeList(threadable);
                    cached = threadableResult.cached;
                    if (logIt) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.info("\tIMAPMessageStorage.getThreadSortedMessages(): In-application thread-sort took " + dur + "msec for folder " + fullName);
                    }
                }
            }
            if (null == threadList) {
                // No threads found
                return Collections.<List<MailMessage>> singletonList(Arrays.asList(getAllMessages(
                    fullName,
                    null,
                    sortField,
                    order,
                    mailFields)));
            }
            /*
             * Fetch messages
             */
            final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), true);
            final boolean descending = OrderDirection.DESC.equals(order);
            if (!body) {
                return threadedMessagesWithoutBody(
                    fullName,
                    indexRange,
                    sortField,
                    order,
                    sentFolder,
                    messageCount,
                    mergeWithSent,
                    merged,
                    cached,
                    threadList,
                    fetchProfile,
                    descending,
                    limit);
            }
            /*-
             * --------------------------------------------------------------------------------------------------------
             * Returned messages shall include body
             *
             * Include body
             */
            final List<MessageInfo> messageIds = ThreadSortUtil.fromThreadResponse(threadList);
            final Message[] msgs;
            if (!mergeWithSent) {
                msgs =
                    new MessageFetchIMAPCommand(
                        imapFolder,
                        imapConfig.getImapCapabilities().hasIMAP4rev1(),
                        MessageInfo.toSeqNums(messageIds),
                        fetchProfile,
                        false,
                        true,
                        body).doCommand();
            } else {
                final Map<String, TIntList> m = new HashMap<String, TIntList>(2);
                final int size = messageIds.size();
                for (final MessageInfo messageId : messageIds) {
                    final String fn = messageId.getFullName();
                    TIntList list = m.get(fn);
                    if (null == list) {
                        list = new TIntArrayList(size);
                        m.put(fn, list);
                    }
                    list.add(messageId.getMessageNumber());
                }
                final Map<MessageInfo, Message> mapping = new HashMap<MessageInfo, Message>(size);
                for (final Entry<String, TIntList> entry : m.entrySet()) {
                    final String fn = entry.getKey();
                    final IMAPFolder f = sentFullName.equals(fn) ? sentFolder : imapFolder;
                    final long start = System.currentTimeMillis();
                    final Message[] messages = f.getMessages(entry.getValue().toArray());
                    f.fetch(messages, fetchProfile);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    for (final Message message : messages) {
                        mapping.put(new MessageInfo(message.getMessageNumber()).setFullName(fn), message);
                    }
                }
                msgs = new Message[size];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = mapping.get(messageIds.get(i));
                }
            }
            /*
             * Apply thread level
             */
            applyThreadLevel(threadList, 0, msgs, 0);
            /*
             * Generate structured list
             */
            final List<ThreadSortMailMessage> structuredList;
            {
                final MailMessage[] mails = setAccountInfo(convert2Mails(msgs, usedFields.toArray(), body));
                structuredList = ThreadSortUtil.toThreadSortStructure(mails);
            }
            /*
             * Sort according to order direction
             */
            List<List<MailMessage>> list;
            if (MailSortField.RECEIVED_DATE.equals(sortField)) {
                list = ThreadSortUtil.toSimplifiedStructure(structuredList, OrderDirection.DESC.equals(order) ? COMPARATOR_DESC : COMPARATOR_ASC);
            } else {
                list = ThreadSortUtil.toSimplifiedStructure(structuredList, COMPARATOR_DESC);
            }
            /*
             * Sort according to order direction
             */
            {
                final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
                final Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, order, getLocale());
                Collections.sort(list, listComparator);
            }
            if (null != indexRange) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                final int size = list.size();
                if ((fromIndex) > size) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return Collections.emptyList();
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= size) {
                    toIndex = size;
                }
                list = list.subList(fromIndex, toIndex);
            }
            return list;
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            closeSafe(sentFolder);
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - timeStamp;
                LOG.debug("\tIMAPMessageStorage.getThreadSortedMessages() for " + fullName + " took " + dur + "msec");
            }
        }
    }

    private Comparator<List<MailMessage>> getListComparator(final MailSortField sortField, final OrderDirection order, final Locale locale) {
        final MailMessageComparator comparator = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), locale);
        final Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

            @Override
            public int compare(final List<MailMessage> o1, final List<MailMessage> o2) {
                int result = comparator.compare(o1.get(0), o2.get(0));
                if ((0 != result) || (MailSortField.RECEIVED_DATE != sortField)) {
                    return result;
                }
                // Zero as comparison result AND primarily sorted by received-date
                final MailMessage msg1 = o1.get(0);
                final MailMessage msg2 = o2.get(0);
                final String inReplyTo1 = msg1.getInReplyTo();
                final String inReplyTo2 = msg2.getInReplyTo();
                if (null == inReplyTo1) {
                    result = null == inReplyTo2 ? 0 : -1;
                } else {
                    result = null == inReplyTo2 ? 1 : 0;
                }
                return 0 == result ? new MailMessageComparator(MailSortField.SENT_DATE, OrderDirection.DESC.equals(order), null).compare(msg1, msg2) : result;
            }
        };
        return listComparator;
    }

    private List<List<MailMessage>> threadedMessagesWithoutBody(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final IMAPFolder sentFolder, final int messageCount, final boolean mergeWithSent, final boolean merged, final boolean cached, final List<ThreadSortNode> threadList, final FetchProfile fetchProfile, final boolean descending, final int limit) throws MessagingException, OXException {
        final boolean logIt = DEBUG;
        final long st = logIt ? System.currentTimeMillis() : 0L;
        Future<ThreadableMapping> submittedTask = null;
        final Map<MessageInfo, MailMessage> mapping;
        if (mergeWithSent && merged) {
            final Map<String, TIntList> m = ThreadSortUtil.extractSeqNumsAsMap(threadList);
            mapping = new HashMap<MessageInfo, MailMessage>(m.size() << 1);
            final String sentFullName = sentFolder.getFullName();
            for (final Entry<String, TIntList> entry : m.entrySet()) {
                final String fn = entry.getKey();
                if (null != fn) {
                    final IMAPFolder f = sentFullName.equals(fn) ? sentFolder : imapFolder;
                    final TLongObjectMap<MailMessage> messages =
                        new SimpleFetchIMAPCommand(
                            f,
                            getSeparator(f),
                            imapConfig.getImapCapabilities().hasIMAP4rev1(),
                            entry.getValue().toArray(),
                            fetchProfile).doCommand();
                    messages.forEachEntry(new TLongObjectProcedure<MailMessage>() {

                        @Override
                        public boolean execute(final long seqNum, final MailMessage m) {
                            mapping.put(new MessageInfo((int) seqNum).setFullName(fn), m);
                            return true;
                        }
                    });
                }
            }
        } else {
            if (mergeWithSent && !merged) {
                submittedTask = getThreadableMapping(sentFolder, limit, fetchProfile, byEnvelope());
            }
            final TIntList seqNums = ThreadSortUtil.extractSeqNumsAsList(threadList);
            final TLongObjectMap<MailMessage> messages =
                new SimpleFetchIMAPCommand(
                    imapFolder,
                    getSeparator(imapFolder),
                    imapConfig.getImapCapabilities().hasIMAP4rev1(),
                    seqNums.toArray(),
                    fetchProfile).doCommand();
            mapping = new HashMap<MessageInfo, MailMessage>(seqNums.size());
            messages.forEachEntry(new TLongObjectProcedure<MailMessage>() {

                @Override
                public boolean execute(final long seqNum, final MailMessage m) {
                    mapping.put(new MessageInfo((int) seqNum).setFullName(fullName), m);
                    return true;
                }
            });
        }
        if (logIt) {
            final long dur = System.currentTimeMillis() - st;
            LOG.debug("\tMessage fetch took " + dur + "msec for folder " + fullName);
        }
        /*
         * Apply account identifier
         */
        setAccountInfo(mapping.values());
        /*
         * Generate structure
         */
        final MailMessageComparator threadComparator = MailSortField.RECEIVED_DATE.equals(sortField) ? OrderDirection.DESC.equals(order) ? COMPARATOR_DESC : COMPARATOR_ASC : COMPARATOR_DESC;
        List<List<MailMessage>> list = ThreadSortUtil.toSimplifiedStructure(ThreadSortUtil.toThreadSortStructure(threadList, mapping), threadComparator);
        /*
         * Sort according to order direction
         */
        {
            final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
            final Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, order, getLocale());
            Collections.sort(list, listComparator);
        }
        /*
         * Check for available mapping indicating that sent folder results have to be merged
         */
        if (null != submittedTask) {
            final ThreadableMapping threadableMapping = getFrom(submittedTask);
            for (final List<MailMessage> thread : list) {
                if (threadableMapping.checkFor(new ArrayList<MailMessage>(thread), thread)) { // Iterate over copy
                    // Re-Sort thread
                    Collections.sort(thread, threadComparator);
                }
            }
        }
        if (null != indexRange) {
            final int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            final int size = list.size();
            if ((fromIndex) > size) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return Collections.emptyList();
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= size) {
                toIndex = size;
            }
            list = list.subList(fromIndex, toIndex);
        }
        return new PropertizedList<List<MailMessage>>(list).setProperty("cached", Boolean.valueOf(cached)).setProperty(
            "more",
            Integer.valueOf(messageCount));
    }

    private static Future<ThreadableMapping> getThreadableMapping(final IMAPFolder sentFolder, final int limit, final FetchProfile fetchProfile, final boolean byEnvelope) {
        Conversations.checkFetchProfile(fetchProfile, byEnvelope);
        // Get ThreadableMapping
        final IMAPFolder sent = sentFolder;
        final Props props = LogProperties.optLogProperties(Thread.currentThread());
        final Task<ThreadableMapping> task = new AbstractTrackableTask<ThreadableMapping>() {

            @Override
            public ThreadableMapping call() throws Exception {
                final List<MailMessage> mails = Threadables.getAllMailsFrom(sent, limit, fetchProfile);
                return new ThreadableMapping(64).initWith(mails);
            }

            @Override
            public Props optLogProperties() {
                return props;
            }

        };
        return ThreadPools.getThreadPool().submit(task, CallerRunsBehavior.<ThreadableMapping> getInstance());
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String fullName, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] mailFields) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return EMPTY_RETVAL;
            }
            /*
             * Shall a search be performed?
             */
            final int[] filter;
            if (null == searchTerm) {
                filter = null;
            } else {
                /*
                 * Preselect message list according to given search pattern
                 */
                filter = IMAPSearch.searchMessages(imapFolder, searchTerm, imapConfig);
                if ((filter == null) || (filter.length == 0)) {
                    return EMPTY_RETVAL;
                }
            }
            final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
            final boolean descending = OrderDirection.DESC.equals(order);
            /*
             * Create threaded structure dependent on THREAD=REFERENCES capability
             */
            final String threadResp;
            if (imapConfig.getImapCapabilities().hasThreadReferences()) {
                /*
                 * Sort messages by thread reference
                 */
                final String sortRange;
                if (null == filter) {
                    /*
                     * Select all messages
                     */
                    sortRange = "ALL";
                } else {
                    /*
                     * Define sequence of valid message numbers: e.g.: 2,34,35,43,51
                     */
                    final com.openexchange.java.StringAllocator tmp = new com.openexchange.java.StringAllocator(filter.length << 2);
                    tmp.append(filter[0]);
                    for (int i = 1; i < filter.length; i++) {
                        tmp.append(',').append(filter[i]);
                    }
                    sortRange = tmp.toString();
                }
                /*
                 * Get THREAD response; e.g: "((1)(2)(3)(4)(5)(6)(7)(8)(9)(10)(11)(12)(13))"
                 */
                threadResp = ThreadSortUtil.getThreadResponse(imapFolder, sortRange);
            } else {
                Threadable threadable = Threadables.getAllThreadablesFrom(imapFolder, -1);
                threadable = applyThreaderTo(threadable);
                threadResp = Threadables.toThreadReferences(threadable, null == filter ? null : new TIntHashSet(filter));
            }
            /*
             * Parse THREAD response to a list structure and extract sequence numbers
             */
            final List<ThreadSortNode> threadList = ThreadSortUtil.parseThreadResponse(threadResp);
            if (null == threadList) {
                // No threads found
                return getAllMessages(fullName, indexRange, sortField, order, mailFields);
            }
            final List<MessageInfo> messageIds = ThreadSortUtil.fromThreadResponse(threadList);
            final TIntObjectMap<MessageInfo> seqNum2MessageId = new TIntObjectHashMap<MessageInfo>(messageIds.size());
            for (final MessageInfo messageId : messageIds) {
                seqNum2MessageId.put(messageId.getMessageNumber(), messageId);
            }
            /*
             * Fetch messages
             */
            final MailFields usedFields = new MailFields();
            // Add desired fields
            usedFields.addAll(mailFields);
            usedFields.add(MailField.THREAD_LEVEL);
            // Add sort field
            usedFields.add(MailField.toField(effectiveSortField.getListField()));
            final FetchProfile fetchProfile = getFetchProfile(usedFields.toArray(), getIMAPProperties().isFastFetch());
            final boolean body = usedFields.contains(MailField.BODY) || usedFields.contains(MailField.FULL);
            if (!body) {
                final Map<MessageInfo, MailMessage> mapping;
                {
                    final TLongObjectMap<MailMessage> messages =
                        new SimpleFetchIMAPCommand(
                            imapFolder,
                            getSeparator(imapFolder),
                            imapConfig.getImapCapabilities().hasIMAP4rev1(),
                            MessageInfo.toSeqNums(messageIds).toArray(),
                            fetchProfile).doCommand();
                    mapping = new HashMap<MessageInfo, MailMessage>(messages.size());
                    messages.forEachEntry(new TLongObjectProcedure<MailMessage>() {

                        @Override
                        public boolean execute(final long seqNum, final MailMessage m) {
                            mapping.put(seqNum2MessageId.get((int) seqNum), m);
                            return true;
                        }
                    });
                }
                final List<ThreadSortMailMessage> structuredList = ThreadSortUtil.toThreadSortStructure(threadList, mapping);
                /*
                 * Sort according to order direction
                 */
                Collections.sort(structuredList, new MailMessageComparator(effectiveSortField, descending, getLocale()));
                /*
                 * Output as flat list
                 */
                final List<MailMessage> flatList = new ArrayList<MailMessage>(mapping.size());
                if (usedFields.contains(MailField.ACCOUNT_NAME) || usedFields.contains(MailField.FULL)) {
                    for (final MailMessage mail : flatList) {
                        setAccountInfo(mail);
                    }
                }
                ThreadSortUtil.toFlatList(structuredList, flatList);
                return flatList.toArray(new MailMessage[flatList.size()]);
            }
            /*
             * Include body
             */
            Message[] msgs =
                new MessageFetchIMAPCommand(
                    imapFolder,
                    imapConfig.getImapCapabilities().hasIMAP4rev1(),
                    MessageInfo.toSeqNums(messageIds),
                    fetchProfile,
                    false,
                    true,
                    body).doCommand();
            /*
             * Apply thread level
             */
            applyThreadLevel(threadList, 0, msgs, 0);
            /*
             * ... and return
             */
            if (indexRange != null) {
                final int fromIndex = indexRange.start;
                int toIndex = indexRange.end;
                if ((msgs == null) || (msgs.length == 0)) {
                    return EMPTY_RETVAL;
                }
                if ((fromIndex) > msgs.length) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return EMPTY_RETVAL;
                }
                /*
                 * Reset end index if out of range
                 */
                if (toIndex >= msgs.length) {
                    toIndex = msgs.length;
                }
                final Message[] tmp = msgs;
                final int retvalLength = toIndex - fromIndex;
                msgs = new ExtendedMimeMessage[retvalLength];
                System.arraycopy(tmp, fromIndex, msgs, 0, retvalLength);
            }
            /*
             * Generate structured list
             */
            final List<ThreadSortMailMessage> structuredList;
            {
                final MailMessage[] mails;
                if (usedFields.contains(MailField.ACCOUNT_NAME) || usedFields.contains(MailField.FULL)) {
                    mails = setAccountInfo(convert2Mails(msgs, usedFields.toArray(), body));
                } else {
                    mails = convert2Mails(msgs, usedFields.toArray(), body);
                }
                structuredList = ThreadSortUtil.toThreadSortStructure(mails);
            }
            /*
             * Sort according to order direction
             */
            Collections.sort(structuredList, new MailMessageComparator(effectiveSortField, descending, getLocale()));
            /*
             * Output as flat list
             */
            final List<MailMessage> flatList = new ArrayList<MailMessage>(msgs.length);
            ThreadSortUtil.toFlatList(structuredList, flatList);
            return flatList.toArray(new MailMessage[flatList.size()]);
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] mailFields, final int limit) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            MailMessage[] mails;
            {
                /*
                 * Ensure mail ID is contained in requested fields
                 */
                final MailFields fieldSet = new MailFields(mailFields);
                final MailField[] fields = fieldSet.toArray();
                /*
                 * Get ( & fetch) new messages
                 */
                final Message[] msgs =
                    IMAPCommandsCollection.getUnreadMessages(imapFolder, fields, sortField, order, getIMAPProperties().isFastFetch(), limit);
                if ((msgs == null) || (msgs.length == 0) || limit == 0) {
                    return EMPTY_RETVAL;
                }
                /*
                 * Sort
                 */
                mails = convert2Mails(msgs, fields);
                if (fieldSet.contains(MailField.ACCOUNT_NAME) || fieldSet.contains(MailField.FULL)) {
                    setAccountInfo(mails);
                }
                final List<MailMessage> msgList = Arrays.asList(mails);
                Collections.sort(msgList, new MailMessageComparator(sortField, order == OrderDirection.DESC, getLocale()));
                mails = msgList.toArray(mails);
            }
            /*
             * Check for limit
             */
            if (limit > 0 && limit < mails.length) {
                final MailMessage[] retval = new MailMessage[limit];
                System.arraycopy(mails, 0, retval, 0, limit);
                mails = retval;
            }
            return mails;
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void deleteMessagesLong(final String fullName, final long[] msgUIDs, final boolean hardDelete) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                        imapConfig,
                        session,
                        imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(
                    imapFolder,
                    true,
                    session,
                    accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            /*
             * Set marker
             */
            final OperationKey opKey = new OperationKey(Type.MSG_DELETE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                imapFolderStorage.removeFromCache(fullName);
                if (hardDelete || usm.isHardDeleteMsgs()) {
                    blockwiseDeletion(msgUIDs, false, null);
                    notifyIMAPFolderModification(fullName);
                    return;
                }
                final String trashFullname = imapAccess.getFolderStorage().getTrashFolder();
                if (null == trashFullname) {
                    // TODO: Bug#8992 -> What to do if trash folder is null
                    if (LOG.isErrorEnabled()) {
                        LOG.error("\n\tDefault trash folder is not set: aborting delete operation");
                    }
                    throw IMAPException.create(IMAPException.Code.MISSING_DEFAULT_FOLDER_NAME, imapConfig, session, "trash");
                }
                final boolean backup = (!isSubfolderOf(fullName, trashFullname, getSeparator(imapFolder)));
                blockwiseDeletion(msgUIDs, backup, backup ? trashFullname : null);
                if (IMAPSessionStorageAccess.isEnabled()) {
                    IMAPSessionStorageAccess.removeDeletedSessionData(msgUIDs, accountId, session, fullName);
                }
                notifyIMAPFolderModification(fullName);
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private void blockwiseDeletion(final long[] msgUIDs, final boolean backup, final String trashFullname) throws OXException, MessagingException {
        if (0 == msgUIDs.length) {
            // Nothing to do on empty ID array
            return;
        }
        final com.openexchange.java.StringAllocator debug = DEBUG ? new com.openexchange.java.StringAllocator(128) : null;
        final long[] remain;
        final int blockSize = getIMAPProperties().getBlockSize();
        if (blockSize > 0 && msgUIDs.length > blockSize) {
            /*
             * Block-wise deletion
             */
            int offset = 0;
            final long[] tmp = new long[blockSize];
            for (int len = msgUIDs.length; len > blockSize; len -= blockSize) {
                System.arraycopy(msgUIDs, offset, tmp, 0, tmp.length);
                offset += blockSize;
                deleteByUIDs(trashFullname, backup, tmp, debug);
            }
            remain = new long[msgUIDs.length - offset];
            System.arraycopy(msgUIDs, offset, remain, 0, remain.length);
        } else {
            remain = msgUIDs;
        }
        deleteByUIDs(trashFullname, backup, remain, debug);
        /*
         * Close folder to force JavaMail-internal message cache update
         */
        imapFolder.close(false);
        resetIMAPFolder();
    }

    private void deleteByUIDs(final String trashFullname, final boolean backup, final long[] uids, final com.openexchange.java.StringAllocator sb) throws OXException, MessagingException {
        if (backup) {
            /*
             * Copy messages to folder "TRASH"
             */
            final boolean supportsMove = imapConfig.asMap().containsKey("MOVE");
            try {
                AbstractIMAPCommand<long[]> command;
                if (supportsMove) {
                    command = new MoveIMAPCommand(imapFolder, uids, trashFullname, false, true);
                } else {
                    command = new CopyIMAPCommand(imapFolder, uids, trashFullname, false, true);
                }
                if (DEBUG) {
                    final long start = System.currentTimeMillis();
                    command.doCommand();
                    final long time = System.currentTimeMillis() - start;
                    sb.reinitTo(0);
                    if (supportsMove) {
                        LOG.debug(sb.append("\"Move\": ").append(uids.length).append(" messages moved to default trash folder \"").append(
                            trashFullname).append("\" in ").append(time).append(STR_MSEC).toString());
                    } else {
                        LOG.debug(sb.append("\"Soft Delete\": ").append(uids.length).append(" messages copied to default trash folder \"").append(
                            trashFullname).append("\" in ").append(time).append(STR_MSEC).toString());
                    }
                } else {
                    command.doCommand();
                }
            } catch (final MessagingException e) {
                final String err = toLowerCase(e.getMessage());
                if (err.indexOf("[nonexistent]") >= 0) {
                    // Obviously message does not/no more exist
                    return;
                }
                if (err.indexOf("quota") >= 0) {
                    /*
                     * We face an Over-Quota-Exception
                     */
                    throw MailExceptionCode.DELETE_FAILED_OVER_QUOTA.create(e, new Object[0]);
                }
                final Exception nestedExc = e.getNextException();
                if (nestedExc != null && toLowerCase(nestedExc.getMessage()).indexOf("quota") >= 0) {
                    /*
                     * We face an Over-Quota-Exception
                     */
                    throw MailExceptionCode.DELETE_FAILED_OVER_QUOTA.create(e, new Object[0]);
                }
                throw IMAPException.create(IMAPException.Code.MOVE_ON_DELETE_FAILED, imapConfig, session, e, new Object[0]);
            }
            if (supportsMove) {
                return;
            }
        }
        /*
         * Mark messages as \DELETED...
         */
        if (DEBUG) {
            final long start = System.currentTimeMillis();
            new FlagsIMAPCommand(imapFolder, uids, FLAGS_DELETED, true, true, false).doCommand();
            final long dur = System.currentTimeMillis() - start;
            sb.reinitTo(0);
            LOG.debug(sb.append(uids.length).append(" messages marked as deleted (through system flag \\DELETED) in ").append(dur).append(
                STR_MSEC).toString());
        } else {
            new FlagsIMAPCommand(imapFolder, uids, FLAGS_DELETED, true, true, false).doCommand();
        }
        /*
         * ... and perform EXPUNGE
         */
        try {
            IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, uids, imapConfig.getImapCapabilities().hasUIDPlus());
        } catch (final FolderClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw IMAPException.create(
                IMAPException.Code.CONNECT_ERROR,
                imapConfig,
                session,
                e,
                imapAccess.getMailConfig().getServer(),
                imapAccess.getMailConfig().getLogin());
        } catch (final StoreClosedException e) {
            /*
             * Not possible to retry since connection is broken
             */
            throw IMAPException.create(
                IMAPException.Code.CONNECT_ERROR,
                imapConfig,
                session,
                e,
                imapAccess.getMailConfig().getServer(),
                imapAccess.getMailConfig().getLogin());
        } catch (final MessagingException e) {
            throw IMAPException.create(
                IMAPException.Code.UID_EXPUNGE_FAILED,
                imapConfig,
                session,
                e,
                Arrays.toString(uids),
                imapFolder.getFullName(),
                e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public long[] copyMessagesLong(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws OXException {
        return copyOrMoveMessages(sourceFolder, destFolder, mailIds, false, fast);
    }

    @Override
    public long[] moveMessagesLong(final String sourceFolder, final String destFolder, final long[] mailIds, final boolean fast) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(destFolder)) {
            throw IMAPException.create(IMAPException.Code.NO_ROOT_MOVE, imapConfig, session, new Object[0]);
        }
        return copyOrMoveMessages(sourceFolder, destFolder, mailIds, true, fast);
    }

    private long[] copyOrMoveMessages(final String sourceFullName, final String destFullName, final long[] mailIds, final boolean move, final boolean fast) throws OXException {
        try {
            if (null == mailIds) {
                throw IMAPException.create(IMAPException.Code.MISSING_PARAMETER, imapConfig, session, "mailIDs");
            } else if ((sourceFullName == null) || (sourceFullName.length() == 0)) {
                throw IMAPException.create(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, imapConfig, session, "source");
            } else if ((destFullName == null) || (destFullName.length() == 0)) {
                throw IMAPException.create(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, imapConfig, session, "target");
            } else if (sourceFullName.equals(destFullName) && move) {
                // Source equals destination, just return the message ids without throwing an exception or doing anything
                return mailIds;
            } else if (0 == mailIds.length) {
                // Nothing to move
                return new long[0];
            }
            imapFolderStorage.clearCache();
            /*
             * Open and check user rights on source folder
             */
            try {
                imapFolder = setAndOpenFolder(imapFolder, sourceFullName, move ? READ_WRITE : READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", sourceFullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, sourceFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                        imapConfig,
                        session,
                        imapFolder.getFullName());
                }
                if (move && imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(
                    imapFolder,
                    true,
                    session,
                    accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            {
                /*
                 * Open and check user rights on destination folder
                 */
                final IMAPFolder destFolder = (IMAPFolder) imapStore.getFolder(destFullName);
                {
                    final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(destFullName, accountId, destFolder, session);
                    if (!STR_INBOX.equals(destFullName) && !listEntry.exists()) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, destFullName);
                    }
                    if (!listEntry.canOpen()) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, destFullName);
                    }
                }
                try {
                    /*
                     * Check if COPY/APPEND is allowed on destination folder
                     */
                    if (imapConfig.isSupportsACLs() && !aclExtension.canInsert(RightsCache.getCachedRights(
                        destFolder,
                        true,
                        session,
                        accountId))) {
                        throw IMAPException.create(IMAPException.Code.NO_INSERT_ACCESS, imapConfig, session, destFolder.getFullName());
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, destFolder.getFullName());
                }
            }
            /*
             * Set marker
             */
            final OperationKey opKey = new OperationKey(Type.MSG_COPY, accountId, new Object[] { sourceFullName, destFullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Copy operation
                 */
                final long[] result = new long[mailIds.length];
                final int blockSize = getIMAPProperties().getBlockSize();
                final StringBuilder debug = DEBUG ? new StringBuilder(128) : null;
                int offset = 0;
                final long[] remain;
                if (blockSize > 0 && mailIds.length > blockSize) {
                    /*
                     * Block-wise deletion
                     */
                    final long[] tmp = new long[blockSize];
                    for (int len = mailIds.length; len > blockSize; len -= blockSize) {
                        System.arraycopy(mailIds, offset, tmp, 0, tmp.length);
                        final long[] uids = copyOrMoveByUID(move, fast, destFullName, tmp, debug);
                        /*
                         * Append UIDs
                         */
                        System.arraycopy(uids, 0, result, offset, uids.length);
                        offset += blockSize;
                    }
                    remain = new long[mailIds.length - offset];
                    System.arraycopy(mailIds, offset, remain, 0, remain.length);
                } else {
                    remain = mailIds;
                }
                final long[] uids = copyOrMoveByUID(move, fast, destFullName, remain, debug);
                System.arraycopy(uids, 0, result, offset, uids.length);
                if (move) {
                    /*
                     * Force folder cache update through a close
                     */
                    imapFolder.close(false);
                    resetIMAPFolder();
                }
                final String draftFullname = imapAccess.getFolderStorage().getDraftsFolder();
                if (destFullName.equals(draftFullname)) {
                    /*
                     * A copy/move to drafts folder. Ensure to set \Draft flag.
                     */
                    final IMAPFolder destFolder = setAndOpenFolder(destFullName, READ_WRITE);
                    try {
                        if (destFolder.getMessageCount() > 0) {
                            if (DEBUG) {
                                final long start = System.currentTimeMillis();
                                new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, true, true).doCommand();
                                final long time = System.currentTimeMillis() - start;
                                LOG.debug(new com.openexchange.java.StringAllocator(128).append("A copy/move to default drafts folder => All messages' \\Draft flag in ").append(
                                    destFullName).append(" set in ").append(time).append(STR_MSEC).toString());
                            } else {
                                new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, true, true).doCommand();
                            }
                        }
                    } finally {
                        destFolder.close(false);
                    }
                } else if (sourceFullName.equals(draftFullname)) {
                    /*
                     * A copy/move from drafts folder. Ensure to unset \Draft flag.
                     */
                    final IMAPFolder destFolder = setAndOpenFolder(destFullName, READ_WRITE);
                    try {
                        if (DEBUG) {
                            final long start = System.currentTimeMillis();
                            new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, false, true).doCommand();
                            final long time = System.currentTimeMillis() - start;
                            LOG.debug(new com.openexchange.java.StringAllocator(128).append("A copy/move from default drafts folder => All messages' \\Draft flag in ").append(
                                destFullName).append(" unset in ").append(time).append(STR_MSEC).toString());
                        } else {
                            new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, false, true).doCommand();
                        }
                    } finally {
                        destFolder.close(false);
                    }
                }
                if (move && IMAPSessionStorageAccess.isEnabled()) {
                    IMAPSessionStorageAccess.removeDeletedSessionData(mailIds, accountId, session, sourceFullName);
                }
                return result;
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", sourceFullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private long[] copyOrMoveByUID(final boolean move, final boolean fast, final String destFullName, final long[] tmp, final StringBuilder sb) throws MessagingException, OXException, IMAPException {
        final boolean supportsMove = move && imapConfig.asMap().containsKey("MOVE");
        final AbstractIMAPCommand<long[]> command;
        if (supportsMove) {
            command = new MoveIMAPCommand(imapFolder, tmp, destFullName, false, fast);
        } else {
            command = new CopyIMAPCommand(imapFolder, tmp, destFullName, false, fast);
        }
        long[] uids;
        if (DEBUG) {
            final long start = System.currentTimeMillis();
            uids = command.doCommand();
            final long time = System.currentTimeMillis() - start;
            sb.setLength(0);
            if (supportsMove) {
                LOG.debug(sb.append(tmp.length).append(" messages moved in ").append(time).append(STR_MSEC).toString());
            } else {
                LOG.debug(sb.append(tmp.length).append(" messages copied in ").append(time).append(STR_MSEC).toString());
            }
        } else {
            uids = command.doCommand();
        }
        if (!fast && ((uids == null) || noUIDsAssigned(uids, tmp.length))) {
            /*
             * Invalid UIDs
             */
            uids = getDestinationUIDs(tmp, destFullName);
        }
        if (supportsMove) {
            return uids;
        }
        if (move) {
            if (DEBUG) {
                final long start = System.currentTimeMillis();
                new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, true, false).doCommand();
                final long time = System.currentTimeMillis() - start;
                sb.setLength(0);
                LOG.debug(sb.append(tmp.length).append(" messages marked as expunged (through system flag \\DELETED) in ").append(time).append(
                    STR_MSEC).toString());
            } else {
                new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, true, false).doCommand();
            }
            try {
                IMAPCommandsCollection.uidExpungeWithFallback(imapFolder, tmp, imapConfig.getImapCapabilities().hasUIDPlus());
            } catch (final FolderClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw IMAPException.create(
                    IMAPException.Code.CONNECT_ERROR,
                    imapConfig,
                    session,
                    e,
                    imapAccess.getMailConfig().getServer(),
                    imapAccess.getMailConfig().getLogin());
            } catch (final StoreClosedException e) {
                /*
                 * Not possible to retry since connection is broken
                 */
                throw IMAPException.create(
                    IMAPException.Code.CONNECT_ERROR,
                    imapConfig,
                    session,
                    e,
                    imapAccess.getMailConfig().getServer(),
                    imapAccess.getMailConfig().getLogin());
            } catch (final MessagingException e) {
                if (e.getNextException() instanceof ProtocolException) {
                    final ProtocolException protocolException = (ProtocolException) e.getNextException();
                    final Response response = protocolException.getResponse();
                    if (response != null && response.isBYE()) {
                        /*
                         * The BYE response is always untagged, and indicates that the server is about to close the connection.
                         */
                        throw IMAPException.create(
                            IMAPException.Code.CONNECT_ERROR,
                            imapConfig,
                            session,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    }
                    final Throwable cause = protocolException.getCause();
                    if (cause instanceof StoreClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw IMAPException.create(
                            IMAPException.Code.CONNECT_ERROR,
                            imapConfig,
                            session,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    } else if (cause instanceof FolderClosedException) {
                        /*
                         * Connection is down. No retry.
                         */
                        throw IMAPException.create(
                            IMAPException.Code.CONNECT_ERROR,
                            imapConfig,
                            session,
                            e,
                            imapAccess.getMailConfig().getServer(),
                            imapAccess.getMailConfig().getLogin());
                    }
                }
                throw IMAPException.create(
                    IMAPException.Code.UID_EXPUNGE_FAILED,
                    imapConfig,
                    session,
                    e,
                    Arrays.toString(tmp),
                    imapFolder.getFullName(),
                    e.getMessage());
            }
        }
        return uids;
    }

    @Override
    public long[] appendMessagesLong(final String destFullName, final MailMessage[] mailMessages) throws OXException {
        if (null == mailMessages) {
            return new long[0];
        }
        final int length = mailMessages.length;
        if (length == 0) {
            return new long[0];
        }
        Message[] msgs = null;
        try {
            /*
             * Open and check user rights on source folder
             */
            try {
                imapFolder = setAndOpenFolder(imapFolder, destFullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", destFullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, destFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                        imapConfig,
                        session,
                        imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canInsert(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_INSERT_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            final OperationKey opKey = new OperationKey(Type.MSG_APPEND, accountId, new Object[] { destFullName });
            final boolean marked = setMarker(opKey);
            try {
                imapFolderStorage.removeFromCache(destFullName);
                /*
                 * Drop special "x-original-headers" header
                 */
                for (final MailMessage mail : filterNullElements(mailMessages)) {
                    mail.removeHeader("x-original-headers");
                }
                /*
                 * Convert messages to JavaMail message objects
                 */
                msgs = new Message[length];
                {
                    final MailMessage m = mailMessages[0];
                    if (null != m) {
                        msgs[0] = MimeMessageConverter.convertMailMessage(m, MimeMessageConverter.BEHAVIOR_CLONE);
                    }
                }
                for (int i = 1; i < length; i++) {
                    final MailMessage m = mailMessages[i];
                    if (null != m) {
                        msgs[i] = MimeMessageConverter.convertMailMessage(m, MimeMessageConverter.BEHAVIOR_CLONE | MimeMessageConverter.BEHAVIOR_STREAM2FILE);
                    }
                }
                /*
                 * Check if destination folder supports user flags
                 */
                final boolean supportsUserFlags = UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId);
                if (!supportsUserFlags) {
                    /*
                     * Remove all user flags from messages before appending to folder
                     */
                    for (final Message message : msgs) {
                        if (null != message) {
                            removeUserFlagsFromMessage(message);
                        }
                    }
                }
                /*
                 * Mark first message for later lookup
                 */
                final List<Message> filteredMsgs = filterNullElements(msgs);
                final String hash = randomUUID();
                filteredMsgs.get(0).setHeader(MessageHeaders.HDR_X_OX_MARKER, fold(13, hash));
                /*
                 * ... and append them to folder
                 */
                long[] retval = null;
                final boolean hasUIDPlus = imapConfig.getImapCapabilities().hasUIDPlus();
                try {
                    if (hasUIDPlus) {
                        // Perform append expecting APPENUID response code
                        retval = checkAndConvertAppendUID(imapFolder.appendUIDMessages(filteredMsgs.toArray(new Message[0])));
                    } else {
                        // Perform simple append
                        imapFolder.appendMessages(filteredMsgs.toArray(new Message[0]));
                    }
                } catch (final MessagingException e) {
                    final Exception nextException = e.getNextException();
                    if (nextException instanceof com.sun.mail.iap.CommandFailedException) {
                        throw IMAPException.create(IMAPException.Code.INVALID_MESSAGE, imapConfig, session, e, new Object[0]);
                    }
                    throw e;
                }
                if (null != retval) {
                    /*
                     * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
                     */
                    notifyIMAPFolderModification(destFullName);
                    if (retval.length >= length) {
                        return retval;
                    }
                    final long[] longs = new long[length];
                    Arrays.fill(longs, -1L);
                    for (int i = 0, k = 0; i < length; i++) {
                        final MailMessage m = mailMessages[i];
                        if (null != m) {
                            longs[i] = retval[k++];
                        }
                    }
                    return longs;
                }
                /*-
                 * OK, go the long way:
                 * 1. Find the marker in folder's messages
                 * 2. Get the UIDs from found message's position
                 */
                if (hasUIDPlus && LOG.isWarnEnabled()) {
                    /*
                     * Missing UID information in APPENDUID response
                     */
                    LOG.warn("Missing UID information in APPENDUID response");
                }
                retval = new long[msgs.length];
                final long[] uids = IMAPCommandsCollection.findMarker(hash, retval.length, imapFolder);
                if (uids.length == 0) {
                    Arrays.fill(retval, -1L);
                } else {
                    System.arraycopy(uids, 0, retval, 0, uids.length);
                }
                /*
                 * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
                 */
                notifyIMAPFolderModification(destFullName);
                if (retval.length >= length) {
                    return retval;
                }
                final long[] longs = new long[length];
                Arrays.fill(longs, -1L);
                for (int i = 0, k = 0; i < length; i++) {
                    final MailMessage m = mailMessages[i];
                    if (null != m) {
                        longs[i] = retval[k++];
                    }
                }
                return longs;
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            if (DEBUG) {
                final Exception next = e.getNextException();
                if (next instanceof CommandFailedException) {
                    final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(8192);
                    sb.append("\r\nAPPEND command failed. Printing messages' headers for debugging purpose:\r\n");
                    for (int i = 0; i < mailMessages.length; i++) {
                        final MailMessage mailMessage = mailMessages[i];
                        if (null != mailMessage) {
                            sb.append("----------------------------------------------------\r\n\r\n");
                            sb.append(i + 1).append(". message's header:\r\n");
                            sb.append(mailMessage.getHeaders().toString());
                            sb.append("----------------------------------------------------\r\n\r\n");
                        }
                    }
                    LOG.debug(sb.toString());
                }
            }
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", destFullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            if (null != msgs) {
                for (final Message message : msgs) {
                    if (message instanceof ManagedMimeMessage) {
                        ((ManagedMimeMessage) message).cleanUp();
                    }
                }
            }
        }
    }

    @Override
    public void updateMessageFlagsLong(final String fullName, final long[] msgUIDs, final int flagsArg, final boolean set) throws OXException {
        if (null == msgUIDs || 0 == msgUIDs.length) {
            // Nothing to do
            return;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            final OperationKey opKey = new OperationKey(Type.MSG_FLAGS_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove non user-alterable system flags
                 */
                imapFolderStorage.removeFromCache(fullName);
                int flags = flagsArg;
                flags &= ~MailMessage.FLAG_RECENT;
                flags &= ~MailMessage.FLAG_USER;
                /*
                 * Set new flags...
                 */
                final Rights myRights = imapConfig.isSupportsACLs() ? RightsCache.getCachedRights(imapFolder, true, session, accountId) : null;
                final Flags affectedFlags = new Flags();
                boolean applyFlags = false;
                if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.ANSWERED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_DELETED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(DELETED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(DRAFT);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.FLAGGED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_SEEN) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canKeepSeen(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_KEEP_SEEN_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.SEEN);
                    applyFlags = true;
                }
                /*
                 * Check for forwarded flag (supported through user flags)
                 */
                Boolean supportsUserFlags = null;
                if (((flags & MailMessage.FLAG_FORWARDED) > 0)) {
                    supportsUserFlags = Boolean.valueOf(UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId));
                    if (supportsUserFlags.booleanValue()) {
                        if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                            throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                        }
                        affectedFlags.add(MailMessage.USER_FORWARDED);
                        applyFlags = true;
                    } else if (DEBUG) {
                        LOG.debug(new com.openexchange.java.StringAllocator().append("IMAP server ").append(imapConfig.getImapServerSocketAddress()).append(
                            " does not support user flags. Skipping forwarded flag."));
                    }
                }
                /*
                 * Check for read acknowledgment flag (supported through user flags)
                 */
                if (((flags & MailMessage.FLAG_READ_ACK) > 0)) {
                    if (null == supportsUserFlags) {
                        supportsUserFlags = Boolean.valueOf(UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId));
                    }
                    if (supportsUserFlags.booleanValue()) {
                        if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                            throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                        }
                        affectedFlags.add(MailMessage.USER_READ_ACK);
                        applyFlags = true;
                    } else if (DEBUG) {
                        LOG.debug(new com.openexchange.java.StringAllocator().append("IMAP server ").append(imapConfig.getImapServerSocketAddress()).append(
                            " does not support user flags. Skipping read-ack flag."));
                    }
                }
                if (applyFlags) {
                    if (DEBUG) {
                        final long start = System.currentTimeMillis();
                        new FlagsIMAPCommand(imapFolder, msgUIDs, affectedFlags, set, true, false).doCommand();
                        final long time = System.currentTimeMillis() - start;
                        LOG.debug(new StringBuilder(128).append("Flags applied to ").append(msgUIDs.length).append(" messages in ").append(time).append(STR_MSEC).toString());
                    } else {
                        new FlagsIMAPCommand(imapFolder, msgUIDs, affectedFlags, set, true, false).doCommand();
                    }
                }
                /*
                 * Check for spam action
                 */
                if (usm.isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
                    handleSpamByUID(msgUIDs, set, true, fullName, READ_WRITE);
                } else {
                    /*
                     * Force JavaMail's cache update through folder closure
                     */
                    imapFolder.close(false);
                    resetIMAPFolder();
                }
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void updateMessageFlags(final String fullName, final int flagsArg, final boolean set) throws OXException {
        if (null == fullName) {
            // Nothing to do
            return;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            final OperationKey opKey = new OperationKey(Type.MSG_FLAGS_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove non user-alterable system flags
                 */
                imapFolderStorage.removeFromCache(fullName);
                int flags = flagsArg;
                flags &= ~MailMessage.FLAG_RECENT;
                flags &= ~MailMessage.FLAG_USER;
                /*
                 * Set new flags...
                 */
                final Rights myRights = imapConfig.isSupportsACLs() ? RightsCache.getCachedRights(imapFolder, true, session, accountId) : null;
                final Flags affectedFlags = new Flags();
                boolean applyFlags = false;
                if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.ANSWERED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_DELETED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(DELETED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(DRAFT);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.FLAGGED);
                    applyFlags = true;
                }
                if (((flags & MailMessage.FLAG_SEEN) > 0)) {
                    if (imapConfig.isSupportsACLs() && !aclExtension.canKeepSeen(myRights)) {
                        throw IMAPException.create(IMAPException.Code.NO_KEEP_SEEN_ACCESS, imapConfig, session, imapFolder.getFullName());
                    }
                    affectedFlags.add(Flags.Flag.SEEN);
                    applyFlags = true;
                }
                /*
                 * Check for forwarded flag (supported through user flags)
                 */
                Boolean supportsUserFlags = null;
                if (((flags & MailMessage.FLAG_FORWARDED) > 0)) {
                    supportsUserFlags = Boolean.valueOf(UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId));
                    if (supportsUserFlags.booleanValue()) {
                        if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                            throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                        }
                        affectedFlags.add(MailMessage.USER_FORWARDED);
                        applyFlags = true;
                    } else if (DEBUG) {
                        LOG.debug(new com.openexchange.java.StringAllocator().append("IMAP server ").append(imapConfig.getImapServerSocketAddress()).append(
                            " does not support user flags. Skipping forwarded flag."));
                    }
                }
                /*
                 * Check for read acknowledgment flag (supported through user flags)
                 */
                if (((flags & MailMessage.FLAG_READ_ACK) > 0)) {
                    if (null == supportsUserFlags) {
                        supportsUserFlags = Boolean.valueOf(UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId));
                    }
                    if (supportsUserFlags.booleanValue()) {
                        if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(myRights)) {
                            throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                        }
                        affectedFlags.add(MailMessage.USER_READ_ACK);
                        applyFlags = true;
                    } else if (DEBUG) {
                        LOG.debug(new com.openexchange.java.StringAllocator().append("IMAP server ").append(imapConfig.getImapServerSocketAddress()).append(
                            " does not support user flags. Skipping read-ack flag."));
                    }
                }
                if (applyFlags) {
                    if (DEBUG) {
                        final long start = System.currentTimeMillis();
                        new FlagsIMAPCommand(imapFolder, affectedFlags, set, true).doCommand();
                        final long time = System.currentTimeMillis() - start;
                        LOG.debug(new com.openexchange.java.StringAllocator(128).append("Flags applied to all messages in ").append(time).append(STR_MSEC).toString());
                    } else {
                        new FlagsIMAPCommand(imapFolder, affectedFlags, set, true).doCommand();
                    }
                }
                /*
                 * Check for spam action
                 */
                if (usm.isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
                    final long[] uids = IMAPCommandsCollection.getUIDs(imapFolder);
                    handleSpamByUID(uids, set, true, fullName, READ_WRITE);
                } else {
                    /*
                     * Force JavaMail's cache update through folder closure
                     */
                    imapFolder.close(false);
                    resetIMAPFolder();
                }
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void updateMessageColorLabelLong(final String fullName, final long[] msgUIDs, final int colorLabel) throws OXException {
        if (null == msgUIDs || 0 == msgUIDs.length) {
            // Nothing to do
            return;
        }
        try {
            if (!MailProperties.getInstance().isUserFlagsEnabled()) {
                /*
                 * User flags are disabled
                 */
                if (DEBUG) {
                    LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
                }
                return;
            }
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                        imapConfig,
                        session,
                        imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            if (!UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId)) {
                LOG.error(new com.openexchange.java.StringAllocator().append("Folder \"").append(imapFolder.getFullName()).append(
                    "\" does not support user-defined flags. Update of color flag ignored."));
                return;
            }
            final OperationKey opKey = new OperationKey(Type.MSG_LABEL_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove all old color label flag(s) and set new color label flag
                 */
                imapFolderStorage.removeFromCache(fullName);
                long start = DEBUG ? System.currentTimeMillis() : 0L;
                IMAPCommandsCollection.clearAllColorLabels(imapFolder, msgUIDs);
                if (DEBUG) {
                    LOG.debug(new com.openexchange.java.StringAllocator(128).append("All color flags cleared from ").append(msgUIDs.length).append(" messages in ").append(
                        (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
                }
                start = DEBUG ? System.currentTimeMillis() : 0L;
                IMAPCommandsCollection.setColorLabel(imapFolder, msgUIDs, MailMessage.getColorLabelStringValue(colorLabel));
                if (DEBUG) {
                    LOG.debug(new com.openexchange.java.StringAllocator(128).append("All color flags set in ").append(msgUIDs.length).append(" messages in ").append(
                        (System.currentTimeMillis() - start)).append(STR_MSEC).toString());
                }
                /*
                 * Force JavaMail's cache update through folder closure
                 */
                imapFolder.close(false);
                resetIMAPFolder();
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void updateMessageColorLabel(final String fullName, final int colorLabel) throws OXException {
        if (null == fullName) {
            // Nothing to do
            return;
        }
        try {
            if (!MailProperties.getInstance().isUserFlagsEnabled()) {
                /*
                 * User flags are disabled
                 */
                if (DEBUG) {
                    LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
                }
                return;
            }
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(
                        IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES,
                        imapConfig,
                        session,
                        imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            if (!UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId)) {
                LOG.error(new com.openexchange.java.StringAllocator().append("Folder \"").append(imapFolder.getFullName()).append(
                    "\" does not support user-defined flags. Update of color flag ignored."));
                return;
            }
            final OperationKey opKey = new OperationKey(Type.MSG_LABEL_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove all old color label flag(s) and set new color label flag
                 */
                imapFolderStorage.removeFromCache(fullName);
                long start = DEBUG ? System.currentTimeMillis() : 0L;
                IMAPCommandsCollection.clearAllColorLabels(imapFolder, null);
                if (DEBUG) {
                    LOG.debug(new com.openexchange.java.StringAllocator(128).append("All color flags cleared from all messages in ").append((System.currentTimeMillis() - start)).append(
                        STR_MSEC).toString());
                }
                start = DEBUG ? System.currentTimeMillis() : 0L;
                IMAPCommandsCollection.setColorLabel(imapFolder, null, MailMessage.getColorLabelStringValue(colorLabel));
                if (DEBUG) {
                    LOG.debug(new com.openexchange.java.StringAllocator(128).append("All color flags set in all messages in ").append((System.currentTimeMillis() - start)).append(
                        STR_MSEC).toString());
                }
                /*
                 * Force JavaMail's cache update through folder closure
                 */
                imapFolder.close(false);
                resetIMAPFolder();
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullName, final ComposedMailMessage composedMail) throws OXException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(imapAccess.getMailSession());
            /*
             * Fill message
             */
            final long uid;
            try {
                final MimeMessageFiller filler = new MimeMessageFiller(session, ctx);
                filler.setAccountId(accountId);
                composedMail.setFiller(filler);
                /*
                 * Set headers
                 */
                filler.setMessageHeaders(composedMail, mimeMessage);
                /*
                 * Set common headers
                 */
                filler.setCommonHeaders(mimeMessage);
                /*
                 * Fill body
                 */
                filler.fillMailBody(composedMail, mimeMessage, ComposeType.NEW);
                mimeMessage.setFlag(DRAFT, true);
                mimeMessage.saveChanges();
                // Remove generated Message-Id for template message
                mimeMessage.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
                /*
                 * Append message to draft folder
                 */
                imapFolderStorage.removeFromCache(draftFullName);
                uid = appendMessagesLong(draftFullName, new MailMessage[] { MimeMessageConverter.convertMessage(mimeMessage, false) })[0];
            } finally {
                composedMail.cleanUp();
            }
            /*
             * Check for draft-edit operation: Delete old version
             */
            final MailPath msgref = composedMail.getMsgref();
            if (msgref != null && draftFullName.equals(msgref.getFolder())) {
                final ComposeType sendType = composedMail.getSendType();
                if (null == sendType || ComposeType.DRAFT_EDIT.equals(sendType)) {
                    if (accountId != msgref.getAccountId()) {
                        LOG.warn(
                            new com.openexchange.java.StringAllocator("Differing account ID in msgref attribute.\nMessage storage account ID: ").append(accountId).append(
                                ".\nmsgref account ID: ").append(msgref.getAccountId()).toString(),
                                new Throwable());
                    }
                    deleteMessagesLong(msgref.getFolder(), new long[] { parseUnsignedLong(msgref.getMailID()) }, true);
                    composedMail.setMsgref(null);
                }
            }
            /*
             * Force folder update
             */
            notifyIMAPFolderModification(draftFullName);
            /*
             * Return draft mail
             */
            return getMessageLong(draftFullName, uid, true);
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", draftFullName));
        } catch (final IOException e) {
            throw IMAPException.create(IMAPException.Code.IO_ERROR, imapConfig, session, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String fullName, final MailField[] fields) throws OXException {
        // TODO: Needs to be thoroughly tested
        return EMPTY_RETVAL;
        // return getChangedMessages(folder, fields, 0);
    }

    @Override
    public MailMessage[] getDeletedMessages(final String fullName, final MailField[] fields) throws OXException {
        // TODO: Needs to be thoroughly tested
        return EMPTY_RETVAL;
        // return getChangedMessages(folder, fields, 1);
    }

    private MailMessage[] getChangedMessages(final String fullName, final MailField[] fields, final int index) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (!holdsMessages()) {
                throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
            }
            final long[] uids = IMAPSessionStorageAccess.getChanges(accountId, imapFolder, session, index + 1)[index];
            return getMessagesLong(fullName, uids, fields);
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * +++++++++++++++++ Helper methods +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private void unsetMarker(final OperationKey key) {
        OperationKey.unsetMarker(key, session);
    }

    private boolean setMarker(final OperationKey key) throws OXException {
        final int result = OperationKey.setMarker(key, session);
        if (result < 0) {
            throw MimeMailExceptionCode.IN_USE_ERROR_EXT.create(
                imapConfig.getServer(),
                imapConfig.getLogin(),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()),
                MimeMailException.appendInfo("Mailbox is currently in use.", imapFolder));
        }
        return result > 0;
    }

    private static final MailFields MAILFIELDS_DEFAULT = new MailFields(MailField.ID, MailField.FOLDER_ID);

    private static boolean assumeIMAPSortIsReliable() {
        return false; // Introduce config parameter?
    }

    /**
     * Performs the FETCH command on currently active IMAP folder on all messages using the 1:* sequence range argument.
     *
     * @param fullName The IMAP folder's full name
     * @param lowCostFields The low-cost fields
     * @param order The order direction (needed to possibly flip the results)
     * @return The fetched mail messages with only ID and folder ID set.
     * @throws MessagingException If a messaging error occurs
     */
    private MailMessage[] performLowCostFetch(final String fullName, final MailFields lowCostFields, final OrderDirection order, final IndexRange indexRange) throws MessagingException {
        /*
         * Perform simple fetch
         */
        MailMessage[] retval = null;
        {
            boolean allFetch = true;
            if (assumeIMAPSortIsReliable() && MAILFIELDS_DEFAULT.equals(lowCostFields)) { // Enable if sure that IMAP sort works reliably
                try {
                    final long start = System.currentTimeMillis();
                    final long[] uids = IMAPSort.allUIDs(imapFolder, OrderDirection.DESC.equals(order), imapConfig);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    if (null != uids) {
                        final int len = uids.length;
                        final List<MailMessage> list = new ArrayList<MailMessage>(len);
                        for (int i = 0; i < len; i++) {
                            final IDMailMessage mail = new IDMailMessage(Long.toString(uids[i]), fullName);
                            list.add(mail);
                        }
                        retval = list.toArray(new MailMessage[list.size()]);
                        allFetch = false;
                    }
                } catch (final MessagingException e) {
                    LOG.warn(
                        new com.openexchange.java.StringAllocator("SORT command on IMAP server \"").append(imapConfig.getServer()).append("\" failed with login ").append(
                            imapConfig.getLogin()).append(" (user=").append(session.getUserId()).append(", context=").append(
                            session.getContextId()).append("): ").append(e.getMessage()),
                        e);
                }
            }
            if (allFetch) {
                lowCostFields.add(MailField.RECEIVED_DATE);
                final AllFetch.LowCostItem[] lowCostItems = getLowCostItems(lowCostFields);
                final long start = DEBUG ? System.currentTimeMillis() : 0L;
                retval = AllFetch.fetchLowCost(imapFolder, lowCostItems, OrderDirection.ASC.equals(order), imapConfig, session);
                if (DEBUG) {
                    LOG.debug(
                        new com.openexchange.java.StringAllocator(128).append(fullName).append(": IMAP all fetch >>>FETCH 1:* (").append(
                            AllFetch.getFetchCommand(lowCostItems)).append(")<<< took ").append((System.currentTimeMillis() - start)).append(
                            STR_MSEC).toString(),
                        new Throwable());
                }
            }
        }
        if (retval == null || retval.length == 0) {
            return EMPTY_RETVAL;
        }
        if (indexRange != null) {
            final int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            if ((fromIndex) > retval.length) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return EMPTY_RETVAL;
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= retval.length) {
                toIndex = retval.length;
            }
            final MailMessage[] tmp = retval;
            final int retvalLength = toIndex - fromIndex;
            retval = new MailMessage[retvalLength];
            System.arraycopy(tmp, fromIndex, retval, 0, retvalLength);
        }
        return retval;
    }

    private static final MailFields FIELDS_ENV = new MailFields(new MailField[] {
        MailField.SENT_DATE, MailField.FROM, MailField.TO, MailField.CC, MailField.BCC, MailField.SUBJECT });

    private static AllFetch.LowCostItem[] getLowCostItems(final MailFields fields) {
        final Set<AllFetch.LowCostItem> l = EnumSet.noneOf(AllFetch.LowCostItem.class);
        if (fields.contains(MailField.RECEIVED_DATE)) {
            l.add(AllFetch.LowCostItem.INTERNALDATE);
        }
        if (fields.contains(MailField.ID)) {
            l.add(AllFetch.LowCostItem.UID);
        }
        if (fields.contains(MailField.FLAGS) || fields.contains(MailField.COLOR_LABEL)) {
            l.add(AllFetch.LowCostItem.FLAGS);
        }
        if (fields.contains(MailField.CONTENT_TYPE)) {
            l.add(AllFetch.LowCostItem.BODYSTRUCTURE);
        }
        if (fields.contains(MailField.SIZE)) {
            l.add(AllFetch.LowCostItem.SIZE);
        }
        if (fields.containsAny(FIELDS_ENV)) {
            l.add(AllFetch.LowCostItem.ENVELOPE);
        }
        return l.toArray(new AllFetch.LowCostItem[l.size()]);
    }

    private static final EnumSet<MailField> LOW_COST = EnumSet.of(
        MailField.ID,
        MailField.FOLDER_ID,
        MailField.RECEIVED_DATE,
        MailField.FLAGS,
        MailField.COLOR_LABEL,
        MailField.SIZE,
        MailField.CONTENT_TYPE,
        MailField.SENT_DATE,
        MailField.FROM,
        MailField.TO,
        MailField.CC,
        MailField.BCC,
        MailField.SUBJECT);

    private static boolean onlyLowCostFields(final MailFields fields) {
        final Set<MailField> set = fields.toSet();
        if (!set.removeAll(LOW_COST)) {
            return false;
        }
        return set.isEmpty();
    }

    private static int applyThreadLevel(final List<ThreadSortNode> threadList, final int level, final Message[] msgs, final int index) {
        if (null == threadList) {
            return index;
        }
        int idx = index;
        final int threadListSize = threadList.size();
        final Iterator<ThreadSortNode> iter = threadList.iterator();
        for (int i = 0; i < threadListSize; i++) {
            final ThreadSortNode currentNode = iter.next();
            ((ExtendedMimeMessage) msgs[idx]).setThreadLevel(level);
            idx++;
            idx = applyThreadLevel(currentNode.getChilds(), level + 1, msgs, idx);
        }
        return idx;
    }

    private static boolean noUIDsAssigned(final long[] arr, final int expectedLen) {
        final long[] tmp = new long[expectedLen];
        Arrays.fill(tmp, -1L);
        return Arrays.equals(arr, tmp);
    }

    /**
     * Determines the corresponding UIDs in destination folder
     *
     * @param msgUIDs The UIDs in source folder
     * @param destFullName The destination folder's full name
     * @return The corresponding UIDs in destination folder
     * @throws MessagingException
     * @throws OXException
     */
    private long[] getDestinationUIDs(final long[] msgUIDs, final String destFullName) throws MessagingException, OXException {
        /*
         * No COPYUID present in response code. Since UIDs are assigned in strictly ascending order in the mailbox (refer to IMAPv4 rfc3501,
         * section 2.3.1.1), we can discover corresponding UIDs by selecting the destination mailbox and detecting the location of messages
         * placed in the destination mailbox by using FETCH and/or SEARCH commands (e.g., for Message-ID or some unique marker placed in the
         * message in an APPEND).
         */
        final long[] retval = new long[msgUIDs.length];
        Arrays.fill(retval, -1L);
        if (!IMAPCommandsCollection.canBeOpened(imapFolder, destFullName, READ_ONLY)) {
            // No look-up possible
            return retval;
        }
        final String messageId;
        {
            int minIndex = 0;
            long minVal = msgUIDs[0];
            for (int i = 1; i < msgUIDs.length; i++) {
                if (msgUIDs[i] < minVal) {
                    minIndex = i;
                    minVal = msgUIDs[i];
                }
            }
            final IMAPMessage imapMessage = (IMAPMessage) (imapFolder.getMessageByUID(msgUIDs[minIndex]));
            if (imapMessage == null) {
                /*
                 * No message found whose UID matches msgUIDs[minIndex]
                 */
                messageId = null;
            } else {
                messageId = imapMessage.getMessageID();
            }
        }
        if (messageId != null) {
            final IMAPFolder destFolder = (IMAPFolder) imapStore.getFolder(destFullName);
            destFolder.open(READ_ONLY);
            try {
                /*
                 * Find this message ID in destination folder
                 */
                long startUID = IMAPCommandsCollection.messageId2UID(destFolder, messageId)[0];
                if (startUID != -1) {
                    for (int i = 0; i < msgUIDs.length; i++) {
                        retval[i] = startUID++;
                    }
                }
            } finally {
                closeSafe(destFolder);
            }
        }
        return retval;
    }

    private void handleSpamByUID(final long[] msgUIDs, final boolean isSpam, final boolean move, final String fullName, final int desiredMode) throws MessagingException, OXException {
        /*
         * Check for spam handling
         */
        if (usm.isSpamEnabled()) {
            final boolean locatedInSpamFolder = imapAccess.getFolderStorage().getSpamFolder().equals(imapFolder.getFullName());
            if (isSpam) {
                if (locatedInSpamFolder) {
                    /*
                     * A message that already has been detected as spam should again be learned as spam: Abort.
                     */
                    return;
                }
                /*
                 * Handle spam
                 */
                {
                    SpamHandlerRegistry.getSpamHandlerBySession(session, accountId, IMAPProvider.getInstance()).handleSpam(
                        accountId,
                        imapFolder.getFullName(),
                        longs2uids(msgUIDs),
                        move,
                        session);
                    /*
                     * Close and reopen to force internal message cache update
                     */
                    resetIMAPFolder();
                    try {
                        imapFolder = setAndOpenFolder(imapFolder, fullName, desiredMode);
                    } catch (final MessagingException e) {
                        final Exception next = e.getNextException();
                        if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                            throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                        }
                        throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
                    }
                }
                return;
            }
            if (!locatedInSpamFolder) {
                /*
                 * A message that already has been detected as ham should again be learned as ham: Abort.
                 */
                return;
            }
            /*
             * Handle ham.
             */
            {
                SpamHandlerRegistry.getSpamHandlerBySession(session, accountId, IMAPProvider.getInstance()).handleHam(
                    accountId,
                    imapFolder.getFullName(),
                    longs2uids(msgUIDs),
                    move,
                    session);
                /*
                 * Close and reopen to force internal message cache update
                 */
                resetIMAPFolder();
                try {
                    imapFolder = setAndOpenFolder(imapFolder, fullName, desiredMode);
                } catch (final MessagingException e) {
                    final Exception next = e.getNextException();
                    if (!(next instanceof com.sun.mail.iap.CommandFailedException) || (toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                        throw IMAPException.handleMessagingException(e, imapConfig, session, imapFolder, accountId, mapFor("fullName", fullName));
                    }
                    throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
                }
            }
        }
    }

    /**
     * Checks and converts specified APPENDUID response.
     *
     * @param appendUIDs The APPENDUID response
     * @return An array of long for each valid {@link AppendUID} element or a zero size array of long if an invalid {@link AppendUID}
     *         element was detected.
     */
    private static long[] checkAndConvertAppendUID(final AppendUID[] appendUIDs) {
        if (appendUIDs == null || appendUIDs.length == 0) {
            return new long[0];
        }
        final long[] retval = new long[appendUIDs.length];
        for (int i = 0; i < appendUIDs.length; i++) {
            if (appendUIDs[i] == null) {
                /*
                 * A null element means the server didn't return UID information for the appended message.
                 */
                return new long[0];
            }
            retval[i] = appendUIDs[i].uid;
        }
        return retval;
    }

    /**
     * Removes all user flags from given message's flags
     *
     * @param message The message whose user flags shall be removed
     * @throws MessagingException If removing user flags fails
     */
    private static void removeUserFlagsFromMessage(final Message message) throws MessagingException {
        final String[] userFlags = message.getFlags().getUserFlags();
        if (userFlags.length > 0) {
            /*
             * Create a new flags container necessary for later removal
             */
            final Flags remove = new Flags();
            for (final String userFlag : userFlags) {
                remove.add(userFlag);
            }
            /*
             * Remove gathered user flags from message's flags; flags which do not occur in flags object are unaffected.
             */
            message.setFlags(remove, false);
        }
    }

    /**
     * Generates a UUID using {@link UUID#randomUUID()}; e.g.:<br>
     * <i>a5aa65cb-6c7e-4089-9ce2-b107d21b9d15</i>
     *
     * @return A UUID string
     */
    private static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Sets account ID and name in given instance of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instance
     * @return The given instance of {@link MailMessage} with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage setAccountInfo(final MailMessage mailMessage) throws OXException {
        if (null == mailMessage) {
            return null;
        }
        final MailAccount account = getMailAccount();
        mailMessage.setAccountId(account.getId());
        mailMessage.setAccountName(account.getName());
        return mailMessage;
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage[] setAccountInfo(final MailMessage[] mailMessages) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            if (null != mailMessage) {
                mailMessage.setAccountId(id);
                mailMessage.setAccountName(name);
            }
        }
        return mailMessages;
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private <C extends Collection<MailMessage>> C setAccountInfo(final C mailMessages) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (final MailMessage mailMessage : mailMessages) {
            if (null != mailMessage) {
                mailMessage.setAccountId(id);
                mailMessage.setAccountName(name);
            }
        }
        return mailMessages;
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(final W col) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (final C mailMessages : col) {
            for (final MailMessage mailMessage : mailMessages) {
                if (null != mailMessage) {
                    mailMessage.setAccountId(id);
                    mailMessage.setAccountName(name);
                }
            }
        }
        return col;
    }

    private MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields) throws OXException {
        return convert2Mails(msgs, fields, null, false);
    }

    private MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields, final boolean includeBody) throws OXException {
        return convert2Mails(msgs, fields, null, includeBody);
    }

    private MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields, final String[] headerNames) throws OXException {
        return convert2Mails(msgs, fields, headerNames, false);
    }

    private MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields, final String[] headerNames, final boolean includeBody) throws OXException {
        return MimeMessageConverter.convertMessages(msgs, fields, headerNames, includeBody);
    }

    private char getSeparator(final IMAPFolder imapFolder) throws OXException, MessagingException {
        return getLISTEntry(STR_INBOX, imapFolder).getSeparator();
    }

    private ListLsubEntry getLISTEntry(final String fullName, final IMAPFolder imapFolder) throws OXException, MessagingException {
        return ListLsubCache.getCachedLISTEntry(fullName, accountId, imapFolder, session);
    }

    private static boolean isSubfolderOf(final String fullName, final String possibleParent, final char separator) {
        if (!fullName.startsWith(possibleParent)) {
            return false;
        }
        final int length = possibleParent.length();
        if (length >= fullName.length()) {
            return true;
        }
        return fullName.charAt(length) == separator;
    }

    private static <T> T getFrom(final Future<T> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // Keep interrupted state
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof MessagingException) {
                throw MimeMailException.handleMessagingException((MessagingException) cause);
            }
            throw ThreadPools.launderThrowable(e, OXException.class);
        }

    }

    private static void closeSafe(final IMAPFolder imapFolder) {
        if (null != imapFolder) {
            try {
                imapFolder.close(false);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /** ASCII-wise upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

    private static Map<String, Object> mapFor(final String... pairs) {
        if (null == pairs) {
            return null;
        }
        final int length = pairs.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }
        final Map<String, Object> map = new HashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(pairs[i], pairs[i+1]);
        }
        return map;
    }

    private static <E> List<E> filterNullElements(final E[] elements) {
        if (null == elements) {
            return Collections.emptyList();
        }
        final int length = elements.length;
        final List<E> list = new ArrayList<E>(length);
        for (int i = 0; i < length; i++) {
            final E elem = elements[i];
            if (null != elem) {
                list.add(elem);
            }
        }
        return list;
    }

    private static FetchProfile cloneFetchProfile(final FetchProfile fetchProfile) {
        if (null == fetchProfile) {
            return null;
        }
        final FetchProfile newFetchProfile = new FetchProfile();
        for (final Item item : fetchProfile.getItems()) {
            newFetchProfile.add(item);
        }
        for (final String headerName : fetchProfile.getHeaderNames()) {
            newFetchProfile.add(headerName);
        }
        return newFetchProfile;
    }

}
