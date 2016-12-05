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

package com.openexchange.user.json.parser;

import java.util.Locale;
import java.util.Map;
import com.openexchange.groupware.ldap.User;

/**
 * {@link ParsedUser} - A parsed user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ParsedUser implements User {

    private static final long serialVersionUID = 3853026806561002845L;

    private int id;

    private Locale locale;

    private String timeZone;

    /**
     * Initializes a new {@link ParsedUser}.
     */
    public ParsedUser() {
        super();
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return null;
    }

    @Override
    public int getContactId() {
        return -1;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getGivenName() {
        return null;
    }

    @Override
    public int[] getGroups() {
        return null;
    }

    @Override
    public int getId() {
        return id;
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
        return locale;
    }

    @Override
    public String getLoginInfo() {
        return null;
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
        return null == locale ? null : locale.toString();
    }

    @Override
    public int getShadowLastChange() {
        return 0;
    }

    @Override
    public String getSmtpServer() {
        return null;
    }

    @Override
    public String getSurname() {
        return null;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    @Override
    public String getUserPassword() {
        return null;
    }

    @Override
    public boolean isMailEnabled() {
        return true;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the locale
     *
     * @param locale The locale to set
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the time zone
     *
     * @param timeZone The time zone to set
     */
    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
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
