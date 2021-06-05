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

package com.openexchange.user.json.anonymizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.user.User;

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

        // Anonymize the user
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
        public byte[] getSalt() {
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
