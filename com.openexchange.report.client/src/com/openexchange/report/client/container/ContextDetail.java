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

package com.openexchange.report.client.container;

import java.util.ArrayList;
import java.util.List;

public class ContextDetail {

    private String id;

    private String users;

    private String age;

    private String created;

    private String mappings;

    private String adminmac;

    private List<ContextModuleAccessCombination> moduleAccessCombinations;

    public ContextDetail() {
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public ContextDetail(final String id, final String users, final String age, final String created, final String mappings, final String adminmac) {
        this.id = id;
        this.users = users;
        this.age = age;
        this.created = created;
        this.mappings = mappings;
        this.adminmac = adminmac;
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public ContextDetail(final String id, final String users, final String age, final String created, final String mappings, final String adminmac, final List<ContextModuleAccessCombination> moduleAccessCombinations) {
        this.id = id;
        this.users = users;
        this.age = age;
        this.created = created;
        this.mappings = mappings;
        this.adminmac = adminmac;
        this.moduleAccessCombinations = moduleAccessCombinations;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(final String users) {
        this.users = users;
    }

    public String getAge() {
        return age;
    }

    public void setAge(final String age) {
        this.age = age;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getMappings() {
        return mappings;
    }

    public void setMappings(final String mappings) {
        this.mappings = mappings;
    }

    public String getAdminmac() {
        return adminmac;
    }

    public void setAdminmac(final String adminmac) {
        this.adminmac = adminmac;
    }

    public List<ContextModuleAccessCombination> getModuleAccessCombinations() {
        return moduleAccessCombinations;
    }

    public void setModuleAccessCombinations(final List<ContextModuleAccessCombination> moduleAccessCombinations) {
        this.moduleAccessCombinations = moduleAccessCombinations;
    }

    public void clearModuleAccessCombinations() {
        this.moduleAccessCombinations = new ArrayList<ContextModuleAccessCombination>();
    }

    public void addModuleAccessCombination(final ContextModuleAccessCombination moduleAccessCombination) {
        moduleAccessCombinations.add(moduleAccessCombination);
    }

    public ContextModuleAccessCombination getModuleAccessCombination(final int position) {
        return moduleAccessCombinations.get(position);
    }

    public void setModuleAccessCombination(final ContextModuleAccessCombination moduleAccessCombination, final int position) {
        moduleAccessCombinations.set(position, moduleAccessCombination);
    }

    public void removeModuleAccessCombination(final int position) {
        moduleAccessCombinations.remove(position);
    }

    @Override
    public String toString() {
        return "ContextDetailObject [age=" + age + ", created=" + created + ", id=" + id + ", mappings=" + mappings + ", moduleAccessCombinations=" + moduleAccessCombinations + ", users=" + users + "]";
    }

}
