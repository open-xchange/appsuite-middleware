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

package com.openexchange.admin.autocontextid.console.extensionimpl;

import java.util.HashMap;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.context.extensioninterfaces.ContextConsoleCreateInterface;
import com.openexchange.admin.console.user.UserAbstraction.CSVConstants;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

public class ContextConsoleCreateImpl implements ContextConsoleCreateInterface {

    private static final String CONTEXTID = "contextid";

    @Override
    public void addExtensionOptions(final AdminParser parser) {
        parser.removeOption("c", CONTEXTID);
    }

    @Override
    public void setAndFillExtension(final AdminParser parser, final Context ctx, final Credentials auth) {
        //
    }

    @Override
    public void processCSVConstants(final HashMap<String, CSVConstants> constantsMap) {
        final CSVConstants csvConstants = constantsMap.get(CONTEXTID);
        constantsMap.replace(CONTEXTID, new CSVConstants() {

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public String getString() {
                return csvConstants.getString();
            }

            @Override
            public int getIndex() {
                return csvConstants.getIndex();
            }
        });
    }

    @Override
    public void applyExtensionValuesFromCSV(String[] nextLine, int[] idarray, Context context) {
        //
    }

}
