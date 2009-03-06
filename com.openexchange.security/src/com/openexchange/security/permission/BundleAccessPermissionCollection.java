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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.security.permission;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link BundleAccessPermissionCollection} - Stores a collection of
 * {@link BundleAccessPermission} permissions. {@link BundleAccessPermission}
 * objects must be stored in a manner that allows them to be inserted in any
 * order, but enable the implies function to evaluate the implies method in an
 * efficient (and consistent) manner.
 * 
 * A {@link BundleAccessPermissionCollection} handles comparing a permission
 * like "a.b.c.d.e" with a Permission such as "a.b.*", or "*".
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class BundleAccessPermissionCollection extends PermissionCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1058822504713725539L;

	/**
	 * Key is name, value is permission. All permission objects in collection
	 * must be of the same type. Not serialized; see serialization section at
	 * end of class.
	 */
	private transient Map<String, Permission> perms;

	/**
	 * This is set to <code>true</code> if this BundleAccessPermissionCollection
	 * contains a permission with '*' as its permission name.
	 * 
	 * @see #serialPersistentFields
	 */
	private boolean all_allowed;

	/**
	 * The class to which all BundleAccessPermission in this
	 * BundleAccessPermissionCollection belongs.
	 * 
	 * @see #serialPersistentFields
	 */
	private Class<? extends Permission> permClass;

	/**
	 * Create an empty {@link BundleAccessPermissionCollection}.
	 */
	public BundleAccessPermissionCollection() {
		perms = new ConcurrentHashMap<String, Permission>(11);
		all_allowed = false;
	}

	/**
	 * Adds a permission to the BundleAccessPermission. The key for the hash is
	 * permission.path.
	 * 
	 * @param permission
	 *            the Permission object to add.
	 * 
	 * @exception IllegalArgumentException
	 *                If the permission is not a BundleAccessPermission, or if
	 *                the permission is not of the same Class as the other
	 *                permissions in this collection.
	 * 
	 * @exception SecurityException
	 *                If this BundleAccessPermissionCollection object has been
	 *                marked read-only
	 */
	@Override
	public void add(final Permission permission) {
		if (!(permission instanceof BundleAccessPermission)) {
			throw new IllegalArgumentException("Invalid permission: " + permission);
		}
		if (isReadOnly()) {
			throw new SecurityException("Attempt to add a permission to a read-only permission collection");
		}
		final BundleAccessPermission bp = (BundleAccessPermission) permission;
		if (perms.isEmpty()) {
			permClass = bp.getClass();
		} else {
			if (bp.getClass() != permClass) {
				throw new IllegalArgumentException("Invalid permission: " + permission);
			}
		}
		perms.put(bp.getName(), bp);
		if (!all_allowed) {
			final String path = bp.getName();
			all_allowed = (path.length() == 1 && path.charAt(0) == '*');
		}
	}

	/**
	 * Check if this set of permissions implies the specified permission
	 * 
	 * @param p
	 *            The permission to compare
	 * 
	 * @return <code>true</code> if permission is a proper subset of a
	 *         permission in the collection, <code>false</code> if not.
	 */

	@Override
	public boolean implies(final Permission permission) {
		if (!(permission instanceof BundleAccessPermission)) {
			return false;
		}
		final BundleAccessPermission bp = (BundleAccessPermission) permission;
		/*
		 * Random subclasses of BundleAccessPermission do not imply each other
		 */
		if (bp.getClass() != permClass) {
			return false;
		}
		/*
		 * Short circuit if the "*" Permission was added
		 */
		if (all_allowed) {
			return true;
		}
		/*
		 * Strategy: Check for full match first. Then work our way up the path
		 * looking for matches on a.b..
		 */
		String path = bp.getName();
		Permission x = perms.get(path);
		if (x != null) {
			/*
			 * Direct hit
			 */
			return x.implies(permission);
		}
		/*
		 * Work our way up the tree...
		 */
		int last, offset;
		offset = path.length() - 1;
		while ((last = path.lastIndexOf('.', offset)) != -1) {
			path = path.substring(0, last + 1) + '*';
			x = perms.get(path);
			if (x != null) {
				return x.implies(permission);
			}
			offset = last - 1;
		}
		/*
		 * Don't check for "*" as it was already checked at the top
		 * (all_allowed), so we just return false
		 */
		return false;
	}

	/**
	 * Returns an {@link Enumeration enumeration} of all the
	 * {@link BundleAccessPermission} objects in the container.
	 * 
	 * @return an {@link Enumeration enumeration} of all the
	 *         {@link BundleAccessPermission} objects.
	 */
	@Override
	public Enumeration<Permission> elements() {
		return Collections.enumeration(perms.values());
	}

	// Need to maintain serialization interoperability with earlier releases,
	// which had the serializable field:
	//
	// @serial the Hashtable is indexed by the BundleAccessPermission name
	//
	// private Hashtable permissions;
	/**
	 * @serialField permissions java.util.Hashtable The BundleAccessPermissions
	 *              in this BundleAccessPermissionCollection. All
	 *              BundleAccessPermissions in the collection must belong to the
	 *              same class. The {@link Hashtable hashtable} is indexed by
	 *              the BundleAccessPermission name; the value of the The
	 *              {@link Hashtable hashtable} entry is the permission.
	 * @serialField all_allowed boolean This is set to <code>true</code> if this
	 *              BundleAccessPermissionCollection contains a
	 *              BundleAccessPermission with '*' as its permission name.
	 * @serialField permClass java.lang.Class The class to which all
	 *              BundleAccessPermissions in this
	 *              BundleAccessPermissionCollection belongs.
	 */
	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField("permissions", Hashtable.class), new ObjectStreamField("all_allowed", Boolean.TYPE),
			new ObjectStreamField("permClass", Class.class), };

	/**
	 * @serialData Default fields.
	 */
	/*
	 * Writes the contents of the perms field out as a Hashtable for
	 * serialization compatibility with earlier releases. all_allowed and
	 * permClass unchanged.
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException {
		// Don't call out.defaultWriteObject()

		// Copy perms into a Hashtable
		final Hashtable<String, Permission> permissions = new Hashtable<String, Permission>(perms.size() << 1);
		permissions.putAll(perms);

		// Write out serializable fields
		final ObjectOutputStream.PutField pfields = out.putFields();
		pfields.put("all_allowed", all_allowed);
		pfields.put("permissions", permissions);
		pfields.put("permClass", permClass);
		out.writeFields();
	}

	/**
	 * readObject is called to restore the state of the
	 * BundleAccessPermissionCollection from a stream.
	 */
	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		// Don't call defaultReadObject()

		// Read in serialized fields
		final ObjectInputStream.GetField gfields = in.readFields();

		// Get permissions
		final Hashtable<String, Permission> permissions = (Hashtable<String, Permission>) gfields.get("permissions",
				null);
		perms = new ConcurrentHashMap<String, Permission>(permissions.size() << 1);
		perms.putAll(permissions);

		// Get all_allowed
		all_allowed = gfields.get("all_allowed", false);

		// Get permClass
		permClass = (Class<? extends Permission>) gfields.get("permClass", null);

		if (permClass == null) {
			// set permClass
			final Enumeration<Permission> e = permissions.elements();
			if (e.hasMoreElements()) {
				final Permission p = e.nextElement();
				permClass = p.getClass();
			}
		}
	}

}
