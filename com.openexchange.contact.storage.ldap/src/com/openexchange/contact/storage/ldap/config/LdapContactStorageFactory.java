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
        } else {
            LOG.debug("File {} contains no contact-storage-ldap settings, skipping.", propertyFile);
            return null;
        }
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

