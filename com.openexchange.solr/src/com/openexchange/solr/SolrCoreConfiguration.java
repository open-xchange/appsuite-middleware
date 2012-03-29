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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.solr;

import java.io.File;
import java.net.URI;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.solr.internal.Services;


/**
 * {@link SolrCoreConfiguration}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreConfiguration {

    private final SolrCoreIdentifier identifier;
    
    private final String coreDirPath;
    
    private final String coreName;
    
    private final String dataDirPath;
    
    private final String configDirPath;
    
    private final String schemaFileName;
    
    private final String configFileName;

    private final String configFilePath;
    
    private final String configDirName;
    
    private final String dataDirName;
    
    
    public SolrCoreConfiguration(final URI coreStoreUri, final SolrCoreIdentifier identifier) throws OXException {
        super();
        final ConfigurationService config = Services.getService(ConfigurationService.class);        
        this.identifier = identifier;
        coreName = identifier.toString();
        coreDirPath = coreStoreUri.getPath() + File.separator + coreName;
        dataDirPath = coreDirPath + File.separator + config.getProperty(SolrProperties.PROP_DATA_DIR_NAME);
        configDirPath = coreDirPath + File.separator + config.getProperty(SolrProperties.PROP_CONFIG_DIR_NAME);
        schemaFileName = config.getProperty(getPropertyForSchemaFileName(identifier.getModule()));
        configFileName = config.getProperty(getPropertyForConfigFileName(identifier.getModule()));
        configFilePath = configDirPath + File.separator + configFileName;
        configDirName = config.getProperty(SolrProperties.PROP_CONFIG_DIR_NAME);;
        dataDirName = config.getProperty(SolrProperties.PROP_DATA_DIR_NAME);
    }
    
    public String getCoreName() {
        return coreName;
    }
    
    public String getCoreDirPath() {
        return coreDirPath;
    }
    
    public String getDataDirPath() {
        return dataDirPath;
    }
    
    public String getConfigDirPath() {
        return configDirPath;
    }
    
    public String getSchemaFileName() {
        return schemaFileName;
    }
    
    public String getConfigFileName() {
        return configFileName;
    }
    
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    public String getConfigDirName() {
        return configDirName;
    }

    public String getDataDirName() {
        return dataDirName;
    }

    public SolrCoreIdentifier getIdentifier() {
        return identifier;
    }
    
    private static String getPropertyForSchemaFileName(final int module) throws OXException {
        switch (module) {
        
        case Types.EMAIL:
            return SolrProperties.PROP_SCHEMA_MAIL;
                
        default:
            throw SolrExceptionCodes.UNKNOWN_MODULE.create(module);
            
        }
    }
    
    private static String getPropertyForConfigFileName(final int module) throws OXException {
        switch (module) {
        
        case Types.EMAIL:
            return SolrProperties.PROP_CONFIG_MAIL_NAME;
                
        default:
            throw SolrExceptionCodes.UNKNOWN_MODULE.create(module);
            
        }
    }
}
