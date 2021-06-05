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
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.exception.OXException;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.secret.recovery.json.SecretRecoveryAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.SetableSession;
import com.openexchange.session.SetableSessionFactory;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MigrateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MigrateAction extends AbstractSecretRecoveryAction {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MigrateAction.class);
    }

    private final Set<SecretMigrator> secretMigrators;

    /**
     * Initializes a new {@link MigrateAction}.
     *
     * @param services
     * @param secretMigrators
     */
    public MigrateAction(final ServiceLookup services, Set<SecretMigrator> secretMigrators) {
        super(services);
        this.secretMigrators = secretMigrators;
    }

    @Override
    protected AJAXRequestResult perform(final SecretRecoveryAJAXRequest req) throws OXException, JSONException {
        final SecretService secretService = getService(SecretService.class);
        if (null == secretService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SecretService.class.getName());
        }

        final String password = req.getParameter("password");
        final ServerSession session = req.getSession();

        // Old secret
        String oldSecret;
        SecretUsesPasswordChecker usesPasswordChecker = services.getOptionalService(SecretUsesPasswordChecker.class);
        if (usesPasswordChecker != null && usesPasswordChecker.usesPassword()) {
            SetableSession setableSession = SetableSessionFactory.getFactory().setableSessionFor(session);
            setableSession.setPassword(password);
            oldSecret = secretService.getSecret(setableSession);
        } else {
            oldSecret = password;
        }

        // New secret
        final String secret = secretService.getSecret(session);
        if (oldSecret.equals(secret)) {
            // Nothing to do...
            return new AJAXRequestResult(Integer.valueOf(1), "int");
        }

        for (SecretMigrator migrator : secretMigrators) {
            try {
                migrator.migrate(oldSecret, secret, session);
            } catch (OXException e) {
                if (!CryptoErrorMessage.BadPassword.equals(e)) {
                    throw e;
                }
                LoggerHolder.LOG.warn("{} is unable to re-crypt.", migrator.getClass().getName(), e);
            }
        }

        return new AJAXRequestResult(Integer.valueOf(1), "int");
    }

}
