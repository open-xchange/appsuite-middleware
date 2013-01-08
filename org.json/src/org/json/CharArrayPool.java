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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package org.json;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link CharArrayPool} - A character array pool.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharArrayPool {

    /**
     * A pool part.
     */
    public static enum Part {

        /**
         * Denotes the pool part for small-sized character arrays.
         */
        SMALL,
        /**
         * Denotes the pool part for medium-sized character arrays.
         */
        MEDIUM,
        /**
         * Denotes the pool part for large-sized character arrays.
         */
        LARGE;

    }

    private static final AtomicInteger NUMBER_OF_SMALL = new AtomicInteger(100000);
    private static final AtomicInteger NUMBER_OF_MEDIUM = new AtomicInteger(10000);
    private static final AtomicInteger NUMBER_OF_LARGE = new AtomicInteger(1000);

    /**
     * Sets the numbers.
     * 
     * @param numberOfSmall The capacity for collection of small-sized character arrays
     * @param numberOfMedium The capacity for collection of medium-sized character arrays
     * @param numberOfLarge The capacity for collection of large-sized character arrays
     */
    public static void setCapacities(final int numberOfSmall, final int numberOfMedium, final int numberOfLarge) {
        if (numberOfSmall < 0 || numberOfMedium < 0 || numberOfLarge < 0) {
            throw new IllegalArgumentException(
                "smallCapacity < 0 OR mediumCapacity < 0 OR largeCapacity < 0: " + Arrays.toString(new int[] {
                    numberOfSmall, numberOfMedium, numberOfLarge }));
        }
        NUMBER_OF_SMALL.set(numberOfSmall);
        NUMBER_OF_MEDIUM.set(numberOfMedium);
        NUMBER_OF_LARGE.set(numberOfLarge);
    }

    private static final AtomicInteger LENGTH_OF_SMALL = new AtomicInteger(1024); // 1 KB
    private static final AtomicInteger LENGTH_OF_MEDIUM = new AtomicInteger(1024 * 100); // 100 KB
    private static final AtomicInteger LENGTH_OF_LARGE = new AtomicInteger(1024 * 1000); // 1000 KB

    /**
     * Sets the lengths.
     * 
     * @param lengthOfSmall The length for small-sized character arrays
     * @param lengthOfMedium The length for medium-sized character arrays
     * @param lengthOfLarge The length for large-sized character arrays
     */
    public static void setLengths(final int lengthOfSmall, final int lengthOfMedium, final int lengthOfLarge) {
        if (lengthOfSmall < 0 || lengthOfMedium < 0 || lengthOfLarge < 0) {
            throw new IllegalArgumentException("smallSize < 0 OR mediumSize < 0 OR largeSize < 0: " + Arrays.toString(new int[] {
                lengthOfSmall, lengthOfMedium, lengthOfLarge }));
        }
        if (lengthOfLarge < lengthOfMedium || lengthOfLarge < lengthOfSmall) {
            throw new IllegalArgumentException("largeSize < mediumSize OR largeSize < smallSize: " + Arrays.toString(new int[] {
                lengthOfSmall, lengthOfMedium, lengthOfLarge }));
        }
        if (lengthOfMedium < lengthOfSmall) {
            throw new IllegalArgumentException("mediumSize < largeSize: " + Arrays.toString(new int[] {
                lengthOfSmall, lengthOfMedium, lengthOfLarge }));
        }
        LENGTH_OF_SMALL.set(lengthOfSmall);
        LENGTH_OF_MEDIUM.set(lengthOfMedium);
        LENGTH_OF_LARGE.set(lengthOfLarge);
    }

    private static volatile CharArrayPool instance;

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    protected static CharArrayPool getInstance() {
        CharArrayPool tmp = instance;
        if (null == tmp) {
            synchronized (CharArrayPool.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new CharArrayPool(LENGTH_OF_SMALL.get(), LENGTH_OF_MEDIUM.get(), LENGTH_OF_LARGE.get());
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    private static final AtomicLong MISSES = new AtomicLong();

    /**
     * Gets the number of misses for pooled character arrays.
     * 
     * @return The number of misses
     */
    public static long getMisses() {
        return MISSES.get();
    }

    /**
     * Gets the number of total pooled character arrays for specified pool part.
     * 
     * @param part The pool part
     * @return The number of total pooled character arrays
     */
    public static int getTotal(final Part part) {
        if (null == instance) {
            return 0;
        }
        if (Part.SMALL.equals(part)) {
            return NUMBER_OF_SMALL.get();
        }
        if (Part.MEDIUM.equals(part)) {
            return NUMBER_OF_MEDIUM.get();
        }
        return NUMBER_OF_LARGE.get();
    }

    /**
     * Gets the number of in-use pooled character arrays for specified pool part.
     * 
     * @param part The pool part
     * @return The number of in-use pooled character arrays
     */
    public static int getInUse(final Part part) {
        final CharArrayPool tmp = instance;
        if (null == tmp) {
            return 0;
        }
        if (Part.SMALL.equals(part)) {
            return NUMBER_OF_SMALL.get() - tmp.smallQueue.size();
        }
        if (Part.MEDIUM.equals(part)) {
            return NUMBER_OF_MEDIUM.get() - tmp.mediumQueue.size();
        }
        return NUMBER_OF_LARGE.get() - tmp.largeQueue.size();
    }

    /**
     * Gets the number of currently pooled character arrays for specified pool part.
     * 
     * @param part The pool part
     * @return The number of currently pooled character arrays
     */
    public static int getPooled(final Part part) {
        final CharArrayPool tmp = instance;
        if (null == tmp) {
            return 0;
        }
        if (Part.SMALL.equals(part)) {
            return tmp.smallQueue.size();
        }
        if (Part.MEDIUM.equals(part)) {
            return tmp.mediumQueue.size();
        }
        return tmp.largeQueue.size();
    }

    /*-
     * ------------------------------------------ Member stuff ------------------------------------------
     */

    private final BlockingQueue<CharArray> smallQueue;
    private final BlockingQueue<CharArray> mediumQueue;
    private final BlockingQueue<CharArray> largeQueue;
    private final int lengthOfSmall;
    private final int lengthOfMedium;
    private final int lengthOfLarge;

    /**
     * Initializes a new {@link CharArrayPool}.
     */
    private CharArrayPool(final int lengthOfSmall, final int lengthOfMedium, final int lengthOfLarge) {
        super();
        this.lengthOfSmall = lengthOfSmall;
        this.lengthOfMedium = lengthOfMedium;
        this.lengthOfLarge = lengthOfLarge;
        // Create queues
        class QueueCreator {

            BlockingQueue<CharArray> newQueue(final int numberOfArrays, final int lengthOfArrays) {
                if (numberOfArrays <= 0 || lengthOfArrays <= 0) {
                    return null;
                }
                final BlockingQueue<CharArray> q = new ArrayBlockingQueue<CharArray>(numberOfArrays);
                for (int i = 0; i < numberOfArrays; i++) {
                    q.offer(new CharArray(lengthOfArrays));
                }
                return q;
            }
        }
        final QueueCreator qr = new QueueCreator();
        smallQueue = qr.newQueue(NUMBER_OF_SMALL.get(), lengthOfSmall);
        mediumQueue = qr.newQueue(NUMBER_OF_MEDIUM.get(), lengthOfMedium);
        largeQueue = qr.newQueue(NUMBER_OF_LARGE.get(), lengthOfLarge);
    }

    /**
     * Gets the length for a character array appropriate for given length.
     * 
     * @param capacity The desired length
     * @return The pool-chosen length
     */
    protected int getCharArrayLength(final int capacity) {
        if (capacity <= lengthOfSmall) {
            return lengthOfSmall;
        }
        if (capacity <= lengthOfMedium) {
            return lengthOfMedium;
        }
        if (capacity <= lengthOfLarge) {
            return lengthOfLarge;
        }
        return capacity;
    }

    /**
     * Gets the small length
     * 
     * @return The small length
     */
    protected int getSmallLength() {
        return lengthOfSmall;
    }

    /**
     * Gets the medium length
     * 
     * @return The medium length
     */
    protected int getMediumLength() {
        return lengthOfMedium;
    }

    /**
     * Gets the large length
     * 
     * @return The large length
     */
    protected int getLargeLength() {
        return lengthOfLarge;
    }

    /**
     * Gets a pooled character array appropriate for small capacity.
     * 
     * @return An appropriate character array or <code>null</code>
     */
    protected ICharArray getSmallCharArray() {
        return getCharArrayFor(Part.SMALL);
    }

    /**
     * Gets a pooled character array appropriate for medium capacity.
     * 
     * @return An appropriate character array or <code>null</code>
     */
    protected ICharArray getMediumCharArray() {
        return getCharArrayFor(Part.MEDIUM);
    }

    /**
     * Gets a pooled character array appropriate for large capacity.
     * 
     * @return An appropriate character array or <code>null</code>
     */
    protected ICharArray getLargeCharArray() {
        return getCharArrayFor(Part.LARGE);
    }

    /**
     * Gets a pooled character array appropriate for given pool part.
     * 
     * @param capacity The desired pool part
     * @return An appropriate character array or <code>null</code>
     */
    protected ICharArray getCharArrayFor(final Part part) {
        ICharArray ret = null;
        switch (part) {
        case SMALL:
            ret = smallQueue.poll();
            if (null != ret) {
                return ret;
            }
            //$FALL-THROUGH$
        case MEDIUM:
            ret = mediumQueue.poll();
            if (null != ret) {
                return ret;
            }
            //$FALL-THROUGH$
        case LARGE:
            ret = largeQueue.poll();
            if (null != ret) {
                return ret;
            }
            //$FALL-THROUGH$
        default:
            //$FALL-THROUGH$
        }
        MISSES.incrementAndGet();
        return ret;
    }

    /**
     * Gets a pooled character array appropriate for given capacity.
     * 
     * @param capacity The desired capacity
     * @return An appropriate character array or <code>null</code>
     */
    protected CharArray getCharArrayFor(final int capacity) {
        CharArray ret = null;
        if (capacity <= lengthOfSmall) {
            ret = smallQueue.poll();
            if (null != ret) {
                return ret;
            }
        }
        if (capacity <= lengthOfMedium) {
            ret = mediumQueue.poll();
            if (null != ret) {
                return ret;
            }
        }
        if (capacity <= lengthOfLarge) {
            ret = largeQueue.poll();
            if (null != ret) {
                return ret;
            }
        }
        MISSES.incrementAndGet();
        return ret;
    }

    /**
     * Offers specified character array to pool.
     * 
     * @param charArray The character array to offer
     * @return <code>true</code> if character array was added to pool; otherwise <code>false</code>
     */
    protected boolean offer(final CharArray charArray) {
        if (null == charArray) {
            return false;
        }
        charArray.reset();
        final int capacity = charArray.capacity();
        if (capacity <= lengthOfSmall) {
            return smallQueue.offer(charArray);
        }
        if (capacity <= lengthOfMedium) {
            return mediumQueue.offer(charArray);
        }
        if (capacity <= lengthOfLarge) {
            return largeQueue.offer(charArray);
        }
        return false;
    }

}
