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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting.UserSettingMailAttributeChangers;

/**
 * {@link AbstractUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractUserAttributeChangers extends AbstractAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(UserSettingMailAttributeChangers.class);

    private final Map<Attribute, UserAttributeChanger> changers;
    private String table;

    /**
     * Initialises a new {@link AbstractUserAttributeChangers}.
     */
    public AbstractUserAttributeChangers(String table, EnumSet<? extends Attribute> attributes) {
        super(attributes);
        this.table = table;
        changers = initialiseChangers();
    }

    /**
     * Initialises the changers
     * 
     * @return a map with the changers
     */
    protected abstract Map<Attribute, UserAttributeChanger> initialiseChangers();

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(java.util.Set, com.openexchange.admin.rmi.dataobjects.User, int, int, java.sql.Connection)
     */
    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection) throws StorageException {
        Set<String> changedAttributes = new HashSet<>();
        for (Attribute attribute : getAttributes()) {
            if (change(attribute, userData, userId, contextId, connection)) {
                changedAttributes.add(attribute.getName());
            }
        }
        return Collections.unmodifiableSet(changedAttributes);
    }

    /**
     * Changes the specified {@link Attribute}
     * 
     * @param attribute The {@link Attribute} to change
     * @param userData The {@link User} data
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param connection The {@link Connection} to use
     * @return <code>true</code> if the attribute was changed successfully; <code>false</code> otherwise
     * @throws StorageException if an SQL error or any other error is occurred
     */
    private boolean change(Attribute attribute, User userData, int userId, int contextId, Connection connection) throws StorageException {
        UserAttributeChanger changer = changers.get(attribute);
        if (changer == null) {
            LOG.debug("No user attribute changer found for user attribute '{}' in table '{}'. The attribute will not be changed.", attribute.getSQLFieldName(), table);
            return false;
        }
        try {
            return changer.changeAttribute(userId, contextId, userData, connection);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
