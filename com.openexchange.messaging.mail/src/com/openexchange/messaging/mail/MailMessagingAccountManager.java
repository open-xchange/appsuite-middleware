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

package com.openexchange.messaging.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.ServiceAware;
import com.openexchange.messaging.mail.services.MailMessagingServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailMessagingAccountManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailMessagingAccountManager implements MessagingAccountManager {

    private final MailMessageService mailMessageService;

    /**
     * Initializes a new {@link MailMessagingAccountManager}.
     */
    public MailMessagingAccountManager(final MailMessageService mailMessageService) {
        super();
        this.mailMessageService = mailMessageService;
    }

    public MessagingAccount newAccount() throws OXException {
        return new MessagingAccountImpl();
    }

    @Override
    public boolean hasAccount(final Session session) throws OXException {
        final MailAccountStorageService mass =
            MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
        final MailAccount[] accounts = mass.getUserMailAccounts(session.getUserId(), session.getContextId());
        return null != accounts && accounts.length > 0;
    }

    @Override
    public int addAccount(final MessagingAccount account, final Session session) throws OXException {
        final MailAccountStorageService mass =
            MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);

        final MailAccountDescription accountDescription = new MailAccountDescription();
        accountDescription.setName(account.getDisplayName());

        final Map<String, Object> configuration = account.getConfiguration();
        accountDescription.setLogin(getString(MailConstants.MAIL_LOGIN, configuration));
        accountDescription.setPassword(getString(MailConstants.MAIL_PASSWORD, configuration));

        accountDescription.setConfirmedHam(optString(MailConstants.MAIL_CONFIRMED_HAM, configuration));
        accountDescription.setConfirmedHamFullname(optString(MailConstants.MAIL_CONFIRMED_HAM_FULLNAME, configuration));

        accountDescription.setConfirmedSpam(optString(MailConstants.MAIL_CONFIRMED_SPAM, configuration));
        accountDescription.setConfirmedSpamFullname(optString(MailConstants.MAIL_CONFIRMED_SPAM_FULLNAME, configuration));

        accountDescription.setDrafts(optString(MailConstants.MAIL_DRAFTS, configuration));
        accountDescription.setDraftsFullname(optString(MailConstants.MAIL_DRAFTS_FULLNAME, configuration));

        accountDescription.setSent(optString(MailConstants.MAIL_SENT, configuration));
        accountDescription.setSentFullname(optString(MailConstants.MAIL_SENT_FULLNAME, configuration));

        accountDescription.setSpam(optString(MailConstants.MAIL_SPAM, configuration));
        accountDescription.setSpamFullname(optString(MailConstants.MAIL_SPAM_FULLNAME, configuration));

        accountDescription.setTrash(optString(MailConstants.MAIL_TRASH, configuration));
        accountDescription.setTrashFullname(optString(MailConstants.MAIL_TRASH_FULLNAME, configuration));

        accountDescription.setMailPort(getInt(MailConstants.MAIL_PORT, configuration));
        accountDescription.setMailProtocol(getString(MailConstants.MAIL_PROTOCOL, configuration));
        {
            final Boolean mailSecure = optBoolean(MailConstants.MAIL_SECURE, configuration);
            accountDescription.setMailSecure(null == mailSecure ? false : mailSecure.booleanValue());
        }
        accountDescription.setMailServer(getString(MailConstants.MAIL_SERVER, configuration));

        accountDescription.setPersonal(optString(MailConstants.MAIL_PERSONAL, configuration));

        accountDescription.setPrimaryAddress(getString(MailConstants.MAIL_PRIMARY_ADDRESS, configuration));

        accountDescription.setTransportLogin(optString(MailConstants.TRANSPORT_LOGIN, configuration));
        accountDescription.setTransportPassword(optString(MailConstants.TRANSPORT_PASSWORD, configuration));

        accountDescription.setTransportPort(optInt(MailConstants.TRANSPORT_PORT, configuration));
        accountDescription.setTransportProtocol(optString(MailConstants.TRANSPORT_PROTOCOL, configuration));
        {
            final Boolean transportSecure = optBoolean(MailConstants.TRANSPORT_SECURE, configuration);
            accountDescription.setTransportSecure(null == transportSecure ? false : transportSecure.booleanValue());
        }
        accountDescription.setTransportServer(optString(MailConstants.TRANSPORT_SERVER, configuration));

        {
            final Boolean unifiedINBOXEnabled = optBoolean(MailConstants.UNIFIED_MAIL_ENABLED, configuration);
            accountDescription.setUnifiedINBOXEnabled(null == unifiedINBOXEnabled ? false : unifiedINBOXEnabled.booleanValue());
        }

        for (final Entry<String, Object> entry : configuration.entrySet()) {
            final String name = entry.getKey();
            if (!MailConstants.ALL.contains(name)) {
                accountDescription.addProperty(name, entry.getValue().toString());
            }
        }

        return mass.insertMailAccount(
            accountDescription,
            session.getUserId(),
            getContext(session.getContextId()),
            session);
    }

    @Override
    public void deleteAccount(final MessagingAccount account, final Session session) throws OXException {
        final MailAccountStorageService mass =
            MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);

        mass.deleteMailAccount(account.getId(), Collections.<String, Object> emptyMap(), session.getUserId(), session.getContextId());
    }

    @Override
    public MessagingAccount getAccount(final int id, final Session session) throws OXException {
        final MailAccountStorageService mass =
            MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);
        final MailAccount mailAccount = mass.getMailAccount(id, session.getUserId(), session.getContextId());
        return convert2MessagingAccount(mailAccount);
    }

    private MessagingAccountImpl convert2MessagingAccount(final MailAccount mailAccount) {
        final MessagingAccountImpl ret = new MessagingAccountImpl();
        ret.setDisplayName(mailAccount.getName());
        ret.setId(mailAccount.getId());
        ret.setMessagingService(mailMessageService);

        ret.addParameter2Config(MailConstants.MAIL_LOGIN, mailAccount.getLogin());
        ret.addParameter2Config(MailConstants.MAIL_PASSWORD, mailAccount.getPassword());

        ret.addParameter2Config(MailConstants.MAIL_CONFIRMED_HAM, mailAccount.getConfirmedHam());
        ret.addParameter2Config(MailConstants.MAIL_CONFIRMED_HAM_FULLNAME, mailAccount.getConfirmedHamFullname());

        ret.addParameter2Config(MailConstants.MAIL_CONFIRMED_SPAM, mailAccount.getConfirmedSpam());
        ret.addParameter2Config(MailConstants.MAIL_CONFIRMED_SPAM_FULLNAME, mailAccount.getConfirmedSpamFullname());

        ret.addParameter2Config(MailConstants.MAIL_DRAFTS, mailAccount.getDrafts());
        ret.addParameter2Config(MailConstants.MAIL_DRAFTS_FULLNAME, mailAccount.getDraftsFullname());

        ret.addParameter2Config(MailConstants.MAIL_SENT, mailAccount.getSent());
        ret.addParameter2Config(MailConstants.MAIL_SENT_FULLNAME, mailAccount.getSentFullname());

        ret.addParameter2Config(MailConstants.MAIL_SPAM, mailAccount.getSpam());
        ret.addParameter2Config(MailConstants.MAIL_SPAM_FULLNAME, mailAccount.getSpamFullname());

        ret.addParameter2Config(MailConstants.MAIL_TRASH, mailAccount.getTrash());
        ret.addParameter2Config(MailConstants.MAIL_TRASH_FULLNAME, mailAccount.getTrashFullname());

        ret.addParameter2Config(MailConstants.MAIL_PERSONAL, mailAccount.getPersonal());

        ret.addParameter2Config(MailConstants.MAIL_PORT, Integer.valueOf(mailAccount.getMailPort()));
        ret.addParameter2Config(MailConstants.MAIL_PROTOCOL, mailAccount.getMailProtocol());
        ret.addParameter2Config(MailConstants.MAIL_SECURE, Boolean.valueOf(mailAccount.isMailSecure()));
        ret.addParameter2Config(MailConstants.MAIL_SERVER, mailAccount.getMailServer());

        ret.addParameter2Config(MailConstants.MAIL_PRIMARY_ADDRESS, mailAccount.getPrimaryAddress());

        ret.addParameter2Config(MailConstants.TRANSPORT_LOGIN, mailAccount.getTransportLogin());
        ret.addParameter2Config(MailConstants.TRANSPORT_PASSWORD, mailAccount.getTransportPassword());

        ret.addParameter2Config(MailConstants.TRANSPORT_PORT, Integer.valueOf(mailAccount.getTransportPort()));
        ret.addParameter2Config(MailConstants.TRANSPORT_PROTOCOL, mailAccount.getTransportProtocol());
        ret.addParameter2Config(MailConstants.TRANSPORT_SECURE, Boolean.valueOf(mailAccount.isTransportSecure()));
        ret.addParameter2Config(MailConstants.TRANSPORT_SERVER, mailAccount.getTransportServer());

        ret.addParameter2Config(MailConstants.UNIFIED_MAIL_ENABLED, Boolean.valueOf(mailAccount.isUnifiedINBOXEnabled()));
        return ret;
    }

    @Override
    public List<MessagingAccount> getAccounts(final Session session) throws OXException {
        try {
            final MailAccountStorageService mass =
                MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);

            final MailAccount[] accounts = mass.getUserMailAccounts(session.getUserId(), session.getContextId());
            final List<MessagingAccount> list = new ArrayList<MessagingAccount>(accounts.length);
            for (final MailAccount mailAccount : accounts) {
                list.add(convert2MessagingAccount(mailAccount));
            }
            return list;
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void updateAccount(final MessagingAccount account, final Session session) throws OXException {
        try {
            final MailAccountStorageService mass =
                MailMessagingServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class, true);

            final MailAccountDescription accountDescription = new MailAccountDescription();
            final Set<Attribute> attributes = new HashSet<Attribute>();

            {
                final String displayName = account.getDisplayName();
                if (null != displayName) {
                    accountDescription.setName(displayName);
                    attributes.add(Attribute.NAME_LITERAL);
                }
            }

            final Map<String, Object> configuration = account.getConfiguration();

            {
                final String login = optString(MailConstants.MAIL_LOGIN, configuration);
                if (null != login) {
                    accountDescription.setLogin(login);
                    attributes.add(Attribute.LOGIN_LITERAL);
                }
            }
            {
                final String password = optString(MailConstants.MAIL_PASSWORD, configuration);
                if (password != null) {
                    accountDescription.setPassword(password);
                    attributes.add(Attribute.PASSWORD_LITERAL);
                }
            }
            {
                final String confirmedHam = optString(MailConstants.MAIL_CONFIRMED_HAM, configuration);
                if (null != confirmedHam) {
                    accountDescription.setConfirmedHam(confirmedHam);
                    attributes.add(Attribute.CONFIRMED_HAM_LITERAL);
                }
            }
            {
                final String confirmedHamFullname = optString(MailConstants.MAIL_CONFIRMED_HAM_FULLNAME, configuration);
                if (null != confirmedHamFullname) {
                    accountDescription.setConfirmedHamFullname(confirmedHamFullname);
                    attributes.add(Attribute.CONFIRMED_HAM_FULLNAME_LITERAL);
                }
            }
            {
                final String confirmedSpam = optString(MailConstants.MAIL_CONFIRMED_SPAM, configuration);
                if (null != confirmedSpam) {
                    accountDescription.setConfirmedSpam(confirmedSpam);
                    attributes.add(Attribute.CONFIRMED_SPAM_LITERAL);
                }
            }
            {
                final String confirmedSpamFullname = optString(MailConstants.MAIL_CONFIRMED_SPAM_FULLNAME, configuration);
                if (null != confirmedSpamFullname) {
                    accountDescription.setConfirmedSpamFullname(confirmedSpamFullname);
                    attributes.add(Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL);
                }
            }
            {
                final String drafts = optString(MailConstants.MAIL_DRAFTS, configuration);
                if (null != drafts) {
                    accountDescription.setDrafts(drafts);
                    attributes.add(Attribute.DRAFTS_LITERAL);
                }
            }
            {
                final String draftsFullname = optString(MailConstants.MAIL_DRAFTS_FULLNAME, configuration);
                if (null != draftsFullname) {
                    accountDescription.setDraftsFullname(draftsFullname);
                    attributes.add(Attribute.DRAFTS_FULLNAME_LITERAL);
                }
            }
            {
                final String sent = optString(MailConstants.MAIL_SENT, configuration);
                if (null != sent) {
                    accountDescription.setSent(sent);
                    attributes.add(Attribute.SENT_LITERAL);
                }
            }
            {
                final String sentFullname = optString(MailConstants.MAIL_SENT_FULLNAME, configuration);
                if (null != sentFullname) {
                    accountDescription.setSentFullname(sentFullname);
                    attributes.add(Attribute.SENT_FULLNAME_LITERAL);
                }
            }
            {
                final String spam = optString(MailConstants.MAIL_SPAM, configuration);
                if (null != spam) {
                    accountDescription.setSpam(spam);
                    attributes.add(Attribute.SPAM_LITERAL);
                }
            }
            {
                final String spamFullname = optString(MailConstants.MAIL_SPAM_FULLNAME, configuration);
                if (null != spamFullname) {
                    accountDescription.setSpamFullname(spamFullname);
                    attributes.add(Attribute.SPAM_FULLNAME_LITERAL);
                }
            }
            {
                final String trash = optString(MailConstants.MAIL_TRASH, configuration);
                if (null != trash) {
                    accountDescription.setTrash(trash);
                    attributes.add(Attribute.TRASH_LITERAL);
                }
            }
            {
                final String trashFullname = optString(MailConstants.MAIL_TRASH_FULLNAME, configuration);
                if (null != trashFullname) {
                    accountDescription.setTrashFullname(trashFullname);
                    attributes.add(Attribute.TRASH_FULLNAME_LITERAL);
                }
            }
            {
                final int port = optInt(MailConstants.MAIL_PORT, configuration);
                if (port >= 0) {
                    accountDescription.setMailPort(port);
                    attributes.add(Attribute.MAIL_PORT_LITERAL);
                }
            }
            {
                final String mailProtocol = optString(MailConstants.MAIL_PROTOCOL, configuration);
                if (null != mailProtocol) {
                    accountDescription.setMailProtocol(mailProtocol);
                    attributes.add(Attribute.MAIL_PROTOCOL_LITERAL);
                }
            }
            {
                final Boolean secure = optBoolean(MailConstants.MAIL_SECURE, configuration);
                if (null != secure) {
                    accountDescription.setMailSecure(secure.booleanValue());
                    attributes.add(Attribute.MAIL_SECURE_LITERAL);
                }
            }
            {
                final String mailServer = optString(MailConstants.MAIL_SERVER, configuration);
                if (null != mailServer) {
                    accountDescription.setMailServer(mailServer);
                    attributes.add(Attribute.MAIL_SERVER_LITERAL);
                }
            }
            {
                final String replyTo = optString(MailConstants.MAIL_REPLY_TO, configuration);
                if (null != replyTo) {
                    accountDescription.setReplyTo(replyTo);
                    attributes.add(Attribute.REPLY_TO_LITERAL);
                }
            }
            {
                final String personal = optString(MailConstants.MAIL_PERSONAL, configuration);
                if (null != personal) {
                    accountDescription.setPersonal(personal);
                    attributes.add(Attribute.PERSONAL_LITERAL);
                }
            }
            {
                final String primaryAddress = optString(MailConstants.MAIL_PRIMARY_ADDRESS, configuration);
                if (null != primaryAddress) {
                    accountDescription.setPrimaryAddress(primaryAddress);
                    attributes.add(Attribute.PRIMARY_ADDRESS_LITERAL);
                }
            }
            {
                final String transportLogin = optString(MailConstants.TRANSPORT_LOGIN, configuration);
                if (null != transportLogin) {
                    accountDescription.setTransportLogin(transportLogin);
                    attributes.add(Attribute.TRANSPORT_LOGIN_LITERAL);
                }
            }
            {
                final String transportPassword = optString(MailConstants.TRANSPORT_PASSWORD, configuration);
                if (null != transportPassword) {
                    accountDescription.setTransportPassword(transportPassword);
                    attributes.add(Attribute.TRANSPORT_PASSWORD_LITERAL);
                }
            }
            {
                final int transportPort = optInt(MailConstants.TRANSPORT_PORT, configuration);
                if (transportPort >= 0) {
                    accountDescription.setTransportPort(transportPort);
                    attributes.add(Attribute.TRANSPORT_PORT_LITERAL);
                }
            }
            {
                final String transportProtocol = optString(MailConstants.TRANSPORT_PROTOCOL, configuration);
                if (null != transportProtocol) {
                    accountDescription.setTransportProtocol(transportProtocol);
                    attributes.add(Attribute.TRANSPORT_PROTOCOL_LITERAL);
                }
            }
            {
                final Boolean transportSecure = optBoolean(MailConstants.TRANSPORT_SECURE, configuration);
                if (null != transportSecure) {
                    accountDescription.setTransportSecure(transportSecure.booleanValue());
                    attributes.add(Attribute.TRANSPORT_SECURE_LITERAL);
                }
            }
            {
                final String transportServer = optString(MailConstants.TRANSPORT_SERVER, configuration);
                if (null != transportServer) {
                    accountDescription.setTransportServer(transportServer);
                    attributes.add(Attribute.TRANSPORT_SERVER_LITERAL);
                }
            }
            {
                final Boolean unifiedINBOXEnabled = optBoolean(MailConstants.UNIFIED_MAIL_ENABLED, configuration);
                if (null != unifiedINBOXEnabled) {
                    accountDescription.setUnifiedINBOXEnabled(unifiedINBOXEnabled.booleanValue());
                    attributes.add(Attribute.UNIFIED_INBOX_ENABLED_LITERAL);
                }
            }
            for (final Entry<String, Object> entry : configuration.entrySet()) {
                final String name = entry.getKey();
                if (!MailConstants.ALL.contains(name)) {
                    if ("pop3_delete_write_through".equals(name)) {
                        attributes.add(Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL);
                    } else if ("pop3_refresh_rate".equals(name)) {
                        attributes.add(Attribute.POP3_REFRESH_RATE_LITERAL);
                    } else if ("pop3_expunge_on_quit".equals(name)) {
                        attributes.add(Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL);
                    } else if ("pop3_storage".equals(name)) {
                        attributes.add(Attribute.POP3_STORAGE_LITERAL);
                    } else if ("pop3_path".equals(name)) {
                        attributes.add(Attribute.POP3_PATH_LITERAL);
                    }
                    accountDescription.addProperty(name, entry.getValue().toString());
                }
            }

            mass.updateMailAccount(accountDescription, attributes, session.getUserId(), session.getContextId(), session);
        } catch (final OXException e) {
            throw e;
        }
    }

    private static String optString(final String name, final Map<String, Object> configuration) throws OXException {
        final Object value = configuration.get(name);
        if (null == value) {
            return null;
        }
        try {
            return (String) value;
        } catch (final ClassCastException e) {
            throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, String.class.getName(), e.getMessage());
        }
    }

    private static String getString(final String name, final Map<String, Object> configuration) throws OXException {
        final Object value = configuration.get(name);
        if (null == value) {
            throw MailMessagingExceptionCodes.MISSING_CONFIGURATION_PARAMETER.create(name);
        }
        try {
            return (String) value;
        } catch (final ClassCastException e) {
            throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, String.class.getName(), e.getMessage());
        }
    }

    private static int getInt(final String name, final Map<String, Object> configuration) throws OXException {
        final Object value = configuration.get(name);
        if (null == value) {
            throw MailMessagingExceptionCodes.MISSING_CONFIGURATION_PARAMETER.create(name);
        }
        try {
            return ((Integer) value).intValue();
        } catch (final ClassCastException e) {
            if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (final NumberFormatException nfe) {
                    throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, Integer.class.getName(), e.getMessage());
                }
            }
            throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, Integer.class.getName(), e.getMessage());
        }
    }

    private static int optInt(final String name, final Map<String, Object> configuration) throws OXException {
        final Object value = configuration.get(name);
        if (null == value) {
            return -1;
        }
        try {
            return ((Integer) value).intValue();
        } catch (final ClassCastException e) {
            if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (final NumberFormatException nfe) {
                    throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, Integer.class.getName(), e.getMessage());
                }
            }
            throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, Integer.class.getName(), e.getMessage());
        }
    }

    private static Boolean optBoolean(final String name, final Map<String, Object> configuration) throws OXException {
        final Object value = configuration.get(name);
        if (null == value) {
            return null;
        }
        try {
            return (Boolean) value;
        } catch (final ClassCastException e) {
            if (value instanceof String) {
                final String sVal = (String) value;
                if ("true".equalsIgnoreCase(sVal)) {
                    return Boolean.TRUE;
                } else if ("false".equalsIgnoreCase(sVal)) {
                    return Boolean.FALSE;
                }
            }
            throw MailMessagingExceptionCodes.WRONG_CONFIGURATION_PARAMETER.create(e, Boolean.class.getName(), e.getMessage());
        }
    }

    private static Context getContext(final int contextId) throws OXException {
        final ContextService service = MailMessagingServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
        return service.getContext(contextId);
    }

    private static class MessagingAccountImpl implements MessagingAccount, ServiceAware {

        private static final long serialVersionUID = 5355396480305094516L;

        private final Map<String, Object> map;

        private String displayName;

        private int id;

        private MessagingService messagingService;

        public MessagingAccountImpl() {
            super();
            map = new HashMap<String, Object>();
        }

        @Override
        public String getServiceId() {
            return "com.openexchange.messaging.mail";
        }

        public void addParameter2Config(final String name, final Object value) {
            map.put(name, value);
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return Collections.unmodifiableMap(map);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public MessagingService getMessagingService() {
            return messagingService;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public void setMessagingService(final MessagingService messagingService) {
            this.messagingService = messagingService;
        }

    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // We do this elsewhere
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // We do this elsewhere
    }
    
    @Override
    public void removeUnrecoverableItems(String secret, Session session) {
        // We do this elsewhere
    }
}
