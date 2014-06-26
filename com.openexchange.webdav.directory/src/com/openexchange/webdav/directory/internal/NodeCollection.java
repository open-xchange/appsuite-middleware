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

package com.openexchange.webdav.directory.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.webdav.acl.mixins.CurrentUserPrincipal;
import com.openexchange.webdav.acl.mixins.PrincipalURL;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.helpers.AbstractCollection;


/**
 * {@link NodeCollection}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NodeCollection extends AbstractCollection {

    private final Node node;
    private final DirectoryWebdavFactory factory;
    private final WebdavPath url;

    public NodeCollection(Node node, DirectoryWebdavFactory factory, WebdavPath url) {
        super();
        this.node = node;
        this.factory = factory;
        this.url = url;
        includeProperties(new CurrentUserPrincipal(factory.getSessionHolder()), new PrincipalURL(factory.getSessionHolder().getUser()));
    }

    @Override
    protected WebdavFactory getFactory() {
        return factory;
    }

    @Override
    protected void internalDelete() {
        // Nothing to do

    }

    @Override
    protected List<WebdavProperty> internalGetAllProps() {
        return Collections.emptyList();
    }

    @Override
    protected WebdavProperty internalGetProperty(String namespace, String name) {
        return null;
    }

    @Override
    protected void internalPutProperty(WebdavProperty prop) {

    }

    @Override
    protected void internalRemoveProperty(String namespace, String name) {

    }

    @Override
    protected boolean isset(Property p) {
        return true;
    }

    @Override
    public void setCreationDate(Date date) {

    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<Node> childNodes = node.getChildren();
        List<WebdavResource> children = new ArrayList<WebdavResource>(childNodes.size());
        for (Node childNode : childNodes) {
            children.add(factory.resolveCollection(url.dup().append(childNode.getName())));
        }
        return children;
    }

    @Override
    public void create() {

    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public Date getCreationDate() {
        return new Date(0);
    }

    @Override
    public String getDisplayName() {
        return node.getName();
    }

    @Override
    public Date getLastModified() {
        return new Date(0);
    }

    @Override
    public WebdavLock getLock(String token) {
        return null;
    }

    @Override
    public List<WebdavLock> getLocks() {
        return Collections.emptyList();
    }

    @Override
    public WebdavLock getOwnLock(String token) {
        return null;
    }

    @Override
    public List<WebdavLock> getOwnLocks() {
        return Collections.emptyList();
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public WebdavPath getUrl() {
        return url;
    }

    @Override
    public void lock(WebdavLock lock) {

    }

    @Override
    public void save() {

    }

    @Override
    public void setDisplayName(String displayName) {

    }


    @Override
    public void unlock(String token) {

    }



}
