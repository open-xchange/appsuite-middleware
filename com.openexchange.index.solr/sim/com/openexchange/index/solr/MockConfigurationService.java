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

package com.openexchange.index.solr;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;


/**
 * {@link MockConfigurationService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockConfigurationService implements ConfigurationService {

    @Override
    public String getProperty(final String name) {
        return "IAmAString";
    }

    @Override
    public Map<String, String> getProperties(final PropertyFilter filter) throws OXException {
        return Collections.emptyMap();
    }

    @Override
    public String getProperty(final String name, final String defaultValue) {
        // Nothing to do
        return null;
    }

    @Override
    public String getProperty(final String name, final PropertyListener listener) {
        // Nothing to do
        return null;
    }

    @Override
    public String getProperty(final String name, final String defaultValue, final PropertyListener listener) {
        // Nothing to do
        return null;
    }

    @Override
    public void removePropertyListener(final String name, final PropertyListener listener) {
        // Nothing to do

    }

    @Override
    public File getDirectory(final String directoryName) {
        // Nothing to do
        return null;
    }

    @Override
    public File getFileByName(final String fileName) {
        // Nothing to do
        return null;
    }

    @Override
    public Properties getFile(final String fileName) {
        return new Properties();
    }

    @Override
    public String getText(final String fileName) {
        // Nothing to do
        return null;
    }

    @Override
    public Properties getPropertiesInFolder(final String folderName) {
        // Nothing to do
        return null;
    }

    @Override
    public boolean getBoolProperty(final String name, final boolean defaultValue) {
        // Nothing to do
        return false;
    }

    @Override
    public int getIntProperty(final String name, final int defaultValue) {
        // Nothing to do
        return 0;
    }

    @Override
    public Iterator<String> propertyNames() {
        // Nothing to do
        return null;
    }

    @Override
    public int size() {
        // Nothing to do
        return 0;
    }

    @Override
    public Object getYaml(final String filename) {
        // Nothing to do
        return null;
    }

    @Override
    public Map<String, Object> getYamlInFolder(final String dirName) {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.config.ConfigurationService#getFilterFromProperty(java.lang.String)
     */
    @Override
    public Filter getFilterFromProperty(final String name) {
        // Nothing to do
        return null;
    }

}
