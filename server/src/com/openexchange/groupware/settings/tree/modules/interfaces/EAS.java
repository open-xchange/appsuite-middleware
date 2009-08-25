package com.openexchange.groupware.settings.tree.modules.interfaces;

import com.openexchange.groupware.settings.tree.AbstractModules;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 * Contains initialization for the modules configuration tree setting exchange active sync.
 * @author <a href="mailto:marcus@open-xchange.org">Francisco Laguna</a> - Faithfully cargo culted from the SyncML class
 */
public class EAS extends AbstractModules {

    /**
     * Default constructor.
     */
    public EAS() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPath() {
        return new String[] { "modules", "interfaces", "eas" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getModule(final UserConfiguration userConfig) {
		return userConfig.hasActiveSync();
	}
}

