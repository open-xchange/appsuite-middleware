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

package com.openexchange.auth.rmi.impl;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.auth.mbean.impl.AuthenticatorMBeanImpl;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link RemoteAuthenticatorImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemoteAuthenticatorImpl implements RemoteAuthenticator {

    /**
     * Initializes a new {@link RemoteAuthenticatorImpl}.
     */
    public RemoteAuthenticatorImpl() {
        super();
    }

    @Override
    public boolean isMasterAuthenticationDisabled() throws RemoteException {
        final Logger logger = LoggerFactory.getLogger(AuthenticatorMBeanImpl.class);
        try {
            final Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            return authenticator.isMasterAuthenticationDisabled();
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public boolean isContextAuthenticationDisabled() throws RemoteException {
        final Logger logger = LoggerFactory.getLogger(AuthenticatorMBeanImpl.class);
        try {
            final Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            return authenticator.isContextAuthenticationDisabled();
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public void doAuthentication(String login, String password) throws RemoteException {
        final Logger logger = LoggerFactory.getLogger(AuthenticatorMBeanImpl.class);
        try {
            final Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            authenticator.doAuthentication(new Credentials(login, password));
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public void doAuthentication(String login, String password, int contextId) throws RemoteException {
        final Logger logger = LoggerFactory.getLogger(AuthenticatorMBeanImpl.class);
        try {
            final Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            authenticator.doAuthentication(new Credentials(login, password), contextId);
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public void doUserAuthentication(String login, String password, int contextId) throws RemoteException {
        final Logger logger = LoggerFactory.getLogger(AuthenticatorMBeanImpl.class);
        try {
            final Authenticator authenticator = ServerServiceRegistry.getInstance().getService(Authenticator.class);
            if (null == authenticator) {
                throw ServiceExceptionCode.absentService(Authenticator.class);
            }
            authenticator.doUserAuthentication(new Credentials(login, password), contextId);
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

}
