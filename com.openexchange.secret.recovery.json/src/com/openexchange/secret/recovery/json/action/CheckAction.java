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

import static com.openexchange.java.Autoboxing.I;
import org.json.ImmutableJSONObject;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.secret.recovery.SecretInconsistencyDetector;
import com.openexchange.secret.recovery.json.SecretRecoveryAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CheckAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckAction extends AbstractSecretRecoveryAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CheckAction.class);

    private final JSONObject jSecretWorks;

    /**
     * Initializes a new {@link CheckAction}.
     *
     * @param services
     */
    public CheckAction(final ServiceLookup services) {
        super(services);
        this.jSecretWorks = ImmutableJSONObject.immutableFor(new JSONObject(2).putSafe("secretWorks", Boolean.TRUE));
    }

    @Override
    protected AJAXRequestResult perform(final SecretRecoveryAJAXRequest req) throws OXException, JSONException {
        final SecretInconsistencyDetector secretInconsistencyDetector = getService(SecretInconsistencyDetector.class);
        if (null == secretInconsistencyDetector) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SecretInconsistencyDetector.class.getName());
        }

        // Check...
        ServerSession session = req.getSession();
        String diagnosis = secretInconsistencyDetector.isSecretWorking(session);

        // Compose JSON response
        if (diagnosis == null) {
            // Works...
            return new AJAXRequestResult(jSecretWorks, "json");
        }

        LOG.info("Secrets in session {} (user={}, context={}) seem to need migration: {}", session.getSessionID(), I(session.getUserId()), I(session.getContextId()), diagnosis);
        JSONObject jResult = new JSONObject(4);
        jResult.put("secretWorks", false);
        jResult.put("diagnosis", diagnosis);
        return new AJAXRequestResult(jResult, "json");
    }

}
