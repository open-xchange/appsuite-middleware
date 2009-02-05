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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.contactcollector.internal;

import java.util.List;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactServices;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link Memorizer}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Memorizer implements Runnable {

    private static final Log LOG = LogFactory.getLog(ServerUserSetting.class);

    private final List<InternetAddress> addresses;

    private final Session session;

    /**
     * Initializes a new {@link Memorizer}.
     * 
     * @param addresses The addresses to insert if not already present
     * @param session The associated session
     */
    public Memorizer(final List<InternetAddress> addresses, final Session session) {
        this.addresses = addresses;
        this.session = session;
    }

    public void run() {
        if (!isEnabled() || getFolderId() == 0) {
            return;
        }

        for (final InternetAddress address : addresses) {
            try {
                memorizeContact(address, session);
            } catch (final AbstractOXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private int memorizeContact(final InternetAddress address, final Session session) throws AbstractOXException {
        final ContactObject contact = transformInternetAddress(address);
        final Context ctx;
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException ct) {
            throw new ContactException(ct);
        }
        final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx);
        ContactInterface contactInterface = ContactServices.getInstance().getService(contact.getParentFolderID(), ctx.getContextId());
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(session, ctx);
        }
        contactInterface.setSession(session);

        ContactObject foundContact = null;
        {
            final ContactSearchObject searchObject = new ContactSearchObject();
            searchObject.setEmailAutoComplete(true);
            searchObject.setDynamicSearchField(new int[] { ContactObject.EMAIL1, ContactObject.EMAIL2, ContactObject.EMAIL3, });
            searchObject.setDynamicSearchFieldValue(new String[] { contact.getEmail1(), contact.getEmail1(), contact.getEmail1() });
            final int[] columns = new int[] { 
                DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, DataObject.LAST_MODIFIED, ContactObject.USERFIELD20 };
            final SearchIterator<ContactObject> iterator = contactInterface.getContactsByExtendedSearch(searchObject, 0, null, columns);
            try {
                if (iterator.hasNext()) {
                    foundContact = iterator.next();
                }
            } finally {
                iterator.close();
            }
        }

        final int retval;
        if (null == foundContact) {
            contactInterface.insertContactObject(contact);
            retval = contact.getObjectID();
        } else {
            try {
                foundContact.setUserField20(String.valueOf(Integer.parseInt(foundContact.getUserField20() + 1)));
            } catch (final NumberFormatException nfe) {
                foundContact.setUserField20(String.valueOf(1));
            }
            final OCLPermission perm = new OXFolderAccess(ctx).getFolderPermission(
                foundContact.getParentFolderID(),
                session.getUserId(),
                userConfig);
            if (perm.canWriteAllObjects()) {
                contactInterface.updateContactObject(foundContact, foundContact.getParentFolderID(), foundContact.getLastModified());
            }
            retval = foundContact.getObjectID();
        }
        return retval;
    }

    private int getFolderId() {
        int retval = 0;
        try {
            final Integer folder = ServerUserSetting.getContactCollectionFolder(session.getContextId(), session.getUserId());
            if (null != folder) {
                retval = folder.intValue();
            }
        } catch (final SettingException e) {
            LOG.error(e.getMessage(), e);
        }
        return retval;
    }

    private boolean isEnabled() {
        Boolean enabled = null;
        try {
            enabled = ServerUserSetting.isContactCollectionEnabled(session.getContextId(), session.getUserId());
        } catch (final SettingException e) {
            LOG.error(e.getMessage(), e);
        }
        return enabled != null && enabled.booleanValue();
    }

    private ContactObject transformInternetAddress(final InternetAddress address) {
        final ContactObject retval = new ContactObject();
        retval.setEmail1(address.getAddress());
        final String displayName;
        if (address.getPersonal() != null && !"".equals(address.getPersonal().trim())) {
            displayName = address.getPersonal();
        } else {
            displayName =address.getAddress();
        }
        retval.setDisplayName(displayName);
        retval.setParentFolderID(getFolderId());
        retval.setUserField20(String.valueOf(1));
        return retval;
    }
}
