package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;


public class FolderProperties {

    public enum AuthType {
        AdminDN("AdminDN"),
        anonymous("anonymous"),
        user("user");
        
        private final String type;
        
        private AuthType(final String type) {
            this.type = type;
        }

        
        public final String getType() {
            return type;
        }
        
    }
    
    public enum SearchScope {
        base("base"),
        one("one"),
        sub("sub");
        
        private final String type;
        
        private SearchScope(final String type) {
            this.type = type;
        }

        public final String getType() {
            return type;
        }
        
    }

    public enum Sorting {
        groupware,
        server;
    }

    
    private enum Parameters {
        foldername("foldername"),
        searchfilter("searchfilter"),
        AdminBindPW("AdminBindPW"),
        AdminDN("AdminDN"),
        authtype("authtype"),
        baseDN("baseDN"),
        mappingfile("mappingfile"),
        memorymapping("memorymapping"),
        pagesize("pagesize"),
        searchScope("searchScope"),
        sorting("sorting"),
        uri("uri");

        
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

    private String adminBindPW;

    private String adminDN;

    private AuthType authtype;

    private String baseDN;

    private Mappings mappings;

    private boolean memorymapping;

    private int pagesize;

    private SearchScope searchScope;

    private Sorting sorting;

    private String uri;
    
    private void setFoldername(final String foldername) {
        this.foldername = foldername;
    }

    private void setSearchfilter(final String searchfilter) {
        this.searchfilter = searchfilter;
    }

    public String getFoldername() {
        return foldername;
    }

    public String getSearchfilter() {
        return searchfilter;
    }

    public String getAdminBindPW() {
        return adminBindPW;
    }

    public String getAdminDN() {
        return adminDN;
    }

    public AuthType getAuthtype() {
        return authtype;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public Mappings getMappings() {
        return mappings;
    }

    public int getPagesize() {
        return pagesize;
    }

    public SearchScope getSearchScope() {
        return searchScope;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public boolean isMemorymapping() {
        return memorymapping;
    }

    public final String getUri() {
        return uri;
    }
    
    private void setAdminBindPW(final String adminBindPW) {
        this.adminBindPW = adminBindPW;
    }
    
    private void setAdminDN(final String adminDN) {
        this.adminDN = adminDN;
    }
    
    private void setAuthtype(final AuthType authtype) {
        this.authtype = authtype;
    }
    
    private void setBaseDN(final String baseDN) {
        this.baseDN = baseDN;
    }
    
    private void setMappings(final Mappings mappings) {
        this.mappings = mappings;
    }
    
    private void setMemorymapping(final boolean memorymapping) {
        this.memorymapping = memorymapping;
    }
    
    private void setPagesize(final int pagesize) {
        this.pagesize = pagesize;
    }
    
    private void setSearchScope(final SearchScope searchScope) {
        this.searchScope = searchScope;
    }
    
    private void setSorting(final Sorting sorting) {
        this.sorting = sorting;
    }
    
    private void setUri(final String uri) {
        this.uri = uri;
    }

    public static FolderProperties getFolderPropertiesFromProperties(final ConfigurationService configuration, final String name, final String folder, final String contextnr, final StringBuilder logBuilder) throws LdapConfigurationException {
        final String prefix = PropertyHandler.bundlename + "context" + contextnr + "." + folder + ".";
        
        final Properties conf = configuration.getFile(name);
        final FolderProperties retval = new FolderProperties();
        
        
        final String folderparameter = prefix + Parameters.foldername.getName();
        final String searchparameter = prefix + Parameters.searchfilter.getName();
        final String foldername = conf.getProperty(folderparameter);
        final String searchfilter = conf.getProperty(searchparameter);
        if (null != foldername && foldername.length() != 0) {
            retval.setFoldername(foldername);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, folderparameter, name);
        }

        logBuilder.append("-------------------------------------------------------------------------------").append('\n');
        logBuilder.append("Properties for Context: ").append(contextnr).append(" Propertyfile: ").append(name).append(':').append(" Foldername: ").append(retval.getFoldername()).append('\n');
        logBuilder.append("-------------------------------------------------------------------------------").append('\n');

        if (null != searchfilter && searchfilter.length() != 0) {
            retval.setSearchfilter(searchfilter);
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, searchfilter, name);
        }

        // Here we iterate over all properties...
        retval.setUri(PropertyHandler.checkStringProperty(conf, prefix + Parameters.uri.getName(), null));
        logBuilder.append("\tUri: ").append(retval.getUri()).append('\n');
        
        retval.setBaseDN(PropertyHandler.checkStringProperty(conf, prefix + Parameters.baseDN.getName(), null));
        logBuilder.append("\tBaseDN: ").append(retval.getBaseDN()).append('\n');
        
        retval.setAdminDN(PropertyHandler.checkStringProperty(conf, prefix + Parameters.AdminDN.getName(), null));
        logBuilder.append("\tAdminDN: ").append(retval.getAdminDN()).append('\n');
        
        retval.setAdminBindPW(PropertyHandler.checkStringProperty(conf, prefix + Parameters.AdminBindPW.getName(), null));
        
        final String searchScopeString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.searchScope.getName(), null);
        try {
            retval.setSearchScope(SearchScope.valueOf(searchScopeString));
            logBuilder.append("\tsearchScope: ").append(retval.getSearchScope()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SEARCH_SCOPE_WRONG, searchScopeString);
        }
        
        final String authstring = PropertyHandler.checkStringProperty(conf, prefix + Parameters.authtype.getName(), null);
        try {
            retval.setAuthtype(AuthType.valueOf(authstring));
            logBuilder.append("\tauthtype: ").append(retval.getAuthtype()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.AUTH_TYPE_WRONG, authstring);
        }

        final String sortingString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.sorting.getName(), null);
        try {
            retval.setSorting(Sorting.valueOf(sortingString));
            logBuilder.append("\tsorting: ").append(retval.getSorting()).append('\n');
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SORTING_WRONG, authstring);
        }
        
        final String memoryMappingString = PropertyHandler.checkStringProperty(conf, prefix + Parameters.memorymapping.getName(), null);
        
        // TODO: Throws no error, so use an error checking method
        retval.setMemorymapping(Boolean.parseBoolean(memoryMappingString));
        logBuilder.append("\tmemorymapping: ").append(retval.isMemorymapping()).append('\n');

        final String pagesizestring = PropertyHandler.checkStringProperty(conf, prefix + Parameters.pagesize.getName(), null);
        try {
            retval.setPagesize(Integer.parseInt(pagesizestring));
            logBuilder.append("\tpagesize: ").append(retval.getPagesize()).append('\n');
        } catch (final NumberFormatException e) {
            throw new LdapConfigurationException(Code.INVALID_PAGESIZE, pagesizestring);
        }

        final String mappingfile = PropertyHandler.checkStringProperty(conf, prefix + Parameters.mappingfile.getName(), null);
        final Properties mapprops = configuration.getFile(mappingfile);
        if (mapprops.isEmpty()) {
            throw new LdapConfigurationException(Code.INVALID_MAPPING_FILE, mappingfile);
        } else {
            retval.setMappings(Mappings.getMappingsFromProperties(mapprops, PropertyHandler.bundlename +  mappingfile.replace(".properties", ""), mappingfile));
        }

        return retval;
    }

}
