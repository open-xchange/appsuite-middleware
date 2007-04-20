package com.openexchange.admin.console;

import java.util.ArrayList;

/**
 * This class is used to extend the CmdLineParser which two main things:
 * 1. The ability to output help texts
 * 2. The ability to have mandatory options
 */
public class AdminParser extends CmdLineParser {
    public class MissingOptionException extends Exception {
        /**
		 * 
		 */
		private static final long serialVersionUID = 8134438398224308263L;

		MissingOptionException(final String msg) { super(msg); }
    }
    
	private class OptionInfo {
		public boolean needed = false;
		public Option option = null;
		public String shortForm = null;
		public String longForm = null;
		public String longFormParameterDescription = null;
		public String description = null;
		
		public OptionInfo(final boolean needed, final Option option, final String shortForm, final String longForm, final String longFormParameterDescription, final String description) {
			super();
			this.needed = needed;
			this.option = option;
			this.shortForm = shortForm;
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
		
	}
	
	ArrayList<OptionInfo> optinfolist = new ArrayList<OptionInfo>();
        private String appname = null;
	
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
		optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, description));
		
		return retval;
	}

	/**
	 * This method is used if you want to add an option with a description for the long parameter
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
		optinfolist.add(new OptionInfo(needed, retval, shortForm, longForm, description));		
		return retval;
	}
        
        public Option addOption(final char shortForm, final String longForm, final String longFormParameterDescription, final String description, final boolean needed,final boolean hasarg) {
            if(hasarg){
                final Option retval = this.addStringOption(shortForm,longForm);
                optinfolist.add(new OptionInfo(needed, retval,shortForm, longForm, description));              
                return retval;
            }else{
                final Option retval = this.addBooleanOption(shortForm,longForm);
                optinfolist.add(new OptionInfo(false, retval,shortForm, longForm, description));              
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
                final Option retval = this.addStringOption( longForm);
                optinfolist.add(new OptionInfo(needed, retval, longForm, description));              
                return retval;
        }
        
        public Option addOption(final String longForm, final String longFormParameterDescription, final String description, final boolean needed,final boolean hasarg) {
            
            if(hasarg){
                final Option retval = this.addStringOption(longForm);
                optinfolist.add(new OptionInfo(needed, retval, longForm, description));              
                return retval;
            }else{
                final Option retval = this.addBooleanOption(longForm);
                optinfolist.add(new OptionInfo(false, retval, longForm, description));              
                return retval;
            }
            
    }
	
	// As parse is declared final in CmdLineParser we cannot override it so we use another 
	// function name here
	public void ownparse(final String[] args) throws IllegalOptionValueException, UnknownOptionException, MissingOptionException {
		// First parse the whole args then get through the list an check is options that are needed
		// aren't set. By this we implement the missing feature of mandatory options
		parse(args);
                StringBuilder sb = new StringBuilder();
		for (final OptionInfo optInfo : optinfolist) {                        
			if (optInfo.needed) {
				if (null == getOptionValue(optInfo.option)) {
                                    sb.append(optInfo.longForm+",");
				}
			}
		}
                
                // show all missing opts                 
                if(sb.toString().length()>0){
                    sb.deleteCharAt(sb.length()-1);
                    throw new MissingOptionException("Option(s) \""+sb.toString()+"\" missing");
                }
                
                
	}
	
	public void printUsage() {
            System.err.println("Usage: "+this.appname);
            
            for (final OptionInfo optInfo : optinfolist) {          
                String format_this = " %s,%-30s %s\n";
                if(optInfo.shortForm==null){  
                    format_this = " %s %-30s %s\n";                 
                    Object[] format_with_ = {"  ","--"+optInfo.longForm,optInfo.description};
                    System.err.format(format_this, format_with_);                
                }else{
                    // example result :
                    // -c,--contextid                    The id of the context                    
                    Object[] format_with = {"-"+optInfo.shortForm,"--"+optInfo.longForm,optInfo.description};
                    System.err.format(format_this, format_with); 
                }
            }
    }
        
        public AdminParser (String appname){
            super();
            this.appname  = appname;
        }
        
        

}
