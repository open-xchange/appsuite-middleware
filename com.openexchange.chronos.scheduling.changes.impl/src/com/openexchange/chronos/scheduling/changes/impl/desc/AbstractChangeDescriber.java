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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link AbstractChangeDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The {@link Class} of the {@link EventField} the description is for
 * @since v7.10.3
 */
public abstract class AbstractChangeDescriber<T> implements ChangeDescriber {

    protected final EventField field;

    protected final Class<T> clazz;

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractChangeDescriber.class);

    /**
     * Initializes a new {@link AbstractChangeDescriber}.
     * 
     * @param field The field that can be described
     * @param clazz The {@link Class} of the {@link EventField} the description is for
     */
    public AbstractChangeDescriber(EventField field, Class<T> clazz) {
        super();
        this.field = field;
        this.clazz = clazz;
    }

    /**
     * Describe the change
     * 
     * @param value The value
     * @param helper The {@link StringHelper} to get description in the correct {@link Locale}
     * @return The change description
     */
    abstract List<SentenceImpl> describe(T original, T updated);

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { field };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        if (eventUpdate.getUpdatedFields().contains(field)) {
            return new DefaultDescription(describe(castValueTo(clazz, field, eventUpdate.getOriginal()), castValueTo(clazz, field, eventUpdate.getUpdate())), field);
        }
        return null;
    }

    /**
     * Casts the value to the appropriate class
     * 
     * @param clazz The class to cast to
     * @param value The current value
     * @return The value as the given class
     * @throws IllegalArgumentException If the class of the object differs
     * @throws OXException If getting the value fails
     */
    static <T> T castValueTo(Class<T> clazz, EventField field, Event event) throws IllegalArgumentException {
        try {
            Object value = getValue(field, event);
            if (null == value) {
                return null;
            }
            if (clazz.isAssignableFrom(value.getClass())) {
                return clazz.cast(value);
            }
        } catch (OXException e) {
            LOGGER.error("Unexpected error", e);
        }
        throw new IllegalArgumentException("The value is either null or not of the correct class");
    }

    static Object getValue(EventField field, Event event) throws OXException {
        return EventMapper.getInstance().get(field).get(event);
    }
}
