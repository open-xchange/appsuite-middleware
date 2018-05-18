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

package com.openexchange.chronos.storage.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.java.Streams;

/**
 * {@link ExtendedPropertyParametersMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ExtendedPropertyParametersMapping<O> extends DefaultDbMapping<List<ExtendedPropertyParameter>, O> {

    /**
     * Initializes a new {@link ExtendedPropertyParametersMapping}.
     *
     * @param columnLabel The column label
     * @param readableName The readable name for the mapped field
     */
    protected ExtendedPropertyParametersMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.BLOB);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        try {
            byte[] data = ExtendedPropertiesCodec.encodeParameters(get(object));
            if (null == data) {
                statement.setNull(parameterIndex, getSqlType());
            } else {
                statement.setBinaryStream(parameterIndex, Streams.newByteArrayInputStream(data), data.length);
            }
            return 1;
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<ExtendedPropertyParameter> get(ResultSet resultSet, String columnLabel) throws SQLException {
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnLabel);
            return ExtendedPropertiesCodec.decodeParameters(inputStream);
        } catch (IOException e) {
            throw new SQLException(e);
        } finally {
            Streams.close(inputStream);
        }
    }

}
