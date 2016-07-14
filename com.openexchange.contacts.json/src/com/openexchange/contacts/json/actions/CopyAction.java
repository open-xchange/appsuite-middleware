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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.contacts.json.actions;

import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;


/**
 * {@link CopyAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "copy", description = "", parameters = {},
requestBody = "",
responseDescription = "")
@OAuthAction(ContactActionFactory.OAUTH_WRITE_SCOPE)
public class CopyAction extends ContactAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CopyAction.class);

    /**
     * Initializes a new {@link CopyAction}.
     * @param serviceLookup
     */
    public CopyAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest request) throws OXException {
        /*
         * prepare original contact
         */
        final ContactService contactService = getContactService();
        final Contact contact = contactService.getContact(request.getSession(), request.getFolderID(), request.getObjectID());
        final int originalFolderID = contact.getParentFolderID();
        final int originalObjectID = contact.getObjectID();
        contact.removeObjectID();
        contact.removeParentFolderID();
        contact.removeInternalUserId();
        contact.removeUid();
        boolean hasAttachments = 0 < contact.getNumberOfAttachments();
        /*
         * create copy
         */
        String folderID = request.getFolderIDFromData();
        if (hasAttachments) {
            contact.removeNumberOfAttachments();
	        contactService.createContact(request.getSession(), folderID, contact);
	        copyAttachments(Integer.parseInt(folderID), request.getSession(), request.getSession().getContext(),
	        		contact, originalObjectID, originalFolderID, request.getSession().getUser(), request.getSession().getUserConfiguration());
        } else {
	        contactService.createContact(request.getSession(), folderID, contact);
        }
        /*
         * respond with new object ID
         */
        final JSONObject response = new JSONObject();
        try {
            response.put("id", contact.getObjectID());
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }

        return new AJAXRequestResult(response, contact.getLastModified(), "json");
    }

    public static void copyAttachments(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user, final UserConfiguration uc) throws OXException {
        /*
         * Copy attachments
         */
        final AttachmentBase attachmentBase = Attachments.getInstance();
        final SearchIterator<?> iterator = attachmentBase.getAttachments(session, origFolderId, origObjectId, Types.CONTACT, ctx, user, uc).results();
        try {
            if (iterator.hasNext()) {
                try {
                    attachmentBase.startTransaction();
                    do {
                        final AttachmentMetadataFactory factory = new AttachmentMetadataFactory();
                        final AttachmentMetadata orig = (AttachmentMetadata) iterator.next();
                        final AttachmentMetadata copy = factory.newAttachmentMetadata(orig);
                        copy.setFolderId(folderId);
                        copy.setAttachedId(contactObj.getObjectID());
                        copy.setId(AttachmentBase.NEW);
                        InputStream file = null;
                        try {
                            file = attachmentBase.getAttachedFile(session, origFolderId, origObjectId, Types.CONTACT, orig.getId(), ctx, user, uc);
                            attachmentBase.attachToObject(copy, file, session, ctx, user, uc);
                        } finally {
                            Streams.close(file);
                        }
                    } while (iterator.hasNext());
                    attachmentBase.commit();
                } catch (final SearchIteratorException e) {
                    try {
                        attachmentBase.rollback();
                    } catch (final OXException e1) {
                        LOG.error("Attachment transaction rollback failed", e);
                    }
                    throw e;
                } catch (final OXException e) {
                    try {
                        attachmentBase.rollback();
                    } catch (final OXException e1) {
                        LOG.error("Attachment transaction rollback failed", e);
                    }
                    throw e;
                } finally {
                    try {
                        attachmentBase.finish();
                    } catch (final OXException e) {
                        LOG.error("Attachment transaction finish failed", e);
                    }
                }
            }
        } finally {
            SearchIterators.close(iterator);
        }
    }

}
