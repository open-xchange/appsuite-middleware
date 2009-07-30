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

package com.openexchange.eav;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link EAVTypeCoercion}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVTypeCoercion {

    public static enum Mode { INCOMING, OUTGOING }
    
    private static final Log LOG = LogFactory.getLog(EAVTypeCoercion.class);

    private Mode mode = Mode.INCOMING;
    
    public EAVTypeCoercion(Mode mode) {
        this.mode = mode;
    }
    
    public Object coerce(EAVType origType, Object payload, EAVTypeMetadataNode typeInfo) throws EAVException {
        return coerce(origType, payload, typeInfo, null);
    }

    public Object coerce(EAVType origType, Object payload, EAVTypeMetadataNode typeInfo, TimeZone defaultTZ) throws EAVException {
        EAVType type = typeInfo.getType();
        if(type == null) {
            type = origType;
        }
        type.checkCoercible(origType, payload);

        switch (origType) {
        case NUMBER:
            switch (type) {
            case DATE:
                return payload;
            case TIME: {
                long utc = coerceTime(payload, typeInfo, defaultTZ);
                return utc;
            }
            }
            break;
        case STRING:
            switch (type) {
            case BINARY: {
                return decodeBase64((String) payload);
            }
            }
            break;
        case TIME :
            long utc = coerceTime(payload, typeInfo, defaultTZ);
            return utc;
        }

        return payload;
    }

    private long coerceTime(Object payload, EAVTypeMetadataNode typeInfo, TimeZone defaultTZ) {
        TimeZone tz = defaultTZ;
        if (typeInfo.hasOption("timezone")) {
            tz = TimeZone.getTimeZone((String) typeInfo.getOption("timezone"));
        }

        long utc = (Long) payload;
        if (tz != null) {
            utc = recalculateTime(utc, tz);
        }
        return utc;
    }

    public Object[] coerceMultiple(EAVType origType, Object[] payload, EAVTypeMetadataNode typeInfo) throws EAVException {
        return coerceMultiple(origType, payload, typeInfo, null);
    }

    public Object[] coerceMultiple(EAVType origType, Object[] payload, EAVTypeMetadataNode typeInfo, TimeZone defaultTZ) throws EAVException {
        EAVType type = typeInfo.getType();
        if(type == null) {
            type = origType;
        }
        Object[] coerced = type.getArray(payload.length);
        int index = 0;
        for (Object origElement : payload) {
            Object coercedElement = coerce(origType, origElement, typeInfo, defaultTZ);
            coerced[index++] = coercedElement;
        }

        return coerced;
    }

    private InputStream decodeBase64(String payload) {
        Base64 base64 = new Base64();
        try {
            return new ByteArrayInputStream(base64.decode(payload.getBytes("ASCII")));
        } catch (UnsupportedEncodingException e) {
            LOG.fatal(e.getMessage(), e);
        }
        return null;
    }

    private long recalculateTime(long time, TimeZone timeZone) {
        switch(mode) {
        case INCOMING: return time - timeZone.getOffset(time);
        case OUTGOING : return time + timeZone.getOffset(time);
        }
        return -1; 
    }

}
