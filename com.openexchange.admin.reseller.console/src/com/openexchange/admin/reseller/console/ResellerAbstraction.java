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

package com.openexchange.admin.reseller.console;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException.Code;
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
    // Also used in console extension
    public static final char OPT_ADD_RESTRICTION_SHORT = 'a';
    public static final String OPT_ADD_RESTRICTION_LONG = "addrestriction";
    public static final char OPT_EDIT_RESTRICTION_SHORT = 'e';
    public static final String OPT_EDIT_RESTRICTION_LONG = "editrestriction";
    public static final char OPT_REMOVE_RESTRICTION_SHORT = 'r';
    public static final String OPT_REMOVE_RESTRICTION_LONG = "removerestriction";
    public static final char OPT_CUSTOMID_SHORT = 'C';
    public static final String OPT_CUSTOMID_LONG = "customid";
    public static final char OPT_PARENT_ID_SHORT = 'n';
    public static final String OPT_PARENT_ID_LONG = "parentid";

    protected CLIOption idOption = null;
    protected CLIOption adminNameOption = null;
    protected CLIOption displayNameOption = null;
    protected CLIOption passwordOption = null;
    protected CLIOption passwordMechOption = null;
    protected CLIOption addRestrictionsOption = null;
    protected CLIOption editRestrictionsOption = null;
    protected CLIOption removeRestrictionsOption = null;
    protected CLIOption parentIdOption = null;

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
        this.passwordMechOption = setShortLongOpt(admp, OPT_PASSWORDMECH_SHORT, OPT_PASSWORDMECH_LONG, "Password mechanism to use (CRYPT/SHA/BCRYPT)", true, NeededQuadState.notneeded);
    }

    protected final void setAddRestrictionsOption(final AdminParser admp) {
        this.addRestrictionsOption = setShortLongOpt(admp, OPT_ADD_RESTRICTION_SHORT, OPT_ADD_RESTRICTION_LONG, "Restriction to add (can be specified multiple times)", true, NeededQuadState.notneeded);
    }

    protected final void setEditRestrictionsOption(final AdminParser admp) {
        this.editRestrictionsOption = setShortLongOpt(admp, OPT_EDIT_RESTRICTION_SHORT, OPT_EDIT_RESTRICTION_LONG, "Restriction to edit (can be specified multiple times)", true, NeededQuadState.notneeded);
    }

    protected final void setRemoveRestrictionsOption(final AdminParser admp) {
        this.removeRestrictionsOption = setShortLongOpt(admp, OPT_REMOVE_RESTRICTION_SHORT, OPT_REMOVE_RESTRICTION_LONG, "Restriction to remove (can be specified multiple times)", true, NeededQuadState.notneeded);
    }

    protected final void setParentIdOption(final AdminParser admp){
        this.parentIdOption = setShortLongOpt(admp, OPT_PARENT_ID_SHORT, OPT_PARENT_ID_LONG, "ParentId of the user", true, NeededQuadState.notneeded);
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
        setEditRestrictionsOption(parser);
        setRemoveRestrictionsOption(parser);
        setParentIdOption(parser);
    }

    protected void setCreateOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        setAdminnameOption(parser, NeededQuadState.needed);
        setDisplayNameOption(parser, NeededQuadState.needed);
        setPasswordOption(parser, NeededQuadState.needed);
        setPasswordMechOption(parser);
        setAddRestrictionsOption(parser);
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
            this.adminid = new Integer(optionValue);
            adm.setId(adminid);
        }
    }

    protected void parseAndSetParentId(final AdminParser parser, final ResellerAdmin adm) {
        final String optionValue = (String) parser.getOptionValue(this.parentIdOption);
        if (null != optionValue) {
            adm.setParentId(new Integer(optionValue));
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

    public static HashSet<String> getRestrictionsToRemove(final AdminParser parser, final CLIOption option) {
        final Collection<Object> resopts = parser.getOptionValues(option);
        if (0 == resopts.size()) {
            return null;
        }
        final HashSet<String> ret = new HashSet<String>();

        for (Object opt : resopts) {
            ret.add((String) opt);
        }
        return ret;
    }

    public static HashSet<Restriction> getRestrictionsToEdit(final AdminParser parser, final CLIOption option) throws InvalidDataException {
        return parseRestrictions(parser, option);
    }

    protected static void parseAndSetAddRestrictions(final AdminParser parser, final ResellerAdmin adm, final CLIOption option) throws InvalidDataException {
        HashSet<Restriction> res = parseRestrictions(parser, option);
        if( res.size() > 0 ) {
            adm.setRestrictions(res.toArray(new Restriction[res.size()]));
        }
    }

    public static HashSet<Restriction> parseRestrictions(final AdminParser parser, final CLIOption option) throws InvalidDataException {
        final Collection<Object> resopts = parser.getOptionValues(option);
        HashSet<Restriction> res = new HashSet<Restriction>();
        for (final Object obj : resopts) {
            final String opt = (String) obj;
            res.add(getRestrictionFromString(opt));
        }
        return res;
    }

    public static Restriction getRestrictionFromString(final String opt) throws InvalidDataException {
        if( opt.indexOf('=') < 0 ) {
            throw new InvalidDataException("Restriction must be key=value pair");
        }
        final String[] keyval = opt.split("=");
        if( keyval.length > 2 ) {
            throw new InvalidDataException("Restriction must only contain one \"=\" character");
        }
        final Restriction restriction = new Restriction(keyval[0], keyval[1]);
        return restriction;
    }

    public static final String parseCustomId(final AdminParser parser, final CLIOption customIdOption) {
        final String customid = (String) parser.getOptionValue(customIdOption);
        return customid;
    }

    protected final ResellerAdmin parseDeleteOptions(final AdminParser parser) {
        final ResellerAdmin adm = new ResellerAdmin();

        parseAndSetAdminId(parser, adm);
        parseAndSetAdminname(parser, adm);

        return adm;
    }

    protected final ResellerAdmin parseChangeOptions(final AdminParser parser) throws InvalidDataException {
        final ResellerAdmin adm = parseCreateOptions(parser);
        parseAndSetParentId(parser, adm);
        
        return adm;
    }

    protected final ResellerAdmin parseCreateOptions(final AdminParser parser) throws InvalidDataException {
        final ResellerAdmin adm = new ResellerAdmin();

        parseAndSetAdminname(parser, adm);
        parseAndSetDisplayname(parser, adm);
        parseAndSetPassword(parser, adm);
        parseAndSetPasswordMech(parser, adm);
        parseAndSetAddRestrictions(parser, adm, this.addRestrictionsOption);

        return adm;
    }

    protected OXResellerInterface getResellerInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXResellerInterface) Naming.lookup(RMI_HOSTNAME + OXResellerInterface.RMI_NAME);
    }

    @Override
    protected String getObjectName() {
        return "admin";
    }

    public static HashSet<Restriction> handleAddEditRemoveRestrictions(final HashSet<Restriction> dbres, final HashSet<Restriction> addres, HashSet<String> removeRes, HashSet<Restriction> editRes) throws OXResellerException {
        // check whether user want's to remove restrictions
        final boolean wants2add = addres != null && addres.size() > 0;
        final boolean wants2edit = editRes != null && editRes.size() > 0;
        final boolean wants2remove = removeRes != null && removeRes.size() > 0;
        // XOR, either remove or add
        if ((wants2remove ^ wants2add ^ wants2edit) ^ (wants2remove && wants2add && wants2edit)) {
            if (wants2remove) {
                // remove existing restrictions from db
                if (dbres == null || dbres.size() == 0) {
                    throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "delete.");
                }
                final HashSet<Restriction> newres = new HashSet<Restriction>();
                for (final Restriction key : dbres) {
                    if (!removeRes.contains(key.getName())) {
                        if (!newres.add(key)) {
                            throw new OXResellerException(Code.RESTRICTION_ALREADY_CONTAINED, key.getName());
                        }
                    }
                }
                return newres;
            } else if (wants2add) {
                // add new restrictions to db
                if (dbres != null) {
                    final HashSet<Restriction> newset = new HashSet<Restriction>(addres);
                    for (final Restriction res : dbres) {
                        if (!newset.add(res)) {
                            throw new OXResellerException(Code.RESTRICTION_ALREADY_CONTAINED, res.getName());
                        }
                    }
                    return newset;
                } else {
                    return addres;
                }
            } else {
                // edit restrictions
                if (dbres == null || dbres.size() == 0) {
                    throw new OXResellerException(Code.NO_RESTRICTIONS_AVAILABLE_TO, "edit.");
                }
                for (final Restriction key : editRes) {
                    if (dbres.contains(key)) {
                        dbres.remove(key);
                        dbres.add(key);
                    } else {
                        throw new OXResellerException(Code.RESTRICTION_NOT_CONTAINED, key.getName());
                    }
                }
                return dbres;
            }
        } else {
            return null;
        }
    }

    /**
     * This method takes an array of objects and format them in one comma-separated string
     *
     * @param objects
     * @return
     */
    public static String getObjectsAsString(final Restriction[] objects) {
        final StringBuilder sb = new StringBuilder();
        if (null != objects && objects.length > 0) {
            for (final Restriction id : objects) {
                sb.append(id.getName());
                sb.append("=");
                sb.append(id.getValue());
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);

            return sb.toString();
        } else {
            return "";
        }
    }
}
