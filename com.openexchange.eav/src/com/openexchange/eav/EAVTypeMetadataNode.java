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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * {@link EAVTypeMetadataNode}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVTypeMetadataNode extends AbstractNode<EAVTypeMetadataNode> {

    private EAVType type = null;
    private Map<String, Object> options = new HashMap<String, Object>();
    private EAVContainerType containerType = EAVContainerType.SINGLE;
    
    
    public EAVTypeMetadataNode() {
        super();
    }


    public EAVTypeMetadataNode(EAVTypeMetadataNode parent, String name) {
        super(parent, name);
    }


    public EAVTypeMetadataNode(EAVTypeMetadataNode parent) {
        super(parent);
    }


    public EAVTypeMetadataNode(String name) {
        super(name);
    }


    @Override
    public void copyPayloadFromOther(EAVTypeMetadataNode other) {
        type = other.type;
        options = new HashMap<String, Object>(other.options);
        containerType = other.containerType;
    }


    @Override
    public EAVTypeMetadataNode newInstance() {
        return new EAVTypeMetadataNode();
    }
    
    public void setOptions(Map<String, Object> options) {
        this.options = new HashMap<String, Object>(options);
    }
    
    public void setOption(String option, Object value) {
        this.options.put(option, value);
    }
    
    public Object getOption(String option) {
        return options.get(option);
    }
    
    public void setType(EAVType type) {
        this.type = type;
    }
    
    public void setContainerType(EAVContainerType containerType) {
        this.containerType = containerType;
    }
    
    public EAVType getType() {
        return type;
    }
 
    public EAVContainerType getContainerType() {
        return containerType;
    }
 
    private static final EAVTypeOptionVerifier verifier = new EAVTypeOptionVerifier();
    
    public void verifyOptions() throws EAVException {
        if(isLeaf()) {
            EAVException x = (EAVException) type.doSwitch(verifier, options);
            if(x != null) {
                throw x;
            }
            return;
        }
        for(EAVTypeMetadataNode child : children) {
            child.verifyOptions();
        }
    }


    public boolean hasOption(String string) {
        return options.containsKey(string);
    }


    public EAVTypeMetadataNode mergeWith(EAVTypeMetadataNode other) throws EAVException {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(getName());
        Set<String> alreadyHandled = new HashSet<String>();
        for(EAVTypeMetadataNode child : getChildren()) {
            EAVTypeMetadataNode otherChild = other.resolve(child.getRelativePath(this));
            if(otherChild != null) {
                alreadyHandled.add(otherChild.getName());
            }
            if(child.isLeaf()) {
                if(otherChild != null && ! child.hasEqualPayloadAs(otherChild)) {
                    throw EAVErrorMessage.WRONG_TYPES.create(child.getPath().toString(), child.getTypeDescription(), otherChild.getTypeDescription());
                }
                node.addChild(TreeTools.copy(child));
            } else {
                EAVTypeMetadataNode toAdd = null;
                if(otherChild != null) {
                    toAdd = child.mergeWith(otherChild);
                } else {
                    toAdd = TreeTools.copy(child);
                }
                node.addChild(toAdd);
            }
        }
        
        for(EAVTypeMetadataNode child : other.getChildren()) {
            if(!alreadyHandled.contains(child.getName())) {
                node.addChild(child);
            }
        }
        
        return node;
    }


    public String getTypeDescription() {
        return type.name()+" "+containerType.name();
    }


    public boolean hasEqualPayloadAs(EAVTypeMetadataNode other) {
        return type == other.type && containerType == other.containerType;
    }


    public Map<String, Object> getOptions() {
        return options;
    }


   

}
