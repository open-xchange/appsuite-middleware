/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contact.storage.ldap.config;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.config.LdapConfig.IDMapping;
import com.openexchange.contact.storage.ldap.folder.LdapGlobalFolderCreator;
import com.openexchange.contact.storage.ldap.internal.CachingLdapContactStorage;
import com.openexchange.contact.storage.ldap.internal.LdapContactStorage;
import com.openexchange.contact.storage.ldap.internal.Tools;
import com.openexchange.exception.OXException;

/**
 * {@link LdapContactStorageFactory}
 *
 * Factory for LDAP contact storages based on property files.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class LdapContactStorageFactory {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapContactStorageFactory.class);

    private LdapContactStorageFactory() {
        super();
        // prevent instantiation
    }

    public static ContactStorage create(File propertyFile) throws OXException {
        Properties properties = Tools.loadProperties(propertyFile);
        if (null != properties && properties.containsKey("com.openexchange.contact.storage.ldap.contextID")) {
            return create(properties);
        }
        LOG.debug("File {} contains no contact-storage-ldap settings, skipping.", propertyFile);
        return null;
    }

    public static ContactStorage create(Properties properties) throws OXException {
        LdapConfig config = new LdapConfig(properties);
        if (null == config.getFolderID()) {
            if (null == config.getFoldername()) {
                throw LdapExceptionCodes.MISSING_CONFIG_VALUE.create("foldername");
            }
            config.setFolderID(Integer.toString(getFolderID(config.getContextID(), config.getFoldername())));
        }
        ContactStorage storage = null;
        if (0 < config.getRefreshinterval()) {
            if (IDMapping.DYNAMIC.equals(config.getIDMapping())) {
                throw LdapExceptionCodes.WRONG_OR_MISSING_CONFIG_VALUE.create("refreshinterval");
            }
            storage = new CachingLdapContactStorage(config);
            LOG.info("Caching LDAP storage created, using the following configuration:");
            LOG.info(config.toString());
        } else {
            storage = new LdapContactStorage(config);
            LOG.info("LDAP storage created, using the following configuration:");
            LOG.info(config.toString());
        }
        return storage;
    }

    public static List<ContactStorage> createAll() throws OXException {
        List<ContactStorage> storages = new ArrayList<ContactStorage>();
        for (File propertyFile : Tools.listPropertyFiles()) {
            ContactStorage storage = null;
            try {
                storage = create(propertyFile);
            } catch (OXException e) {
                LOG.error("Error creating LDAP contact storage", e);
            }
            if (null != storage) {
                storages.add(storage);
            }
        }
        return storages;
    }

    private static int getFolderID(int contextID, String folderName) throws OXException {
        try {
            return LdapGlobalFolderCreator.createGlobalFolder(contextID, folderName).getFolderid();
        } catch (SQLException e) {
            throw LdapExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

}

