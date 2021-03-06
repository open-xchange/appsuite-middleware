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
import org.apache.jsieve.parser.generated.ASTblock;
import org.apache.jsieve.parser.generated.ASTcommand;
import org.apache.jsieve.parser.generated.ASTstart;
import org.apache.jsieve.parser.generated.Node;
import org.apache.jsieve.parser.generated.SieveParserVisitor;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.IfOrElseIfCommand;
import com.openexchange.jsieve.commands.RequireCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;

/**
 * {@link NodeType} - Used to distinguish between the main control blocks in a sieve script
 *
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum NodeType {

    /**
     * Parses the 'require' directive of a sieve script
     */
    REQUIRE() {

        @Override
        void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException {
            final RequireCommand requireCommand = new RequireCommand((ArrayList<ArrayList<String>>) VisitorUtil.visitChildren(node, null, sieveParserVisitor));
            final ArrayList<Command> commands = new ArrayList<Command>();
            commands.add(requireCommand);
            ((ArrayList<Rule>) data).add(new Rule(commands, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
        }
    },

    /**
     * Parses the 'if' control block of a sieve rule
     */
    IF() {

        @Override
        void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException {
            final IfOrElseIfCommand ifCommand = new IfCommand();
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                final Node child = node.jjtGetChild(i);
                final Object jjtAccept = child.jjtAccept(sieveParserVisitor, data);
                if (jjtAccept instanceof TestCommand) {
                    final TestCommand command = (TestCommand) jjtAccept;
                    ifCommand.setTestcommand(command);
                } else if (jjtAccept instanceof ArrayList) {
                    final ArrayList<ActionCommand> command = (ArrayList<ActionCommand>) jjtAccept;
                    for (Object o : command) {
                        if (o instanceof Rule) {
                            throw new SieveException("Nested 'if' commands are not supported by this implementation");
                        }
                    }
                    ifCommand.setActionCommands(command);
                }
            }
            final ArrayList<Command> commandList = new ArrayList<Command>();
            commandList.add(ifCommand);
            ((ArrayList<Rule>) data).add(new Rule(commandList, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
        }
    },

    /**
     * Parses the 'elsif' control block of a sieve rule. Unsupported at the moment
     */
    ELSIF() {

        @Override
        void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException {
            parseUnsupported(node, data, commented);
        }

    },

    /**
     * Parses the 'else' control block of a sieve rule. Unsupported at the moment
     */
    ELSE() {

        @Override
        void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException {
            parseUnsupported(node, data, commented);
        }

    },

    /**
     * Parses everything else, i.e tag arguments and action commands
     */
    OTHER() {

        @Override
        void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException {
            final ArrayList<Object> visitChildren = (ArrayList<Object>) VisitorUtil.visitChildren(node, data, sieveParserVisitor);
            final ArrayList<Object> arguments = new ArrayList<Object>();
            for (final Object obj : visitChildren) {
                if (obj instanceof TagArgument) {
                    final TagArgument tag = (TagArgument) obj;
                    arguments.add(tag);
                } else if (obj instanceof NumberArgument) {
                    final NumberArgument numberarg = (NumberArgument) obj;
                    arguments.add(numberarg);
                } else if (obj instanceof ArrayList) {
                    arguments.add(obj);
                } else {
                    final ArrayList<String> arrayList = new ArrayList<String>();
                    arrayList.add(obj.toString());
                    arguments.add(arrayList);
                }
            }

            final String name = node.getName();
            for (final ActionCommand.Commands command : ActionCommand.Commands.values()) {
                if (!command.getCommandName().equals(name)) {
                    // What the hell....
                    continue;
                }

                final ActionCommand actionCommand = new ActionCommand(command, arguments);
                // Here we have to decide if we are on the base level or inside a control command. 
                // If we are inside a control command the parent-parent node is a block, so test this.
                if (node.jjtGetParent().jjtGetParent() instanceof ASTblock) {
                    ((ArrayList<ActionCommand>) data).add(actionCommand);
                } else if (node.jjtGetParent().jjtGetParent() instanceof ASTstart) {
                    // As a workaround we surround the commands with an if (true)
                    final ArrayList<ActionCommand> actionCommands = new ArrayList<ActionCommand>();
                    actionCommands.add(actionCommand);
                    final IfOrElseIfCommand ifCommand = new IfCommand();
                    ifCommand.setTestcommand(new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>()));
                    ifCommand.setActionCommands(actionCommands);
                    final ArrayList<Command> cmds = new ArrayList<Command>();
                    cmds.add(ifCommand);
                    ((ArrayList<Rule>) data).add(new Rule(cmds, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
                }
            }
        }
    }

    ;

    /**
     * Parses the unsupported node
     * 
     * @param node The node
     * @param data The data
     * @param commented whether to comment the rule
     */
    static void parseUnsupported(ASTcommand node, Object data, boolean commented) {
        ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), node.getName() + " is not support by this implementation."));
    }

    /**
     * Parses the specified {@link ASTcommand} and to the specified data object
     * 
     * @param node The node to parse
     * @param data The data to parse the node into
     * @param commented if <code>true</code> the parsed rule will be commented/disabled
     * @param sieveParserVisitor the {@link SieveParserVisitor} to use
     * 
     * @throws SieveException if a parsing error is occurred
     */
    abstract void parse(ASTcommand node, Object data, boolean commented, SieveParserVisitor sieveParserVisitor) throws SieveException;
}
