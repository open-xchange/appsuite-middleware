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

    public enum Commands {
        KEEP("keep", 0, new Hashtable<String, Integer>(), "keep", Collections.<String> emptyList()),
        DISCARD("discard", 0, new Hashtable<String, Integer>(), "discard", Collections.<String> emptyList()),
        REDIRECT("redirect", 1, new Hashtable<String, Integer>(), "redirect", Collections.<String> emptyList()),
        FILEINTO("fileinto", 1, new Hashtable<String, Integer>(), "move", Collections.singletonList("fileinto")),
        REJECT("reject", 1, new Hashtable<String, Integer>(), "reject", Collections.singletonList("reject")),
        STOP("stop", 0, new Hashtable<String, Integer>(), "stop", Collections.<String> emptyList()),
        VACATION("vacation", 1, vacationtags(), "vacation", Collections.singletonList("vacation")),
        ENOTIFY("notify", 1, enotifytags(), "notify", Collections.singletonList("enotify")),
        ADDFLAG("addflag", 1, new Hashtable<String, Integer>(), "addflags", java.util.Arrays.asList("imapflags", "imap4flags")),
        PGP_ENCRYPT("pgp_encrypt", 0, pgp_encrypt_tags(), "pgp", java.util.Arrays.asList("vnd.dovecot.pgp-encrypt")),
        ADDHEADER("addheader", 0, addheadertags(), "addheader", Collections.singletonList("editheader")),
        DELETEHEADER("deleteheader", 0, deleteheadertags(), "deleteheader", Collections.singletonList("editheader")),
        SET("set", 1, new Hashtable<String, Integer>(), "set", java.util.Arrays.asList("variables"));

        private static Hashtable<String, Integer> addheadertags() {
            /*
             * http://tools.ietf.org/html/rfc5293
             *
             * "addheader" [":last"] <field-name: string> <value: string>
             */
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":last", Integer.valueOf(2));
            return retval;
        }

        private static Hashtable<String, Integer> deleteheadertags() {
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

        private static Hashtable<String, Integer> variables_tags() {
            /*
             * http://tools.ietf.org/html/rfc5229
             *
             *    Usage:  ":lower" / ":upper" / ":lowerfirst" /
             *    ":upperfirst" / ":quotewildcard" / ":length"
             */
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            return retval;
        }

        private static Hashtable<String, Integer> vacationtags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            // The second parameter given the number of parameters which are
            // allowed after this tag
            // The tags :handle and :mime are intentionally left out because
            // there's no way to deal with that
            // later in the frontend
            retval.put(":days", Integer.valueOf(1));
            retval.put(":addresses", Integer.valueOf(1));
            retval.put(":subject", Integer.valueOf(1));
            return retval;
        }

        private static Hashtable<String, Integer> enotifytags() {
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

        private static Hashtable<String, Integer> pgp_encrypt_tags() {
            final Hashtable<String, Integer> retval = new Hashtable<String, Integer>();
            retval.put(":keys", Integer.valueOf(1));
            return retval;
        }

        /**
         * The number of arguments which this command takes at least
         */
        private final int minNumberOfArguments;

        /**
         * The name of the command
         */
        private final String commandname;

        /**
         * Defines if this command can take a match-type argument or not
         */
        private final Hashtable<String, Integer> tagargs;

        /**
         * Defines what must be included for this command to run
         */
        private final List<String> required;

        /**
         * Stores the name of the parameter for the json object
         */
        private final String jsonname;

        Commands(final String commandname, final int minNumberOfArguments, final Hashtable<String, Integer> tagargs, final String jsonname, final List<String> required) {
            this.commandname = commandname;
            this.minNumberOfArguments = minNumberOfArguments;
            this.tagargs = tagargs;
            this.required = null == required || required.isEmpty() ? Collections.<String> emptyList() : Collections.unmodifiableList(required);
            this.jsonname = jsonname;
        }

        public final int getMinNumberOfArguments() {
            return minNumberOfArguments;
        }

        public final String getCommandname() {
            return commandname;
        }

        /**
         * @return the jsonname
         */
        public final String getJsonname() {
            return jsonname;
        }

        public final List<String> getRequired() {
            return required;
        }

        public final Hashtable<String, Integer> getTagargs() {
            return tagargs;
        }

    }

    private final Commands command;

    /**
     * This Hashtable contains the tagargument and the corresponding value
     */
    private final Hashtable<String, List<String>> tagarguments;

    /**
     * Provides all types of arguments. The object here can either be a
     * TagArgument, a NumberArgument or an ArrayList<String>
     */
    private final ArrayList<Object> arguments;

    public ActionCommand(final Commands command, final ArrayList<Object> arguments) throws SieveException {
        this.command = command;
        this.arguments = arguments;
        this.tagarguments = new Hashtable<String, List<String>>();
        final int size = arguments.size();
        for (int i = 0; i < size; i++) {
            final Object object = arguments.get(i);
            if (object instanceof TagArgument) {
                final TagArgument tagarg = (TagArgument) object;
                final String tag = tagarg.getTag();
                // Check if an argument is allowed for this tag
                final Hashtable<String, Integer> tagargs = this.command.getTagargs();
                if (null != tagargs && tagargs.containsKey(tag) && 0 < tagargs.get(tag)) {
                    // Get next element check if it is a list and insert
                    final Object object2 = arguments.get(++i);
                    if (object2 instanceof List) {
                        final List<String> list = (List<String>) object2;
                        this.tagarguments.put(tag, list);
                    } else if (object2 instanceof NumberArgument) {
                        final ArrayList<String> arrayList = new ArrayList<String>();
                        arrayList.add((String.valueOf(((NumberArgument) object2).getInteger())));
                        this.tagarguments.put(tag, arrayList);
                    } else {
                        throw new SieveException("No right argument for tag " + tag + " found.");
                    }
                } else {
                    this.tagarguments.put(tag, new ArrayList<String>());
                }
            } else {
                if (i != (size - 1)) {
                    throw new SieveException("The main argument has to stand at the last position in rule: " + this.toString());
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
        if (null != this.tagarguments) {
            // This complicated copying is needed because there's no way in java
            // to clone a set. And because the set
            // is backed up by the Hshtable we would otherwise delete the
            // elements in the Hashtable.
            final Set<String> tagarray = this.tagarguments.keySet();
            final Set<String> tagargs = this.command.getTagargs().keySet();
            final ArrayList<String> rest = new ArrayList<String>();
            for (final String string : tagarray) {
                if (!tagargs.contains(string)) {
                    rest.add(string);
                }
            }
            // if (null != tagarray) {
            // tagarray.removeAll(tagargs);
            // }
            if (!rest.isEmpty()) {
                throw new SieveException("One of the tagarguments: " + rest + " is not valid for " + this.command.getCommandname());
            }
        }
        final int counttags = counttags();

        if (null != this.arguments && this.command.getMinNumberOfArguments() >= 0 && (this.arguments.size() - counttags) < this.command.getMinNumberOfArguments()) {
            throw new SieveException("The number of arguments for " + this.command.getCommandname() + " is not valid. ; " + this.toString());
        }
    }

    /**
     * This method count the arguments inside the tags
     *
     * @return
     */
    private int counttags() {
        int i = 0;
        for (final List<String> list : this.tagarguments.values()) {
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
        return this.getClass().getSimpleName() + " : " + this.command.getCommandname() + " : " + this.tagarguments + " : " + this.arguments;
    }

    public final Commands getCommand() {
        return command;
    }

    public final Hashtable<String, List<String>> getTagarguments() {
        return tagarguments;
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
        return this.tagarguments.get(tag);
    }

    public final ArrayList<Object> getArguments() {
        return arguments;
    }

    @Override
    public HashSet<String> getRequired() {
        return new HashSet<String>(this.command.getRequired());
    }
}
