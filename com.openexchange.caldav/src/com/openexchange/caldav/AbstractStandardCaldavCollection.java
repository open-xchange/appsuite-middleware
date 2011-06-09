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

package com.openexchange.caldav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

/**
 * An {@link AbstractStandardCaldavCollection} is the root class of certain caldav collection models. It represents, essentially, a read only caldav collection.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractStandardCaldavCollection extends AbstractCollection {

    protected final static ContentType CALENDAR_CTYPE = CalendarContentType.getInstance();
    
    protected GroupwareCaldavFactory factory;
    
    
    public AbstractStandardCaldavCollection(GroupwareCaldavFactory factory) {
        this.factory = factory;

    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        throw new WebdavProtocolException(GroupwareCaldavFactory.ROOT_URL, HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) throws WebdavProtocolException {
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) throws WebdavProtocolException {
        // IGNORE
    }

    @Override
    protected boolean isset(Property p) {
        if (p.getId() == Protocol.GETCONTENTLANGUAGE || p.getId() == Protocol.GETCONTENTLENGTH || p.getId() == Protocol.GETETAG) {
            return false;
        }
        return true;
    }

    @Override
    public void setCreationDate(Date date) throws WebdavProtocolException {

    }

    public void create() throws WebdavProtocolException {
        // IGNORE
    }

    public boolean exists() throws WebdavProtocolException {
        return true;
    }

    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0);
    }

    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0);
    }

    public WebdavLock getLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    public WebdavLock getOwnLock(String token) throws WebdavProtocolException {
        return null;
    }

    public List<WebdavLock> getOwnLocks() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    public String getSource() throws WebdavProtocolException {
        return null;
    }

    public void lock(WebdavLock lock) throws WebdavProtocolException {
        // IGNORE
    }

    public void save() throws WebdavProtocolException {
        // IGNORE
    }

    public void setDisplayName(String displayName) throws WebdavProtocolException {
        // IGNORE
    }

    public void unlock(String token) throws WebdavProtocolException {
        // IGNORE
    }
    
    public WebdavProtocolException internalError(AbstractOXException x) {
        return new WebdavProtocolException(getUrl(), 500);
    }
    
    protected List<WebdavResource> getVisibleCalendarFoldersOfType(Type type) throws WebdavProtocolException {
        try {
            FolderResponse<UserizedFolder[]> visibleFolders = factory.getFolderService().getVisibleFolders(FolderStorage.REAL_TREE_ID, CALENDAR_CTYPE, type, true, factory.getSession(), null);
            UserizedFolder[] response = visibleFolders.getResponse();
            List<WebdavResource> children = new ArrayList<WebdavResource>(response.length);
            for (UserizedFolder folder : response) {
                if (folder.getOwnPermission().getReadPermission() > Permission.READ_OWN_OBJECTS) {
                    children.add(new CaldavCollection(this, folder, factory));
                }
            }
            return children;
        } catch (FolderException e) {
            throw internalError(e);
        }
    }

}
