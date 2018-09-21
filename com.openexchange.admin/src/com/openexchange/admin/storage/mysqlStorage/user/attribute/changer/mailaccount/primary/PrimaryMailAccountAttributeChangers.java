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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount.primary;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.MethodMetadata;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.ReturnType;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UpdateProperties;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link PrimaryMailAccountAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PrimaryMailAccountAttributeChangers extends AbstractAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(PrimaryMailAccountAttributeChangers.class);

    private final Map<UserMailAccountMapper, ValueSetter> setters;

    /**
     * {@link ValueSetter}
     */
    private interface ValueSetter {

        /**
         *
         * @param account
         * @param user
         * @param setAttributes
         */
        void setValue(MailAccountDescription account, User user, Set<com.openexchange.mailaccount.Attribute> setAttributes) throws StorageException;
    }

    private static final String DEFAULT_SMTP_SERVER_CREATE = "smtp://localhost:25";
    private static final String DEFAULT_IMAP_SERVER_CREATE = "imap://localhost:143";

    /**
     * Initialises a new {@link PrimaryMailAccountAttributeChangers}.
     */
    public PrimaryMailAccountAttributeChangers() {
        super();
        Map<UserMailAccountMapper, ValueSetter> s = new HashMap<>();
        s.put(UserMailAccountMapper.PRIMARY_ACCOUNT_NAME, (account, user, setAttributes) -> {
            if (user.isPrimaryAccountNameSet()) {
                account.setName(Strings.isEmpty(user.getPrimaryAccountName()) ? MailFolder.DEFAULT_FOLDER_NAME : user.getPrimaryAccountName());
                setAttributes(setAttributes, UserMailAccountMapper.PRIMARY_ACCOUNT_NAME);
            } else {
                account.setName(MailFolder.DEFAULT_FOLDER_NAME);
            }
        });
        s.put(UserMailAccountMapper.IMAP_SERVER, (account, user, setAttributes) -> {
            setServer(account, user, UserMailAccountMapper.IMAP_SERVER, DEFAULT_IMAP_SERVER_CREATE, URIDefaults.IMAP, setAttributes);
        });
        s.put(UserMailAccountMapper.SMTP_SERVER, (account, user, setAttributes) -> {
            setServer(account, user, UserMailAccountMapper.SMTP_SERVER, DEFAULT_SMTP_SERVER_CREATE, URIDefaults.SMTP, setAttributes);
        });
        s.put(UserMailAccountMapper.IMAP_LOGIN, (account, user, setAttributes) -> {
            if (user.isImapLoginset() || null != user.getImapLogin()) {
                account.setLogin(null == user.getImapLogin() ? "" : user.getImapLogin());
                setAttributes(setAttributes, UserMailAccountMapper.IMAP_LOGIN);
            }
        });
        s.put(UserMailAccountMapper.PRIMARY_EMAIL, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.PRIMARY_EMAIL, setAttributes);
        });
        s.put(UserMailAccountMapper.DRAFTS, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.DRAFTS, setAttributes);
        });
        s.put(UserMailAccountMapper.SENT, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.SENT, setAttributes);
        });
        s.put(UserMailAccountMapper.SPAM, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.SPAM, setAttributes);
        });
        s.put(UserMailAccountMapper.TRASH, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.TRASH, setAttributes);
        });
        s.put(UserMailAccountMapper.ARCHIVE, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.ARCHIVE, setAttributes);
        });
        s.put(UserMailAccountMapper.CONFIRMED_HAM, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.CONFIRMED_HAM, setAttributes);
        });
        s.put(UserMailAccountMapper.CONFIRMED_SPAM, (account, user, setAttributes) -> {
            setSingle(user, account, UserMailAccountMapper.CONFIRMED_SPAM, setAttributes);
        });

        setters = Collections.unmodifiableMap(s);
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        MailAccountDescription account = new MailAccountDescription();
        Set<com.openexchange.mailaccount.Attribute> changed = new HashSet<>();
        account.setDefaultFlag(true);
        account.setId(0);

        for (MethodMetadata methodMetadata : getGetters(userData.getClass().getMethods())) {
            UserMailAccountMapper mapper = UserMailAccountMapper.getMapper(methodMetadata.getName());
            if (mapper == null) {
                continue;
            }
            ValueSetter setter = setters.get(mapper);
            if (setter == null) {
                continue;
            }
            setter.setValue(account, userData, changed);
        }

        if (changed.isEmpty()) {
            return EMPTY_SET;
        }
        try {
            UpdateProperties updateProperties = new UpdateProperties.Builder().setChangePrimary(true).setChangeProtocol(true).setCon(connection).setSession(null).build();
            MailAccountStorageService mass = AdminServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            mass.updateMailAccount(account, changed, userId, contextId, updateProperties);
            return convert(changed);
        } catch (final OXException e) {
            LOG.error("Problem storing the primary mail account.", e);
            throw new StorageException(e);
        }
    }

    /**
     * {@link UserMailAccountMapper}
     */
    private enum UserMailAccountMapper {
        PRIMARY_ACCOUNT_NAME("PrimaryAccountName", "Name", com.openexchange.mailaccount.Attribute.NAME_LITERAL),
        IMAP_SERVER("ImapServerString", "MailServer", URI.class, com.openexchange.mailaccount.Attribute.MAIL_URL_LITERAL),
        IMAP_LOGIN("ImapLogin", "Login", com.openexchange.mailaccount.Attribute.LOGIN_LITERAL, com.openexchange.mailaccount.Attribute.TRANSPORT_LOGIN_LITERAL),
        PRIMARY_EMAIL("PrimaryEmail", "PrimaryAddress", com.openexchange.mailaccount.Attribute.PRIMARY_ADDRESS_LITERAL),
        DRAFTS("Mail_folder_drafts_name", "Drafts", com.openexchange.mailaccount.Attribute.DRAFTS_LITERAL),
        SENT("Mail_folder_sent_name", "Sent", com.openexchange.mailaccount.Attribute.SENT_LITERAL),
        SPAM("Mail_folder_spam_name", "Spam", com.openexchange.mailaccount.Attribute.SPAM_LITERAL),
        TRASH("Mail_folder_trash_name", "Trash", com.openexchange.mailaccount.Attribute.TRASH_LITERAL),
        ARCHIVE("Mail_folder_archive_full_name", "ArchiveFullname", com.openexchange.mailaccount.Attribute.ARCHIVE_FULLNAME_LITERAL),
        CONFIRMED_HAM("Mail_folder_confirmed_ham_name", "ConfirmedHam", com.openexchange.mailaccount.Attribute.CONFIRMED_HAM_LITERAL),
        CONFIRMED_SPAM("Mail_folder_confirmed_spam_name", "ConfirmedSpam", com.openexchange.mailaccount.Attribute.CONFIRMED_SPAM_LITERAL),
        SMTP_SERVER("SmtpServerString", "TransportServer", URI.class, com.openexchange.mailaccount.Attribute.TRANSPORT_URL_LITERAL),
        ;

        private final String userValueGetter;
        private final String mailAccountSetter;
        private final com.openexchange.mailaccount.Attribute[] attributes;

        private static final Map<String, UserMailAccountMapper> entries;
        private final Class<?> type;
        static {
            Map<String, UserMailAccountMapper> m = new HashMap<>();
            for (UserMailAccountMapper mapper : UserMailAccountMapper.values()) {
                m.put(mapper.getUserValueGetter(), mapper);
            }
            entries = Collections.unmodifiableMap(m);
        }

        private UserMailAccountMapper(String userValueGetter, String mailAccountSetter, com.openexchange.mailaccount.Attribute... attributes) {
            this(userValueGetter, mailAccountSetter, String.class, attributes);
        }

        /**
         * Initialises a new {@link PrimaryMailAccountAttributeChangers.UserMailAccountMapper}.
         */
        private UserMailAccountMapper(String userValueGetter, String mailAccountSetter, Class<?> type, com.openexchange.mailaccount.Attribute... attributes) {
            this.userValueGetter = userValueGetter;
            this.mailAccountSetter = mailAccountSetter;
            this.type = type;
            this.attributes = attributes;
        }

        /**
         * Gets the userValueGetter
         *
         * @return The userValueGetter
         */
        public String getUserValueGetter() {
            return userValueGetter;
        }

        /**
         * Gets the mailAccountSetter
         *
         * @return The mailAccountSetter
         */
        public String getMailAccountSetter() {
            return mailAccountSetter;
        }

        /**
         * Gets the attributes
         *
         * @return The attributes
         */
        public com.openexchange.mailaccount.Attribute[] getAttributes() {
            return attributes;
        }

        public static UserMailAccountMapper getMapper(String getterName) {
            return entries.get(getterName);
        }

        /**
         * Gets the type
         *
         * @return The type
         */
        public Class<?> getType() {
            return type;
        }
    }

    /**
     * @return
     */
    private Set<String> convert(Set<com.openexchange.mailaccount.Attribute> changed) {
        Set<String> attributes = new HashSet<>();
        for (com.openexchange.mailaccount.Attribute attribute : changed) {
            attributes.add(attribute.getName());
        }
        return attributes;
    }

    /**
     *
     * @param user
     * @param account
     * @param mapper
     * @param setAttributes
     * @throws StorageException
     */
    private void setSingle(User user, MailAccountDescription account, UserMailAccountMapper mapper, Set<com.openexchange.mailaccount.Attribute> setAttributes) throws StorageException {
        try {
            Method getter = User.class.getMethod("get" + mapper.getUserValueGetter());
            Method setter = MailAccountDescription.class.getMethod("set" + mapper.getMailAccountSetter(), String.class);

            Object value = getter.invoke(user, (Object[]) null);
            if (value == null) {
                return;
            }
            setter.invoke(account, value);
            setAttributes(setAttributes, mapper);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new StorageException(e);
        }
    }

    /**
     *
     * @param account
     * @param user
     * @param mapper
     * @param defaultValue
     * @param uriDefault
     * @param setAttributes
     * @throws StorageException
     */
    private void setServer(MailAccountDescription account, User user, UserMailAccountMapper mapper, String defaultValue, URIDefaults uriDefault, Set<com.openexchange.mailaccount.Attribute> setAttributes) throws StorageException {
        try {
            Method getter = User.class.getMethod("get" + mapper.getUserValueGetter());
            Object value = getter.invoke(user, (Object[]) null);
            int index = mapper.getUserValueGetter().indexOf("String");
            String getterName = mapper.getUserValueGetter();
            if (index > 0) {
                getterName = getterName.substring(0, index);
            }
            String methodName = "is" + getterName + "set";
            Method retVal = User.class.getMethod(methodName);
            boolean isSet = ((Boolean) retVal.invoke(user, (Object[]) null)).booleanValue();
            if (isSet || null != value) {
                String server = (String) value;
                if (null == server) {
                    server = defaultValue;
                }
                Method setter = MailAccountDescription.class.getMethod("set" + mapper.getMailAccountSetter(), mapper.getType());
                setter.invoke(account, URIParser.parse(server, uriDefault));
                setAttributes(setAttributes, mapper);
            }
        } catch (final URISyntaxException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            LOG.error("Problem storing the primary mail account.", e);
            throw new StorageException(e);
        }
    }

    /**
     *
     * @param setAttributes
     * @param mapper
     */
    private void setAttributes(Set<com.openexchange.mailaccount.Attribute> setAttributes, UserMailAccountMapper mapper) {
        for (com.openexchange.mailaccount.Attribute attribute : mapper.getAttributes()) {
            setAttributes.add(attribute);
        }
    }

    ///////////////////// vvv REFACTOR AND CONSOLIDATE vvv ///////////////////////

    private enum MethodPrefix {
        get, is;
    }

    /**
     * Gets the getters from the specified methods
     *
     * @param methods The methods from which to retrieve the getters
     * @return A {@link List} with all the getter methods
     */
    private List<MethodMetadata> getGetters(Method[] methods) {
        List<MethodMetadata> methodMetadata = new ArrayList<>();
        for (final Method method : methods) {
            final String methodName = method.getName();
            for (MethodPrefix methodPrefix : MethodPrefix.values()) {
                if (!methodName.startsWith(methodPrefix.name())) {
                    continue;
                }
                String methodNameWithoutPrefix = methodName.substring(methodPrefix.name().length());
                final String returnType = method.getReturnType().getName();
                ReturnType rt = ReturnType.getReturnType(returnType);
                if (rt == null) {
                    LOG.debug("Unknown return type '{}'. Method '{}' will be ignored", rt, methodName);
                    continue;
                }
                methodMetadata.add(new MethodMetadata(method, methodNameWithoutPrefix, rt));
            }
        }
        return methodMetadata;
    }
}
