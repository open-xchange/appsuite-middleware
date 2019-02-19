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

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentDescription;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.OpenCompositionSpaceParameters;
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

    /**
     * Initializes a new {@link CryptoCompositionSpaceService}.
     *
     * @param keyStorageService The key storage
     * @param services The service look-up
     */
    public CryptoCompositionSpaceService(CompositionSpaceService delegate, CompositionSpaceKeyStorageService keyStorageService, ServiceLookup services) {
        super(keyStorageService, services);
        this.delegate = delegate;
    }

    private void autoDeleteSafe(UUID optCompositionSpaceId, Session session, OXException missingKeyError) {
        UUID compositionSpaceId = optCompositionSpaceId;
        if (null == compositionSpaceId) {
            String unformattedString = (String) missingKeyError.getLogArgs()[0];
            try {
                compositionSpaceId = UUIDs.fromUnformattedString(unformattedString);
            } catch (IllegalArgumentException e) {
                LoggerHolder.LOG.debug("Failed to parse compositon space identifier {}", unformattedString, e);
            }
        }

        try {
            if (autoDeleteIfKeyIsMissing(session)) {
                delegate.closeCompositionSpace(compositionSpaceId, session);
            }
        } catch (Exception e) {
            LoggerHolder.LOG.debug("Failed to delete compositon space {} due to missing key", UUIDs.getUnformattedString(compositionSpaceId), e);
        }
    }

    @Override
    public MailPath transportCompositionSpace(UUID compositionSpaceId, UserSettingMail mailSettings, AJAXRequestData requestData, List<OXException> warnings, boolean deleteAfterTransport, Session session) throws OXException {
        try {
            return delegate.transportCompositionSpace(compositionSpaceId, mailSettings, requestData, warnings, deleteAfterTransport, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public MailPath saveCompositionSpaceToDraftMail(UUID compositionSpaceId, boolean deleteAfterSave, Session session) throws OXException {
        try {
            return delegate.saveCompositionSpaceToDraftMail(compositionSpaceId, deleteAfterSave, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public CompositionSpace openCompositionSpace(OpenCompositionSpaceParameters parameters, Session session) throws OXException {
        try {
            return delegate.openCompositionSpace(parameters, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(null, session, e);
            }
            throw e;
        }
    }

    @Override
    public boolean closeCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        try {
            return delegate.closeCompositionSpace(compositionSpaceId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public void closeExpiredCompositionSpaces(long maxIdleTimeMillis, Session session) throws OXException {
        delegate.closeExpiredCompositionSpaces(maxIdleTimeMillis, session);
    }

    @Override
    public CompositionSpace getCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        try {
            return delegate.getCompositionSpace(compositionSpaceId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public List<CompositionSpace> getCompositionSpaces(MessageField[] fields, Session session) throws OXException {
        try {
            return delegate.getCompositionSpaces(fields, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(null, session, e);
            }
            throw e;
        }
    }

    @Override
    public CompositionSpace updateCompositionSpace(UUID compositionSpaceId, MessageDescription messageDescription, Session session) throws OXException {
        try {
            return delegate.updateCompositionSpace(compositionSpaceId, messageDescription, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public Attachment replaceAttachmentInCompositionSpace(UUID compositionSpaceId, UUID attachmentId, StreamedUploadFileIterator uploadedAttachments, String disposition, Session session) throws OXException {
        try {
            return delegate.replaceAttachmentInCompositionSpace(compositionSpaceId, attachmentId, uploadedAttachments, disposition, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public List<Attachment> addAttachmentToCompositionSpace(UUID compositionSpaceId, StreamedUploadFileIterator uploadedAttachments, String disposition, Session session) throws OXException {
        try {
            return delegate.addAttachmentToCompositionSpace(compositionSpaceId, uploadedAttachments, disposition, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public Attachment addAttachmentToCompositionSpace(UUID compositionSpaceId, AttachmentDescription attachment, InputStream data, Session session) throws OXException {
        try {
            return delegate.addAttachmentToCompositionSpace(compositionSpaceId, attachment, data, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public Attachment addVCardToCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        try {
            return delegate.addVCardToCompositionSpace(compositionSpaceId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public Attachment addContactVCardToCompositionSpace(UUID compositionSpaceId, String contactId, String folderId, Session session) throws OXException {
        try {
            return delegate.addContactVCardToCompositionSpace(compositionSpaceId, contactId, folderId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public List<Attachment> addOriginalAttachmentsToCompositionSpace(UUID compositionSpaceId, Session session) throws OXException {
        try {
            return delegate.addOriginalAttachmentsToCompositionSpace(compositionSpaceId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public void deleteAttachment(UUID compositionSpaceId, UUID attachmentId, Session session) throws OXException {
        try {
            delegate.deleteAttachment(compositionSpaceId, attachmentId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

    @Override
    public Attachment getAttachment(UUID compositionSpaceId, UUID attachmentId, Session session) throws OXException {
        try {
            return delegate.getAttachment(compositionSpaceId, attachmentId, session);
        } catch (OXException e) {
            if (CompositionSpaceErrorCode.MISSING_KEY.equals(e)) {
                autoDeleteSafe(compositionSpaceId, session, e);
            }
            throw e;
        }
    }

}
