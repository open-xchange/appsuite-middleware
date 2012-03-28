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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.frontend.uwa.rmi;

import static com.openexchange.java.Autoboxing.a2b;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.modules.model.AbstractModel;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Metadata;


/**
 * {@link Widget}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Widget extends AbstractModel<Widget> implements Serializable{
    public String id;
    public String title;
    public String url;
    public Map<String, String> parameters = new HashMap<String, String>();
    
    public boolean autorefresh;
    public boolean standalone;
    public boolean visible;
    
    // Change tracking
    
    private Set<Field> modified = new HashSet<Field>();
    private boolean parametersModified;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
        modified.add(Field.ID);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        modified.add(Field.TITLE);
    }
    
    public String getURL() {
        return url;
    }
    
    public void setURL(String url) {
        this.url = url;
        modified.add(Field.URL);
    }
    
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public void setParameter(String name, String value) {
        this.parameters.put(name, value);
        parametersModified = true;
    }
    
    public boolean getAutorefresh() {
        return autorefresh;
    }
    
    public void setAutorefresh(boolean autorefresh) {
        this.autorefresh = autorefresh;
        modified.add(Field.AUTOREFRESH);
    }
    
    public boolean isStandalone() {
        return standalone;
    }
    
    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
        modified.add(Field.STANDALONE);
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        modified.add(Field.VISIBLE);
    }

    // Change Tracking
    
    public boolean isIdModified() {
        return modified.contains(Field.ID);
    }

    
    public boolean isTitleModified() {
        return modified.contains(Field.TITLE);
    }

    
    public boolean isURLModified() {
        return modified.contains(Field.URL);
    }

    
    public boolean areParametersModified() {
        return parametersModified;
    }

    
    public boolean isAutorefreshModified() {
        return modified.contains(Field.AUTOREFRESH);
    }

    
    public boolean isStandaloneModified() {
        return modified.contains(Field.STANDALONE);
    }

    
    public boolean isVisibleModified() {
        return modified.contains(Field.VISIBLE);
    }
    
    
    public Set<Field> getModified() {
        return modified;
    }

    public Metadata<Widget> getMetadata() {
        return METADATA;
    }
    
    
    
    // Modelling
    
    public enum Field implements Attribute<Widget> {
        AUTOREFRESH(Boolean.class),
        VISIBLE(Boolean.class),
        ID(String.class),
        STANDALONE(Boolean.class),
        TITLE(String.class),
        URL(String.class);

        private Class type;
        
        Field(Class type) {
            this.type = type;
        }
        
        public Object get(Widget thing) {
            switch(this) {
            case AUTOREFRESH: return thing.getAutorefresh();
            case ID: return thing.getId();
            case STANDALONE: return thing.isStandalone();
            case TITLE: return thing.getTitle();
            case URL: return thing.getURL();
            case VISIBLE: return thing.isVisible();
            }
            return null;
        }

        public String getName() {
            return this.name().toLowerCase();
        }
        
        private static final EnumSet<Field> NULLABLE = EnumSet.of(ID, TITLE, URL);

        public void set(Widget thing, Object value) {
            if(value == null && ! NULLABLE.contains(this)) {
                return ;
            }
            switch(this) {
            case AUTOREFRESH:  thing.setAutorefresh(a2b(value)); break;
            case VISIBLE: thing.setVisible(a2b(value)); break;
            case ID:  thing.setId((String) value); break;
            case STANDALONE:  thing.setStandalone(a2b(value)); break;
            case TITLE:  thing.setTitle((String) value); break;
            case URL:  thing.setURL((String) value); break;
            }
        }
        
        public Class getType() {
            return type;
        }
    }
    
    public static WidgetMetadata METADATA = new WidgetMetadata();

    private static final class WidgetMetadata implements Metadata<Widget> {

        private List<Attribute<Widget>> allFields; 
        
        public WidgetMetadata() {
            allFields = new LinkedList<Attribute<Widget>>();
            for(Field f : Field.values()) {
                allFields.add(f);
            }
        }
        
        public Widget create() {
            return new Widget();
        }

        public List<Attribute<Widget>> getAllFields() {
            return allFields;
        }

        public Attribute<Widget> getIdField() {
            return Field.ID;
        }

        public String getName() {
            return "uwaWidget";
        }

        public List<Attribute<Widget>> getPersistentFields() {
            return allFields;
        }

    }
    
    
    
    
}
