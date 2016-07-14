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

    public Change(final String[] args2) {
        final AdminParser parser = new AdminParser("changecontext");

        commonfunctions(parser, args2);
    }

    public static void main(final String args[]) {
        new Change(args);
    }

    @Override
    protected void maincall(final AdminParser parser, final Context ctx, final Credentials auth) throws MalformedURLException, RemoteException, NotBoundException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // get rmi ref
        final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

        // add login mappings
        ctxabs.parseAndSetAddLoginMapping(parser);

        // remove login mappings
        ctxabs.parseAndSetRemoveLoginMapping(parser);

        ctxabs.changeMappingSetting(oxctx, ctx, auth, true);

        // do the change
        oxctx.change(ctx, auth);


        UserModuleAccess changed_access = oxctx.getModuleAccess(ctx, auth);
        final boolean wantsChange = setModuleAccessOptions(parser, changed_access);

        final String accessCombinationName = parseAndSetAccessCombinationName(parser);

        if( wantsChange && accessCombinationName == null) {
            // user wants to change individual perms
            oxctx.changeModuleAccess(ctx, changed_access, auth);
        } else if (accessCombinationName != null && !wantsChange) {
            oxctx.changeModuleAccess(ctx, accessCombinationName, auth);
        } else if ( accessCombinationName != null && wantsChange ) {
            throw new InvalidDataException(ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR);
        }

        final Set<String> capabilitiesToAdd = parseAndSetCapabilitiesToAdd(parser);
        final Set<String> capabilitiesToRemove = parseAndSetCapabilitiesToRemove(parser);
        final Set<String> capabilitiesToDrop = parseAndSetCapabilitiesToDrop(parser);
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
