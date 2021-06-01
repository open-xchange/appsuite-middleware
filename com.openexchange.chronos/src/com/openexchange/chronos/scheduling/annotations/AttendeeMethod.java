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

package com.openexchange.chronos.scheduling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link AttendeeMethod} - Marks this method to be used by an attendee.
 * <p>
 * {@link #role()} can be defined to
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface AttendeeMethod {

    public enum ROLE {
        /**
         * The attendee has no special role
         */
        DEFAULT,

        /**
         * The attendee is action on behalf of another attendee or event the
         * organizer
         */
        ON_BEHALF_OF;
    }

    /**
     * The role the attendee has.
     * 
     * @return {@link ROLE#DEFAULT} per default or {@link ROLE#ON_BEHALF_OF}
     */
    public ROLE role() default ROLE.DEFAULT;

}
