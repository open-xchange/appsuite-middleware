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


package com.openexchange.user.copy.internal.mailaccount;

/**
 * {@link GenericAccountData}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class GenericAccountData {
    protected int id;
    protected int cid;
    protected int user;
    protected String name;
    protected String url;
    protected String login;
    protected String password;
    protected String primaryAddr;
    protected String personal;
    protected String replyTo;
    protected boolean defaultFlag;
    protected boolean unifiedInbox;

    protected GenericAccountData() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(final int cid) {
        this.cid = cid;
    }

    public int getUser() {
        return user;
    }

    public void setUser(final int user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPrimaryAddr() {
        return primaryAddr;
    }

    public void setPrimaryAddr(final String primaryAddr) {
        this.primaryAddr = primaryAddr;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(final String personal) {
        this.personal = personal;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
    }

    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(final boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    public boolean isUnifiedInbox() {
        return unifiedInbox;
    }

    public void setUnifiedInbox(final boolean unifiedInbox) {
        this.unifiedInbox = unifiedInbox;
    }

}
