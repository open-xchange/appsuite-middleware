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

package com.openexchange.ajax.request;

import static com.openexchange.mail.utils.StorageUtility.UNLIMITED_QUOTA;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * FIXME replace QuotaFileStorage FileStorage
 */
public class QuotaRequest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuotaRequest.class);

    private final QuotaFileStorage qfs;
    private final OXException fsException;
    private final ServerSession session;

    /**
     * Initializes a new {@link QuotaRequest}.
     *
     * @param session The session
     */
    public QuotaRequest(final ServerSession session) {
        super();
        QuotaFileStorage qfs = null;
        OXException fsException = null;
        try {
            qfs = getFileStorage(session.getUserId(), session.getContextId());
        } catch (OXException e) {
            fsException = e;
        }
        this.qfs = qfs;
        this.fsException = fsException;
        this.session = session;
    }

    private QuotaFileStorage getFileStorage(int userId, int contextId) throws OXException {
        QuotaFileStorageService storageService = FileStorages.getQuotaFileStorageService();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }
        return storageService.getQuotaFileStorage(userId, contextId, Info.drive(userId));
    }

    /**
     * Performs specified action
     *
     * @param action The action to perform
     * @return The result
     * @throws OXException If action fails
     * @throws JSONException If a JSON error occurs
     */
    public JSONValue action(final String action) throws OXException, JSONException {
        if (AJAXServlet.ACTION_GET.equals(action)) {
            return filestore();
        } else if ("filestore".equals(action)) {
            return filestore();
        } else if ("mail".equals(action)) {
            return mail();
        }
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
    }

    private JSONObject filestore() throws OXException, JSONException {
        if (fsException != null) {
            throw fsException;
        }
        final long use = qfs.getUsage();
        final long quota = qfs.getQuota();
        final JSONObject data = new JSONObject();
        data.put("quota", quota);
        data.put("use", use);
        /*
         * Return JSON object
         */
        return data;
    }

    private JSONObject mail() throws JSONException {
        long[][] quotaInfo;
        try {
            quotaInfo = getQuotaInfo(session);
        } catch (OXException e) {
            if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e)) {
                LOG.debug("", e);
            } else {
                LOG.error("", e);
            }
            quotaInfo = new long[][] { { UNLIMITED_QUOTA, UNLIMITED_QUOTA }, { UNLIMITED_QUOTA, UNLIMITED_QUOTA } };
        }

        final JSONObject data = new JSONObject();
        // STORAGE
        data.put("quota", quotaInfo[0][0] << 10);
        data.put("use", quotaInfo[0][1] << 10);
        // MESSAGE
        data.put("countquota", quotaInfo[1][0]);
        data.put("countuse", quotaInfo[1][1]);
        /*
         * Write JSON object into writer as data content of a response object
         */
        return data;
    }

    private long[][] getQuotaInfo(Session session) throws OXException {
        MailServletInterface mi = null;
        try {
            mi = MailServletInterface.getInstance(session);
            return mi.getQuotas(new int[] { MailServletInterface.QUOTA_RESOURCE_STORAGE, MailServletInterface.QUOTA_RESOURCE_MESSAGE });
        } finally {
            if (mi != null) {
                try {
                    mi.close(false);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

}
