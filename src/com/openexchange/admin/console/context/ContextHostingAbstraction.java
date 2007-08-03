package com.openexchange.admin.console.context;

import java.rmi.RemoteException;
import java.util.Arrays;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This class is used to abstract to context related attributes and methods which are only needed
 * in the hosting part of Open-Xchange. This class is not only used to derive from it but it is also
 * used as aggregation inside some object. So the public method are used through aggregation while the
 * protected are used by inheritance.
 * 
 * @author d7
 *
 */
public class ContextHostingAbstraction extends ContextAbstraction {
//    private final static char OPT_REASON_SHORT = 'r';
//    private final static String OPT_REASON_LONG= "reason";

    private final static char OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT = 'L';
    private final static String OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG = "addmapping";
    
    private final static char OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT = 'R';
    private final static String OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG = "removemapping";
    
    private Option addLoginMappingOption = null;
    private Option removeLoginMappingOption = null;
    
    private String[] remove_mappings = null;
    private String[] add_mappings = null;

//    protected Option maintenanceReasonIDOption = null;

//    protected void setMaintenanceReasodIDOption(final AdminParser parser,final boolean required){
//        this.maintenanceReasonIDOption = setShortLongOpt(parser, OPT_REASON_SHORT,OPT_REASON_LONG,"Maintenance reason id",true, convertBooleantoTriState(required));
//    }
//    
    public void setAddMappingOption(final AdminParser parser,final boolean required ){
        this.addLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_ADD_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_ADD_LOGIN_MAPPINGS_LONG,"Add login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    public void setRemoveMappingOption(final AdminParser parser,final boolean required ){
        this.removeLoginMappingOption = setShortLongOpt(parser, OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_SHORT,OPT_CONTEXT_REMOVE_LOGIN_MAPPINGS_LONG,"Remove login mappings.Seperated by \",\"",true, convertBooleantoTriState(required));
    }
    
    protected final void displayDisabledMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(String.valueOf(id), ctxid, "disabled");
    }

    protected final void displayEnabledMessage(final String id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "enabled");
    }

    protected final void displayMovedMessage(final Integer id, final Integer ctxid, final String text) {
        createMessageForStdout(String.valueOf(id), ctxid, text);
    }

    public void parseAndSetRemoveLoginMapping(AdminParser parser) {
        if (parser.getOptionValue(this.removeLoginMappingOption) != null) {
            this.remove_mappings = ((String) parser.getOptionValue(this.removeLoginMappingOption)).split(",");
        }
    }

    public void parseAndSetAddLoginMapping(AdminParser parser) {
        if (parser.getOptionValue(this.addLoginMappingOption) != null) {
            this.add_mappings = ((String) parser.getOptionValue(this.addLoginMappingOption)).split(",");
        }
    }

    public void changeMappingSetting(final OXContextInterface oxres, final Context ctx, final Credentials auth, final boolean change) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        // check if wants to change login mappings, then first load current mappings from server
        if(add_mappings!=null || remove_mappings!=null){
            if (change) {
                Context server_ctx = oxres.getData(ctx, auth);
                ctx.setLoginMappings(server_ctx.getLoginMappings());
            } else {
                ctx.addLoginMapping(ctx.getIdAsString());
            }
            // add new mappings
            if (add_mappings != null) {
                ctx.addLoginMappings(Arrays.asList(add_mappings));
            }

            // remove mappings
            if(remove_mappings!=null){
                ctx.removeLoginMappings(Arrays.asList(remove_mappings));
            }
        }
    }

    /**
     * The disable, enable and move* command line tools are extended from this class so we can override
     * this method in order to create proper error messages.
     */
    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid) {
        if (getClass().getName().matches("^.*\\.\\w*(?i)enable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be enabled: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)disable\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be disabled: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wdatabase\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to database ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)move\\wfilestore\\w*$")) {
            final StringBuilder sb = new StringBuilder(getObjectName());
            if (null != id) {
                sb.append(" ");
                sb.append(id);
            }
            if (null != ctxid) {
                sb.append(" to filestore ");
                sb.append(ctxid);
            }
            sb.append(" could not be scheduled: ");
            System.err.println(sb.toString());
        } else {
            super.printFirstPartOfErrorText(id, ctxid);
        }
    }

    @Override
    protected void printErrors(final String id, final Integer ctxid, final Exception e, final AdminParser parser) {
        if (e instanceof NoSuchReasonException) {
            final NoSuchReasonException exc = (NoSuchReasonException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof OXContextException) {
            final OXContextException exc = (OXContextException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof NoSuchFilestoreException) {
            final NoSuchFilestoreException exc = (NoSuchFilestoreException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else {
            super.printErrors(id, ctxid, e, parser);
        }
    }
    
}
