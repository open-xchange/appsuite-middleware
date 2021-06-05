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
import com.openexchange.file.storage.Quota;
import com.openexchange.quota.json.QuotaAJAXRequest;
import com.openexchange.server.ServiceLookup;


/**
 * {@link FilestoreAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FilestoreAction extends AbstractQuotaAction {

    /**
     * Initializes a new {@link FilestoreAction}.
     *
     * @param services The OSGi service look-up
     */
    public FilestoreAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final QuotaAJAXRequest req) throws OXException, JSONException {
        Quota storageQuota = req.getStorageQuota();
        JSONObject data = new JSONObject(4).put("quota", storageQuota.getLimit()).put("use", storageQuota.getUsage());
        return new AJAXRequestResult(data, "json");
    }

}
