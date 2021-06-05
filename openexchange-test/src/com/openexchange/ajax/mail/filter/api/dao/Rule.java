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

package com.openexchange.ajax.mail.filter.api.dao;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.action.Action;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.ActionArgument;
import com.openexchange.ajax.mail.filter.api.dao.test.Test;

/**
 * {@link Rule}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Rule {

    public static final int ID = 1200;

    public static final int RULENAME = 1201;

    public static final int ACTIVE = 1202;

    public static final int FLAGS = 1203;

    public static final int POSITION = 1204;

    public static final int TEST = 1205;

    public static final int ACTIONCMDS = 1206;

    public static final int TEXT = 1207;

    protected int position = -1;

    protected int id = -1;

    protected String name = null;

    protected boolean active = false;

    protected String[] flags = new String[0];

    protected Test<?> test = null;

    protected List<Action<? extends ActionArgument>> actions;

    protected String rawData = null;

    protected String errormsg = null;

    public Rule() {
        actions = new LinkedList<>();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String[] getFlags() {
        return flags;
    }

    public void setFlags(String[] flags) {
        this.flags = flags;
    }

    public Test<?> getTest() {
        return test;
    }

    public void setTest(Test<?> test) {
        this.test = test;
    }

    public List<Action<? extends ActionArgument>> getActions() {
        return actions;
    }

    public void setActions(List<Action<? extends ActionArgument>> actions) {
        this.actions = actions;
    }

    public void addAction(Action<? extends ActionArgument> action) {
        actions.add(action);
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    @Override
    public String toString() {
        return "Rule [position=" + position + ", id=" + id + ", name=" + name + "]";
    }
}
