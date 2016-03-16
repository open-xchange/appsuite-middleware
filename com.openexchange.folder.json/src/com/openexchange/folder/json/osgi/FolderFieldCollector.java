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

package com.openexchange.folder.json.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;

/**
 * {@link FolderFieldCollector} - Collector for additional folder fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderFieldCollector implements ServiceTrackerCustomizer<AdditionalFolderField, AdditionalFolderField> {

    /**
     * The list.
     */
    private final AdditionalFolderFieldList list;

    /**
     * The bundle context.
     */
    private final BundleContext context;

    /**
     * Initializes a new {@link FolderFieldCollector}.
     *
     * @param context The bundle context
     * @param list The additional folder field list used to store tracked fields.
     */
    public FolderFieldCollector(final BundleContext context, final AdditionalFolderFieldList list) {
        super();
        this.list = list;
        this.context = context;
    }

    @Override
    public AdditionalFolderField addingService(final ServiceReference<AdditionalFolderField> reference) {
        final AdditionalFolderField field = context.getService(reference);
        list.addField(field);
        return field;
    }

    @Override
    public void modifiedService(final ServiceReference<AdditionalFolderField> reference, final AdditionalFolderField service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<AdditionalFolderField> reference, final AdditionalFolderField service) {
        try {
            final AdditionalFolderField field = service;
            list.remove(field.getColumnID());
        } finally {
            context.ungetService(reference);
        }
    }

}
