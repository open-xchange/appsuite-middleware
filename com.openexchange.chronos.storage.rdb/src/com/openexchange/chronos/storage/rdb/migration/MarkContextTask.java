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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.java.Autoboxing.I;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;

/**
 * {@link MarkContextTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class MarkContextTask extends MigrationTask {

    private final Map<String, String> attributesToSet;

    /**
     * Initializes a new {@link MarkContextTask}.
     *
     * @param config The migration config
     * @param contextId The identifier of the context being migrated
     * @param sourceStorage The source calendar storage
     * @param destinationStorage The destination calendar storage
     * @param attributesToSet The attributes to set
     */
    public MarkContextTask(MigrationConfig config, int contextId, CalendarStorage sourceStorage, CalendarStorage destinationStorage, Map<String, String> attributesToSet) {
        super(config, contextId, sourceStorage, destinationStorage);
        this.attributesToSet = attributesToSet;
    }

    @Override
    protected void perform() throws OXException {
        if (config.isUncommitted()) {
            LOG.info("Skipping {} as migration is running in 'uncommitted' mode in context {}.", getName(), I(contextId));
            return;
        }
        ContextService contextService = config.getServiceLookup().getService(ContextService.class);
        for (Entry<String, String> entry : attributesToSet.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            LOG.trace("About to set attribute \"{}\" to \"{}\" in context {}...", name, value, I(contextId));
            contextService.setAttribute(entry.getKey(), entry.getValue(), contextId);
            LOG.trace("Successfully set attribute \"{}\" to \"{}\" in context {}.", name, value, I(contextId));
        }
        LOG.info("Successfully set {} attributes in context {}.", I(attributesToSet.size()), I(contextId));
    }

}
