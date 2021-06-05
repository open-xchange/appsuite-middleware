/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.group.json.osgi;

import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.group.GroupService;
import com.openexchange.group.json.GroupActionFactory;
import com.openexchange.group.json.anonymizer.GroupAnonymizer;
import com.openexchange.group.json.resultconverter.GroupJsonResultConverter;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link GroupJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GroupJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link GroupJSONActivator}.
     */
    public GroupJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { GroupService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new GroupActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "group");
        registerService(ResultConverter.class, new GroupJsonResultConverter());
        registerService(AnonymizerService.class.getName(), new GroupAnonymizer());
    }

}
