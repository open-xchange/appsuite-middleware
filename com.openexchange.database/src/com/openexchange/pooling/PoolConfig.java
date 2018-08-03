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

package com.openexchange.pooling;

/**
 * {@link PoolConfig}
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a> 
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - extracted
 * @since v7.10.1
 */
public class PoolConfig implements Cloneable {

    public int maxIdle;

    public long maxIdleTime;

    public int maxActive;

    public long maxWait;

    public long maxLifeTime;

    public ExhaustedActions exhaustedAction;

    public boolean testOnActivate;

    public boolean testOnDeactivate;

    public boolean testOnIdle;

    public boolean testThreads;

    public PoolConfig() {
        super();
        maxIdle = -1;
        maxIdleTime = 60000;
        maxActive = -1;
        maxWait = 10000;
        maxLifeTime = -1;
        exhaustedAction = ExhaustedActions.GROW;
        testOnActivate = true;
        testOnDeactivate = true;
        testOnIdle = false;
        testThreads = false;
    }

    @Override
    public PoolConfig clone() {
        try {
            return (PoolConfig) super.clone();
        } catch (final CloneNotSupportedException e) {
            // Will not appear!
            throw new Error("Assertion failed!", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Database pooling options:\n\tMaximum idle connections: ");
        sb.append(maxIdle);
        sb.append("\n\tMaximum idle time: ");
        sb.append(maxIdleTime);
        sb.append("ms\n\tMaximum active connections: ");
        sb.append(maxActive);
        sb.append("\n\tMaximum wait time for a connection: ");
        sb.append(maxWait);
        sb.append("ms\n\tMaximum life time of a connection: ");
        sb.append(maxLifeTime);
        sb.append("ms\n\tAction if connections exhausted: ");
        sb.append(exhaustedAction.toString());
        sb.append("\n\tTest connections on activate  : ");
        sb.append(testOnActivate);
        sb.append("\n\tTest connections on deactivate: ");
        sb.append(testOnDeactivate);
        sb.append("\n\tTest idle connections         : ");
        sb.append(testOnIdle);
        sb.append("\n\tTest threads for bad connection usage (SLOW): ");
        sb.append(testThreads);
        return sb.toString();
    }
    
    public static final PoolConfig DEFAULT_CONFIG;

    static {
        DEFAULT_CONFIG = new PoolConfig();
        DEFAULT_CONFIG.maxIdle = -1;
        DEFAULT_CONFIG.maxIdleTime = 60000;
        DEFAULT_CONFIG.maxActive = -1;
        DEFAULT_CONFIG.maxWait = 10000;
        DEFAULT_CONFIG.maxLifeTime = -1;
        DEFAULT_CONFIG.exhaustedAction = ExhaustedActions.BLOCK;
        DEFAULT_CONFIG.testOnActivate = false;
        DEFAULT_CONFIG.testOnDeactivate = true;
        DEFAULT_CONFIG.testOnIdle = false;
        DEFAULT_CONFIG.testThreads = false;
    }
}
