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

import java.util.Map;
import java.util.TreeMap;
import java.io.InputStream;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * {@link EAVType}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum EAVType {
    OBJECT("object"),
    STRING("string"),
    DATE("date"),
    TIME("time"),
    BINARY("binary"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    NULL("null");
    
    public static final String KEY = "t";
    
    private static Map<String, EAVType> types = new TreeMap<String, EAVType>(String.CASE_INSENSITIVE_ORDER);;
    
    private static Map<Class<?>, EAVType> guessedTypes = new HashMap<Class<?>, EAVType>();

    private static final EnumMap<EAVType, EnumSet<EAVType>> COERCIBLE = new EnumMap<EAVType, EnumSet<EAVType>>(EAVType.class);
    
    static {
        EAVTypeSwitcher guessSwitcher = getGuessSwitcher();
        COERCIBLE.put(EAVType.STRING, EnumSet.of(EAVType.BINARY));
        COERCIBLE.put(EAVType.NUMBER, EnumSet.of(EAVType.TIME, EAVType.DATE));
        for (EAVType type : EAVType.values()) {
            types.put(type.getKeyword(), type);
            for (Class<?> clazz : (Class[])type.doSwitch(guessSwitcher)) {
                guessedTypes.put(clazz, type);
            }
            if(!COERCIBLE.containsKey(type)) {
                COERCIBLE.put(type, EnumSet.noneOf(EAVType.class));
            }
        }
    }

    private String keyword;
    
    public Object doSwitch(EAVTypeSwitcher switcher, Object... args) {
        switch (this) {
        case OBJECT:
            return switcher.object(args);
        case STRING:
            return switcher.string(args);
        case DATE:
            return switcher.date(args);
        case TIME:
            return switcher.time(args);
        case BINARY:
            return switcher.binary(args);
        case NUMBER:
            return switcher.number(args);
        case BOOLEAN:
            return switcher.bool(args);
        case NULL:
            return switcher.nullValue(args);
        }
        throw new IllegalArgumentException(this.name());
    }

    public void checkCoercible(EAVType origType, Object value) throws EAVException {
        if(!isCoercibleFrom(origType)) {
            throw EAVErrorMessage.INCOMPATIBLE_TYPES.create(origType.name(), this.name());
        }
        EAVException result = (EAVException) doSwitch(validationSwitch, value);
        if(result != null) {
            throw result;
        }
    }
    
    public static EAVType guessType(Class<?> clazz) {
        return guessedTypes.get(clazz);
    }
    
    public boolean isCoercibleFrom(EAVType origType) {
        if(origType == NULL) {
            return true;
        }
        if(origType == this) {
            return true;
        }
        
        return COERCIBLE.get(origType).contains(this);
    }
    
    public String getSQLTable() {
        return (String) doSwitch(tableSwitcher);
    }
    
    public static final EAVTypeSwitcher valueSwitcher = new EAVTypeSwitcher() {

        public Object binary(Object... args) {
            // TODO Auto-generated method stub
            return null;
        }

        public Object bool(Object... args) {
            EAVNode node = (EAVNode) args[0];
            EAVContainerType cType = (EAVContainerType) args[1];
            if (cType == EAVContainerType.SINGLE) {
                node.setPayload((Boolean) args[2]);
            } else {
                Object[] source = (Object[]) args[2];
                Boolean[] values = new Boolean[source.length];
                System.arraycopy(source, 0, values, 0, values.length);
                node.setPayload(values);
            }
            return null;
        }

        public Object date(Object... args) {
            EAVNode node = (EAVNode) args[0];
            EAVContainerType cType = (EAVContainerType) args[1];
            if (cType == EAVContainerType.SINGLE) {
                node.setPayload((Number) args[2]);
            } else {
                Object[] source = (Object[]) args[2];
                Number[] values = new Number[source.length];
                System.arraycopy(source, 0, values, 0, values.length);
                node.setPayload(values);
            }
            return null;
        }

        public Object nullValue(Object... args) {
            // TODO Auto-generated method stub
            return null;
        }

        public Object number(Object... args) {
            EAVNode node = (EAVNode) args[0];
            EAVContainerType cType = (EAVContainerType) args[1];
            if (cType == EAVContainerType.SINGLE) {
                node.setPayload((Number) args[2]);
            } else {
                Object[] source = (Object[]) args[2];
                Number[] values = new Number[source.length];
                System.arraycopy(source, 0, values, 0, values.length);
                node.setPayload(values);
            }
            return null;
        }

        public Object object(Object... args) {
            // TODO Auto-generated method stub
            return null;
        }

        public Object string(Object... args) {
            EAVNode node = (EAVNode) args[0];
            EAVContainerType cType = (EAVContainerType) args[1];
            if (cType == EAVContainerType.SINGLE) {
                node.setPayload((String) args[2]);
            } else {
                Object[] source = (Object[]) args[2];
                String[] values = new String[source.length];
                System.arraycopy(source, 0, values, 0, values.length);
                node.setPayload(values);
            }
            return null;
        }

        public Object time(Object... args) {
            EAVNode node = (EAVNode) args[0];
            EAVContainerType cType = (EAVContainerType) args[1];
            if (cType == EAVContainerType.SINGLE) {
                node.setPayload((Number) args[2]);
            } else {
                Object[] source = (Object[]) args[2];
                Number[] values = new Number[source.length];
                System.arraycopy(source, 0, values, 0, values.length);
                node.setPayload(values);
            }
            return null;
        }
        
    };
    
    public static final EAVTypeSwitcher tableSwitcher = new EAVTypeSwitcher() {

        public Object binary(Object... args) {
            return "blobTable";
        }

        public Object bool(Object... args) {
            return "boolTable";
        }

        public Object date(Object... args) {
            return "intTable";
        }

        public Object nullValue(Object... args) {
            return null;
        }

        public Object number(Object... args) {
            return "intTable";
        }

        public Object object(Object... args) {
            return null;
        }

        public Object string(Object... args) {
            return "textTable";
        }

        public Object time(Object... args) {
            return "intTable";
        }
        
    };
    
    private static EAVTypeSwitcher getGuessSwitcher() {
        return new EAVTypeSwitcher() {

            public Object binary(Object... args) {
                return new Class[0];
            }

            public Object bool(Object... args) {
                return new Class[]{Boolean.class};
            }

            public Object date(Object... args) {
                return new Class[0];
            }

            public Object nullValue(Object... args) {
                return new Class[0];
            }

            public Object number(Object... args) {
                return new Class[]{Integer.class, Long.class, Float.class, Double.class};
            }

            public Object object(Object... args) {
                return new Class[0];
            }

            public Object string(Object... args) {
                return new Class[]{String.class};
            }

            public Object time(Object... args) {
                return new Class[0];
            }
            
        };
    }

    private static final EAVTypeSwitcher validationSwitch = new EAVTypeSwitcher() {

        public Object binary(Object... args) {
            return null;
        }

        public Object bool(Object... args) {
            return null;
        }

        public Object date(Object... args) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis((Long) args[0]);
            for(int field : new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND}) {
                if(0 != calendar.get(field)) {
                    return EAVErrorMessage.ILLEGAL_VALUE.create(args[0].toString(), EAVType.DATE.name());
                }
            }
            return null;
        }

        public Object nullValue(Object... args) {
            return null;
        }

        public Object number(Object... args) {
            return null;
        }

        public Object object(Object... args) {
            return null;
        }

        public Object string(Object... args) {
            return null;
        }

        public Object time(Object... args) {
            return null;
        }
        
    };
    
    public Object[] getArray(int size) {
        return (Object[]) doSwitch(TYPED_ARRAY, size);
    }
    
    public static final EAVTypeSwitcher TYPED_ARRAY = new EAVTypeSwitcher() {

        public Object binary(Object... args) {
            return new InputStream[(Integer) args[0]];
        }

        public Object bool(Object... args) {
            return new Boolean[(Integer) args[0]];
        }

        public Object date(Object... args) {
            return new Number[(Integer) args[0]];
        }

        public Object nullValue(Object... args) {
            throw new IllegalArgumentException("There are no sets of type NULL");
        }

        public Object number(Object... args) {
            return new Number[(Integer) args[0]];
        }

        public Object object(Object... args) {
            throw new IllegalArgumentException("There are no sets of type OBJECT");
        }

        public Object string(Object... args) {
            return new String[(Integer) args[0]];
        }

        public Object time(Object... args) {
            return new Number[(Integer) args[0]];
        }

    };
    
    
    private EAVType(String keyword) {
        this.keyword = keyword;
    }
    
    public static EAVType getType(Object keyword) {
        return types.get(keyword);
    }
    
    public static boolean containsType(Object keyword) {
        return types.containsKey(keyword);
    }
    
    public String getKeyword() {
        return keyword;
    }
}
