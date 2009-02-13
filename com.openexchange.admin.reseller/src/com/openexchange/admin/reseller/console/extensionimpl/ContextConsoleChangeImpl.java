package com.openexchange.admin.reseller.console.extensionimpl;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleChangeInterface;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtension;
import com.openexchange.admin.rmi.dataobjects.Context;


public class ContextConsoleChangeImpl implements ContextConsoleChangeInterface {
    
    protected Option addRestrictionsOption = null;
    protected Option editRestrictionsOption = null;
    protected Option removeRestrictionsOption = null;

    public void addExtensionOptions(final AdminParser parser) {
        this.addRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_ADD_RESTRICTION_SHORT, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, ResellerAbstraction.OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", NeededQuadState.notneeded, true);
        this.editRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_EDIT_RESTRICTION_SHORT, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, ResellerAbstraction.OPT_EDIT_RESTRICTION_LONG, "Restriction to edit (can be specified multiple times)", NeededQuadState.notneeded, true);
        this.removeRestrictionsOption = parser.addOption(ResellerAbstraction.OPT_REMOVE_RESTRICTION_SHORT, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, ResellerAbstraction.OPT_REMOVE_RESTRICTION_LONG, "Restriction to remove (can be specified multiple times)", NeededQuadState.notneeded, true);
    }

    public void setAndFillExtension(final AdminParser parser, final Context ctx) {
        final String restrictions = (String) parser.getOptionValue(this.addRestrictionsOption);
        final OXContextExtension firstExtensionByName = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
//        if (null == firstExtensionByName) {
//            firstExtensionByName.setRestriction(restrictions);
//        } else {
//            ctx.addExtension(new OXContextExtension(restrictions));
//        }
    }

}
