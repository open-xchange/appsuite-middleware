package com.openexchange.admin.reseller.console.extensionimpl;

import java.util.ArrayList;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleListInterface;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.reseller.console.ResellerAbstraction;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtension;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;


public class ContextConsoleListImpl implements ContextConsoleListInterface {
    
    private static final ArrayList<String> columnnames = new ArrayList<String>();

    private static final ArrayList<String> columnnamesCSV = new ArrayList<String>();
    
    static {
        columnnames.add("CustomID");
        columnnames.add("Owner");
        columnnamesCSV.addAll(columnnames);
        columnnamesCSV.add("Restrictions");
    }
    
    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException {
    }

    public void setAndFillExtension(final AdminParser parser, final Context ctx, Credentials auth) throws OXConsolePluginException {
    }

    public ArrayList<String> getColumnNamesCSV() {
        return columnnamesCSV;
    }

    public ArrayList<String> getColumnNamesHumanReadable() {
        return columnnames;
    }

    public ArrayList<String> getCSVData(final Context ctx) {
        final ArrayList<String> retval = new ArrayList<String>();
        final OXContextExtension extension = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
        final String customid = extension.getCustomid();
        if(extension.isCustomidset() && null != customid) {
            retval.add(customid);
        } else {
            retval.add(null);
        }
        final ResellerAdmin owner2 = extension.getOwner();
        if (extension.isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        final HashSet<Restriction> restriction = extension.getRestriction();
        if (extension.isRestrictionset() && null != restriction) {
            retval.add(ResellerAbstraction.getObjectsAsString(restriction));
        } else {
            retval.add(null);
        }
        
        return retval;
    }

    public ArrayList<String> getHumanReadableData(final Context ctx) {
        final ArrayList<String> retval = new ArrayList<String>();
        final OXContextExtension extension = (OXContextExtension) ctx.getFirstExtensionByName(OXContextExtension.class.getName());
        final String customid = extension.getCustomid();
        if(extension.isCustomidset() && null != customid) {
            retval.add(customid);
        } else {
            retval.add(null);
        }
        final ResellerAdmin owner2 = extension.getOwner();
        if (extension.isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        return retval;
    }

}
