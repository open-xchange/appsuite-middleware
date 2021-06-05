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

package com.openexchange.jsieve.visitors;

import java.util.ArrayList;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.ASTtest;
import org.apache.jsieve.parser.generated.Node;
import org.apache.jsieve.parser.generated.SieveParserVisitor;
import org.apache.jsieve.parser.generated.SimpleNode;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.test.ITestCommand;

/**
 * {@link VisitorUtil}
 *
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class VisitorUtil {

    /**
     * Visits the specified {@link SimpleNode}
     * 
     * @param node The simple node to visit
     * @param data The data
     * @return
     * @throws SieveException if a parsing error is occurred
     */
    static Object visitChildren(final SimpleNode node, final Object data, SieveParserVisitor sieveParserVisitor) throws SieveException {
        Object jjtAccept = data;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            jjtAccept = child.jjtAccept(sieveParserVisitor, jjtAccept);
        }
        return jjtAccept;
    }

    /**
     * Visits the specified {@link ASTtest} node and parses the specifie {@link ITestCommand}
     * 
     * @param node The node to visit
     * @param data The rest of the data
     * @param name The name of the command
     * @param command The command to parse
     * @return The parsed {@link TestCommand}
     * @throws SieveException if a parsing error is occurred
     */
    static TestCommand visit(final ASTtest node, final Object data, final String name, final ITestCommand command, SieveParserVisitor sieveParserVisitor) throws SieveException {
        if (!command.getCommandName().equals(name)) {
            return null;
        }
        
        TestCommand testCommand = null;
        final Object visitChildren = visitChildren(node, data, sieveParserVisitor);
        if (visitChildren instanceof ArrayList) {
            final ArrayList<String> tagargs = new ArrayList<String>();
            final ArrayList<Object> arguments = new ArrayList<Object>();
            final ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();
            for (final Object obj : ((ArrayList<Object>) visitChildren)) {
                if (obj instanceof TagArgument) {
                    final TagArgument tag = (TagArgument) obj;
                    final String string = tag.toString();
                    tagargs.add(string);
                    arguments.add(tag);
                } else if (obj instanceof ArrayList) {
                    // Here we have to determine which type is inside, so we must check 
                    // the size first and then get first element
                    final ArrayList<Object> array = (ArrayList<Object>) obj;
                    if (!array.isEmpty()) {
                        final Object object = array.get(0);
                        if (object instanceof TestCommand) {
                            testcommands.addAll((ArrayList<TestCommand>) obj);
                        } else if (object instanceof String) {
                            arguments.add(obj);
                        }
                    }
                } else if (obj instanceof TestCommand) {
                    final TestCommand testcommand = (TestCommand) obj;
                    testcommands.add(testcommand);
                } else if (obj instanceof NumberArgument) {
                    final NumberArgument numberarg = (NumberArgument) obj;
                    arguments.add(numberarg);
                } else {
                    final ArrayList<String> array = new ArrayList<String>(1);
                    array.add(obj.toString());
                    arguments.add(array);
                }
            }
            testCommand = new TestCommand(command, arguments, testcommands);
        } else if (visitChildren instanceof TestCommand) {
            final ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();
            testcommands.add((TestCommand) visitChildren);
            testCommand = new TestCommand(command, new ArrayList<Object>(), testcommands);
        }
        return testCommand;
    }
}
