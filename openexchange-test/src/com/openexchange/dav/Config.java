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

package com.openexchange.dav;

import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;

/**
 * {@link Config}
 * 
 * Provides static access to configuration settings fetched from {@link AJAXConfig}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Config {
	
	private Config() {
		// prevent instantiation
	}
	
    public static String getBaseUri() throws OXException {
        return getProtocol() + "://" + getHostname();
    }
    
    public static User getUser() {
    	return User.User1;
    }
    
    public static String getLogin() throws OXException {
    	return getLogin(getUser());
    }
    
    public static String getUsername() throws OXException {
    	return getUsername(getUser());
    }
    
    public static String getPassword() throws OXException {
    	return getPassword(getUser());
    }
    
    public static String getHostname() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.HOSTNAME);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.HOSTNAME.getPropertyName());
        }
        return hostname;
    }
    
    public static String getProtocol() throws OXException {
        final String hostname = AJAXConfig.getProperty(Property.PROTOCOL);
        if (null == hostname) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.PROTOCOL.getPropertyName());
        }
        return hostname;
    }
    
    public static String getLogin(final User user) throws OXException {
        final String login = AJAXConfig.getProperty(user.getLogin());
        if (null == login) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else if (login.contains("@")) {
        	return login;
        } else {
            final String context = AJAXConfig.getProperty(Property.CONTEXTNAME);
            if (null == context) {
                throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(Property.CONTEXTNAME.getPropertyName());
            }
            return login + "@" + context;
        }
    }
    
    public static String getUsername(final User user) throws OXException {
        final String username = AJAXConfig.getProperty(user.getLogin());
        if (null == username) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getLogin().getPropertyName());
        } else {
        	return username.contains("@") ? username.substring(0, username.indexOf("@")) : username;
        }
    }
    
    public static String getPassword(final User user) throws OXException {
        final String password = AJAXConfig.getProperty(user.getPassword());
        if (null == password) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(user.getPassword().getPropertyName());
        }
        return password;
    }
}
