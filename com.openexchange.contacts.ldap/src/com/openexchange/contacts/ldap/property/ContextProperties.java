package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;


public class ContextProperties {

    private enum Parameters {
        foldername("foldername"),
        searchfilter("searchfilter");
        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        public final String getName() {
            return name;
        }    
    }
    
    private String foldername;
    
    private String searchfilter;
    
    private void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    private void setSearchfilter(String searchfilter) {
        this.searchfilter = searchfilter;
    }

    public String getFoldername() {
        return foldername;
    }

    public String getSearchfilter() {
        return searchfilter;
    }

    public static ContextProperties getContextPropertiesFromProperties(final Properties props, final String contextnr) throws LdapConfigurationException {
        final String prefix = PropertyHandler.bundlename + "context" + contextnr + ".";
        
        final ContextProperties retval = new ContextProperties();
        
        final String folderparameter = prefix + Parameters.foldername.getName();
        final String searchparameter = prefix + Parameters.searchfilter.getName();
        final String foldername = props.getProperty(folderparameter);
        final String searchfilter = props.getProperty(searchparameter);
        if (null != foldername && foldername.length() != 0) {
            retval.setFoldername(foldername);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, folderparameter);
        }
        if (null != searchfilter && searchfilter.length() != 0) {
            retval.setSearchfilter(searchfilter);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, searchfilter);
        }

        return retval;
    }
}
