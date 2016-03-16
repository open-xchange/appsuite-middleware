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

package com.openexchange.user.json.filter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.ldap.User;
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

        private final User delegate;

        private FilteredUser(final User delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public String[] getAliases() {
            if(UserField.isProtected(UserField.ALIASES.getColumn())) {
                return null;
            }
            return delegate.getAliases();
        }

        @Override
        public Map<String, Set<String>> getAttributes() {
            return Collections.emptyMap();
        }

        @Override
        public int getContactId() {
            if(UserField.isProtected(UserField.CONTACT_ID.getColumn())) {
                return -1;
            }
            return delegate.getContactId();
        }

        @Override
        public String getDisplayName() {
            if(UserField.isProtected(UserField.DISPLAY_NAME.getColumn())) {
                return null;
            }
            return delegate.getDisplayName();
        }

        @Override
        public String getGivenName() {
            if(UserField.isProtected(UserField.FIRST_NAME.getColumn())) {
                return null;
            }
            return delegate.getGivenName();
        }

        @Override
        public int[] getGroups() {
            if(UserField.isProtected(UserField.GROUPS.getColumn())) {
                return null;
            }
            return delegate.getGroups();
        }

        @Override
        public int getId() {
            if(UserField.isProtected(UserField.ID.getColumn())) {
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
            if(UserField.isProtected(UserField.LOCALE.getColumn())) {
                return null;
            }
            return delegate.getLocale();
        }

        @Override
        public String getLoginInfo() {
            if(UserField.isProtected(UserField.LOGIN_INFO.getColumn())) {
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
            if(UserField.isProtected(UserField.LAST_NAME.getColumn())) {
                return null;
            }
            return delegate.getSurname();
        }

        @Override
        public String getTimeZone() {
            if(UserField.isProtected(UserField.TIME_ZONE.getColumn())) {
                return null;
            }
            return delegate.getTimeZone();
        }

        @Override
        public String getUserPassword() {
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
