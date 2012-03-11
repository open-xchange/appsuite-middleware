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

package com.openexchange.carddav;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.carddav.GroupwareCarddavFactory.State;
import com.openexchange.carddav.mixins.CTag;
import com.openexchange.carddav.mixins.SupportedReportSet;
import com.openexchange.carddav.mixins.SyncToken;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.acl.mixins.CurrentUserPrivilegeSet;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link AggregatedCollection}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AggregatedCollection extends AbstractCarddavCollection {

    private static final Log LOG = LogFactory.getLog(AggregatedCollection.class);

	/**
	 * Initializes a new {@link AggregatedCollection}
	 * @param url
	 * @param factory
	 * @throws WebdavProtocolException
	 */
    public AggregatedCollection(final WebdavPath url, final GroupwareCarddavFactory factory) throws WebdavProtocolException {
        super(factory, url);
        try {
        	final UserizedFolder folder = this.factory.getState().getFolder(
        			Integer.toString(this.factory.getState().getDefaultFolderId()));
        	final Permission permission = folder.getOwnPermission();        	
            super.includeProperties(
            		new SupportedReportSet(), 
            		new CTag(factory), 
            		new SyncToken(factory), 
            		new CurrentUserPrivilegeSet(permission))
            ;
        } catch (final Exception e) {
            throw internalError(e);
        }
    }

    @Override
	public List<WebdavResource> getChildren() throws WebdavProtocolException {
        try {
        	/*
        	 * get child resources
        	 */
            final State state = factory.getState();
            final Collection<Contact> contacts = state.getContacts();
            final List<UserizedFolder> folders = state.getFolders();
            final List<WebdavResource> children = new ArrayList<WebdavResource>(
            		(null != contacts ? contacts.size() : 0) + (null != folders ? folders.size() : 0));
            /*
             * add contacts
             */
            for (final Contact contact : contacts) {
                if (contact.getMarkAsDistribtuionlist()) {
                    continue;
                }
                children.add(new ContactResource(this, factory, contact));
            }
            if (LOG.isDebugEnabled()) {
            	LOG.debug(this.getUrl() + ": added " + contacts.size() + " contact resources.");
            }
            /*
             * add folders
             */
            for (final UserizedFolder userizedFolder : folders) {
    			children.add(new FolderGroupResource(this, this.factory, userizedFolder));
            }            
            if (LOG.isDebugEnabled()) {
            	LOG.debug(this.getUrl() + ": added " + folders.size() + " folder group resources.");
            }
            return children;
		} catch (final OXException e) {
            throw internalError(e);
		}
    }

    @Override
	public String getDisplayName() throws WebdavProtocolException {
        return "Contacts";
    }

    /**
     * Gets a child resource by its name
     * 
     * @param name
     * @return
     * @throws WebdavProtocolException
     */
    public AbstractResource getChild(final String name) throws WebdavProtocolException {
    	try {
	    	final String folderId = Tools.extractFolderId(name);
	    	if (null != folderId) {
	    		final UserizedFolder folder = this.factory.getState().getFolder(folderId);
	    		if (null != folder) {
	    			return new FolderGroupResource(this, this.factory, folder);
	    		}
	    	} else {
	        	final String uid = Tools.extractUID(name);
	        	if (null != uid) {        	
	        		final Contact contact = this.factory.getState().load(uid);
	        		if (null != contact) {
	        			return new ContactResource(this, factory, contact);	
	        		}
	        	}
	    	}
            if (LOG.isDebugEnabled()) {
            	LOG.debug(this.getUrl() + ": child '" + name + "' not found, generating UndecidedResource.");
            }
	    	return new UndecidedResource(this, factory, getUrl().dup().append(name));
    	} catch (OXException e) {
    		throw internalError(e);
		}
    }
    
    @Override
    public String getResourceType() throws WebdavProtocolException {
        return super.getResourceType() + CarddavProtocol.ADDRESSBOOK;
    }

    public int getStandardFolder() throws OXException {
    	return this.factory.getState().getDefaultFolderId();
    }
}
