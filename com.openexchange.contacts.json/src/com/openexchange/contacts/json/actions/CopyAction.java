/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contacts.json.actions;

import java.io.InputStream;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.user.User;

/**
 * {@link CopyAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.WRITE)
public class CopyAction extends IDBasedContactAction {

    /**
     * Initializes a new {@link CopyAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public CopyAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        Contact contact = access.getContact(getContactID(request.getFolderID(), request.getObjectID()));
        int originalFolderID = contact.getParentFolderID();
        int originalObjectID = contact.getObjectID();
        contact.removeObjectID();
        contact.removeParentFolderID();
        contact.removeInternalUserId();
        contact.removeUid();
        boolean hasAttachments = 0 < contact.getNumberOfAttachments();

        String folderID = request.getFolderIDFromData();
        if (hasAttachments) {
            contact.removeNumberOfAttachments();
            access.createContact(folderID, contact);
            copyAttachments(Integer.parseInt(folderID), request.getSession(), request.getSession().getContext(), contact, originalObjectID, originalFolderID, request.getSession().getUser(), request.getSession().getUserConfiguration());
        } else {
            access.createContact(folderID, contact);
        }

        JSONObject response = new JSONObject();
        try {
            response.put("id", contact.getId());
            return new AJAXRequestResult(response, contact.getLastModified(), "json");
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    /**
     * Copies the attachments of the contact
     *
     * @param folderId The folder identifier
     * @param session The session
     * @param ctx The context
     * @param contactObj The contact object
     * @param origObjectId The original object identifier
     * @param origFolderId The original folder identifier
     * @param user The user
     * @param uc The user configuration
     */
    private static void copyAttachments(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user, final UserConfiguration uc) throws OXException {
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
                } catch (SearchIteratorException e) {
                    try {
                        attachmentBase.rollback();
                    } catch (OXException e1) {
                        LOG.error("Attachment transaction rollback failed", e1);
                    }
                    throw e;
                } catch (OXException e) {
                    try {
                        attachmentBase.rollback();
                    } catch (OXException e1) {
                        LOG.error("Attachment transaction rollback failed", e1);
                    }
                    throw e;
                } finally {
                    try {
                        attachmentBase.finish();
                    } catch (OXException e) {
                        LOG.error("Attachment transaction finish failed", e);
                    }
                }
            }
        } finally {
            SearchIterators.close(iterator);
        }
    }

}
