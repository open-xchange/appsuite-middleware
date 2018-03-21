/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.mail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Flags class represents the set of flags on a Message.  Flags
 * are composed of predefined system flags, and user defined flags. <p>
 *
 * A System flag is represented by the <code>Flags.Flag</code> 
 * inner class. A User defined flag is represented as a String.
 * User flags are case-independent. <p>
 *
 * A set of standard system flags are predefined.  Most folder
 * implementations are expected to support these flags.  Some
 * implementations may also support arbitrary user-defined flags.  The
 * <code>getPermanentFlags</code> method on a Folder returns a Flags
 * object that holds all the flags that are supported by that folder
 * implementation. <p>
 *
 * A Flags object is serializable so that (for example) the
 * use of Flags objects in search terms can be serialized
 * along with the search terms. <p>
 *
 * <strong>Warning:</strong>
 * Serialized objects of this class may not be compatible with future
 * JavaMail API releases.  The current serialization support is
 * appropriate for short term storage. <p>
 *
 * The below code sample illustrates how to set, examine, and get the 
 * flags for a message.
 * <pre>
 *
 * Message m = folder.getMessage(1);
 * m.setFlag(Flags.Flag.DELETED, true); // set the DELETED flag
 *
 * // Check if DELETED flag is set on this message
 * if (m.isSet(Flags.Flag.DELETED))
 *	System.out.println("DELETED message");
 *
 * // Examine ALL system flags for this message
 * Flags flags = m.getFlags();
 * Flags.Flag[] sf = flags.getSystemFlags();
 * for (int i = 0; i &lt; sf.length; i++) {
 *	if (sf[i] == Flags.Flag.DELETED)
 *            System.out.println("DELETED message");
 *	else if (sf[i] == Flags.Flag.SEEN)
 *            System.out.println("SEEN message");
 *      ......
 *      ......
 * }
 * </pre>
 * <p>
 *
 * @see    Folder#getPermanentFlags
 * @author John Mani
 * @author Bill Shannon
 */

public class Flags implements Cloneable, Serializable {

    private int system_flags = 0;
    // used as a case-independent Set that preserves the original case,
    // the key is the lowercase flag name and the value is the original
    private HashMap<String, String> user_flags = null;

    private final static int ANSWERED_BIT 	= 0x01;
    private final static int DELETED_BIT 	= 0x02;
    private final static int DRAFT_BIT 		= 0x04;
    private final static int FLAGGED_BIT 	= 0x08;
    private final static int RECENT_BIT		= 0x10;
    private final static int SEEN_BIT		= 0x20;
    private final static int USER_BIT		= 0x80000000;

    private static final long serialVersionUID = 6243590407214169028L;

    /**
     * This inner class represents an individual system flag. A set
     * of standard system flag objects are predefined here.
     */
    public static final class Flag {
	/**
	 * This message has been answered. This flag is set by clients 
	 * to indicate that this message has been answered to.
	 */
	public static final Flag ANSWERED = new Flag(ANSWERED_BIT);

	/**
	 * This message is marked deleted. Clients set this flag to
	 * mark a message as deleted. The expunge operation on a folder
	 * removes all messages in that folder that are marked for deletion.
	 */
	public static final Flag DELETED = new Flag(DELETED_BIT);

	/**
	 * This message is a draft. This flag is set by clients
	 * to indicate that the message is a draft message.
	 */
	public static final Flag DRAFT = new Flag(DRAFT_BIT);

	/**
	 * This message is flagged. No semantic is defined for this flag.
	 * Clients alter this flag.
	 */
	public static final Flag FLAGGED = new Flag(FLAGGED_BIT);

	/**
	 * This message is recent. Folder implementations set this flag
	 * to indicate that this message is new to this folder, that is,
	 * it has arrived since the last time this folder was opened. <p>
	 *
	 * Clients cannot alter this flag.
	 */
	public static final Flag RECENT = new Flag(RECENT_BIT);

	/**
	 * This message is seen. This flag is implicitly set by the 
	 * implementation when this Message's content is returned 
	 * to the client in some form. The <code>getInputStream</code>
	 * and <code>getContent</code> methods on Message cause this
	 * flag to be set. <p>
	 *
	 * Clients can alter this flag.
	 */
	public static final Flag SEEN = new Flag(SEEN_BIT);

	/**
	 * A special flag that indicates that this folder supports
	 * user defined flags. <p>
	 *
	 * The implementation sets this flag. Clients cannot alter 
	 * this flag but can use it to determine if a folder supports
	 * user defined flags by using
	 * <code>folder.getPermanentFlags().contains(Flags.Flag.USER)</code>.
	 */
	public static final Flag USER = new Flag(USER_BIT);

	// flags are stored as bits for efficiency
	private int bit;
	private Flag(int bit) {
	    this.bit = bit;
	}
    }


    /**
     * Construct an empty Flags object.
     */
    public Flags() { }

    /**
     * Construct a Flags object initialized with the given flags.
     *
     * @param flags	the flags for initialization
     */
    @SuppressWarnings("unchecked")
    public Flags(Flags flags) {
	this.system_flags = flags.system_flags;
	if (flags.user_flags != null)
	    this.user_flags = (HashMap<String, String>)flags.user_flags.clone();
    }

    /**
     * Construct a Flags object initialized with the given system flag.
     *
     * @param flag	the flag for initialization
     */
    public Flags(Flag flag) {
	this.system_flags |= flag.bit;
    }

    /**
     * Construct a Flags object initialized with the given user flag.
     *
     * @param flag	the flag for initialization
     */
    public Flags(String flag) {
	user_flags = new HashMap<String, String>(1);
	user_flags.put(asciiLowerCase(flag), flag);
    }

    /**
     * Add the specified system flag to this Flags object.
     *
     * @param flag	the flag to add
     */
    public void add(Flag flag) {
	system_flags |= flag.bit;
    }

    /**
     * Add the specified user flag to this Flags object.
     *
     * @param flag	the flag to add
     */
    public void add(String flag) {
	if (user_flags == null)
	    user_flags = new HashMap<String, String>(1);
	user_flags.put(asciiLowerCase(flag), flag);
    }

    /**
     * Add all the flags in the given Flags object to this
     * Flags object.
     *
     * @param f	Flags object
     */
    public void add(Flags f) {
	system_flags |= f.system_flags; // add system flags

	HashMap<String, String> otherUserFlags = f.user_flags;
    if (otherUserFlags != null) { // add user-defined flags
	    if (user_flags == null) {
	        user_flags = new HashMap<String, String>(otherUserFlags);
	    } else {	        
	        for (Map.Entry<String,String> entry : otherUserFlags.entrySet()) {
	            user_flags.put(entry.getKey(), entry.getValue());
	        }
	    }
	}
    }

    /**
     * Remove the specified system flag from this Flags object.
     *
     * @param	flag 	the flag to be removed
     */
    public void remove(Flag flag) {
	system_flags &= ~flag.bit;
    }

    /**
     * Remove the specified user flag from this Flags object.
     *
     * @param	flag 	the flag to be removed
     */
    public void remove(String flag) {
	if (user_flags != null)
	    user_flags.remove(asciiLowerCase(flag));
    }

    /**
     * Remove all flags in the given Flags object from this 
     * Flags object.
     *
     * @param	f 	the flag to be removed
     */
    public void remove(Flags f) {
	system_flags &= ~f.system_flags; // remove system flags

	HashMap<String, String> otherUserFlags = f.user_flags;
    if (otherUserFlags != null && user_flags != null) {
	    for (String key : otherUserFlags.keySet()) {
	        user_flags.remove(key);
	    }
	}
    }

    /**
     * Remove any flags <strong>not</strong> in the given Flags object.
     * Useful for clearing flags not supported by a server.  If the
     * given Flags object includes the Flags.Flag.USER flag, all user
     * flags in this Flags object are retained.
     *
     * @param	f	the flags to keep
     * @return		true if this Flags object changed
     * @since		JavaMail 1.6
     */
    public boolean retainAll(Flags f) {
	boolean changed = false;
	int sf = system_flags & f.system_flags;
	if (system_flags != sf) {
	    system_flags = sf;
	    changed = true;
	}

	// if we have user flags, and the USER flag is not set in "f",
	// determine which user flags to clear
	if (user_flags != null && (f.system_flags & USER_BIT) == 0) {
	    HashMap<String, String> otherUserFlags = f.user_flags;
        if (otherUserFlags != null) {
		for (Iterator<String> it = user_flags.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            if (!otherUserFlags.containsKey(key)) {
			it.remove();
			changed = true;
		    }
        }
	    } else {
		// if anything in user_flags, throw them away
		changed = !user_flags.isEmpty();
		user_flags = null;
	    }
	}
	return changed;
    }

    /**
     * Check whether the specified system flag is present in this Flags object.
     *
     * @param	flag	the flag to test
     * @return 		true of the given flag is present, otherwise false.
     */
    public boolean contains(Flag flag) {
	return (system_flags & flag.bit) != 0;
    }

    /**
     * Check whether the specified user flag is present in this Flags object.
     *
     * @param	flag	the flag to test
     * @return 		true of the given flag is present, otherwise false.
     */
    public boolean contains(String flag) {
	if (user_flags == null) 
	    return false;
	else
	    return user_flags.containsKey(asciiLowerCase(flag));
    }

    /**
     * Check whether all the flags in the specified Flags object are
     * present in this Flags object.
     *
     * @param	f	the flags to test
     * @return	true if all flags in the given Flags object are present, 
     *		otherwise false.
     */
    public boolean contains(Flags f) {
	// Check system flags
	if ((f.system_flags & system_flags) != f.system_flags)
	    return false;

	// Check user flags
	HashMap<String, String> otherUserFlags = f.user_flags;
    if (otherUserFlags != null) {
	    if (user_flags == null)
		return false;
	    for (String key : otherUserFlags.keySet()) {
	        if (!user_flags.containsKey(key)) {
	            return false;
	        } 
	    }
	}

	// If we've made it till here, return true
	return true;
    }

    /**
     * Check whether the two Flags objects are equal.
     *
     * @return	true if they're equal
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Flags))
	    return false;

	Flags f = (Flags)obj;

	// Check system flags
	if (f.system_flags != this.system_flags)
	    return false;

	// Check user flags
	int size = this.user_flags == null ? 0 : this.user_flags.size();
	int fsize = f.user_flags == null ? 0 : f.user_flags.size();
	if (size == 0 && fsize == 0)
	    return true;
	if (f.user_flags != null && this.user_flags != null && fsize == size)
	    return user_flags.keySet().equals(f.user_flags.keySet());

	return false;
    }

    /**
     * Compute a hash code for this Flags object.
     *
     * @return	the hash code
     */
    @Override
    public int hashCode() {
	int hash = system_flags;
	if (user_flags != null) {
	    for (String key : user_flags.keySet()) {
	        hash += key.hashCode();
	    }
	}
	return hash;
    }

    /**
     * Return all the system flags in this Flags object.  Returns
     * an array of size zero if no flags are set.
     *
     * @return	array of Flags.Flag objects representing system flags
     */
    public Flag[] getSystemFlags() {
	List<Flag> v = new ArrayList<>(8);
	if ((system_flags & ANSWERED_BIT) != 0)
	    v.add(Flag.ANSWERED);
	if ((system_flags & DELETED_BIT) != 0)
	    v.add(Flag.DELETED);
	if ((system_flags & DRAFT_BIT) != 0)
	    v.add(Flag.DRAFT);
	if ((system_flags & FLAGGED_BIT) != 0)
	    v.add(Flag.FLAGGED);
	if ((system_flags & RECENT_BIT) != 0)
	    v.add(Flag.RECENT);
	if ((system_flags & SEEN_BIT) != 0)
	    v.add(Flag.SEEN);
	if ((system_flags & USER_BIT) != 0)
	    v.add(Flag.USER);

	return v.toArray(new Flag[v.size()]);
    }

    /**
     * Return all the user flags in this Flags object.  Returns
     * an array of size zero if no flags are set.
     *
     * @return	array of Strings, each String represents a flag.
     */
    public String[] getUserFlags() {
    HashMap<String, String> user_flags = this.user_flags;
    int size;
    if (null == user_flags || (size = user_flags.size()) <= 0) {
        return new String[0];
    }
    
    String[] v = new String[size];
    Iterator<String> it = user_flags.values().iterator();
    for (int i = 0; i < size; i++) {
        v[i] = it.next();
    }
    return v;
    }

    /**
     * Return all the user flags in this Flags object.  Returns
     * a list of size zero if no flags are set.
     *
     * @return  list of Strings, each String represents a flag.
     */
    public List<String> getUserFlagsAsList() {
    HashMap<String, String> user_flags = this.user_flags;
    if (null == user_flags || user_flags.isEmpty()) {
        return java.util.Collections.emptyList();
    }
        
    return new ArrayList<String>(user_flags.values());
    }

    /**
     * Clear all of the system flags.
     *
     * @since	JavaMail 1.6
     */
    public void clearSystemFlags() {
	system_flags = 0;
    }

    /**
     * Clear all of the user flags.
     *
     * @since	JavaMail 1.6
     */
    public void clearUserFlags() {
	user_flags = null;
    }

    /**
     * Removes all user flags from this Flags object.
     *
     * @return This Flags object with all user flags removed
     */
    public Flags removeAllUserFlags() {
        user_flags = null;
        return this;
    }

    /**
     * Returns a clone of this Flags object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
	Flags f = null;
	try {
	    f = (Flags)super.clone();
	} catch (CloneNotSupportedException cex) {
	    // ignore, can't happen
	}
	if (this.user_flags != null)
	    f.user_flags = (HashMap<String, String>)this.user_flags.clone();
	return f;
    }

    /**
     * Return a string representation of this Flags object.
     * Note that the exact format of the string is subject to change.
     */
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if ((system_flags & ANSWERED_BIT) != 0)
	    sb.append("\\Answered ");
	if ((system_flags & DELETED_BIT) != 0)
	    sb.append("\\Deleted ");
	if ((system_flags & DRAFT_BIT) != 0)
	    sb.append("\\Draft ");
	if ((system_flags & FLAGGED_BIT) != 0)
	    sb.append("\\Flagged ");
	if ((system_flags & RECENT_BIT) != 0)
	    sb.append("\\Recent ");
	if ((system_flags & SEEN_BIT) != 0)
	    sb.append("\\Seen ");
	if ((system_flags & USER_BIT) != 0)
	    sb.append("\\* ");

	boolean first = true;
	if (user_flags != null) {
	    for (String value : user_flags.values()) {
		if (first)
		    first = false;
		else
		    sb.append(' ');
		sb.append(value);
	    }
	}

	if (first && sb.length() > 0)
	    sb.setLength(sb.length() - 1);	// smash trailing space

	return sb.toString();
    }

    private static char[] lowercases = {
        '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014', '\015', '\016', '\017',
        '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035', '\036', '\037',
        '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056', '\057',
        '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
        '\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137',
        '\140', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157',
        '\160', '\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

    /**
     * Fast lower-case conversion.
     *
     * @param s The string
     * @return The lower-case string
     */
    private static String asciiLowerCase(String s) {
        if (null == s) {
            return null;
        }

        char[] c = null;
        int i = s.length();

        // look for first conversion
        while (i-- > 0) {
            char c1 = s.charAt(i);
            if (c1 <= 127) {
                char c2 = lowercases[c1];
                if (c1 != c2) {
                    c = s.toCharArray();
                    c[i] = c2;
                    break;
                }
            }
        }

        while (i-- > 0) {
            if (c[i] <= 127) {
                c[i] = lowercases[c[i]];
            }
        }

        return c == null ? s : new String(c);
    }

    /*****
    public static void main(String argv[]) throws Exception {
	// a new flags object
	Flags f1 = new Flags();
	f1.add(Flags.Flag.DELETED);
	f1.add(Flags.Flag.SEEN);
	f1.add(Flags.Flag.RECENT);
	f1.add(Flags.Flag.ANSWERED);

	// check copy constructor with only system flags
	Flags fc = new Flags(f1);
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check clone with only system flags
	fc = (Flags)f1.clone();
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// add a user flag and make sure it still works right
	f1.add("MyFlag");

	// shouldn't be equal here
	if (!f1.equals(fc) && !fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check clone
	fc = (Flags)f1.clone();
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// make sure user flag hash tables are separate
	fc.add("AnotherFlag");
	if (!f1.equals(fc) && !fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check copy constructor
	fc = new Flags(f1);
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// another new flags object
	Flags f2 = new Flags(Flags.Flag.ANSWERED);
	f2.add("MyFlag");

	if (f1.contains(Flags.Flag.DELETED))
	    System.out.println("success");
	else
	    System.out.println("fail");
		
	if (f1.contains(Flags.Flag.SEEN))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f1.contains(Flags.Flag.RECENT))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f1.contains("MyFlag"))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f2.contains(Flags.Flag.ANSWERED))
	    System.out.println("success");
	else
	    System.out.println("fail");


	System.out.println("----------------");

	String[] s = f1.getUserFlags();
	for (int i = 0; i < s.length; i++)
	    System.out.println(s[i]);
	System.out.println("----------------");
	s = f2.getUserFlags();
	for (int i = 0; i < s.length; i++)
	    System.out.println(s[i]);

	System.out.println("----------------");

	if (f1.contains(f2)) // this should be true
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (!f2.contains(f1)) // this should be false
	    System.out.println("success");
	else
	    System.out.println("fail");

	Flags f3 = new Flags();
	f3.add(Flags.Flag.DELETED);
	f3.add(Flags.Flag.SEEN);
	f3.add(Flags.Flag.RECENT);
	f3.add(Flags.Flag.ANSWERED);
	f3.add("ANOTHERFLAG");
	f3.add("MYFLAG");

	f1.add("AnotherFlag");

	if (f1.equals(f3))
	    System.out.println("equals success");
	else
	    System.out.println("fail");
	if (f3.equals(f1))
	    System.out.println("equals success");
	else
	    System.out.println("fail");
	System.out.println("f1 hash code " + f1.hashCode());
	System.out.println("f3 hash code " + f3.hashCode());
	if (f1.hashCode() == f3.hashCode())
	    System.out.println("success");
	else
	    System.out.println("fail");
    }
    ****/
}
