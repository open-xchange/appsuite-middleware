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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.serverconfig.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.capabilities.Capability;
import com.openexchange.serverconfig.ClientServerConfigFilter;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ShareMailConfig;

/**
 * {@link ServerConfigImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ServerConfigImpl implements ServerConfig {
    
    private Map<String, Object> mappings;
    private List<ClientServerConfigFilter> clientServerConfigFilters;
    
    public ServerConfigImpl(Map<String, Object> mappings, List<ClientServerConfigFilter> clientServerConfigFilters) {
        this.mappings = mappings;
        this.clientServerConfigFilters = clientServerConfigFilters;
    }

    @Override
    public Map<String, Object> asMap() {
        return new HashMap<>(mappings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Capability> getCapabilities() {
        return (Set<Capability>) mappings.get("capabilities");
    }

    @Override
    public boolean isForceHttps() {
        return (boolean) mappings.get("forceHttps");
    }

    @Override
    public String[] getHosts() {
        return (String[]) mappings.get("hosts");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SimpleEntry<String, String>> getLanguages() {
        return (List<SimpleEntry<String, String>>) mappings.get("languages");
    }

    @Override
    public String getServerVersion() {
        return (String) mappings.get("serverVersion");
    }

    @Override
    public String getServerBuildDate() {
        return (String) mappings.get("buildDate");
    }

    @Override
    public String getUIVersion() {
        return (String) mappings.get("version");
    }

    @Override
    public String getProductName() {
        return (String) mappings.get("productName");
    }

    @Override
    public Map<String, Object> forClient() {
        for(ClientServerConfigFilter filter : clientServerConfigFilters) {
            filter.apply(mappings);
        }
        return mappings;
    }

    @Override
    public ShareMailConfig getShareMailConfig() {
        Map<String, Object> sharingMap = (Map<String, Object>) mappings.get("sharing");
        if(sharingMap != null) {
            Map<String, Object> mailsMap = (Map<String, Object>) sharingMap.get("mails");
            if(mailsMap != null) {
                Map<String, Object> buttonMap = (Map<String,Object>) mailsMap.get("button");
                ShareMailConfig shareMailConfig = new ShareMailConfig();
                
                String footerImage = (String) mailsMap.get("footer-image");
                String footerText = (String) mailsMap.get("footer-text");
                shareMailConfig.setFooterImage(footerImage);
                shareMailConfig.setFooterText(footerText);
                
                String buttonBackgroundColor = (String) buttonMap.get("background-color");
                String buttonBorderColor = (String) buttonMap.get("border-color");
                String buttonColor = (String) buttonMap.get("color");                   
                shareMailConfig.setButtonBackgroundColor(buttonBackgroundColor);
                shareMailConfig.setButtonBorderColor(buttonBorderColor);
                shareMailConfig.setButtoncolor(buttonColor);
                return shareMailConfig;
            }
        }
        return null;
    }

}
