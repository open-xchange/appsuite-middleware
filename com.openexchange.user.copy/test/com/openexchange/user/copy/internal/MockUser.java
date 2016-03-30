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

package com.openexchange.user.copy.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.ldap.User;


/**
 * {@link MockUser}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockUser implements User {
    
    private static final long serialVersionUID = -2628375812844354364L;
    
    private final int uid;

    
    public MockUser(final int uid) {
        super();
        this.uid = uid;
    }
    
    /**
     * @see com.openexchange.groupware.ldap.User#getUserPassword()
     */
    public String getUserPassword() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getPasswordMech()
     */
    public String getPasswordMech() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getId()
     */
    public int getId() {
        return uid;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#isMailEnabled()
     */
    public boolean isMailEnabled() {
        return false;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getShadowLastChange()
     */
    public int getShadowLastChange() {
        return 0;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getImapServer()
     */
    public String getImapServer() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getImapLogin()
     */
    public String getImapLogin() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getSmtpServer()
     */
    public String getSmtpServer() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getMailDomain()
     */
    public String getMailDomain() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getGivenName()
     */
    public String getGivenName() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getSurname()
     */
    public String getSurname() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getMail()
     */
    public String getMail() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getAliases()
     */
    public String[] getAliases() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getAttributes()
     */
    public Map<String, Set<String>> getAttributes() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getDisplayName()
     */
    public String getDisplayName() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getTimeZone()
     */
    public String getTimeZone() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getPreferredLanguage()
     */
    public String getPreferredLanguage() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getLocale()
     */
    public Locale getLocale() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getGroups()
     */
    public int[] getGroups() {
        return null;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getContactId()
     */
    public int getContactId() {
        return 0;
    }

    /**
     * @see com.openexchange.groupware.ldap.User#getLoginInfo()
     */
    public String getLoginInfo() {
        return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uid;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MockUser other = (MockUser) obj;
        if (uid != other.uid) {
            return false;
        }
        return true;
    }

}
