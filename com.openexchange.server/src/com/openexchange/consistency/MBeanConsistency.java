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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import com.openexchange.exception.OXException;

/**
 * Proxy for an MBean provided ConsistencyMBean
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
final class MBeanConsistency implements ConsistencyMBean {

    private final MBeanServerConnection mbsc;
    private final ObjectName name;

    public MBeanConsistency(final MBeanServerConnection mbsc, final ObjectName name) {
        this.mbsc = mbsc;
        this.name = name;
    }

    @Override
    public List<String> listMissingFilesInContext(final int contextId) throws MBeanException {
        try {
            return (List<String>) mbsc.invoke(name, "listMissingFilesInContext", new Object[] { contextId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listMissingFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listMissingFilesInFilestore", new Object[] { filestoreId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listMissingFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listMissingFilesInDatabase", new Object[] { databaseId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listAllMissingFiles() throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listAllMissingFiles", new Object[] {}, new String[] {});
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public List<String> listUnassignedFilesInContext(final int contextId) throws MBeanException {
        try {
            return (List<String>) mbsc.invoke(name, "listUnassignedFilesInContext", new Object[] { contextId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listUnassignedFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listUnassignedFilesInFilestore", new Object[] { filestoreId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listUnassignedFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listUnassignedFilesInDatabase", new Object[] { databaseId }, new String[] { "int" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public Map<MBeanEntity, List<String>> listAllUnassignedFiles() throws MBeanException {
        try {
            return (Map<MBeanEntity, List<String>>) mbsc.invoke(name, "listAllUnassignedFiles", new Object[] {}, new String[] {});
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    @Override
    public void repairFilesInContext(final int contextId, final String resolverPolicy) throws MBeanException {
        try {
            mbsc.invoke(name, "repairFilesInContext", new Object[] { contextId, resolverPolicy }, new String[] { "int", "java.lang.String" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            exception(e);
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
    }

    @Override
    public void repairFilesInFilestore(final int filestoreId, final String resolverPolicy) throws MBeanException {
        try {
            mbsc.invoke(name, "repairFilesInFilestore", new Object[] { filestoreId, resolverPolicy }, new String[] { "int", "java.lang.String" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
    }

    @Override
    public void repairFilesInDatabase(final int databaseId, final String resolverPolicy) throws MBeanException {
        try {
            mbsc.invoke(name, "repairFilesInDatabase", new Object[] { databaseId, resolverPolicy }, new String[] { "int", "java.lang.String" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
    }

    @Override
    public void repairAllFiles(final String resolverPolicy) throws MBeanException {
        try {
            mbsc.invoke(name, "repairAllFiles", new Object[] { resolverPolicy }, new String[] { "java.lang.String" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            throw e;
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
    }

    @Override
    public List<String> checkOrRepairConfigDB(final boolean repair) throws MBeanException {
        try {
            return (List<String>) mbsc.invoke(name, "checkOrRepairConfigDB", new Object[] { repair }, new String[] { "boolean" });
        } catch (final InstanceNotFoundException e) {
            exception(e);
        } catch (final MBeanException e) {
            exception(e);
        } catch (final ReflectionException e) {
            exception(e);
        } catch (final IOException e) {
            exception(e);
        }
        return null;
    }

    private void exception(final OXException e) throws MBeanException {
        final Exception wrapMe = new Exception(e.getMessage());
        throw new MBeanException(wrapMe, e.getMessage());
    }

    private void exception(final Exception e) throws MBeanException {
        final OXException oxe = ConsistencyExceptionCodes.COMMUNICATION_PROBLEM.create(e, e.getMessage());
        final Exception wrapMe = new Exception(oxe.getMessage());
        throw new MBeanException(wrapMe, e.getMessage());
    }

}
