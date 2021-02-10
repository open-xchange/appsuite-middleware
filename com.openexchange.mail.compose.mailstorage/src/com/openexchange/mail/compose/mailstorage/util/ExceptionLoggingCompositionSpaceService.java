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

package com.openexchange.mail.compose.mailstorage.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link ExceptionLoggingCompositionSpaceService} - Logs any exception.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ExceptionLoggingCompositionSpaceService implements CompositionSpaceService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExceptionLoggingCompositionSpaceService.class);

    private final CompositionSpaceService service;

    /**
     * Initializes a new {@link ExceptionLoggingCompositionSpaceService}.
     *
     * @param service The service to delegate to
     */
    public ExceptionLoggingCompositionSpaceService(CompositionSpaceService service) {
        super();
        this.service = service;
    }

    @Override
    public String getServiceId() {
        return service.getServiceId();
    }

    @Override
    public Collection<OXException> getWarnings() {
        return service.getWarnings();
    }

    @Override
    public MailPath transportCompositionSpace(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, UserSettingMail mailSettings, AJAXRequestData requestData, List<OXException> warnings, boolean deleteAfterTransport, ClientToken clientToken) throws OXException {
        try {
            return service.transportCompositionSpace(compositionSpaceId, optionalUploadedAttachments, mailSettings, requestData, warnings, deleteAfterTransport, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling transportCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, boolean deleteAfterSave, ClientToken clientToken) throws OXException {
        try {
            return service.saveCompositionSpaceToDraftMail(compositionSpaceId, optionalUploadedAttachments, deleteAfterSave, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling saveCompositionSpaceToDraftMail() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public CompositionSpace openCompositionSpace(OpenCompositionSpaceParameters parameters) throws OXException {
        try {
            return service.openCompositionSpace(parameters);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling openCompositionSpace()", e);
            throw e;
        }
    }

    @Override
    public boolean closeCompositionSpace(UUID compositionSpaceId, boolean hardDelete, ClientToken clientToken) throws OXException {
        try {
            return service.closeCompositionSpace(compositionSpaceId, hardDelete, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling closeCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public void closeExpiredCompositionSpaces(long maxIdleTimeMillis) throws OXException {
        try {
            service.closeExpiredCompositionSpaces(maxIdleTimeMillis);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling closeExpiredCompositionSpaces()", e);
            throw e;
        }
    }

    @Override
    public CompositionSpace getCompositionSpace(UUID compositionSpaceId) throws OXException {
        try {
            return service.getCompositionSpace(compositionSpaceId);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling getCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(MessageField[] fields) throws OXException {
        try {
            return service.getCompositionSpaces(fields);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling getCompositionSpaces()", e);
            throw e;
        }
    }

    @Override
    public CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription messageDescription, ClientToken clientToken) throws OXException {
        try {
            return service.updateCompositionSpace(compositionSpaceId, messageDescription, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling updateCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException {
        try {
            return service.replaceAttachmentInCompositionSpace(compositionSpaceId, attachmentId, uploadedAttachments, disposition, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling replaceAttachmentInCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition, ClientToken clientToken) throws OXException {
        try {
            return service.addAttachmentToCompositionSpace(compositionSpaceId, uploadedAttachments, disposition, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling addAttachmentToCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachment, InputStream data, ClientToken clientToken) throws OXException {
        try {
            return service.addAttachmentToCompositionSpace(compositionSpaceId, attachment, data, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling addAttachmentToCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult addVCardToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException {
        try {
            return service.addVCardToCompositionSpace(compositionSpaceId, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling addVCardToCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId, ClientToken clientToken) throws OXException {
        try {
            return service.addContactVCardToCompositionSpace(compositionSpaceId, contactId, folderId, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling addContactVCardToCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId, ClientToken clientToken) throws OXException {
        try {
            return service.addOriginalAttachmentsToCompositionSpace(compositionSpaceId, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling addOriginalAttachmentsToCompositionSpace() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult deleteAttachment(UUID compositionSpaceId, UUID attachmentId, ClientToken clientToken) throws OXException {
        try {
            return service.deleteAttachment(compositionSpaceId, attachmentId, clientToken);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling deleteAttachment() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public AttachmentResult getAttachment(UUID compositionSpaceId, UUID attachmentId) throws OXException {
        try {
            return service.getAttachment(compositionSpaceId, attachmentId);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling getAttachment() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

    @Override
    public UploadLimits getAttachmentUploadLimits(UUID compositionSpaceId) throws OXException {
        try {
            return service.getAttachmentUploadLimits(compositionSpaceId);
        } catch (OXException e) {
            LOG.debug("Exception occurred while calling getAttachmentUploadLimits() with composition space identifier {}", UUIDs.getUnformattedString(compositionSpaceId), e);
            throw e;
        }
    }

}
