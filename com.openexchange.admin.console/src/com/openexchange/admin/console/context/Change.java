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

package com.openexchange.admin.console.context;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Set;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class Change extends ChangeCore {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();

    /**
     * Entry point
     *
     * @param args command line arguments
     */
    public static void main(final String args[]) {
        new Change().execute(args);
    }

    /**
     * Initializes a new {@link Change}.
     */
    private Change() {
        super();
    }

    /**
     * Executes the command
     *
     * @param args the command line arguments
     */
    private void execute(String[] args) {
        commonfunctions(new AdminParser("changecontext"), args);
    }

    @Override
    protected void maincall(final AdminParser parser, final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // get rmi ref
        OXContextInterface oxctx = OXContextInterface.class.cast(Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME));

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);

        // remove login mappings
        ctxabs.parseAndSetRemoveLoginMapping(parser);

        ctxabs.changeMappingSetting(oxctx, ctx, auth, true);

        // do the change
        oxctx.change(ctx, auth);

        UserModuleAccess changed_access = oxctx.getModuleAccess(ctx, auth);
        boolean wantsChange = setModuleAccessOptions(parser, changed_access);

        String accessCombinationName = parseAndSetAccessCombinationName(parser);

        if (wantsChange && accessCombinationName == null) {
            // user wants to change individual perms
            oxctx.changeModuleAccess(ctx, changed_access, auth);
        } else if (accessCombinationName != null && !wantsChange) {
            oxctx.changeModuleAccess(ctx, accessCombinationName, auth);
        } else if (accessCombinationName != null && wantsChange) {
            throw new InvalidDataException(ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR);
        }

        Set<String> capabilitiesToAdd = parseAndSetCapabilitiesToAdd(parser);
        Set<String> capabilitiesToRemove = parseAndSetCapabilitiesToRemove(parser);
        Set<String> capabilitiesToDrop = parseAndSetCapabilitiesToDrop(parser);
        if ((null != capabilitiesToAdd && !capabilitiesToAdd.isEmpty()) || (null != capabilitiesToRemove && !capabilitiesToRemove.isEmpty()) || (null != capabilitiesToDrop && !capabilitiesToDrop.isEmpty())) {
            oxctx.changeCapabilities(ctx, capabilitiesToAdd, capabilitiesToRemove, capabilitiesToDrop, auth);
        }

        final String module = parseAndSetQuotaModule(parser);
        if (null == module) {
            final Long quotaValue = parseAndSetQuotaValue(parser);
            if (null != quotaValue) {
                throw new InvalidDataException("'--quota-value' argument specified, but '--quota-module' argument is missing.");
            }
        } else {
            final Long quotaValue = parseAndSetQuotaValue(parser);
            if (null == quotaValue) {
                throw new InvalidDataException("'--quota-module' argument specified, but '--quota-value' argument is missing.");
            }
            oxctx.changeQuota(ctx, module, quotaValue.longValue(), auth);
        }
    }

    @Override
    protected void setFurtherOptions(final AdminParser parser) {
        parser.setExtendedOptions();
        ctxabs.setAddMappingOption(parser, false);
        ctxabs.setRemoveMappingOption(parser, false);
        setConfigOption(parser);
        setRemoveConfigOption(parser);
        setAddAccessRightCombinationNameOption(parser);
        setModuleAccessOptions(parser);
        setCapsToAdd(parser);
        setCapsToRemove(parser);
        setCapsToDrop(parser);
        setQuotaModule(parser);
        setQuotaValue(parser);
    }
}
