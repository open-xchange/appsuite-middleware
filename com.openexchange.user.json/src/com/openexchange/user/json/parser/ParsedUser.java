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

package com.openexchange.user.json.parser;

import java.util.Locale;
import java.util.Map;
import com.openexchange.user.User;

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
    public byte[] getSalt() {
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
