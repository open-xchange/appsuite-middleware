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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.external.accounts.clt;

import static com.openexchange.java.Autoboxing.I;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountRMIService;

/**
 * {@link DeleteExternalAccounts}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class DeleteExternalAccounts extends AbstractExternalAccountCLT {

    private static final String SYNTAX = "deleteexternalaccounts " + USAGE + " [-i <accountId>] " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "Warning! /!\\ If an OAuth account is selected for deletion (-m OAUTH), then ALL external accounts tied to that OAuth account will also be deleted /!\\";

    private int id;
    private int userId;
    private int contextId;
    private ExternalAccountModule module;

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new DeleteExternalAccounts().execute(args);
    }

    /**
     * Initializes a new {@link DeleteExternalAccounts}.
     */
    private DeleteExternalAccounts() {
        super(SYNTAX, FOOTER);
    }

    @Override
    boolean isModuleMandatory() {
        return true;
    }

    @Override
    boolean isUserMandatory() {
        return true;
    }

    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption(createArgumentOption("i", "accountId", "accountId", "Required. The account identifier", true));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        ExternalAccountRMIService service = getRmiStub(ExternalAccountRMIService.RMI_NAME);
        if (service.delete(id, contextId, userId, module)) {
            System.out.println(String.format("Account from module '%1$s' with id '%2$s' for user '%3$s' in context '%4$s' was deleted.", module, I(id), I(userId), I(contextId)));
        }
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) throws ParseException {
        this.contextId = getMandatoryInt('c', -1, cmd, options);
        this.userId = getMandatoryInt('u', -1, cmd, options);
        this.id = getMandatoryInt('i', -1, cmd, options);
        if (false == cmd.hasOption('m')) {
            System.err.println("Module is mandatory and not set. Use the '-m' option.");
            printHelp();
            System.exit(1);
        }
        this.module = getModule(cmd);
    }
}
