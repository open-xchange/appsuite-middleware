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
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.Types;
import com.openexchange.solr.internal.Services;


/**
 * {@link SolrCoreConfiguration}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreConfiguration {

    private final SolrCoreIdentifier identifier;

    private final String baseUri;
    
    
    public SolrCoreConfiguration(final String baseUri, final SolrCoreIdentifier identifier) {
        super();
        this.baseUri = baseUri;
        this.identifier = identifier;
    }
    
    public String getCoreName() {
        return getIdentifier().toString();
    }
    
    public String getInstanceDir() {
        return baseUri + File.pathSeparator + getIdentifier().toString();
    }
    
    public String getDataDir() {
        final ConfigurationService config = Services.getService(ConfigurationService.class);        
        return getInstanceDir() + File.pathSeparator + config.getProperty(SolrProperties.PROP_DATA_DIR_NAME);
    }
    
    public String getSchemaPath() {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        switch (getIdentifier().getModule()) {
        
            case Types.EMAIL:
                return config.getProperty(SolrProperties.PROP_SCHEMA_MAIL);
                
            default:
                return null;
            
        }
    }
    
    public String getConfigPath() {
        final ConfigurationService config = Services.getService(ConfigurationService.class);
        String configFile;
        switch (getIdentifier().getModule()) {
        
            case Types.EMAIL:
                configFile = config.getProperty(SolrProperties.PROP_CONFIG_MAIL_NAME);
                break;
                
            default:
                return null;
        
        }
        
        return getInstanceDir() + File.pathSeparator + config.getProperty(SolrProperties.PROP_CONFIG_DIR_NAME) + File.pathSeparator + configFile;
    }

    public SolrCoreIdentifier getIdentifier() {
        return identifier;
    }

}
