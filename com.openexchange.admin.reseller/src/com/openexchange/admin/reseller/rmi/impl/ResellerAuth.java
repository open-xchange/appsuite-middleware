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

package com.openexchange.admin.reseller.rmi.impl;

import org.slf4j.Logger;
import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.storage.interfaces.OXResellerStorageInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.OXCommonImpl;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.java.Strings;

/**
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 *
 */
public class ResellerAuth extends OXCommonImpl implements BasicAuthenticatorPluginInterface {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerAuth.class);
    }

    /**
     * Initializes a new {@link ResellerAuth}.
     */
    public ResellerAuth() {
        super();
    }

    @Override
    public void doAuthentication(Credentials creds) throws InvalidCredentialsException {
        try {
            doNullCheck(creds);
        } catch (InvalidDataException e) {
            LoggerHolder.LOG.error("authdata is null", e);
            throw new InvalidCredentialsException("authentication failed");
        }
        if (Strings.isEmpty(creds.getLogin())) {
            LoggerHolder.LOG.error("authdata has empty login");
            throw new InvalidCredentialsException("authentication failed");
        }
        try {
            OXResellerStorageInterface oxresell = OXResellerStorageInterface.getInstance();
            ResellerAdmin adm = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin()) })[0];
            if (!GenericChecks.authByMech(adm.getPassword(), creds.getPassword(), adm.getPasswordMech(), adm.getSalt())) {
                throw new InvalidCredentialsException("authentication failed");
            }
        } catch (StorageException e) {
            LoggerHolder.LOG.error("", e);
            throw new InvalidCredentialsException("authentication failed");
        } catch (InvalidCredentialsException e) {
            LoggerHolder.LOG.error("", e);
            throw e;
        }
    }


    @Override
    public boolean isMasterOfContext(Credentials creds, Context ctx) throws InvalidCredentialsException {
        try {
            doNullCheck(creds);
        } catch (InvalidDataException e) {
            LoggerHolder.LOG.error("authdata is null", e);
            throw new InvalidCredentialsException("authentication failed");
        }
        if (Strings.isEmpty(creds.getLogin())) {
            LoggerHolder.LOG.error("authdata has empty login");
            throw new InvalidCredentialsException("authentication failed");
        }
        try {
            OXResellerStorageInterface oxresell = OXResellerStorageInterface.getInstance();
            if (!oxresell.existsAdmin(new ResellerAdmin(creds.getLogin()))) {
                return false;
            }
            ResellerAdmin adm = oxresell.getData(new ResellerAdmin[] { new ResellerAdmin(creds.getLogin()) })[0];
            return oxresell.ownsContextOrIsPidOfOwner(ctx, adm.getId().intValue());
        } catch (StorageException e) {
            LoggerHolder.LOG.error("", e);
            throw new InvalidCredentialsException("authentication failed");
        }
    }

}
