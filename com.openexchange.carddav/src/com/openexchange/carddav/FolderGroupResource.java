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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.contact.ContactService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link FolderGroupResource}
 * 
 * CardDAV resource for folder groups.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderGroupResource extends CarddavResource {

    private UserizedFolder folder = null;
    
    private FolderObject folderToSave = null;

    private List<String> contactUIDsToAddToFolder = null;
    
    private Date lastModified = null;

	public FolderGroupResource(final AggregatedCollection parent, final GroupwareCarddavFactory factory, final UserizedFolder folder) {
		super(parent, factory);
    	if (null == folder) {
    		throw new IllegalArgumentException("folder");
    	}
		this.folder = folder;
	}

    public FolderGroupResource(final AggregatedCollection parent, final GroupwareCarddavFactory factory, final VersitObject versitObject, 
    		final WebdavPath url) throws WebdavProtocolException {
    	super(parent, factory);
    	this.applyVersitObject(versitObject);
    }
	
    /**
     * Overrides the currently stored last modification time of this folder with the supplied value.
     * @param folderLastModified
     */
	public void overrrideLastModified(final Date folderLastModified) {
		this.lastModified = folderLastModified;
	}
    
	@Override
	public void create() throws WebdavProtocolException {
		if (CarddavProtocol.REDUCED_FOLDER_SET) {
			LOG.debug("Creating folders not supported in reduced folder set. Overriding next sync token for client recovery.");
			this.factory.overrideNextSyncToken();
			return; // don't throw an exception here!
    	} else if (this.exists()) {
			throw super.protocolException(HttpServletResponse.SC_CONFLICT);
		} else if (null == this.folderToSave) {
			throw super.protocolException(HttpServletResponse.SC_NOT_FOUND);
    	}
		/*
		 * Create folder
		 */
    	try {
        	final OXFolderManager folderManager = this.factory.getOXFolderManager();
        	this.folderToSave = folderManager.createFolder(folderToSave, true, new Date().getTime());
            LOG.debug(this.getUrl() + ": created.");
    	} catch (final OXException e) {
			if (Category.CATEGORY_PERMISSION_DENIED == e.getCategory()) {
		        throw super.protocolException(HttpServletResponse.SC_FORBIDDEN);
			} else {
				throw super.internalError(e);
			}
        }
    	/*
    	 * Update contents
    	 */
    	if (null != this.contactUIDsToAddToFolder) {
    		this.updateContents();
    	}
	}

	@Override
	public boolean exists() {
		return null != this.folder;
	}

	@Override
	public void delete() throws WebdavProtocolException {
		if (CarddavProtocol.REDUCED_FOLDER_SET) {
			LOG.debug("Deleting folders not supported in reduced folder set. Overriding next sync token for client recovery.");
			this.factory.overrideNextSyncToken();			
			return; // don't throw an exception here!
    	} else if (false == this.exists() || null == this.folderToSave) {
			throw super.protocolException(HttpServletResponse.SC_NOT_FOUND);
		}
		/*
		 * Delete folder
		 */
        final FolderObject folderObject = new FolderObject();
        folderObject.setObjectID(Integer.parseInt(folder.getID()));
        folderObject.setParentFolderID(Integer.parseInt(folder.getParentID()));
        try {
            factory.getOXFolderManager().deleteFolder(folderObject, true, System.currentTimeMillis());
            //TODO: separate handling of permission based exceptions
            this.folder = null;
        } catch (final OXException e) {
        	throw internalError(e);
        }
	}

	@Override
	public void save() throws WebdavProtocolException {
		if (CarddavProtocol.REDUCED_FOLDER_SET) {
			/*
			 * Contact creation only allowed in default folder
			 */
			try {
				if (this.exists() && null != this.folderToSave && this.parent.getStandardFolder() == this.folderToSave.getObjectID()) {
					// group member change, ignore since contact is created in default folder anyway
					return;
				} else {
					LOG.debug("Only default folder contents may be modified, overriding next sync token for client recovery.");
					this.factory.overrideNextSyncToken();
					return; // don't throw an exception here!
				}
			} catch (final OXException e) {
				throw super.internalError(e);
			}
		} else if (false == this.exists()) {
			throw super.protocolException(HttpServletResponse.SC_NOT_FOUND);
		} 
		/*
		 * Update folder 
		 */
    	try {
        	final OXFolderManager folderManager = this.factory.getOXFolderManager();
        	this.folderToSave = folderManager.updateFolder(this.folderToSave, true, new Date().getTime());
            LOG.debug(this.getUrl() + ": updated.");
    	} catch (final OXException e) {
			if (Category.CATEGORY_PERMISSION_DENIED == e.getCategory()) {
				throw super.protocolException(e, HttpServletResponse.SC_FORBIDDEN);
			} else {
				throw super.internalError(e);
			}
        }
    	/*
    	 * Update contents
    	 */
    	if (null != this.contactUIDsToAddToFolder) {
    		this.updateContents();
    	}
    }
	
	private void updateContents() throws WebdavProtocolException {
        if (null != this.contactUIDsToAddToFolder && 0 < this.contactUIDsToAddToFolder.size()) {
        	final int newFolderID = this.folderToSave.getObjectID();
        	try {
        		final ContactService contactService = this.factory.getContactService();
            	for (final String uid : this.contactUIDsToAddToFolder) {
					Contact contact = this.factory.getState().get(uid);
					if (null == contact) {
						/*
						 * New contact
						 */
		                LOG.warn(this.getUrl() + ": unknown uid, creating place holder resource for " + uid + ".");
		                contact = new Contact();
		                contact.setUid(uid);
						contact.setParentFolderID(newFolderID);
						contactService.createContact(factory.getSession(), Integer.toString(newFolderID), contact);
		                LOG.debug(this.getUrl() + ": created " + uid + ".");
					} else {
						/*
						 * Move contact
						 */
						final int previousFolderID = contact.getParentFolderID();
						contact.setParentFolderID(newFolderID);
						contactService.updateContact(factory.getSession(), Integer.toString(previousFolderID), 
								Integer.toString(contact.getObjectID()), contact, contact.getLastModified());
		                LOG.debug(this.getUrl() + ": moved " + uid + ".");
					}
            	}
            	//TODO: deleted contacts?
			} catch (final OXException e) {
				throw super.internalError(e);
			}
        }
	}
        
	@Override
	public Date getCreationDate() throws WebdavProtocolException {
    	return this.exists() ? this.folder.getCreationDate() : new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		if (null == this.lastModified && this.exists()) {
			try {
				this.lastModified = this.factory.getState().getLastModified(this.folder);
			} catch (final OXException e) {
				throw internalError(e);
			}
		} 
		return null != this.lastModified ? this.lastModified : new Date(0);
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		if (this.exists()) {
	    	final Locale locale = this.factory.getUser().getLocale();
	    	final String name = null != locale ? this.folder.getLocalizedName(locale) : this.folder.getName();
	    	if (SharedType.getInstance().equals(this.folder.getType())) {
	    		String ownerName = null;
	            final Permission[] permissions = this.folder.getPermissions();
	            for (final Permission permission : permissions) {
	                if (permission.isAdmin()) {
	                    int entity = permission.getEntity();
	                    try {
	                        ownerName = factory.resolveUser(entity).getDisplayName();
	                    } catch (WebdavProtocolException e) {
	                        LOG.error(e.getMessage(), e);
	                        ownerName = new Integer(entity).toString();
	                    }
	                    break;
	                }
	            }	    		
	    		return String.format("%s (%s)", name, ownerName);
	    	} else {
	    		return name;
	    	}
		} else {
	    	return null;
		}
	}

	@Override
	public void setDisplayName(final String displayName) throws WebdavProtocolException {
		if (CarddavProtocol.REDUCED_FOLDER_SET) {
    		throw super.protocolException(HttpServletResponse.SC_FORBIDDEN);
		} else if (false == this.exists()) {
			throw protocolException(HttpServletResponse.SC_NOT_FOUND);
		} else {
			//TODO: remove possibly duplicate owner information
			this.folder.setName(displayName);
		}
	}

	@Override
	protected void applyVersitObject(final VersitObject versitObject) throws WebdavProtocolException {
        	/*
        	 * Deserialize folder
        	 */
        	this.folderToSave = new FolderObject();
        	this.folderToSave.setFolderName(versitObject.getProperty("FN").getValue().toString());
        	this.folderToSave.setModule(FolderObject.CONTACT);
        	this.folderToSave.setLastModified(new Date());
        	this.folderToSave.setModifiedBy(this.factory.getSession().getUserId());
            if (this.exists()) {
            	/*
            	 * Take over metadata from existing folder
            	 */
            	this.folderToSave.setType(this.folder.getType().getType());
            	this.folderToSave.setParentFolderID(Integer.parseInt(this.folder.getParentID()));
            	this.folderToSave.setObjectID(Integer.parseInt(this.folder.getID()));
            	this.folderToSave.setModule(FolderObject.CONTACT);
            } else {
            	/*
            	 * Apply default metadata
            	 */
            	try {
					this.folderToSave.setParentFolderID(factory.getState().getDefaultFolderId());
				} catch (final OXException e) {
					super.internalError(e);
				}
//                this.folderToSave.setParentFolderID(factory.getState().getStandardContactFolderId());
            	this.folderToSave.setType(FolderObject.PRIVATE);
            	this.folderToSave.setModule(FolderObject.CONTACT);
            	this.folderToSave.setPermissionsAsArray(this.generateDefaultPermissions());
            }
            /*
             * Deserialize contents
             */
        	this.contactUIDsToAddToFolder = extractMembers(versitObject);
	}
	
    private static String extractMemberUID(final com.openexchange.tools.versit.Property property) {
    	String uid = null;
    	final Object value = property.getValue();
    	if (null != value) {
    		uid = (String)value;
        	final String prefix = "urn:uuid:";
    		if (uid.toLowerCase().startsWith(prefix) && prefix.length() < uid.length()) {
    			uid = uid.substring(prefix.length());
    		}
    	} 
    	return uid;
    }
    
    private static List<String> extractMembers(final VersitObject versitObject) {
    	final List<String> uids = new ArrayList<String>();
        for (int i = 0, size = versitObject.getPropertyCount(); i < size; i++) {
            final com.openexchange.tools.versit.Property property = versitObject.getProperty(i);
            if (property.getName().equals("X-ADDRESSBOOKSERVER-MEMBER")) {
            	final String uid = extractMemberUID(property);
            	if (null == uid) {
            		LOG.warn("Got no value for 'X-ADDRESSBOOKSERVER-MEMBER' property, skipping.");
            		continue;
            	} else {
            		uids.add(uid);
            	}
            }
        }    	
    	return uids;
    }

	private OCLPermission[] generateDefaultPermissions() {
		final OCLPermission perm = new OCLPermission();
        perm.setEntity(this.factory.getSession().getUserId());
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
        perm.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
        perm.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
        perm.setGroupPermission(false);
        return new OCLPermission[] { perm };
	}

	@Override
	protected String generateVCard() throws WebdavProtocolException {
        final StringBuilder stringBuilder = new StringBuilder();
        final String name = this.getDisplayName();
        stringBuilder
        	.append("BEGIN:VCARD\n")
        	.append("VERSION:3.0\n")
        	.append("X-ADDRESSBOOKSERVER-KIND:group\n")
        	.append("N:").append(name).append('\n')
        	.append("FN:").append(name).append('\n')
        	.append("UID:").append(this.getUID()).append('\n')
        ;
        try {
            final List<Contact> contacts = this.factory.getState().getContacts(Integer.parseInt(folder.getID()));
            for (final Contact contact : contacts) {
                stringBuilder.append("X-ADDRESSBOOKSERVER-MEMBER:urn:uuid:").append(contact.getUid()).append('\n');
            }
        } catch (final OXException e) {
        	throw internalError(e);
        }
        stringBuilder.append("END:VCARD");
        return stringBuilder.toString();
	}

	@Override
	protected String getUID() {
    	return this.exists() ? String.format("f%d_%s", this.factory.getSession().getContextId(), this.folder.getID()) : null;
	}
}
