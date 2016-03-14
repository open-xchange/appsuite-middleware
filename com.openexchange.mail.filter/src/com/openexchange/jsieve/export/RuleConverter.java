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
package com.openexchange.jsieve.export;

import java.util.ArrayList;
import java.util.List;
import org.apache.jsieve.NumberArgument;
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
import org.apache.jsieve.parser.generated.Token;
import com.openexchange.jsieve.commands.ActionCommand;
import com.openexchange.jsieve.commands.Command;
import com.openexchange.jsieve.commands.ElseCommand;
import com.openexchange.jsieve.commands.IfCommand;
import com.openexchange.jsieve.commands.IfOrElseIfCommand;
import com.openexchange.jsieve.commands.RequireCommand;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.jsieve.commands.TestCommand;

/**
 * This class contains all the methods to convert a list of rules into the AST
 * notation
 *
 * @author d7
 *
 */
public class RuleConverter {

    /**
     * Initializes a new {@link RuleConverter}.
     */
    private RuleConverter() {
        super();
    }

    private static void addArguments(final ASTarguments targuments, final List<Object> argumentslist, final int[] js, final int[] p) {
        for (int k = 0; k < argumentslist.size(); k++) {
            final Object object = argumentslist.get(k);
            if (object instanceof List) {
                final List sublist = (List) object;
                final ASTargument arg = new ASTargument(js[0]++);
                arg.jjtAddChild(createStringList(sublist, js), 0);
                targuments.jjtAddChild(arg, p[0]++);
            } else if (object instanceof NumberArgument) {
                final NumberArgument arg = (NumberArgument) object;
                final ASTargument targument = new ASTargument(js[0]++);
                targument.setValue(arg);
                targuments.jjtAddChild(targument, p[0]++);
            } else if (object instanceof TagArgument) {
                final TagArgument tag = (TagArgument) object;
                addTagArgument(targuments, tag.getTag(), js, p);
            }
        }
    }

    private static void addTagArgument(final ASTarguments targuments2, final String text, final int[] js, final int[] p) {
        final Token token = new Token();
        token.image = text;
        final ASTargument targument = new ASTargument(js[0]++);
        targument.setValue(new TagArgument(token));
        targuments2.jjtAddChild(targument, p[0]++);
    }

    private static ASTblock createActionBlockForTest(final List<ActionCommand> actioncommands, final int[] js, final int linenumber) {
        final ASTblock tblock = new ASTblock(js[0]++);
        tblock.setFirstToken(getDummyToken("{"));
        tblock.setLastToken(getDummyToken("}"));
        tblock.jjtAddChild(createActionCommands(actioncommands, js, linenumber), 0);
        return tblock;
    }

    private static ASTcommand createActionCommand(final ArrayList<Object> arguments, final String commandname, final int[] js, final int line) {
        final ASTcommand tcommand = createCommand(commandname, js, line);
        if (!arguments.isEmpty()) {
            tcommand.jjtAddChild(createArguments(arguments, js), 0);
        }
        return tcommand;
    }

    private static ASTcommands createActionCommands(final List<ActionCommand> actioncommands, final int[] js, final int linenumber) {
        final ASTcommands tcommands = new ASTcommands(js[0]++);
        for (int k = 0; k < actioncommands.size(); k++) {
            final ActionCommand actionCommand = actioncommands.get(k);
            final String commandname = actionCommand.getCommand().getCommandName();
            final ASTcommand tcommand = createActionCommand(actionCommand.getArguments(), commandname, js, linenumber);
            tcommands.jjtAddChild(tcommand, k);
        }
        return tcommands;
    }

    private static ASTarguments createArguments(final ArrayList<Object> arguments, final int[] iarray) {
        final ASTarguments targuments = new ASTarguments(iarray[0]++);
        for (int i = 0; i < arguments.size(); i++) {
            final Object obj = arguments.get(i);
            if (obj instanceof List) {
                final List<String> arrayList = (List<String>) obj;
                final ASTargument targument = new ASTargument(iarray[0]++);
                targument.jjtAddChild(createStringList(arrayList, iarray), 0);
                targuments.jjtAddChild(targument, i);
            } else if (obj instanceof NumberArgument) {
                final NumberArgument arg = (NumberArgument) obj;
                final ASTargument targument = new ASTargument(iarray[0]++);
                targument.setValue(arg);
                targuments.jjtAddChild(targument, i);
            } else if (obj instanceof TagArgument) {
                final ASTargument targument = new ASTargument(iarray[0]++);
                targument.setValue(obj);
                targuments.jjtAddChild(targument, i);
            }
        }
        return targuments;
    }

    private static ASTcommand createCommand(final String commandname, final int[] iarray, final int line) {
        int i = iarray[0];
        final ASTcommand tcommand = new ASTcommand(i++);
        tcommand.setName(commandname);
        tcommand.setFirstToken(getDummyToken(commandname, line));
        tcommand.setLastToken(getDummyToken(";"));
        iarray[0] = i;
        return tcommand;
    }

    private static ASTtest createCompleteTestPart(final TestCommand testcommand, final int[] js) {
        final ASTtest ttest = new ASTtest(js[0]++);
        final List<TestCommand> testcommands = testcommand.getTestCommands();
        final String commandname = testcommand.getCommand().getCommandName();
        ttest.setName(commandname);
        if ("not".equals(commandname)) {
            ttest.jjtAddChild(createCompleteTestPart(testcommands.get(0), js), 0);
            return ttest;
        }
        if (!testcommands.isEmpty()) {
            final ASTarguments targuments = new ASTarguments(js[0]++);
            ttest.jjtAddChild(targuments, 0);
            final ASTtest_list ttest_list = new ASTtest_list(js[0]++);
            final int size = testcommands.size();
            ttest_list.setFirstToken(getDummyToken("("));
            ttest_list.setLastToken(getDummyToken(")"));
            targuments.jjtAddChild(ttest_list, 0);
            for (int i = 0; i < size; i++) {
                final TestCommand testCommand2 = testcommands.get(i);
                ttest_list.jjtAddChild(createCompleteTestPart(testCommand2, js), i);
            }
        } else {
            ttest.jjtAddChild(createTagAndNormalArgumentsForTest(testcommand, js), 0);
        }
        return ttest;
    }

    private static ASTstring_list createStringList(final List<String> arrayList, final int[] iarray) {
        int i = iarray[0];
        final ASTstring_list tstring_list = new ASTstring_list(i++);
        final int size = arrayList.size();
        // The sieve syntax forces brackets only if there are more than one
        // arguments
        if (size > 1) {
            tstring_list.setFirstToken(getDummyToken("["));
            tstring_list.setLastToken(getDummyToken("]"));
        }
        final StringBuilder builder = new StringBuilder(128).append('"');
        for (int k = 0; k < size; k++) {
            final ASTstring tstring = new ASTstring(i++);
            builder.setLength(1);
            tstring.setValue(builder.append(arrayList.get(k).replace("\\", "\\\\").replace("\"", "\\\"")).append('"').toString());
            tstring_list.jjtAddChild(tstring, k);
        }
        iarray[0] = i;
        return tstring_list;
    }

    private static ASTarguments createTagAndNormalArgumentsForTest(final TestCommand testcommand, final int[] js) {
        final ASTarguments targuments2 = new ASTarguments(js[0]++);
        final int[] p = new int[] { 0 };
        addArguments(targuments2, testcommand.getArguments(), js, p);
        return targuments2;
    }

    private static Token getDummyToken(final String string) {
        final Token token = new Token();
        token.image = string;
        token.beginColumn = 0;
        token.beginLine = 0;
        token.endColumn = 0;
        token.endLine = 0;
        return token;
    }

    private static Token getDummyToken(final String string, final int beginLine) {
        final Token token = new Token();
        token.image = string;
        token.beginColumn = 0;
        token.beginLine = beginLine;
        token.endColumn = 0;
        token.endLine = 0;
        return token;
    }

    /**
     * Converts specified rules to an appropriate {@link Node} instance.
     *
     * @param rules The rules to convert
     * @return The resulting {@link Node} instance
     */
    public static Node rulesToNodes(final ArrayList<Rule> rules) {
        final Node startnode = new ASTstart(0);
        final ASTcommands tcommands = new ASTcommands(1);
        startnode.jjtAddChild(tcommands, 0);
        // The general counter for the node id
        final int i = 2;
        // The children counter for tcommand
        int o = 0;
        for (final Rule rule : rules) {
            final ArrayList<Command> commands = rule.getCommands();
            if (null != commands) {
                for (final Command command : commands) {
                    final int[] js = new int[] { i };
                    if (command instanceof RequireCommand) {
                        final RequireCommand requirecommand = (RequireCommand) command;
                        final ASTcommand tcommand = createCommand("require", js, rule.getLinenumber());

                        final ArrayList<String> list = requirecommand.getList().get(0);
                        final ASTarguments targuments = new ASTarguments(js[0]++);
                        final ASTargument targument = new ASTargument(js[0]++);
                        tcommand.jjtAddChild(targuments, 0);
                        targuments.jjtAddChild(targument, 0);
                        targument.jjtAddChild(createStringList(list, js), 0);
                        tcommands.jjtAddChild(tcommand, o);
                    } else if (command instanceof IfOrElseIfCommand) {
                        final IfOrElseIfCommand ifcommand = (IfOrElseIfCommand) command;
                        // We need an array here, because we have to make
                        // call-by-reference through the call-by-value of java
                        ASTcommand tcommand;
                        if (command instanceof IfCommand) {
                            tcommand = createCommand("if", js, rule.getLinenumber());
                        } else {
                            tcommand = createCommand("elsif", js, rule.getLinenumber());
                        }
                        final ASTarguments arguments = new ASTarguments(js[0]++);
                        final TestCommand testcommand = ifcommand.getTestcommand();
                        final ASTtest ttest = createCompleteTestPart(testcommand, js);
                        arguments.jjtAddChild(ttest, 0);
                        tcommand.jjtAddChild(arguments, 0);

                        // ... and finally the actioncommand block
                        final ASTblock tblock = createActionBlockForTest(ifcommand.getActionCommands(), js, rule.getLinenumber());
                        tcommand.jjtAddChild(tblock, 1);
                        tcommands.jjtAddChild(tcommand, o);
                    } else if (command instanceof ElseCommand) {
                        final ElseCommand elsecommand = (ElseCommand) command;
                        // We need an array here, because we have to make
                        // call-by-reference through the call-by-value of java
                        final ASTcommand tcommand = createCommand("else", js, rule.getLinenumber());

                        final ASTarguments arguments = new ASTarguments(js[0]++);
                        tcommand.jjtAddChild(arguments, 0);

                        // ... and finally the actioncommand block
                        final ASTblock tblock = createActionBlockForTest(elsecommand.getActionCommands(), js, rule.getLinenumber());
                        tcommand.jjtAddChild(tblock, 1);
                        tcommands.jjtAddChild(tcommand, o);
                    } else if (command instanceof ActionCommand) {
                        final ActionCommand actionCommand = (ActionCommand) command;
                        // We need an array here, because we have to make
                        // call-by-reference through the call-by-value of java
                        final ASTcommand tcommand = createActionCommand(actionCommand.getArguments(), actionCommand.getCommand().getCommandName(), js, rule.getLinenumber());
                        tcommands.jjtAddChild(tcommand, o);
                    }
                    o++;
                }
            }
        }
        return startnode;
    }

}
