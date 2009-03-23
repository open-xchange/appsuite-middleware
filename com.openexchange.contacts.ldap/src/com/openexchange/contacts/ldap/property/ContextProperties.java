package com.openexchange.contacts.ldap.property;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;


public class ContextProperties {

    private List<FolderProperties> folderproperties;

    /**
     * Initializes a new {@link ContextProperties}.
     */
    private ContextProperties() {
        this.folderproperties = new ArrayList<FolderProperties>();
    }
    
    public static ContextProperties getContextPropertiesFromDir(final ConfigurationService service, final File dir, final int contextid, StringBuilder logBuilder) throws LdapConfigurationException {
        final ContextProperties retval = new ContextProperties();
        // First list the folderdirs which should be registered to that context
        final File[] folderdirs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        for (final File folderdir : folderdirs) {
            final String name = folderdir.getName();
            final String purename = name.replace(".properties", "");
            retval.addFolderProperties(FolderProperties.getFolderPropertiesFromProperties(service, name, purename, String.valueOf(contextid), logBuilder));
        }
        return retval;
    }

    public List<FolderProperties> getFolderproperties() {
        return folderproperties;
    }

    /**
     * @param o
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    private boolean addFolderProperties(FolderProperties o) {
        return folderproperties.add(o);
    }
}
