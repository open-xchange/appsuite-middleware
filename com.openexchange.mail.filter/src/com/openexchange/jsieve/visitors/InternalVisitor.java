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
package com.openexchange.jsieve.visitors;

import java.util.ArrayList;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.ASTargument;
import org.apache.jsieve.parser.generated.ASTarguments;
import org.apache.jsieve.parser.generated.ASTblock;
import org.apache.jsieve.parser.generated.ASTcommand;
import org.apache.jsieve.parser.generated.ASTcommands;
import org.apache.jsieve.parser.generated.ASTstart;
import org.apache.jsieve.parser.generated.ASTstring;
import org.apache.jsieve.parser.generated.ASTstring_list;
import org.apache.jsieve.parser.generated.ASTtest;
import org.apache.jsieve.parser.generated.ASTtest_list;
import org.apache.jsieve.parser.generated.Node;
import org.apache.jsieve.parser.generated.SieveParserVisitor;
import org.apache.jsieve.parser.generated.SimpleNode;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.IfOrElseIfCommand;
import com.openexchange.jsieve.commands.RequireCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;
import com.openexchange.jsieve.commands.test.ITestCommand;
import com.openexchange.jsieve.registry.TestCommandRegistry;
import com.openexchange.mailfilter.services.Services;

/**
 * This class is used to convert the JJTree Objects into the internal
 * representation via the visitor pattern
 *
 * @author d7
 *
 */
public class InternalVisitor implements SieveParserVisitor {

    private boolean commented;

    protected Object visitChildren(final SimpleNode node, final Object data) throws SieveException {
        Object jjtAccept = data;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            jjtAccept = child.jjtAccept(this, jjtAccept);
        }
        return jjtAccept;
    }

    @Override
    public Object visit(final SimpleNode node, final Object data) throws SieveException {
        return null;
    }

    @Override
    public Object visit(final ASTstart node, final Object data) throws SieveException {
        if (data instanceof Boolean) {
            final Boolean value = (Boolean) data;
            if (value) {
                commented = true;
            } else {
                commented = false;
            }
        }
        final Object visitChildren = visitChildren(node, null);
        return visitChildren;
    }

    @Override
    public Object visit(final ASTcommands node, final Object data) throws SieveException {
        if (null != data) {
            final Object visitChildren = visitChildren(node, data);
            return visitChildren;
        } else {
            final Object visitChildren = visitChildren(node, new ArrayList<Rule>(node.jjtGetNumChildren()));
            return visitChildren;
        }
    }

    @Override
    public Object visit(final ASTcommand node, final Object data) throws SieveException {
        final String name = node.getName();
        if ("require".equals(name)) {
            final ArrayList<Command> commands;
            try {
                final RequireCommand requireCommand = new RequireCommand((ArrayList<ArrayList<String>>) visitChildren(node, null));
                commands = new ArrayList<Command>();
                commands.add(requireCommand);
                ((ArrayList<Rule>) data).add(new Rule(commands, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
            } catch (final SieveException e) {
                ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), e.getMessage()));
            }
        } else if ("if".equals(name)) {
            try {
                final IfOrElseIfCommand ifCommand = new IfCommand();
                for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                    final Node child = node.jjtGetChild(i);
                    final Object jjtAccept = child.jjtAccept(this, data);
                    if (jjtAccept instanceof TestCommand) {
                        final TestCommand command = (TestCommand) jjtAccept;
                        ifCommand.setTestcommand(command);
                    } else if (jjtAccept instanceof ArrayList) {
                        final ArrayList command = (ArrayList) jjtAccept;
                        for (Object o : command) {
                            if (o instanceof Rule) {
                                throw new SieveException("Nested 'if' commands are not supported by this implementation");
                            }
                        }
                        ifCommand.setActionCommands(command);
                    }
                }
                final ArrayList<Command> commands = new ArrayList<Command>();
                commands.add(ifCommand);
                ((ArrayList<Rule>) data).add(new Rule(commands, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
            } catch (final SieveException e) {
                ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), e.getMessage()));
            }
        } else if ("elsif".equals(name)) {
            ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), "elsif is not support by this implementation."));
//            throw new SieveException("elsif is not support by this implementation.");
//            final IfOrElseIfCommand elsifCommand = new ElsifCommand();
//            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//                final Node child = node.jjtGetChild(i);
//                final Object jjtAccept = child.jjtAccept(this, data);
//                if (jjtAccept instanceof TestCommand) {
//                    TestCommand command = (TestCommand) jjtAccept;
//                    elsifCommand.setTestcommand(command);
//                } else if (jjtAccept instanceof ArrayList) {
//                    ArrayList command = (ArrayList) jjtAccept;
//                    elsifCommand.setActioncommands(command);
//                }
//            }
//
//            // Elseif makes only sense in conjunction with an if beforehand, so
//            // we check this here, and add
//            // the command to the corresponding rule
//            final Rule rule = ((ArrayList<Rule>) data).get(((ArrayList<Rule>) data).size() - 1);
//            final ArrayList<Command> commands = rule.getCommands();
//            final Command command = commands.get(0);
//            if (!(command instanceof IfCommand)) {
//                throw new SieveException("Found elsif without if before. Line " + node.getCoordinate().getStartLineNumber());
//            }
//            commands.add(elsifCommand);
        } else if ("else".equals(name)) {
            ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), "else is not support by this implementation."));
//            throw new SieveException("else is not supported by this implementation");
//            final IfStructureCommand elseCommand = new ElseCommand();
//            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
//                final Node child = node.jjtGetChild(i);
//                final Object jjtAccept = child.jjtAccept(this, data);
//                if (jjtAccept instanceof ArrayList) {
//                    ArrayList command = (ArrayList) jjtAccept;
//                    elseCommand.setActioncommands(command);
//                }
//            }
//
//            // Elseif makes only sense in conjunction with an if beforehand, so
//            // we check this here, and add
//            // the command to the corresponding rule
//            final Rule rule = ((ArrayList<Rule>) data).get(((ArrayList<Rule>) data).size() - 1);
//            final ArrayList<Command> commands = rule.getCommands();
//            final Command command = commands.get(commands.size() - 1);
//            if (!(command instanceof IfCommand) && !(command instanceof ElsifCommand)) {
//                throw new SieveException("Found else without if or elsif before. Line " + node.getCoordinate().getStartLineNumber());
//            }
//            commands.add(elseCommand);
        } else {
            try {
                final ArrayList<Object> visitChildren = (ArrayList<Object>) visitChildren(node, data);
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
                for (final ActionCommand.Commands command : ActionCommand.Commands.values()) {
                    if (command.getCommandName().equals(name)) {
                        final ActionCommand actionCommand = new ActionCommand(command, arguments);
                        // Here we have to decide if we are on the base level or
                        // inside a control command. If we are inside
                        // a control command the parent-parent node is a block, so
                        // test this
                        if (node.jjtGetParent().jjtGetParent() instanceof ASTblock) {
                            ((ArrayList<ActionCommand>) data).add(actionCommand);
                        } else if (node.jjtGetParent().jjtGetParent() instanceof ASTstart) {
                            // As a workaround we surround the commands with an if (true)
                            final ArrayList<ActionCommand> actionCommands = new ArrayList<ActionCommand>();
                            actionCommands.add(actionCommand);
                            final IfOrElseIfCommand ifCommand = new IfCommand();
                            ifCommand.setTestcommand(new TestCommand(TestCommand.Commands.TRUE, new ArrayList<Object>(), new ArrayList<TestCommand>()));
                            ifCommand.setActionCommands(actionCommands);
                            final ArrayList<Command> commands = new ArrayList<Command>();
                            commands.add(ifCommand);
                            ((ArrayList<Rule>) data).add(new Rule(commands, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), commented));
//                            ((ArrayList<Rule>) data).add(new Rule(commands, node.getCoordinate().getStartLineNumber(), commented));
//                            throw new SieveException("Action commands are not allowed on base level, line " + node.getCoordinate().getStartLineNumber());
                        } else {
                            // What the hell....
                        }
                    }
                }
            } catch (final SieveException e) {
                ((ArrayList<Rule>) data).add(new Rule(commented, node.getCoordinate().getStartLineNumber(), node.getCoordinate().getEndLineNumber(), e.getMessage()));
            }
        }
        return data;
    }

    @Override
    public Object visit(final ASTblock node, final Object data) throws SieveException {
        return visitChildren(node, new ArrayList<ActionCommand>());
    }

    @Override
    public Object visit(final ASTarguments node, final Object data) throws SieveException {
        return visitChildren(node, new ArrayList<Object>());
    }

    @Override
    public Object visit(final ASTargument node, final Object data) throws SieveException {
        if (0 < node.jjtGetNumChildren()) {
            final Object visitChildren = visitChildren(node, data);
            if (visitChildren instanceof ArrayList) {
                final ArrayList<String> list = (ArrayList<String>) visitChildren;
                ((ArrayList<ArrayList<String>>) data).add(list);
                return data;
            }
            return visitChildren;
        } else {
            final Object value = node.getValue();
            if (value instanceof TagArgument) {
                ((ArrayList<Object>) data).add(value);
                return data;
            } else if (value instanceof NumberArgument) {
                ((ArrayList<Object>) data).add(value);
                return data;
            } else {
                final String string = value.toString();
                ((ArrayList<Object>) data).add(string);
                return data;
            }
        }
    }

    @Override
    public Object visit(final ASTtest node, final Object data) throws SieveException {
        final String name = node.getName();
        // now use registry:
        TestCommandRegistry testCommandRegistry = Services.getService(TestCommandRegistry.class);
        for (final ITestCommand command : testCommandRegistry.getCommands()) {
            TestCommand testCommand = visit0(node, data, name, command);
            if (null != testCommand) {
                return testCommand;
            }
        }

        throw new SieveException("Found not known test name: " + name + " in line " + node.getCoordinate().getStartLineNumber());
    }

    private TestCommand visit0(final ASTtest node, final Object data, final String name, final ITestCommand command) throws SieveException {
        TestCommand testCommand = null;
        if (command.getCommandName().equals(name)) {
            final Object visitChildren = visitChildren(node, data);
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
                        // Here we have to determine which type is inside,
                        // so we must check the size first and then get
                        // first element
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
                testCommand =  new TestCommand(command, arguments, testcommands);
            } else if (visitChildren instanceof TestCommand) {
                final ArrayList<TestCommand> testcommands = new ArrayList<TestCommand>();
                testcommands.add((TestCommand) visitChildren);
                testCommand =  new TestCommand(command, new ArrayList<Object>(), testcommands);
            }
        }
        return testCommand;
    }

    @Override
    public Object visit(final ASTtest_list node, final Object data) throws SieveException {
        final ArrayList<TestCommand> list = new ArrayList<TestCommand>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            final TestCommand test = (TestCommand) child.jjtAccept(this, null);
            list.add(test);
        }
        ((ArrayList<Object>) data).add(list);
        return data;
    }

    @Override
    public Object visit(final ASTstring node, final Object data) throws SieveException {
        final Object value = node.getValue();
        final String string = value.toString();
        if (string.charAt(0) == '\"') {
            return string.substring(1, string.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        } else {
            final int linebreak = string.indexOf("\n");
            // Here we have to cut 5 chars from the end, because a linebreak in
            // a sieve script
            // consists of CRLF and the "text:" tag finishes with an empty "."
            // after the text
            return string.substring(linebreak + 1, string.length() - 5).replace("\\\"", "\"").replace("\\\\", "\\");
        }
    }

    @Override
    public Object visit(final ASTstring_list node, final Object data) throws SieveException {
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            final Node child = node.jjtGetChild(i);
            final String string = (String) child.jjtAccept(this, null);
            list.add(string);
        }
        return list;
    }

}
