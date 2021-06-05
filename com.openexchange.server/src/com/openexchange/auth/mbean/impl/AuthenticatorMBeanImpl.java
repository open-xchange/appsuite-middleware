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

package com.openexchange.auth.mbean.impl;

import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link AuthenticatorMBeanImpl} - The authenticator MBean implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class AuthenticatorMBeanImpl extends StandardMBean implements AuthenticatorMBean {

    /**
     * Initializes a new {@link AuthenticatorMBeanImpl}.
     *
     * @throws NotCompliantMBeanException If initialization fails
     */
    public AuthenticatorMBeanImpl() throws NotCompliantMBeanException {
        super(AuthenticatorMBean.class);
    }

    @Override
    public boolean isMasterAuthenticationDisabled() throws MBeanException {
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
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public boolean isContextAuthenticationDisabled() throws MBeanException {
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
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void doAuthentication(String login, String password) throws MBeanException {
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
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void doAuthentication(String login, String password, int contextId) throws MBeanException {
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
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void doUserAuthentication(String login, String password, int contextId) throws MBeanException {
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
            throw new MBeanException(new Exception(message), message);
        }
    }

}
