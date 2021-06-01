/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
