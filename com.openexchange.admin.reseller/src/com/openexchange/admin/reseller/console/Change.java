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

import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * @author choeger
 */
public class Change extends ResellerAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setChangeOptions(parser);
    }

    /**
     * 
     */
    public Change() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Change change = new Change();
        change.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("changeadmin");

        setOptions(parser);

        String successtext = null;
        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);
            final ResellerAdmin adm = parseChangeOptions(parser);

            final OXResellerInterface rsi = getResellerInterface();

            parseAndSetAdminId(parser, adm);
            parseAndSetAdminname(parser, adm);

            successtext = nameOrIdSetInt(this.adminid, this.adminname, "admin");

            // check whether user want's to remove restrictions
            final HashSet<String> removeRes = getRestrictionsToRemove(parser);
            final HashSet<Restriction> editRes = getRestrictionsToEdit(parser);
            final HashSet<Restriction> addres = adm.getRestrictions();
            final boolean wants2add = addres != null && addres.size() > 0;
            final boolean wants2edit = editRes != null && editRes.size() > 0;
            final boolean wants2remove = removeRes != null && removeRes.size() > 0;
            // XOR, either remove or add
            if ((wants2remove ^ wants2add ^ wants2edit) ^ (wants2remove && wants2add && wants2edit)) {
                final ResellerAdmin dbadm = rsi.getData(adm, auth);
                final HashSet<Restriction> dbres = dbadm.getRestrictions();
                if (wants2remove) {
                    // remove existing restrictions from db
                    if (dbres == null || dbres.size() == 0) {
                        throw new OXResellerException("No restrictions available to delete.");
                    }
                    final HashSet<Restriction> newres = new HashSet<Restriction>();
                    for (final Restriction key : dbres) {
                        if (!removeRes.contains(key.getName())) {
                            if (!newres.add(key)) {
                                throw new OXResellerException("The element " + key.getName() + " is already contained");
                            }
                        }
                    }
                    adm.setRestrictions(newres);
                } else if (wants2add) {
                    // add new restrictions to db
                    if (dbres != null) {
                        for (final Restriction res : dbres) {
                            if (!adm.getRestrictions().add(res)) {
                                throw new OXResellerException("The element " + res.getName() + " is already contained");
                            }
                        }
                    }
                } else {
                    // edit restrictions
                    if (dbres == null || dbres.size() == 0) {
                        throw new OXResellerException("No restrictions available to edit.");
                    }
                    for (final Restriction key : editRes) {
                        if (dbres.contains(key)) {
                            dbres.remove(key);
                            dbres.add(key);
                        } else {
                            throw new OXResellerException("The element " + key.getName() + " is not contained in the current restriction and thus cannot be edited");
                        }
                    }
                    adm.setRestrictions(dbres);
                }
            } else {
                throw new OXResellerException("Either add, edit or remove restrictions");
            }

            rsi.change(adm, auth);
            displayChangedMessage(successtext, null, parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
            sysexit(1);
        }
    }

}
