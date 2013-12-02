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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.logging.mbean;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.MDC;
import org.slf4j.Marker;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;


/**
 * {@link ExtendedMDCFilter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ExtendedMDCFilter extends TurboFilter {
    
    private Set<Tuple> tuples = new HashSet<Tuple>();
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtendedMDCFilter other = (ExtendedMDCFilter) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    private final String name;
    
    public ExtendedMDCFilter(String n) {
        name = n;
    }
    
    /* (non-Javadoc)
     * @see ch.qos.logback.classic.turbo.TurboFilter#decide(org.slf4j.Marker, ch.qos.logback.classic.Logger, ch.qos.logback.classic.Level, java.lang.String, java.lang.Object[], java.lang.Throwable)
     */
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable th) {
        
        for(Tuple t : tuples) {
            String v = MDC.get(t.getKey());
            if (v == null)
                return FilterReply.NEUTRAL;
            else {
                if (!v.equals(t.getValue())) {
                    return FilterReply.NEUTRAL;
                }
            }
        }
            
        return FilterReply.ACCEPT;
    }
    
    public void addTuple(String k, String v) {
        tuples.add(new Tuple(k,v));
    }
    
    public String getName() {
        return name;
    }
    
    //TODO clean me :)
    private class Tuple {
        private String key;
        private String value;
        
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }


        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple other = (Tuple) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }


        /**
         * Gets the key
         *
         * @return The key
         */
        public String getKey() {
            return key;
        }

        
        /**
         * Sets the key
         *
         * @param key The key to set
         */
        public void setKey(String key) {
            this.key = key;
        }

        
        /**
         * Gets the value
         *
         * @return The value
         */
        public String getValue() {
            return value;
        }

        
        /**
         * Sets the value
         *
         * @param value The value to set
         */
        public void setValue(String value) {
            this.value = value;
        }

        public Tuple(String k, String v) {
            key = k;
            value = v;
        }


        private ExtendedMDCFilter getOuterType() {
            return ExtendedMDCFilter.this;
        }
        
    }

}
