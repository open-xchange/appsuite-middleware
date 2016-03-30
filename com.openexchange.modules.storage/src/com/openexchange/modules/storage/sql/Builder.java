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

package com.openexchange.modules.storage.sql;

import java.util.List;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;
import com.openexchange.sql.grammar.Constant;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;


/**
 * {@link Builder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Builder<T extends Model<T>> {

    private final Metadata<T> metadata;
    private String tableName = null;

    public Builder(Metadata<T> metadata) {
        this.metadata = metadata;
    }

    public INSERT insert(List<Attribute<T>> attributes, List<String> extraFields) {
        INSERT insert = new INSERT().INTO(getTableName());
        for (Attribute<T> attribute : attributes) {
            insert = insert.SET(attribute.getName(), Constant.PLACEHOLDER);
        }
        for (String field : extraFields) {
            insert = insert.SET(field, Constant.PLACEHOLDER);
        }
        return insert;
    }

    public UPDATE update(List<? extends Attribute<T>> attributes) {
        UPDATE update = new UPDATE(getTableName());
        for (Attribute<T> attribute : attributes) {
            update = update.SET(attribute.getName(), Constant.PLACEHOLDER);
        }
        update.WHERE(matchOne());
        return update;
    }


    public DELETE delete() {
        DELETE delete = new DELETE().FROM(getTableName());
        delete.WHERE(matchOne());
        return delete;
    }

    public SELECT select(List<Attribute<T>> attributes) {
        SELECT select = selectWithoutWhere(attributes);
        select.WHERE(matchOne());

        return select;
    }

    public SELECT selectWithoutWhere(List<Attribute<T>> attributes) {
        SELECT select = new SELECT().FROM(getTableName());
        for (Attribute<T> attribute : attributes) {
            select.addColumn(attribute.getName());
        }
        return select;
    }

    public Predicate matchOne() {
        return new EQUALS(metadata.getIdField().getName(), Constant.PLACEHOLDER).AND(new EQUALS("cid", Constant.PLACEHOLDER));
    }

    public String getTableName() {
        if(tableName == null) {
            return metadata.getName();
        }
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
