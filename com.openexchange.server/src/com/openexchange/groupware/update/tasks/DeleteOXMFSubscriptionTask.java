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

package com.openexchange.groupware.update.tasks;

import java.util.Arrays;
import java.util.List;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.WorkingLevel;

/**
 * {@link DeleteOXMFSubscriptionTask} - Deletes leftovers of OXMF aka. microformats from the subscriptions table
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class DeleteOXMFSubscriptionTask extends SubscriptionRemoverTask  {

    /**
     * The MF contacts source id from MicroformatSubscribeService
     */
    private static final String CONTACTS_SOURCE_ID = "com.openexchange.subscribe.microformats.contacts.http";

    /**
     * The infostore a.k.a. files a.k.a. drive source id from MicroformatSubscribeService
     */
    private static final String INFOSTORE_SOURCE_ID = "com.openexchange.subscribe.microformats.infostore.http";
    
    private static final List<String> SOURCE_IDS = Arrays.asList(CONTACTS_SOURCE_ID, INFOSTORE_SOURCE_ID);

    /**
     * Initializes a new {@link DeleteOXMFSubscriptionTask}.
     * 
     */
    public DeleteOXMFSubscriptionTask() {
        super(SOURCE_IDS);
    }


    @Override
    public String[] getDependencies() {
        return new String[] { DropPublicationTablesTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND, WorkingLevel.SCHEMA);
    }

}
