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
        if(0 == userId) {
            userId = getConfig().getInt(Tree.Identifier);
        }
        return userId;
    }

    public int getContactId() {
        if(0 == contactId) {
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
        } catch (JSONException e) {
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

    public boolean hasOXUpdater() {
        if (!hasOXUpdaterSet) {
            hasOXUpdater = getConfig().getBool(Tree.OXUpdater);
            hasOXUpdaterSet = true;
        }
        return hasOXUpdater;
    }

    public TimeZone getTimeZone() {
        if(null == timezone) {
            timezone = TimeZone.getTimeZone(getConfig().getString(Tree.TimeZone));
        }
        return timezone;
    }

    public Locale getLocale() throws FixtureException {
        if (null == locale) {
            try {
                final String[] language_country = getConfig().getString(Tree.Language).split("_");
                locale = new Locale(language_country[0], language_country[1]);
            } catch (final Exception e) {
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
        return (null != this.login ? this.login.equals(that.login) : null == that.login) &&
	    	(null != this.password ? this.password.equals(that.password) : null == that.password);
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
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
