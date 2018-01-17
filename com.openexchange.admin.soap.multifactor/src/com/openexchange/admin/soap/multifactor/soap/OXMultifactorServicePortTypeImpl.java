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

package com.openexchange.admin.soap.multifactor.soap;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.soap.multifactor.dataobjects.Credentials;
import com.openexchange.multifactor.rmi.MultifactorManagementRemoteService;

/**
 * {@link OXMultifactorServicePortType}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
@javax.jws.WebService(
    serviceName = "OXMultifactorService",
    portName = "OXMultifactorServiceHttpsEndpoint",
    targetNamespace = "http://soap.admin.openexchange.com",
    endpointInterface = "com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortType")
public class OXMultifactorServicePortTypeImpl implements OXMultifactorServicePortType {

    public static final AtomicReference<MultifactorManagementRemoteService> RMI_REFERENCE = new AtomicReference<>();

    private static com.openexchange.admin.rmi.dataobjects.Credentials soap2Credentials(Credentials soapCredentials){
        if(soapCredentials == null) {
            return null;
        }
        return new com.openexchange.admin.rmi.dataobjects.Credentials(soapCredentials.getLogin(), soapCredentials.getPassword());
    }

    private MultifactorManagementRemoteService getManagementService() throws Exception {
        MultifactorManagementRemoteService managementService = RMI_REFERENCE.get();
        if(managementService == null) {
            throw new Exception("Missing " + MultifactorManagementRemoteService.class.getName() + " instance.");
        }
        return managementService;

    }
    private MultifactorDeviceResult createResult(com.openexchange.multifactor.rmi.MultifactorDeviceResult result) {
       MultifactorDeviceResult ret = new MultifactorDeviceResult();
       ret.setId(result.getId());
       ret.setName(result.getName());
       ret.setProviderName(result.getProviderName());
       ret.setEnabled(result.isEnabled());
       ret.setBackupDevice(result.isBackupDevice());
       return ret;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortType#getMultifactorDevices(int, int, com.openexchange.admin.soap.multifactor.dataobjects.Credentials)
     */
    @Override
    public MultifactorDeviceResult[] getMultifactorDevices(int contextId, int userId, Credentials credentials) throws Exception {
        com.openexchange.multifactor.rmi.MultifactorDeviceResult[] devices = getManagementService().getMultifactorDevices(contextId, userId, soap2Credentials(credentials));
        MultifactorDeviceResult ret[] = new MultifactorDeviceResult[devices.length];
        for(int i=0; i<devices.length; i++) {
           ret[i] = createResult(devices[i]);
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortType#removeDevice(int, int, java.lang.String, java.lang.String, com.openexchange.admin.soap.multifactor.dataobjects.Credentials)
     */
    @Override
    public void removeDevice(int contextId, int userId, String providerName, String deviceId, Credentials credentials) throws Exception {
        getManagementService().removeDevice(contextId, userId, providerName, deviceId, soap2Credentials(credentials));
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.soap.multifactor.soap.OXMultifactorServicePortType#removeAllDevices(int, int, com.openexchange.admin.soap.multifactor.dataobjects.Credentials)
     */
    @Override
    public void removeAllDevices(int contextId, int userId, Credentials credentials) throws Exception {
        getManagementService().removeAllDevices(contextId, userId, soap2Credentials(credentials));
    }
}
