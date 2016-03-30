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

package com.openexchange.config.cascade;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.openexchange.exception.OXException;


/**
 * {@link ConfigProviderService} - Provides access to properties/attributes obeying a certain scope.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface ConfigProviderService {

    /** Constant for no context */
    public static final int NO_CONTEXT = -1;

    /** Constant for no user */
    public static final int NO_USER = -1;

    /** Constant for no property */
    public static final BasicProperty NO_PROPERTY = new BasicProperty() {

        @Override
        public String get() throws OXException {
            return null;
        }

        @Override
        public String get(final String metadataName) throws OXException {
            return null;
        }

        @Override
        public boolean isDefined() throws OXException {
            return false;
        }

        @Override
        public void set(final String value) throws OXException {
            // Ignore
        }

        @Override
        public void set(final String metadataName, final String value) throws OXException {
            // Ignore
        }

        @Override
        public List<String> getMetadataNames() throws OXException {
            return Collections.emptyList();
        }

    };

    /**
     * Gets the denoted property.
     *
     * @param property The property name
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The property if found; otherwise {@link #NO_PROPERTY}
     * @throws OXException If returning property fails for any reason
     */
    BasicProperty get(String property, int contextId, int userId) throws OXException;

    /**
     * Gets all property names for specified user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return The property names
     * @throws OXException If returning property names fails for any reason
     */
    Collection<String> getAllPropertyNames(int contextId, int userId) throws OXException;

	/**
	 * Gets the scope of this provider
	 * <p>
	 * Currently known scopes:
	 * <ul>
	 * <li><code>"server"</code></li>
	 * <li><code>"contextSets"</code></li>
	 * <li><code>"context"</code></li>
	 * <li><code>"user"</code></li>
	 * </ul>
	 *
	 * @return The scope of the provider
	 */
	String getScope();

}
