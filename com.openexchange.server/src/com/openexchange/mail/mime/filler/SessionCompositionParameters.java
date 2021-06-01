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

package com.openexchange.mail.mime.filler;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.compose.CompositionSpaces;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.filler.MimeMessageFiller.ImageDataImageProvider;
import com.openexchange.mail.mime.filler.MimeMessageFiller.ImageProvider;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MsisdnUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * The composition parameters associated with a session.
 */
public final class SessionCompositionParameters implements CompositionParameters {

    private final Session session;
    private final Context ctx;
    private final UserSettingMail usm;
    private int accountId = MailAccount.DEFAULT_ID;

    public SessionCompositionParameters(Session session, Context ctx, UserSettingMail usm) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.usm = usm;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    @Override
    public String getOrganization() throws OXException {
        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        return contactService.getOrganization(session);
    }

    @Override
    public String getClient() throws OXException {
        return session.getClient();
    }

    @Override
    public String getOriginatingIP() throws OXException {
        String origIp = session.getLocalIp();
        if (MimeMessageFiller.isLocalhost(origIp)) {
            MimeMessageFiller.LOG.debug("Session provides localhost as client IP address: {}", origIp);
            // Prefer request's remote address if local IP seems to denote local host
            origIp = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS);
        }

        return origIp;
    }

    @Override
    public InternetAddress getSenderAddress(InternetAddress from) throws OXException, AddressException {
        InternetAddress sender = null;
        final MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
        if (null != mass) {
            try {
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                int id = mass.getByPrimaryAddress(from.getAddress(), userId, contextId);
                if (id < 0) {
                    id = mass.getByPrimaryAddress(IDNA.toIDN(from.getAddress()), userId, contextId);
                    if (id < 0) {
                        /*
                         * No appropriate mail account found which matches from address
                         */
                        final String sendAddr = usm.getSendAddr();
                        if (sendAddr != null && sendAddr.length() > 0) {
                            try {
                                sender = new QuotedInternetAddress(sendAddr, true);
                            } catch (AddressException e) {
                                MimeMessageFiller.LOG.error("Default send address cannot be parsed", e);
                            }
                        }
                    }
                }
            } catch (OXException e) {
                /*
                 * Conflict during look-up
                 */
                MimeMessageFiller.LOG.debug("", e);
            }
        }

        if (sender != null || !from.equals(sender)) {
            final Set<InternetAddress> aliases;
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
            final User user = userService.getUser(session.getUserId(), ctx);
            aliases = new LinkedHashSet<InternetAddress>();
            for (String alias : user.getAliases()) {
                aliases.add(new QuotedInternetAddress(alias));
            }
            if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                MsisdnUtility.addMsisdnAddress(aliases, session);
                final String address = from.getAddress();
                final int pos = address.indexOf('/');
                if (pos > 0) {
                    from.setAddress(address.substring(0, pos));
                }
            }

            if (from.equals(sender) || aliases.contains(sender)) {
                sender = null;
            }
        }

        return sender;
    }

    @Override
    public String getTimeZoneID() throws OXException {
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
        return userService.getUser(session.getUserId(), ctx).getTimeZone();
    }

    @Override
    public boolean setReplyTo() {
        return true;
    }

    @Override
    public String getReplyToAddress() throws OXException {
        String replyTo = usm.getReplyToAddr();
        if (isEmpty(replyTo)) {
            MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null != mass) {
                if (mass.existsMailAccount(accountId, session.getUserId(), session.getContextId())) {
                    MailAccount mailAccount = mass.getMailAccount(accountId, session.getUserId(), session.getContextId());
                    if (!UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                        String sReplyTo = mailAccount.getReplyTo();
                        if (!isEmpty(sReplyTo) && !toLowerCase(sReplyTo).startsWith("null")) {
                            replyTo = sReplyTo;
                        }
                    }
                } else {
                    TransportAccount transportAccount = mass.getTransportAccount(accountId, session.getUserId(), session.getContextId());
                    String sReplyTo = transportAccount.getReplyTo();
                    if (!isEmpty(sReplyTo) && !toLowerCase(sReplyTo).startsWith("null")) {
                        replyTo = sReplyTo;
                    }
                }
            }
        }

        return replyTo;
    }

    @Override
    public String getEnvelopeFrom() throws OXException {
        String address = UserStorage.getInstance().getUser(session.getUserId(), ctx).getMail();
        try {
            return IDNA.toACE(address);
        } catch (AddressException e) {
            throw MimeMailExceptionCode.INVALID_EMAIL_ADDRESS.create(e, address);
        }
    }

    @Override
    public Locale getLocale() throws OXException {
        return UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale();
    }

    @Override
    public String getUserVCardFileName() throws OXException {
        return CompositionSpaces.getUserVCardFileName(session);
    }

    @Override
    public byte[] getUserVCard() throws OXException {
        return CompositionSpaces.getUserVCardBytes(session);
    }

    @Override
    public int getAutoLinebreak() {
        return usm.getAutoLinebreak();
    }

    @Override
    public boolean isForwardAsAttachment() {
        return usm.isForwardAsAttachment();
    }

    @Override
    public ImageProvider createImageProvider(ImageDataSource dataSource, ImageLocation imageLocation) throws OXException {
        return new ImageDataImageProvider(dataSource, imageLocation, session);
    }
}