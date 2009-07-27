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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * {@link EAVNode}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVNode extends AbstractNode<EAVNode> {

    private Object payload;

    private EAVType type;

    private EAVContainerType containerType = EAVContainerType.SINGLE;

    public EAVNode newInstance(){
        return new EAVNode();
    }
    
    public EAVNode() {
        super();
    }

    public EAVNode(EAVNode parent, String name) {
        super(parent, name);
    }

    public EAVNode(EAVNode parent) {
        super(parent);
    }

    public EAVNode(String name) {
        super(name);
    }

    public EAVType getType() {
        if (type == null) {
            type = EAVType.OBJECT;
        }
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    // Strings
    public void setPayload(String value) {
        this.payload = value;
        this.type = EAVType.STRING;
        this.containerType = EAVContainerType.SINGLE;
    }

    public void setPayload(String... values) {
        setPayload(EAVContainerType.MULTISET, values);
    }

    public void setPayload(EAVContainerType cType, String... values) {
        this.payload = values;
        this.type = EAVType.STRING;
        this.containerType = cType;
    }

    // Booleans

    public void setPayload(Boolean value) {
        this.payload = value;
        this.type = EAVType.BOOLEAN;
        this.containerType = EAVContainerType.SINGLE;
    }

    public void setPayload(Boolean... values) {
        setPayload(EAVContainerType.MULTISET, values);
    }

    public void setPayload(EAVContainerType cType, Boolean... values) {
        this.payload = values;
        this.type = EAVType.BOOLEAN;
        this.containerType = cType;
    }

    // Numbers

    public void setPayload(Number value) {
        this.payload = value;
        this.type = EAVType.NUMBER;
        this.containerType = EAVContainerType.SINGLE;
    }

    public void setPayload(Number... values) {
        setPayload(EAVType.NUMBER, EAVContainerType.MULTISET, values);
    }

    public void setPayload(EAVType type, Number value) {
        this.payload = value;
        this.type = type;
        this.containerType = EAVContainerType.SINGLE;
    }

    public void setPayload(EAVType type, Number... values) {
        setPayload(type, EAVContainerType.MULTISET, values);
    }

    public void setPayload(EAVType type, EAVContainerType cType, Number... values) {
        this.payload = values;
        this.type = type;
        this.containerType = cType;
    }

    public boolean isMultiple() {
        return containerType.isMultiple();
    }

    public EAVPath getRelativePath(EAVPath relativePath) {
        if (getPath().equals(relativePath)) {
            return new EAVPath(name);
        }
        return parent.getRelativePath(relativePath).append(name);
    }

    public EAVPath getRelativePath(EAVNode relativeNode) {
        return getRelativePath(relativeNode.getPath());
    }

    public void copyPayloadFromOther(EAVNode other) {
        this.payload = other.payload;
        this.type = other.type;
        this.containerType = other.containerType;
    }

    public static EAVNode nullNode(String name) {
        EAVNode node = new EAVNode(name);
        node.payload = null;
        node.type = EAVType.NULL;
        return node;
    }

    public EAVContainerType getContainerType() {
        return containerType;
    }

    public void setPayload(EAVType type, EAVContainerType containerType, Collection<Object> collection) {
        this.type = type;
        this.containerType = containerType;

        this.payload = collection.toArray((Object[]) type.doSwitch(TYPED_ARRAY, collection.size()));

    }
    
    public void setPayload(InputStream inputStream) {
        this.type = EAVType.BINARY;
        this.containerType = EAVContainerType.SINGLE;
        this.payload = inputStream;
    }
    
    public void setPayload(InputStream[] inputStreams) {
        setPayload(EAVContainerType.MULTISET, inputStreams);
    }
    
    public void setPayload(EAVContainerType cType, InputStream[] inputStreams) {
        this.type = EAVType.BINARY;
        this.containerType = cType;
        this.payload = inputStreams;
    }
    
    public void setPayload(EAVType type, EAVContainerType cType,Object payload) {
        this.type = type;
        this.containerType  = cType;
        this.payload = payload;
    }



    private static final EAVTypeSwitcher TYPED_ARRAY = new EAVTypeSwitcher() {

        public Object binary(Object... args) {
            throw new UnsupportedOperationException("Implement me ;)");
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



}
