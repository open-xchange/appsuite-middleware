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

package com.openexchange.mail.json.compose;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractQuotaAwareComposeContext} - A compose context; storing necessary state information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class AbstractQuotaAwareComposeContext extends AbstractComposeContext {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractQuotaAwareComposeContext.class);

    /** Whether any quota limitation is enabled */
    protected final boolean doAction;

    /** The total quota limitation */
    protected final long uploadQuota;

    /** The quota limitation per file */
    protected final long uploadQuotaPerFile;

    /** Keeps track of already <i>consumed</i> bytes */
    protected long consumed;

    /**
     * Initializes a new {@link AbstractQuotaAwareComposeContext}.
     *
     * @param request The compose request associated with this context
     * @throws OXException If initialization fails
     */
    protected AbstractQuotaAwareComposeContext(ComposeRequest request) throws OXException {
        super(request);
        MailUploadQuotaChecker checker = new MailUploadQuotaChecker(request.getSession().getUserSettingMail());
        uploadQuota = checker.getQuotaMax();
        uploadQuotaPerFile = checker.getFileQuotaMax();
        doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
    }

    /**
     * Initializes a new {@link AbstractQuotaAwareComposeContext}.
     *
     * @param accountId The account identifier
     * @param session The associated session
     * @throws OXException If initialization fails
     */
    protected AbstractQuotaAwareComposeContext(int accountId, ServerSession session) throws OXException {
        super(accountId, session);
        MailUploadQuotaChecker checker = new MailUploadQuotaChecker(session.getUserSettingMail());
        uploadQuota = checker.getQuotaMax();
        uploadQuotaPerFile = checker.getFileQuotaMax();
        doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
    }

    @Override
    protected void onPartAdd(MailPart part, ComposedMailPart info) throws OXException {
        if (doAction && ComposedPartType.FILE == info.getType()) {
            long size = part.getSize();
            if (size <= 0) {
                LOG.debug("Missing size: {}", Long.valueOf(size), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                onFileUploadQuotaExceeded(uploadQuotaPerFile, size, part);
                return;
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                onTotalUploadQuotaExceeded(uploadQuota, consumed);
                return;
            }
        }
    }

    /**
     * Invoked in case a file upload quota is exceeded.
     *
     * @param uploadQuotaPerFile The configured file upload quota
     * @param size The part's size
     * @param part The part
     * @throws OXException If handling throws an error
     */
    protected abstract void onFileUploadQuotaExceeded(long uploadQuotaPerFile, long size, MailPart part) throws OXException;

    /**
     * Invoked in case a total upload quota is exceeded.
     *
     * @param uploadQuota The total upload quota
     * @param consumed The number of consumed bytes
     * @throws OXException If handling throws an error
     */
    protected abstract void onTotalUploadQuotaExceeded(long uploadQuota, long consumed) throws OXException;

}
