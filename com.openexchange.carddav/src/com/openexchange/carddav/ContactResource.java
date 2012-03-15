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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.carddav.mapping.ContactMapper;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ContactResource}
 * 
 * CardDAV resource for {@link Contact}s.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactResource extends CarddavResource {

	/**
	 * All contact fields that may be set in vCards
	 */
//    private static final int[] CARDDAV_FIELDS = {
//    	Contact.DISPLAY_NAME, // FN
//    	Contact.SUR_NAME, Contact.GIVEN_NAME, // N
//    	Contact.COMPANY, // ORG
//    	Contact.EMAIL1, // EMAIL;type=WORK
//    	Contact.EMAIL2, // EMAIL;type=HOME
//    	Contact.CELLULAR_TELEPHONE1, // TEL;type=VOICE;type=CELL
//    	Contact.CELLULAR_TELEPHONE2, // TEL;type=VOICE;type=IPHONE
//    	Contact.TELEPHONE_HOME1, // TEL;type=VOICE;type=HOME
//    	Contact.TELEPHONE_BUSINESS1, // TEL;type=VOICE;type=WORK
//    	Contact.TELEPHONE_BUSINESS2, // TEL;type=VOICE;type=MAIN //TODO:Contact.TELEPHONE_COMPANY?
//    	Contact.FAX_HOME, // TEL;type=HOME;type=FAX
//    	Contact.FAX_BUSINESS, // TEL;type=WORK;type=FAX
//    	Contact.FAX_OTHER, // TEL;type=OTHER;type=FAX
//    	Contact.TELEPHONE_PAGER, // TEL;type=PAGER
//    	Contact.NOTE, // NOTE
//    	Contact.URL, // URL
//    	Contact.STREET_HOME, Contact.POSTAL_CODE_HOME, Contact.CITY_HOME, Contact.COUNTRY_HOME, // ADR;TYPE=home
//    	Contact.STREET_BUSINESS, Contact.POSTAL_CODE_BUSINESS, Contact.CITY_BUSINESS, Contact.COUNTRY_BUSINESS, // ADR;TYPE=work
//    	Contact.PROFESSION, // TITLE
//    };

    private static final OXContainerConverter converter = new OXContainerConverter((TimeZone) null, (String) null);
    
    private Contact contact = null;    
    
	public ContactResource(final AggregatedCollection parent, final GroupwareCarddavFactory factory, final Contact contact) {
		super(parent, factory);
    	if (null == contact) {
    		throw new IllegalArgumentException("contact");
    	}
		this.contact = contact;
	}

    public ContactResource(final AggregatedCollection parent, final GroupwareCarddavFactory factory, final VersitObject versitObject, 
    		final WebdavPath url) throws WebdavProtocolException {
    	super(parent, factory);
    	this.applyVersitObject(versitObject);
        if (this.exists()) {
        	final String uid = Tools.extractUID(url);
        	if (null != uid && 0 < uid.length()) {
            	if (false == uid.equals(this.contact.getUid())) {
                	/*
                	 * Always extract the UID from the URL; the Addressbook client in MacOS 10.6 uses different UIDs in the WebDAV path and 
                	 * the UID field in the vCard, so we need to override the UID in the vCard to recognize later updates on the resource.
                	 */
            		LOG.debug("Overwriting previous UID ('" + this.contact.getUid() + "') with UID from WebDAV path ('" + uid + "').");
            		this.contact.setUid(Tools.extractUID(url));
            	}
        	}
        }
    }
    
	@Override
	public void create() throws WebdavProtocolException {
		if (false == this.exists()) {
			throw protocolException(HttpServletResponse.SC_NOT_FOUND);
		}		
        try {
        	String uid = this.contact.getUid();
        	/*
        	 * Check for required uid
        	 */
        	if (null == uid || 0 == uid.length()) {
        		uid = UUID.randomUUID().toString();
        		LOG.debug(this.getUrl() + ": no uid assigned, using auto-generated uid of '" + uid); 
        		this.contact.setUid(uid);
        	} else {
            	/*
            	 * Check for uid uniqueness 
            	 */
            	//TODO: this check should be preferably done inside the database
            	if (null != this.factory.getState().get(uid)) {
            		throw OXException.conflict();
            	}
        	}
        	/*
        	 * Insert contact
        	 */
        	this.factory.getContactService().createContact(factory.getSession(), Integer.toString(contact.getParentFolderID()), contact);
            LOG.debug(this.getUrl() + ": created.");
        } catch (final OXException e) {
        	if (Tools.isImageProblem(e)) {
        		/*
        		 * image problem, handle by create without image
        		 */
            	LOG.warn(this.getUrl() + ": " + e.getMessage() + " - creating contact without image.");
            	this.contact.removeImage1();
            	this.create();
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
        		throw super.internalError(e);
        	}
        }   
	}

	@Override
	public boolean exists() {
		return null != this.contact;
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
        			Integer.toString(contact.getObjectID()),  contact.getLastModified());
            LOG.debug(this.getUrl() + ": deleted.");
            this.contact = null;
        } catch (final OXException e) {
        	if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
        		/*
        		 * handle by overriding sync-token
        		 */
        		LOG.debug(this.getUrl() + ": " + e.getMessage());
            	LOG.debug(this.getUrl() + ": overriding next sync token for client recovery.");
    			this.factory.overrideNextSyncToken();
        	} else if (Category.CATEGORY_CONFLICT.equals(e.getCategory())) {
        		throw super.protocolException(e, HttpServletResponse.SC_CONFLICT);
        	} else {
        		throw super.internalError(e);
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
        } catch (final OXException e) {
        	if (Tools.isImageProblem(e)) {
        		/*
        		 * image problem, handle by create without image
        		 */
            	LOG.warn(this.getUrl() + ": " + e.getMessage() + " - saving contact without image.");
            	this.contact.removeImage1();
            	this.save();
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
        		throw super.internalError(e);
        	}
        }
    }
	
	@Override
	public Date getCreationDate() throws WebdavProtocolException {
    	return this.exists() ? this.contact.getCreationDate() : new Date(0);
	}

	@Override
	public Date getLastModified() throws WebdavProtocolException {
    	return this.exists() ? this.contact.getLastModified() : new Date(0);
	}

	@Override
	public String getDisplayName() throws WebdavProtocolException {
		return this.exists() ? this.contact.getDisplayName() : null;
	}

	@Override
	public void setDisplayName(final String displayName) throws WebdavProtocolException {
		if (false == this.exists()) {
			throw protocolException(HttpServletResponse.SC_FORBIDDEN);
		} else {
			this.contact.setDisplayName(displayName);
		}
	}

	@Override
	protected void applyVersitObject(final VersitObject versitObject) throws WebdavProtocolException {
		try {
			/*
			 * Deserialize contact
			 */
			final Contact newContact = converter.convertContact(versitObject);
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
				for (final Mapping<? extends Object, Contact> mapping : ContactMapper.getInstance().getMappings().values()) {
					if (mapping.isSet(this.contact)) {
						if (false == mapping.isSet(newContact)) {
							// set this one explicitly so that the property gets removed during update
							mapping.copy(newContact, newContact);
						}
					}
				}
//		        for (final int field : ContactResource.CARDDAV_FIELDS) {
//					if (this.contact.contains(field)) {
//						if (false == newContact.contains(field)) {
//							// set this one explicitly so that the property gets removed during update
//							newContact.set(field, newContact.get(field));
//						} else {
//							final Object oldValue = this.contact.get(field);
//							final Object newValue = newContact.get(field);
//							if (null == oldValue && null == newValue || null != oldValue && oldValue.equals(newValue)) {
//								// this is no change, so ignore in update
//								newContact.remove(field);								
//							}
//						}
//					}
//				}
		        /*
		         * Never update the UID
		         */
		        newContact.removeUid();
		    } else {
		    	/*
		    	 * Apply default metadata
		    	 */
		        newContact.setContextId(this.factory.getSession().getContextId());
		        newContact.setParentFolderID(this.parent.getStandardFolder());
		    }
		    /*
		     * Take over new contact
		     */
		    this.contact = newContact;
		} catch (final ConverterException e) {
			throw internalError(e);
		} catch (final OXException e) {
			throw internalError(e);
		}
	}

	@Override
	protected String generateVCard() throws WebdavProtocolException {		
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final VersitDefinition contactDef = Versit.getDefinition("text/vcard");
        VersitDefinition.Writer versitWriter = null;
        try {
            versitWriter = contactDef.getWriter(byteArrayOutputStream, "UTF-8");
            final VersitObject versitObject = converter.convertContact(contact, "3.0");
            contactDef.write(versitWriter, versitObject);
            versitWriter.flush();
            String outputString = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            outputString = Patches.OutgoingFile.removeXOPENXCHANGEAttributes(outputString);
            return outputString;
        } catch (final IOException e) {
        	throw super.internalError(e);
        } catch (final ConverterException e) {
        	throw super.internalError(e);
        } finally {
        	if (null != versitWriter) { try { versitWriter.close(); } catch (final IOException e) { } }
        }
	}

	@Override
	protected String getUID() {
    	return this.exists() ? this.contact.getUid() : null;
	}
}
