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

package com.openexchange.consistency;

import static com.openexchange.java.Autoboxing.I;
import org.apache.commons.collections.keyvalue.MultiKey;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link EntityImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EntityImpl implements Entity {

    private static final long serialVersionUID = -4838502301287665200L;

    private final EntityType type;
    private final MultiKey identifier;
    private final Context context;
    private final User user;

    /**
     * Initializes a new {@link EntityImpl} of type {@link EntityType#Context context}.
     *
     * @param context The context
     */
    public EntityImpl(Context context) {
        this(new MultiKey(new Object[] { I(context.getContextId()) }), context, null, EntityType.Context);
    }

    /**
     * Initializes a new {@link EntityImpl} of type {@link EntityType#User user}.
     *
     * @param context The context
     * @param user The user
     */
    public EntityImpl(Context context, User user) {
        this(new MultiKey(I(context.getContextId()), I(user.getId())), context, user, EntityType.User);
    }

    /**
     * Initialises a new {@link EntityImpl}.
     */
    private EntityImpl(MultiKey identifier, Context context, User user, EntityType type) {
        super();
        this.identifier = identifier;
        this.type = type;
        this.context = context;
        this.user = user;
    }

    @Override
    public MultiKey getId() {
        return identifier;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Entity [type=").append(type).append(", identifier=").append(identifier).append("]");
        return builder.toString();
    }

}
