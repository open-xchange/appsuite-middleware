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

package com.openexchange.onboarding;


/**
 * {@link OnboardingSelectionKey} - A key for an on-boarding selection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingSelectionKey {

    /**
     * Creates a new key instance using given arguments
     *
     * @param entityPath The entity path
     * @param type The type
     * @return The key instance
     */
    public static OnboardingSelectionKey keyFor(EntityPath entityPath, OnboardingType type) {
        return new OnboardingSelectionKey(entityPath, type);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final Device device;
    private final Module module;
    private final String serviceId;
    private final OnboardingType type;
    private final int hash;

    /**
     * Initializes a new {@link OnboardingSelectionKey}.
     */
    public OnboardingSelectionKey(OnboardingSelection selection) {
        super();
        EntityPath entityPath = selection.getEntityPath();
        this.device = entityPath.getDevice();
        this.module = entityPath.getModule();
        this.serviceId = entityPath.getService().getId();
        this.type = selection.getType();

        int prime = 31;
        int result = prime * 1 + ((device == null) ? 0 : device.hashCode());
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hash = result;
    }

    /**
     * Initializes a new {@link OnboardingSelectionKey}.
     */
    public OnboardingSelectionKey(EntityPath entityPath, OnboardingType type) {
        super();
        this.device = entityPath.getDevice();
        this.module = entityPath.getModule();
        this.serviceId = entityPath.getService().getId();
        this.type = type;

        int prime = 31;
        int result = prime * 1 + ((device == null) ? 0 : device.hashCode());
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OnboardingSelectionKey)) {
            return false;
        }
        OnboardingSelectionKey other = (OnboardingSelectionKey) obj;
        if (device != other.device) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        return true;
    }

}
