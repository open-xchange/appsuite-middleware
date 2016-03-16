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
package com.openexchange.admin.console.util.reason;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;

/**
 *
 * @author d7,cutmasta
 *
 */
public class DeleteReason extends ReasonAbstraction {

    private final static char OPT_NAME_REASON_ID_SHORT = 'i';

    private final static String OPT_NAME_REASON_ID_LONG = "reasonid";

    private CLIOption reasonIDOption = null;

    public DeleteReason(final String[] args2) {

        final AdminParser parser = new AdminParser("deletereason");

        setOptions(parser);

        String reason_id = null;
        try {
            parser.ownparse(args2);

            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            // get rmi ref
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME +OXUtilInterface.RMI_NAME);

            reason_id = (String) parser.getOptionValue(this.reasonIDOption);
            final MaintenanceReason[] mrs = new MaintenanceReason[1];
            mrs[0] = new MaintenanceReason();
            mrs[0].setId(Integer.parseInt(reason_id));
            oxutil.deleteMaintenanceReason(mrs, auth);

            displayDeletedMessage(reason_id, null, parser);
            sysexit(0);
        } catch (final Exception e) {
            printErrors(reason_id, null, e, parser);
        }
    }

    public static void main(final String args[]) {
        new DeleteReason(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.reasonIDOption = setShortLongOpt(parser, OPT_NAME_REASON_ID_SHORT, OPT_NAME_REASON_ID_LONG, "the id for the reason to be deleted", true, NeededQuadState.needed);
    }
}
