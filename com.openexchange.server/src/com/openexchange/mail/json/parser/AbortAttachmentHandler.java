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

package com.openexchange.mail.json.parser;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.session.Session;
/**
 * {@link AbortAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that throws a {@link OXException} on exceeded quota
 * (either overall or per-file quota).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AbortAttachmentHandler extends AbstractAttachmentHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbortAttachmentHandler.class);

    private TextBodyMailPart textPart;

    private long consumed;

    /**
     * Initializes a new {@link AbortAttachmentHandler}.
     *
     * @param session The session providing needed user information
     * @throws OXException If initialization fails
     */
    public AbortAttachmentHandler(Session session) throws OXException {
        super(session);
    }

    @Override
    public void addAttachment(MailPart attachment) throws OXException {
        if (doAction && isFileMailPart(attachment)) {
            final long size = attachment.getSize();
            if (size <= 0) {
                LOG.debug("Missing size: {}", Long.valueOf(size), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                final String fileName = attachment.getFileName();
                throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(UploadUtility.getSize(uploadQuotaPerFile), null == fileName ? "" : fileName, UploadUtility.getSize(size));
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(UploadUtility.getSize(uploadQuota));
            }
        }
        attachments.add(attachment);
    }

    private boolean isFileMailPart(MailPart attachment) {
        return (attachment instanceof ComposedMailPart) && ((ComposedMailPart) attachment).getType() == ComposedPartType.FILE;
    }

    @Override
    public ComposedMailMessage[] generateComposedMails(ComposedMailMessage source, List<OXException> warnings) throws OXException {
        source.setBodyPart(textPart);
        for (MailPart attachment : attachments) {
            source.addEnclosedPart(attachment);
        }
        return new ComposedMailMessage[] { source };
    }

    @Override
    public void setTextPart(TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

}
