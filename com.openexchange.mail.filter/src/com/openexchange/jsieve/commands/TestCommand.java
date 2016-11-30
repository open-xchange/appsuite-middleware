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
import org.apache.jsieve.SieveException;
import org.apache.jsieve.TagArgument;
import com.openexchange.jsieve.commands.test.ITestCommand;

/**
 * A {@link TestCommand} is used as part of a control command. It is used to
 * specify whether or not the block of code given to the control command is
 * executed.
 *
 * General supported tests: "address", "allof", "anyof", "exists", "false",
 * "header", "not", "size", and "true"
 *
 * Need require "envelope"
 * <ul>
 * <li><code>address [ADDRESS-PART] [COMPARATOR] [MATCH-TYPE] &lt;header-list: string-list&gt; &lt;key-list: string-list&gt;</code></li>
 * <li><code>envelope [COMPARATOR] [ADDRESS-PART] [MATCH-TYPE] &lt;envelope-part: string-list&gt; &lt;key-list: string-list&gt;</code></li>
 * <li><code>[ADDRESS-PART] = ":localpart" / ":domain" / ":all"</code></li>
 * <li><code>exists &lt;header-names: string-list&gt;</code></li>
 * <li><code>false</code></li>
 * <li><code>true</code></li>
 * <li><code>not &lt;test&gt;</code></li>
 * <li><code>size &lt;":over" / ":under"&gt; &lt;limit: number&gt;</code></li>
 * <li><code>header [COMPARATOR] [MATCH-TYPE] &lt;header-names: string-list&gt; &lt;key-list: string-list&gt;</code></li>
 * <li><code>allof &lt;tests: test-list&gt;</code> (logical AND)</li>
 * <li><code>anyof &lt;tests: test-list&gt; </code> (logical OR)</code></li>
 * <li><code>Match-types are ":is", ":contains", and ":matches"</code></li>
 * </ul>
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TestCommand extends Command {

    public enum Commands implements ITestCommand {
        ADDRESS("address", 2, Integer.MAX_VALUE, standardAddressPart(), standardComparators(), standardAddressMatchTypes(), standardJSONAddressMatchTypes(), null),
        ENVELOPE("envelope", 2, Integer.MAX_VALUE, standardAddressPart(), standardComparators(), standardMatchTypes(), standardJSONMatchTypes(), "envelope"),
        //        EXITS("exists", 1, 1, null, null, null, null, null),
        FALSE("false", 0, 0, null, null, null, null, null),
        TRUE("true", 0, 0, null, null, null, null, null),
        NOT("not", 0, 0, null, null, null, null, null),
        SIZE("size", 1, 1, null, null, matchTypeSize(), standardJSONSizeMatchTypes(), null),
        HEADER("header", 2, Integer.MAX_VALUE, standardAddressPart(), standardComparators(), standardMatchTypes(), standardJSONMatchTypes(), null),
        ALLOF("allof", 0, 0, null, null, null, null, null),
        ANYOF("anyof", 0, 0, null, null, null, null, null),
        BODY("body", 1, 1, standardBodyPart(), null, standardMatchTypes(), standardJSONMatchTypes(), "body"),
        //DATE("date", 3, null, null, date_match_types(), "date"),
        CURRENTDATE("currentdate", 2, Integer.MAX_VALUE, null, null, dateMatchTypes(), dateJSONMatchTypes(), "date"),
        HASFLAG("hasflag", 1, Integer.MAX_VALUE, null, standardComparators(), standardMatchTypes(), standardJSONMatchTypes(), null);

        private static Hashtable<String, String> matchTypeSize() {
            final Hashtable<String, String> match_type_size = new Hashtable<String, String>(2);
            match_type_size.put(":over", "");
            match_type_size.put(":under", "");
            return match_type_size;
        }

        private static List<String[]> standardJSONSizeMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[] { "", "over" });
            standard_match_types.add(new String[] { "", "under" });
            return standard_match_types;
        }

        private static Hashtable<String, String> standardAddressPart() {
            final Hashtable<String, String> standard_address_part = new Hashtable<String, String>(3);
            standard_address_part.put(":localpart", "");
            standard_address_part.put(":domain", "");
            standard_address_part.put(":all", "");
            // Add further extensions here...
            return standard_address_part;
        }

        private static Hashtable<String, String> standardBodyPart() {
            final Hashtable<String, String> standard_address_part = new Hashtable<String, String>(3);
            //standard_address_part.put(":raw", "");
            standard_address_part.put(":content", "");
            standard_address_part.put(":text", "");
            // Add further extensions here...
            return standard_address_part;
        }

        private static Hashtable<String, String> standardAddressMatchTypes() {
            final Hashtable<String, String> standard_match_types = standardMatchTypes();
            standard_match_types.putAll(standardAddressPart());
            standard_match_types.put(":user", "subaddress");
            standard_match_types.put(":detail", "subaddress");
            return standard_match_types;
        }

        private static List<String[]> standardJSONAddressMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[] { "subaddress", "user" });
            standard_match_types.add(new String[] { "subaddress", "detail" });
            standard_match_types.add(new String[] { "", "all" });
            standard_match_types.add(new String[] { "", "domain" });
            standard_match_types.add(new String[] { "", "localpart" });
            return standard_match_types;
        }

        private static Hashtable<String, String> standardMatchTypes() {
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
            standard_match_types.add(new String[] { "regex", "regex" });
            standard_match_types.add(new String[] { "", "is" });
            standard_match_types.add(new String[] { "", "contains" });
            standard_match_types.add(new String[] { "", "matches" });
            return standard_match_types;
        }

        private static Hashtable<String, String> dateMatchTypes() {
            final Hashtable<String, String> standard_match_types = new Hashtable<String, String>(2);
            standard_match_types.put(":is", "");
            standard_match_types.put(":contains", "");
            standard_match_types.put(":matches", "");
            standard_match_types.put(":value", "relational");
            return standard_match_types;
        }

        private static List<String[]> dateJSONMatchTypes() {
            final List<String[]> standard_match_types = Collections.synchronizedList(new ArrayList<String[]>(2));
            standard_match_types.add(new String[] { "relational", "ge" });
            standard_match_types.add(new String[] { "relational", "le" });
            standard_match_types.add(new String[] { "", "is" });
            standard_match_types.add(new String[] { "", "contains" });
            standard_match_types.add(new String[] { "", "matches" });
            return standard_match_types;
        }

        private static Hashtable<String, String> standardComparators() {
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
        private int numberOfArguments;

        /**
         * Defines how many arguments this test can have at max
         */
        private final int maxNumberOfArguments;

        /**
         * The name of the command
         */
        private String commandName;

        /**
         * Defines if this command can take a comparator argument or not
         */
        private Hashtable<String, String> comparator;

        /**
         * Defines if this command can take a match-type argument or not
         */
        private Hashtable<String, String> matchTypes;

        /**
         * Needed for the resolution of the configuration parameters for JSON
         */
        private final List<String[]> jsonMatchTypes;

        /**
         * Defines if this command needs a require or not
         */
        private String required;

        Commands(final String commandName, final int numberOfArguments, int maxNumberOfArguments, final Hashtable<String, String> address, final Hashtable<String, String> comparator, final Hashtable<String, String> matchTypes, List<String[]> jsonMatchTypes, final String required) {
            this.commandName = commandName;
            this.numberOfArguments = numberOfArguments;
            this.maxNumberOfArguments = maxNumberOfArguments;
            this.address = address;
            this.comparator = comparator;
            this.matchTypes = matchTypes;
            this.jsonMatchTypes = jsonMatchTypes;
            this.required = required;
        }

        @Override
        public final int getNumberOfArguments() {
            return numberOfArguments;
        }

        @Override
        public final int getMaxNumberOfArguments() {
            return maxNumberOfArguments;
        }

        @Override
        public final String getCommandName() {
            return commandName;
        }

        @Override
        public final Hashtable<String, String> getAddress() {
            return address;
        }

        @Override
        public final Hashtable<String, String> getComparator() {
            return comparator;
        }

        @Override
        public final String getRequired() {
            return required;
        }

        public final void setAddress(final Hashtable<String, String> address) {
            this.address = address;
        }

        public final void setNumberOfArguments(final int arguments) {
            this.numberOfArguments = arguments;
        }

        public final void setCommandName(final String commandname) {
            this.commandName = commandname;
        }

        public final void setComparator(final Hashtable<String, String> comparator) {
            this.comparator = comparator;
        }

        public final void setRequire(final String required) {
            this.required = required;
        }

        @Override
        public final Hashtable<String, String> getMatchTypes() {
            return matchTypes;
        }

        public final void setMatchTypes(final Hashtable<String, String> matchtypes) {
            this.matchTypes = matchtypes;
        }

        @Override
        public List<String[]> getJsonMatchTypes() {
            return jsonMatchTypes;
        }
    }

    private ITestCommand command;

    private final List<String> tagArguments;

    private final List<Object> arguments;

    private final List<TestCommand> testCommands;

    private final int indexOfComparator = -1;

    /**
     *
     */
    public TestCommand() {
        super();
        this.testCommands = new ArrayList<TestCommand>();
        this.arguments = new ArrayList<Object>();
        this.tagArguments = new ArrayList<String>();
    }

    public TestCommand(final ITestCommand command, final List<Object> arguments, final List<TestCommand> testcommands) throws SieveException {
        this.command = command;
        this.tagArguments = new ArrayList<String>();
        this.arguments = arguments;
        for (final Object arg : this.arguments) {
            if (arg instanceof TagArgument) {
                final TagArgument tagarg = (TagArgument) arg;
                this.tagArguments.add(tagarg.getTag());
            }
        }
        this.testCommands = testcommands;
        checkCommand();
    }

    private void checkCommand() throws SieveException {
        if (null != this.tagArguments) {
            final ArrayList<String> tagArray = new ArrayList<String>(this.tagArguments);
            final Hashtable<String, String> matchTypes = this.command.getMatchTypes();
            if (null != matchTypes) {
                tagArray.removeAll(matchTypes.keySet());
            }
            final Hashtable<String, String> address = this.command.getAddress();
            if (null != address) {
                tagArray.removeAll(address.keySet());
            }
            if (tagArray.contains(":comparator")) {
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
            if (!tagArray.isEmpty()) {
                throw new SieveException("One of the tagArguments: " + tagArray + " is not valid for " + this.command.getCommandName());
            }
        }
        if (null != this.arguments && this.command.getNumberOfArguments() >= 0) {
            final int realArguments = this.arguments.size() - this.tagArguments.size();
            final int minArguments = this.command.getNumberOfArguments() + ((-1 != indexOfComparator) ? 1 : 0);
            final int maxArguments = this.command.getMaxNumberOfArguments();
            if (realArguments < minArguments || realArguments > maxArguments) {
                throw new SieveException("The number of arguments (" + realArguments + ") for " + this.command.getCommandName() + " is not valid.");
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
    private int searchComparator() throws SieveException {
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

    public final ITestCommand getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public final void setCommand(final Commands command) {
        this.command = command;
    }

    public final List<String> getTagArguments() {
        return tagArguments;
    }

    /**
     * @param o
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean addTagArguments(final String o) {
        return tagArguments.add(o);
    }

    public final List<Object> getArguments() {
        return arguments;
    }

    /**
     * This method returns the matchtype of this command
     *
     * @return
     */
    public final String getMatchType() {
        final ArrayList<String> arrayList = new ArrayList<String>(this.command.getMatchTypes().keySet());
        arrayList.retainAll(this.tagArguments);
        if (1 == arrayList.size()) {
            return arrayList.get(0);
        } else {
            return null;
        }
    }

    public final List<TestCommand> getTestCommands() {
        return testCommands;
    }

    public final void removeTestCommand(TestCommand command) {
        this.testCommands.remove(command);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + this.command.getCommandName() + " : " + this.tagArguments + " : " + this.arguments + " : " + this.testCommands;
    }

    @Override
    public HashSet<String> getRequired() {
        final HashSet<String> retval = new HashSet<String>();
        final String required = this.command.getRequired();
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
        for (final TestCommand command : this.getTestCommands()) {
            retval.addAll(command.getRequired());
        }
        for (final String text : this.tagArguments) {
            final String string = this.command.getMatchTypes().get(text);
            if (null != string && (0 != string.length())) {
                retval.add(string);
            }
        }
        return retval;
    }
}
