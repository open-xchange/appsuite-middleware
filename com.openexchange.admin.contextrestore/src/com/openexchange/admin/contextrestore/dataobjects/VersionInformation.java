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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

/**
 *
 */
package com.openexchange.admin.contextrestore.dataobjects;

public class VersionInformation {
    private final int version;

    private final int locked;

    private final int gw_compatible;

    private final int admin_compatible;

    private final String server;

    /**
     * @param admin_compatible
     * @param gw_compatible
     * @param locked
     * @param server
     * @param version
     */
    public VersionInformation(final int admin_compatible, final int gw_compatible, final int locked, final String server, final int version) {
        this.admin_compatible = admin_compatible;
        this.gw_compatible = gw_compatible;
        this.locked = locked;
        this.server = server;
        this.version = version;
    }

    public final int getVersion() {
        return version;
    }

    public final int getLocked() {
        return locked;
    }

    public final int getGw_compatible() {
        return gw_compatible;
    }

    public final int getAdmin_compatible() {
        return admin_compatible;
    }

    public final String getServer() {
        return server;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + admin_compatible;
        result = prime * result + gw_compatible;
        result = prime * result + locked;
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VersionInformation)) {
            return false;
        }
        VersionInformation other = (VersionInformation) obj;
        if (admin_compatible != other.admin_compatible) {
            return false;
        }
        if (gw_compatible != other.gw_compatible) {
            return false;
        }
        if (locked != other.locked) {
            return false;
        }
        if (server == null) {
            if (other.server != null) {
                return false;
            }
        } else if (!server.equals(other.server)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

}
