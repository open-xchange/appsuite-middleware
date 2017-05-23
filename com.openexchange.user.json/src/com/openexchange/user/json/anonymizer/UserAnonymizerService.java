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

package com.openexchange.user.json.anonymizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.json.osgi.Services;

/**
 * {@link UserAnonymizerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UserAnonymizerService implements AnonymizerService<User> {

    /**
     * Initializes a new {@link UserAnonymizerService}.
     */
    public UserAnonymizerService() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.USER;
    }

    @Override
    public User anonymize(User entity, Session session) throws OXException {
        if (null == entity) {
            return null;
        }

        // Do not anonymize yourself
        if (session.getUserId() == entity.getId()) {
            return entity;
        }

        // Check if associated guest was invited by given user entity
        {
            if (entity.getId() == ServerSessionAdapter.valueOf(session).getUser().getCreatedBy()) {
                return entity;
            }
            ShareService shareService = Services.getService(ShareService.class);
            if (null != shareService) {
                Set<Integer> userIds = shareService.getSharingUsersFor(session.getContextId(), session.getUserId());
                if (userIds.contains(Integer.valueOf(entity.getId()))) {
                    return entity;
                }
            }
        }

        // Otherwise anonymize the user
        return new AnonymizingUser(entity, session);
    }

    // ---------------------------------------------------------------------------------------------

    private static final class AnonymizingUser implements User {

        private static final long serialVersionUID = -3389011471434047151L;

        private final User delegate;
        private final String displayName;
        private final String i18n;

        AnonymizingUser(User delegate, Session session) throws OXException {
            super();
            this.delegate = delegate;
            i18n = Anonymizers.getUserI18nFor(session);
            displayName = new StringBuilder(i18n).append(' ').append(delegate.getId()).toString();
        }

        @Override
        public String getUserPassword() {
            return null;
        }

        @Override
        public String getPasswordMech() {
            return null;
        }

        @Override
        public int getId() {
            return delegate.getId();
        }

        @Override
        public int getCreatedBy() {
            return delegate.getCreatedBy();
        }

        @Override
        public boolean isGuest() {
            return delegate.isGuest();
        }

        @Override
        public boolean isMailEnabled() {
            return delegate.isMailEnabled();
        }

        @Override
        public int getShadowLastChange() {
            return delegate.getShadowLastChange();
        }

        @Override
        public String getImapServer() {
            return delegate.getImapServer();
        }

        @Override
        public String getImapLogin() {
            return delegate.getImapLogin();
        }

        @Override
        public String getSmtpServer() {
            return delegate.getSmtpServer();
        }

        @Override
        public String getMailDomain() {
            return delegate.getMailDomain();
        }

        @Override
        public String getGivenName() {
            return Integer.toString(delegate.getId());
        }

        @Override
        public String getSurname() {
            return i18n;
        }

        @Override
        public String getMail() {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[0];
        }

        @Override
        public Map<String, String> getAttributes() {
            return new HashMap<String, String>(0);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getTimeZone() {
            return delegate.getTimeZone();
        }

        @Override
        public String getPreferredLanguage() {
            return delegate.getPreferredLanguage();
        }

        @Override
        public Locale getLocale() {
            return delegate.getLocale();
        }

        @Override
        public int[] getGroups() {
            return delegate.getGroups();
        }

        @Override
        public int getContactId() {
            return delegate.getContactId();
        }

        @Override
        public String getLoginInfo() {
            return delegate.getLoginInfo();
        }

        @Override
        public String[] getFileStorageAuth() {
            return delegate.getFileStorageAuth();
        }

        @Override
        public long getFileStorageQuota() {
            return delegate.getFileStorageQuota();
        }

        @Override
        public int getFilestoreId() {
            return delegate.getFilestoreId();
        }

        @Override
        public String getFilestoreName() {
            return delegate.getFilestoreName();
        }

        @Override
        public int getFileStorageOwner() {
            return delegate.getFileStorageOwner();
        }

    }

}
