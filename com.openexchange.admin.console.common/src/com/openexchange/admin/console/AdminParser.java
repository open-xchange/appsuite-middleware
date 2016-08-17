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
 *    trademarks of the OX Software GmbH group of companies.
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
package com.openexchange.admin.console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;

/**
 * This class is used to extend the CmdLineParser which two main things:
 * 1. The ability to output help texts
 * 2. The ability to have mandatory options
 */
public class AdminParser extends CLIParser {
    public enum NeededQuadState {
        notneeded,
        possibly,
        eitheror,
        needed;
    }

    private static final String OPT_HELP_LONG = "help";

    private static final char OPT_HELP_SHORT = 'h';

    private static final String OPT_CHECK_UNIQUENESS = "check";

    private static final String OPT_ENVOPTS_LONG = "environment";

    private static final String OPT_EXTENDED_LONG = "extendedoptions";

    private static final String OPT_NAME_NONEWLINE_LONG = "nonl";

    private static final String OPT_NAME_NONEWLINE_DESCRIPTION = "Remove all newlines (\\n) from output";

    private static final String OPT_RESPONSETIMEOUT_LONG = "responsetimeout";

    private final ArrayList<OptionInfo> optinfolist = new ArrayList<OptionInfo>();

    private String appname = null;

    private String usage = "";

    private CLIOption csvImportOption = null;

    final private CLIOption helpoption;

    final private CLIOption envoption;

    final CLIOption responseTimeoutOption;

    private final CLIOption checkuniquenessoption;

    private CLIOption extendedoption;

    private final CLIOption noNewlineOption;

    private boolean allowDynamic;

    private final Map<String, Map<String, String>> dynamicMaps = new HashMap<String, Map<String, String>>();

    private class OptionInfo {
        public NeededQuadState needed = NeededQuadState.notneeded;

        public CLIOption option = null;

        public String shortForm = null;

        public String longForm = null;

        public String longFormParameterDescription = null;

        public String description = null;

        public boolean extended = false;

        public boolean hidden = false;

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final char shortForm, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final char shortForm, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final String longForm, final String description, final boolean extended) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
            this.extended = extended;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final String longForm, final String description, final boolean extended, final boolean hidden) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
            this.extended = extended;
            this.hidden = hidden;
        }

        public OptionInfo(final NeededQuadState needed, final CLIOption option, final String longForm, final String longFormParameterDescription, final String description, final boolean extended) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
            this.extended = extended;
        }

    }

    public AdminParser(final String appname) {
        super();
        this.appname = appname;
        this.helpoption = this.addOption(OPT_HELP_SHORT, OPT_HELP_LONG, null, "Prints a help text", NeededQuadState.notneeded, false);
        this.envoption = this.addOption(OPT_ENVOPTS_LONG, "Output this help text", "Show info about commandline environment", false, false);
        this.checkuniquenessoption = this.addOption(OPT_CHECK_UNIQUENESS, "Checks if short parameters are unique", false, false, true);
        this.noNewlineOption = this.addOption(OPT_NAME_NONEWLINE_LONG, OPT_NAME_NONEWLINE_DESCRIPTION, false, false, false);
        this.responseTimeoutOption = this.addOption(OPT_RESPONSETIMEOUT_LONG, OPT_RESPONSETIMEOUT_LONG, "response timeout in seconds for reading response from the backend (default 0s; infinite)", false, true);
    }


    /**
     * This method is used to add an option with a mandatory field
     *
     * @param shortForm
     * @param longForm
     * @param description
     * @param needed
     * @return
     */
    public final CLIOption addOption(final char shortForm, final String longForm, final String description, final boolean needed) {
        final CLIOption retval = addStringOption(shortForm, longForm);
        this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, shortForm, longForm, description));

        return retval;
    }

    /**
     * This method is used if you want to add an option with a description for
     * the long parameter
     *
     *
     * @param shortForm
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @return
     */
    public final CLIOption addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final CLIOption retval = this.addStringOption(shortForm, longForm);
        this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, shortForm, longForm, longFormParameterDescription, description));
        return retval;
    }

    /**
     * @param shortForm
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @param hasarg
     * @return
     */
    public final CLIOption addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final NeededQuadState needed, final boolean hasarg) {
        if (hasarg) {
            final CLIOption retval = this.addStringOption(shortForm, longForm);
            this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final CLIOption retval = this.addBooleanOption(shortForm, longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, shortForm, longForm, description));
            return retval;
        }
    }

    /**
     *
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @return
     */
    public final CLIOption addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final CLIOption retval = this.addStringOption(longForm);
        this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description));
        return retval;
    }

    /**
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @param hasarg
     * @return
     */
    public final CLIOption addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg) {
        if (hasarg) {
            final CLIOption retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final CLIOption retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description));
            return retval;
        }
    }

    /**
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @param hasarg
     * @param extended
     * @return
     */
    public final CLIOption addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {
        if (hasarg) {
            final CLIOption retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final CLIOption retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended));
            return retval;
        }
    }

    /**
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @param hasarg
     * @param extended
     * @return
     */
    public final CLIOption addIntegerOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {
        if (hasarg) {
            final CLIOption retval = this.addIntegerOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final CLIOption retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended));
            return retval;
        }
    }

    /**
     * @param longForm
     * @param longFormParameterDescription
     * @param description
     * @param needed
     * @param hasarg
     * @param extended
     * @return
     */
    private final CLIOption addOption(final String longForm, final String description, final boolean needed, final boolean extended, final boolean hidden) {
        final CLIOption retval = this.addBooleanOption(longForm);
        this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended, hidden));
        return retval;
    }

    public final CLIOption addSettableBooleanOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {
        if (hasarg) {
            final CLIOption retval = this.addSettableBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final CLIOption retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended));
            return retval;
        }
    }

    public final CLIOption addOption(final char shortForm, final String longForm, final String description, final NeededQuadState needed) {
        final CLIOption retval = this.addBooleanOption(shortForm, longForm);
        this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, description));
        return retval;
    }

    public final boolean checkNoNewLine() {
        if (null != this.getOptionValue(this.noNewlineOption)) {
            return true;
        }
        return false;
    }

    public CLIOption getCsvImportOption() {
        return csvImportOption;
    }

    // As parse is declared final in CmdLineParser we cannot override it so we use another
    // function name here
    public final void ownparse(String[] args) throws CLIParseException, CLIIllegalOptionValueException, CLIUnknownOptionException, MissingOptionException {
        // First parse the whole args then get through the list an check is options that are needed
        // aren't set. By this we implement the missing feature of mandatory options
        if(allowDynamic) {
            args = extractDynamic(args);
        }
        parse(args);
        if (getRemainingArgs().length > 0) {
            throw new CLIUnknownOptionException(getRemainingArgs()[0]);
        }
        if (null != this.getOptionValue(this.checkuniquenessoption)) {
            checkOptionUniqueness();
        }
        if (null != this.getOptionValue(this.helpoption)) {
            printUsage();
            System.exit(0);
        }

        {
            String value = (String) this.getOptionValue(this.responseTimeoutOption);
            if (null != value) {
                try {
                    int responseTimeout = Integer.parseInt(value) * 1000;
                    if (responseTimeout > 0) {
                        /*
                         * The value of this property represents the length of time (in milliseconds) that the client-side Java RMI runtime will
                         * use as a socket read timeout on an established JRMP connection when reading response data for a remote method invocation.
                         * Therefore, this property can be used to impose a timeout on waiting for the results of remote invocations;
                         * if this timeout expires, the associated invocation will fail with a java.rmi.RemoteException.
                         *
                         * Setting this property should be done with due consideration, however, because it effectively places an upper bound on the
                         * allowed duration of any successful outgoing remote invocation. The maximum value is Integer.MAX_VALUE, and a value of
                         * zero indicates an infinite timeout. The default value is zero (no timeout).
                         */
                        System.setProperty("sun.rmi.transport.tcp.responseTimeout", Integer.toString(responseTimeout));
                    }
                } catch (NumberFormatException e) {
                    throw new CLIIllegalOptionValueException(responseTimeoutOption, value, e);
                }
            }
        }

        if (null != this.getOptionValue(this.envoption)) {
            printEnvUsage();
            System.exit(0);
        }
        if (null != this.extendedoption && null != this.getOptionValue(this.extendedoption)) {
            printUsageExtended();
            System.exit(0);
        }
        if (null == this.csvImportOption || null == getOptionValue(this.csvImportOption)) {
            final StringBuilder sb = new StringBuilder();
            for (final OptionInfo optInfo : this.optinfolist) {
                if (optInfo.needed == NeededQuadState.needed) {
                    if (null == getOptionValue(optInfo.option)) {
                        sb.append(optInfo.longForm);
                        sb.append(",");
                    }
                }
            }

            // show all missing opts
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                throw new MissingOptionException("Option(s) \"" + sb.toString() + "\" missing");
            }
        }
    }

    private String[] extractDynamic(String[] args) {
        List<String> staticArgs = new ArrayList<String>(args.length);
        for(String arg : args) {
            if(isExtendedOption(arg) && isDynamicOption(arg)) {
                parseDynamicOption(arg);
            } else {
                staticArgs.add(arg);
            }
        }
        return staticArgs.toArray(new String[staticArgs.size()]);
    }

    private void parseDynamicOption(String arg) {

        String namespace = null;
        String name = null;
        String value = null;

        int slashPos = arg.indexOf('/');


        if(arg.startsWith("--remove-")) {
            namespace = arg.substring(9, slashPos);
            name = arg.substring(slashPos+1);
        } else {
            int equalPos = arg.indexOf('=');

            if(slashPos == -1) {
                return;
            }

            if(equalPos == -1) {
                return;
            }

            namespace = arg.substring(2,slashPos);
            name = arg.substring(slashPos+1, equalPos);
            value = arg.substring(equalPos+1);
        }

        getDynamicMap(namespace).put(name, value);
    }


    private Map<String, String> getDynamicMap(String namespace) {
        Map<String, String> namespacedMap = dynamicMaps.get(namespace);
        if(namespacedMap == null) {
            namespacedMap = new HashMap<String, String>();
            dynamicMaps.put(namespace, namespacedMap);
        }
        return namespacedMap;
    }


    private boolean isDynamicOption(String arg) {
        int slashPos = arg.indexOf('/');
        if(slashPos == -1) {
            return false;
        }
        return slashPos < arg.indexOf('=') || arg.startsWith("--remove-");
    }


    private boolean isExtendedOption(String arg) {
        return arg.startsWith("--");
    }

    public void setCsvImportOption(CLIOption csvImportOption) {
        this.csvImportOption = csvImportOption;
    }

    public final void setExtendedOptions() {
        this.extendedoption = addOption(OPT_EXTENDED_LONG, OPT_EXTENDED_LONG, "Set this if you want to see all options, use this instead of help option", false,false);
    }

    public final void printEnvUsage() {
        System.out.println("\nThe following environment variables and their current value are known\n" +
        		"and can be modified to change behaviour:\n");
        final Hashtable<String, String> env = BasicCommandlineOptions.getEnvOptions();
        for( final Entry<String, String> entry : env.entrySet()) {
            System.out.println("\t" + entry.getKey() + "=" + entry.getValue());
        }
        System.out.println("\n");
        System.out.println("Date format is used to format all dates coming out of or going into the system.");
        System.out.println("\n");
        System.out.println("For possible date formats, see\n http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html");
        System.out.println("For possible timezones, see\n http://java.sun.com/j2se/1.5.0/docs/api/java/util/TimeZone.html");
    }

    public final void printUsage() {
        System.out.print("Usage: " + this.appname);
        System.out.println(" " + usage);

        for (final OptionInfo optInfo : this.optinfolist) {
            if (!optInfo.extended && !optInfo.hidden) {
                basicOutput(optInfo);
            }
        }
        printFinalLines();
    }

    private void printFinalLines() {
        System.out.println("\nEntries marked with an asterisk (*) are mandatory.");
        System.out.println("Entries marked with an question mark (?) are mandatory depending on your");
        System.out.println("configuration.");
        System.out.println("Entries marked with a pipe (|) are mandatory for one another which means that");
        System.out.println("at least one of them must be set.\n");
    }

    public final void printUsageExtended() {
        System.out.println("Usage: " + this.appname);
        System.out.println(" " + usage);

        for (final OptionInfo optInfo : this.optinfolist) {
            basicOutput(optInfo);
        }
        printFinalLines();
    }

    public void removeOption(final String shortForm, final String longForm) {
        final ArrayList<OptionInfo> removeList = new ArrayList<OptionInfo>();
        for (final OptionInfo optInfo : this.optinfolist) {
            if (null != shortForm && shortForm.equals(optInfo.shortForm)) {
                removeList.add(optInfo);
                this.removeOption(optInfo.option);
            } else if (null != longForm && longForm.equals(optInfo.longForm)) {
                removeList.add(optInfo);
                this.removeOption(optInfo.option);
            }
        }
        this.optinfolist.removeAll(removeList);
    }

    private final void basicOutput(final OptionInfo optInfo) {
        if (optInfo.shortForm == null) {
            final String format_this = " %s %-45s%-2s%-28s\n";
            if (null != optInfo.longFormParameterDescription) {
                final StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(optInfo.longForm);
                sb.append(" <");
                sb.append(optInfo.longFormParameterDescription);
                sb.append(">");
                final Object[] format_with_ = { "  ", sb.toString(), getrightmarker(optInfo.needed), optInfo.description };
                System.out.format(format_this, format_with_);
            } else {
                final Object[] format_with_ = { "  ", "--" + optInfo.longForm, getrightmarker(optInfo.needed), optInfo.description };
                System.out.format(format_this, format_with_);
            }
        } else {
            final String format_this = " %s,%-45s%-2s%-28s\n";
            if (null != optInfo.longFormParameterDescription) {
                // example result :
                // -c,--contextid The id of the context
                final StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(optInfo.longForm);
                sb.append(" <");
                sb.append(optInfo.longFormParameterDescription);
                sb.append(">");

                final Object[] format_with = { "-" + optInfo.shortForm, sb.toString(), getrightmarker(optInfo.needed), optInfo.description };
                System.out.format(format_this, format_with);
            } else {
                final Object[] format_with = { "-" + optInfo.shortForm, "--" + optInfo.longForm, getrightmarker(optInfo.needed), optInfo.description };
                System.out.format(format_this, format_with);
            }
        }
    }

    private NeededQuadState convertBooleantoTriState(final boolean needed) {
        if (needed) {
            return NeededQuadState.needed;
        } else {
            return NeededQuadState.notneeded;
        }
    }

    private void checkOptionUniqueness() {
        final HashSet<String> set = new HashSet<String>();
        final HashSet<String> longset = new HashSet<String>();
        for (final OptionInfo optinfo : this.optinfolist) {
            if (null != optinfo.shortForm && !set.add(optinfo.shortForm)) {
                System.err.println(this.appname + ": The option: " + optinfo.shortForm + " for "+ optinfo.description + " is duplicate");
                System.exit(1);
            }
            if (null != optinfo.longForm && !longset.add(optinfo.longForm)) {
                System.err.println(this.appname + ": The option: " + optinfo.longForm + " for "+ optinfo.description + " is duplicate");
                System.exit(1);
            }
        }
        System.exit(0);
    }

    private String getrightmarker(final NeededQuadState needed) {
        if (needed == NeededQuadState.needed) {
            return "*";
        } else if (needed == NeededQuadState.possibly) {
            return "?";
        } else if (needed == NeededQuadState.eitheror) {
            return "|";
        } else {
            return " ";
        }
    }


    public void allowDynamicOptions() {
        allowDynamic=true;
    }

    public void forbidDynamicOptions() {
        allowDynamic=false;
    }


    public Map<String, Map<String, String>> getDynamicArguments() {
        return dynamicMaps;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }
}
