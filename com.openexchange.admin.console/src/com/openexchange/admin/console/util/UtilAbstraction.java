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
package com.openexchange.admin.console.util;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.console.AdminParser.NeededQuadState;

/**
 *
 * @author d7,cutmasta
 *
 */
public abstract class UtilAbstraction extends ObjectNamingAbstraction {

    //  Setting names for options
    protected final static char OPT_NAME_SEARCH_PATTERN_SHORT = 's';
    protected final static String OPT_NAME_SEARCH_PATTERN_LONG = "searchpattern";

    protected void setSearchOption(final AdminParser parser){
        this.searchOption = setShortLongOpt(parser, OPT_NAME_SEARCH_PATTERN_SHORT,OPT_NAME_SEARCH_PATTERN_LONG,"Search/List pattern!",true, NeededQuadState.notneeded);
    }

    protected void displayRegisteredMessage(final String id, final AdminParser parser) {
        createMessageForStdout(id, null, "registered", parser);
    }

    protected void displayUnregisteredMessage(final String id, final AdminParser parser) {
        createMessageForStdout(id, null, "unregistered", parser);
    }

    @Override
    protected void printFirstPartOfErrorText(final String id, final Integer ctxid, final AdminParser parser) {
        // Be aware of the order register matches also unregister so unregister must
        // be checked first
        if (getClass().getName().matches("^.*\\.\\w*(?i)unregister\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be unregistered: ", parser);
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)register\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be registered: ", parser);
        } else {
            super.printFirstPartOfErrorText(id, ctxid, parser);
        }
    }
}
