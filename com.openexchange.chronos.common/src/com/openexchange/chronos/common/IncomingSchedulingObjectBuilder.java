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

package com.openexchange.chronos.common;

import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.scheduling.IncomingSchedulingObject;
import com.openexchange.exception.OXException;

/**
 * {@link IncomingSchedulingObjectBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class IncomingSchedulingObjectBuilder {

    protected CalendarUser originator;

    /**
     * Initializes a new {@link IncomingSchedulingObjectBuilder}.
     */
    private IncomingSchedulingObjectBuilder() {}

    /**
     * Initializes a new {@link IncomingSchedulingMessageBuilder}.
     *
     * @return This instance for chaining
     */
    public static IncomingSchedulingObjectBuilder newBuilder() {
        return new IncomingSchedulingObjectBuilder();
    }

    /**
     * Set the originator
     *
     * @param originator The originator to set
     * @return This instance for chaining
     */
    public IncomingSchedulingObjectBuilder setOriginator(CalendarUser originator) {
        this.originator = originator;
        return this;
    }

    /**
     * Builds the object
     *
     * @return The {@link IncomingSchedulingObject}
     */
    public IncomingSchedulingObject build() {
        return new IncomingSchedulingObjectImpl(this);
    }
}

class IncomingSchedulingObjectImpl implements IncomingSchedulingObject {

    private final @NonNull CalendarUser originator;

    /**
     * Initializes a new {@link IncomingSchedulingObjectImpl}.
     * 
     * @param builder The builder
     */
    public IncomingSchedulingObjectImpl(IncomingSchedulingObjectBuilder builder) {
        super();
        CalendarUser originator = builder.originator;
        if (null == originator) {
            throw new IllegalStateException();
        }
        this.originator = originator;
    }

    @Override
    @NonNull
    public CalendarUser getOriginator() throws OXException {
        return originator;
    }

    @Override
    public String toString() {
        return "IncomingSchedulingMailMeta [originator=" + originator + "]";
    }

}
