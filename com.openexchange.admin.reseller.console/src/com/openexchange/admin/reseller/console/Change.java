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

import java.util.HashSet;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.OXResellerTools;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
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

            final HashSet<String> removeRes = getRestrictionsToRemove(parser, this.removeRestrictionsOption);
            final HashSet<Restriction> editRes = getRestrictionsToEdit(parser, this.editRestrictionsOption);
            final ResellerAdmin dbadm = rsi.getData(adm, auth);
            final HashSet<Restriction> dbres = OXResellerTools.array2HashSet(dbadm.getRestrictions());
            final HashSet<Restriction> retRestrictions = handleAddEditRemoveRestrictions(dbres, OXResellerTools.array2HashSet(adm.getRestrictions()), removeRes, editRes);
            if (null != retRestrictions) {
                adm.setRestrictions(retRestrictions.toArray(new Restriction[retRestrictions.size()]));
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
