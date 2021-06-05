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
package com.openexchange.jsieve.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.jsieve.commands.test.IActionCommand;

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
        this.actionCommands = actionCommands;
    }

    /**
     * Gets the command of the first action command contained in action list
     *
     * @return The first command or <code>null</code> (if there is no action command available)
     */
    public IActionCommand getFirstCommand() {
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
    public void setActionCommands(List<ActionCommand> commands) {
        if (null == commands) {
            this.actionCommands = null;
        } else {
            List<ActionCommand> actionCommands = this.actionCommands;
            if (null == actionCommands) {
                this.actionCommands = new ArrayList<ActionCommand>(commands);
            } else {
                actionCommands.addAll(commands);
            }
        }
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
        this.actionCommands = actionCommands;
    }

    @Override
    public String toString() {
        List<ActionCommand> actionCommands = this.actionCommands;
        return (null == actionCommands) ? "<empty>" : actionCommands.toString();
    }

}
