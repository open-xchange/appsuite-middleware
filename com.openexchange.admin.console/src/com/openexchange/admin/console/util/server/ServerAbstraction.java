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
package com.openexchange.admin.console.util.server;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.dataobjects.Server;

public abstract class ServerAbstraction extends UtilAbstraction {

    static final String OPT_NAME_LONG = "name";
    static final String OPT_NAME_SERVER_ID_LONG = "id";
    static final char OPT_NAME_SERVER_ID_SHORT = 'i';
    static final char OPT_NAME_SHORT = 'n';

    protected String serverid = null;
    protected CLIOption serverIdOption = null;

    protected String servername = null;
    protected CLIOption serverNameOption = null;

    @Override
    protected String getObjectName() {
        return "server";
    }

    protected void parseAndSetServerID(final AdminParser parser, final Server sv) {
        serverid = (String) parser.getOptionValue(this.serverIdOption);
        if (null != serverid) {
            sv.setId(Integer.parseInt(serverid));
        }
    }

    protected void parseAndSetServername(final AdminParser parser, final Server sv) {
        servername = (String) parser.getOptionValue(this.serverNameOption);
        if (null != servername) {
            sv.setName(servername);
        }
    }

    protected void setServeridOption(final AdminParser parser) {
        serverIdOption = setShortLongOpt(parser, OPT_NAME_SERVER_ID_SHORT, OPT_NAME_SERVER_ID_LONG, "The id of the server which should be deleted", true, NeededQuadState.eitheror);
    }

    protected void setServernameOption(final AdminParser parser, final NeededQuadState needed) {
        this.serverNameOption = setShortLongOpt(parser, OPT_NAME_SHORT, OPT_NAME_LONG, "The name of the server", true, needed);
    }
}
