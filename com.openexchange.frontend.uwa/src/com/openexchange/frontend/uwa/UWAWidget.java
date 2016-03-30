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

package com.openexchange.frontend.uwa;

import static com.openexchange.java.Autoboxing.a2b;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.modules.model.AbstractModel;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.Metadata;
/**
 * {@link UWAWidget}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UWAWidget extends AbstractModel<UWAWidget> implements Serializable{

    private String adj;

    private boolean autorefresh, standalone, visible, prot;

    private String title, url, parameters, id;

    public void setADJ(String adj) {
        this.adj = adj;
    }

    public String getADJ() {
        return adj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAutorefresh() {
        return autorefresh;
    }

    public void setAutorefresh(boolean autorefresh) {
        this.autorefresh = autorefresh;
    }

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isProtected() {
        return prot;
    }

    public void setProtected(boolean prot) {
        this.prot = prot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public Metadata<UWAWidget> getMetadata() {
        return METADATA;
    }

    public enum Field implements Attribute<UWAWidget> {
        ADJ(String.class),
        AUTOREFRESH(Boolean.class),
        ID(String.class),
        STANDALONE(Boolean.class),
        TITLE(String.class),
        URL(String.class),
        VISIBLE(Boolean.class),
        PROTECTED(Boolean.class),
        PARAMETERS(String.class);

        private final Class type;

        Field(Class type) {
            this.type = type;
        }

        @Override
        public Object get(UWAWidget thing) {
            switch(this) {
            case ADJ: return thing.getADJ();
            case AUTOREFRESH: return thing.isAutorefresh();
            case ID: return thing.getId();
            case STANDALONE: return thing.isStandalone();
            case TITLE: return thing.getTitle();
            case VISIBLE: return thing.isVisible();
            case URL: return thing.getURL();
            case PROTECTED: return thing.isProtected();
            case PARAMETERS: return thing.getParameters();
            }
            return null;
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }

        private static final EnumSet<Field> NULLABLE = EnumSet.of(ID, TITLE, URL, PARAMETERS);

        @Override
        public void set(UWAWidget thing, Object value) {
            if(value == null && ! NULLABLE.contains(this)) {
                return ;
            }
            switch(this) {
            case ADJ: thing.setADJ((String) value); break;
            case AUTOREFRESH:  thing.setAutorefresh(a2b(value)); break;
            case ID:  thing.setId((String) value); break;
            case STANDALONE:  thing.setStandalone(a2b(value)); break;
            case TITLE:  thing.setTitle((String) value); break;
            case VISIBLE:  thing.setVisible(a2b(value)); break;
            case URL:  thing.setURL((String) value); break;
            case PROTECTED:  thing.setProtected(a2b(value)); break;
            case PARAMETERS:  thing.setParameters((String) value); break;
            }
        }

        public static List<Attribute<UWAWidget>> toAttributes(List<Field> fields) {
            List<Attribute<UWAWidget>> attributes = new ArrayList<Attribute<UWAWidget>>(fields.size());
            for (Field field : fields) {
                attributes.add(field);
            }
            return attributes;
        }

        @Override
        public Class getType() {
            return type;
        }
    }

    public static UWAWidgetMetadata METADATA = new UWAWidgetMetadata();

    private static final class UWAWidgetMetadata implements Metadata<UWAWidget> {

        private final List<Attribute<UWAWidget>> allFields;
        private final List<Attribute<UWAWidget>> persistentFields;

        public UWAWidgetMetadata() {
            allFields = new LinkedList<Attribute<UWAWidget>>();
            for(Field f : Field.values()) {
                allFields.add(f);
            }

            persistentFields = new LinkedList<Attribute<UWAWidget>>();

            for(Field f : EnumSet.complementOf(EnumSet.of(Field.PROTECTED))) {
                persistentFields.add(f);
            }
        }

        @Override
        public UWAWidget create() {
            return new UWAWidget();
        }

        @Override
        public List<Attribute<UWAWidget>> getAllFields() {
            return allFields;
        }

        @Override
        public Attribute<UWAWidget> getIdField() {
            return Field.ID;
        }

        @Override
        public String getName() {
            return "uwaWidget";
        }

        @Override
        public List<Attribute<UWAWidget>> getPersistentFields() {
            return persistentFields;
        }

    }

}
