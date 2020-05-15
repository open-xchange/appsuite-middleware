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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.external.account.impl;

import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountRMIService;
import com.openexchange.external.account.ExternalAccountService;

/**
 * {@link ExternalAccountRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ExternalAccountRMIServiceImpl implements ExternalAccountRMIService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExternalAccountRMIServiceImpl.class);

    private final ExternalAccountService service;

    /**
     * Initializes a new {@link ExternalAccountRMIServiceImpl}.
     *
     * @param service The external account service
     */
    public ExternalAccountRMIServiceImpl(ExternalAccountService service) {
        super();
        this.service = service;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws RemoteException {
        try {
            return service.list(contextId);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws RemoteException {
        try {
            return service.list(contextId, userId);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws RemoteException {
        try {
            return service.list(contextId, providerId);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, ExternalAccountModule module) throws RemoteException {
        try {
            return service.list(contextId, module);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, ExternalAccountModule module) throws RemoteException {
        try {
            return service.list(contextId, userId, module);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws RemoteException {
        try {
            return service.list(contextId, userId, providerId);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId, ExternalAccountModule module) throws RemoteException {
        try {
            return service.list(contextId, providerId, module);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId, ExternalAccountModule module) throws RemoteException {
        try {
            return service.list(contextId, userId, providerId, module);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void delete(int id, int contextId, int userId, ExternalAccountModule module) throws RemoteException {
        try {
            service.delete(id, contextId, userId, module);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

}
