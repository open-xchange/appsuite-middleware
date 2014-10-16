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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.share.storage.mapping;

import java.util.Date;
import java.util.EnumMap;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.BinaryMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.DefaultShare;

/**
 * {@link ShareMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareMapper extends DefaultDbMapper<DefaultShare, ShareField> {

    /**
     * Initializes a new {@link ShareMapper}.
     */
    public ShareMapper() {
        super();
    }

    @Override
    public DefaultShare newInstance() {
        return new DefaultShare();
    }

    @Override
    public ShareField[] newArray(int size) {
        return new ShareField[size];
    }

    @Override
    protected EnumMap<ShareField, ? extends DbMapping<? extends Object, DefaultShare>> createMappings() {

        EnumMap<ShareField, DbMapping<? extends Object, DefaultShare>> mappings = new
            EnumMap<ShareField, DbMapping<? extends Object, DefaultShare>>(ShareField.class);

        mappings.put(ShareField.TOKEN, new BinaryMapping<DefaultShare>("token", "Token") {

            @Override
            public void set(DefaultShare share, byte[] value) {
                share.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(value)));
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return null != share.getToken();
            }

            @Override
            public byte[] get(DefaultShare share) {
                return UUIDs.toByteArray(UUIDs.fromUnformattedString(share.getToken()));
            }

            @Override
            public void remove(DefaultShare share) {
                share.setToken(null);
            }
        });
        mappings.put(ShareField.CONTEXT_ID, new IntegerMapping<DefaultShare>("cid", "Context ID") {

            @Override
            public void set(DefaultShare share, Integer value) {
                share.setContextID(value.intValue());
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return 0 < share.getContextID();
            }

            @Override
            public Integer get(DefaultShare share) {
                return Integer.valueOf(share.getContextID());
            }

            @Override
            public void remove(DefaultShare share) {
                share.setContextID(0);
            }
        });
        mappings.put(ShareField.CREATION_DATE, new BigIntMapping<DefaultShare>("created", "Creation date") {

            @Override
            public void set(DefaultShare share, Long value) {
                share.setCreated(new Date(value));
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return null != share.getCreated();
            }

            @Override
            public Long get(DefaultShare share) {
                return share.getCreated().getTime();
            }

            @Override
            public void remove(DefaultShare share) {
                share.setCreated(null);
            }
        });
        mappings.put(ShareField.CREATED_BY, new IntegerMapping<DefaultShare>("createdBy", "Created by") {

            @Override
            public void set(DefaultShare share, Integer value) {
                share.setCreatedBy(value.intValue());
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return 0 < share.getCreatedBy();
            }

            @Override
            public Integer get(DefaultShare share) {
                return Integer.valueOf(share.getCreatedBy());
            }

            @Override
            public void remove(DefaultShare share) {
                share.setCreatedBy(0);
            }
        });
        mappings.put(ShareField.LAST_MODIFIED, new BigIntMapping<DefaultShare>("lastModified", "Last modification date") {

            @Override
            public void set(DefaultShare share, Long value) {
                share.setLastModified(new Date(value));
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return null != share.getLastModified();
            }

            @Override
            public Long get(DefaultShare share) {
                return share.getLastModified().getTime();
            }

            @Override
            public void remove(DefaultShare share) {
                share.setLastModified(null);
            }
        });
        mappings.put(ShareField.MODIFIED_BY, new IntegerMapping<DefaultShare>("modifiedBy", "Modified by") {

            @Override
            public void set(DefaultShare share, Integer value) {
                share.setModifiedBy(value.intValue());
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return 0 < share.getModifiedBy();
            }

            @Override
            public Integer get(DefaultShare share) {
                return Integer.valueOf(share.getModifiedBy());
            }

            @Override
            public void remove(DefaultShare share) {
                share.setModifiedBy(0);
            }
        });
        mappings.put(ShareField.GUEST_ID, new IntegerMapping<DefaultShare>("guest", "Guest ID") {

            @Override
            public void set(DefaultShare share, Integer value) {
                share.setGuest(value.intValue());
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return 0 < share.getGuest();
            }

            @Override
            public Integer get(DefaultShare share) {
                return Integer.valueOf(share.getGuest());
            }

            @Override
            public void remove(DefaultShare share) {
                share.setGuest(0);
            }
        });
        mappings.put(ShareField.AUTHENTICATION, new IntegerMapping<DefaultShare>("auth", "Authentication") {

            @Override
            public void set(DefaultShare share, Integer value) {
                AuthenticationMode authentication;
                switch (value.intValue()) {
                case 0:
                    authentication = AuthenticationMode.ANONYMOUS;
                    break;
                case 1:
                    authentication = AuthenticationMode.ANONYMOUS_PASSWORD;
                    break;
                case 2:
                    authentication = AuthenticationMode.GUEST_PASSWORD;
                    break;
                default:
                    throw new IllegalArgumentException("value");
                }
                share.setAuthentication(authentication);
            }

            @Override
            public boolean isSet(DefaultShare share) {
                return null != share.getAuthentication();
            }

            @Override
            public Integer get(DefaultShare share) {
                switch (share.getAuthentication()) {
                case ANONYMOUS:
                    return Integer.valueOf(0);
                case ANONYMOUS_PASSWORD:
                    return Integer.valueOf(1);
                case GUEST_PASSWORD:
                    return Integer.valueOf(2);
                default:
                    throw new IllegalArgumentException("authentication");
                }
            }

            @Override
            public void remove(DefaultShare share) {
                share.setAuthentication(null);
            }
        });

        return mappings;
    }

}

