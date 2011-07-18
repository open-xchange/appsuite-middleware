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
import java.util.Arrays;
import java.util.List;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;


/**
 * {@link RootCollection}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RootCollection extends AbstractCarddavCollection {


    public RootCollection(GroupwareCarddavFactory factory) {
        super(factory, new WebdavPath());
    }

    
    public String getDisplayName() throws WebdavProtocolException {
        return "Contacts";
    }


    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<WebdavResource> children = new ArrayList<WebdavResource>(3);
        for (Type type : Arrays.asList(PrivateType.getInstance(), PublicType.getInstance(), SharedType.getInstance())) {
            children.addAll(getVisibleCalendarFoldersOfType(type));
        }
        return children;
    }
    
    protected final static ContentType CONTACT_CTYPE = ContactContentType.getInstance();

    
    protected List<WebdavResource> getVisibleCalendarFoldersOfType(Type type) throws WebdavProtocolException {
        try {
            FolderResponse<UserizedFolder[]> visibleFolders = factory.getFolderService().getVisibleFolders(FolderStorage.REAL_TREE_ID, CONTACT_CTYPE, type, true, factory.getSession(), null);
            UserizedFolder[] response = visibleFolders.getResponse();
            List<WebdavResource> children = new ArrayList<WebdavResource>(response.length);
            for (UserizedFolder folder : response) {
                if (folder.getOwnPermission().getReadPermission() > Permission.READ_OWN_OBJECTS) {
                    children.add(new CarddavCollection(this, folder, getUrl().dup().append(folder.getName()), factory));
                }
            }
            return children;
        } catch (FolderException e) {
            throw internalError(e);
        }
    }


   

}
