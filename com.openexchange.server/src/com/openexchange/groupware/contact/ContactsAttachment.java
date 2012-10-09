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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link ContactsAttachment}
 * 
 * Attachment listener and -authorizer for the contacts module.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactsAttachment implements AttachmentListener, AttachmentAuthorization {

    /**
     * Initializes a new {@link ContactsAttachment}.
     */
    public ContactsAttachment () {
        super();
    }

    @Override
    public long attached(AttachmentEvent event) throws OXException {
        return modifyAttachmentCount(event.getSession(), String.valueOf(event.getFolderId()), String.valueOf(event.getAttachedId()), 1); 
    }

    @Override
    public long detached(AttachmentEvent event) throws OXException {
        return modifyAttachmentCount(event.getSession(), String.valueOf(event.getFolderId()), String.valueOf(event.getAttachedId()), 
            -1 * event.getDetached().length);
    }

    @Override
    public void checkMayAttach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        checkPermissions(folderId, objectId, user, userConfig, ctx, true, true);
    }

    @Override
    public void checkMayDetach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        checkMayAttach(folderId, objectId, user, userConfig, ctx);
    }

    @Override
    public void checkMayReadAttachments(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) 
        throws OXException {
        checkPermissions(folderId, objectId, user, userConfig, ctx, true, false);
    }

    /**
     * Checks a user's object permissions for a specific contact, throwing appropriate exceptions in case of the requested permissions 
     * are not met.
     * 
     * @param folderID The parent folder ID of the contact
     * @param objectID the object ID of the contact
     * @param user The user to check the permissions for
     * @param userConfig The user configuration
     * @param context The context
     * @param needsReadAccess <code>true</code> whether read access is requested, <code>false</code>, otherwise
     * @param needsWriteAccess <code>true</code> whether write access is requested, <code>false</code>, otherwise
     * @throws OXException
     */
    private static void checkPermissions(int folderID, int objectID, final User user, UserConfiguration userConfig, final Context context, 
        boolean needsReadAccess, boolean needsWriteAccess) throws OXException {
        if (needsReadAccess || needsWriteAccess) {
            OXFolderAccess folderAccess = new OXFolderAccess(context);
            EffectivePermission permission = folderAccess.getFolderPermission(folderID, user.getId(), userConfig);
            if (needsReadAccess && false == permission.canReadOwnObjects()) {
                throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderID), I(context.getContextId()), I(user.getId()));
            }
            if (needsWriteAccess && false == permission.canWriteOwnObjects()) {
                throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(I(objectID), I(context.getContextId()));
            }
            ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            //TODO: this Session may not be sufficient for all storages
            Session session = new Session() {
                @Override
                public int getContextId() {
                    return context.getContextId();
                }
                @Override
                public String getLocalIp() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void setLocalIp(String ip) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getLoginName() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public boolean containsParameter(String name) {
                    return false;
                }
                @Override
                public Object getParameter(String name) {
                    return null;
                }
                @Override
                public String getPassword() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getRandomToken() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getSecret() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getSessionID() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public int getUserId() {
                    return user.getId();
                }
                @Override
                public String getUserlogin() {
                    return user.getLoginInfo();
                }
                @Override
                public String getLogin() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void setParameter(String name, Object value) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void removeRandomToken() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getAuthId() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getHash() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void setHash(String hash) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String getClient() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public void setClient(String client) {
                    throw new UnsupportedOperationException();
                }
            };
            Contact contact = contactService.getContact(session, Integer.toString(folderID), Integer.toString(objectID), 
                new ContactField[] { ContactField.CREATED_BY, ContactField.PRIVATE_FLAG });
            if (contact.getCreatedBy() != session.getUserId()) {
                if (contact.getPrivateFlag() || needsReadAccess && false == permission.canReadAllObjects()) {
                    throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderID), I(context.getContextId()), I(user.getId()));
                }
                if (needsWriteAccess && false == permission.canWriteAllObjects()) {
                    throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(I(objectID), I(context.getContextId()));
                }
            }
        }        
    }
    
    /**
     * Increases or decreases the attachment count for a contact.
     * 
     * @param session The session
     * @param folderID The parent folder ID
     * @param objectID the object ID
     * @param delta The delta to increase/decrease the number of attachments property
     * @return The updated last modified timestamp
     * @throws OXException
     */
    private static long modifyAttachmentCount(Session session, String folderID, String objectID, int delta) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        Contact contact = contactService.getContact(session, folderID, objectID, 
            new ContactField[] { ContactField.NUMBER_OF_ATTACHMENTS, ContactField.LAST_MODIFIED });
        if (0 > contact.getNumberOfAttachments() + delta) {
            throw ContactExceptionCodes.TOO_FEW_ATTACHMENTS.create();
        }
        if (0 != delta) {
            contact.setNumberOfAttachments(contact.getNumberOfAttachments() + delta);
            contactService.updateContact(session, folderID, objectID, contact, contact.getLastModified());
        }
        return contact.getLastModified().getTime();
    }
    
}
