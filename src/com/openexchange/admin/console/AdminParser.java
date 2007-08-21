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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.HashSet;
import java.util.Hashtable;

import com.openexchange.admin.rmi.exceptions.MissingOptionException;

/**
 * This class is used to extend the CmdLineParser which two main things:
 * 1. The ability to output help texts
 * 2. The ability to have mandatory options
 */
public class AdminParser extends CmdLineParser {
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
    
    private ArrayList<OptionInfo> optinfolist = new ArrayList<OptionInfo>();
    
    private String appname = null;
    
    final private Option helpoption;
    
    final private Option envoption;
    
    private Option checkuniquenessoption;
    
    private Option extendedoption;
    
    private class OptionInfo {
        public NeededQuadState needed = NeededQuadState.notneeded;

        public Option option = null;

        public String shortForm = null;

        public String longForm = null;

        public String longFormParameterDescription = null;

        public String description = null;

        public boolean extended = false;

        public boolean hidden = false;

        public OptionInfo(final NeededQuadState needed, final Option option, final char shortForm, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final char shortForm, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final String longForm, final String description, final boolean extended) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
            this.extended = extended;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final String longForm, final String description, final boolean extended, final boolean hidden) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
            this.extended = extended;
            this.hidden = hidden;
        }

        public OptionInfo(final NeededQuadState needed, final Option option, final String longForm, final String longFormParameterDescription, final String description, final boolean extended) {
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
    public final Option addOption(final char shortForm, final String longForm, final String description, final boolean needed) {
        final Option retval = addStringOption(shortForm, longForm);
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
    public final Option addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final Option retval = this.addStringOption(shortForm, longForm);
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
    public final Option addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final NeededQuadState needed, final boolean hasarg) {
        if (hasarg) {
            final Option retval = this.addStringOption(shortForm, longForm);
            this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(shortForm, longForm);
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
    public final Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final Option retval = this.addStringOption(longForm);
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
    public final Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg) {
        if (hasarg) {
            final Option retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(longForm);
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
    public final Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {
        if (hasarg) {
            final Option retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(longForm);
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
    private final Option addOption(final String longForm, final String description, final boolean needed, final boolean extended, final boolean hidden) {
        final Option retval = this.addBooleanOption(longForm);
        this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended, hidden));
        return retval;
    }
    
    public final Option addSettableBooleanOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {
        if (hasarg) {
            final Option retval = this.addSettableBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(convertBooleantoTriState(needed), retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(NeededQuadState.notneeded, retval, longForm, description, extended));
            return retval;
        }
    }


    // As parse is declared final in CmdLineParser we cannot override it so we use another
    // function name here
    public final void ownparse(final String[] args) throws IllegalOptionValueException, UnknownOptionException, MissingOptionException {
        // First parse the whole args then get through the list an check is options that are needed
        // aren't set. By this we implement the missing feature of mandatory options
        parse(args);
        if (null != this.getOptionValue(this.checkuniquenessoption)) {
            checkOptionUniqueness();
        }
        if (null != this.getOptionValue(this.helpoption)) {
            printUsage();
            System.exit(0);
        }
        if (null != this.getOptionValue(this.envoption)) {
            printEnvUsage();
            System.exit(0);
        }
        if (null != this.extendedoption && null != this.getOptionValue(this.extendedoption)) {
            printUsageExtended();
            System.exit(0);
        }
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
        if (sb.toString().length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            throw new MissingOptionException("Option(s) \"" + sb.toString() + "\" missing");
        }
    }

    public final void setExtendedOptions() {
        this.extendedoption = addOption(OPT_EXTENDED_LONG, OPT_EXTENDED_LONG, "Set this if you want to see all options, use this instead of help option", false,false);
    }
    
    public final void printEnvUsage() {
        System.out.println("\nThe following environment variables and their current value are known\n" +
        		"and can be modified to change behaviour:\n");
        Hashtable<String, String> env = BasicCommandlineOptions.getEnvOptions();
        for( final String key : env.keySet()) {
            System.out.println("\t" + key + "=" + env.get(key));
        }
        System.out.println("\n");
        System.out.println("Date format is used to format all dates coming out of or going into the system.");
        System.out.println("\n");
        System.out.println("For possible date formats, see\n http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html");
        System.out.println("For possible timezones, see\n http://java.sun.com/j2se/1.5.0/docs/api/java/util/TimeZone.html");
    }
    
    public final void printUsage() {
        System.err.println("Usage: " + this.appname);

        for (final OptionInfo optInfo : this.optinfolist) {
            if (!optInfo.extended && !optInfo.hidden) {
                basicOutput(optInfo);
            }
        }
        printFinalLines();
    }

    private void printFinalLines() {
        System.err.println("\nEntries marked with an asterisk (*) are mandatory.");
        System.err.println("Entries marked with an question mark (?) are mandatory depending on your");
        System.err.println("configuration.");
        System.err.println("Entries marked with a pipe (|) are mandatory for one another which means that");
        System.err.println("at least one of them must be set.\n");
    }

    public final void printUsageExtended() {
        System.err.println("Usage: " + this.appname);
        
        for (final OptionInfo optInfo : this.optinfolist) {
            basicOutput(optInfo);
        }
        printFinalLines();
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
                System.err.format(format_this, format_with_);
            } else {
                final Object[] format_with_ = { "  ", "--" + optInfo.longForm, getrightmarker(optInfo.needed), optInfo.description };
                System.err.format(format_this, format_with_);
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
                System.err.format(format_this, format_with);
            } else {
                final Object[] format_with = { "-" + optInfo.shortForm, "--" + optInfo.longForm, getrightmarker(optInfo.needed), optInfo.description };
                System.err.format(format_this, format_with);
            }
        }
    }

    private NeededQuadState convertBooleantoTriState(boolean needed) {
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
}
