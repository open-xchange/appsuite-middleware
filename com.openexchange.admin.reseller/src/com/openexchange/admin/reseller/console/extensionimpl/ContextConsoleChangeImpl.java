package com.openexchange.admin.reseller.console.extensionimpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleChangeInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtension;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;


public class ContextConsoleChangeImpl implements ContextConsoleChangeInterface {
    
    protected Option addRestrictionsOption = null;
    protected Option editRestrictionsOption = null;
    protected Option removeRestrictionsOption = null;

    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        editRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_EDIT_RESTRICTION_SHORT, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, "Restriction to edit (can be specified multiple times)", NeededQuadState.notneeded, true);
        removeRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_REMOVE_RESTRICTION_SHORT, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, "Restriction to remove (can be specified multiple times)", NeededQuadState.notneeded, true);
    }

    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) throws OXConsolePluginException {
        final OXContextExtension firstExtensionByName = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
        try {
            final HashSet<Restriction> addres = ResellerAbstraction.parseRestrictions(parser, this.addRestrictionsOption);
            final HashSet<String> removeRes = ResellerAbstraction.getRestrictionsToRemove(parser, this.removeRestrictionsOption);
            final HashSet<Restriction> editRes = ResellerAbstraction.getRestrictionsToEdit(parser, this.editRestrictionsOption);
            
            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup("rmi://localhost:1099/" + OXContextInterface.RMI_NAME);
            final Context data = oxctx.getData(ctx, auth);
            final HashSet<Restriction> dbres;
            final OXContextExtension dbctxext = (OXContextExtension) data.getFirstExtensionByName(OXContextExtension.class.getName());
            if (null == dbctxext) {
                dbres = new HashSet<Restriction>();
            } else {
                dbres = dbctxext.getRestriction();
            }
            final HashSet<Restriction> restrictions = ResellerAbstraction.handleAddEditRemoveRestrictions(dbres, addres, removeRes, editRes);
            if (null == firstExtensionByName) {
                ctx.addExtension(new OXContextExtension(restrictions));
            } else {
                firstExtensionByName.setRestriction(restrictions);
            }
        } catch (final InvalidDataException e) {
            throw new OXConsolePluginException(e);
        } catch (final RemoteException e) {
            throw new OXConsolePluginException(e);
        } catch (final MalformedURLException e) {
            throw new OXConsolePluginException(e);
        } catch (final InvalidCredentialsException e) {
            throw new OXConsolePluginException(e);
        } catch (final StorageException e) {
            throw new OXConsolePluginException(e);
        } catch (final OXResellerException e) {
            throw new OXConsolePluginException("A reseller exception occured: " + e.getMessage());
        } catch (final NotBoundException e) {
            throw new OXConsolePluginException(e);
        } catch (final DuplicateExtensionException e) {
            // Throw this one, but this should never occur as we check beforehand
            throw new OXConsolePluginException(e);
        } catch (final NoSuchContextException e) {
            throw new OXConsolePluginException(e);
        }

    }

}
