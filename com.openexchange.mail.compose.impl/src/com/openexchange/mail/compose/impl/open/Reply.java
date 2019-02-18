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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.open;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.impl.attachment.AttachmentImageDataSource;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link Reply} - Utility class to open a composition space for a forward.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Reply extends AbstractOpener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Reply.class);
    }

    /**
     * Initializes a new {@link Reply}.
     */
    public Reply(AttachmentStorageService attachmentStorageService, ServiceLookup services) {
        super(attachmentStorageService, services);
    }

    private static final String PREFIX_RE = "Re: ";

    /**
     * Prepares opening a composition space for a reply.
     *
     * @param replyAll Whether a reply to all recipients should be prepared
     * @param parameters The parameters
     * @param state The state
     * @param session The session
     * @throws OXException If an Open-Xchange error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public void doOpenForReply(boolean replyAll, OpenCompositionSpaceParameters parameters, OpenState state, Session session) throws OXException, MessagingException {
        MailPath replyFor = parameters.getReferencedMails().get(0);
        state.metaBuilder.withReplyFor(replyFor);
        state.mailInterface = MailServletInterface.getInstanceWithDecryptionSupport(session, null);
        MailMessage originalMail = requireMailMessage(replyFor, state.mailInterface);
        state.metaBuilder.withDate(originalMail.getSentDate());

        Context context = getContext(session);
        UserSettingMail usm = parameters.getMailSettings();

        int accountId = replyFor.getAccountId();
        boolean preferToAsRecipient = false;
        {
            String originalMailFolder = replyFor.getFolder();
            String[] arr = MailSessionCache.getInstance(session).getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
            if (arr == null) {
                MailAccess<?, ?> mailAccess = state.mailInterface.getMailAccess();
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                preferToAsRecipient = originalMailFolder.equals(folderStorage.getSentFolder()) || originalMailFolder.equals(folderStorage.getDraftsFolder());
            } else {
                preferToAsRecipient = originalMailFolder.equals(arr[StorageUtility.INDEX_SENT]) || originalMailFolder.equals(arr[StorageUtility.INDEX_DRAFTS]);
            }
        }

        // Reply, pre-set subject
        String origSubject = originalMail.getSubject();
        if (Strings.isEmpty(origSubject)) {
            state.message.setSubject(PREFIX_RE);
        } else {
            if (origSubject.regionMatches(true, 0, PREFIX_RE, 0, PREFIX_RE.length())) {
                state.message.setSubject(origSubject);
            } else {
                state.message.setSubject(new StringBuilder(PREFIX_RE.length() + origSubject.length()).append(PREFIX_RE).append(origSubject).toString());
            }
        }

        // Determine "From"
        InternetAddress from = null;
        {
            FromAddressProvider fromAddressProvider = FromAddressProvider.byAccountId();
            if (null != fromAddressProvider) {
                if (fromAddressProvider.isDetectBy()) {
                    from = MimeProcessingUtility.determinePossibleFrom(false, originalMail, accountId, session, context);
                    /*
                     * Set if a "From" candidate applies
                     */
                    if (null != from) {
                        state.message.setFrom(toAddress(from));
                    }
                } else if (fromAddressProvider.isSpecified()) {
                    from = fromAddressProvider.getFromAddress();
                    if (null != from) {
                        state.message.setFrom(toAddress(from));
                    }
                }
            }
        }

        /*
         * Set the appropriate recipients. Taken from RFC 822 section 4.4.4: If the "Reply-To" field exists, then the reply should go to
         * the addresses indicated in that field and not to the address(es) indicated in the "From" field.
         */
        InternetAddress[] recipientAddrs;
        if (preferToAsRecipient) {
            recipientAddrs = originalMail.getTo();
        } else {
            Set<InternetAddress> tmpSet = new LinkedHashSet<InternetAddress>(4);
            boolean fromAdded;
            {
                String[] replyTo = originalMail.getHeader(MessageHeaders.HDR_REPLY_TO);
                if (MimeMessageUtility.isEmptyHeader(replyTo)) {
                    String owner = MimeProcessingUtility.getFolderOwnerIfShared(replyFor.getFolder(), replyFor.getAccountId(), session);
                    if (null != owner) {
                        final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                        if (null != users && users.length > 0) {
                            InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), false);
                            state.message.setFrom(toAddress(onBehalfOf));
                            QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), false);
                            state.message.setSender(toAddress(sender));
                        }
                    }
                    /*
                     * Set from as recipient
                     */
                    tmpSet.addAll(Arrays.asList(originalMail.getFrom()));
                    fromAdded = true;
                } else {
                    /*
                     * Message holds header 'Reply-To'
                     */
                    tmpSet.addAll(Arrays.asList(MimeMessageUtility.getAddressHeader(unfold(replyTo[0]))));
                    fromAdded = false;
                }
            }
            if (replyAll) {
                /*-
                 * Check 'From' has been added
                 */
                if (!fromAdded) {
                    tmpSet.addAll(Arrays.asList(originalMail.getFrom()));
                }
            }
            recipientAddrs = tmpSet.toArray(new InternetAddress[tmpSet.size()]);
        }

        if (replyAll) {
            /*
             * Create a filter which is used to sort out addresses before adding them to either field 'To' or 'Cc'
             */
            Set<InternetAddress> filter = new HashSet<InternetAddress>();
            if (null != from) {
                filter.add(from);
            }
            /*
             * Add user's address to filter
             */
            if (accountId == MailAccount.DEFAULT_ID) {
                MimeProcessingUtility.addUserAliases(filter, session, context);
            } else {
                // Check for Unified Mail account
                UnifiedInboxManagement management = services.getService(UnifiedInboxManagement.class);
                if ((null != management) && (accountId == management.getUnifiedINBOXAccountID(session))) {
                    int realAccountId;
                    try {
                        UnifiedInboxUID uid = new UnifiedInboxUID(originalMail.getMailId());
                        realAccountId = uid.getAccountId();
                    } catch (OXException e) {
                        // No Unified Mail identifier
                        LoggerHolder.LOG.trace("", e);
                        FullnameArgument fa = UnifiedInboxUID.parsePossibleNestedFullName(originalMail.getFolder());
                        realAccountId = null == fa ? MailAccount.DEFAULT_ID : fa.getAccountId();
                    }

                    if (realAccountId == MailAccount.DEFAULT_ID) {
                        MimeProcessingUtility.addUserAliases(filter, session, context);
                    } else {
                        MailAccountStorageService mass = services.getService(MailAccountStorageService.class);
                        if (null == mass) {
                            MimeProcessingUtility.addUserAliases(filter, session, context);
                        } else {
                            filter.add(new QuotedInternetAddress(mass.getMailAccount(realAccountId, session.getUserId(), session.getContextId()).getPrimaryAddress(), false));
                        }
                    }
                } else {
                    MailAccountStorageService mass = services.getService(MailAccountStorageService.class);
                    if (null == mass) {
                        MimeProcessingUtility.addUserAliases(filter, session, context);
                    } else {
                        filter.add(new QuotedInternetAddress(mass.getMailAccount(accountId, session.getUserId(), session.getContextId()).getPrimaryAddress(), false));
                    }
                }
            }
            /*
             * Determine if other original recipients should be added to 'Cc'.
             */
            final boolean replyallcc = usm.isReplyAllCc();
            /*
             * Filter the recipients of 'Reply-To'/'From' field
             */
            final Set<InternetAddress> filteredAddrs = filter(filter, recipientAddrs);
            /*
             * Add filtered recipients from 'To' field
             */
            String hdrVal = originalMail.getHeader(MessageHeaders.HDR_TO, MessageHeaders.HDR_ADDR_DELIM);
            InternetAddress[] toAddrs = null;
            if (hdrVal != null) {
                filteredAddrs.addAll(filter(filter, (toAddrs = parseAddressList(hdrVal, true))));
            }
            /*
             * ... and add filtered addresses to either 'To' or 'Cc' field
             */
            if (!filteredAddrs.isEmpty()) {
                if (replyallcc) {
                    // Put original sender into 'To'
                    state.message.setTo(toAddresses(recipientAddrs));
                    // All other into 'Cc'
                    filteredAddrs.removeAll(Arrays.asList(recipientAddrs));
                    state.message.setCc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                } else {
                    state.message.setTo(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
                }
            } else if (toAddrs != null) {
                final Set<InternetAddress> tmpSet = new HashSet<InternetAddress>(Arrays.asList(recipientAddrs));
                tmpSet.removeAll(Arrays.asList(toAddrs));
                if (tmpSet.isEmpty()) {
                    /*
                     * The message was sent from the user to himself. In this special case allow user's own address in field 'To' to
                     * avoid an empty 'To' field
                     */
                    state.message.setTo(toAddresses(recipientAddrs));
                }
            }
            /*
             * Filter recipients from 'Cc' field
             */
            filteredAddrs.clear();
            hdrVal = originalMail.getHeader(MessageHeaders.HDR_CC, MessageHeaders.HDR_ADDR_DELIM);
            if (hdrVal != null) {
                filteredAddrs.addAll(filter(filter, parseAddressList(unfold(hdrVal), true)));
            }
            if (!filteredAddrs.isEmpty()) {
                state.message.setCc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
            }
            /*
             * Filter recipients from 'Bcc' field
             */
            filteredAddrs.clear();
            hdrVal = originalMail.getHeader(MessageHeaders.HDR_BCC, MessageHeaders.HDR_ADDR_DELIM);
            if (hdrVal != null) {
                filteredAddrs.addAll(filter(filter, parseAddressList(unfold(hdrVal), true)));
            }
            if (!filteredAddrs.isEmpty()) {
                state.message.setBcc(toAddresses(filteredAddrs.toArray(new InternetAddress[filteredAddrs.size()])));
            }
        } else {
            /*
             * Plain reply: Just put original sender into 'To' field
             */
            state.message.setTo(toAddresses(recipientAddrs));
        }

        // Check whether to attach original message
        if (usm.getAttachOriginalMessage() > 0) {
            // Obtain attachment storage (can only be null here)
            state.attachmentStorage = getAttachmentStorage(session);

            ThresholdFileHolder sink = new ThresholdFileHolder();
            try {
                originalMail.writeTo(sink.asOutputStream());

                // Compile attachment
                AttachmentDescription attachment = AttachmentStorages.createAttachmentDescriptionFor(originalMail, 0, sink.getLength(), state.uuid);
                Attachment emlAttachment = AttachmentStorages.saveAttachment(sink.getStream(), attachment, session, state.attachmentStorage);
                state.attachments = new ArrayList<>(1);
                state.attachments.add(emlAttachment);
            } finally {
                Streams.close(sink);
            }
        }

        {
            TextAndContentType textForReply = usm.isIgnoreOriginalMailTextOnReply() ? null : MimeProcessingUtility.getTextForReply(originalMail, usm.isDisplayHtmlInlineContent(), false, session);
            if (null == textForReply) {
                state.message.setContent("");
                state.message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
            } else {
                state.message.setContent(textForReply.getText());
                state.message.setContentType(textForReply.isHtml() ? TEXT_HTML : TEXT_PLAIN);
            }
        }

        // Add mail's inline images
        List<String> contentIds = new ArrayList<String>();
        if (TEXT_HTML == state.message.getContentType()) {
            MimeProcessingUtility.getTextForForward(originalMail, true, false, contentIds, session);

            if (!contentIds.isEmpty()) {
                InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                if (null != inlineParts && !inlineParts.isEmpty()) {
                    if (null == state.attachmentStorage) {
                        state.attachmentStorage = getAttachmentStorage(session);
                    }
                    if (null == state.attachments) {
                        state.attachments = new ArrayList<>(inlineParts.size());
                    }

                    Map<String, Attachment> inlineAttachments = new HashMap<String, Attachment>(inlineParts.size());
                    int i = 0;
                    for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                        MailPart mailPart = inlineEntry.getValue();
                        // Compile & store attachment
                        AttachmentDescription attachment = AttachmentStorages.createInlineAttachmentDescriptionFor(mailPart, inlineEntry.getKey(), i + 1, state.uuid);
                        Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, session, state.attachmentStorage);
                        state.attachments.add(partAttachment);

                        inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                        i++;
                    }

                    state.message.setContent(CompositionSpaces.replaceCidInlineImages(state.message.getContent(), inlineAttachments, AttachmentImageDataSource.getInstance(), session));
                }
            }
        }

        if (parameters.isAppendOriginalAttachments()) {
            // Add mail's non-inline parts
            NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
            if (false == contentIds.isEmpty()) {
                handler.setImageContentIds(contentIds);
            }
            new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, handler);
            List<MailPart> nonInlineParts = handler.getNonInlineParts();
            if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                // Obtain attachment storage
                if (null == state.attachmentStorage) {
                    state.attachmentStorage = getAttachmentStorage(session);
                }
                if (null == state.attachments) {
                    state.attachments = new ArrayList<>(nonInlineParts.size());
                }

                int i = state.attachments.size();
                for (MailPart mailPart : nonInlineParts) {
                    // Compile & store attachment
                    AttachmentDescription attachment = AttachmentStorages.createAttachmentDescriptionFor(mailPart, i + 1, state.uuid, session);
                    Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, session, state.attachmentStorage);
                    state.attachments.add(partAttachment);
                    i++;
                }
            }
        }
    }

    /**
     * Filters given address array against given filter set. All addresses currently contained in filter set are removed from specified
     * <code>addrs</code> and all addresses not contained in filter set are added to filter set for future invocations.
     *
     * @param filter The current address filter
     * @param addrs The address list to filter
     * @return The filtered set of addresses
     */
    private static Set<InternetAddress> filter(final Set<InternetAddress> filter, final InternetAddress[] addrs) {
        if (addrs == null) {
            return new HashSet<InternetAddress>(0);
        }
        final Set<InternetAddress> set = new LinkedHashSet<InternetAddress>(Arrays.asList(addrs));
        /*
         * Remove all addresses from set which are contained in filter
         */
        set.removeAll(filter);
        /*
         * Add new addresses to filter
         */
        filter.addAll(set);
        return set;
    }

}
