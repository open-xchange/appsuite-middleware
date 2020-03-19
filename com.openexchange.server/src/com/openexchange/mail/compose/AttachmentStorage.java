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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.Service;
import com.openexchange.session.Session;

/**
 * {@link AttachmentStorage} - A storage for attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
@Service
public interface AttachmentStorage {

    /** The registration name for registered image data source backed by attachments */
    public static final String IMAGE_REGISTRATION_NAME = "com.openexchange.mail.compose.image";

    /** The alias for the image data source backed by an attachment */
    public static final String IMAGE_DATA_SOURCE_ALIAS = "/mail/compose/image";

    /**
     * Gets the type for this attachment storage.
     *
     * @return The type
     */
    AttachmentStorageType getStorageType();

    /**
     * Checks if this attachment storage is applicable for given session and capabilities.
     *
     * @param capabilities The capabilities granted to session-associated user
     * @param session The session providing user data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException
     */
    boolean isApplicableFor(CapabilitySet capabilities, Session session) throws OXException;

    /**
     * Gets the attachment associated with given identifier
     *
     * @param id The attachment identifier
     * @param optionalEncrypt The optional encryption flag on initial opening of a composition space. If present and <code>true</code> the
     *                        attachment to save is supposed to be encrypted according to caller. If present and <code>false</code>  the
     *                        attachment to save is <b>not</b> supposed to be encrypted according to caller. If absent, encryption is
     *                        automatically determined.<br>
     *                        <b>Note</b>: The flag MUST be aligned to associated composition space
     * @param session The session providing user information
     * @return The attachment or <code>null</code> if no such attachment exists
     * @throws OXException If attachment cannot be returned
     */
    Attachment getAttachment(UUID id, Optional<Boolean> optionalEncrypt, Session session) throws OXException;

    /**
     * Gets the attachments associated with given identifiers
     *
     * @param ids The attachment identifiers
     * @param optionalEncrypt The optional encryption flag on initial opening of a composition space. If present and <code>true</code> the
     *                        attachment to save is supposed to be encrypted according to caller. If present and <code>false</code>  the
     *                        attachment to save is <b>not</b> supposed to be encrypted according to caller. If absent, encryption is
     *                        automatically determined.<br>
     *                        <b>Note</b>: The flag MUST be aligned to associated composition space
     * @param session The session providing user information
     * @return The attachments as an array. If a certain attachment does not exist, the appropriate index position in returned array is <code>null</code>
     * @throws OXException If attachment cannot be returned
     */
    default Attachment[] getAttachments(List<UUID> ids, Optional<Boolean> optionalEncrypt, Session session) throws OXException {
        if (ids == null || ids.isEmpty()) {
            return new Attachment[0];
        }

        Attachment[] retval = new Attachment[ids.size()];
        int index = 0;
        for (UUID id : ids) {
            retval[index++] = getAttachment(id, optionalEncrypt, session);
        }
        return retval;
    }

    /**
     * Gets the attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @return The attachments or an empty list if there are no attachments associated with given composition space
     * @throws OXException If attachments cannot be returned
     */
    List<Attachment> getAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException;

    /**
     * Gets the total size of attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @return The attachments' size
     * @throws OXException If attachments' size cannot be returned
     */
    SizeReturner getSizeOfAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException;

    /**
     * Saves the specified attachment binary data and meta data.
     *
     * @param input The input stream providing binary data
     * @param attachment The attachment providing meta data
     * @param sizeProvider The optional size provider
     * @param optionalEncrypt The optional encryption flag on initial opening of a composition space. If present and <code>true</code> the
     *                        attachment to save is supposed to be encrypted according to caller. If present and <code>false</code>  the
     *                        attachment to save is <b>not</b> supposed to be encrypted according to caller. If absent, encryption is
     *                        automatically determined.<br>
     *                        <b>Note</b>: The flag MUST be aligned to associated composition space
     * @param session The session providing user information
     * @return The resulting attachment
     * @throws OXException If saving attachment fails
     */
    Attachment saveAttachment(InputStream input, AttachmentDescription attachment, SizeProvider sizeProvider, Optional<Boolean> optionalEncrypt, Session session) throws OXException;

    /**
     * Deletes the attachment associated with given identifier
     *
     * @param id The attachment identifier
     * @param session The session providing user information
     * @throws OXException If attachment cannot be deleted
     */
    default void deleteAttachment(UUID id, Session session) throws OXException {
        if (null != id) {
            deleteAttachments(Collections.singletonList(id), session);
        }
    }

    /**
     * Deletes the attachments associated with given identifiers
     *
     * @param ids The attachment identifiers
     * @param session The session providing user information
     * @throws OXException If attachments cannot be deleted
     */
    void deleteAttachments(List<UUID> ids, Session session) throws OXException;

    /**
     * Deletes the attachments associated with given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param session The session providing user information
     * @throws OXException If attachments cannot be deleted
     */
    void deleteAttachmentsByCompositionSpace(UUID compositionSpaceId, Session session) throws OXException;

    /**
     * Deletes all user-associated attachments, which are not referenced by an existent composition space.
     *
     * @param session The session providing user information
     * @throws OXException If attachments cannot be deleted
     */
    void deleteUnreferencedAttachments(Session session) throws OXException;

}
