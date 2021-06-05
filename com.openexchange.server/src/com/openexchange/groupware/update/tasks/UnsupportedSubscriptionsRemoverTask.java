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

/**
 *
 * {@link UnsupportedSubscriptionsRemoverTask} removes all subscriptions which are not supported anymore.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class UnsupportedSubscriptionsRemoverTask extends SubscriptionRemoverTask {

    public UnsupportedSubscriptionsRemoverTask() {
        super(Arrays.asList("com.openexchange.subscribe.crawler.facebook",
                            "com.openexchange.subscribe.socialplugin.facebook",
                            "com.openexchange.subscribe.crawler.googlemail",
                            "com.openexchange.subscribe.crawler.gmx",
                            "com.openexchange.subscribe.crawler.gmx.de",
                            "com.openexchange.subscribe.crawler.google.calendar",
                            "com.openexchange.subscribe.google.contact",
                            "com.openexchange.subscribe.socialplugin.linkedin"
                            ));
    }

}
