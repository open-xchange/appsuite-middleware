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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * StringHelper
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StringHelper {
	
	private static final Log LOG = LogFactory.getLog(StringHelper.class);
	
	public static final String SERVER_BUNDLE = "com.openexchange.groupware.i18n.ServerMessages";

	private ResourceBundle serverBundle;
	
	public StringHelper(final Locale locale) {
		// We won't have a dedicated english bundle, so don't use default locale!
		if(locale.getLanguage().equalsIgnoreCase("en")) {
			this.serverBundle = null;
			return;
		}
		try {
			this.serverBundle = ResourceBundle.getBundle(SERVER_BUNDLE, locale);
		} catch (MissingResourceException x) {
			LOG.debug("Cannot find bundle for Locale "+locale);
			this.serverBundle = null;
		}
	}

	public StringHelper(final String resourceBundle, final Locale locale) {
		// We won't have a dedicated english bundle, so don't use default locale!
		if(locale.getLanguage().equalsIgnoreCase("en")) {
			this.serverBundle = null;
			return;
		}
		try {
			this.serverBundle = ResourceBundle.getBundle(resourceBundle, locale);
		} catch (MissingResourceException x) {
			LOG.debug("Cannot find bundle "+resourceBundle+" for Locale "+locale);
			this.serverBundle = null;
		}
	}

	/**
	 * Tries to load a String under key for the given locale in the resource
	 * bundle. If either the resource bundle or the String is not found the key
	 * is returned instead. This makes most sense for ResourceBundles created
	 * with the gettext tools.
	 */
	public final String getString(final String key) {
		if (serverBundle == null) {
			return key;
		}
		try {
			return serverBundle.getString(key);
		} catch (MissingResourceException x) {
			if (LOG.isInfoEnabled()) {
				LOG.info(new StringBuilder("Using default for bundle ").append(serverBundle));
			}
			return key;
		}
	}
	
	@Override
	public int hashCode(){
		if(serverBundle == null) {
			return 0;
		}
		return serverBundle.getClass().hashCode();
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o instanceof StringHelper) {
			final StringHelper sh = (StringHelper) o;
			if(serverBundle == null && sh.serverBundle == null) {
				return true;
			}
			if(serverBundle == null && sh.serverBundle != null) {
				return false;
			}
			if(serverBundle != null && sh.serverBundle == null) {
				return false;
			}
			
			return sh.serverBundle.hashCode() == serverBundle.hashCode();
		}
		return false;
	}
}
