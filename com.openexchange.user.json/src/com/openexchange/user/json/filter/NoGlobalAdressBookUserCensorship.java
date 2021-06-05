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

package com.openexchange.user.json.filter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import com.openexchange.user.User;
import com.openexchange.user.json.field.UserField;


/**
 * {@link NoGlobalAdressBookUserCensorship}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NoGlobalAdressBookUserCensorship implements UserCensorship {

    @Override
    public User censor(final User user) {
        return new FilteredUser(user);
    }

    private static final class FilteredUser implements User {

        private static final long serialVersionUID = 910610141809461770L;

        private final User delegate;

        FilteredUser(final User delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public String[] getAliases() {
            if (UserField.isProtected(UserField.ALIASES.getColumn())) {
                return null;
            }
            return delegate.getAliases();
        }

        @Override
        public Map<String, String> getAttributes() {
            return Collections.emptyMap();
        }

        @Override
        public int getContactId() {
            if (UserField.isProtected(UserField.CONTACT_ID.getColumn())) {
                return -1;
            }
            return delegate.getContactId();
        }

        @Override
        public String getDisplayName() {
            if (UserField.isProtected(UserField.DISPLAY_NAME.getColumn())) {
                return null;
            }
            return delegate.getDisplayName();
        }

        @Override
        public String getGivenName() {
            if (UserField.isProtected(UserField.FIRST_NAME.getColumn())) {
                return null;
            }
            return delegate.getGivenName();
        }

        @Override
        public int[] getGroups() {
            if (UserField.isProtected(UserField.GROUPS.getColumn())) {
                return null;
            }
            return delegate.getGroups();
        }

        @Override
        public int getId() {
            if (UserField.isProtected(UserField.ID.getColumn())) {
                return -1;
            }
            return delegate.getId();
        }

        @Override
        public String getImapLogin() {
            return null;
        }

        @Override
        public String getImapServer() {
            return null;
        }

        @Override
        public Locale getLocale() {
            if (UserField.isProtected(UserField.LOCALE.getColumn())) {
                return null;
            }
            return delegate.getLocale();
        }

        @Override
        public String getLoginInfo() {
            if (UserField.isProtected(UserField.LOGIN_INFO.getColumn())) {
                return null;
            }
            return delegate.getLoginInfo();
        }

        @Override
        public String getMail() {
            return null;
        }

        @Override
        public String getMailDomain() {
            return null;
        }

        @Override
        public String getPasswordMech() {
            return null;
        }

        @Override
        public String getPreferredLanguage() {
            return null;
        }

        @Override
        public int getShadowLastChange() {
            return -1;
        }

        @Override
        public String getSmtpServer() {
            return null;
        }

        @Override
        public String getSurname() {
            if (UserField.isProtected(UserField.LAST_NAME.getColumn())) {
                return null;
            }
            return delegate.getSurname();
        }

        @Override
        public String getTimeZone() {
            if (UserField.isProtected(UserField.TIME_ZONE.getColumn())) {
                return null;
            }
            return delegate.getTimeZone();
        }

        @Override
        public String getUserPassword() {
            return null;
        }

        @Override
        public byte[] getSalt() {
            return null;
        }

        @Override
        public boolean isMailEnabled() {
            return false;
        }

        @Override
        public boolean isGuest() {
            return delegate.isGuest();
        }

        @Override
        public int getCreatedBy() {
            return delegate.getCreatedBy();
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
