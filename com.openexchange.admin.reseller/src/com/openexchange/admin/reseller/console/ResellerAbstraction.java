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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.reseller.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;


/**
 * @author choeger
 *
 */
public abstract class ResellerAbstraction extends ObjectNamingAbstraction {

    protected static final char OPT_ID_SHORT = 'i';
    protected static final String OPT_ID_LONG = "adminid";
    protected static final char OPT_ADMINNAME_SHORT = 'u';
    protected static final String OPT_ADMINNAME_LONG = "adminname";
    protected static final char OPT_DISPLAYNAME_SHORT = 'd';
    protected static final String OPT_DISPLAYNAME_LONG = "displayname";
    protected static final char OPT_PASSWORD_SHORT = 'p';
    protected static final String OPT_PASSWORD_LONG = "password";
    protected static final char OPT_PASSWORDMECH_SHORT = 'm';
    protected static final String OPT_PASSWORDMECH_LONG = "passwordmech";
    protected static final char OPT_LIST_RESTRICTION_SHORT = 'l';
    protected static final String OPT_LIST_RESTRICTION_LONG = "listrestrictions";
    protected static final char OPT_ADD_RESTRICTION_SHORT = 'a';
    protected static final String OPT_ADD_RESTRICTION_LONG = "addrestriction";
    protected static final char OPT_REMOVE_RESTRICTION_SHORT = 'r';
    protected static final String OPT_REMOVE_RESTRICTION_LONG = "removerestriction";

    protected Option idOption = null;
    protected Option adminNameOption = null;
    protected Option displayNameOption = null;
    protected Option passwordOption = null;
    protected Option passwordMechOption = null;
    protected Option listRestrictionsOption = null;
    protected Option addRestrictionsOption = null;
    protected Option removeRestrictionsOption = null;
    
    protected Integer adminid = null;
    protected String adminname = null;
    
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

    protected final void setPasswordMechOption(final AdminParser admp) {
        this.passwordMechOption = setShortLongOpt(admp, OPT_PASSWORDMECH_SHORT, OPT_PASSWORDMECH_LONG, "Password mechanism to use (CRYPT/SHA)", true, NeededQuadState.notneeded);
    }

    protected final void setAddRestrictionsOption(final AdminParser admp) {
        this.addRestrictionsOption = setShortLongOpt(admp, OPT_ADD_RESTRICTION_SHORT, OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", true, NeededQuadState.notneeded);
    }

    protected final void setRemoveRestrictionsOption(final AdminParser admp) {
        this.removeRestrictionsOption = setShortLongOpt(admp, OPT_REMOVE_RESTRICTION_SHORT, OPT_REMOVE_RESTRICTION_LONG, "Restriction to remove (can be specified multiple times)", true, NeededQuadState.notneeded);
    }

    protected final void setListRestrictionsOption(final AdminParser admp) {
        this.listRestrictionsOption = setShortLongOpt(admp, OPT_LIST_RESTRICTION_SHORT, OPT_LIST_RESTRICTION_LONG, "List available restrictions", false, NeededQuadState.notneeded);
    }

    protected void setNameAndIdOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        
        setIdOption(parser);
        setAdminnameOption(parser, NeededQuadState.eitheror);
    }

    protected void setChangeOptions(final AdminParser parser) {
        setNameAndIdOptions(parser);

        setDisplayNameOption(parser, NeededQuadState.notneeded);
        setPasswordOption(parser, NeededQuadState.notneeded);
        setPasswordMechOption(parser);
        setAddRestrictionsOption(parser);
        setRemoveRestrictionsOption(parser);
        setListRestrictionsOption(parser);
    }

    protected void setCreateOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        
        setAdminnameOption(parser, NeededQuadState.needed);
        setDisplayNameOption(parser, NeededQuadState.needed);
        setPasswordOption(parser, NeededQuadState.needed);
        setPasswordMechOption(parser);
        setAddRestrictionsOption(parser);
        setListRestrictionsOption(parser);
    }
    
    protected void parseAndSetAdminname(final AdminParser parser, final ResellerAdmin adm) {
        final String adminname = (String) parser.getOptionValue(this.adminNameOption);
        if (null != adminname) {
            this.adminname = adminname;
            adm.setName(adminname);
        }
    }
    
    protected void parseAndSetAdminId(final AdminParser parser, final ResellerAdmin adm) {
        final String optionValue = (String) parser.getOptionValue(this.idOption);
        if (null != optionValue) {
            this.adminid = Integer.parseInt(optionValue);
            adm.setId(adminid);
        }
    }

    protected void parseAndSetDisplayname(final AdminParser parser, final ResellerAdmin adm) {
        final String displayname = (String) parser.getOptionValue(this.displayNameOption);
        if (null != displayname) {
            adm.setDisplayname(displayname);
        }
    }

    protected void parseAndSetPassword(final AdminParser parser, final ResellerAdmin adm) {
        final String password = (String) parser.getOptionValue(this.passwordOption);
        if (null != password) {
            adm.setPassword(password);
        }
    }

    protected void parseAndSetPasswordMech(final AdminParser parser, final ResellerAdmin adm) {
        final String passwordMech = (String) parser.getOptionValue(this.passwordMechOption);
        if (null != passwordMech) {
            adm.setPasswordMech(passwordMech);
        }
    }

    protected final HashSet<String> getRestrictionsToRemove(final AdminParser parser) throws InvalidDataException {
        final Vector<Object> resopts = parser.getOptionValues(this.removeRestrictionsOption);
        final HashSet<String> ret = new HashSet<String>();
        final Iterator<Object> i = resopts.iterator();
        while( i.hasNext() ) {
            final String opt = (String)i.next();
            ret.add(opt);
        }
        if( ret.size() > 0 ) {
            return ret;
        } else {
            return null;
        }
    }

    protected void parseAndSetAddRestrictions(final AdminParser parser, final ResellerAdmin adm) throws InvalidDataException {
        final Vector<Object> resopts = parser.getOptionValues(this.addRestrictionsOption);
        Iterator<Object> i = resopts.iterator();
        HashSet<Restriction> res = new HashSet<Restriction>();
        while( i.hasNext() ) {
            final String opt = (String)i.next();
            if( ! opt.contains("=") ) {
                throw new InvalidDataException("Restriction must be key=value pair");
            }
            final String[] keyval = opt.split("=");
            if( keyval.length > 2 ) {
                throw new InvalidDataException("Restriction must only contain one \"=\" character");
            }
            final Restriction r = new Restriction(keyval[0], keyval[1]);
            res.add(r);
        }
        if( res.size() > 0 ) {
            adm.setRestrictions(res);
        }
    }

    protected final ResellerAdmin parseDeleteOptions(final AdminParser parser) {
        final ResellerAdmin adm = new ResellerAdmin();
        
        parseAndSetAdminId(parser, adm);
        parseAndSetAdminname(parser, adm);
        
        return adm;
    }

    protected final ResellerAdmin parseChangeOptions(final AdminParser parser) throws InvalidDataException {
        return parseCreateOptions(parser);
    }

    protected final ResellerAdmin parseCreateOptions(final AdminParser parser) throws InvalidDataException {
        final ResellerAdmin adm = new ResellerAdmin();
        
        parseAndSetAdminname(parser, adm);
        parseAndSetDisplayname(parser, adm);
        parseAndSetPassword(parser, adm);
        parseAndSetPasswordMech(parser, adm);
        parseAndSetAddRestrictions(parser, adm);
        
        return adm;
    }

    /**
     * List available restrictions and call System.exit()
     * 
     * @param parser
     * @param rsi
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     */
    protected void listRestrictionsIfSpecified(final AdminParser parser, final OXResellerInterface rsi, final Credentials auth) throws RemoteException, InvalidCredentialsException {
        if( parser.getOptionValue(this.listRestrictionsOption) != null ) {
            final HashSet<Restriction> res = rsi.getAvailableRestrictions(auth);
            final Iterator<Restriction> i = res.iterator();
            System.out.println("Listing of available restrictions");
            while( i.hasNext() ) {
                final Restriction r = i.next();
                System.out.println(r.getName());
            }
            System.exit(0);
        }
    }
    
    protected OXResellerInterface getResellerInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXResellerInterface) Naming.lookup(RMI_HOSTNAME + OXResellerInterface.RMI_NAME);
    }

    @Override
    protected String getObjectName() {
        return "admin";
    }

}
