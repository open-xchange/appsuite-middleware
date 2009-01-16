package com.openexchange.admin.reseller.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;


public abstract class ResellerAbstraction extends BasicCommandlineOptions {

    private String  adminname   = null;
    private Integer adminid     = null;
    private String  displayname = null;
    private String  password    = null;

    protected static final char OPT_ID_SHORT = 'i';
    protected static final String OPT_ID_LONG = "adminid";
    protected static final char OPT_ADMINNAME_SHORT = 'a';
    protected static final String OPT_ADMINNAME_LONG = "adminname";
    protected static final char OPT_DISPLAYNAME_SHORT = 'd';
    protected static final String OPT_DISPLAYNAME_LONG = "displayname";
    protected static final char OPT_PASSWORD_SHORT = 'p';
    protected static final String OPT_PASSWORD_LONG = "password";

    protected Option idOption = null;
    protected Option adminNameOption = null;
    protected Option displayNameOption = null;
    protected Option passwordOption = null;

    protected final void setIdOption(final AdminParser admp){
        this.idOption =  setShortLongOpt(admp,OPT_ID_SHORT,OPT_ID_LONG,"Id of the user", true, NeededQuadState.eitheror);
    }
    
    protected final void setAdminnameOption(final AdminParser admp, final NeededQuadState needed) {
        this.adminNameOption = setShortLongOpt(admp,OPT_ADMINNAME_SHORT,OPT_ADMINNAME_LONG,"Name of the admin user", true, needed);
    }
    
    protected final void setDisplayNameOption(final AdminParser admp, final NeededQuadState needed) {
        this.displayNameOption = setShortLongOpt(admp,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the admin user", true, needed); 
    }
    
    protected final void setPasswordOption(final AdminParser admp, final NeededQuadState needed) {
        this.passwordOption =  setShortLongOpt(admp,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the admin user", true, needed); 
    }

    protected void setCreateOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
//        setAdminUserOption(parser);
//        setAdminPassOption(parser);
        
        setIdOption(parser);
        setAdminnameOption(parser, NeededQuadState.needed);
        setDisplayNameOption(parser, NeededQuadState.needed);
        setPasswordOption(parser, NeededQuadState.needed);
        
    }
    
    protected void parseAndSetAdminname(final AdminParser parser, final ResellerAdmin adm) {
        this.adminname = (String) parser.getOptionValue(this.adminNameOption);
        if (null != this.adminname) {
            adm.setName(this.adminname);
        }
    }
    
    protected void parseAndSetAdminId(final AdminParser parser, final ResellerAdmin adm) {
        final String optionValue = (String) parser.getOptionValue(this.idOption);
        if (null != optionValue) {
            adminid = Integer.parseInt(optionValue);
            adm.setId(adminid);
        }
    }

    protected void parseAndSetDisplayname(final AdminParser parser, final ResellerAdmin adm) {
        this.displayname = (String) parser.getOptionValue(this.displayNameOption);
        if (null != this.displayname) {
            adm.setName(this.displayname);
        }
    }

    protected void parseAndSetPassword(final AdminParser parser, final ResellerAdmin adm) {
        this.password = (String) parser.getOptionValue(this.passwordOption);
        if (null != this.password) {
            adm.setName(this.password);
        }
    }

    protected final ResellerAdmin resellerparsing(final AdminParser parser) {
        final ResellerAdmin adm = new ResellerAdmin();
        
        parseAndSetAdminId(parser, adm);
        parseAndSetAdminname(parser, adm);
        parseAndSetDisplayname(parser, adm);
        parseAndSetPassword(parser, adm);
        
        return adm;
    }

    protected OXResellerInterface getResellerInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXResellerInterface) Naming.lookup(RMI_HOSTNAME + OXResellerInterface.RMI_NAME);
    }

}
