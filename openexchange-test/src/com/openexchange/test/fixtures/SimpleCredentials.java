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

package com.openexchange.test.fixtures;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.groupware.container.Contact;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Stefan Preuss <stefan.preuss@open-xchange.com>
 */
public class SimpleCredentials implements Cloneable {

    private String login;
    private String imapLogin;
    private String password;
    private Contact contact;
    private TestUserConfig config;
    private TestUserConfigFactory userConfigFactory = null;
    private final ContactFinder contactFinder;
    private Locale locale = null;
    private int userId = 0;
    private int contactId = 0;
    private String privateAppointmentFolderId = null;
    private String privateTaskFolderId = null;
    private String privateContactFolderId = null;
    private String privateInfostoreFolderId = null;
    private TimeZone timezone = null;

    private boolean hasFullGroupware;
    private boolean hasFullGroupwareSet = false;
    private boolean hasActiveSync;
    private boolean hasActiveSyncSet = false;
    private boolean hasOXUpdater;
    private boolean hasOXUpdaterSet = false;

    public SimpleCredentials(final TestUserConfigFactory userConfigFactory, final ContactFinder contactFinder) {
        super();
        this.userConfigFactory = userConfigFactory;
        this.contactFinder = contactFinder;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getIMAPLogin() {
        return null == this.imapLogin ? this.login : imapLogin;
    }

    public void setIMAPLogin(final String imapLogin) {
        this.imapLogin = imapLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Contact asContact() {
        if (null == this.contact) {
            this.contact = contactFinder.getContact(this);
        }
        return contact;
    }

    public TestUserConfig getConfig() {
        if (null == config) {
            config = userConfigFactory.create(this);
        }
        return config;
    }

    public int getUserId() {
        if (0 == userId) {
            userId = getConfig().getInt(Tree.Identifier);
        }
        return userId;
    }

    public int getContactId() {
        if (0 == contactId) {
            contactId = getConfig().getInt(Tree.Identifier);
        }
        return contactId;
    }

    private void resolveFolderIDs() {
        try {
            final JSONObject obj = new JSONObject(getConfig().getString(Tree.PrivateFolders));

            privateAppointmentFolderId = obj.getString("calendar");
            privateContactFolderId = obj.getString("contacts");
            privateTaskFolderId = obj.getString("tasks");

            if (obj.has("infostore")) {
                privateInfostoreFolderId = obj.getString("infostore");
            } else {
                privateInfostoreFolderId = "";
            }
        } catch (@SuppressWarnings("unused") JSONException e) {
            // do nothing
        }
    }

    public String getPrivateAppointmentFolderId() {
        if (null == privateAppointmentFolderId) {
            resolveFolderIDs();
        }
        return privateAppointmentFolderId;
    }

    public String getPrivateTaskFolderId() {
        if (null == privateTaskFolderId) {
            resolveFolderIDs();
        }
        return privateTaskFolderId;
    }

    public String getPrivateContactFolderId() {
        if (null == privateContactFolderId) {
            resolveFolderIDs();
        }
        return privateContactFolderId;
    }

    public String getPrivateInfostoreFolderId() {
        if (null == privateInfostoreFolderId) {
            resolveFolderIDs();
        }
        return privateInfostoreFolderId;
    }

    public boolean hasFullGroupware() {
        if (!hasFullGroupwareSet) {
            hasFullGroupware = getConfig().getBool(Tree.InfostoreEnabled);
            hasFullGroupwareSet = true;
        }
        return hasFullGroupware;
    }

    public boolean hasActiveSync() {
        if (!hasActiveSyncSet) {
            hasActiveSync = getConfig().getBool(Tree.ActiveSync);
            hasActiveSyncSet = true;
        }
        return hasActiveSync;
    }

    @Deprecated
    public boolean hasOXUpdater() {
        if (!hasOXUpdaterSet) {
            hasOXUpdater = getConfig().getBool(Tree.OXUpdater);
            hasOXUpdaterSet = true;
        }
        return hasOXUpdater;
    }

    public TimeZone getTimeZone() {
        if (null == timezone) {
            timezone = TimeZone.getTimeZone(getConfig().getString(Tree.TimeZone));
        }
        return timezone;
    }

    public Locale getLocale() throws FixtureException {
        if (null == locale) {
            try {
                final String[] language_country = getConfig().getString(Tree.Language).split("_");
                locale = new Locale(language_country[0], language_country[1]);
            } catch (Exception e) {
                throw new FixtureException("Unable to determine locale.", e);
            }
        }
        return locale;
    }

    public void resetLocal() {
        config = null;
        locale = null;
    }

    public Calendar getCalendar() {
        final Calendar calendar = Calendar.getInstance(getTimeZone());
        calendar.setMinimalDaysInFirstWeek(4);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleCredentials that = (SimpleCredentials) o;
        return (null != this.login ? this.login.equals(that.login) : null == that.login) && (null != this.password ? this.password.equals(that.password) : null == that.password);
    }

    @Override
    public int hashCode() {
        int result;
        result = (login != null ? login.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("SimpleCredentials[%s]", null != this.getLogin() ? this.getLogin() : "");
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
