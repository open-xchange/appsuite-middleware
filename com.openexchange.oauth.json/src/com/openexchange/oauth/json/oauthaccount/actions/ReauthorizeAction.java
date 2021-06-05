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

package com.openexchange.oauth.json.oauthaccount.actions;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReauthorizeAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReauthorizeAction extends AbstractOAuthTokenAction {

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final String accountId = request.getParameter("id");
        if (null == accountId) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
        }
        final int id = getUnsignedInteger(accountId);
        if (id < 0) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("id", Integer.valueOf(id));
        }

        final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
        if (serviceId == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.SERVICE_ID.getName());
        }

        ClusterLockService clusterLockService = Services.getService(ClusterLockService.class);
        clusterLockService.runClusterTask(new ReauthorizeClusterTask(request, session, accountId, serviceId), new ExponentialBackOffRetryPolicy());

        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(new SecureContentWrapper(Boolean.TRUE, "boolean"), SecureContentWrapper.CONTENT_TYPE);

    }
}
