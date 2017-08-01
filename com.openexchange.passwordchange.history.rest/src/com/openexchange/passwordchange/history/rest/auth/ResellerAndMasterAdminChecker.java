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

package com.openexchange.passwordchange.history.rest.auth;

import com.openexchange.admin.plugins.BasicAuthenticatorPluginInterface;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.auth.Authenticator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ResellerAndMasterAdminChecker}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ResellerAndMasterAdminChecker implements AuthChecker {

    private final boolean masterAccountOverride;
    private ServiceLookup service;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResellerAndMasterAdminChecker.class);

    /**
     * Initializes a new {@link ResellerAndMasterAdminChecker}.
     * 
     * @param service To get configuration from
     */
    public ResellerAndMasterAdminChecker(ServiceLookup service) {
        super();
        this.service = service;
        ConfigurationService configService = service.getService(ConfigurationService.class);

        if (null == configService) {
            this.masterAccountOverride = false;
            LOG.warn("Configuration service could not be aquired. Using standard values for checking API access.");
        } else {
            this.masterAccountOverride = configService.getBoolProperty("MASTER_ACCOUNT_OVERRIDE", false);
        }
    }

    @Override
    public boolean checkAccess(ServerSession session, String auth) {
        if (masterAccountOverride) {
            // Decode auth and validate
            if (Authorization.checkForBasicAuthorization(auth)) {
                // Valid header
                Credentials creds = Authorization.decode(auth);
                if (Authorization.checkLogin(creds.getPassword())) {
                    Authenticator authenticator = service.getService(Authenticator.class);
                    if (null != authenticator) {
                        // Authenticate the master administrator
                        try {
                            authenticator.doAuthentication(new com.openexchange.auth.Credentials(creds.getLogin(), creds.getPassword()));
                            return true;
                        } catch (OXException oxe) {
                            // Try reseller administrator
                            LOG.debug("Master admin could not be authenticated. Trying to authenticate reseller admin.");
                            BasicAuthenticatorPluginInterface resellerAuth = service.getService(BasicAuthenticatorPluginInterface.class);
                            if (null != resellerAuth) {
                                try {
                                    resellerAuth.doAuthentication(new com.openexchange.admin.rmi.dataobjects.Credentials(creds.getLogin(), creds.getPassword()));
                                    return true;
                                } catch (InvalidCredentialsException ice) {
                                    // Fall through
                                    LOG.debug("Reseller admin could not be authenticated.");
                                } // No valid reseller administrator found
                            } // No reseller Authenticator                             
                        } // Not the master administrator
                    } // No Authenticator service                    
                } // Flawed password
            } // No valid header
        } // Not configured to access API
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 23;
        int result = 1;
        result = prime * result + (masterAccountOverride ? 1327 : 1399);
        return result;
    }
}
