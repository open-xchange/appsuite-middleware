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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXThrows;

import javax.management.*;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * Proxy for an MBean provided ConsistencyMBean
 * 
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
@OXExceptionSource(classId = ConsistencyClasses.MBEAN_CONSISTENCY, component = EnumComponent.CONSISTENCY)
final class MBeanConsistency implements ConsistencyMBean {

    private static final ConsistencyExceptionFactory EXCEPTIONS = new ConsistencyExceptionFactory(MBeanConsistency.class);

    private MBeanServerConnection mbsc;
    private ObjectName name;

    public MBeanConsistency(MBeanServerConnection mbsc, ObjectName name) {
        this.mbsc = mbsc;
        this.name = name;
    }

    public List<String> listMissingFilesInContext(int contextId) throws AbstractOXException {
        try {
            return (List<String>) mbsc.invoke(name, "listMissingFilesInContext", new Object[]{contextId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }



    public Map<Integer, List<String>> listMissingFilesInFilestore(int filestoreId) throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listMissingFilesInFilestore", new Object[]{filestoreId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public Map<Integer, List<String>> listMissingFilesInDatabase(int databaseId) throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listMissingFilesInDatabase", new Object[]{databaseId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public Map<Integer, List<String>> listAllMissingFiles() throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listAllMissingFiles", new Object[]{}, new String[]{});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public List<String> listUnassignedFilesInContext(int contextId) throws AbstractOXException {
        try {
            return (List<String>) mbsc.invoke(name, "listUnassignedFilesInContext", new Object[]{contextId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public Map<Integer, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listUnassignedFilesInFilestore", new Object[]{filestoreId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public Map<Integer, List<String>> listUnassignedFilesInDatabase(int databaseId) throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listUnassignedFilesInDatabase", new Object[]{databaseId}, new String[]{"int"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public Map<Integer, List<String>> listAllUnassignedFiles() throws AbstractOXException {
        try {
            return (Map<Integer, List<String>>) mbsc.invoke(name, "listAllUnassignedFiles", new Object[]{}, new String[]{});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
        return null;
    }

    public void repairFilesInContext(int contextId, String resolverPolicy) throws AbstractOXException {
        try {
            mbsc.invoke(name, "repairFilesInContext", new Object[]{contextId, resolverPolicy}, new String[]{"int", "java.lang.String"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
    }

    public void repairFilesInFilestore(int filestoreId, String resolverPolicy) throws AbstractOXException {
        try {
            mbsc.invoke(name, "repairFilesInFilestore", new Object[]{filestoreId, resolverPolicy}, new String[]{"int", "java.lang.String"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
    }

    public void repairFilesInDatabase(int databaseId, String resolverPolicy) throws AbstractOXException {
        try {
            mbsc.invoke(name, "repairFilesInDatabase", new Object[]{databaseId, resolverPolicy}, new String[]{"int", "java.lang.String"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
    }

    public void repairAllFiles(String resolverPolicy) throws AbstractOXException {
        try {
            mbsc.invoke(name, "repairAllFiles", new Object[]{resolverPolicy}, new String[]{"java.lang.String"});
        } catch (InstanceNotFoundException e) {
            exception(e);
        } catch (MBeanException e) {
            exception(e);
        } catch (ReflectionException e) {
            exception(e);
        } catch (IOException e) {
            exception(e);
        }
    }

    @OXThrows(category = AbstractOXException.Category.INTERNAL_ERROR, desc = "", exceptionId = 1, msg = "Error communicating with mbean in server: %s")
    private void exception(Exception e) throws ConsistencyException {
        throw EXCEPTIONS.createException(1, e, e.getLocalizedMessage());
    }
}
