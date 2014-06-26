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

package com.openexchange.realtime;

import java.util.concurrent.TimeUnit;
import com.openexchange.realtime.packet.ID;

/**
 * A {@link Component} is a service that manages synthetic resources, i.e. addressees that represent a system component.
 * It is a factory for {@link ComponentHandle}s and manages their state. 
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface Component {
    
    /**
     * 
     * An {@link EvictionPolicy} determines how and when a ComponentHandle should be disposed of.
     *
     * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
     */
    public interface EvictionPolicy {
        // Marker interface
    }
    
    /**
     * 
     * An eviction policy that closes the ComponentHandler after the {@link Timeout} has elapsed
     *
     * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
     */
    public class Timeout implements EvictionPolicy {

        private long timeout;
        private TimeUnit unit;

        public Timeout(long timeout, TimeUnit unit) {
            super();
            this.timeout = timeout;
            this.unit = unit;
        }

        public Timeout(long timeout) {
            this(timeout, TimeUnit.MILLISECONDS);
        }

        public long getTimeout() {
            return timeout;
        }

        public TimeUnit getUnit() {
            return unit;
        }

        public void setUnit(TimeUnit unit) {
            this.unit = unit;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public void onExpire() {
            // Nothing to do
        }
    }

    /**
     * Don't evict automatically
     */
    public EvictionPolicy NONE = new EvictionPolicy() {
        // Nothing to do
    };
    
    /**
     * Create a component handle for the given ID. Can return null, if it doesn't want to create the handle. 
     */
    ComponentHandle create(ID id);
    
    /**
     * Get's the name of this component type. This is used along with the 'synthetic' protocol to construct a component handles ID. 
     * The general form of a components ID is: synthetic.[name]://[restOfId]
     */
    String getId();
    
    /**
     * Provide the eviction policy for the component handle
     */
    EvictionPolicy getEvictionPolicy();

}
