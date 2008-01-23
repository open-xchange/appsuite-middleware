package com.openexchange.admin.console.context;

import java.rmi.Naming;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;

public class ListContextByFilestore extends ContextAbstraction {

    private final ContextHostingAbstraction ctxabs = new ContextHostingAbstraction();
    
    protected void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        ctxabs.setFilestoreIdOption(parser);
        
        setCSVOutputOption(parser);
    }

    public ListContextByFilestore(final String[] args2) {

        final AdminParser parser = new AdminParser("listcontext");

        setOptions(parser);
        
        try {
            parser.ownparse(args2);

            final Filestore fs = ctxabs.parseAndSetFilestoreId(parser);

            final Credentials auth = credentialsparsing(parser);

            final OXContextInterface oxctx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
            final Context[] ctxs = oxctx.listByFilestore(fs, auth);

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(ctxs);
            } else {
                sysoutOutput(ctxs);
            }

            sysexit(0);
        } catch (final Exception e) {
            if (null == ctxabs.getFilestoreid()) {
                printErrors(null, null, e, parser);
            } else {
                printErrors(String.valueOf(ctxabs.getFilestoreid()), null, e, parser);
            }
        }
    }

    public static void main(final String args[]) {
        new ListContextByFilestore(args);
    }
    
    @Override
    protected final String getObjectName() {
        return "contexts for filestore";
    }
}
