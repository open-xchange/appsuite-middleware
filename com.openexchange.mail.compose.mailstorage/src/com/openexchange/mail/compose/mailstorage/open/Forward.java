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

package com.openexchange.mail.compose.mailstorage.open;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.DefaultAttachment;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceImageDataSource;
import com.openexchange.mail.compose.mailstorage.ThresholdFileHolderDataProvider;
import com.openexchange.mail.compose.mailstorage.ThresholdFileHolderFactory;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link Forward} - Utility class to open a composition space for a forward.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class Forward extends AbstractOpener {

    /**
     * Initializes a new {@link Forward}.
     *
     * @param services The service look-up
     */
    public Forward(ServiceLookup services) {
        super(services);
    }

    private static final String PREFIX_FWD = "Fwd: ";

    /**
     * Prepares opening a composition space for a forward.
     *
     * @param parameters The parameters
     * @param state The state
     * @param session The session
     * @throws OXException If an Open-Xchange error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public void doOpenForForward(OpenCompositionSpaceParameters parameters, OpenState state, Session session) throws OXException, MessagingException {
        List<MailPath> forwardsFor = parameters.getReferencedMails();
        state.metaBuilder.withForwardsFor(forwardsFor);
        MailServletInterface mailInterface = MailServletInterface.getInstanceWithDecryptionSupport(session, null);
        state.mailInterface = mailInterface;
        MailMessage originalMail = requireMailMessage(forwardsFor.get(0), mailInterface);
        state.metaBuilder.withDate(originalMail.getSentDate());

        Context context = getContext(session);
        UserSettingMail usm = parameters.getMailSettings();

        // Forward, pre-set subject
        String origSubject = originalMail.getSubject();
        if (Strings.isEmpty(origSubject)) {
            state.message.setSubject(PREFIX_FWD);
        } else {
            if (origSubject.regionMatches(true, 0, PREFIX_FWD, 0, PREFIX_FWD.length())) {
                state.message.setSubject(origSubject);
            } else {
                state.message.setSubject(new StringBuilder(PREFIX_FWD.length() + origSubject.length()).append(PREFIX_FWD).append(origSubject).toString());
            }
        }

        // Determine "From"
        {
            FromAddressProvider fromAddressProvider = FromAddressProvider.byAccountId();
            boolean fromSet = false;
            if (null != fromAddressProvider) {
                if (fromAddressProvider.isDetectBy()) {
                    InternetAddress from = MimeProcessingUtility.determinePossibleFrom(true, originalMail, forwardsFor.get(0).getAccountId(), session, context);
                    if (null != from) {
                        state.message.setFrom(toAddress(from, false));
                        fromSet = true;
                    }
                } else if (fromAddressProvider.isSpecified()) {
                    InternetAddress from = fromAddressProvider.getFromAddress();
                    if (null != from) {
                        state.message.setFrom(toAddress(from, false));
                        fromSet = true;
                    }
                }
            }
            if (!fromSet) {
                String sendAddr = usm.getSendAddr();
                if (sendAddr != null) {
                    state.message.setFrom(toAddress(new QuotedInternetAddress(sendAddr, false), false));
                }
            }
        }

        if (forwardsFor.size() == 1) {
            String owner = MimeProcessingUtility.getFolderOwnerIfShared(forwardsFor.get(0).getFolder(), forwardsFor.get(0).getAccountId(), session);
            if (null != owner) {
                final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                if (null != users && users.length > 0) {
                    InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), false);
                    state.message.setFrom(toAddress(onBehalfOf, false));
                    QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), false);
                    state.message.setSender(toAddress(sender, false));
                }
            }
        }

        Optional<Boolean> optionalEncrypt = Optional.of(state.encrypt);
        if (usm.isForwardAsAttachment() || forwardsFor.size() > 1) {
            state.metaBuilder.withType(Meta.MetaType.FORWARD_ATTACHMENT);
            // Forward as attachment, add mail(s) as attachment(s)

            // Add each mail
            state.attachments = new ArrayList<>(forwardsFor.size());
            int i = 0;
            for (MailPath forwardFor : forwardsFor) {
                ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                try {
                    MailMessage forwardedMail = i == 0 ? originalMail : requireMailMessage(forwardFor, mailInterface);
                    forwardedMail.writeTo(sink.asOutputStream());

                    // Compile attachment
                    AttachmentDescription attachmentDesc = AttachmentStorages.createAttachmentDescriptionFor(forwardedMail, i + 1, sink.getLength(), state.compositionSpaceId);
                    DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
                    if (attachmentDesc.getId() == null) {
                        attachment.withId(UUID.randomUUID());
                    }
                    attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));

                    Attachment emlAttachment = attachment.build();
                    state.attachments.add(emlAttachment);
                    sink = null; // Avoid premature closing
                } finally {
                    Streams.close(sink);
                }
                i++;
            }
        } else {
            state.metaBuilder.withType(Meta.MetaType.FORWARD_INLINE);
            // Forward inline

            // Fix possible "application/smil" parts
            MailMessage forwardedMail;
            {
                ContentType contentType = originalMail.getContentType();
                if (contentType.startsWith("multipart/related") && ("application/smil".equals(contentType.getParameter(com.openexchange.java.Strings.toLowerCase("type"))))) {
                    forwardedMail = MimeSmilFixer.getInstance().process(originalMail);
                } else {
                    if (originalMail.getContentType().startsWith("multipart/")) {
                        // Clone to avoid unnecessary IMAP communication
                        try {
                            originalMail = MimeMessageConverter.convertMessage(MimeMessageUtility.mimeMessageFrom(originalMail), false);
                        } catch (IOException e) {
                            throw CompositionSpaceErrorCode.IO_ERROR.create(e, e.getMessage());
                        }
                    }
                    forwardedMail = originalMail;
                }
            }

            // Grab first seen text from original message and check for possible referenced inline images
            boolean multipart = forwardedMail.getContentType().startsWith("multipart/");
            List<String> contentIds = multipart ? new ArrayList<String>() : null;
            {
                Message.ContentType desiredContentType = parameters.getContentType();
                boolean allowHtmlContent = desiredContentType == null ? usm.isDisplayHtmlInlineContent() : desiredContentType.isImpliesHtml();
                TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, allowHtmlContent, false, contentIds, session);
                if (null == textForForward) {
                    state.message.setContent("");
                    state.message.setContentType(desiredContentType == null ? (usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN) : desiredContentType);
                } else {
                    state.message.setContent(textForForward.getText());
                    state.message.setContentType(textForForward.isHtml() ? (desiredContentType == null || !desiredContentType.isImpliesHtml() ? TEXT_HTML : desiredContentType) : TEXT_PLAIN);
                }
            }

            // Add mail's non-inline parts
            {
                NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                if (null != contentIds && !contentIds.isEmpty()) {
                    handler.setImageContentIds(contentIds);
                }
                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(forwardedMail, handler);
                List<MailPart> nonInlineParts = handler.getNonInlineParts();
                if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                    state.attachments = new ArrayList<>(nonInlineParts.size());
                    int i = 0;
                    for (MailPart mailPart : nonInlineParts) {
                        ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                        try {
                            sink.write(mailPart.getInputStream());

                            // Compile attachment
                            AttachmentDescription attachmentDesc = AttachmentStorages.createAttachmentDescriptionFor(mailPart, i + 1, sink.getLength(), state.compositionSpaceId, session);
                            DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
                            if (attachmentDesc.getId() == null) {
                                attachment.withId(UUID.randomUUID());
                            }
                            attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));

                            Attachment partAttachment = attachment.build();
                            state.attachments.add(partAttachment);
                            sink = null; // Avoid premature closing
                        } finally {
                            Streams.close(sink);
                        }
                        i++;
                    }
                }
            }

            // Add mail's inline images
            if (multipart && state.message.getContentType().isImpliesHtml() && null != contentIds && !contentIds.isEmpty()) {
                InlineContentHandler inlineHandler = new InlineContentHandler(contentIds);
                new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(originalMail, inlineHandler);
                Map<String, MailPart> inlineParts = inlineHandler.getInlineContents();
                if (null != inlineParts && !inlineParts.isEmpty()) {
                    if (null == state.attachments) {
                        state.attachments = new ArrayList<>(inlineParts.size());
                    }

                    Map<ContentId, Attachment> inlineAttachments = new HashMap<ContentId, Attachment>(inlineParts.size());
                    int i = state.attachments.size();
                    for (Map.Entry<String, MailPart> inlineEntry : inlineParts.entrySet()) {
                        MailPart mailPart = inlineEntry.getValue();
                        ThresholdFileHolder sink = ThresholdFileHolderFactory.getInstance().createFileHolder(session);
                        try {
                            sink.write(mailPart.getInputStream());

                            // Compile attachment
                            ContentId contentId = ContentId.valueOf(inlineEntry.getKey());
                            AttachmentDescription attachmentDesc = AttachmentStorages.createInlineAttachmentDescriptionFor(mailPart, contentId, i + 1, state.compositionSpaceId);
                            DefaultAttachment.Builder attachment = DefaultAttachment.builder(attachmentDesc);
                            if (attachmentDesc.getId() == null) {
                                attachment.withId(UUID.randomUUID());
                            }
                            attachment.withDataProvider(new ThresholdFileHolderDataProvider(sink));

                            Attachment partAttachment = attachment.build();
                            state.attachments.add(partAttachment);
                            inlineAttachments.put(contentId, partAttachment);
                            sink = null; // Avoid premature closing
                        } finally {
                            Streams.close(sink);
                        }
                        i++;
                    }

                    state.message.setContent(CompositionSpaces.replaceCidInlineImages(state.message.getContent(), Optional.of(state.compositionSpaceId), inlineAttachments, MailStorageCompositionSpaceImageDataSource.getInstance(), session));
                }
            }
        }
    }

}
