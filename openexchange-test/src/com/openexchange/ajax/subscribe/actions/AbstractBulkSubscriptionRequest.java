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

package com.openexchange.ajax.subscribe.actions;

import java.util.List;
import java.util.Map;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.Strings;

/**
 * A superclass for the bulk request type subscriptions that can request different kinds of fields
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractBulkSubscriptionRequest<T extends AbstractSubscriptionResponse> extends AbstractSubscriptionRequest<T> {

    protected List<String> columns;

    protected Map<String, List<String>> dynamicColumns;

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Parameter getColumnsAsParameter(){
        if(getColumns() == null) {
            return null;
        }
        return new Parameter("columns", Strings.join(getColumns(), ","));
    }

    public void setDynamicColumns(Map<String, List<String>> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    public Map<String, List<String>> getDynamicColumns() {
        return dynamicColumns;
    }

    public Params getDynamicColumnsAsParameter(){
        if(getDynamicColumns() == null) {
            return null;
        }

        Params params = new Params();
        for (String plugin : getDynamicColumns().keySet()) {
            params.add(plugin, Strings.join(getDynamicColumns().get(plugin), ","));
        }
        return params;
    }


}
