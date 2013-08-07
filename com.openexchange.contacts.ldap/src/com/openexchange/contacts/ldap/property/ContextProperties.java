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

package com.openexchange.contacts.ldap.property;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;


/**
 * Holds all context specific properties
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class ContextProperties {

    private final List<FolderProperties> folderproperties;

    /**
     * Initializes a new {@link ContextProperties}.
     */
    private ContextProperties() {
        this.folderproperties = new ArrayList<FolderProperties>();
    }

    public static ContextProperties getContextPropertiesFromDir(final ConfigurationService service, final File dir, final int contextid, StringBuilder logBuilder) throws OXException {
        final ContextProperties retval = new ContextProperties();
        // First list the folderdirs which should be registered to that context
        final File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".properties");
            }
        });
        for (final File file : files) {
            final String name = file.getName();
            final String purename = name.replace(".properties", "");
            retval.addFolderProperties(FolderProperties.getFolderPropertiesFromProperties(service, name, purename, String.valueOf(contextid), logBuilder));
        }
        return retval;
    }

    public List<FolderProperties> getFolderproperties() {
        return folderproperties;
    }

    /**
     * @param o
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    private boolean addFolderProperties(FolderProperties o) {
        return folderproperties.add(o);
    }
}
