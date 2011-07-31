/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit http://creativecommons.org/licenses/publicdomain/ or
 * send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 *
 * Check HCard.java's top-level comment for more information.
 */
package org.microformats.hCard;

import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Represents one XFN relationship, such as 'friend'.
 * The usual suspects, such as 'ME', 'MET', 'FRIEND', etcetera are public static constants in this class.
 * You can create your own using the #create(String) method.
 *
 * XFNRelationship objects are <em>guaranteed</em> to be the same object if they have the same relationship name.
 * You can therefore safely use == to compare them, and not worry about re-using the same object; just create
 * to your hearts content, this class will take care of giving you the same XFNRelationship object back every time.
 *
 * @author Reinier Zwitserloot
 * @author Carlton Northern
 */
public final class XFNRelationship {
	private static final Map<XFNRelationship, WeakReference<XFNRelationship>> VALUES =
		new WeakHashMap<XFNRelationship, WeakReference<XFNRelationship>>();

	public static final XFNRelationship
		ME = create("me"),
		ACQUAINTANCE = create("acquaintance"), FRIEND = create("friend"),
		MET = create("met"),
		CO_WORKER = create("co-worker"), COLLEAGUE = create("colleague"),
		CO_RESIDENT = create("co-resident"), NEIGHBOR = create("neighbor"),
		CHILD = create("child"), PARENT = create("parent"), SIBLING = create("sibling"), SPOUSE = create("spouse"),
		MUSE = create("muse"), CRUSH = create("crush"), DATE = create("date"), SWEETHEART = create("sweetheart");

	private final String rel;

	private XFNRelationship(String rel) {
		this.rel = rel;
	}

	public String rel() {
		return rel;
	}

	@Override public int hashCode() {
		return rel.hashCode();
	}

	@Override public boolean equals(Object o) {
		if ( !(o instanceof XFNRelationship) ) {
            return false;
        }
		return ((XFNRelationship)o).rel.equals(rel);
	}

	@Override public String toString() {
		return rel;
	}

	@Override protected final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static XFNRelationship create(String rel) {
		if ( rel == null ) {
            throw new NullPointerException();
        }
		return lookup(new XFNRelationship(rel));
	}

	private Object readResolve() throws ObjectStreamException {
		return lookup(this);
	}

	private static XFNRelationship lookup(XFNRelationship xfn) {
		synchronized ( VALUES ) {
			WeakReference<XFNRelationship> weak = VALUES.get(xfn);
			XFNRelationship xfn2 = null;
			if ( weak != null ) {
                xfn2 = weak.get();
            }
			if ( xfn2 != null ) {
                return xfn2;
            }
			VALUES.put(xfn, new WeakReference<XFNRelationship>(xfn));
			return xfn;
		}
	}
}
