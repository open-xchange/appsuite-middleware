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

package com.openexchange.event.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Properties;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;

/**
 * {@link EventConfigImpl}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class EventConfigImpl extends AbstractConfigWrapper implements EventConfig {

    private boolean isEventQueueEnabled;

    private int eventQueueDelay = 60000;

    //private boolean isInit;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EventConfigImpl.class);

    public EventConfigImpl() {

    }

    public EventConfigImpl(final Properties props) {
        /*-
         * This if statement always yields false
         *
        if (isInit) {
            return;
        }
        */

        if (props == null) {
            LOG.error("missing propfile");
            return;
        }

        isEventQueueEnabled = parseProperty(props, "com.openexchange.event.isEventQueueEnabled", false);
        LOG.debug("Event property: com.openexchange.event.isEventQueueEnabled={}", isEventQueueEnabled ? Boolean.TRUE : Boolean.FALSE);

        eventQueueDelay = parseProperty(props, "com.openexchange.event.eventQueueDelay", eventQueueDelay);
        LOG.debug("Event property: com.openexchange.event.eventQueueDelay={}", I(eventQueueDelay));

        /*-
         * Field "isInit" is never used
         *
        isInit = true;
        */
    }

    @Override
    public boolean isEventQueueEnabled() {
        return isEventQueueEnabled;
    }

    @Override
    public void setEventQueueEnabled(final boolean isEventQueueEnabled) {
        this.isEventQueueEnabled = isEventQueueEnabled;
    }

    @Override
    public int getEventQueueDelay() {
        return eventQueueDelay;
    }

    @Override
    public void setEventQueueDelay(final int eventQueueDelay) {
        this.eventQueueDelay = eventQueueDelay;
    }
}
