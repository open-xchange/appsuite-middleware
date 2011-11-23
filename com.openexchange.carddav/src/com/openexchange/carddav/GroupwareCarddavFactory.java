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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.carddav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * {@link GroupwareCarddavFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GroupwareCarddavFactory extends AbstractWebdavFactory {

    private static final Log LOG = LogFactory.getLog(GroupwareCarddavFactory.class);

    public static final CarddavProtocol PROTOCOL = new CarddavProtocol();

    private final FolderService folders;

    private final SessionHolder sessionHolder;

    private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();

    public GroupwareCarddavFactory(FolderService folders, SessionHolder sessionHolder) {
        super();
        this.folders = folders;
        this.sessionHolder = sessionHolder;
    }

    @Override
    public void beginRequest() {
        super.beginRequest();
        stateHolder.set(new State(this));
    }

    @Override
    public void endRequest(int status) {
        stateHolder.set(null);
        super.endRequest(status);
    }

    public CarddavProtocol getProtocol() {
        return PROTOCOL;
    }

    public WebdavCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
        if (url.size() > 1) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, 404);
        }
        if (isRoot(url)) {
            return mixin(new RootCollection(this));
        }
        return mixin(new AggregatedCollection(url, this));
    }

    public WebdavCollection resolveChild(WebdavCollection collection, WebdavPath url) throws WebdavProtocolException {
        for (WebdavResource resource : collection) {
            if (resource.getUrl().equals(url)) {
                return mixin((AbstractCollection) resource);
            }
        }
        throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, 404);
    }

    // TODO: i18n

    public boolean isRoot(WebdavPath url) {
        return url.size() == 0;
    }

    public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        if (url.size() == 2) {
            return mixin(((AggregatedCollection) resolveCollection(url.parent())).getChild(url.name()));
        }
        return resolveCollection(url);
    }

    public FolderService getFolderService() {
        return folders;
    }

    public Context getContext() {
        return sessionHolder.getContext();
    }

    public Session getSession() {
        return sessionHolder.getSessionObject();
    }

    public User getUser() {
        return sessionHolder.getUser();
    }

    public ContactSQLInterface getContactInterface() throws OXException {
        return new RdbContactSQLImpl(getSession());
    }

    public State getState() {
        return stateHolder.get();
    }

    public OXFolderAccess getOXFolderAccess() {
        return new OXFolderAccess(getContext());
    }


    public static final class State {

        private final static int[] FIELDS_FOR_ALL_REQUEST = { DataObject.OBJECT_ID };



        private final GroupwareCarddavFactory factory;

        public State(GroupwareCarddavFactory factory) {
            this.factory = factory;
        }

        private final Map<Integer, Contact> contactCache = new HashMap<Integer, Contact>();

        private final Map<Integer, List<Contact>> folderCache = new HashMap<Integer, List<Contact>>();

        public void cacheFolder(int folderId) throws OXException {
            cacheFolderSlow(folderId);
        }

        public void cacheFolderSlow(int folderId) throws OXException {
            if (folderCache.containsKey(folderId)) {
                return;
            }
            ContactSQLInterface contacts = factory.getContactInterface();
            try {
                SearchIterator<Contact> iterator = contacts.getContactsInFolder(folderId, 0, 0, -1, null, null, FIELDS_FOR_ALL_REQUEST);
                List<Contact> children = new LinkedList<Contact>();
                while (iterator.hasNext()) {
                    Contact contact = iterator.next();

                    contact = contacts.getObjectById(contact.getObjectID(), folderId);
                    children.add(contact);
                    contactCache.put(contact.getObjectID(), contact);
                }
                folderCache.put(folderId, children);

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        public Contact get(int id, int folderId) throws OXException {
            cacheFolder(folderId);
            Contact contact = contactCache.get(id);
            return contact;
        }

        public List<Contact> getFolder(int id) throws OXException {
            cacheFolder(id);
            List<Contact> contacts = folderCache.get(id);
            if (contacts == null) {
                return Collections.emptyList();
            }
            return contacts;
        }

        public List<Contact> getAggregatedContacts() throws OXException {

            List<Contact> contacts = new ArrayList<Contact>();

            //TODO: Karsten: Merge all of these
            // ContactSimilarity.areSimilar(original, candidate);

            contacts.addAll(getFolder(getStandardContactFolderId()));
            contacts.addAll(getFolder(6));

            return contacts;
        }

        public int getStandardContactFolderId() throws OXException {
            final OXFolderAccess acc = factory.getOXFolderAccess();
            return acc.getDefaultFolder(factory.getUser().getId(), FolderObject.CONTACT).getObjectID();
        }

    }


}
