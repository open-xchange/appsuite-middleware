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

package com.openexchange.subscribe.mslive;

import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.AbstractSubscribeTestEnvironment;

/**
 * {@link MSLiveSubscribeTestEnvironment}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MSLiveSubscribeTestEnvironment extends AbstractSubscribeTestEnvironment {

    protected static final String CONTACT_SOURCE_ID = "com.openexchange.subscribe.mslive.contact";

    /**
     * Initializes a new {@link MSLiveSubscribeTestEnvironment}.
     */
    public MSLiveSubscribeTestEnvironment() {
        super("com.openexchange.oauth.msliveconnect");
    }

    @Override
    protected void initEnvironment() throws Exception {
        // nothing yet, prolly create the oauth account
    }

    @Override
    protected void createSubscriptions() throws Exception {
        int userId = ajaxClient.getValues().getUserId();
        final int privateContactFolder = ajaxClient.getValues().getPrivateContactFolder();
        createSubscription(getAccountId(), CONTACT_SOURCE_ID, FolderObject.CONTACT, privateContactFolder, userId);
    }
}
