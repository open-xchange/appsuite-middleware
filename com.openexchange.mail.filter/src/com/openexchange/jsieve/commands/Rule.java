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
package com.openexchange.jsieve.commands;

import java.util.ArrayList;

// Comparable<Rule> allows only a Rule object for compare
public class Rule implements Comparable<Rule> {

    private RuleComment ruleComment;

    private boolean commented;

    private int linenumber = -1;

    private int endlinenumber = -1;

    private int position = -1;

    private String text;

    private String errormsg;

    private ArrayList<Command> commands;

    public Rule() {
        this.commands = new ArrayList<Command>();
    }

    public Rule(final RuleComment name, final ArrayList<Command> commands) {
        super();
        this.ruleComment = name;
        this.commands = commands;
    }

    public Rule(final ArrayList<Command> commands, final int linenumber) {
        super();
        this.commands = commands;
        this.linenumber = linenumber;
    }

    public Rule(final ArrayList<Command> commands, final int linenumber, final int endlinenumber, final boolean commented) {
        super();
        this.commands = commands;
        this.linenumber = linenumber;
        this.endlinenumber = endlinenumber;
        this.commented = commented;
    }

    /**
     * @param commented
     * @param linenumber
     * @param text
     * @param errormsg
     */
    public Rule(final boolean commented, final int linenumber, final int endlinenumber, final String errormsg) {
        super();
        this.commented = commented;
        this.linenumber = linenumber;
        this.endlinenumber = endlinenumber;
        this.errormsg = errormsg;
    }

    public Rule(final RuleComment name, final ArrayList<Command> command, final boolean commented) {
        super();
        this.ruleComment = name;
        this.commands = command;
        this.commented = commented;
    }

    public Rule(final RuleComment name, final ArrayList<Command> command, final int linenumber, final boolean commented) {
        super();
        this.ruleComment = name;
        this.commands = command;
        this.linenumber = linenumber;
        this.commented = commented;
    }

    public final RuleComment getRuleComment() {
        return ruleComment;
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    public boolean addCommand(final Command o) {
        return commands.add(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.ArrayList#remove(java.lang.Object)
     */
    public boolean removeCommand(final Object o) {
        return commands.remove(o);
    }

    public final ArrayList<Command> getCommands() {
        if(commands == null) {
            commands = new ArrayList<Command>();
        }
        return commands;
    }

    /**
     * A convenience method to get the require command if one is contained
     * @return the require command or null if none is contained
     */
    public final RequireCommand getRequireCommand() {
        // If a require command is contained here it is located at the first position
        if (null == this.commands) {
            return null;
        }
        if (!this.commands.isEmpty()) {
            final Command command = this.commands.get(0);
            if (command instanceof RequireCommand) {
                final RequireCommand requirecmd = (RequireCommand) command;
                return requirecmd;
            }
        }
        return null;
    }

    /**
     * A convenience method to get the if command if one is contained
     * @return the if command or null if none is contained
     */
    public final IfCommand getIfCommand() {
        if (null == this.commands) {
            return null;
        }
        for (final Command command : this.commands) {
            if (command instanceof IfCommand) {
                final IfCommand ifcommand = (IfCommand) command;
                return ifcommand;
            }
        }
        return null;
    }

    /**
     * A convenience method to get the test command if one is contained
     * @return the test command or null if none is contained
     */
    public final TestCommand getTestCommand() {
        final IfCommand ifCommand = getIfCommand();
        if (null == ifCommand) {
            return null;
        }

        return ifCommand.getTestcommand();
    }

    /**
     * A convenience method for directly accessing the unique id
     * @return -1 if there is no unique id for this rule; a value > -1 otherwise
     */
    public int getUniqueId() {
        if (null == this.ruleComment) {
            return -1;
        }

        return this.ruleComment.getUniqueid();
    }

    public final void setRuleComments(final RuleComment ruleComment) {
        this.ruleComment = ruleComment;
    }

    public final void setCommands(final ArrayList<Command> commands) {
        this.commands = commands;
    }

    public final int getLinenumber() {
        return linenumber;
    }

    public final void setLinenumber(final int linenumber) {
        this.linenumber = linenumber;
    }

    /**
     * @return the position
     */
    public final int getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public final void setPosition(final int position) {
        this.position = position;
    }

    public final boolean isCommented() {
        return commented;
    }

    public final void setCommented(final boolean commented) {
        this.commented = commented;
    }

    /**
     * @return the text
     */
    public final String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public final void setText(final String text) {
        this.text = text;
    }

    /**
     * @return the errormsg
     */
    public final String getErrormsg() {
        return errormsg;
    }

    /**
     * @param errormsg the errormsg to set
     */
    public final void setErrormsg(final String errormsg) {
        this.errormsg = errormsg;
    }

    /**
     * @return the endlinenumber
     */
    public final int getEndlinenumber() {
        return endlinenumber;
    }

    /**
     * @param endlinenumber the endlinenumber to set
     */
    public final void setEndlinenumber(final int endlinenumber) {
        this.endlinenumber = endlinenumber;
    }

    @Override
    public String toString() {
        return "Name: " + ((null != this.ruleComment && null != this.ruleComment.getRulename()) ? this.ruleComment.getRulename() : null) + ": " + this.commands;
    }

    @Override
    public int compareTo(final Rule o) {
        return Integer.valueOf(this.linenumber).compareTo(Integer.valueOf(o.linenumber));
    }

}
