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


	
package com.openexchange.groupware.contact;

import java.util.Date;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 Contacts
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 
 */
	
@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACTS_CONTACTSATTACHMENT,
		component=Component.CONTACT
	)
public class ContactsAttachment implements AttachmentListener, AttachmentAuthorization {

	private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(ContactsAttachment.class);
	
	private static final Log LOG = LogFactory.getLog(ContactsAttachment.class);

	public ContactsAttachment (){	}

	public long attached(final AttachmentEvent e) throws OXException {
		try{
			final ContactObject co = Contacts.getContactById(e.getAttachedId(),e.getUser().getId(),e.getUser().getGroups(),e.getContext(),e.getUserConfig(),e.getWriteConnection());
			co.setNumberOfAttachments(co.getNumberOfAttachments()+1);
			final Date d = new Date(System.currentTimeMillis());
			Contacts.performContactStorageUpdate(co,co.getParentFolderID(),d,e.getUser().getId(),e.getUser().getGroups(),e.getContext(),e.getUserConfig());
			return co.getLastModified().getTime();
		} catch (final OXException ex) {
			throw ex;
		}
	}

	@OXThrows(
			category=Category.USER_INPUT,
			desc="",
			exceptionId=0,
			msg= "Number of  documents attached to this contact is below zero. You can not remove any more attachments."
	)
	public long detached(final AttachmentEvent e) throws OXException {
		try{
			final ContactObject co = Contacts.getContactById(e.getAttachedId(),e.getUser().getId(),e.getUser().getGroups(),e.getContext(),e.getUserConfig(),e.getWriteConnection());
			if (co.getNumberOfAttachments() < 1){
				throw EXCEPTIONS.create(0);
				//throw new OXException("Numer of Attached Documents to this Contact is below zero. You can not remove more Attachments!");
			}
			final int[] xx = e.getDetached();
			co.setNumberOfAttachments(co.getNumberOfAttachments()-xx.length);
			final Date d = new Date(System.currentTimeMillis());
			Contacts.performContactStorageUpdate(co,co.getParentFolderID(),d,e.getUser().getId(),e.getUser().getGroups(),e.getContext(),e.getUserConfig());
			return co.getLastModified().getTime();
		} catch (final OXException ex) {
			throw ex;
		}
	}

	@OXThrows(
			category=Category.PERMISSION,
			desc="",
			exceptionId=1,
			msg= "Insufficient write rights for this folder. Unable to attach document. Context %4$d Folder %1$d Object %2$d User %3$d"
	)
	public void checkMayAttach(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
		try{
			final boolean back = Contacts.performContactWriteCheckByID(folderId, objectId,user.getId(),user.getGroups(),ctx,userConfig);
			if (!back){
				throw EXCEPTIONS.create(1, Integer.valueOf(folderId),Integer.valueOf(objectId),Integer.valueOf(user.getId()),Integer.valueOf(ctx.getContextId()));
	            //throw new OXException("Insufficient write rights for this folder! Unable to attach Document.");
			}
		} catch (final OXException e){
			LOG.error("Insufficient write rights for this folder! Unable to attach document.", e);
			throw e;
		}
	}

	public void checkMayDetach(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
		checkMayAttach(folderId, objectId,user,userConfig,ctx);
	}

	@OXThrows(
			category=Category.PERMISSION,
			desc="",
			exceptionId=2,
			msg= "Insufficient write rights for this folder. Unable to attach document. Context %4$d Folder %1$d Object %2$d User %3$d"
	)
	public void checkMayReadAttachments(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
		try{
			final boolean back = Contacts.performContactReadCheckByID(folderId, objectId,user.getId(),user.getGroups(),ctx,userConfig);
			if (!back){
				throw EXCEPTIONS.create(2, Integer.valueOf(folderId),Integer.valueOf(objectId),Integer.valueOf(user.getId()),Integer.valueOf(ctx.getContextId()));
	            //throw new OXException("Insufficient write rights for this folder! Unable to attach Document.");
			}
		} catch (final OXException e){
			LOG.error("Insufficient read rights for this folder! Unable to attach Document.", e);
			throw e;
		}		
	}

}
