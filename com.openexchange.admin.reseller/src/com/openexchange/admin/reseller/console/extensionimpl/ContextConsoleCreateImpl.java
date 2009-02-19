package com.openexchange.admin.reseller.console.extensionimpl;

import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtension;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


public class ContextConsoleCreateImpl implements ContextConsoleCreateInterface {
    
    protected Option addRestrictionsOption = null;
    protected Option customidOption = null;

    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
        parser.removeOption("c", "contextid");
        addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        customidOption = parser.addOption(ResellerAbstraction.OPT_CUSTOMID_SHORT, ResellerAbstraction.OPT_CUSTOMID_LONG, ResellerAbstraction.OPT_CUSTOMID_LONG, "Custom Context ID", NeededQuadState.notneeded, true); 
    }

    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) throws OXConsolePluginException {
        final OXContextExtension firstExtensionByName = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
        try {
            HashSet<Restriction> restrictions = getRestrictions(parser);
            final String customid = ResellerAbstraction.parseCustomId(parser, customidOption);
            if (null == firstExtensionByName) {
                final OXContextExtension ctxext;
                if (null != restrictions) {
                    ctxext = new OXContextExtension(restrictions);
                }  else {
                    ctxext = new OXContextExtension();
                }
                if (null != customid) {
                    ctxext.setCustomid(customid);
                }
                // TODO: Maybe it's worth checking if the extension has data here, this may depent on the how much overhead a set extension imposes to the server
                ctx.addExtension(ctxext);
            } else {
                if (null != restrictions) {
                    firstExtensionByName.setRestriction(restrictions);
                }
                if (null != customid) {
                    firstExtensionByName.setCustomid(customid);
                }
            }
        } catch (final InvalidDataException e) {
            throw new OXConsolePluginException(e);
        } catch (final OXResellerException e) {
            throw new OXConsolePluginException("A reseller exception occured: " + e.getMessage());
        } catch (final DuplicateExtensionException e) {
            // Throw this one, but this should never occur as we check beforehand
            throw new OXConsolePluginException(e);
        }

    }

    private HashSet<Restriction> getRestrictions(final AdminParser parser) throws InvalidDataException, OXResellerException {
        final HashSet<Restriction> addres = ResellerAbstraction.parseRestrictions(parser, this.addRestrictionsOption);
        
        HashSet<Restriction> restrictions = null;
        if (!addres.isEmpty()) {
            final HashSet<Restriction> dbres = new HashSet<Restriction>();
            restrictions = ResellerAbstraction.handleAddEditRemoveRestrictions(dbres, addres, null, null);
        }
        return restrictions;
    }

}
