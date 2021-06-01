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

package com.openexchange.mail.json.compose.share;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * {@link FileItems} - A listing of file items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileItems implements List<FileItem> {

    private final List<FileItem> fileItems;

    /**
     * Initializes a new {@link FileItems}.
     */
    public FileItems() {
        super();
        fileItems = new ArrayList<>();
    }

    /**
     * Initializes a new {@link FileItems}.
     *
     * @param initialCapacity The initial capacity
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public FileItems(int initialCapacity) {
        super();
        fileItems = new ArrayList<>(initialCapacity);
    }

    /**
     * Initializes a new {@link FileItems}.
     *
     * @param c the collection whose file items are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public FileItems(Collection<? extends FileItem> c) {
        super();
        fileItems = new ArrayList<>(c);
    }

    /**
     * Performs the given action for each file item of the {@code Iterable}
     * until all file items have been processed or the action throws an
     * exception. Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified). Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @implSpec
     *           <p>The default implementation behaves as if:
     *           <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }      </pre>
     *
     * @param action The action to be performed for each file item
     * @throws NullPointerException if the specified action is null
     */
    @Override
    public void forEach(Consumer<? super FileItem> action) {
        fileItems.forEach(action);
    }

    /**
     * Returns the number of file items in this list.
     *
     * @return The number of file items in this list
     */
    @Override
    public int size() {
        return fileItems.size();
    }

    /**
     * Returns <code>true</code> if this list contains no file items.
     *
     * @return <code>true</code> if this list contains no file items
     */
    @Override
    public boolean isEmpty() {
        return fileItems.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified file item.
     * <p>
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one file item <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o The file item whose presence in this list is to be tested
     * @return <code>true</code> if this list contains the specified file item
     */
    @Override
    public boolean contains(Object o) {
        return fileItems.contains(o);
    }

    /**
     * Returns an iterator over the file items in this list in proper sequence.
     *
     * @return an iterator over the file items in this list in proper sequence
     */
    @Override
    public Iterator<FileItem> iterator() {
        return fileItems.iterator();
    }

    /**
     * Returns an array containing all of the file items in this list in proper
     * sequence (from first to last file item).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must
     * allocate a new array even if this list is backed by an array).
     * The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the file items in this list in proper
     *         sequence
     * @see Arrays#asList(Object[])
     */
    @Override
    public Object[] toArray() {
        return fileItems.toArray();
    }

    /**
     * Returns an array containing all of the file items in this list in
     * proper sequence (from first to last file item); the runtime type of
     * the returned array is that of the specified array. If the list fits
     * in the specified array, it is returned therein. Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     *
     * <p>If the list fits in the specified array with room to spare (i.e.,
     * the array has more file items than the list), the file item in the array
     * immediately following the end of the list is set to <tt>null</tt>.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null file items.)
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs. Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>{@code
     *     String[] y = x.toArray(new String[0]);
     * }</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the file items of this list are to
     *            be stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose.
     * @return an array containing the file items of this list
     * @throws ArrayStoreException if the runtime type of the specified array
     *             is not a supertype of the runtime type of every file item in
     *             this list
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return fileItems.toArray(a);
    }

    /**
     * Appends the specified file item to the end of this list (optional
     * operation).
     *
     * <p>Lists that support this operation may place limitations on what
     * file items may be added to this list. In particular, some
     * lists will refuse to add null file items, and others will impose
     * restrictions on the type of file items that may be added. List
     * classes should clearly specify in their documentation any restrictions
     * on what file items may be added.
     *
     * @param e The file item to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of the specified file item
     *             prevents it from being added to this list
     * @throws NullPointerException if the specified file item is null and this
     *             list does not permit null file items
     * @throws IllegalArgumentException if some property of this file item
     *             prevents it from being added to this list
     */
    @Override
    public boolean add(FileItem e) {
        return fileItems.add(e);
    }

    /**
     * Removes the first occurrence of the specified file item from this list,
     * if it is present (optional operation). If this list does not contain
     * the file item, it is unchanged. More formally, removes the file item with
     * the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an file item exists). Returns <tt>true</tt> if this list
     * contained the specified file item (or equivalently, if this list changed
     * as a result of the call).
     *
     * @param o file item to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified file item
     * @throws ClassCastException if the type of the specified file item
     *             is incompatible with this list
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified file item is null and this
     *             list does not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *             is not supported by this list
     */
    @Override
    public boolean remove(Object o) {
        return fileItems.remove(o);
    }

    /**
     * Returns <tt>true</tt> if this list contains all of the file items of the
     * specified collection.
     *
     * @param c collection to be checked for containment in this list
     * @return <tt>true</tt> if this list contains all of the file items of the
     *         specified collection
     * @throws ClassCastException if the types of one or more file items
     *             in the specified collection are incompatible with this
     *             list
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified collection contains one
     *             or more null file items and this list does not permit null
     *             file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>),
     *             or if the specified collection is null
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return fileItems.containsAll(c);
    }

    /**
     * Appends all of the file items in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation). The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress. (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection containing file items to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of an file item of the specified
     *             collection prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one
     *             or more null file items and this list does not permit null
     *             file items, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an file item of the
     *             specified collection prevents it from being added to this list
     * @see #add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends FileItem> c) {
        return fileItems.addAll(c);
    }

    /**
     * Inserts all of the file items in the specified collection into this
     * list at the specified position (optional operation). Shifts the
     * file item currently at that position (if any) and any subsequent
     * file items to the right (increases their indices). The new file items
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator. The behavior of this operation is
     * undefined if the specified collection is modified while the
     * operation is in progress. (Note that this will occur if the specified
     * collection is this list, and it's nonempty.)
     *
     * @param index index at which to insert the first file item from the
     *            specified collection
     * @param c collection containing file items to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of an file item of the specified
     *             collection prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one
     *             or more null file items and this list does not permit null
     *             file items, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an file item of the
     *             specified collection prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    @Override
    public boolean addAll(int index, Collection<? extends FileItem> c) {
        return fileItems.addAll(index, c);
    }

    /**
     * Removes from this list all of its file items that are contained in the
     * specified collection (optional operation).
     *
     * @param c collection containing file items to be removed from this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of an file item of this list
     *             is incompatible with the specified collection
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null file item and the
     *             specified collection does not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>),
     *             or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return fileItems.removeAll(c);
    }

    /**
     * Retains only the file items in this list that are contained in the
     * specified collection (optional operation). In other words, removes
     * from this list all of its file items that are not contained in the
     * specified collection.
     *
     * @param c collection containing file items to be retained in this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of an file item of this list
     *             is incompatible with the specified collection
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if this list contains a null file item and the
     *             specified collection does not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>),
     *             or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return fileItems.retainAll(c);
    }

    /**
     * Replaces each file item of this list with the result of applying the
     * operator to that file item. Errors or runtime exceptions thrown by
     * the operator are relayed to the caller.
     *
     * @implSpec
     *           The default implementation is equivalent to, for this {@code list}:
     *           <pre>{@code
     *     final ListIterator<E> li = list.listIterator();
     *     while (li.hasNext()) {
     *         li.set(operator.apply(li.next()));
     *           }
     *           }</pre>
     *
     *           If the list's list-iterator does not support the {@code set} operation
     *           then an {@code UnsupportedOperationException} will be thrown when
     *           replacing the first file item.
     *
     * @param operator the operator to apply to each file item
     * @throws UnsupportedOperationException if this list is unmodifiable.
     *             Implementations may throw this exception if an file item
     *             cannot be replaced or if, in general, modification is not
     *             supported
     * @throws NullPointerException if the specified operator is null or
     *             if the operator result is a null value and this list does
     *             not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public void replaceAll(UnaryOperator<FileItem> operator) {
        fileItems.replaceAll(operator);
    }

    /**
     * Removes all of the file items of this collection that satisfy the given
     * predicate. Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @implSpec
     *           The default implementation traverses all file items of the collection using
     *           its {@link #iterator}. Each matching file item is removed using
     *           {@link Iterator#remove()}. If the collection's iterator does not
     *           support removal then an {@code UnsupportedOperationException} will be
     *           thrown on the first matching file item.
     *
     * @param filter a predicate which returns {@code true} for file items to be
     *            removed
     * @return {@code true} if any file items were removed
     * @throws NullPointerException if the specified filter is null
     * @throws UnsupportedOperationException if file items cannot be removed
     *             from this collection. Implementations may throw this exception if a
     *             matching file item cannot be removed or if, in general, removal is not
     *             supported.
     */
    @Override
    public boolean removeIf(Predicate<? super FileItem> filter) {
        return fileItems.removeIf(filter);
    }

    /**
     * Sorts this list according to the order induced by the specified
     * {@link Comparator}.
     *
     * <p>All file items in this list must be <i>mutually comparable</i> using the
     * specified comparator (that is, {@code c.compare(e1, e2)} must not throw
     * a {@code ClassCastException} for any file items {@code e1} and {@code e2}
     * in the list).
     *
     * <p>If the specified comparator is {@code null} then all file items in this
     * list must implement the {@link Comparable} interface and the file items'
     * {@linkplain Comparable natural ordering} should be used.
     *
     * <p>This list must be modifiable, but need not be resizable.
     *
     * @implSpec
     *           The default implementation obtains an array containing all file items in
     *           this list, sorts the array, and iterates over this list resetting each
     *           file item from the corresponding position in the array. (This avoids the
     *           n<sup>2</sup> log(n) performance that would result from attempting
     *           to sort a linked list in place.)
     *
     * @implNote
     *           This implementation is a stable, adaptive, iterative mergesort that
     *           requires far fewer than n lg(n) comparisons when the input array is
     *           partially sorted, while offering the performance of a traditional
     *           mergesort when the input array is randomly ordered. If the input array
     *           is nearly sorted, the implementation requires approximately n
     *           comparisons. Temporary storage requirements vary from a small constant
     *           for nearly sorted input arrays to n/2 object references for randomly
     *           ordered input arrays.
     *
     *           <p>The implementation takes equal advantage of ascending and
     *           descending order in its input array, and can take advantage of
     *           ascending and descending order in different parts of the same
     *           input array. It is well-suited to merging two or more sorted arrays:
     *           simply concatenate the arrays and sort the resulting array.
     *
     *           <p>The implementation was adapted from Tim Peters's list sort for Python
     *           (<a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">
     *           TimSort</a>). It uses techniques from Peter McIlroy's "Optimistic
     *           Sorting and Information Theoretic Complexity", in Proceedings of the
     *           Fourth Annual ACM-SIAM Symposium on Discrete Algorithms, pp 467-474,
     *           January 1993.
     *
     * @param c the {@code Comparator} used to compare list file items.
     *            A {@code null} value indicates that the file items'
     *            {@linkplain Comparable natural ordering} should be used
     * @throws ClassCastException if the list contains file items that are not
     *             <i>mutually comparable</i> using the specified comparator
     * @throws UnsupportedOperationException if the list's list-iterator does
     *             not support the {@code set} operation
     * @throws IllegalArgumentException
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     *             if the comparator is found to violate the {@link Comparator}
     *             contract
     */
    @Override
    public void sort(Comparator<? super FileItem> c) {
        fileItems.sort(c);
    }

    /**
     * Removes all of the file items from this list (optional operation).
     * The list will be empty after this call returns.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *             is not supported by this list
     */
    @Override
    public void clear() {
        fileItems.clear();
    }

    /**
     * Returns the file item at the specified position in this list.
     *
     * @param index index of the file item to return
     * @return the file item at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public FileItem get(int index) {
        return fileItems.get(index);
    }

    /**
     * Replaces the file item at the specified position in this list with the
     * specified file item (optional operation).
     *
     * @param index The index of the file item to replace
     * @param fileItem The file item to be stored at the specified position
     * @return the file item previously at the specified position
     * @throws UnsupportedOperationException if the <tt>set</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of the specified file item
     *             prevents it from being added to this list
     * @throws NullPointerException if the specified file item is null and
     *             this list does not permit null file items
     * @throws IllegalArgumentException if some property of the specified
     *             file item prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public FileItem set(int index, FileItem fileItem) {
        return fileItems.set(index, fileItem);
    }

    /**
     * Inserts the specified file item at the specified position in this list
     * (optional operation). Shifts the file item currently at that position
     * (if any) and any subsequent file items to the right (adds one to their
     * indices).
     *
     * @param index The index at which the specified file item is to be inserted
     * @param fileItem The file item to be inserted
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *             is not supported by this list
     * @throws ClassCastException if the class of the specified file item
     *             prevents it from being added to this list
     * @throws NullPointerException if the specified file item is null and
     *             this list does not permit null file items
     * @throws IllegalArgumentException if some property of the specified
     *             file item prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    @Override
    public void add(int index, FileItem fileItem) {
        fileItems.add(index, fileItem);
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @implSpec
     *           The default implementation creates a sequential {@code Stream} from the
     *           collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the file items in this collection
     */
    @Override
    public Stream<FileItem> stream() {
        return fileItems.stream();
    }

    /**
     * Removes the file item at the specified position in this list (optional
     * operation). Shifts any subsequent file items to the left (subtracts one
     * from their indices). Returns the file item that was removed from the
     * list.
     *
     * @param index the index of the file item to be removed
     * @return the file item previously at the specified position
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *             is not supported by this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public FileItem remove(int index) {
        return fileItems.remove(index);
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its
     * source. It is allowable for this method to return a sequential stream.
     *
     * <p>This method should be overridden when the {@link #spliterator()}
     * method cannot return a spliterator that is {@code IMMUTABLE},
     * {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()}
     * for details.)
     *
     * @implSpec
     *           The default implementation creates a parallel {@code Stream} from the
     *           collection's {@code Spliterator}.
     *
     * @return a possibly parallel {@code Stream} over the file items in this
     *         collection
     */
    @Override
    public Stream<FileItem> parallelStream() {
        return fileItems.parallelStream();
    }

    /**
     * Returns the index of the first occurrence of the specified file item
     * in this list, or -1 if this list does not contain the file item.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o file item to search for
     * @return the index of the first occurrence of the specified file item in
     *         this list, or -1 if this list does not contain the file item
     * @throws ClassCastException if the type of the specified file item
     *             is incompatible with this list
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified file item is null and this
     *             list does not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int indexOf(Object o) {
        return fileItems.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified file item
     * in this list, or -1 if this list does not contain the file item.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o file item to search for
     * @return the index of the last occurrence of the specified file item in
     *         this list, or -1 if this list does not contain the file item
     * @throws ClassCastException if the type of the specified file item
     *             is incompatible with this list
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified file item is null and this
     *             list does not permit null file items
     *             (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int lastIndexOf(Object o) {
        return fileItems.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the file items in this list (in proper
     * sequence).
     *
     * @return a list iterator over the file items in this list (in proper
     *         sequence)
     */
    @Override
    public ListIterator<FileItem> listIterator() {
        return fileItems.listIterator();
    }

    /**
     * Returns a list iterator over the file items in this list (in proper
     * sequence), starting at the specified position in the list.
     * The specified index indicates the first file item that would be
     * returned by an initial call to {@link ListIterator#next next}.
     * An initial call to {@link ListIterator#previous previous} would
     * return the file item with the specified index minus one.
     *
     * @param index index of the first file item to be returned from the
     *            list iterator (by a call to {@link ListIterator#next next})
     * @return a list iterator over the file items in this list (in proper
     *         sequence), starting at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     *             ({@code index < 0 || index > size()})
     */
    @Override
    public ListIterator<FileItem> listIterator(int index) {
        return fileItems.listIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive. (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.) The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations supported
     * by this list.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays). Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list. For example, the following idiom
     * removes a range of file items from a list:
     * <pre>{@code
     *      list.subList(from, to).clear();
     * }</pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list. (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *             (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *             fromIndex &gt; toIndex</tt>)
     */
    @Override
    public List<FileItem> subList(int fromIndex, int toIndex) {
        return fileItems.subList(fromIndex, toIndex);
    }

    /**
     * Creates a {@link Spliterator} over the file items in this list.
     *
     * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
     * {@link Spliterator#ORDERED}. Implementations should document the
     * reporting of additional characteristic values.
     *
     * @implSpec
     *           The default implementation creates a
     *           <em><a href="Spliterator.html#binding">late-binding</a></em> spliterator
     *           from the list's {@code Iterator}. The spliterator inherits the
     *           <em>fail-fast</em> properties of the list's iterator.
     *
     * @implNote
     *           The created {@code Spliterator} additionally reports
     *           {@link Spliterator#SUBSIZED}.
     *
     * @return a {@code Spliterator} over the file items in this list
     */
    @Override
    public Spliterator<FileItem> spliterator() {
        return fileItems.spliterator();
    }

}
