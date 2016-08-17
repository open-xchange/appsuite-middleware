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

package com.openexchange.mail.json.compose;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart;
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
        if (doAction) {
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
