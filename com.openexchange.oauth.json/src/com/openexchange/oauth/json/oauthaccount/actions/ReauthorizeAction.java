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

package com.openexchange.oauth.json.oauthaccount.actions;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.ExponentialBackOffRetryPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.scope.OAuthScope;
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

    private class ReauthorizeClusterTask implements ClusterTask<Void> {

        private final String taskName;
        private final ServerSession session;
        private final String accountId;
        private final String serviceId;
        private final AJAXRequestData request;

        /**
         * Initialises a new {@link ReauthorizeAction.ReauthorizeClusterTask}.
         */
        public ReauthorizeClusterTask(AJAXRequestData request, ServerSession session, String accountId, String serviceId) {
            super();
            this.request = request;
            this.session = session;
            this.accountId = accountId;
            this.serviceId = serviceId;

            StringBuilder builder = new StringBuilder();
            builder.append(session.getUserId()).append("@");
            builder.append(session.getContextId());
            builder.append(":").append(accountId);
            builder.append(":").append(serviceId);

            taskName = builder.toString();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.openexchange.cluster.lock.ClusterTask#getTaskName()
         */
        @Override
        public String getTaskName() {
            return taskName;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.openexchange.cluster.lock.ClusterTask#perform()
         */
        @Override
        public Void perform() throws OXException {
            OAuthService oauthService = getOAuthService();
            OAuthAccount dbOAuthAccount = oauthService.getAccount(Integer.parseInt(accountId), session, session.getUserId(), session.getContextId());

            OAuthAccessRegistryService registryService = Services.getService(OAuthAccessRegistryService.class);
            OAuthAccessRegistry oAuthAccessRegistry = registryService.get(serviceId);
            OAuthAccess access = oAuthAccessRegistry.get(session.getContextId(), session.getUserId());

            if (access == null) {
                performReauthorize(oauthService);
            } else {
                // If the OAuth access is not initialised yet reload from DB, as it may have been changed from another node
                OAuthAccount cachedOAuthAccount = (access.getOAuthAccount() == null) ? oauthService.getAccount(Integer.parseInt(accountId), session, session.getUserId(), session.getContextId()) : access.getOAuthAccount();
                if (dbOAuthAccount.getToken().equals(cachedOAuthAccount.getToken()) && dbOAuthAccount.getSecret().equals(cachedOAuthAccount.getSecret())) {
                    performReauthorize(oauthService);
                } else {
                    access.initialize();
                }
            }

            return null;
        }

        private void performReauthorize(OAuthService oauthService) throws OXException {
            OAuthServiceMetaData service = oauthService.getMetaDataRegistry().getService(serviceId, session.getUserId(), session.getContextId());
            Map<String, Object> arguments = processOAuthArguments(request, session, service);
            // Get the scopes
            Set<OAuthScope> scopes = getScopes(request, serviceId);
            // By now it doesn't matter which interaction type is passed
            oauthService.updateAccount(Integer.parseInt(accountId), serviceId, OAuthInteractionType.CALLBACK, arguments, session.getUserId(), session.getContextId(), scopes);
        }

        /*
         * (non-Javadoc)
         *
         * @see com.openexchange.cluster.lock.ClusterTask#getContextId()
         */
        @Override
        public int getContextId() {
            return session.getContextId();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.openexchange.cluster.lock.ClusterTask#getUserId()
         */
        @Override
        public int getUserId() {
            return session.getUserId();
        }
    }
}
