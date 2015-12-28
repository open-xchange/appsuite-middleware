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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.jsieve.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.jsieve.commands.ActionCommand.Commands;

/**
 * {@link IfStructureCommand}
 */
public abstract class IfStructureCommand extends ControlCommand {

    private List<ActionCommand> actionCommands;

    /**
     * Initializes a new {@link IfStructureCommand}.
     */
    protected IfStructureCommand() {
        super();
    }

    /**
     * Initializes a new {@link IfStructureCommand}.
     *
     * @param actionCommands The initial action commands
     */
    protected IfStructureCommand(final List<ActionCommand> actionCommands) {
        super();
        this.actionCommands = sort(actionCommands);
    }

    /**
     * Gets the command of the first action command contained in action list
     *
     * @return The first command or <code>null</code> (if there is no action command available)
     */
    public Commands getFirstCommand() {
        final List<ActionCommand> thisActionCommands = this.actionCommands;
        return null == thisActionCommands ? null : (thisActionCommands.isEmpty() ? null : thisActionCommands.get(0).getCommand());
    }

    /**
     * Gets the action commands associated with this <code>"if"</code> command.
     *
     * @return The list of action commands; never <code>null</code>
     */
    public List<ActionCommand> getActionCommands() {
        List<ActionCommand> thisActionCommands = this.actionCommands;
        return null == thisActionCommands ? Collections.<ActionCommand> emptyList() : thisActionCommands;
    }

    /**
     * Sets the action commands for this <code>"if"</code> command (replacing any existing commands).
     *
     * @param commands The action commands to set
     */
    public void setActionCommands(final List<ActionCommand> commands) {
        this.actionCommands = sort(commands);
    }

    /**
     * Adds specified action command to this <code>"if"</code> command.
     *
     * @param command The action command to add
     */
    public void addActionCommand(final ActionCommand command) {
        List<ActionCommand> actionCommands = this.actionCommands;
        if (null == actionCommands) {
            actionCommands = new ArrayList<ActionCommand>();
            this.actionCommands = actionCommands;
        }
        actionCommands.add(command);
        this.actionCommands = sort(actionCommands);
    }

    @Override
    public String toString() {
        List<ActionCommand> actionCommands = this.actionCommands;
        return (null == actionCommands) ? "<empty>" : actionCommands.toString();
    }

    // ---------------------------------------------------------------------------------------------------------------------

    /**
     * Sorts specified action commands associated with an <tt>"if"</tt> command.
     * <p>
     * Ensures a <tt>"fileinto"</tt> happens after any message-modifying command (e.g. <tt>"addflag"</tt>, <tt>"addheader"</tt>, <tt>"deleteheader"</tt>)
     * while trying to maintain the order of other commands.
     *
     * @param actionCommands The action commands to sort
     * @return The sorted action commands
     */
    protected static List<ActionCommand> sort(List<ActionCommand> actionCommands) {
        if (null == actionCommands) {
            return actionCommands;
        }

        int size = actionCommands.size();
        if (size <= 1) {
            return actionCommands;
        }

        int msize = size - 1;
        boolean keepOn = true;
        int start = 0;
        while (keepOn) {
            keepOn = false;

            for (int i = start; i <= msize; i++) {
                ActionCommand actionCommand1 = actionCommands.get(i);
                if (isFileInto(actionCommand1)) {
                    if (i < msize) {
                        int swap = -1;
                        for (int j = i+1; j <= msize; j++) {
                            ActionCommand actionCommand2 = actionCommands.get(j);
                            if (isMessageOp(actionCommand2)) {
                                swap = j;
                                j = size;
                            }
                        }

                        if (swap >= 0) {
                            actionCommands.add(i, actionCommands.remove(swap));
                            keepOn = true;
                            start = i + 1;
                            i = size;
                        }
                    }
                }
            }
        }

        return actionCommands;
    }

    private static boolean isFileInto(ActionCommand actionCommand) {
        return ActionCommand.Commands.FILEINTO.equals(actionCommand.getCommand());
    }

    private static final EnumSet<ActionCommand.Commands> MESSAGE_OPS = EnumSet.of(ActionCommand.Commands.ADDFLAG, ActionCommand.Commands.ADDHEADER, ActionCommand.Commands.DELETEHEADER);

    private static boolean isMessageOp(ActionCommand actionCommand) {
        return MESSAGE_OPS.contains(actionCommand.getCommand());
    }

}
