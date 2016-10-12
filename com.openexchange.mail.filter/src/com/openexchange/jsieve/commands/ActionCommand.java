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
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;

public class ActionCommand extends ControlOrActionCommand {

    /*
     * An action command is an identifier followed by zero or more arguments,
     * terminated by a semicolon. Action commands do not take tests or blocks as
     * arguments.
     *
     * "keep", "discard", and "redirect" these require a require: "reject" and
     * "fileinto"
     *
     * reject <reason: string> fileinto <folder: string> redirect <address:
     * string> keep discard
     */

    /**
     * <p>
     * Enum Arguments:
     * </p>
     * <ul>
     * <li>Command name</li>
     * <li>Minimum number of arguments</li>
     * <li>Tag arguments</li>
     * <li>JSON name</li>
     * <li>Required directive</li>
     * </ul>
     */
    public enum Commands {
        KEEP("keep", 0, new Hashtable<String, Integer>(), "keep", Collections.<String> emptyList()),
        DISCARD("discard", 0, new Hashtable<String, Integer>(), "discard", Collections.<String> emptyList()),
        REDIRECT("redirect", 1, redirectTags(), "redirect", java.util.Arrays.asList("copy")),
        FILEINTO("fileinto", 1, new Hashtable<String, Integer>(), "move", Collections.singletonList("fileinto")),
        REJECT("reject", 1, new Hashtable<String, Integer>(), "reject", Collections.singletonList("reject")),
        STOP("stop", 0, new Hashtable<String, Integer>(), "stop", Collections.<String> emptyList()),
        VACATION("vacation", 1, vacationTags(), "vacation", Collections.singletonList("vacation")),
        ENOTIFY("notify", 1, enotifyTags(), "notify", Collections.singletonList("enotify")),
        ADDFLAG("addflag", 1, new Hashtable<String, Integer>(), "addflags", java.util.Arrays.asList("imapflags", "imap4flags")),
        REMOVEFLAG("removeflag", 1, new Hashtable<String, Integer>(), "removeflags", java.util.Arrays.asList("imapflags", "imap4flags")),
        PGP_ENCRYPT("pgp_encrypt", 0, pgpEncryptTags(), "pgp", java.util.Arrays.asList("vnd.dovecot.pgp-encrypt")),
        ADDHEADER("addheader", 2, addHeaderTags(), "addheader", Collections.singletonList("editheader")),
        DELETEHEADER("deleteheader", 1, deleteHeaderTags(), "deleteheader", Collections.singletonList("editheader")),
        SET("set", 2, variablesTags(), "set", Collections.singletonList("variables"));

        private static Hashtable<String, Integer> addHeaderTags() {
            /*
             * http://tools.ietf.org/html/rfc5293
             *
             * "addheader" [":last"] <field-name: string> <value: string>
             */
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":last", Integer.valueOf(0));
            return retval;
        }

        private static Hashtable<String, Integer> deleteHeaderTags() {
            /*
             * http://tools.ietf.org/html/rfc5293
             *
             * "deleteheader" [":index" <fieldno: number> [":last"]]
             *     [COMPARATOR] [MATCH-TYPE]
             *      <field-name: string>
             *     [<value-patterns: string-list>]
             */
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            return retval;
        }

        private static Hashtable<String, Integer> variablesTags() {
            /*
             * http://tools.ietf.org/html/rfc5229
             *
             *    Usage:  ":lower" / ":upper" / ":lowerfirst" /
             *    ":upperfirst" / ":quotewildcard" / ":length"
             */
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            return retval;
        }

        private static Hashtable<String, Integer> vacationTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            // The second parameter given the number of parameters which are
            // allowed after this tag
            // The tags :handle and :mime are intentionally left out because
            // there's no way to deal with that
            // later in the frontend
            retval.put(":days", Integer.valueOf(1));
            retval.put(":addresses", Integer.valueOf(1));
            retval.put(":subject", Integer.valueOf(1));
            retval.put(":from", Integer.valueOf(1));
            return retval;
        }

        private static Hashtable<String, Integer> enotifyTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            /*
             * http://tools.ietf.org/html/rfc5435
             *
             *    Usage:  notify [":from" string]
             * [":importance" <"1" / "2" / "3">]
             * [":options" string-list]
             * [":message" string]
             * <method: string>
             *
             * only :message is supported
             *
             */
            retval.put(":message", Integer.valueOf(1));
            return retval;
        }

        private static Hashtable<String, Integer> pgpEncryptTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":keys", Integer.valueOf(1));
            return retval;
        }
        
        private static Hashtable<String, Integer> redirectTags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":copy", Integer.valueOf(0));
            return retval;
        }

        /**
         * The number of arguments which this command takes at least
         */
        private final int minNumberOfArguments;

        /**
         * The name of the command
         */
        private final String commandName;

        /**
         * Defines if this command can take a match-type argument or not
         */
        private final Hashtable<String, Integer> tagArgs;

        /**
         * Defines what must be included for this command to run
         */
        private final List<String> required;

        /**
         * Stores the name of the parameter for the json object
         */
        private final String jsonName;

        Commands(final String commandName, final int minNumberOfArguments, final Hashtable<String, Integer> tagArgs, final String jsonName, final List<String> required) {
            this.commandName = commandName;
            this.minNumberOfArguments = minNumberOfArguments;
            this.tagArgs = tagArgs;
            this.required = null == required || required.isEmpty() ? Collections.<String> emptyList() : Collections.unmodifiableList(required);
            this.jsonName = jsonName;
        }

        public final int getMinNumberOfArguments() {
            return minNumberOfArguments;
        }

        public final String getCommandName() {
            return commandName;
        }

        /**
         * @return the jsonname
         */
        public final String getJsonName() {
            return jsonName;
        }

        public final List<String> getRequired() {
            return required;
        }

        public final Hashtable<String, Integer> getTagArgs() {
            return tagArgs;
        }

    }

    private final Commands command;

    /**
     * This Hashtable contains the tagargument and the corresponding value
     */
    private final Hashtable<String, List<String>> tagArguments;

    /**
     * Provides all types of arguments. The object here can either be a
     * TagArgument, a NumberArgument or an ArrayList<String>
     */
    private final ArrayList<Object> arguments;

    public ActionCommand(final Commands command, final ArrayList<Object> arguments) throws SieveException {
        this.command = command;
        this.arguments = arguments;
        this.tagArguments = new Hashtable<String, List<String>>();
        final int size = arguments.size();
        for (int i = 0; i < size; i++) {
            final Object object = arguments.get(i);
            if (object instanceof TagArgument) {
                final TagArgument tagArg = (TagArgument) object;
                final String tag = tagArg.getTag();
                // Check if an argument is allowed for this tag
                final Hashtable<String, Integer> tagArgs = this.command.getTagArgs();
                if (null != tagArgs && tagArgs.containsKey(tag) && 0 < tagArgs.get(tag)) {
                    // Get next element check if it is a list and insert
                    final Object object2 = arguments.get(++i);
                    if (object2 instanceof List) {
                        final List<String> list = (List<String>) object2;
                        this.tagArguments.put(tag, list);
                    } else if (object2 instanceof NumberArgument) {
                        final ArrayList<String> arrayList = new ArrayList<String>();
                        arrayList.add((String.valueOf(((NumberArgument) object2).getInteger())));
                        this.tagArguments.put(tag, arrayList);
                    } else {
                        throw new SieveException("No right argument for tag " + tag + " found.");
                    }
                } else {
                    this.tagArguments.put(tag, new ArrayList<String>());
                }
            } else {
                for (String tag : command.getTagArgs().keySet()) {
                    if (command.getTagArgs().get(tag) > 0 && i == 0) {
                        throw new SieveException("The main arguments have to stand after the tag argument in the rule: " + this.toString());
                    }
                }
            }
        }
        checkCommand();
    }

    /**
     * Checks if this command has the right arguments
     *
     * @throws SieveException
     */
    private void checkCommand() throws SieveException {
        if (null != this.tagArguments) {
            // This complicated copying is needed because there's no way in java
            // to clone a set. And because the set
            // is backed up by the Hshtable we would otherwise delete the
            // elements in the Hashtable.
            final Set<String> tagArray = this.tagArguments.keySet();
            final Set<String> tagArgs = this.command.getTagArgs().keySet();
            final ArrayList<String> rest = new ArrayList<String>();
            for (final String string : tagArray) {
                if (!tagArgs.contains(string)) {
                    rest.add(string);
                }
            }
            // if (null != tagarray) {
            // tagarray.removeAll(tagargs);
            // }
            if (!rest.isEmpty()) {
                throw new SieveException("One of the tagarguments: " + rest + " is not valid for " + this.command.getCommandName());
            }
        }
        final int countTags = countTags();

        if (null != this.arguments && this.command.getMinNumberOfArguments() >= 0 && (this.arguments.size() - countTags) < this.command.getMinNumberOfArguments()) {
            throw new SieveException("The number of arguments for " + this.command.getCommandName() + " is not valid. ; " + this.toString());
        }
    }

    /**
     * This method count the arguments inside the tags
     *
     * @return
     */
    private int countTags() {
        int i = 0;
        for (final List<String> list : this.tagArguments.values()) {
            if (list.isEmpty()) {
                i++;
            } else {
                i = i + 2;
            }
        }
        return i;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " : " + this.command.getCommandName() + " : " + this.tagArguments + " : " + this.arguments;
    }

    public final Commands getCommand() {
        return command;
    }

    public final Hashtable<String, List<String>> getTagArguments() {
        return tagArguments;
    }

    /**
     * With this method you can get the argument to the given tag. E.g. for
     * <code>vacation :days 1 "Test"</code>
     *
     * The following <code>getArgumentToTag(":days");</code> returns
     * <code>["1"]</code>
     *
     *
     * @param tag
     * @return
     */
    public final List<String> getArgumentToTag(final String tag) {
        return this.tagArguments.get(tag);
    }

    public final ArrayList<Object> getArguments() {
        return arguments;
    }

    @Override
    public HashSet<String> getRequired() {
        return new HashSet<String>(this.command.getRequired());
    }
}
