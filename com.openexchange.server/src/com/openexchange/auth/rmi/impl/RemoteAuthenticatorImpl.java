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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

}
