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

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.fold;
import static com.openexchange.mail.mime.utils.MimeStorageUtility.getFetchProfile;
import static com.openexchange.mail.utils.StorageUtility.prepareMailFieldsForSearch;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import org.apache.commons.lang.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
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
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.search.IMAPSearch;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.sort.IMAPSort.ImapSortResult;
import com.openexchange.imap.util.AppendEmptyMessageTracer;
import com.openexchange.imap.util.IMAPSessionStorageAccess;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.imap.util.ImmutableReference;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageBatchCopyMove;
import com.openexchange.mail.api.IMailMessageStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.IMailMessageStorageMimeSupport;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeCleanUp;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.mime.utils.MimeStorageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MailPartHandler;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.text.TextFinder;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mail.uuencode.UUEncodedMultiPart;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.textxtraction.TextXtractService;
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
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.ReadableMime;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

/**
 * {@link IMAPMessageStorage} - The IMAP implementation of message storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPMessageStorage extends IMAPFolderWorker implements IMailMessageStorageExt, IMailMessageStorageBatch, ISimplifiedThreadStructure, IMailMessageStorageMimeSupport, IMailMessageStorageBatchCopyMove {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPMessageStorage.class);

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

    private static volatile Boolean allowESORT;
    /** Whether ESORT is allowed to be utilized */
    static boolean allowESORT() {
        Boolean b = allowESORT;
        if (null == b) {
            synchronized (IMAPMessageStorage.class) {
                b = allowESORT;
                if (null == b) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    b = Boolean.valueOf(null == service || service.getBoolProperty("com.openexchange.imap.allowESORT", true));
                    allowESORT = b;
                }
            }
        }
        return b.booleanValue();
    }

    /** Whether SORT=DISPLAY is allowed to be utilized */
    public static boolean allowSORTDISPLAY(Session session, int accountId) throws OXException {
        return allowSORTDISPLAY(session.getUserId(), session.getContextId(), accountId);
    }

    /** Whether SORT=DISPLAY is allowed to be utilized */
    public static boolean allowSORTDISPLAY(int userId, int contextId, int accountId) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, contextId);

        if (MailAccount.DEFAULT_ID == accountId) {
            ComposedConfigProperty<Boolean> property = view.property("com.openexchange.imap.primary.allowSORTDISPLAY", boolean.class);
            if (property.isDefined()) {
                return property.get().booleanValue();
            }
        }

        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.imap.allowSORTDISPLAY", boolean.class);
        return property.isDefined() ? property.get().booleanValue() : false;
    }

    /** Whether in-app sort is supposed to be utilized if IMAP-side SORT fails with a "NO" response */
    public static boolean fallbackOnFailedSORT(Session session, int accountId) throws OXException {
        return fallbackOnFailedSORT(session.getUserId(), session.getContextId(), accountId);
    }

    /** Whether in-app sort is supposed to be utilized if IMAP-side SORT fails with a "NO" response */
    public static boolean fallbackOnFailedSORT(int userId, int contextId, int accountId) throws OXException {
        ConfigViewFactory factory = Services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(userId, contextId);

        if (MailAccount.DEFAULT_ID == accountId) {
            ComposedConfigProperty<Boolean> property = view.property("com.openexchange.imap.primary.fallbackOnFailedSORT", boolean.class);
            if (property.isDefined()) {
                return property.get().booleanValue();
            }
        }

        ComposedConfigProperty<Boolean> property = view.property("com.openexchange.imap.fallbackOnFailedSORT", boolean.class);
        return property.isDefined() ? property.get().booleanValue() : false;
    }

    private static volatile ImmutableReference<String> allMessagesFolder;
    /** The full name for the virtual "all messages" folder */
    public static String allMessagesFolder() {
        ImmutableReference<String> fa = allMessagesFolder;
        if (null == fa) {
            synchronized (IMAPMessageStorage.class) {
                fa = allMessagesFolder;
                if (null == fa) {
                    ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return null;
                    }

                    String fn = service.getProperty("com.openexchange.find.basic.mail.allMessagesFolder", "").trim();
                    fa = new ImmutableReference<String>(fn.length() <= 0 ? null : fn);
                    allMessagesFolder = fa;
                }
            }
        }
        return fa.getValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                useImapThreaderIfSupported = null;
                allowESORT = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

    /**
     * Gets the prepared mail fields in case full name denotes the special "all messages" folder.
     *
     * @param mailFields The requested mail fields by client
     * @param fullName The full name
     * @return The prepared mail fields
     */
    static MailFields prepareMailFieldsForVirtualFolder(MailField[] mailFields, String fullName) {
        MailFields fields = new MailFields(mailFields);
        prepareMailFieldsForVirtualFolder(fields, fullName);
        return fields;
    }

    /**
     * Gets the prepared mail fields in case full name denotes the special "all messages" folder.
     *
     * @param mailFields The current mail fields
     * @param fullName The full name
     * @return The prepared mail fields
     */
    static void prepareMailFieldsForVirtualFolder(MailFields mailFields, String fullName) {
        if (null == fullName || !fullName.equals(allMessagesFolder())) {
            return;
        }

        mailFields.add(MailField.ORIGINAL_FOLDER_ID);
        mailFields.add(MailField.ORIGINAL_ID);
    }

    private static final int _5MB = 5242880; /* 5MB */

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
    public IMAPMessageStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws OXException {
        super(imapStore, imapAccess, session);
        imapFolderStorage = imapAccess.getFolderStorage();
    }

    /**
     * Handles specified {@link MessagingException} instance.
     *
     * @param e The {@link MessagingException} instance
     * @return The appropriate {@link OXException} instance
     */
    public OXException handleMessagingException(MessagingException e) {
        return handleMessagingException(null, e);
    }

    /**
     * Handles specified {@link MessagingException} instance.
     *
     * @param optFullName The optional full name
     * @param e The {@link MessagingException} instance
     * @return The appropriate {@link OXException} instance
     */
    public OXException handleMessagingException(String optFullName, MessagingException e) {
        if (null != optFullName && MimeMailException.isInUseException(e)) {
            IMAPFolderWorker.markForFailFast(imapStore, optFullName, e);
        }
        return IMAPException.handleMessagingException(e, imapConfig, session, accountId, null == optFullName ? null : mapFor("fullName", optFullName));
    }

    /**
     * Gets the mail account
     *
     * @return The mail account
     * @throws OXException If mail account cannot be returned
     */
    protected MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountFacade mailAccountFacade = Services.getService(MailAccountFacade.class);
                mailAccount = mailAccountFacade.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final RuntimeException e) {
                throw handleRuntimeException(e);
            }
        }
        return mailAccount;
    }

    /**
     * Gets the user's locale
     *
     * @return The locale
     * @throws OXException If locale cannot be returned
     */
    protected Locale getLocale() throws OXException {
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

    /**
     * Gets the IMAP properties
     *
     * @return The IMAP properties
     */
    protected IIMAPProperties getIMAPProperties() {
        if (null == imapProperties) {
            imapProperties = imapConfig.getIMAPProperties();
        }
        return imapProperties;
    }

    /**
     * Opens the denoted folder in read-only mode.
     *
     * @param fullName The full name of the folder to open
     * @throws OXException If opening folder fails
     */
    protected void openReadOnly(final String fullName) throws OXException {
        try {
            imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
        } catch (final MessagingException e) {
            final Exception next = e.getNextException();
            if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                throw handleMessagingException(fullName, e);
            }
            throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
        }
    }

    /**
     * Gets the associated IMAP store.
     *
     * @return The IMAP store
     */
    public IMAPStore getImapStore() {
        return imapStore;
    }

    /**
     * Gets the current IMAP folder in use.
     *
     * @return The IMAP folder or <code>null</code>
     */
    public IMAPFolder getImapFolder() {
        return imapFolder;
    }

    /**
     * Gets the IMAP configuration
     *
     * @return The IMAP configuration
     */
    public IMAPConfig getImapConfig() {
        return imapConfig;
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
        return textXtractService.extractFrom(Streams.newByteArrayInputStream(content.getBytes(Charsets.UTF_8)), optMimeType);
    }

    @Override
    public String[] getPrimaryContentsLong(final String fullName, final long[] mailIds) throws OXException {
        if (!imapConfig.getImapCapabilities().hasIMAP4rev1()) {
            return super.getPrimaryContentsLong(fullName, mailIds);
        }
        try {
            openReadOnly(fullName);
            final BODYSTRUCTURE[] bodystructures = new BodystructureFetchIMAPCommand(imapFolder, mailIds).doCommand();
            final String[] retval = new String[mailIds.length];

            for (int i = 0; i < bodystructures.length; i++) {
                final BODYSTRUCTURE bodystructure = bodystructures[i];
                if (null != bodystructure) {
                    try {
                        retval[i] = handleBODYSTRUCTURE(fullName, mailIds[i], bodystructure, null, 1, new boolean[1]);
                    } catch (final Exception e) {
                        LOG.debug("Ignoring failed handling of BODYSTRUCTURE item", e);
                        retval[i] = null;
                    }
                }
            }
            return retval;
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static final Whitelist WHITELIST = Whitelist.relaxed();

    // private static final Pattern PATTERN_CRLF = Pattern.compile("(\r?\n)+");

    private String handleBODYSTRUCTURE(final String fullName, final long mailId, final BODYSTRUCTURE bodystructure, final String prefix, final int partCount, final boolean[] mpDetected) throws OXException {
        try {
            final String type = com.openexchange.java.Strings.toLowerCase(bodystructure.type);
            if ("text".equals(type)) {
                final String sequenceId = getSequenceId(prefix, partCount);
                String content;
                {
                    final byte[] bytes = new BodyFetchIMAPCommand(imapFolder, mailId, sequenceId, true).doCommand();
                    final ParameterList cParams = bodystructure.cParams;
                    content = readContent(bytes, null == cParams ? null : MimeMessageUtility.decodeEnvelopeHeader(cParams.get("charset")), bodystructure.encoding);
                }
                final String subtype = com.openexchange.java.Strings.toLowerCase(bodystructure.subtype);
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
                        LOG.trace("", s);
                        return null;
                    }
                    // content = PATTERN_CRLF.matcher(content).replaceAll("");// .replaceAll("(  )+", "");
                }
                try {
                    return extractPlainText(content, new StringBuilder(type).append('/').append(subtype).toString());
                } catch (final OXException e) {
                    if (!subtype.startsWith("htm")) {
                        final StringBuilder sb =
                            new StringBuilder("Failed extracting plain text from \"text/").append(subtype).append("\" part:\n");
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
                    return extractPlainText(Jsoup.clean(content, WHITELIST), new StringBuilder(type).append('/').append(subtype).toString());
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
                final String subtype = com.openexchange.java.Strings.toLowerCase(bodystructure.subtype);
                final int count = bodies.length;
                if ("alternative".equals(subtype)) {
                    /*
                     * Prefer HTML text over plain text
                     */
                    String text = null;
                    for (int i = 0; i < count; i++) {
                        final BODYSTRUCTURE bp = bodies[i];
                        final String bpType = com.openexchange.java.Strings.toLowerCase(bp.type);
                        final String bpSubtype = com.openexchange.java.Strings.toLowerCase(bp.subtype);
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
            final String subtype = com.openexchange.java.Strings.toLowerCase(bodystructure.subtype);
            if ("application".equals(type) && (subtype.startsWith("application/ms-tnef") || subtype.startsWith("application/vnd.ms-tnef"))) {
                final String sequenceId = getSequenceId(prefix, partCount);
                final byte[] bytes = new BodyFetchIMAPCommand(imapFolder, mailId, sequenceId, true).doCommand();
                return new TextFinder().handleTNEFStream(Streams.newByteArrayInputStream(bytes));
            }
            return null;
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
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
            LOG.trace("", e);
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
        return new StringBuilder(prefix).append('.').append(partCount).toString();
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
        prepareMailFieldsForVirtualFolder(fieldSet, fullName);
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
            openReadOnly(fullName);
            /*
             * Fetch desired messages by given UIDs. Turn UIDs to corresponding sequence numbers to maintain order cause some IMAP servers
             * ignore the order of UIDs provided in a "UID FETCH" command.
             */
            final MailMessage[] messages;
            final MailField[] fields = fieldSet.toArray();
            if (imapConfig.asMap().containsKey("UIDPLUS")) {
                long[] valids = filterNegativeElements(uids);
                final TLongObjectHashMap<MailMessage> fetchedMsgs =
                    fetchValidWithFallbackFor(
                        fullName,
                        valids,
                        valids.length,
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
                long[] valids = filterNegativeElements(uids);
                final TLongIntMap seqNumsMap = IMAPCommandsCollection.uids2SeqNumsMap(imapFolder, valids);
                final TLongObjectMap<MailMessage> fetchedMsgs =
                    fetchValidWithFallbackFor(
                        fullName,
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
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            clearCache(imapFolder);
        }
    }

    private TLongObjectHashMap<MailMessage> fetchValidWithFallbackFor(final String fullName, final Object array, final int len, final FetchProfile fetchProfile, final boolean isRev1, final boolean seqnum) throws OXException {
        final String key = new StringBuilder(16).append(accountId).append(".imap.fetch.modifier").toString();
        final FetchProfile fp = fetchProfile;
        int retry = 0;
        while (true) {
            try {
                final FetchProfileModifier modifier = (FetchProfileModifier) session.getParameter(key);
                if (null == modifier) {
                    // session.setParameter(key, FetchIMAPCommand.DEFAULT_PROFILE_MODIFIER);
                    return fetchValidFor(fullName, array, len, fp, isRev1, seqnum, false);
                }
                return fetchValidFor(fullName, array, len, modifier.modify(fp), isRev1, seqnum, modifier.byContentTypeHeader());
            } catch (final FolderClosedException e) {
                throw handleMessagingException(imapFolder.getFullName(), e);
            } catch (final StoreClosedException e) {
                throw handleMessagingException(imapFolder.getFullName(), e);
            } catch (final MessagingException e) {
                final Exception nextException = e.getNextException();
                if ((nextException instanceof BadCommandException) || (nextException instanceof CommandFailedException)) {
                    if (LOG.isDebugEnabled()) {
                        final StringBuilder sb = new StringBuilder(128).append("Fetch with fetch profile failed: ");
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
                        throw handleMessagingException(imapFolder.getFullName(), e);
                    }
                } else {
                    throw handleMessagingException(imapFolder.getFullName(), e);
                }
            } catch (final ArrayIndexOutOfBoundsException e) {
                /*
                 * May occur while parsing invalid BODYSTRUCTURE response
                 */
                if (LOG.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder(128).append("Fetch with fetch profile failed: ");
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

    private TLongObjectHashMap<MailMessage> fetchValidFor(final String fullName, final Object array, final int len, final FetchProfile fetchProfile, final boolean isRev1, final boolean seqnum, final boolean byContentType) throws MessagingException, OXException {
        if (null == imapFolder || !imapFolder.checkOpen()) {
            openReadOnly(fullName);
        }
        final TLongObjectHashMap<MailMessage> map = new TLongObjectHashMap<MailMessage>(len);
        // final MailMessage[] tmp = new NewFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, array, fetchProfile, false,
        // false, false).setDetermineAttachmentByHeader(byContentType).doCommand();
        final MailMessageFetchIMAPCommand command;
        if (array instanceof long[]) {
            command =
                new MailMessageFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, (long[]) array, fetchProfile, imapServerInfo).setDetermineAttachmentByHeader(byContentType);
        } else {
            command =
                new MailMessageFetchIMAPCommand(imapFolder, getSeparator(imapFolder), isRev1, (int[]) array, fetchProfile, imapServerInfo).setDetermineAttachmentByHeader(byContentType);
        }
        final long start = System.currentTimeMillis();
        final MailMessage[] tmp = command.doCommand();
        final long time = System.currentTimeMillis() - start;
        mailInterfaceMonitor.addUseTime(time);
        LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(len), Long.valueOf(time));
        for (final MailMessage mailMessage : tmp) {
            if (null != mailMessage) {
                IDMailMessage idmm = (IDMailMessage) mailMessage;
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
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException("INBOX", e);
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
            throw handleMessagingException(imapFolder.getFullName(), e);
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
            openReadOnly(fullName);
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            /*
             * Check Content-Type
             */
            boolean useGetPart = true;
            {
                IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
                if (null == msg) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(msgUID), fullName);
                }
                String sContentType = msg.getContentType();
                if (null != sContentType && Strings.asciiLowerCase(sContentType.trim()).startsWith("multipart/signed")) {
                    useGetPart = false;
                }
            }
            /*
             * Try by Content-ID
             */
            if (useGetPart) {
                try {
                    final MailPart part = IMAPCommandsCollection.getPart(imapFolder, msgUID, sequenceId, false);
                    if (null != part) {
                        // Appropriate part found -- check for special content
                        final ContentType contentType = part.getContentType();
                        if (!isTNEFMimeType(contentType) && !isUUEncoded(part, contentType)) {
                            return part;
                        }
                    }
                } catch (MessageRemovedException e) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(e, Long.valueOf(msgUID), fullName);
                } catch (final IOException e) {
                    if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(e, Long.valueOf(msgUID), fullName);
                    }
                    // Ignore
                } catch (final Exception e) {
                    // Ignore
                    LOG.trace("", e);
                }
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
            final MailPart mailPart = handler.getMailPart();
            if (mailPart == null) {
                throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, Long.valueOf(msgUID), fullName);
            }
            return mailPart;
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw handleMessagingException(fullName, e);
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
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            /*
             * Check Content-Type
             */
            boolean useGetPart = true;
            {
                IMAPMessage msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
                if (null == msg) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(msgUID), fullName);
                }
                String sContentType = msg.getContentType();
                if (null != sContentType && Strings.asciiLowerCase(sContentType.trim()).startsWith("multipart/signed")) {
                    useGetPart = false;
                }
            }
            /*
             * Try by Content-ID
             */
            if (useGetPart) {
                try {
                    final MailPart partByContentId = IMAPCommandsCollection.getPartByContentId(imapFolder, msgUID, contentId, false);
                    if (null != partByContentId) {
                        return partByContentId;
                    }
                } catch (final Exception e) {
                    // Ignore
                    LOG.trace("", e);
                }
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
                MimeMessage tmp = MimeMessageUtility.mimeMessageFrom(msg);
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
            throw handleMessagingException(fullName, e);
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
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
            String ct = Strings.toLowerCase(getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_TYPE, part));

            String realFilename = null;
            boolean considerAsImage = false;
            if (null == ct) {
                realFilename = getRealFilename(part);
                if (false == Strings.isEmpty(realFilename) && MimeType2ExtMap.getContentType(realFilename, "").startsWith("image/")) {
                    considerAsImage = true;
                }
            } else if (ct.startsWith("image/")) {
                considerAsImage = true;
            }

            if (considerAsImage) {
                String partContentId = getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_ID, part);
                if (null == partContentId) {
                    /*
                     * Compare with file name
                     */
                    if (null == realFilename) {
                        realFilename = getRealFilename(part);
                    }
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
                if (null == realFilename) {
                    realFilename = getRealFilename(part);
                }
                if (MimeMessageUtility.equalsCID(contentId, realFilename)) {
                    return part;
                }
            } else if (null != ct && ct.startsWith("multipart/")) {
                Multipart m;
                {
                    final Object content = part.getContent();
                    if (content instanceof Multipart) {
                        m = (Multipart) content;
                    } else {
                        m = new MimeMultipart(part.getDataHandler().getDataSource());
                    }
                }
                int count = m.getCount();
                for (int i = 0; i < count; i++) {
                    final Part p = examinePart(m.getBodyPart(i), contentId);
                    if (null != p) {
                        return p;
                    }
                }
            }
            return null;
        } catch (MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw handleMessagingException(imapFolder.getFullName(), e);
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static String getRealFilename(final Part part) throws MessagingException {
        String fileName;
        try {
            fileName = part.getFileName();
        } catch (final javax.mail.internet.ParseException e) {
            // JavaMail failed to parse Content-Disposition header
            LOG.trace("JavaMail failed to parse Content-Disposition header", e);
            fileName = null;
        }
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
            LOG.trace("Failed to parse Content-Disposition header", e);
            return getContentTypeFilename(part);
        }
    }

    private static String getContentTypeFilename(final Part part) throws MessagingException {
        final String hdr = getFirstHeaderFrom(MessageHeaders.HDR_CONTENT_TYPE, part);
        if (hdr == null || hdr.length() == 0) {
            return null;
        }
        try {
            return new ContentType(hdr).getNameParameter();
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    private static String getFirstHeaderFrom(final String name, final Part part) throws MessagingException {
        return MimeMessageUtility.getHeader(name, null, part);
    }

    private static final FetchProfile FETCH_PROFILE_ENVELOPE = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
        }
    };

    @Override
    public Message getMimeMessage(String fullName, String id, boolean markSeen) throws OXException {
        if (null == id) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create("null", fullName);
        }
        try {
            final int desiredMode = markSeen ? READ_WRITE : READ_ONLY;
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, desiredMode);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            }
            final long uid = parseUnsignedLong(id);
            IMAPMessage msg;
            try {
                final long start = System.currentTimeMillis();
                msg = (IMAPMessage) imapFolder.getMessageByUID(uid);
                imapFolder.fetch(new Message[] {msg}, FETCH_PROFILE_ENVELOPE);
                mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
            } catch (final java.lang.NullPointerException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            } catch (final MessageRemovedException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            } catch (final MessagingException e) {
                final Exception cause = e.getNextException();
                if (!(cause instanceof BadCommandException)) {
                    throw e;
                }
                // Hm... Something weird with executed "UID FETCH" command; retry manually...
                final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, new long[] { uid });
                if ((null == seqNums) || (0 == seqNums.length)) {
                    LOG.warn("No message with UID '{}' found in folder '{}'", id, fullName, cause);
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
                }
                final int msgnum = seqNums[0];
                if (msgnum < 1) {
                    /*
                     * message-numbers start at 1
                     */
                    LOG.warn("No message with UID '{}' found in folder '{}'", id, fullName, cause);
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
                }
                msg = (IMAPMessage) imapFolder.getMessage(msgnum);
            }
            if (msg == null || msg.isExpunged()) {
                // throw new OXException(OXException.Code.MAIL_NOT_FOUND,
                // String.valueOf(msgUID), imapFolder
                // .toString());
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            }
            return msg;
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(id, fullName);
            }
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Checks existence for a message denoted by given identifier.
     *
     * @param fullName The mailbox full name
     * @param msgUID The message identifier
     * @return <code>true</code> if such a message exists; otherwise <code>false</code> if absent
     * @throws OXException If operation fails
     */
    public boolean exists(String fullName, String msgUID)  throws OXException {
        return existsLong(fullName, parseUnsignedLong(msgUID));
    }

    /**
     * Checks existence for a message denoted by given identifier.
     *
     * @param fullName The mailbox full name
     * @param msgUID The message identifier
     * @return <code>true</code> if such a message exists; otherwise <code>false</code> if absent
     * @throws OXException If operation fails
     */
    public boolean existsLong(String fullName, long msgUID)  throws OXException {
        if (msgUID < 0) {
            return false;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return false;
            }
            try {
                return null != imapFolder.getMessageByUID(msgUID);
            } catch (final java.lang.NullPointerException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return false;
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return false;
            } catch (final MessageRemovedException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return false;
            } catch (final MessagingException e) {
                final Exception cause = e.getNextException();
                if (!(cause instanceof BadCommandException)) {
                    throw e;
                }
                // Hm... Something weird with executed "UID FETCH" command; retry manually...
                final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, new long[] { msgUID });
                if ((null == seqNums) || (0 == seqNums.length)) {
                    LOG.debug("No message with UID '{}' found in folder '{}'", Long.valueOf(msgUID), fullName, cause);
                    return false;
                }
                final int msgnum = seqNums[0];
                if (msgnum < 1) {
                    /*
                     * message-numbers start at 1
                     */
                    LOG.debug("No message with UID '{}' found in folder '{}'", Long.valueOf(msgUID), fullName, cause);
                    return false;
                }
                return true;
            }
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return false;
            }
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private static final FetchProfile FETCH_PROFILE_GET = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
            add(FetchProfile.Item.FLAGS);
            add(FetchProfile.Item.CONTENT_INFO);
            add(FetchProfile.Item.SIZE);
            add(IMAPFolder.FetchProfileItem.HEADERS);
        }
    };

    private static final FetchProfile FETCH_PROFILE_GET_FOR_VIRTUAL = new FetchProfile() {

        // Unnamed block
        {
            add(FetchProfile.Item.ENVELOPE);
            add(FetchProfile.Item.FLAGS);
            add(FetchProfile.Item.CONTENT_INFO);
            add(FetchProfile.Item.SIZE);
            add(IMAPFolder.FetchProfileItem.HEADERS);
            add(MimeStorageUtility.ORIGINAL_MAILBOX);
            add(MimeStorageUtility.ORIGINAL_UID);
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
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            if (0 >= imapFolder.getMessageCount()) {
                return null;
            }
            IMAPMessage msg;
            try {
                long start = System.currentTimeMillis();
                msg = (IMAPMessage) imapFolder.getMessageByUID(msgUID);
                if (null == msg) {
                    return null;
                }
                // Force to pre-load envelope data
                imapFolder.fetch(new Message[] { msg }, null != fullName && fullName.equals(allMessagesFolder()) ? FETCH_PROFILE_GET_FOR_VIRTUAL : FETCH_PROFILE_GET);
                long duration = System.currentTimeMillis() - start;

                if (duration > 1000L) {
                    LOG.warn("Retrieval of message {} in folder {} from IMAP mailbox {} took {}msec", Long.valueOf(msgUID), fullName, imapStore, Long.valueOf(duration));
                }

                mailInterfaceMonitor.addUseTime(duration);
            } catch (final java.lang.NullPointerException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return null;
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return null;
            } catch (final MessageRemovedException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
                return null;
            } catch (final MessagingException e) {
                final Exception cause = e.getNextException();
                if (!(cause instanceof BadCommandException)) {
                    throw e;
                }
                // Hm... Something weird with executed "UID FETCH" command; retry manually...
                final int[] seqNums = IMAPCommandsCollection.uids2SeqNums(imapFolder, new long[] { msgUID });
                if ((null == seqNums) || (0 == seqNums.length)) {
                    LOG.debug("No message with UID '{}' found in folder '{}'", Long.valueOf(msgUID), fullName, cause);
                    return null;
                }
                final int msgnum = seqNums[0];
                if (msgnum < 1) {
                    /*
                     * message-numbers start at 1
                     */
                    LOG.debug("No message with UID '{}' found in folder '{}'", Long.valueOf(msgUID), fullName, cause);
                    return null;
                }
                msg = (IMAPMessage) imapFolder.getMessage(msgnum);
            }

            // Check existence
            if (msg == null || msg.isExpunged()) {
                return null;
            }
            msg.setUID(msgUID);
            msg.setPeek(!markSeen);

            // Convert to a MailMessage instance
            MailMessage mail;
            try {
                long size = msg.getSize();
                Long origUid = (Long) msg.getItem("X-REAL-UID");
                String origFolder = (String) msg.getItem("X-MAILBOX");
                if (size > _5MB && isComplex(msg.getBodystructure())) {
                    int blkSize = imapStore.getFetchBlockSize();
                    try {
                        // Copy complete MIME stream
                        imapStore.setFetchBlockSize(_5MB);
                        MimeMessage copy = MimeMessageUtility.newMimeMessage(msg.getMimeStream(), null);
                        mail = MimeMessageConverter.convertMessage(copy, false);
                        // Set flags and received date
                        MimeMessageConverter.parseFlags(msg.getFlags(), mail);
                        if (!mail.containsColorLabel()) {
                            mail.setColorLabel(MailMessage.COLOR_LABEL_NONE);
                        }
                        mail.setReceivedDate(msg.getReceivedDate());
                    } finally {
                        // Restore fetch block size
                        imapStore.setFetchBlockSize(blkSize);
                    }
                } else {
                    mail = MimeMessageConverter.convertMessage(msg, false);
                }
                mail.setFolder(fullName);
                if (null != origUid) {
                    mail.setOriginalId(origUid.toString());
                }
                if (null != origFolder) {
                    mail.setOriginalFolder(origFolder);
                }
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
                    LOG.debug("Generic messaging error occurred for mail \"{}\" in folder \"{}\" with login \"{}\" on server \"{}\" (user={}, context={})", Long.valueOf(msgUID), fullName, imapConfig.getLogin(), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                }
                throw e;
            } catch (final java.lang.IndexOutOfBoundsException e) {
                /*
                 * Obviously message was removed in the meantime
                 */
                LOG.trace("Obviously message was removed in the meantime", e);
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
                        LOG.warn("/SEEN flag could not be set on message #{} in folder {}", mail.getMailId(), mail.getFolder(), e);
                    }
                } else {
                    setSeenFlag(fullName, mail, msg);
                }
            }
            clearCache(imapFolder);
            return setAccountInfo(mail);
        } catch (final MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return null;
            }
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private boolean isComplex(BODYSTRUCTURE bodystructure) {
        int threshold = 10;
        return countNested(bodystructure, 0, threshold) >= threshold;
    }

    private int countNested(BODYSTRUCTURE bodystructure, int current, int threshold) {
        int count = current;
        if (count >= threshold) {
            return count;
        }

        if (bodystructure.isNested()) {
            count++;
        }

        BODYSTRUCTURE[] bodies = bodystructure.bodies;
        if (null != bodies) {
            for (BODYSTRUCTURE subbody : bodies) {
                count = countNested(subbody, count, threshold);
                if (count >= threshold) {
                    return count;
                }
            }
        }

        return count;
    }

    private void setSeenFlag(final String fullName, final MailMessage mail, final IMAPMessage msg) {
        try {
            msg.setFlags(FLAGS_SEEN, true);
            mail.setFlag(MailMessage.FLAG_SEEN, true);
            final int cur = mail.getUnreadMessages();
            mail.setUnreadMessages(cur <= 0 ? 0 : cur - 1);
        } catch (final Exception e) {
            LOG.warn("/SEEN flag could not be set on message #{} in folder {}", mail.getMailId(), mail.getFolder(), e);
        }
    }

    @Override
    public MailMessage[] searchMessages(String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] mailFields) throws OXException {
        return searchMessages(fullName, indexRange, sortField, order, searchTerm, mailFields, null);
    }

    @Override
    public int getUnreadCount(String folder, SearchTerm<?> searchTerm) throws OXException {
        try {
            openReadOnly(folder);
        } catch (OXException e) {
            if (IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.equals(e)) {
                return 0;
            }
            throw e;
        }
        try {
            SearchTerm<?> unseenSearchTerm = new ANDTerm(searchTerm, new FlagTerm(MailMessage.FLAG_SEEN, false));
            return IMAPSearch.searchMessages(imapFolder, unseenSearchTerm, imapConfig).length;
        } catch (MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return 0;
            }
            throw handleMessagingException(folder, e);
        }
    }

    @Override
    public MailMessage[] searchMessages(String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] mailFields, String[] headerNames) throws OXException {
        try {
            openReadOnly(fullName);
        } catch (OXException e) {
            if (IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES.equals(e)) {
                return EMPTY_RETVAL;
            }
            throw e;
        }

        try {
            int messageCount = imapFolder.getMessageCount();
            if (messageCount <= 0) {
                return EMPTY_RETVAL;
            }

            MailSortField effectiveSortField = determineSortFieldForSearch(fullName, sortField);
            MailFields effectiveFields = prepareMailFieldsForSearch(mailFields, effectiveSortField);
            prepareMailFieldsForVirtualFolder(effectiveFields, fullName);
            MailMessage[] mailMessages;
            if (searchViaIMAP(searchTerm == null ? new MailFields() : new MailFields(MailField.getMailFieldsFromSearchTerm(searchTerm)))) {
                try {
                    mailMessages = performIMAPSearch(effectiveSortField, order, searchTerm, effectiveFields, indexRange, headerNames, messageCount);
                } catch (OXException e) {
                    if (false == IMAPException.Code.UNSUPPORTED_SORT_FIELD.equals(e)) {
                        throw e;
                    }
                    // Fall back to in-app search&sort
                    mailMessages = performInAppSearch(effectiveSortField, order, searchTerm, effectiveFields, indexRange, headerNames, messageCount);
                }
            } else {
                mailMessages = performInAppSearch(effectiveSortField, order, searchTerm, effectiveFields, indexRange, headerNames, messageCount);
            }

            if (mailMessages.length == 0) {
                mailMessages = EMPTY_RETVAL;
            }

            return mailMessages;
        } catch (MessagingException e) {
            if (ImapUtility.isInvalidMessageset(e)) {
                return new MailMessage[0];
            }
            throw handleMessagingException(fullName, e);
        } catch (RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            clearCache(imapFolder);
        }
    }

    private boolean searchViaIMAP(MailFields fields) throws MessagingException {
        int msgCount = imapFolder.getMessageCount();
        if (fields.contains(MailField.BODY) || fields.contains(MailField.FULL)) {
            if (imapConfig.forceImapSearch() || (msgCount >= MailProperties.getInstance().getMailFetchLimit())) {
                return true;
            }
        } else {
            return imapConfig.isImapSearch() || (msgCount >= MailProperties.getInstance().getMailFetchLimit());
        }

        return false;
    }

    private MailMessage[] performIMAPSearch(MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailFields fields, IndexRange indexRange, String[] headerNames, int messageCount) throws MessagingException, OXException {
        boolean hasSort = imapConfig.getCapabilities().hasSort();
        boolean fallbackOnFailedSORT = fallbackOnFailedSORT(session, accountId);
        if (hasSort && IMAPSort.isValidSortField(sortField)) {
            /*
             * Use SORT command as it allows searching and sorting at once (https://tools.ietf.org/html/rfc5256)
             */
            boolean sortedByLocalPart;
            int[] msgIds;
            {
                ImapSortResult result = IMAPSort.sortMessages(imapFolder, searchTerm, sortField, order, indexRange, allowESORT(), allowSORTDISPLAY(session, accountId), fallbackOnFailedSORT, imapConfig);
                sortedByLocalPart = result.sortedByLocalPart;
                msgIds = result.msgIds;
                if (false == result.rangeApplied) {
                    msgIds = applyIndexRange(msgIds, indexRange);
                }
                if (msgIds.length == 0) {
                    return EMPTY_RETVAL;
                }
            }

            /*
             * Fetch (possibly) filtered and sorted sequence numbers
             */
            boolean fetchBody = fields.contains(MailField.BODY) || fields.contains(MailField.FULL);
            MailMessage[] mailMessages;
            if (fetchBody) {
                FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, getIMAPProperties().isFastFetch());
                List<MailMessage> list = fetchMessages(msgIds, fetchProfile);
                mailMessages = list.toArray(new MailMessage[list.size()]);
            } else {
                /*
                 * Body content not requested, we simply return IDMailMessage objects filled with requested fields
                 */
                boolean isRev1 = imapConfig.getImapCapabilities().hasIMAP4rev1();
                FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, getIMAPProperties().isFastFetch());
                MailMessage[] tmp = fetchMessages(msgIds, fetchProfile, isRev1, getSeparator(imapFolder));
                mailMessages = setAccountInfo(tmp);
            }

            if (mailMessages.length == 0) {
                return EMPTY_RETVAL;
            }

            // TODO: Return with respect to "sortedByLocalPart"
            // return sortedByLocalPart ? removePersonals(mailMessages) : mailMessages;

            return mailMessages;
        }

        // Check for special sort field
        if (hasSort && MailSortField.FLAG_SEEN.equals(sortField) && null == searchTerm) {
            // Perform "SEARCH UNSEEN" IMAP command
            int[] unseenSeqNums = null;
            int[] seenSeqNums = null;

            int[] seqNumsToFetch = null;
            if (OrderDirection.ASC.equals(order)) {
                SearchTerm<?> unseenSearchterm = new FlagTerm(MailMessage.FLAG_SEEN, false);
                unseenSeqNums = IMAPSort.sortMessages(imapFolder, unseenSearchterm, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, false, false, fallbackOnFailedSORT, imapConfig).msgIds;

                if (unseenSeqNums.length == 0) {
                    // No unseen messages at all
                    return performIMAPSearch(MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, fields, indexRange, headerNames, messageCount);
                }

                if (null != indexRange && indexRange.start < unseenSeqNums.length && indexRange.end <= unseenSeqNums.length) {
                    // Complete requested range can be served
                    seqNumsToFetch = applyIndexRange(unseenSeqNums, indexRange);
                }
            } else {
                SearchTerm<?> seenSearchterm = new FlagTerm(MailMessage.FLAG_SEEN, true);
                seenSeqNums = IMAPSort.sortMessages(imapFolder, seenSearchterm, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, false, false, fallbackOnFailedSORT, imapConfig).msgIds;

                if (seenSeqNums.length == 0) {
                    // No seen messages at all
                    return performIMAPSearch(MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, fields, indexRange, headerNames, messageCount);
                }

                if (null != indexRange && indexRange.start < seenSeqNums.length && indexRange.end <= seenSeqNums.length) {
                    // Complete requested range can be served
                    seqNumsToFetch = applyIndexRange(seenSeqNums, indexRange);
                }
            }

            if (null == seqNumsToFetch) {
                if (null == unseenSeqNums) {
                    SearchTerm<?> unseenSearchterm = new FlagTerm(MailMessage.FLAG_SEEN, false);
                    unseenSeqNums = IMAPSort.sortMessages(imapFolder, unseenSearchterm, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, false, false, fallbackOnFailedSORT, imapConfig).msgIds;
                }
                if (null == seenSeqNums) {
                    SearchTerm<?> seenSearchterm = new FlagTerm(MailMessage.FLAG_SEEN, true);
                    seenSeqNums = IMAPSort.sortMessages(imapFolder, seenSearchterm, MailSortField.RECEIVED_DATE, OrderDirection.DESC, null, false, false, fallbackOnFailedSORT, imapConfig).msgIds;
                }

                int[] sortedSeqNums;
                {
                    int numberOfMessages = unseenSeqNums.length + seenSeqNums.length;
                    if (null == indexRange) {
                        sortedSeqNums = new int[numberOfMessages];
                        if (OrderDirection.ASC.equals(order)) {
                            System.arraycopy(unseenSeqNums, 0, sortedSeqNums, 0, unseenSeqNums.length);
                            System.arraycopy(seenSeqNums, 0, sortedSeqNums, unseenSeqNums.length, seenSeqNums.length);
                        } else {
                            System.arraycopy(seenSeqNums, 0, sortedSeqNums, 0, seenSeqNums.length);
                            System.arraycopy(unseenSeqNums, 0, sortedSeqNums, seenSeqNums.length, unseenSeqNums.length);
                        }
                    } else {
                        int fromIndex = indexRange.start;
                        if ((fromIndex) > numberOfMessages) {
                            return EMPTY_RETVAL;
                        }

                        int toIndex = indexRange.end;
                        if (toIndex >= numberOfMessages) {
                            toIndex = numberOfMessages;
                        }

                        int numToCopy = toIndex - fromIndex;
                        if (numToCopy <= 0) {
                            return EMPTY_RETVAL;
                        }

                        sortedSeqNums = new int[numToCopy];
                        if (OrderDirection.ASC.equals(order)) {
                            int length = Math.min(unseenSeqNums.length, numToCopy);
                            System.arraycopy(unseenSeqNums, 0, sortedSeqNums, 0, length);
                            numToCopy -= length;
                            if (numToCopy > 0) {
                                System.arraycopy(seenSeqNums, 0, sortedSeqNums, length, numToCopy);
                            }
                        } else {
                            int length = Math.min(seenSeqNums.length, numToCopy);
                            System.arraycopy(seenSeqNums, 0, sortedSeqNums, 0, length);
                            numToCopy -= length;
                            if (numToCopy > 0) {
                                System.arraycopy(unseenSeqNums, 0, sortedSeqNums, length, numToCopy);
                            }
                        }
                    }
                }
                seqNumsToFetch = sortedSeqNums;
            }

            boolean fetchBody = fields.contains(MailField.BODY) || fields.contains(MailField.FULL);
            MailMessage[] mailMessages;
            if (fetchBody) {
                FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, getIMAPProperties().isFastFetch());
                List<MailMessage> list = fetchMessages(seqNumsToFetch, fetchProfile);
                mailMessages = list.toArray(new MailMessage[list.size()]);
            } else {
                /*
                 * Body content not requested, we simply return IDMailMessage objects filled with requested fields
                 */
                boolean isRev1 = imapConfig.getImapCapabilities().hasIMAP4rev1();
                FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, getIMAPProperties().isFastFetch());
                MailMessage[] tmp = fetchMessages(seqNumsToFetch, fetchProfile, isRev1, getSeparator(imapFolder));
                mailMessages = setAccountInfo(tmp);
            }

            return mailMessages;
        }

        // Fall-back path...
        int[] msgIds = null == searchTerm ? null : IMAPSearch.issueIMAPSearch(imapFolder, searchTerm);
        /*
         * Do application sort
         */
        return fetchSortAndSlice(msgIds, sortField, order, fields, indexRange, headerNames);
    }

    private MailMessage[] performInAppSearch(MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailFields usedFields, IndexRange indexRange, String[] headerNames, int messageCount) throws MessagingException, OXException {
        int[] seqnums = null;
        if (searchTerm != null) {
            MailFields mailFields = new MailFields(MailField.getMailFieldsFromSearchTerm(searchTerm));
            int chunkSize = -1;
            if (mailFields.contains(MailField.BODY) || mailFields.contains(MailField.FULL)) {
                chunkSize = 100;
            }

            seqnums = IMAPSearch.searchByTerm(imapFolder, searchTerm, chunkSize, messageCount);
        }

        return fetchSortAndSlice(seqnums, sortField, order, usedFields, indexRange, headerNames);
    }

    private MailMessage[] fetchSortAndSlice(int[] seqnums, MailSortField sortField, OrderDirection order, MailFields fields, IndexRange indexRange, String[] headerNames) throws OXException, MessagingException {
        boolean fastFetch = getIMAPProperties().isFastFetch();
        boolean hasIMAP4rev1 = imapConfig.getImapCapabilities().hasIMAP4rev1();
        char separator = getSeparator(imapFolder);

        if (null == indexRange) {
            // Fetch them all
            FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, fastFetch);
            List<MailMessage> list;
            boolean fetchBody = fields.contains(MailField.BODY) || fields.contains(MailField.FULL);
            if (fetchBody) {
                list = fetchMessages(seqnums, fetchProfile);
            } else {
                MailMessage[] tmp = fetchMessages(seqnums, fetchProfile, hasIMAP4rev1, separator);
                list = new ArrayList<MailMessage>(tmp.length);
                for (MailMessage mailMessage : tmp) {
                    if (null != mailMessage) {
                        list.add(mailMessage);
                    }
                }
            }

            if (list.isEmpty()) {
                return EMPTY_RETVAL;
            }

            // Sort
            Collections.sort(list, new MailMessageComparator(sortField, order == OrderDirection.DESC, getLocale()));

            // Return
            MailMessage[] mailMessages = list.toArray(new MailMessage[list.size()]);
            return mailMessages.length > 0 ? mailMessages : EMPTY_RETVAL;
        }

        // A certain range is requested, thus grab messages only with ID and sort field information
        List<MailMessage> list;
        {
            FetchProfile fp = getFetchProfile(new MailField[] { MailField.ID, MailField.toField(sortField.getListField()) }, fastFetch);
            MailMessage[] mailMessages = fetchMessages(seqnums, fp, hasIMAP4rev1, separator);

            list = new ArrayList<MailMessage>(mailMessages.length);
            for (MailMessage mailMessage : mailMessages) {
                if (null != mailMessage) {
                    list.add(mailMessage);
                }
            }
        }

        if (list.isEmpty()) {
            return EMPTY_RETVAL;
        }

        // Sort them
        Collections.sort(list, new MailMessageComparator(sortField, order == OrderDirection.DESC, getLocale()));

        // Apply index range
        list = applyIndexRange(list, indexRange);

        // Determine UIDs
        long[] uids = new long[list.size()];
        int i = 0;
        for (MailMessage mailMessage : list) {
            uids[i++] = ((IDMailMessage) mailMessage).getUid();
        }

        // Fetch with proper attributes by UID
        FetchProfile fetchProfile = getFetchProfile(fields.toArray(), headerNames, null, null, fastFetch);
        MailMessage[] mailMessages;
        boolean fetchBody = fields.contains(MailField.BODY) || fields.contains(MailField.FULL);
        if (fetchBody) {
            List<MailMessage> tmp = fetchMessages(uids, fetchProfile);
            mailMessages = tmp.toArray(new MailMessage[tmp.size()]);
        } else {
            mailMessages = fetchMessages(uids, fetchProfile, hasIMAP4rev1, separator);
        }
        setAccountInfo(mailMessages);
        return mailMessages;
    }

    private MailMessage[] fetchMessages(long[] uids, FetchProfile fetchProfile, boolean hasIMAP4rev1, char separator) throws MessagingException {
        try {
            long start = System.currentTimeMillis();
            MailMessage[] mailMessages = new MailMessageFetchIMAPCommand(imapFolder, separator, hasIMAP4rev1, uids, fetchProfile, imapServerInfo).doCommand();
            long time = System.currentTimeMillis() - start;
            mailInterfaceMonitor.addUseTime(time);
            LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(uids.length), Long.valueOf(time));
            return mailMessages;
        } catch (MessagingException e) {
            if (!MimeMailException.isCommandFailedException(e)) {
                throw e;
            }

            // Chunk-wise
            List<MailMessage> l = new LinkedList<MailMessage>();
            int length = uids.length;
            int chunkSize = 25;

            int off = 0;
            while (off < length) {
                int end = off + chunkSize;
                long[] muids;

                if (end > length) {
                    end = length;
                    muids = new long[end - off];
                } else {
                    muids = new long[chunkSize];
                }

                System.arraycopy(uids, off, muids, 0, muids.length);

                long start = System.currentTimeMillis();
                MailMessage[] mms = new MailMessageFetchIMAPCommand(imapFolder, separator, hasIMAP4rev1, muids, fetchProfile, imapServerInfo).doCommand();
                long time = System.currentTimeMillis() - start;
                mailInterfaceMonitor.addUseTime(time);
                LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(muids.length), Long.valueOf(time));

                for (MailMessage mail : mms) {
                    l.add(mail);
                }

                off = end;
            }

            return l.toArray(new MailMessage[l.size()]);
        }
    }

    private MailMessage[] fetchMessages(int[] seqnums, FetchProfile fetchProfile, boolean hasIMAP4rev1, char separator) throws MessagingException {
        try {
            long start = System.currentTimeMillis();
            MailMessage[] mailMessages;
            if (null == seqnums) {
                mailMessages = new MailMessageFetchIMAPCommand(imapFolder, separator, hasIMAP4rev1, fetchProfile, imapServerInfo).doCommand();
            } else {
                mailMessages = new MailMessageFetchIMAPCommand(imapFolder, separator, hasIMAP4rev1, seqnums, fetchProfile, imapServerInfo).doCommand();
            }
            long time = System.currentTimeMillis() - start;
            mailInterfaceMonitor.addUseTime(time);
            LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(mailMessages.length), Long.valueOf(time));
            return mailMessages;
        } catch (MessagingException e) {
            if (!MimeMailException.isCommandFailedException(e)) {
                throw e;
            }

            // Chunk-wise
            List<MailMessage> l = new LinkedList<MailMessage>();
            int length = null == seqnums ? imapFolder.getMessageCount() : seqnums.length;
            int chunkSize = 25;

            int off = 0;
            while (off < length) {
                int end = off + chunkSize;
                int[] mseqnums;

                if (end > length) {
                    end = length;
                    mseqnums = new int[end - off];
                } else {
                    mseqnums = new int[chunkSize];
                }

                if (null == seqnums) {
                    for (int i = mseqnums.length, v = end; i-- > 0;) {
                        mseqnums[i] = v--;
                    }
                } else {
                    System.arraycopy(seqnums, off, mseqnums, 0, mseqnums.length);
                }

                long start = System.currentTimeMillis();
                MailMessage[] mms = new MailMessageFetchIMAPCommand(imapFolder, separator, hasIMAP4rev1, mseqnums, fetchProfile, imapServerInfo).doCommand();
                long time = System.currentTimeMillis() - start;
                mailInterfaceMonitor.addUseTime(time);
                LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(mseqnums.length), Long.valueOf(time));

                for (MailMessage mail : mms) {
                    l.add(mail);
                }

                off = end;
            }

            return l.toArray(new MailMessage[l.size()]);
        }
    }

    /**
     * Pre-fetches a set of messages based on the given {@link FetchProfile} and
     * converts them to {@link MailMessage}s which are backed by their original {@link Message}
     * objects.
     *
     * @param uids The UIDs to fetch or <code>null</code> to fetch all mails
     * @param fetchProfile
     * @return
     * @throws MessagingException
     * @throws OXException
     */
    private List<MailMessage> fetchMessages(long[] uids, FetchProfile fetchProfile) throws MessagingException, OXException {
        return fetchMessages(uids == null ? imapFolder.getMessages() : imapFolder.getMessagesByUID(uids), fetchProfile);
    }

    /**
     * Pre-fetches a set of messages based on the given {@link FetchProfile} and
     * converts them to {@link MailMessage}s which are backed by their original {@link Message}
     * objects.
     *
     * @param msgIds The IDs to fetch or <code>null</code> to fetch all mails
     * @param fetchProfile
     * @return
     * @throws MessagingException
     * @throws OXException
     */
    private List<MailMessage> fetchMessages(int[] msgIds, FetchProfile fetchProfile) throws MessagingException, OXException {
        return fetchMessages(msgIds == null ? imapFolder.getMessages() : imapFolder.getMessages(msgIds), fetchProfile);
    }

    private List<MailMessage> fetchMessages(Message[] messages, FetchProfile fetchProfile) throws MessagingException, OXException {
        Message[] msgs = messages;
        try {
            long start = System.currentTimeMillis();
            imapFolder.fetch(msgs, fetchProfile);
            long time = System.currentTimeMillis() - start;
            mailInterfaceMonitor.addUseTime(time);
            LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(msgs.length), Long.valueOf(time));
        } catch (MessagingException e) {
            if (!MimeMailException.isCommandFailedException(e)) {
                throw e;
            }

            // Chunk-wise
            List<Message> l = new LinkedList<Message>();
            int length = msgs.length;
            int chunkSize = 25;

            int off = 0;
            while (off < length) {
                int end = off + chunkSize;
                Message[] mmsg;

                if (end > length) {
                    end = length;
                    mmsg = new Message[end - off];
                } else {
                    mmsg = new Message[chunkSize];
                }

                System.arraycopy(msgs, off, mmsg, 0, mmsg.length);

                long start = System.currentTimeMillis();
                imapFolder.fetch(mmsg, fetchProfile);
                long time = System.currentTimeMillis() - start;
                mailInterfaceMonitor.addUseTime(time);
                LOG.debug("IMAP fetch for {} messages took {}msec", Integer.valueOf(mmsg.length), Long.valueOf(time));

                for (Message m : mmsg) {
                    if (null != m) {
                        l.add(m);
                    }
                }

                off = end;
            }

            msgs = l.toArray(new Message[l.size()]);
        }

        if (msgs.length == 0) {
            return Collections.emptyList();
        }

        int unreadMessages = IMAPCommandsCollection.getUnread(imapFolder);
        List<MailMessage> list = new ArrayList<MailMessage>(msgs.length);
        for (Message message : msgs) {
            if (message != null && !message.isExpunged()) {
                MailMessage mailMessage = toMailMessage(getMailAccount(), imapFolder.getFullName(), message, unreadMessages);
                if (mailMessage != null) {
                    list.add(mailMessage);
                }
            }
        }

        return list;
    }

    private int[] applyIndexRange(final int[] sortSeqNums, final IndexRange indexRange) {
        if (indexRange == null) {
            return sortSeqNums;
        }

        if (sortSeqNums.length == 0) {
            return new int[0];
        }

        int fromIndex = indexRange.start;
        if ((fromIndex) > sortSeqNums.length) {
            /*
             * Return empty iterator if start is out of range
             */
            return new int[0];
        }

        int toIndex = indexRange.end;
        /*
         * Reset end index if out of range
         */
        if (toIndex >= sortSeqNums.length) {
            toIndex = sortSeqNums.length;
        }

        int retvalLength = toIndex - fromIndex;
        int[] retval = new int[retvalLength];
        System.arraycopy(sortSeqNums, fromIndex, retval, 0, retvalLength);
        return retval;
    }

    private List<MailMessage> applyIndexRange(final List<MailMessage> mails, final IndexRange indexRange) {
        if (indexRange == null) {
            return mails;
        }
        if (mails == null) {
            return Collections.emptyList();
        }
        int size = mails.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        final int fromIndex = indexRange.start;
        int toIndex = indexRange.end;
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
        return mails.subList(fromIndex, toIndex);
    }

    private MailSortField determineSortFieldForSearch(final String fullName, final MailSortField requestedSortField) throws OXException {
        final MailSortField effectiveSortField;
        if (null == requestedSortField) {
            effectiveSortField = MailSortField.RECEIVED_DATE;
        } else {
            if (MailSortField.SENT_DATE.equals(requestedSortField)) {
                final String draftsFullname = imapAccess.getFolderStorage().getDraftsFolder();
                if (fullName.equals(draftsFullname)) {
                    effectiveSortField = MailSortField.RECEIVED_DATE;
                } else {
                    effectiveSortField = requestedSortField;
                }
            } else {
                effectiveSortField = requestedSortField;
            }
        }

        return effectiveSortField;
    }

    /**
     * Converts a {@link Message} to a {@link MailMessage} that is backed by the original object.
     * @param mailAccount
     * @param fullName
     * @param message
     * @param unreadMessages
     * @return
     * @throws MessagingException
     * @throws OXException
     */
    private MailMessage toMailMessage(final MailAccount mailAccount, final String fullName, final Message message, final int unreadMessages) throws MessagingException, OXException {
        final IMAPMessage imapMessage = (IMAPMessage) message;
        final long msgUID = imapFolder.getUID(message);
        imapMessage.setUID(msgUID);
        imapMessage.setPeek(true);
        final MailMessage mail;
        try {
            mail = MimeMessageConverter.convertMessage(imapMessage, false);
            mail.setFolder(fullName);
            mail.setMailId(Long.toString(msgUID));
            mail.setUnreadMessages(unreadMessages);
            mail.setAccountId(mailAccount.getId());
            mail.setAccountName(mailAccount.getName());
            return mail;
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
                LOG.debug("Generic messaging error occurred for mail \"{}\" in folder \"{}\" with login \"{}\" on server \"{}\" (user={}, context={})", Long.valueOf(msgUID), fullName, imapConfig.getLogin(), imapConfig.getServer(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
            }
            throw e;
        } catch (final java.lang.IndexOutOfBoundsException e) {
            /*
             * Obviously message was removed in the meantime
             */
            return null;
        }
    }

    @Override
    public List<List<MailMessage>> getThreadSortedMessages(String fullName, boolean includeSent, boolean cache, IndexRange indexRange, long max, MailSortField sortField, OrderDirection order, MailField[] mailFields) throws OXException {
        IMAPConversationWorker conversationWorker = new IMAPConversationWorker(this, imapFolderStorage);
        return conversationWorker.getThreadSortedMessages(fullName, includeSent, cache, indexRange, max, sortField, order, mailFields);
    }

    @Override
    public MailMessage[] getThreadSortedMessages(String fullName, IndexRange indexRange, MailSortField sortField, OrderDirection order, SearchTerm<?> searchTerm, MailField[] mailFields) throws OXException {
        IMAPConversationWorker conversationWorker = new IMAPConversationWorker(this, imapFolderStorage);
        return conversationWorker.getThreadSortedMessages(fullName, indexRange, sortField, order, searchTerm, mailFields);
    }

    @Override
    public MailMessage[] getUnreadMessages(final String fullName, final MailSortField sortField, final OrderDirection order, final MailField[] mailFields, final int limit) throws OXException {
        try {
            openReadOnly(fullName);
            MailMessage[] mails;
            {
                /*
                 * Ensure mail ID is contained in requested fields
                 */
                final MailFields fieldSet = new MailFields(mailFields);
                IMAPMessageStorage.prepareMailFieldsForVirtualFolder(fieldSet, fullName);
                final MailField[] fields = fieldSet.toArray();
                /*
                 * Get ( & fetch) new messages
                 */
                Message[] msgs = IMAPCommandsCollection.getUnreadMessages(imapFolder, fields, sortField, order, getIMAPProperties().isFastFetch(), limit, imapServerInfo, session);
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
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            clearCache(imapFolder);
        }
    }

    @Override
    public void deleteMessagesLong(final String fullName, final long[] msgUIDs, final boolean hardDelete) throws OXException {
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights( imapFolder, true, session, accountId))) {
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
                if (hardDelete || getUserSettingMail().isHardDeleteMsgs()) {
                    blockwiseDeletion(msgUIDs, false, null);
                    notifyIMAPFolderModification(fullName);
                    return;
                }
                final String trashFullname = imapAccess.getFolderStorage().getTrashFolder();
                if (null == trashFullname) {
                    LOG.error("\n\tDefault trash folder is not set: aborting delete operation");
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
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private void blockwiseDeletion(final long[] msgUIDs, final boolean backup, final String trashFullname) throws OXException, MessagingException {
        if (0 == msgUIDs.length) {
            // Nothing to do on empty ID array
            return;
        }
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
                deleteByUIDs(trashFullname, backup, tmp);
            }
            remain = new long[msgUIDs.length - offset];
            System.arraycopy(msgUIDs, offset, remain, 0, remain.length);
        } else {
            remain = msgUIDs;
        }
        deleteByUIDs(trashFullname, backup, remain);
        /*
         * Close folder to force JavaMail-internal message cache update
         */
        imapFolder.close(false);
        resetIMAPFolder();
    }

    private void deleteByUIDs(final String trashFullname, final boolean backup, final long[] uids) throws OXException, MessagingException {
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
                command.doCommand();
            } catch (final MessagingException e) {
                final String err = com.openexchange.java.Strings.toLowerCase(e.getMessage());
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
                if (nestedExc != null && com.openexchange.java.Strings.toLowerCase(nestedExc.getMessage()).indexOf("quota") >= 0) {
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
        new FlagsIMAPCommand(imapFolder, uids, FLAGS_DELETED, true, true, false).doCommand();
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
    public void copyMessages(String sourceFolder, String destFolder) throws OXException {
        copyOrMoveAllMessages(sourceFolder, destFolder, false);
    }

    @Override
    public void moveMessages(String sourceFolder, String destFolder) throws OXException {
        copyOrMoveAllMessages(sourceFolder, destFolder, true);
    }

    private void copyOrMoveAllMessages(String sourceFullName, String destFullName, boolean move) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(destFullName)) {
            throw IMAPException.create(IMAPException.Code.NO_ROOT_MOVE, imapConfig, session, new Object[0]);
        }
        if ((sourceFullName == null) || (sourceFullName.length() == 0)) {
            throw IMAPException.create(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, imapConfig, session, "source");
        } else if ((destFullName == null) || (destFullName.length() == 0)) {
            throw IMAPException.create(IMAPException.Code.MISSING_SOURCE_TARGET_FOLDER_ON_MOVE, imapConfig, session, "target");
        } else if (sourceFullName.equals(destFullName) && move) {
            // Source equals destination, just return the message ids without throwing an exception or doing anything
            return;
        }

        try {
            /*
             * Open and check user rights on source folder
             */
            try {
                imapFolder = setAndOpenFolder(imapFolder, sourceFullName, move ? READ_WRITE : READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(sourceFullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, sourceFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
                }
                if (move && imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
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
                    final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(destFullName, accountId, destFolder, session, this.ignoreSubscriptions);
                    if (!"INBOX".equals(destFullName) && !listEntry.exists()) {
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

            if (imapFolder.getMessageCount() <= 0) {
                // Folder is empty
                return;
            }

            boolean supportsMove = move && imapConfig.asMap().containsKey("MOVE");

            AbstractIMAPCommand<long[]> command;
            if (supportsMove) {
                command = new MoveIMAPCommand(imapFolder, destFullName);
            } else {
                command = new CopyIMAPCommand(imapFolder, destFullName);
            }
            command.doCommand();

            if (move && !supportsMove) {
                new FlagsIMAPCommand(imapFolder, FLAGS_DELETED, true, true).doCommand();
                IMAPCommandsCollection.fastExpunge(imapFolder);
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(sourceFullName, e);
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
            /*
             * Open and check user rights on source folder
             */
            try {
                imapFolder = setAndOpenFolder(imapFolder, sourceFullName, move ? READ_WRITE : READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(sourceFullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, sourceFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
                }
                if (move && imapConfig.isSupportsACLs() && !aclExtension.canDeleteMessages(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
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
                    final ListLsubEntry listEntry = ListLsubCache.getCachedLISTEntry(destFullName, accountId, destFolder, session, this.ignoreSubscriptions);
                    if (!"INBOX".equals(destFullName) && !listEntry.exists()) {
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
                int offset = 0;
                final long[] remain;
                if (blockSize > 0 && mailIds.length > blockSize) {
                    /*
                     * Block-wise deletion
                     */
                    final long[] tmp = new long[blockSize];
                    for (int len = mailIds.length; len > blockSize; len -= blockSize) {
                        System.arraycopy(mailIds, offset, tmp, 0, tmp.length);
                        final long[] uids = copyOrMoveByUID(move, fast, destFullName, tmp);
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
                final long[] uids = copyOrMoveByUID(move, fast, destFullName, remain);
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
                            final long start = System.currentTimeMillis();
                            new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, true, true).doCommand();
                            LOG.debug("A copy/move to default drafts folder => All messages' \\Draft flag in {} set in {}msec", destFullName, Long.valueOf(System.currentTimeMillis() - start));
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
                        final long start = System.currentTimeMillis();
                        new FlagsIMAPCommand(destFolder, FLAGS_DRAFT, false, true).doCommand();
                        LOG.debug("A copy/move from default drafts folder => All messages' \\Draft flag in {} unset in {}msec", destFullName, Long.valueOf(System.currentTimeMillis() - start));
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
            throw handleMessagingException(sourceFullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private long[] copyOrMoveByUID(final boolean move, final boolean fast, final String destFullName, final long[] tmp) throws MessagingException, OXException, IMAPException {
        final boolean supportsMove = move && imapConfig.asMap().containsKey("MOVE");
        final AbstractIMAPCommand<long[]> command;
        if (supportsMove) {
            command = new MoveIMAPCommand(imapFolder, tmp, destFullName, false, fast);
        } else {
            command = new CopyIMAPCommand(imapFolder, tmp, destFullName, false, fast);
        }
        long[] uids = command.doCommand();
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
            new FlagsIMAPCommand(imapFolder, tmp, FLAGS_DELETED, true, true, false).doCommand();
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
    public boolean isMimeSupported() {
        return true;
    }

    @Override
    public String[] appendMimeMessages(final String destFullName, final Message[] msgs) throws OXException {
        if (null == msgs) {
            return new String[0];
        }
        final int length = msgs.length;
        if (length == 0) {
            return new String[0];
        }
        try {
            /*
             * Open and check user rights on source folder
             */
            try {
                imapFolder = setAndOpenFolder(imapFolder, destFullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(destFullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, destFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
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
                if (filteredMsgs.isEmpty()) {
                    return new String[0];
                }
                final String hash = randomUUID();
                /*
                 * Try to set marker header
                 */
                try {
                    Message message = filteredMsgs.get(0);
                    /*
                     * Check for empty content
                     */
                    AppendEmptyMessageTracer.checkForEmptyMessage(message, destFullName, imapConfig);
                    /*
                     * Set marker
                     */
                    message.setHeader(MessageHeaders.HDR_X_OX_MARKER, fold(13, hash));
                } catch (final Exception e) {
                    // Is read-only -- create a copy from first message
                    LOG.trace("", e);
                    final MimeMessage newMessage;
                    final Message removed = filteredMsgs.remove(0);
                    if (removed instanceof ReadableMime) {
                        newMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), ((ReadableMime) removed).getMimeStream());
                        newMessage.setFlags(removed.getFlags(), true);
                    } else {
                        newMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), MimeMessageUtility.getStreamFromPart(removed));
                        newMessage.setFlags(removed.getFlags(), true);
                    }
                    newMessage.setHeader(MessageHeaders.HDR_X_OX_MARKER, fold(13, hash));
                    filteredMsgs.add(0, newMessage);
                }
                /*
                 * ... and append them to folder
                 */
                String[] newUids = null;
                final boolean hasUIDPlus = imapConfig.getImapCapabilities().hasUIDPlus();
                try {
                    if (hasUIDPlus) {
                        // Perform append expecting APPENUID response code
                        newUids = longs2uids(checkAndConvertAppendUID(imapFolder.appendUIDMessages(filteredMsgs.toArray(new Message[filteredMsgs.size()]))));
                    } else {
                        // Perform simple append
                        imapFolder.appendMessages(filteredMsgs.toArray(new Message[filteredMsgs.size()]));
                    }
                } catch (final MessagingException e) {
                    final Exception nextException = e.getNextException();
                    if (nextException instanceof com.sun.mail.iap.CommandFailedException) {
                        throw IMAPException.create(IMAPException.Code.INVALID_MESSAGE, imapConfig, session, e, new Object[0]);
                    }
                    throw e;
                }
                if (null != newUids && newUids.length > 0) {
                    /*
                     * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
                     */
                    notifyIMAPFolderModification(destFullName);
                    if (newUids.length >= length) {
                        return newUids;
                    }
                    final String[] longs = new String[length];
                    for (int i = 0, k = 0; i < length; i++) {
                        if (null != msgs[i]) {
                            longs[i] = newUids[k++];
                        }
                    }
                    return longs;
                }
                /*-
                 * OK, go the long way:
                 * 1. Find the marker in folder's messages
                 * 2. Get the UIDs from found message's position
                 */
                if (hasUIDPlus) {
                    /*
                     * Missing UID information in APPENDUID response
                     */
                    LOG.warn("Missing UID information in APPENDUID response");
                }
                newUids = new String[msgs.length];
                {
                    final long[] uids = IMAPCommandsCollection.findMarker(hash, newUids.length, imapFolder);
                    final int uLen = uids.length;
                    if (uLen == 0) {
                        Arrays.fill(newUids, null);
                    } else {
                        for (int i = 0; i < uLen; i++) {
                            newUids[i] = Long.toString(uids[i]);
                        }
                    }
                }
                /*
                 * Close affected IMAP folder to ensure consistency regarding IMAFolder's internal cache.
                 */
                notifyIMAPFolderModification(destFullName);
                if (newUids.length >= length) {
                    return newUids;
                }
                final String[] longs = new String[length];
                for (int i = 0, k = 0; i < length; i++) {
                    if (null != msgs[i]) {
                        longs[i] = newUids[k++];
                    }
                }
                return longs;
            } finally {
                if (marked) {
                    unsetMarker(opKey);
                }
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(destFullName, e);
        } catch (final MessageRemovedIOException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
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
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(destFullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, destFullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
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
                    MailMessage m = mailMessages[0];
                    if (null != m) {
                        msgs[0] = asMessage(m, MimeMessageConverter.BEHAVIOR_CLONE);
                    }
                }
                for (int i = 1; i < length; i++) {
                    MailMessage m = mailMessages[i];
                    if (null != m) {
                        msgs[i] = asMessage(m, MimeMessageConverter.BEHAVIOR_CLONE | MimeMessageConverter.BEHAVIOR_STREAM2FILE);
                    }
                }
                /*
                 * Check if destination folder supports user flags
                 */
                boolean supportsUserFlags = UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId);
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
                if (filteredMsgs.isEmpty()) {
                    return new long[0];
                }
                final String hash = randomUUID();
                try {
                    Message message = filteredMsgs.get(0);
                    /*
                     * Check for empty content
                     */
                    AppendEmptyMessageTracer.checkForEmptyMessage(message, destFullName, imapConfig);
                    /*
                     * Set marker
                     */
                    message.setHeader(MessageHeaders.HDR_X_OX_MARKER, fold(13, hash));
                } catch (final Exception e) {
                    // Is read-only -- create a copy from first message
                    LOG.trace("", e);
                    final MimeMessage newMessage;
                    final Message removed = filteredMsgs.remove(0);
                    if (removed instanceof ReadableMime) {
                        newMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), ((ReadableMime) removed).getMimeStream());
                        newMessage.setFlags(removed.getFlags(), true);
                    } else {
                        newMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), MimeMessageUtility.getStreamFromPart(removed));
                        newMessage.setFlags(removed.getFlags(), true);
                    }
                    newMessage.setHeader(MessageHeaders.HDR_X_OX_MARKER, fold(13, hash));
                    filteredMsgs.add(0, newMessage);
                }
                /*
                 * ... and append them to folder
                 */
                long[] retval = null;
                final boolean hasUIDPlus = imapConfig.getImapCapabilities().hasUIDPlus();
                try {
                    if (hasUIDPlus) {
                        // Perform append expecting APPENDUID response code
                        retval = checkAndConvertAppendUID(imapFolder.appendUIDMessages(filteredMsgs.toArray(new Message[filteredMsgs.size()])));
                    } else {
                        // Perform simple append
                        imapFolder.appendMessages(filteredMsgs.toArray(new Message[filteredMsgs.size()]));
                    }
                } catch (final MessagingException e) {
                    OXException oxe = handleMessagingException(destFullName, e);
                    if (MimeMailExceptionCode.PROCESSING_ERROR.equals(oxe)) {
                        throw IMAPException.create(IMAPException.Code.INVALID_MESSAGE, imapConfig, session, e, new Object[0]);
                    }
                    throw oxe;
                }
                if (null != retval && retval.length > 0) {
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
                if (hasUIDPlus) {
                    /*
                     * Missing UID information in APPENDUID response
                     */
                    LOG.warn("Missing UID information in APPENDUID response for folder {} from IMAP server {} using login {}", imapFolder.getFullName(), imapConfig.getServer(), imapConfig.getLogin());
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
            if (LOG.isDebugEnabled()) {
                final Exception next = e.getNextException();
                if (next instanceof CommandFailedException) {
                    final StringBuilder sb = new StringBuilder(8192);
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
            throw handleMessagingException(destFullName, e);
        } catch (final MessageRemovedIOException e) {
            throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            if (null != msgs) {
                for (final Message message : msgs) {
                    if (message instanceof MimeCleanUp) {
                        ((MimeCleanUp) message).cleanUp();
                    }
                }
            }
        }
    }

    private Message asMessage(MailMessage m, int behavior) throws OXException {
        if (m instanceof MimeRawSource) {
            Part part = ((MimeRawSource) m).getPart();
            if (part instanceof Message) {
                return (Message) part;
            }
            return MimeMessageConverter.convertMailMessage(m, behavior);
        }
        return MimeMessageConverter.convertMailMessage(m, behavior);
    }


    @Override
    public void updateMessageFlagsLong(final String fullName, final long[] msgUIDs, final int flagsArg, final boolean set) throws OXException {
        updateMessageFlagsLong(fullName, msgUIDs, flagsArg, ArrayUtils.EMPTY_STRING_ARRAY, set);
    }

    @Override
    public void updateMessageFlagsLong(final String fullName, final long[] msgUIDs, final int flagsArg, String[] userFlags, final boolean set) throws OXException {
        if (null == msgUIDs || 0 == msgUIDs.length) {
            // Nothing to do
            return;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            // final OperationKey opKey = new OperationKey(Type.MSG_FLAGS_UPDATE, accountId, new Object[] { fullName });
            // final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove non user-alterable system flags
                 */
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
                    } else {
                        LOG.debug(
                            "IMAP server {} does not support user flags. Skipping forwarded flag.",
                            imapConfig.getImapServerSocketAddress());
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
                    } else {
                        LOG.debug("IMAP server {} does not support user flags. Skipping read-ack flag.", imapConfig.getImapServerSocketAddress());
                    }
                }

                for (String userFlag : userFlags) {
                    affectedFlags.add(userFlag);
                }

                applyFlags = ArrayUtils.isEmpty(userFlags) ? applyFlags : true;

                if (applyFlags) {
                    final long start = System.currentTimeMillis();
                    new FlagsIMAPCommand(imapFolder, msgUIDs, affectedFlags, set, true, false).doCommand();
                    LOG.debug("Flags applied to {} messages in {}msec", Integer.valueOf(msgUIDs.length), Long.valueOf(System.currentTimeMillis() - start));
                }
                /*
                 * Check for spam action
                 */
                if (getUserSettingMail().isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
                    handleSpamByUID(msgUIDs, set, true, fullName, READ_WRITE);
                } else {
                    /*
                     * Force JavaMail's cache update through folder closure
                     */
                    imapFolder.close(false);
                    resetIMAPFolder();
                }
            } finally {
                // if (marked) {
                //    unsetMarker(opKey);
                // }
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void updateMessageFlags(final String fullName, final int flagsArg, final boolean set) throws OXException {
        updateMessageFlags(fullName, flagsArg, ArrayUtils.EMPTY_STRING_ARRAY, set);
    }

    @Override
    public void updateMessageFlags(final String fullName, final int flagsArg, String[] userFlags, final boolean set) throws OXException {
        if (null == fullName) {
            // Nothing to do
            return;
        }
        try {
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            final OperationKey opKey = new OperationKey(Type.MSG_FLAGS_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove non user-alterable system flags
                 */
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
                    } else {
                        LOG.debug("IMAP server {} does not support user flags. Skipping forwarded flag.", imapConfig.getImapServerSocketAddress());
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
                    } else {
                        LOG.debug("IMAP server {0} does not support user flags. Skipping read-ack flag.", imapConfig.getImapServerSocketAddress());
                    }
                }

                for (String userFlag : userFlags) {
                    affectedFlags.add(userFlag);
                }

                applyFlags = ArrayUtils.isEmpty(userFlags) ? applyFlags : true;
                if (applyFlags) {
                    final long start = System.currentTimeMillis();
                    new FlagsIMAPCommand(imapFolder, affectedFlags, set, true).doCommand();
                    LOG.debug("Flags applied to all messages in {}msec", Long.valueOf(System.currentTimeMillis() - start));
                }
                /*
                 * Check for spam action
                 */
                if (getUserSettingMail().isSpamEnabled() && ((flags & MailMessage.FLAG_SPAM) > 0)) {
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
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public void updateMessageUserFlagsLong(String fullName, long[] mailIds, String[] flags, boolean set) throws OXException {
        if (null == mailIds || 0 == mailIds.length) {
            // Nothing to do
            return;
        }
        try {
            if (!MailProperties.getInstance().isUserFlagsEnabled()) {
                /*
                 * User flags are disabled
                 */
                LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
                return;
            }
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, fullName);
            }
            try {
                if (!holdsMessages()) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, imapFolder.getFullName());
                }
                if (imapConfig.isSupportsACLs() && !aclExtension.canWrite(RightsCache.getCachedRights(imapFolder, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_WRITE_ACCESS, imapConfig, session, imapFolder.getFullName());
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, imapFolder.getFullName());
            }
            if (!UserFlagsCache.supportsUserFlags(imapFolder, true, session, accountId)) {
                LOG.error("Folder \"{}\" does not support user-defined flags. Update of color flag ignored.", imapFolder.getFullName());
                return;
            }
            final OperationKey opKey = new OperationKey(Type.MSG_USER_FLAGS_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove all old color label flag(s) and set new color label flag
                 */
                IMAPCommandsCollection.setUserFlags(imapFolder, mailIds, flags, set);

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
            throw handleMessagingException(fullName, e);
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
                LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
                return;
            }
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
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
                LOG.error("Folder \"{}\" does not support user-defined flags. Update of color flag ignored.", imapFolder.getFullName());
                return;
            }
            final OperationKey opKey = new OperationKey(Type.MSG_LABEL_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove all old color label flag(s) and set new color label flag
                 */
                IMAPCommandsCollection.clearAndSetColorLabelSafely(imapFolder, msgUIDs, MailMessage.getColorLabelStringValue(colorLabel));

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
            throw handleMessagingException(fullName, e);
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
                LOG.debug("User flags are disabled or not supported. Update of color flag ignored.");
                return;
            }
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_WRITE);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
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
                LOG.error("Folder \"{}\" does not support user-defined flags. Update of color flag ignored.", imapFolder.getFullName());
                return;
            }
            final OperationKey opKey = new OperationKey(Type.MSG_LABEL_UPDATE, accountId, new Object[] { fullName });
            final boolean marked = setMarker(opKey);
            try {
                /*
                 * Remove all old color label flag(s) and set new color label flag
                 */

                IMAPCommandsCollection.clearAndSetColorLabelSafely(imapFolder, null, MailMessage.getColorLabelStringValue(colorLabel));
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
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullName, final ComposedMailMessage composedMail) throws OXException {
        try {
            MimeMessage mimeMessage = new MimeMessage(imapAccess.getMailSession());
            /*
             * Fill message
             */
            long uid;
            try {
                UserSettingMail customSettings = composedMail.getMailSettings();
                MimeMessageFiller filler = null == customSettings ? new MimeMessageFiller(session, ctx) : new MimeMessageFiller(session, ctx, customSettings);
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
                        LOG.warn("Differing account ID in msgref attribute.\nMessage storage account ID: {}.\nmsgref account ID: {}", Integer.valueOf(accountId), Integer.valueOf(msgref.getAccountId()), new Throwable());
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
            throw handleMessagingException(draftFullName, e);
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
            // In use...
            throw MimeMailExceptionCode.IN_USE_ERROR_EXT.create(
                imapConfig.getServer(),
                imapConfig.getLogin(),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()),
                MimeMailException.appendInfo("Mailbox is currently in use.", imapFolder));
        }
        return result > 0;
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

    private void handleSpamByUID(final long[] msgUIDs, final boolean isSpam, final boolean move, final String fullName, final int desiredMode) throws OXException {
        /*
         * Check for spam handling
         */
        if (getUserSettingMail().isSpamEnabled()) {
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
                    final SpamHandler spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId, IMAPProvider.getInstance());
                    spamHandler.handleSpam(accountId, imapFolder.getFullName(), longs2uids(msgUIDs), move, session);
                    /*
                     * Close and reopen to force internal message cache update
                     */
                    resetIMAPFolder();
                    try {
                        imapFolder = setAndOpenFolder(imapFolder, fullName, desiredMode);
                    } catch (final MessagingException e) {
                        final Exception next = e.getNextException();
                        if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                            throw handleMessagingException(fullName, e);
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
                final SpamHandler spamHandler = SpamHandlerRegistry.getSpamHandlerBySession(session, accountId, IMAPProvider.getInstance());
                spamHandler.handleHam(
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
                    if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                        throw handleMessagingException(fullName, e);
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
    protected MailMessage setAccountInfo(final MailMessage mailMessage) throws OXException {
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
    protected MailMessage[] setAccountInfo(final MailMessage[] mailMessages) throws OXException {
        return setAccountInfo(mailMessages, -1);
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @param numberUnreadMessage The number of unread messages
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private MailMessage[] setAccountInfo(final MailMessage[] mailMessages, int numberUnreadMessage) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (int i = 0; i < mailMessages.length; i++) {
            final MailMessage mailMessage = mailMessages[i];
            if (null != mailMessage) {
                mailMessage.setAccountId(id);
                mailMessage.setAccountName(name);
                if (numberUnreadMessage > 0) {
                    mailMessage.setUnreadMessages(numberUnreadMessage);
                }
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
    protected <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(final W col) throws OXException {
        return setAccountInfo2(col, getMailAccount());
    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    protected static <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(final W col, MailAccount account) throws OXException {
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

    /**
     * Converts given MIME messages to {@link MailMessage} instances.
     *
     * @param msgs The MIME messages to convert
     * @param fields The fields to consider
     * @param includeBody Whether body is included
     * @return The {@link MailMessage} instances
     * @throws OXException If conversion fails
     */
    protected MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields, final boolean includeBody) throws OXException {
        return convert2Mails(msgs, fields, null, includeBody);
    }

    private MailMessage[] convert2Mails(final Message[] msgs, final MailField[] fields, final String[] headerNames, final boolean includeBody) throws OXException {
        return MimeMessageConverter.convertMessages(msgs, fields, headerNames, includeBody);
    }

    /**
     * Gets the separator character
     *
     * @param imapFolder The IMAP folder to use
     * @return The separator character
     * @throws OXException If separator character cannot be return
     * @throws MessagingException If a messaging error occurs
     */
    protected char getSeparator(final IMAPFolder imapFolder) throws OXException, MessagingException {
        return getLISTEntry("INBOX", imapFolder).getSeparator();
    }

    private ListLsubEntry getLISTEntry(final String fullName, final IMAPFolder imapFolder) throws OXException, MessagingException {
        return ListLsubCache.getCachedLISTEntry(fullName, accountId, imapFolder, session, this.ignoreSubscriptions);
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

    /**
     * Closes given IMAP folder safely.
     *
     * @param imapFolder The IMAP folder to close
     */
    protected static void closeSafe(final IMAPFolder imapFolder) {
        if (null != imapFolder) {
            try {
                imapFolder.close(false);
            } catch (final Exception e) {
                // Ignore
                LOG.trace("", e);
            }
        }
    }

    private static Map<String, Object> mapFor(String... pairs) {
        if (null == pairs) {
            return null;
        }
        int length = pairs.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(pairs[i], pairs[i+1]);
        }
        return map;
    }

    private static <E> List<E> filterNullElements(E[] elements) {
        if (null == elements) {
            return Collections.emptyList();
        }

        int length = elements.length;
        List<E> list = new ArrayList<E>(length);
        for (int i = 0; i < length; i++) {
            E elem = elements[i];
            if (null != elem) {
                list.add(elem);
            }
        }
        return list;
    }

    private static long[] filterNegativeElements(long[] uids) {
        if (null == uids) {
            return null;
        }

        int i = 0;
        boolean fine = true;
        while (fine && i < uids.length) {
            if (uids[i] < 0) {
                fine = false;
            } else {
                i++;
            }
        }

        if (fine) {
            return uids;
        }

        TLongList valids = new TLongArrayList(uids.length);
        valids.add(uids, 0, i);
        for (int j = i + 1; j < uids.length; j++) {
            long uid = uids[j];
            if (uid >= 0) {
                valids.add(uid);
            }
        }
        return valids.toArray();
    }

    /**
     * Checks given fetch profile to only contain fetch items and no single headers<br>
     * In case {@link IMAPProperties#allowFetchSingleHeaders()} signals <code>true</code>.
     *
     * @param fetchProfile The fetch profile to check
     * @return The checked fetch profile
     */
    protected static FetchProfile checkFetchProfile(FetchProfile fetchProfile) {
        if (null == fetchProfile || IMAPProperties.getInstance().allowFetchSingleHeaders()) {
            return fetchProfile;
        }

        FetchProfile newFetchProfile = new FetchProfile();
        for (final Item item : fetchProfile.getItems()) {
            newFetchProfile.add(item);
        }
        return newFetchProfile;
    }

    @Override
    public MailMessage[] getMessagesByMessageIDByFolder(String fullName, String... messageIDs) throws OXException {
        try {
            final int length = messageIDs.length;
            final MailMessage[] retval = new MailMessage[length];
            try {
                imapFolder = setAndOpenFolder(imapFolder, fullName, READ_ONLY);
            } catch (final MessagingException e) {
                final Exception next = e.getNextException();
                if ((null == next) || !(next instanceof com.sun.mail.iap.CommandFailedException) || (Strings.toUpperCase(next.getMessage()).indexOf("[NOPERM]") <= 0)) {
                    throw handleMessagingException(fullName, e);
                }
                throw IMAPException.create(IMAPException.Code.NO_FOLDER_OPEN, imapConfig, session, e, "INBOX");
            }
            final long[] uids = IMAPCommandsCollection.messageId2UID(imapFolder, messageIDs);
            if (uids.length == length) {
                for (int i = 0; i < uids.length; i++) {
                    final long uid = uids[i];
                    if (uid != -1) {
                        retval[i] = new IDMailMessage(String.valueOf(uid), fullName);
                    }
                }
            }
            return retval;
        } catch (final MessagingException e) {
            throw handleMessagingException(imapFolder.getFullName(), e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Gets the IMAP message storage instance from given mail access.
     *
     * @param mailAccess The connected mail access
     * @return The IMAP message storage or <code>null</code>
     * @throws OXException If IMAP message storage could not be extracted
     */
    protected static IMAPMessageStorage getImapMessageStorageFrom(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailMessageStorage mstore = mailAccess.getMessageStorage();
        if (!(mstore instanceof IMAPMessageStorage)) {
            if (!(mstore instanceof IMailMessageStorageDelegator)) {
                return null;
            }
            mstore = ((IMailMessageStorageDelegator) mstore).getDelegateMessageStorage();
            if (!(mstore instanceof IMAPMessageStorage)) {
                return null;
            }
        }
        return (IMAPMessageStorage) mstore;
    }

}
