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

package com.openexchange.mail.json.compose.share;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.compose.AbstractQuotaAwareComposeContext;
import com.openexchange.mail.json.compose.ComposeRequest;

/**
 * {@link ShareTransportComposeContext}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareTransportComposeContext extends AbstractQuotaAwareComposeContext {

    /**
     * Initializes a new {@link ShareTransportComposeContext}.
     *
     * @param request The compose request
     * @throws OXException If initialization fails
     */
    public ShareTransportComposeContext(ComposeRequest request) throws OXException {
        super(request);
    }

    @Override
    protected void onFileUploadQuotaExceeded(long uploadQuotaPerFile, long size, MailPart part) throws OXException {
        String fileName = part.getFileName();
        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(UploadUtility.getSize(uploadQuotaPerFile), null == fileName ? "" : fileName, UploadUtility.getSize(size));
    }

    @Override
    protected void onTotalUploadQuotaExceeded(long uploadQuota, long consumed) throws OXException {
        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(UploadUtility.getSize(uploadQuota));
    }

}
