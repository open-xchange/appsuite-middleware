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

package com.openexchange.admin.plugins;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.user.User;


/**
 * Takes a {@link com.openexchange.admin.rmi.dataobjects.User} and adapts it to a {@link User}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class UserAdapter implements User {

    private static final long serialVersionUID = 3486332519377510300L;

    private final com.openexchange.admin.rmi.dataobjects.User delegate;

    public UserAdapter(com.openexchange.admin.rmi.dataobjects.User delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String getUserPassword() {
        return delegate.getPassword();
    }

    @Override
    public String getPasswordMech() {
        return delegate.getPasswordMech();
    }

    @Override
    public byte[] getSalt() {
        return delegate.getSalt();
    }

    @Override
    public int getId() {
        return delegate.getId().intValue();
    }

    @Override
    public boolean isMailEnabled() {
        return delegate.getMailenabled().booleanValue();
    }

    @Override
    public int getShadowLastChange() {
        return 0;
    }

    @Override
    public String getImapServer() {
        return delegate.getImapServerString();
    }

    @Override
    public String getImapLogin() {
        return delegate.getImapLogin();
    }

    @Override
    public String getSmtpServer() {
        return delegate.getSmtpServerString();
    }

    @Override
    public String getMailDomain() {
        return null;
    }

    @Override
    public String getGivenName() {
        return delegate.getGiven_name();
    }

    @Override
    public String getSurname() {
        return delegate.getSur_name();
    }

    @Override
    public String getMail() {
        return delegate.getPrimaryEmail();
    }

    @Override
    public String[] getAliases() {
        final Set<String> aliases = delegate.getAliases();
        return null == aliases ? null : aliases.toArray(new String[aliases.size()]);
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplay_name();
    }

    @Override
    public String getTimeZone() {
        return delegate.getTimezone();
    }

    @Override
    public String getPreferredLanguage() {
        return delegate.getLanguage();
    }

    @Override
    public Locale getLocale() {
        return LocaleTools.getLocale(delegate.getLanguage());
    }

    @Override
    public int[] getGroups() {
        return new int[0];
    }

    @Override
    public int getContactId() {
        return -1;
    }

    @Override
    public String getLoginInfo() {
        return delegate.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof UserAdapter) {
            return delegate.equals(((UserAdapter) obj).delegate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean isGuest() {
        return false;
    }

    @Override
    public int getCreatedBy() {
        return 0;
    }

    @Override
    public String[] getFileStorageAuth() {
        return new String[2];
    }

    @Override
    public long getFileStorageQuota() {
        return 0;
    }

    @Override
    public int getFilestoreId() {
        return -1;
    }

    @Override
    public String getFilestoreName() {
        return null;
    }

    @Override
    public int getFileStorageOwner() {
        return -1;
    }

}
