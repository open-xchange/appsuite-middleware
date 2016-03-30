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

package com.openexchange.publish;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link Publication}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Publication {

    private int userId;

    private Context context;

    private int id;

    private String entityId;

    private String module;

    private PublicationTarget target;

    private Map<String, Object> configuration = new HashMap<String, Object>();

    private String displayName;

    private Boolean enabled = null;

    private long created = 0;


    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }


    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
        return created;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public boolean containsEntityId() {
        return getEntityId() != null;
    }

    public String getModule() {
        return module;
    }

    public void setModule(final String module) {
        this.module = module;
    }

    public boolean containsModule() {
        return getModule() != null;
    }

    public PublicationTarget getTarget() {
        return target;
    }

    public void setTarget(final PublicationTarget target) {
        this.target = target;
    }

    public boolean containsTarget() {
        return getTarget() != null;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public boolean containsUserId() {
        return this.getUserId() > 0;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public boolean isEnabled() {
        final Boolean enabled = this.enabled;
        return enabled != null ? enabled.booleanValue() : true;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = Boolean.valueOf(enabled);
    }

    public boolean containsEnabled() {
        return enabled != null;
    }

    public void create() throws OXException {
        final PublicationTarget target = getTarget();
        if (null != target) {
            final PublicationService service = target.getPublicationService();
            if (null != service) {
                service.create(this);
            }
        }
    }

    public void update() throws OXException {
        final PublicationTarget target = getTarget();
        if (null != target) {
            final PublicationService service = target.getPublicationService();
            if (null != service) {
                service.update(this);
            }
        }
    }

    public void destroy() throws OXException {
        final PublicationTarget target = getTarget();
        if (null != target) {
            final PublicationService service = target.getPublicationService();
            if (null != service) {
                service.delete(this);
            }
        }
    }

}
