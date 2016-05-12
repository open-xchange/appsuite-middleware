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

package com.openexchange.mail.json.compose.share;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.AbstractComposeHandler;
import com.openexchange.mail.json.compose.ComposeDraftResult;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.DefaultComposeDraftResult;
import com.openexchange.mail.json.compose.DefaultComposeTransportResult;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.internal.AttachmentStorageRegistry;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareComposeLinkGenerator;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link ShareComposeHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeHandler extends AbstractComposeHandler<ShareTransportComposeContext, ShareDraftComposeContext> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareComposeHandler.class);

    /**
     * Initializes a new {@link ShareComposeHandler}.
     */
    public ShareComposeHandler() {
        super();
    }

    private boolean isCreateShare(ComposeRequest composeRequest) {
        return AJAXRequestDataTools.parseBoolParameter("share_attachments", composeRequest.getRequest(), false);
    }

    /**
     * Checks if share compose is enabled and session-associated user holds sufficient capabilities.
     *
     * @param session The session
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isEnabled(Session session) throws OXException {
        return Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.enabled", true, session) && Utilities.hasCapabilities(session, "drive", "share_links");
    }

    @Override
    public String getId() {
        return "share";
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        // Expect "share_attachments=true" parameter
        if (false == isCreateShare(composeRequest)) {
            return false;
        }

        ServerSession session = composeRequest.getSession();
        boolean applicable = isEnabled(session);
        if (!applicable) {
            throw MailExceptionCode.SHARING_NOT_POSSIBLE.create(I(session.getUserId()), I(session.getContextId()));
        }
        return applicable;
    }

    @Override
    protected ShareDraftComposeContext createDraftComposeContext(ComposeRequest composeRequest) throws OXException {
        return new ShareDraftComposeContext(composeRequest);
    }

    @Override
    protected ShareTransportComposeContext createTransportComposeContext(ComposeRequest composeRequest) throws OXException {
        return new ShareTransportComposeContext(composeRequest);
    }

    @Override
    protected ComposeDraftResult doCreateDraftResult(ComposeRequest composeRequest, ShareDraftComposeContext context) throws OXException {
        ComposedMailMessage composeMessage = createRegularComposeMessage(context);
        return new DefaultComposeDraftResult(composeMessage);
    }

    @Override
    protected ComposeTransportResult doCreateTransportResult(ComposeRequest composeRequest, ShareTransportComposeContext context) throws OXException {
        // Get the basic source message
        ServerSession session = composeRequest.getSession();
        ComposedMailMessage source = context.getSourceMessage();

        // Collect recipients
        Set<Recipient> recipients;
        {
            Set<InternetAddress> addresses = new HashSet<InternetAddress>();
            addresses.addAll(Arrays.asList(source.getTo()));
            addresses.addAll(Arrays.asList(source.getCc()));
            addresses.addAll(Arrays.asList(source.getBcc()));

            UserService userService = ServerServiceRegistry.getServize(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.absentService(UserService.class);
            }
            Context ctx = composeRequest.getContext();

            recipients = new LinkedHashSet<>(addresses.size());
            for (InternetAddress address : addresses) {
                User user = resolveToUser(address, ctx, userService);
                String personal = address.getPersonal();
                String sAddress = address.getAddress();
                recipients.add(null == user ? Recipient.createExternalRecipient(personal, sAddress) : Recipient.createInternalRecipient(personal, sAddress, user));
            }
        }

        // Optional password
        String password = getPassword(composeRequest);

        // Optional expiration date
        Date expirationDate = getExpirationDate(composeRequest);
        if (null == expirationDate && Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.requiredExpiration", false, session)) {
            throw MailExceptionCode.EXPIRATION_DATE_MISSING.create(I(session.getUserId()), I(session.getContextId()));
        }

        // Optional auto-expiration of folder/files
        boolean filesAutoExpire;
        if (null == expirationDate) {
            filesAutoExpire = false;
        } else {
            filesAutoExpire = Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.filesAutoExpire", false, session) || isFilesAutoExpire(composeRequest);
        }

        // Determine attachment storage to use
        AttachmentStorageRegistry storageRegistry = ServerServiceRegistry.getServize(AttachmentStorageRegistry.class);
        if (null == storageRegistry) {
            throw ServiceExceptionCode.absentService(AttachmentStorageRegistry.class);
        }
        AttachmentStorage attachmentStorage = storageRegistry.getAttachmentStorageFor(composeRequest);

        // Some state variables
        StoredAttachmentsControl attachmentsControl = null;
        boolean rollback = true;
        try {
            // Store attachments associated with compose context
            attachmentsControl = attachmentStorage.storeAttachments(source, password, expirationDate, filesAutoExpire, context);

            // The share target for an anonymous user
            ShareTarget folderTarget = attachmentsControl.getFolderTarget();
            ShareService shareService = ServerServiceRegistry.getServize(ShareService.class);
            if (null == shareService) {
                throw ServiceExceptionCode.absentService(ShareService.class);
            }
            ShareLink folderLink = shareService.getLink(session, folderTarget);

            // Create share compose reference
            ShareReference shareReference;
            {
                String basicShareUrl = folderLink.getShareURL(composeRequest.getRequest().getHostData());
                shareReference = new ShareReference.Builder(session.getUserId(), session.getContextId())
                    .expiration(expirationDate)
                    .folderId(attachmentsControl.getFolderId())
                    .itemIds(attachmentsControl.getAttachmentIds())
                    .shareUrl(basicShareUrl)
                    .build();
            }

            // Create share link(s) for recipients
            Map<ShareComposeLink, Set<Recipient>> links = new LinkedHashMap<>(recipients.size());
            {
                GuestInfo guest = folderLink.getGuest();
                for (Recipient recipient : recipients) {
                    ShareComposeLink linkedAttachment = ShareComposeLinkGenerator.getInstance().createShareLink(recipient, folderTarget, guest, null, composeRequest);
                    Set<Recipient> associatedRecipients = links.get(linkedAttachment);
                    if (null == associatedRecipients) {
                        associatedRecipients = new LinkedHashSet<>(recipients.size());
                        links.put(linkedAttachment, associatedRecipients);
                    }
                    associatedRecipients.add(recipient);
                }
            }

            // Create personal share link
            ShareComposeLink personalLink;
            {
                personalLink = ShareComposeLinkGenerator.getInstance().createPersonalShareLink(folderTarget, null, composeRequest);
            }

            // Generate messages from links
            List<ComposedMailMessage> transportMessages = new LinkedList<>();
            ComposedMailMessage sentMessage;
            {
                MessageGeneratorRegistry generatorRegistry = ServerServiceRegistry.getServize(MessageGeneratorRegistry.class);
                if (null == generatorRegistry) {
                    throw ServiceExceptionCode.absentService(MessageGeneratorRegistry.class);
                }
                MessageGenerator messageGenerator = generatorRegistry.getMessageGeneratorFor(composeRequest);
                for (Map.Entry<ShareComposeLink, Set<Recipient>> entry : links.entrySet()) {
                    ShareComposeMessageInfo messageInfo = new ShareComposeMessageInfo(entry.getKey(), new ArrayList<Recipient>(entry.getValue()), password, expirationDate, source, context, composeRequest);
                    List<ComposedMailMessage> generatedTransportMessages = messageGenerator.generateTransportMessagesFor(messageInfo);
                    for (ComposedMailMessage generatedTransportMessage : generatedTransportMessages) {
                        // TODO: Apply header to transport messages, too?
                        // transportMessage.setHeader(HEADER_SHARE_MAIL, referenceString);
                        generatedTransportMessage.setAppendToSentFolder(false);
                        transportMessages.add(generatedTransportMessage);
                    }
                }

                String sendAddr = session.getUserSettingMail().getSendAddr();
                User user = composeRequest.getUser();
                Recipient userRecipient = Recipient.createInternalRecipient(user.getDisplayName(), sendAddr, user);
                sentMessage = messageGenerator.generateSentMessageFor(new ShareComposeMessageInfo(personalLink, Collections.singletonList(userRecipient), password, expirationDate, source, context, composeRequest), shareReference);
            }

            // Commit attachment storage
            attachmentsControl.commit();
            rollback = false;

            return new DefaultComposeTransportResult(transportMessages, sentMessage);
        } finally {
            if (null != attachmentsControl) {
                if (rollback) {
                    attachmentsControl.rollback();
                }
                attachmentsControl.finish();
            }
        }
    }

    /**
     * Checks whether created files are supposed to expire as well (provided that an expiration date is set)
     *
     * @param request The compose request
     * @return <code>true</code> for auto-expiration; otherwise <code>false</code>
     */
    protected boolean isFilesAutoExpire(ComposeRequest request) {
        return AJAXRequestDataTools.parseBoolParameter("files_auto_expire", request.getRequest(), false);
    }

    /**
     * Gets the password from given compose request.
     *
     * @param request The compose request
     * @return The password or <code>null</code>
     */
    protected String getPassword(ComposeRequest request) {
        String value = request.getRequest().getParameter("password");
        return Strings.isEmpty(value) ? null : value;
    }

    /**
     * Gets the expiration date from given compose request.
     *
     * @param request The compose request
     * @return The expiration date or <code>null</code>
     * @throws OXException If value is invalid (NaN)
     */
    protected Date getExpirationDate(ComposeRequest request) throws OXException {
        String value = request.getRequest().getParameter("expires");
        if (Strings.isEmpty(value)) {
            return null;
        }

        try {
            value = value.trim();

            // Value is required to start with a digit in either way
            if (!Strings.isDigit(value.charAt(0))) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("expires", value);
            }

            if (value.indexOf('-') > 0) {
                // Expect a date in ISO8601 format
                return com.openexchange.java.ISO8601Utils.parse(value);
            }

            // Expect a date in milliseconds
            try {
                long millis = Long.parseLong(value);
                int offset = TimeZoneUtils.getTimeZone(request.getSession().getUser().getTimeZone()).getOffset(millis);
                return new Date(millis - offset);
            } catch (NumberFormatException e) {
                // Else expect a date in ISO8601 format
                LOG.trace("NaN: {}", value, e);
                return com.openexchange.java.ISO8601Utils.parse(value);
            }
        } catch (IllegalArgumentException iso8601ParsingFailed) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(iso8601ParsingFailed, "expires", value);
        }
    }

    private User resolveToUser(InternetAddress address, Context ctx, UserService userService) throws OXException {
        User user;
        try {
            user = userService.searchUser(IDNA.toIDN(address.getAddress()), ctx);
        } catch (final OXException e) {
            /*
             * Unfortunately UserService.searchUser() throws an exception if no user could be found matching given email address.
             * Therefore check for this special error code and throw an exception if it is not equal.
             */
            if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                throw e;
            }
            user = null;
        }
        return user;
    }

}
