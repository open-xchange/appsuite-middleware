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

package com.openexchange.contacts.json.actions;

import java.sql.Connection;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.databaseold.Database;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.links.Links;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.log.LogFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link CopyAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "copy", description = "", parameters = {},
requestBody = "",
responseDescription = "")
public class CopyAction extends ContactAction {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CopyAction.class));

    /**
     * Initializes a new {@link CopyAction}.
     * @param serviceLookup
     */
    public CopyAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final int id = req.getId();
        final int inFolder = req.getFolder();
        Date timestamp = new Date(0);
        final int folderId = req.getFolderFromJSON();
        final Context ctx = session.getContext();

        final ContactInterfaceDiscoveryService discoveryService = getContactInterfaceDiscoveryService();
        final ContactInterface srcContactInterface = discoveryService.newContactInterface(inFolder, session);

        final ContactInterface contactInterface = discoveryService.newContactInterface(folderId, session);

        final Contact contact = srcContactInterface.getObjectById(id, inFolder);
        final int origObjectId = contact.getObjectID();
        contact.removeObjectID();
        final int origFolderId = contact.getParentFolderID();
        contact.setParentFolderID(folderId);

        if (inFolder == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
            contact.removeInternalUserId();
        }

        contact.setNumberOfAttachments(0);
        contactInterface.insertContactObject(contact);

        final User user = session.getUser();
        final UserConfiguration uc = session.getUserConfiguration();
        /*
         * Check attachments
         */
        copyAttachments(folderId, session, ctx, contact, origObjectId, origFolderId, user, uc);
        /*
         * Check links
         */
        copyLinks(folderId, session, ctx, contact, origObjectId, origFolderId, user);

        timestamp = contact.getLastModified();

        final JSONObject response = new JSONObject();
        try {
            response.put("id", contact.getObjectID());
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }

        return new AJAXRequestResult(response, timestamp, "json");
    }
    
    @Override
    protected AJAXRequestResult perform2(final ContactRequest request) throws OXException {
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

    public static void copyLinks(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user) throws OXException {
        /*
         * Get all
         */
        Connection readCon = Database.get(ctx, false);
        final LinkObject[] links;
        try {
            links = Links.getAllLinksFromObject(origObjectId, Types.CONTACT, origFolderId, user.getId(), user.getGroups(), session, readCon);
        } finally {
            Database.back(ctx, false, readCon);
            readCon = null;
        }
        if (links == null || links.length == 0) {
            return;
        }
        /*
         * Copy
         */
        final Connection writeCon = Database.get(ctx, true);
        try {
            for (final LinkObject link : links) {
                final LinkObject copy;
                if (link.getFirstId() == origObjectId) {
                    copy = new LinkObject(
                        contactObj.getObjectID(),
                        Types.CONTACT,
                        folderId,
                        link.getSecondId(),
                        link.getSecondType(),
                        link.getSecondFolder(),
                        ctx.getContextId());
                } else if (link.getSecondId() == origObjectId) {
                    copy = new LinkObject(
                        link.getFirstId(),
                        link.getFirstType(),
                        link.getFirstFolder(),
                        contactObj.getObjectID(),
                        Types.CONTACT,
                        folderId,
                        ctx.getContextId());
                } else {
                    LOG.error("Invalid link retrieved from Links.getAllLinksFromObject()." + " Neither first nor second ID matches!");
                    continue;
                }
                Links.performLinkStorage(copy, user.getId(), user.getGroups(), session, writeCon);
            }
        } finally {
            Database.back(ctx, true, writeCon);
        }
    }

    public static void copyAttachments(final int folderId, final Session session, final Context ctx, final Contact contactObj, final int origObjectId, final int origFolderId, final User user, final UserConfiguration uc) throws OXException {
        /*
         * Copy attachments
         */
        final AttachmentBase attachmentBase = Attachments.getInstance();
        final SearchIterator<?> iterator = attachmentBase.getAttachments(origFolderId, origObjectId, Types.CONTACT, ctx, user, uc).results();
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
                    attachmentBase.attachToObject(copy, attachmentBase.getAttachedFile(
                        origFolderId,
                        origObjectId,
                        Types.CONTACT,
                        orig.getId(),
                        ctx,
                        user,
                        uc), session, ctx, user, uc);
                } while (iterator.hasNext());
                attachmentBase.commit();
            } catch (final SearchIteratorException e) {
                try {
                    attachmentBase.rollback();
                } catch (final OXException e1) {
                    LOG.error("Attachment transaction rollback failed", e);
                }
                throw new OXException(e);
            } catch (final OXException e) {
                try {
                    attachmentBase.rollback();
                } catch (final OXException e1) {
                    LOG.error("Attachment transaction rollback failed", e);
                }
                throw e;
            } finally {
                try {
                    iterator.close();
                } catch (final SearchIteratorException e) {
                    LOG.error("SearchIterator could not be closed", e);
                }
                try {
                    attachmentBase.finish();
                } catch (final OXException e) {
                    LOG.error("Attachment transaction finish failed", e);
                }
            }
        }
    }

}
