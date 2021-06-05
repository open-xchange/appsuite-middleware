
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

package com.openexchange.authentication.application.impl;

import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AbstractAJAXActionAnnotationProcessor;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.authentication.application.AppPasswordUtils;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppPasswordAnnotationProcessor}
 * Checks session for restricted authentication. If present, verifies scope
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordAnnotationProcessor extends AbstractAJAXActionAnnotationProcessor<RestrictedAction> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AppPasswordAnnotationProcessor.class);

    /**
     * Initializes a new {@link AppPasswordAnnotationProcessor}.
     */
    public AppPasswordAnnotationProcessor() {
        super();
    }

    @Override
    protected Class<RestrictedAction> getAnnotation() {
        return RestrictedAction.class;
    }

    @Override
    protected void doProcess(RestrictedAction annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        Object restrParam = session.getParameter(Session.PARAM_RESTRICTED);
        if (restrParam == null) {
            return; // Not a restricted session, just return
        }
        RestrictedAction restrAction = action.getClass().getAnnotation(RestrictedAction.class);
        String requiredScope = restrAction.type().getScope(restrAction.module());

        // Check if this action requires full auth.  If so, reject now
        if (RestrictedAction.REQUIRES_FULL_AUTH.equals(restrAction.module())) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }

        // Return if grant all
        if (RestrictedAction.GRANT_ALL.equals(restrAction.module())) {
            return;
        }

        // Check the scopes of authentication for this session
        if (false == (restrParam instanceof String)) {
            throw AppPasswordExceptionCodes.APPLICATION_PASSWORD_GENERIC_ERROR.create("Unkown restricted session type");
        }
        Set<String> restrictedScopes = AppPasswordUtils.getRestrictedScopes(session);
        LOG.debug("Restricted session hit for module " + requestData.getModule() + " action:" + requestData.getAction() + " required:" + requiredScope);
        if (RestrictedAction.GRANT_ALL.equals(requiredScope)) {
            return;
        }
        if (!restrictedScopes.contains(requiredScope)) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }
    }
}
