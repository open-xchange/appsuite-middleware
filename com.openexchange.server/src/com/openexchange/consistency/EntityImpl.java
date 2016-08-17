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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.consistency;

import org.apache.commons.collections.keyvalue.MultiKey;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 * {@link EntityImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EntityImpl implements Entity {

    private static final long serialVersionUID = -4838502301287665200L;
    private EntityType type;
    private MultiKey identifier;
    private Context context;
    private User user;

    /**
     * Initialises a new {@link EntityImpl} as {@link Context}.
     * 
     * @param context The context
     */
    public EntityImpl(Context context) {
        this(new MultiKey(new Object[] { context.getContextId() }));
        this.context = context;

    }

    /**
     * Initialises a new {@link EntityImpl} as {link User}
     * 
     * @param context The context
     * @param user The user
     */
    public EntityImpl(Context context, User user) {
        this(new MultiKey(context.getContextId(), user.getId()));
        this.context = context;
        this.user = user;
    }

    /**
     * Initialises a new {@link EntityImpl}.
     */
    private EntityImpl(MultiKey identifier) {
        super();
        if (identifier == null) {
            throw new IllegalArgumentException("The identifiers can not be 'null'.");
        }
        this.identifier = identifier;
        switch (identifier.getKeys().length) {
            case 1:
                type = EntityType.Context;
                break;
            case 2:
                type = EntityType.User;
                break;
            default:
                throw new IllegalArgumentException("Invalid amount of identifiers specified: " + identifier.getKeys().length);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.Entity#getId()
     */
    @Override
    public MultiKey getId() {
        return identifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.Entity#getType()
     */
    @Override
    public EntityType getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.Entity#getContext()
     */
    @Override
    public Context getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.Entity#getUser()
     */
    @Override
    public User getUser() {
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Entity [type=").append(type).append(", identifier=").append(identifier).append("]");
        return builder.toString();
    }

}
