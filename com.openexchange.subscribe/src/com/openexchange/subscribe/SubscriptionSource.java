/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.subscribe;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;


/**
 * {@link SubscriptionSource}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionSource {

    private String id;
    private String displayName;
    private boolean localizableDisplayName = false;
    private String icon;
    private DynamicFormDescription formDescription;
    private SubscribeService subscribeService;
    private int folderModule = 0;
    private int priority = 0;

    /**
     * Initializes a new {@link SubscriptionSource}.
     */
    public SubscriptionSource() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the display name. If {@link SubscriptionSource#isLocalizableDisplayName()} returns true,
     * it should be translated before returning it to the client.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name. If it is localizable you also need to call {@link SubscriptionSource#setLocalizableDisplayName()}.
     *
     * @param displayName The display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return whether the display name should be translated before returning it to the client.
     */
    public boolean isLocalizableDisplayName() {
        return localizableDisplayName;
    }

    /**
     * If called, the display name will be translated before returning it to the client.
     */
    public void setLocalizableDisplayName() {
        localizableDisplayName = true;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    public void setFormDescription(DynamicFormDescription formDescription) {
        this.formDescription = formDescription;
    }

    public SubscribeService getSubscribeService() {
        return subscribeService;
    }

    public void setSubscribeService(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }

    public void setFolderModule(int folderModule) {
        this.folderModule = folderModule;
    }

    public int getFolderModule() {
        return folderModule;
    }

    public int getPriority() {
        return priority;
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<String> getPasswordFields() {
        Set<String> fields = new HashSet<String>();
        for (FormElement element : getFormDescription()) {
            if (element.getWidget() == FormElement.Widget.PASSWORD) {
                fields.add(element.getName());
            }
        }
        return fields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubscriptionSource other = (SubscriptionSource) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
