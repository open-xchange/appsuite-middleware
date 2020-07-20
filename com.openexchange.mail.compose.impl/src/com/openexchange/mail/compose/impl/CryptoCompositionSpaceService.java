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

package com.openexchange.mail.compose.impl;

import static com.openexchange.java.util.UUIDs.getUnformattedString;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.AttachmentResult;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
import com.openexchange.mail.compose.UploadLimits;
import com.openexchange.mail.compose.security.CompositionSpaceKeyStorageService;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CryptoCompositionSpaceService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CryptoCompositionSpaceService extends AbstractCryptoAware implements CompositionSpaceService {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CryptoCompositionSpaceService.class);
    }

    private final CompositionSpaceService delegate;
    private final Session session;

    /**
     * Initializes a new {@link CryptoCompositionSpaceService}.
     *
     * @param session The session for which this instance is created
     * @param keyStorageService The key storage
     * @param services The service look-up
     */
    public CryptoCompositionSpaceService(Session session, CompositionSpaceService delegate, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.session = session;
        this.delegate = delegate;
    }

    private void autoDeleteSafe(UUID optCompositionSpaceUUID, Session session, OXException missingKeyError) {
        UUID compositionSpaceUUID = optCompositionSpaceUUID;
        if (null == compositionSpaceUUID) {
            try {
                Object object = missingKeyError.getLogArgs()[0];
                if (object instanceof CompositionSpaceId) {
                    compositionSpaceUUID = ((CompositionSpaceId) object).getId();
                } else {
                    compositionSpaceUUID = CompositionSpaceId.valueOf(object.toString()).getId();
                }
            } catch (ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException | IllegalArgumentException e) {
                LoggerHolder.LOG.warn("Failed to get compositon space identifier from missing key exception: {}", e.getMessage(), missingKeyError);
                return;
            }
        }

        try {
            if (autoDeleteIfKeyIsMissing(session)) {
                delegate.closeCompositionSpace(compositionSpaceUUID);
                LoggerHolder.LOG.debug("Closed composition space '{}' due to missing key and enabled option \"com.openexchange.mail.compose.security.autoDeleteIfKeyIsMissing\"", getUnformattedString(compositionSpaceUUID));
            }
        } catch (Exception e) {
            LoggerHolder.LOG.debug("Failed to delete compositon space {} due to missing key", UUIDs.getUnformattedString(compositionSpaceUUID), e);
        }
    }

    @Override
    public Collection<OXException> getWarnings() {
        return delegate.getWarnings();
    }

    @Override
    public MailPath transportCompositionSpace(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, UserSettingMail mailSettings, AJAXRequestData requestData, List<OXException> warnings, boolean deleteAfterTransport) throws OXException {
        try {
            return delegate.transportCompositionSpace(compositionSpaceId, optionalUploadedAttachments, mailSettings, requestData, warnings, deleteAfterTransport);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, Optional<StreamedUploadFileIterator> optionalUploadedAttachments, boolean deleteAfterSave) throws OXException {
        try {
            return delegate.saveCompositionSpaceToDraftMail(compositionSpaceId, optionalUploadedAttachments, deleteAfterSave);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public CompositionSpace openCompositionSpace(OpenCompositionSpaceParameters parameters) throws OXException {
        try {
            return delegate.openCompositionSpace(parameters);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(null, session, e);
            }
            throw e;
        }
    }

    @Override
    public boolean closeCompositionSpace(UUID compositionSpaceId) throws OXException {
        try {
            return delegate.closeCompositionSpace(compositionSpaceId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public void closeExpiredCompositionSpaces(long maxIdleTimeMillis) throws OXException {
        delegate.closeExpiredCompositionSpaces(maxIdleTimeMillis);
    }

    @Override
    public CompositionSpace getCompositionSpace(UUID compositionSpaceId) throws OXException {
        try {
            return delegate.getCompositionSpace(compositionSpaceId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(MessageField[] fields) throws OXException {
        try {
            return delegate.getCompositionSpaces(fields);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(null, session, e);
            }
            throw e;
        }
    }

    @Override
    public CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription messageDescription) throws OXException {
        try {
            return delegate.updateCompositionSpace(compositionSpaceId, messageDescription);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition) throws OXException {
        try {
            return delegate.replaceAttachmentInCompositionSpace(compositionSpaceId, attachmentId, uploadedAttachments, disposition);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition) throws OXException {
        try {
            return delegate.addAttachmentToCompositionSpace(compositionSpaceId, uploadedAttachments, disposition);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachment, InputStream data) throws OXException {
        try {
            return delegate.addAttachmentToCompositionSpace(compositionSpaceId, attachment, data);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult addVCardToCompositionSpace(UUID compositionSpaceId) throws OXException {
        try {
            return delegate.addVCardToCompositionSpace(compositionSpaceId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId) throws OXException {
        try {
            return delegate.addContactVCardToCompositionSpace(compositionSpaceId, contactId, folderId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId) throws OXException {
        try {
            return delegate.addOriginalAttachmentsToCompositionSpace(compositionSpaceId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult deleteAttachment(UUID compositionSpaceId, UUID attachmentId) throws OXException {
        try {
            return delegate.deleteAttachment(compositionSpaceId, attachmentId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public AttachmentResult getAttachment(UUID compositionSpaceId, UUID attachmentId) throws OXException {
        try {
            return delegate.getAttachment(compositionSpaceId, attachmentId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public UploadLimits getAttachmentUploadLimits(UUID compositionSpaceId) throws OXException {
        try {
            return delegate.getAttachmentUploadLimits(compositionSpaceId);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

}
