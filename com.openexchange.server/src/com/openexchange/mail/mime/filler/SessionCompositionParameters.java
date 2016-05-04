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
import com.openexchange.contact.internal.VCardUtil;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.filler.MimeMessageFiller.ImageDataImageProvider;
import com.openexchange.mail.mime.filler.MimeMessageFiller.ImageProvider;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.MsisdnUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * The composition parameters associated with a session.
 */
public final class SessionCompositionParameters implements CompositionParameters {

    private final Session session;
    private final Context ctx;
    private final UserSettingMail usm;
    private int accountId = MailAccount.DEFAULT_ID;

    public SessionCompositionParameters(final Session session, final Context ctx, final UserSettingMail usm) {
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
                            } catch (final AddressException e) {
                                MimeMessageFiller.LOG.error("Default send address cannot be parsed", e);
                            }
                        }
                    }
                }
            } catch (final OXException e) {
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
            for (final String alias : user.getAliases()) {
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
        final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
        final User user = userService.getUser(session.getUserId(), ctx);
        return user.getTimeZone();
    }

    @Override
    public boolean setReplyTo() {
        return true;
    }

    @Override
    public String getReplyToAddress() throws OXException {
        String replyTo = usm.getReplyToAddr();
        if (isEmpty(replyTo)) {
            final MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null != mass) {
                final MailAccount mailAccount = mass.getMailAccount(accountId, session.getUserId(), session.getContextId());
                if (!UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    final String sReplyTo = mailAccount.getReplyTo();
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
            throw MimeMailExceptionCode.INVALID_EMAIL_ADDRESS.create(address);
        }
    }

    @Override
    public Locale getLocale() throws OXException {
        return UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale();
    }

    @Override
    public String getUserVCardFileName() throws OXException {
        final String displayName;
        if (session instanceof ServerSession) {
            displayName = ((ServerSession) session).getUser().getDisplayName();
        } else {
            displayName = UserStorage.getInstance().getUser(session.getUserId(), ctx).getDisplayName();
        }
        final String saneDisplayName = Strings.replaceWhitespacesWith(displayName, "");
        return saneDisplayName + ".vcf";
    }

    @Override
    public byte[] getUserVCard() throws OXException {
        Contact contact = ServerServiceRegistry.getInstance().getService(ContactService.class).getUser(session, session.getUserId());
        VCardExport vCardExport = null;
        try {
            vCardExport = VCardUtil.exportContact(contact, session);
            return vCardExport.toByteArray();
        } finally {
            Streams.close(vCardExport);
        }
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