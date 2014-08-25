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
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;

public class TestCommand extends Command {
    /*
     * A test command is used as part of a control command. It is used to
     * specify whether or not the block of code given to the control command is
     * executed.
     *
     * General supported tests: "address", "allof", "anyof", "exists", "false",
     * "header", "not", "size", and "true"
     *
     * Need require "envelope"
     *
     * address [ADDRESS-PART] [COMPARATOR] [MATCH-TYPE] <header-list:
     * string-list> <key-list: string-list>
     *
     * envelope [COMPARATOR] [ADDRESS-PART] [MATCH-TYPE] <envelope-part:
     * string-list> <key-list: string-list>
     *
     * [ADDRESS-PART] = ":localpart" / ":domain" / ":all"
     *
     * exists <header-names: string-list>
     *
     * false
     *
     * true
     *
     * not <test>
     *
     * size <":over" / ":under"> <limit: number>
     *
     * header [COMPARATOR] [MATCH-TYPE] <header-names: string-list> <key-list:
     * string-list>
     *
     * allof <tests: test-list> logical AND
     *
     * anyof <tests: test-list> logical OR
     *
     * Match-types are ":is", ":contains", and ":matches"
     *
     */

    // protected static class TagArgument {
    // private String tag;
    // private boolean require;
    //
    // public final String getTag() {
    // return tag;
    // }
    //
    // public final boolean isRequire() {
    // return require;
    // }
    //
    // public final void setTag(String tag) {
    // this.tag = tag;
    // }
    //
    // public final void setRequire(boolean require) {
    // this.require = require;
    // }
    //
    // public TagArgument(String tag, boolean require) {
    // super();
    // this.tag = tag;
    // this.require = require;
    // }
    //
    // @Override
    // public boolean equals(Object obj) {
    // if (obj instanceof String) {
    // final String text = (String) obj;
    // return this.tag.equals(text);
    // } else if (obj instanceof TagArgument) {
    // final TagArgument tagArgument = (TagArgument) obj;
    // return tagArgument.tag.equals(tagArgument.tag) && (tagArgument.require ==
    // tagArgument.require);
    // }
    // return false;
    // }
    // }
    public enum Commands {
        ADDRESS("address", 2, Integer.MAX_VALUE, standard_address_part(), standard_comparators(), standard_address_match_types(), standardJSONAddressMatchTypes(), null),
        ENVELOPE("envelope", 2, Integer.MAX_VALUE, standard_address_part(), standard_comparators(), standard_match_types(), standardJSONMatchTypes(), "envelope"),
        EXITS("exists", 1, 1, null, null, null, null, null),
        FALSE("false", 0, 0, null, null, null, null, null),
        TRUE("true", 0, 0, null, null, null, null, null),
        NOT("not", 0, 0, null, null, null, null, null),
        SIZE("size", 1, 1, null, null, match_type_size(), standardJSONSizeMatchTypes(), null),
        HEADER("header", 2, Integer.MAX_VALUE, standard_address_part(), standard_comparators(), standard_match_types(), standardJSONMatchTypes(), null),
        ALLOF("allof", 0, 0, null, null, null, null, null),
        ANYOF("anyof", 0, 0, null, null, null, null, null),
        BODY("body", 1, 1, standard_body_part(), null, standard_match_types(), standardJSONMatchTypes(), "body"),
        //DATE("date", 3, null, null, date_match_types(), "date"),
        CURRENTDATE("currentdate", 2, Integer.MAX_VALUE, null, null, date_match_types(), dateJSONMatchTypes(), "date");

        private static Hashtable<String, String> match_type_size() {
            final Hashtable<String, String> match_type_size = new Hashtable<String, String>(2);
            match_type_size.put(":over", "");
            match_type_size.put(":under", "");
            return match_type_size;
        }

        private static List<String[]> standardJSONSizeMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[]{"", "over"});
            standard_match_types.add(new String[]{"", "under"});
            return standard_match_types;
        }

        private static Hashtable<String, String> standard_address_part() {
            final Hashtable<String, String> standard_address_part = new Hashtable<String, String>(3);
            standard_address_part.put(":localpart", "");
            standard_address_part.put(":domain", "");
            standard_address_part.put(":all", "");
            // Add further extensions here...
            return standard_address_part;
        }

        private static Hashtable<String, String> standard_body_part() {
            final Hashtable<String, String> standard_address_part = new Hashtable<String, String>(3);
            //standard_address_part.put(":raw", "");
            standard_address_part.put(":content", "");
            standard_address_part.put(":text", "");
            // Add further extensions here...
            return standard_address_part;
        }

        private static Hashtable<String, String> standard_address_match_types() {
            final Hashtable<String, String> standard_match_types = standard_match_types();
            standard_match_types.put(":user", "subaddress");
            standard_match_types.put(":detail", "subaddress");
            return standard_match_types;
        }

        private static List<String[]> standardJSONAddressMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[]{"subaddress", "user"});
            standard_match_types.add(new String[]{"subaddress", "detail"});
            return standard_match_types;
        }

        private static Hashtable<String, String> standard_match_types() {
            final Hashtable<String, String> standard_match_types = new Hashtable<String, String>(4);
            standard_match_types.put(":is", "");
            standard_match_types.put(":contains", "");
            standard_match_types.put(":matches", "");
            // Add further extensions here... and don't forget to raise the
            // initial number
            standard_match_types.put(":regex", "regex");
            return standard_match_types;
        }

        private static List<String[]> standardJSONMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(4));
            standard_match_types.add(new String[]{"regex", "regex"});
            standard_match_types.add(new String[]{"", "is"});
            standard_match_types.add(new String[]{"", "contains"});
            standard_match_types.add(new String[]{"", "matches"});
            return standard_match_types;
        }

        private static Hashtable<String, String> date_match_types() {
            final Hashtable<String, String> standard_match_types = new Hashtable<String, String>(2);
            standard_match_types.put(":is", "");
            standard_match_types.put(":contains", "");
            standard_match_types.put(":matches", "");
            standard_match_types.put(":value", "relational");
            return standard_match_types;
        }

        private static List<String[]> dateJSONMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[]{"relational", "ge"});
            standard_match_types.add(new String[]{"relational", "le"});
            standard_match_types.add(new String[]{"", "is"});
            standard_match_types.add(new String[]{"", "contains"});
            standard_match_types.add(new String[]{"", "matches"});
            return standard_match_types;
        }

        private static Hashtable<String, String> standard_comparators() {
            final Hashtable<String, String> standard_comparators = new Hashtable<String, String>(2);
            standard_comparators.put("i;ascii-casemap", "");
            standard_comparators.put("i;octet", "");
            // Add further extensions to comparator here
            // e.g. standard_comparators.put("\"i;test\"",
            // "\"comparator-test\"");
            return standard_comparators;
        }

        /**
         * Defines if this command can take a address argument or not
         */
        private Hashtable<String, String> address;

        /**
         * The number of arguments which this command takes
         */
        private int numberofarguments;

        /**
         * Defines how many arguments this test can have at max
         */
        private final int maxNumberOfArguments;

        /**
         * The name of the command
         */
        private String commandname;

        /**
         * Defines if this command can take a comparator argument or not
         */
        private Hashtable<String, String> comparator;

        /**
         * Defines if this command can take a match-type argument or not
         */
        private Hashtable<String, String> matchtypes;

        /**
         * Needed for the resolution of the configuration parameters for JSON
         */
        private final List<String[]> jsonMatchTypes;

        /**
         * Defines if this command needs a require or not
         */
        private String required;



        Commands(final String commandname, final int numberofarguments, int maxNumberOfArguments, final Hashtable<String, String> address, final Hashtable<String, String> comparator, final Hashtable<String, String> matchtypes, List<String[]> jsonMatchTypes, final String required) {
            this.commandname = commandname;
            this.numberofarguments = numberofarguments;
            this.maxNumberOfArguments = maxNumberOfArguments;
            this.address = address;
            this.comparator = comparator;
            this.matchtypes = matchtypes;
            this.jsonMatchTypes = jsonMatchTypes;
            this.required = required;
        }

        public final int getNumberofarguments() {
            return numberofarguments;
        }

        public final int getMaxNumberOfArguments() {
            return maxNumberOfArguments;
        }

        public final String getCommandname() {
            return commandname;
        }

        public final Hashtable<String, String> getAddress() {
            return address;
        }

        public final Hashtable<String, String> getComparator() {
            return comparator;
        }

        public final String getRequired() {
            return required;
        }

        public final void setAddress(final Hashtable<String, String> address) {
            this.address = address;
        }

        public final void setNumberofarguments(final int arguments) {
            this.numberofarguments = arguments;
        }

        public final void setCommandname(final String commandname) {
            this.commandname = commandname;
        }

        public final void setComparator(final Hashtable<String, String> comparator) {
            this.comparator = comparator;
        }

        public final void setRequire(final String required) {
            this.required = required;
        }

        public final Hashtable<String, String> getMatchtypes() {
            return matchtypes;
        }

        public final void setMatchtypes(final Hashtable<String, String> matchtypes) {
            this.matchtypes = matchtypes;
        }

        public List<String[]> getJsonMatchTypes() {
            return jsonMatchTypes;
        }
    }

    private Commands command;

    private final List<String> tagarguments;

    private final List<Object> arguments;

    private final List<TestCommand> testcommands;

    private final int indexOfComparator = -1;


    /**
     *
     */
    public TestCommand() {
        super();
        this.testcommands = new ArrayList<TestCommand>();
        this.arguments = new ArrayList<Object>();
        this.tagarguments = new ArrayList<String>();
    }

    public TestCommand(final Commands command, final List<Object> arguments, final List<TestCommand> testcommands) throws SieveException {
        this.command = command;
        this.tagarguments = new ArrayList<String>();
        this.arguments = arguments;
        for (final Object arg : this.arguments) {
            if (arg instanceof TagArgument) {
                final TagArgument tagarg = (TagArgument) arg;
                this.tagarguments.add(tagarg.getTag());
            }
        }
        this.testcommands = testcommands;
        checkCommand();
    }

    private void checkCommand() throws SieveException {
        if (null != this.tagarguments) {
            final ArrayList<String> tagarray = new ArrayList<String>(this.tagarguments);
            final Hashtable<String, String> matchtypes = this.command.getMatchtypes();
            if (null != matchtypes) {
                tagarray.removeAll(matchtypes.keySet());
            }
            final Hashtable<String, String> address = this.command.getAddress();
            if (null != address) {
                tagarray.removeAll(address.keySet());
            }
            if (tagarray.contains(":comparator")) {
                throw new SieveException("Sieve comparators aren't supported by this implementation");
            }
//            final Hashtable<String, String> comparator = this.command.getComparator();
//            if (null != comparator) {
//                final boolean comparatorrule = tagarray.remove(":comparator");
//                if (comparatorrule) {
//                    // The argument of the comparator is located one after the
//                    // comparator tag itself
//                    indexOfComparator = searchcomparator() + 1;
//                    final Object object = this.arguments.get(indexOfComparator);
//                    if (object instanceof ArrayList) {
//                        final ArrayList<String> new_name = (ArrayList<String>) object;
//                        final String comparatorarg = new_name.get(0);
//                        if (!comparator.containsKey(comparatorarg)) {
//                            throw new SieveException(comparatorarg + " is no valid comparator for " + this.command.getCommandname());
//                        }
//                    } else {
//                        throw new SieveException(object + " is no valid comparator for " + this.command.getCommandname());
//                    }
//                }
//            }
            if (!tagarray.isEmpty()) {
                throw new SieveException("One of the tagarguments: " + tagarray + " is not valid for " + this.command.getCommandname());
            }
        }
        if (null != this.arguments && this.command.getNumberofarguments() >= 0) {
            final int realArguments = this.arguments.size() - this.tagarguments.size();
            final int minArguments = this.command.getNumberofarguments() + ((-1 != indexOfComparator) ? 1 : 0);
            final int maxArguments = this.command.getMaxNumberOfArguments();
            if (realArguments < minArguments || realArguments > maxArguments) {
                throw new SieveException("The number of arguments (" + realArguments + ") for " + this.command.getCommandname() + " is not valid.");
            }
        }
        // Add test for testcommands here only anyof and allof are allowed to
        // take further tests
    }

    /**
     * This method searches for the comparator tag in the array and returns its
     * position. This method must find the right tag, otherwise this is an
     * error. So an exception is thrown here if the comparator tag isn't found
     *
     * @return
     * @throws SieveException
     */
    private int searchcomparator() throws SieveException {
        for (int i = 0; i < this.arguments.size(); i++) {
            final Object obj = this.arguments.get(i);
            if (obj instanceof TagArgument) {
                final TagArgument tag = (TagArgument) obj;
                if (":comparator".equals(tag.getTag())) {
                    return i;
                }
            }
        }
        throw new SieveException("An error occured while search the comparator tag in the arguments");
    }

    public final Commands getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public final void setCommand(final Commands command) {
        this.command = command;
    }

    public final List<String> getTagarguments() {
        return tagarguments;
    }

    /**
     * @param o
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean addTagarguments(final String o) {
        return tagarguments.add(o);
    }

    public final List<Object> getArguments() {
        return arguments;
    }

    /**
     * This method returns the matchtype of this command
     *
     * @return
     */
    public final String getMatchtype() {
        final ArrayList<String> arrayList = new ArrayList<String>(this.command.getMatchtypes().keySet());
        arrayList.retainAll(this.tagarguments);
        if (1 == arrayList.size()) {
            return arrayList.get(0);
        } else {
            return null;
        }
    }

    public final List<TestCommand> getTestcommands() {
        return testcommands;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + this.command.getCommandname() + " : " + this.tagarguments + " : " + this.arguments + " : " + this.testcommands;
    }

    @Override
    public HashSet<String> getRequired() {
        final HashSet<String> retval = new HashSet<String>();
        final String required = this.command.required;
        if (null != required) {
            retval.add(required);
        }
        // Here we add require for the comparator rule if there are any
        if (-1 != indexOfComparator) {
            final ArrayList<String> object = (ArrayList<String>) this.arguments.get(indexOfComparator);
            final String string = this.command.getComparator().get(object.get(0));
            if (null != string && !string.equals("")) {
                retval.add(string);
            }
        }
        for (final TestCommand command : this.getTestcommands()) {
            retval.addAll(command.getRequired());
        }
        for (final String text : this.tagarguments) {
            final String string = this.command.matchtypes.get(text);
            if (null != string && (0 != string.length())) {
                retval.add(string);
            }
        }
        return retval;
    }
}
