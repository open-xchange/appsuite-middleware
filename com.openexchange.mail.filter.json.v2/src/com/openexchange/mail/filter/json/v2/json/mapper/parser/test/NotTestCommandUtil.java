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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.SieveException;
import com.openexchange.jsieve.commands.TestCommand;

/**
 * {@link NotTestCommandUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class NotTestCommandUtil {

    /**
     * Wraps a given {@link TestCommand} in a {@link TestCommand.Commands.NOT} {@link TestCommand}
     * 
     * @param com
     * @return
     * @throws SieveException
     */
    public static TestCommand wrapTestCommand(TestCommand com) throws SieveException {
        ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();
        final List<Object> argList0 = new ArrayList<Object>();
        testcommands.add(com);
        return new TestCommand(TestCommand.Commands.NOT, argList0, testcommands);
    }

}
