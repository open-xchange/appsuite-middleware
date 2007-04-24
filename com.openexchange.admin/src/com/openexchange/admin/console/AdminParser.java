package com.openexchange.admin.console;

import java.util.ArrayList;

/**
 * This class is used to extend the CmdLineParser which two main things:
 * 1. The ability to output help texts
 * 2. The ability to have mandatory options
 */
public class AdminParser extends CmdLineParser {
    private static final String OPT_HELP_LONG = "help";

    private static final char OPT_HELP_SHORT = 'h';

    public class MissingOptionException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 8134438398224308263L;

        MissingOptionException(final String msg) {
            super(msg);
        }
    }

    private class OptionInfo {
        public boolean needed = false;

        public Option option = null;

        public String shortForm = null;

        public String longForm = null;

        public String longFormParameterDescription = null;

        public String description = null;

        public boolean extended = false;

        public OptionInfo(final boolean needed, final Option option, final char shortForm, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final boolean needed, final Option option, final String longForm, final String longFormParameterDescription, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.longFormParameterDescription = longFormParameterDescription;
            this.description = description;
        }

        public OptionInfo(final boolean needed, final Option option, final char shortForm, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.shortForm = String.valueOf(shortForm);
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final boolean needed, final Option option, final String longForm, final String description) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
        }

        public OptionInfo(final boolean needed, final Option option, final String longForm, final String description, final boolean extended) {
            super();
            this.needed = needed;
            this.option = option;
            this.longForm = longForm;
            this.description = description;
            this.extended = extended;
        }

        public OptionInfo(final boolean needed, final Option option, final String longForm, final String longFormParameterDescription, final String description, final boolean extended) {
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
        this.helpoption = this.addOption(OPT_HELP_SHORT, OPT_HELP_LONG, null, "Output this help text", false, false);
    }

    ArrayList<OptionInfo> optinfolist = new ArrayList<OptionInfo>();

    private String appname = null;

    private Option helpoption;

    /**
     * This method is used to add an option with a mandatory field
     * 
     * @param shortForm
     * @param longForm
     * @param description
     * @param needed
     * @return
     */
    public Option addOption(final char shortForm, final String longForm, final String description, final boolean needed) {
        final Option retval = addStringOption(shortForm, longForm);
        this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, description));

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
    public Option addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final Option retval = this.addStringOption(shortForm, longForm);
        this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, longFormParameterDescription, description));
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
    public Option addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg) {
        if (hasarg) {
            final Option retval = this.addStringOption(shortForm, longForm);
            this.optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(shortForm, longForm);
            this.optinfolist.add(new OptionInfo(false, retval, shortForm, longForm, description));
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
    public Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed) {
        final Option retval = this.addStringOption(longForm);
        this.optinfolist.add(new OptionInfo(needed, retval, longForm, longFormParameterDescription, description));
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
    public Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg) {

        if (hasarg) {
            final Option retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(needed, retval, longForm, longFormParameterDescription, description));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(false, retval, longForm, description));
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
    public Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed, final boolean hasarg, final boolean extended) {

        if (hasarg) {
            final Option retval = this.addStringOption(longForm);
            this.optinfolist.add(new OptionInfo(needed, retval, longForm, longFormParameterDescription, description, extended));
            return retval;
        } else {
            final Option retval = this.addBooleanOption(longForm);
            this.optinfolist.add(new OptionInfo(false, retval, longForm, description, extended));
            return retval;
        }

    }


    // As parse is declared final in CmdLineParser we cannot override it so we use another
    // function name here
    public void ownparse(final String[] args) throws IllegalOptionValueException, UnknownOptionException, MissingOptionException {
        // First parse the whole args then get through the list an check is options that are needed
        // aren't set. By this we implement the missing feature of mandatory options
        parse(args);
        if (null != this.getOptionValue(this.helpoption)) {
            printUsage();
            System.exit(0);
        }
        final StringBuilder sb = new StringBuilder();
        for (final OptionInfo optInfo : this.optinfolist) {
            if (optInfo.needed) {
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

    public void printUsage() {
        System.err.println("Usage: " + this.appname);

        for (final OptionInfo optInfo : this.optinfolist) {
            if (!optInfo.extended) {
                basicOutput(optInfo);
            }
        }
    }

    public void printUsageExtended() {
        System.err.println("Usage: " + this.appname);
        
        for (final OptionInfo optInfo : this.optinfolist) {
            basicOutput(optInfo);
        }
    }

    private void basicOutput(final OptionInfo optInfo) {
        if (optInfo.shortForm == null) {
            final String format_this = " %s %-46s %-28s\n";
            if (null != optInfo.longFormParameterDescription) {
                final StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(optInfo.longForm);
                sb.append(" <");
                sb.append(optInfo.longFormParameterDescription);
                sb.append(">");
                final Object[] format_with_ = { "  ", sb.toString(), optInfo.description };
                System.err.format(format_this, format_with_);
            } else {
                final Object[] format_with_ = { "  ", "--" + optInfo.longForm, optInfo.description };
                System.err.format(format_this, format_with_);
            }
        } else {
            final String format_this = " %s,%-46s %-28s\n";
            if (null != optInfo.longFormParameterDescription) {
                // example result :
                // -c,--contextid The id of the context
                final StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(optInfo.longForm);
                sb.append(" <");
                sb.append(optInfo.longFormParameterDescription);
                sb.append(">");

                final Object[] format_with = { "-" + optInfo.shortForm, sb.toString(), optInfo.description };
                System.err.format(format_this, format_with);
            } else {
                final Object[] format_with = { "-" + optInfo.shortForm, "--" + optInfo.longForm, optInfo.description };
                System.err.format(format_this, format_with);
            }
        }
    }

}
