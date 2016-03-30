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

package com.openexchange.mail.json.parser;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
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
    public AbortAttachmentHandler(final Session session) throws OXException {
        super(session);
    }

    @Override
    public void addAttachment(final MailPart attachment) throws OXException {
        if (doAction) {
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

    @Override
    public ComposedMailMessage[] generateComposedMails(final ComposedMailMessage source, List<OXException> warnings) throws OXException {
        source.setBodyPart(textPart);
        for (final MailPart attachment : attachments) {
            source.addEnclosedPart(attachment);
        }
        return new ComposedMailMessage[] { source };
    }

    @Override
    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

}
