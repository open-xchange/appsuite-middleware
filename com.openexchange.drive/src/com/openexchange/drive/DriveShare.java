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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.drive;

import java.util.Date;
import java.util.Map;
import com.openexchange.share.Share;
import com.openexchange.share.ShareTarget;

/**
 * {@link DriveShare}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class DriveShare extends Share {

    private static final long serialVersionUID = -5781164235991072065L;

    private DriveShareTarget target;
    private Share delegate;

    /**
     * Initializes a new {@link DriveShare}.
     *
     * @param share The underlying share
     * @param target The corresponding drive share target
     */
    public DriveShare(Share share, DriveShareTarget target) {
        super(share.getGuest(), share.getTarget());
        this.delegate = share;
        this.target = target;
    }

    @Override
    public int getGuest() {
        return delegate.getGuest();
    }

    @Override
    public DriveShareTarget getTarget() {
        return target;
    }

    public void setTarget(DriveShareTarget target) {
        this.target = target;
    }

    @Override
    public void setTarget(ShareTarget target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public void setCreatedBy(int sharedBy) {
        delegate.setCreatedBy(sharedBy);
    }

    @Override
    public void setGuest(int guest) {
        delegate.setGuest(guest);
    }

    @Override
    public Date getCreated() {
        return delegate.getCreated();
    }

    @Override
    public void setCreated(Date created) {
        delegate.setCreated(created);
    }

    @Override
    public Date getModified() {
        return delegate.getModified();
    }

    @Override
    public void setModified(Date modified) {
        delegate.setModified(modified);
    }

    @Override
    public int getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public void setModifiedBy(int modifiedBy) {
        delegate.setModifiedBy(modifiedBy);
    }

    @Override
    public Date getExpiryDate() {
        return delegate.getExpiryDate();
    }

    @Override
    public void setExpiryDate(Date expiryDate) {
        delegate.setExpiryDate(expiryDate);
    }

    @Override
    public boolean isExpired() {
        return delegate.isExpired();
    }

    @Override
    public boolean containsExpiryDate() {
        return delegate.containsExpiryDate();
    }

    @Override
    public Map<String, Object> getMeta() {
        return delegate.getMeta();
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
        delegate.setMeta(meta);
    }

    @Override
    public boolean containsMeta() {
        return delegate.containsMeta();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
