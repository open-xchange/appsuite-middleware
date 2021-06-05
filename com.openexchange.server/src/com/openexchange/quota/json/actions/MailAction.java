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

package com.openexchange.quota.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.quota.json.QuotaAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link MailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAction extends AbstractQuotaAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MailAction.class);

    /**
     * Initializes a new {@link MailAction}.
     * @param services
     */
    public MailAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final QuotaAJAXRequest req) throws OXException, JSONException {
        // Get quota info
        long[][] quotaInfo = getQuotaInfo(req.getSession());

        JSONObject data = new JSONObject(6);
        // STORAGE
        data.put("quota", quotaInfo[0][0] << 10);
        data.put("use", quotaInfo[0][1] << 10);
        // MESSAGE
        data.put("countquota", quotaInfo[1][0]);
        data.put("countuse", quotaInfo[1][1]);
        /*
         * Write JSON object into writer as data content of a response object
         */
        return new AJAXRequestResult(data, "json");
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
