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

package com.openexchange.groupware.update.internal;

import com.openexchange.groupware.update.Schema;

/**
 * This class is a data container for the update information of a database schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SchemaImpl implements Schema {

    private static final long serialVersionUID = 8722328691217424068L;

    private boolean locked;

    private boolean groupwareCompatible;

    private boolean adminCompatible;

    private String server;

    private String schema;

    protected SchemaImpl() {
        super();
    }

    public SchemaImpl(boolean locked, boolean groupwareCompatible, boolean adminCompatible) {
        super();
        this.locked = locked;
        this.groupwareCompatible = groupwareCompatible;
        this.adminCompatible = adminCompatible;
    }

    public SchemaImpl(Schema schema) {
        super();
        this.locked = schema.isLocked();
        this.groupwareCompatible = schema.isGroupwareCompatible();
        this.adminCompatible = schema.isAdminCompatible();
        this.server = schema.getServer();
        this.schema = schema.getSchema();
    }

    @Override
    public boolean isAdminCompatible() {
        return adminCompatible;
    }

    public void setAdminCompatible(final boolean adminCompatible) {
        this.adminCompatible = adminCompatible;
    }

    @Override
    public boolean isGroupwareCompatible() {
        return groupwareCompatible;
    }

    public void setGroupwareCompatible(final boolean groupwareCompatible) {
        this.groupwareCompatible = groupwareCompatible;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    void setBlockingUpdatesRunning(final boolean locked) {
        this.locked = locked;
    }

    @Override
    public String getServer() {
        return server;
    }

    public void setServer(final String server) {
        this.server = server;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }
}
