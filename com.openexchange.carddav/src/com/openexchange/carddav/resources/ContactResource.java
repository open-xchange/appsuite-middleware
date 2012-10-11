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

package com.openexchange.carddav.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.mapping.CardDAVMapper;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ContactResource} - Abstract base class for CardDAV resources.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactResource extends CardDAVResource {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ContactResource.class);
	private static final OXContainerConverter CONVERTER = new OXContainerConverter((TimeZone) null, (String) null);
	private static final int MAX_RETRIES = 3;
    
    private boolean exists = false;
    private Contact contact = null;    
    private int retryCount = 0;
    private String parentFolderID = null;

    /**
     * Creates a new {@link ContactResource} representing an existing contact.
     * 
     * @param contact the contact
     * @param factory the CardDAV factory
	 * @param url the WebDAV URL
     */
	public ContactResource(Contact contact, GroupwareCarddavFactory factory, WebdavPath url) {
		super(factory, url);
		this.contact = contact;
		this.exists = null != contact;
	}

	/**
	 * Creates a new placeholder {@link ContactResource} at the specified URL.
	 * 
     * @param factory the CardDAV factory
	 * @param url the WebDAV URL
	 * @param parentFolderID the ID of the parent folder
	 * @throws WebdavProtocolException
	 */
    public ContactResource(GroupwareCarddavFactory factory, WebdavPath url, String parentFolderID) throws WebdavProtocolException {
    	this(null, factory, url);
    	this.parentFolderID = parentFolderID;
    }

	@Override
	public void create() throws WebdavProtocolException {
		if (this.exists()) {
			throw protocolException(HttpServletResponse.SC_CONFLICT);
		} else if (null == this.contact) {
			throw protocolException(HttpServletResponse.SC_NOT_FOUND);
		}
		try {
		    if (false == contact.getMarkAsDistribtuionlist()) {
	            /*
	             * Insert contact
	             */
	            this.factory.getContactService().createContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), contact);
	            LOG.debug(this.getUrl() + ": created.");
		    } else {
	            /*
	             * Insert & delete not supported contact (next sync cleans up the client) 
	             */
                LOG.warn(this.getUrl() + ": contact groups not supported, performing immediate deletion of this resource.");
		        contact.removeDistributionLists();
		        contact.removeNumberOfDistributionLists();
                this.factory.getContactService().createContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), contact);
                this.factory.getContactService().deleteContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), 
                    Integer.toString(contact.getObjectID()), contact.getLastModified());
		    }
        } catch (OXException e) {
        	if (handle(e)) {
        		this.create();
        	}
        }
	}

	@Override
	public boolean exists() throws WebdavProtocolException {
		return this.exists;
	}

	@Override
	public void delete() throws WebdavProtocolException {
		if (false == this.exists()) {
			throw protocolException(HttpServletResponse.SC_NOT_FOUND);
		} 		
    	try {	
    		/*
    		 * Delete contact
    		 */
        	this.factory.getContactService().deleteContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), 
        			Integer.toString(contact.getObjectID()), contact.getLastModified());
            LOG.debug(this.getUrl() + ": deleted.");
            this.contact = null;
        } catch (OXException e) {
        	if (handle(e)) {
        		delete();
        	}
        }   
	}

	@Override
	public void save() throws WebdavProtocolException {
		if (false == this.exists()) {
			throw protocolException(HttpServletResponse.SC_NOT_FOUND);
		}		
        try {
        	/*
        	 * Update contact 
        	 */
        	this.factory.getContactService().updateContact(factory.getSession(), Integer.toString(contact.getParentFolderID()),
        			Integer.toString(contact.getObjectID()), contact, contact.getLastModified());
            LOG.debug(this.getUrl() + ": saved.");
        } catch (OXException e) {
        	if (handle(e)) {
        		save();
        	}
        }
	}

	@Override
	public Date getCreationDate() throws WebdavProtocolException {
		return null != this.contact ? contact.getCreationDate() : new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
		return null != this.contact ? contact.getLastModified() : new Date(0);
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		return null != this.contact ? this.contact.getDisplayName() : null;
	}

	@Override
	public void setDisplayName(String displayName) throws WebdavProtocolException {
		if (null != this.contact) {
			this.contact.setDisplayName(displayName);
		}
	}

	@Override
	protected void applyVersitObject(VersitObject versitObject) throws WebdavProtocolException {
		try {
			/*
			 * Deserialize contact
			 */
			Contact newContact = isGroup(versitObject) ? deserializeAsTemporaryGroup(versitObject) : deserializeAsContact(versitObject);
		    if (this.exists()) {
		    	/*
		    	 * Update previously set metadata
		    	 */
		        newContact.setParentFolderID(this.contact.getParentFolderID());
		        newContact.setContextId(this.contact.getContextId());
		        newContact.setLastModified(this.contact.getLastModified());
		        newContact.setObjectID(this.contact.getObjectID());
		        /*
		         * Check for property changes
		         */
				for (final Mapping<? extends Object, Contact> mapping : CardDAVMapper.getInstance().getMappings().values()) {
					if (mapping.isSet(this.contact) && false == mapping.isSet(newContact)) {
						// set this one explicitly so that the property gets removed during update
						mapping.copy(newContact, newContact);
					} else if (mapping.isSet(newContact) && mapping.equals(contact, newContact)) {
						// this is no change, so ignore in update
						mapping.remove(newContact);
					}
				}
		        /*
		         * Never update the UID
		         */
		        newContact.removeUid();
		    } else {
		    	/*
		    	 * Apply default metadata
		    	 */
		        newContact.setContextId(this.factory.getSession().getContextId());
		        newContact.setParentFolderID(Tools.parse(this.parentFolderID));
	    		if (null != this.url) {
	    			String extractedUID = Tools.extractUID(url);
	    			if (null != extractedUID && false == extractedUID.equals(newContact.getUid())) {
	                	/*
	                	 * Always extract the UID from the URL; the Addressbook client in MacOS 10.6 uses different UIDs in 
	                	 * the WebDAV path and the UID field in the vCard, so we need to store this UID in the contact
	                	 * resource, too, to recognize later updates on the resource.
	                	 */
	            		LOG.debug(getUrl() + ": Storing WebDAV resource name in filename.");
	            		newContact.setFilename(extractedUID);
	            	}
	    		}
		    }
		    /*
		     * Take over new contact
		     */
		    this.contact = newContact;
		} catch (ConverterException e) {
			throw protocolException(e);
		} catch (OXException e) {
			throw protocolException(e);
		}
	}

	@Override
	protected String generateVCard() throws WebdavProtocolException {
		return serializeAsContact();
	}

	@Override
	protected String getUID() {
		return null != this.contact ? this.contact.getUid() : Tools.extractUID(getUrl());
	}

	private Contact deserializeAsContact(VersitObject versitObject) throws OXException, ConverterException {
		return CONVERTER.convertContact(versitObject);
	}
	
    private Contact deserializeAsTemporaryGroup(VersitObject versitObject) throws OXException {
        Contact contact = new Contact();
        contact.setMarkAsDistributionlist(true);
        String formattedName = versitObject.getProperty("FN").getValue().toString(); 
        contact.setDisplayName(formattedName);
        contact.setSurName(formattedName);
        String uid = versitObject.getProperty("UID").getValue().toString();
        if (null != uid && 0 < uid.length()) {
            contact.setUid(uid);
        }
        return contact;
    }
    
	private String serializeAsContact() throws WebdavProtocolException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VersitDefinition contactDef = Versit.getDefinition("text/vcard");
        VersitDefinition.Writer versitWriter = null;
        try {
            versitWriter = contactDef.getWriter(outputStream, "UTF-8");
            VersitObject versitObject = CONVERTER.convertContact(contact, "3.0");
            contactDef.write(versitWriter, versitObject);
            versitWriter.flush();
            return new String(outputStream.toByteArray(), "UTF-8");
        } catch (IOException e) {
        	throw super.protocolException(e);
        } catch (ConverterException e) {
        	throw super.protocolException(e);
        } finally {
        	if (null != versitWriter) { try { versitWriter.close(); } catch (IOException e) { } }
        }
	}

	/**
	 * Tries to handle an exception.
	 * 
	 * @param e the exception to handle
	 * @return <code>true</code>, if the operation should be retried, 
	 * <code>false</code>, otherwise.
	 * @throws WebdavProtocolException
	 */
	private boolean handle(OXException e) throws WebdavProtocolException {
		boolean retry = false;
    	if (Tools.isImageProblem(e)) {
    		/*
    		 * image problem, handle by create without image
    		 */
        	LOG.warn(this.getUrl() + ": " + e.getMessage() + " - removing image and trying again.");
        	this.contact.removeImage1();
        	retry = true;
    	} else if (Tools.isDataTruncation(e)) {
    		/*
    		 * handle by trimming truncated fields
    		 */
        	if (this.trimTruncatedAttributes(e)) {
        		LOG.warn(this.getUrl() + ": " + e.getMessage() + " - trimming fields and trying again.");
        		retry = true;
        	}
    	} else if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
    		/*
    		 * handle by overriding sync-token
    		 */
    		LOG.debug(this.getUrl() + ": " + e.getMessage());
        	LOG.debug(this.getUrl() + ": overriding next sync token for client recovery.");
			this.factory.overrideNextSyncToken();
    	} else if (Category.CATEGORY_CONFLICT.equals(e.getCategory())) {
    		throw super.protocolException(e, HttpServletResponse.SC_CONFLICT);
    	} else {
    		throw super.protocolException(e);
    	}
    	
    	if (retry) {
    		retryCount++;
    		return retryCount <= MAX_RETRIES; 
    	} else {
    		return false;
    	}
	}

	private boolean trimTruncatedAttributes(OXException e) {
		try {
			return MappedTruncation.truncate(e.getProblematics(), this.contact);
		} catch (OXException x) {
			LOG.warn(getUrl() + ": error trying to handle truncated attributes", x);
			return false;
		}
	}
	
    private static boolean isGroup(VersitObject versitObject) {
        com.openexchange.tools.versit.Property property = versitObject.getProperty("X-ADDRESSBOOKSERVER-KIND");
        return null != property  && "group".equals(property.getValue());
    }

}
