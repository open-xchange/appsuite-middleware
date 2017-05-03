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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
