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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.FromAddressProvider;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.AttachmentStorages;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.impl.attachment.AttachmentImageDataSource;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeSmilFixer;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.mime.processing.TextAndContentType;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.InlineContentHandler;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link Forward} - Utility class to open a composition space for a forward.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Forward extends AbstractOpener {

    /**
     * Initializes a new {@link Forward}.
     */
    public Forward(AttachmentStorageService attachmentStorageService, ServiceLookup services) {
        super(attachmentStorageService, services);
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
                        state.message.setFrom(toAddress(from));
                        fromSet = true;
                    }
                } else if (fromAddressProvider.isSpecified()) {
                    InternetAddress from = fromAddressProvider.getFromAddress();
                    if (null != from) {
                        state.message.setFrom(toAddress(from));
                        fromSet = true;
                    }
                }
            }
            if (!fromSet) {
                String sendAddr = usm.getSendAddr();
                if (sendAddr != null) {
                    state.message.setFrom(toAddress(new QuotedInternetAddress(sendAddr, false)));
                }
            }
        }

        if (forwardsFor.size() == 1) {
            String owner = MimeProcessingUtility.getFolderOwnerIfShared(forwardsFor.get(0).getFolder(), forwardsFor.get(0).getAccountId(), session);
            if (null != owner) {
                final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                if (null != users && users.length > 0) {
                    InternetAddress onBehalfOf = new QuotedInternetAddress(users[0].getMail(), false);
                    state.message.setFrom(toAddress(onBehalfOf));
                    QuotedInternetAddress sender = new QuotedInternetAddress(usm.getSendAddr(), false);
                    state.message.setSender(toAddress(sender));
                }
            }
        }

        if (usm.isForwardAsAttachment() || forwardsFor.size() > 1) {
            state.metaBuilder.withType(Meta.MetaType.FORWARD_ATTACHMENT);
            // Forward as attachment, add mail(s) as attachment(s)

            // Obtain attachment storage (can only be null here)
            state.attachmentStorage = getAttachmentStorage(session);

            // Add each mail
            state.attachments = new ArrayList<>(forwardsFor.size());
            int i = 0;
            for (MailPath forwardFor : forwardsFor) {
                ThresholdFileHolder sink = new ThresholdFileHolder();
                try {
                    MailMessage forwardedMail = i == 0 ? originalMail : requireMailMessage(forwardFor, mailInterface);
                    forwardedMail.writeTo(sink.asOutputStream());

                    // Compile attachment
                    AttachmentDescription attachment = AttachmentStorages.createAttachmentDescriptionFor(forwardedMail, i + 1, sink.getLength(), state.compositionSpaceId);
                    Attachment emlAttachment = AttachmentStorages.saveAttachment(sink.getStream(), attachment, session, state.attachmentStorage);
                    state.attachments.add(emlAttachment);
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
                    forwardedMail = originalMail;
                }
            }

            // Grab first seen text from original message and check for possible referenced inline images
            boolean multipart = forwardedMail.getContentType().startsWith("multipart/");
            List<String> contentIds = multipart ? new ArrayList<String>() : null;
            {
                TextAndContentType textForForward = MimeProcessingUtility.getTextForForward(originalMail, usm.isDisplayHtmlInlineContent(), false, contentIds, session);
                if (null == textForForward) {
                    state.message.setContent("");
                    state.message.setContentType(usm.isDisplayHtmlInlineContent() ? TEXT_HTML : TEXT_PLAIN);
                } else {
                    state.message.setContent(textForForward.getText());
                    state.message.setContentType(textForForward.isHtml() ? TEXT_HTML : TEXT_PLAIN);
                }
            }

            // Check if original mail may contain attachments
            if (multipart) {
                // Add mail's non-inline parts
                {
                    NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                    if (null != contentIds && !contentIds.isEmpty()) {
                        handler.setImageContentIds(contentIds);
                    }
                    new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(forwardedMail, handler);
                    List<MailPart> nonInlineParts = handler.getNonInlineParts();
                    if (null != nonInlineParts && !nonInlineParts.isEmpty()) {
                        state.attachmentStorage = getAttachmentStorage(session);
                        state.attachments = new ArrayList<>(nonInlineParts.size());
                        int i = 0;
                        for (MailPart mailPart : nonInlineParts) {
                            // Compile & store attachment
                            AttachmentDescription attachment = AttachmentStorages.createAttachmentDescriptionFor(mailPart, i + 1, state.compositionSpaceId, session);
                            Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, session, state.attachmentStorage);
                            state.attachments.add(partAttachment);
                            i++;
                        }
                    }
                }

                // Add mail's inline images
                if (TEXT_HTML == state.message.getContentType() && null != contentIds && !contentIds.isEmpty()) {
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
                            // Compile & store attachment
                            MailPart mailPart = inlineEntry.getValue();
                            AttachmentDescription attachment = AttachmentStorages.createInlineAttachmentDescriptionFor(mailPart, inlineEntry.getKey(), i + 1, state.compositionSpaceId);
                            Attachment partAttachment = AttachmentStorages.saveAttachment(mailPart.getInputStream(), attachment, session, state.attachmentStorage);
                            state.attachments.add(partAttachment);
                            inlineAttachments.put(inlineEntry.getKey(), partAttachment);
                            i++;
                        }

                        state.message.setContent(CompositionSpaces.replaceCidInlineImages(state.message.getContent(), inlineAttachments, AttachmentImageDataSource.getInstance(), session));
                    }
                }
            }
        }
    }

}
