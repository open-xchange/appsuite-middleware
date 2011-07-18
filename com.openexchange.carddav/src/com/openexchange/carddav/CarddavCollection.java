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
import java.util.List;
import com.openexchange.carddav.GroupwareCarddavFactory.State;
import com.openexchange.carddav.mixins.CTag;
import com.openexchange.carddav.mixins.SupportedReportSet;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CarddavCollection}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CarddavCollection extends AbstractCarddavCollection {

    private RootCollection parent;

    private UserizedFolder folder;

    /**
     * Initializes a new {@link CarddavCollection}.
     * 
     * @param rootCollection
     * @param folder
     * @param factory
     * @throws WebdavProtocolException 
     */
    public CarddavCollection(RootCollection parent, UserizedFolder folder, WebdavPath url, GroupwareCarddavFactory factory) throws WebdavProtocolException {
        super(factory, url);
        this.parent = parent;
        this.folder = folder;
        
        try {
            includeProperties(
                new CurrentUserPrivilegeSet(folder.getOwnPermission()),
                new SupportedReportSet(),
                new CTag(factory.getState().getFolder(getId()), getId())
            );
        } catch (ContextException e) {
            throw internalError(e);
        }

    }

    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        State state = factory.getState();
        List<Contact> contacts;
        try {
            contacts = state.getFolder(getId());

            List<WebdavResource> children = new ArrayList<WebdavResource>(contacts.size());

            for (Contact contact : contacts) {
                if (contact.getMarkAsDistribtuionlist()) {
                    continue;
                }
                CarddavResource resource = new CarddavResource(this, contact, factory);
                children.add(resource);
            }

            return children;
        } catch (ContextException e) {
            throw internalError(e);
        }
    }

    public int getId() {
        return Integer.parseInt(folder.getID());
    }

    public String getDisplayName() throws WebdavProtocolException {
        return folder.getName();
    }

    public CarddavResource getChild(String name) throws WebdavProtocolException {
        try {
            State state = factory.getState();
            // TODO: Robustness
            int id = Integer.parseInt(name.substring(0, name.indexOf('.')));
            Contact contact = state.get(id, getId());
            if (contact != null) {
                return new CarddavResource(this, contact, factory);
            }
        } catch (ContextException x) {
            throw internalError(x);
        } catch (NumberFormatException x) {
            // Ignore, return a new CarddavResource instead
        }
        return new CarddavResource(this, factory);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.webdav.protocol.helpers.AbstractCollection#getResourceType()
     */
    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType()+CarddavProtocol.ADDRESSBOOK;
    }

}
