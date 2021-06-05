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

package com.openexchange.admin.reseller.console.extensionimpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleChangeInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;


public class ContextConsoleChangeImpl extends BasicCommandlineOptions implements ContextConsoleChangeInterface {

    protected CLIOption addRestrictionsOption = null;
    protected CLIOption editRestrictionsOption = null;
    protected CLIOption removeRestrictionsOption = null;
    protected CLIOption customidOption = null;

    @Override
    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        editRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_EDIT_RESTRICTION_SHORT, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, "Restriction to edit (can be specified multiple times)", NeededQuadState.notneeded, true);
        removeRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_REMOVE_RESTRICTION_SHORT, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, "Restriction to remove (can be specified multiple times)", NeededQuadState.notneeded, true);
        customidOption = parser.addOption(ResellerAbstraction.OPT_CUSTOMID_SHORT, ResellerAbstraction.OPT_CUSTOMID_LONG, ResellerAbstraction.OPT_CUSTOMID_LONG, "Custom Context ID", NeededQuadState.notneeded, true);
    }

    @Override
    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) throws OXConsolePluginException {
        if (null == ctx) {
            return;
        }

        final OXContextExtensionImpl firstExtensionByName = (OXContextExtensionImpl) ctx.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
        try {
            final HashSet<Restriction> addres = ResellerAbstraction.parseRestrictions(parser, this.addRestrictionsOption);
            final HashSet<String> removeRes = ResellerAbstraction.getRestrictionsToRemove(parser, this.removeRestrictionsOption);
            final HashSet<Restriction> editRes = ResellerAbstraction.getRestrictionsToEdit(parser, this.editRestrictionsOption);
            final String customid = ResellerAbstraction.parseCustomId(parser, customidOption);

            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
            final Context data = oxctx.getData(ctx, auth);
            final Restriction[] dbres;
            final OXContextExtensionImpl dbctxext = (OXContextExtensionImpl) data.getFirstExtensionByName(OXContextExtensionImpl.class.getName());
            dbres = dbctxext.getRestriction();
            final HashSet<Restriction> ret = ResellerAbstraction.handleAddEditRemoveRestrictions(OXResellerTools.array2HashSet(dbres), addres, removeRes, editRes);
            Restriction[] restrictions = null;
            if ( null != ret ) {
                restrictions = ret.toArray(new Restriction[ret.size()]);
            }
            if (null == firstExtensionByName) {
                final OXContextExtensionImpl ctxext;
                if (null != restrictions) {
                    ctxext = new OXContextExtensionImpl(restrictions);
                } else {
                    ctxext = new OXContextExtensionImpl();
                }
                if ( null != customid ) {
                    ctxext.setCustomid(customid);
                }
                ctx.addExtension(ctxext);
            } else {
                if (null != restrictions) {
                    firstExtensionByName.setRestriction(restrictions);
                }
                if ( null != customid ) {
                    firstExtensionByName.setCustomid(customid);
                }
            }
        } catch (InvalidDataException e) {
            throw new OXConsolePluginException(e);
        } catch (RemoteException e) {
            throw new OXConsolePluginException(e);
        } catch (MalformedURLException e) {
            throw new OXConsolePluginException(e);
        } catch (InvalidCredentialsException e) {
            throw new OXConsolePluginException(e);
        } catch (StorageException e) {
            throw new OXConsolePluginException(e);
        } catch (OXResellerException e) {
            throw new OXConsolePluginException("A reseller exception occured: " + e.getMessage());
        } catch (NotBoundException e) {
            throw new OXConsolePluginException(e);
        } catch (DuplicateExtensionException e) {
            // Throw this one, but this should never occur as we check beforehand
            throw new OXConsolePluginException(e);
        } catch (NoSuchContextException e) {
            throw new OXConsolePluginException(e);
        }

    }

}
