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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caldav.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.CalendarHomeSet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;

/**
 * {@link CalDAVRootCollection} 
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalDAVRootCollection extends CommonCollection {
    
    /**
     * The reserved tree identifier for MS Outlook folder tree: <code>"1"</code>.
     * (copied from com.openexchange.folderstorage.outlook)
     */
    private static final String OUTLOOK_TREE_ID = "1"; 

    private final GroupwareCaldavFactory factory;
    private String trashFolderID;
    private List<UserizedFolder> subfolders;

    /**
     * Initializes a new {@link CalDAVRootCollection}.
     * 
     * @param factory the factory
     */
    public CalDAVRootCollection(GroupwareCaldavFactory factory) {
        super(factory, new WebdavPath());
        this.factory = factory;
        includeProperties(new CalendarHomeSet());
    }
    
    @Override
    protected GroupwareCaldavFactory getFactory() {
        return this.factory;
    }
    
    protected FolderService getFolderService() {
        return factory.getFolderService();
    }

    protected List<UserizedFolder> getSubfolders() throws OXException {
        if (null == this.subfolders) {
            this.subfolders = getVisibleFolders();
        }
        return subfolders;
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0);
    }

    @Override
    public AbstractCollection getChild(String name) throws WebdavProtocolException {
        try {
            for (UserizedFolder folder : getSubfolders()) {
                if (name.equals(folder.getID())) {
                    return createCollection(folder);
                }
            }
        } catch (OXException e) {
            throw protocolException(e);
        }
        throw protocolException(HttpServletResponse.SC_NOT_FOUND);
    }
    
    private CalDAVFolderCollection<?> createCollection(UserizedFolder folder) throws OXException {
        if (TaskContentType.getInstance().equals(folder.getContentType())) {
            return new TaskCollection(factory, constructPathForChildResource(folder), folder);            
        } else if (CalendarContentType.getInstance().equals(folder.getContentType())) {
            return new AppointmentCollection(factory, constructPathForChildResource(folder), folder);            
        } else {
            throw new UnsupportedOperationException("content type " + folder.getContentType() + " not supported");
        }
    }
    
    private CalDAVFolderCollection<?> createCollection(UserizedFolder folder, int order) throws OXException {
        if (TaskContentType.getInstance().equals(folder.getContentType())) {
            return new TaskCollection(factory, constructPathForChildResource(folder), folder, order);            
        } else if (CalendarContentType.getInstance().equals(folder.getContentType())) {
            return new AppointmentCollection(factory, constructPathForChildResource(folder), folder, order);            
        } else {
            throw new UnsupportedOperationException("content type " + folder.getContentType() + " not supported");
        }
    }
    
    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<WebdavResource> children = new ArrayList<WebdavResource>();
        int calendarOrder = 0;
        try {
            for (UserizedFolder folder : getSubfolders()) {
                children.add(createCollection(folder, ++calendarOrder));
                LOG.debug(getUrl() + ": adding folder collection for folder '" + folder.getName() + "' as child resource.");
            }
        } catch (OXException e) {
            throw protocolException(e);
        }

        LOG.debug(getUrl() + ": got " + children.size() + " child resources.");
        return children;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return "Calendars";
    }

    /**
     * Constructs a string representing the WebDAV name for a folder resource.
     * 
     * @param folder the folder to construct the name for
     * @return the name
     */
    private String constructNameForChildResource(UserizedFolder folder) {
        return folder.getID();
    }

    private WebdavPath constructPathForChildResource(UserizedFolder folder) {
        return constructPathForChildResource(constructNameForChildResource(folder));
    }
    
    /**
     * Gets a list of all visible and subscribed calendar folders in the configured folder tree.
     * @return
     * @throws FolderException
     */
    private List<UserizedFolder> getVisibleFolders() throws OXException {
        List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
        folders.addAll(getVisibleFolders(PrivateType.getInstance(), CalendarContentType.getInstance()));
        folders.addAll(getVisibleFolders(PublicType.getInstance(), CalendarContentType.getInstance()));
        folders.addAll(getVisibleFolders(SharedType.getInstance(), CalendarContentType.getInstance()));
        folders.addAll(getVisibleFolders(PrivateType.getInstance(), TaskContentType.getInstance()));
        return folders;
    }
    
    /**
     * Gets a list containing all visible folders of the given {@link Type}.
     * @param type
     * @return
     * @throws FolderException 
     */
    private List<UserizedFolder> getVisibleFolders(Type type, ContentType contentType) throws OXException {
        List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
        FolderResponse<UserizedFolder[]> visibleFoldersResponse = getFolderService().getVisibleFolders(
                factory.getTreeID(), contentType, type, false, factory.getSession(), null);
        UserizedFolder[] response = visibleFoldersResponse.getResponse();
        for (UserizedFolder folder : response) {
            if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == isTrashFolder(folder)) {
                folders.add(folder);
            }
        }
        return folders;
    }
    
    /**
     * Gets the id of the default trash folder
     * 
     * @return
     */
    private String getTrashFolderID() {
        if (null == trashFolderID) {
            try {
                trashFolderID = getFolderService().getDefaultFolder(factory.getUser(), OUTLOOK_TREE_ID, 
                        TrashContentType.getInstance(), factory.getSession(), null).getID();
            } catch (OXException e) {
                LOG.warn("unable to determine default trash folder", e);
            }
        }
        return this.trashFolderID;
    }
    
    /**
     * Checks whether the supplied folder is a trash folder, i.e. one of 
     * it's parent folders is the default trash folder.
     * 
     * @param folder
     * @return
     * @throws WebdavProtocolException
     * @throws FolderException 
     */
    private boolean isTrashFolder(UserizedFolder folder) throws OXException {
        String trashFolderId = this.getTrashFolderID();
        if (null != trashFolderId) {
            FolderResponse<UserizedFolder[]> pathResponse = getFolderService().getPath(
                    OUTLOOK_TREE_ID, folder.getID(), this.factory.getSession(), null);
            UserizedFolder[] response = pathResponse.getResponse();
            for (UserizedFolder parentFolder : response) {
                if (trashFolderId.equals(parentFolder.getID())) {
                    LOG.debug("Detected folder below trash: " + folder);
                    return true;
                }
            }
        } else {
            LOG.warn("No config value for trash folder id found");
        }
        return false;
    }

}
