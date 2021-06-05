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

package com.openexchange.secret.recovery.json.action;

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.json.SecretRecoveryAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RemoveAction}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RemoveAction extends AbstractSecretRecoveryAction {

    private final Set<EncryptedItemCleanUpService> cleanUpServices;

    public RemoveAction(ServiceLookup services, Set<EncryptedItemCleanUpService> cleanUpServices) {
        super(services);
        this.cleanUpServices = cleanUpServices;
    }

    @Override
    protected AJAXRequestResult perform(SecretRecoveryAJAXRequest req) throws OXException, JSONException {
        final SecretService secretService = getService(SecretService.class);
        if (null == secretService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SecretService.class.getName());
        }
        // Get the secret string
        final String secret = secretService.getSecret(req.getSession());
        // Do the clean-up
        for (EncryptedItemCleanUpService cleanUp : cleanUpServices) {
            cleanUp.removeUnrecoverableItems(secret, req.getSession());
        }
        // Prepare response
        final JSONObject object = new JSONObject(1);
        object.put("clean_up", true);
        return new AJAXRequestResult(object, "json");
    }

}
