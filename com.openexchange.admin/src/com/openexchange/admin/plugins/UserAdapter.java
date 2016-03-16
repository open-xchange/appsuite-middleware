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

package com.openexchange.admin.plugins;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.LocaleTools;


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
    public int getId() {
        return delegate.getId();
    }

    @Override
    public boolean isMailEnabled() {
        return delegate.getMailenabled();
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
    public Map<String, Set<String>> getAttributes() {
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
