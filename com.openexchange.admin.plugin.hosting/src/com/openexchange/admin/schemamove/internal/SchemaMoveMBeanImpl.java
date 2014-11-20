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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.admin.schemamove.internal;

import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.schemamove.SchemaMoveService;
import com.openexchange.admin.schemamove.mbean.SchemaMoveMBean;


/**
 * {@link SchemaMoveMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaMoveMBeanImpl extends StandardMBean implements SchemaMoveMBean {

    private final SchemaMoveService schemaMoveService;

    /**
     * Initializes a new {@link SchemaMoveMBeanImpl}.
     *
     * @throws NotCompliantMBeanException If initialization fails
     */
    public SchemaMoveMBeanImpl(SchemaMoveService schemaMoveService) throws NotCompliantMBeanException {
        super(SchemaMoveMBean.class);
        this.schemaMoveService = schemaMoveService;
    }

    @Override
    public void disableSchema(String schemaName) throws TargetDatabaseException, StorageException, NoSuchObjectException, MissingServiceException {
        schemaMoveService.disableSchema(schemaName);
    }

    @Override
    public AttributeList getDbAccessInfoForSchema(String schemaName) throws MBeanException {
        try {
            Map<String, String> map = schemaMoveService.getDbAccessInfoForSchema(schemaName);
            AttributeList list = new AttributeList(map.size());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                list.add(new Attribute(entry.getKey(), entry.getValue()));
            }
            return list;
        } catch (Exception e) {
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public void enableSchema(String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException {
        schemaMoveService.enableSchema(schemaName);
    }

    @Override
    public void restorePoolReferences(String sourceSchema, String targetSchema, int writeDbPoolId, int readDbPoolId) throws StorageException {
        schemaMoveService.restorePoolReferences(sourceSchema, targetSchema, writeDbPoolId, readDbPoolId);        
    }

}
