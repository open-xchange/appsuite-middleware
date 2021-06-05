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

package com.openexchange.messaging;

/**
 * {@link MessagingPermissions} - Tools for {@link MessagingPermission} class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MessagingPermissions {

    /**
     * Initializes a new {@link MessagingPermissions}.
     */
    private MessagingPermissions() {
        super();
    }

    /**
     * Gets an unmodifiable view of the specified permission. Attempts to modify the returned permission result in an
     * <tt>UnsupportedOperationException</tt>.
     *
     * @param messagingPermission The messaging permission
     * @return An unmodifiable view of the specified permission
     */
    public static MessagingPermission unmodifiablePermission(final MessagingPermission messagingPermission) {
        return new UnmodifiableMessagingPermission(messagingPermission);
    }

    static final class UnmodifiableMessagingPermission implements MessagingPermission {

        private static final long serialVersionUID = 5026003840275420722L;
        
        private MessagingPermission delegate;

        UnmodifiableMessagingPermission(final MessagingPermission delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public Object clone() {
            try {
                final UnmodifiableMessagingPermission clone = (UnmodifiableMessagingPermission) super.clone();
                clone.delegate = (MessagingPermission) (null == delegate ? null : delegate.clone());
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }
        
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public int getDeletePermission() {
            return delegate.getDeletePermission();
        }

        @Override
        public int getEntity() {
            return delegate.getEntity();
        }

        @Override
        public int getFolderPermission() {
            return delegate.getFolderPermission();
        }

        @Override
        public int getReadPermission() {
            return delegate.getReadPermission();
        }

        @Override
        public int getSystem() {
            return delegate.getSystem();
        }

        @Override
        public int getWritePermission() {
            return delegate.getWritePermission();
        }

        @Override
        public boolean isAdmin() {
            return delegate.isAdmin();
        }

        @Override
        public boolean isGroup() {
            return delegate.isGroup();
        }

        @Override
        public void setAdmin(final boolean admin) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setAdmin()");
        }

        @Override
        public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setAllPermissions()");
        }

        @Override
        public void setDeletePermission(final int permission) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setDeletePermission()");
        }

        @Override
        public void setEntity(final int entity) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setEntity()");
        }

        @Override
        public void setFolderPermission(final int permission) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setFolderPermission()");
        }

        @Override
        public void setGroup(final boolean group) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setGroup()");
        }

        @Override
        public void setMaxPermissions() {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setMaxPermissions()");
        }

        @Override
        public void setNoPermissions() {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setNoPermissions()");
        }

        @Override
        public void setReadPermission(final int permission) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setReadPermission()");
        }

        @Override
        public void setSystem(final int system) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setSystem()");
        }

        @Override
        public void setWritePermission(final int permission) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setWritePermission()");
        }

        @Override
        public MessagingFolderPermissionType getType() {
            return delegate.getType();
        }

        @Override
        public void setType(MessagingFolderPermissionType type) {
            throw new UnsupportedOperationException("MessagingPermissions.UnmodifiableMessagingPermission.setType()");
        }

    } // End of UnmodifiableMessagingPermission

}
