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

package com.openexchange.ajp13.coyote.util;

import java.io.IOException;


/**
 * {@link CharChunk}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharChunk implements CharSequence {

 // Input interface, used when the buffer is emptied.
    public static interface CharInputChannel {
        /**
         * Read new bytes ( usually the internal conversion buffer ).
         * The implementation is allowed to ignore the parameters,
         * and mutate the chunk if it wishes to implement its own buffering.
         */
        public int realReadChars(char cbuf[], int off, int len)
            throws IOException;
    }
    /**
     *  When we need more space we'll either
     *  grow the buffer ( up to the limit ) or send it to a channel.
     */
    public static interface CharOutputChannel {
    /** Send the bytes ( usually the internal conversion buffer ).
     *  Expect 8k output if the buffer is full.
     */
        public void realWriteChars(char cbuf[], int off, int len)
            throws IOException;
    }

    // --------------------
    // char[]
    private char buff[];

    private int start;
    private int end;

    private boolean isSet=false;  // XXX

    private boolean isOutput=false;

    // -1: grow undefinitely
    // maximum amount to be cached
    private int limit=-1;

    private CharInputChannel in = null;
    private CharOutputChannel out = null;

    private boolean optimizedWrite=true;

    /**
     * Creates a new, uninitialized CharChunk object.
     */
    public CharChunk() {
    }

    public CharChunk(final int size) {
    allocate( size, -1 );
    }

    // --------------------

    public CharChunk getClone() {
    try {
        return (CharChunk)this.clone();
    } catch( final Exception ex) {
        return null;
    }
    }

    public boolean isNull() {
    if( end > 0 ) {
        return false;
    }
    return !isSet; //XXX
    }

    /**
     * Resets the message bytes to an uninitialized state.
     */
    public void recycle() {
    //  buff=null;
    isSet=false; // XXX
    start=0;
    end=0;
    }

    public void reset() {
    buff=null;
    }

    // -------------------- Setup --------------------

    public void allocate( final int initial, final int limit  ) {
    isOutput=true;
    if( buff==null || buff.length < initial ) {
        buff=new char[initial];
    }
    this.limit=limit;
    start=0;
    end=0;
    isOutput=true;
    isSet=true;
    }


    public void setOptimizedWrite(final boolean optimizedWrite) {
        this.optimizedWrite = optimizedWrite;
    }

    public void setChars( final char[] c, final int off, final int len ) {
        buff=c;
        start=off;
        end=start + len;
        isSet=true;
    }

    /** Maximum amount of data in this buffer.
     *
     *  If -1 or not set, the buffer will grow undefinitely.
     *  Can be smaller than the current buffer size ( which will not shrink ).
     *  When the limit is reached, the buffer will be flushed ( if out is set )
     *  or throw exception.
     */
    public void setLimit(final int limit) {
    this.limit=limit;
    }

    public int getLimit() {
    return limit;
    }

    /**
     * When the buffer is empty, read the data from the input channel.
     */
    public void setCharInputChannel(final CharInputChannel in) {
        this.in = in;
    }

    /** When the buffer is full, write the data to the output channel.
     *  Also used when large amount of data is appended.
     *
     *  If not set, the buffer will grow to the limit.
     */
    public void setCharOutputChannel(final CharOutputChannel out) {
    this.out=out;
    }

    // compat
    public char[] getChars()
    {
    return getBuffer();
    }

    public char[] getBuffer()
    {
    return buff;
    }

    /**
     * Returns the start offset of the bytes.
     * For output this is the end of the buffer.
     */
    public int getStart() {
    return start;
    }

    public int getOffset() {
    return start;
    }

    /**
     * Returns the start offset of the bytes.
     */
    public void setOffset(final int off) {
    start=off;
    }

    /**
     * Returns the length of the bytes.
     */
    public int getLength() {
    return end-start;
    }


    public int getEnd() {
    return end;
    }

    public void setEnd( final int i ) {
    end=i;
    }

    // -------------------- Adding data --------------------

    public void append( final char b )
    throws IOException
    {
    makeSpace( 1 );

    // couldn't make space
    if( limit >0 && end >= limit ) {
        flushBuffer();
    }
    buff[end++]=b;
    }

    public void append( final CharChunk src )
    throws IOException
    {
    append( src.getBuffer(), src.getOffset(), src.getLength());
    }

    /** Add data to the buffer
     */
    public void append( final char src[], final int off, final int len )
    throws IOException
    {
    // will grow, up to limit
    makeSpace( len );

    // if we don't have limit: makeSpace can grow as it wants
    if( limit < 0 ) {
        // assert: makeSpace made enough space
        System.arraycopy( src, off, buff, end, len );
        end+=len;
        return;
    }

        // Optimize on a common case.
        // If the source is going to fill up all the space in buffer, may
        // as well write it directly to the output, and avoid an extra copy
        if ( optimizedWrite && len == limit && end == start && out != null ) {
            out.realWriteChars( src, off, len );
            return;
        }

    // if we have limit and we're below
    if( len <= limit - end ) {
        // makeSpace will grow the buffer to the limit,
        // so we have space
        System.arraycopy( src, off, buff, end, len );

        end+=len;
        return;
    }

    // need more space than we can afford, need to flush
    // buffer

    // the buffer is already at ( or bigger than ) limit

    // Optimization:
    // If len-avail < length ( i.e. after we fill the buffer with
    // what we can, the remaining will fit in the buffer ) we'll just
    // copy the first part, flush, then copy the second part - 1 write
    // and still have some space for more. We'll still have 2 writes, but
    // we write more on the first.

    if( len + end < 2 * limit ) {
        /* If the request length exceeds the size of the output buffer,
           flush the output buffer and then write the data directly.
           We can't avoid 2 writes, but we can write more on the second
        */
        final int avail=limit-end;
        System.arraycopy(src, off, buff, end, avail);
        end += avail;

        flushBuffer();

        System.arraycopy(src, off+avail, buff, end, len - avail);
        end+= len - avail;

    } else {    // len > buf.length + avail
        // long write - flush the buffer and write the rest
        // directly from source
        flushBuffer();

        out.realWriteChars( src, off, len );
    }
    }


    /** Add data to the buffer
     */
    public void append( final StringBuffer sb )
    throws IOException
    {
    final int len=sb.length();

    // will grow, up to limit
    makeSpace( len );

    // if we don't have limit: makeSpace can grow as it wants
    if( limit < 0 ) {
        // assert: makeSpace made enough space
        sb.getChars(0, len, buff, end );
        end+=len;
        return;
    }

    final int off=0;
    int sbOff = off;
    final int sbEnd = off + len;
    while (sbOff < sbEnd) {
        final int d = min(limit - end, sbEnd - sbOff);
        sb.getChars( sbOff, sbOff+d, buff, end);
        sbOff += d;
        end += d;
        if (end >= limit) {
            flushBuffer();
        }
    }
    }

    /** Append a string to the buffer
     */
    public void append(final String s) throws IOException {
        append(s, 0, s.length());
    }

    /** Append a string to the buffer
     */
    public void append(final String s, final int off, final int len) throws IOException {
    if (s==null) {
        return;
    }

    // will grow, up to limit
    makeSpace( len );

    // if we don't have limit: makeSpace can grow as it wants
    if( limit < 0 ) {
        // assert: makeSpace made enough space
        s.getChars(off, off+len, buff, end );
        end+=len;
        return;
    }

    int sOff = off;
    final int sEnd = off + len;
    while (sOff < sEnd) {
        final int d = min(limit - end, sEnd - sOff);
        s.getChars( sOff, sOff+d, buff, end);
        sOff += d;
        end += d;
        if (end >= limit) {
            flushBuffer();
        }
    }
    }

    // -------------------- Removing data from the buffer --------------------

    public int substract()
        throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadChars(buff, end, buff.length - end);
            if (n < 0) {
                return -1;
            }
        }

        return (buff[start++]);

    }

    public int substract(final CharChunk src)
        throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadChars( buff, end, buff.length - end);
            if (n < 0) {
                return -1;
            }
        }

        final int len = getLength();
        src.append(buff, start, len);
        start = end;
        return len;

    }

    public int substract( final char src[], final int off, final int len )
        throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadChars( buff, end, buff.length - end);
            if (n < 0) {
                return -1;
            }
        }

        int n = len;
        if (len > getLength()) {
            n = getLength();
        }
        System.arraycopy(buff, start, src, off, n);
        start += n;
        return n;

    }


    public void flushBuffer()
    throws IOException
    {
    //assert out!=null
    if( out==null ) {
        throw new IOException( "Buffer overflow, no sink " + limit + " " +
                   buff.length  );
    }
    out.realWriteChars( buff, start, end - start );
    end=start;
    }

    /** Make space for len chars. If len is small, allocate
     *  a reserve space too. Never grow bigger than limit.
     */
    private void makeSpace(final int count)
    {
    char[] tmp = null;

    int newSize;
    int desiredSize=end + count;

    // Can't grow above the limit
    if( limit > 0 &&
        desiredSize > limit) {
        desiredSize=limit;
    }

    if( buff==null ) {
        if( desiredSize < 256 )
         {
            desiredSize=256; // take a minimum
        }
        buff=new char[desiredSize];
    }

    // limit < buf.length ( the buffer is already big )
    // or we already have space XXX
    if( desiredSize <= buff.length) {
        return;
    }
    // grow in larger chunks
    if( desiredSize < 2 * buff.length ) {
        newSize= buff.length * 2;
        if( limit >0 &&
        newSize > limit ) {
            newSize=limit;
        }
        tmp=new char[newSize];
    } else {
        newSize= buff.length * 2 + count ;
        if( limit > 0 &&
        newSize > limit ) {
            newSize=limit;
        }
        tmp=new char[newSize];
    }

    System.arraycopy(buff, 0, tmp, 0, end);
    buff = tmp;
    tmp = null;
    }

    // -------------------- Conversion and getters --------------------

    @Override
    public String toString() {
        if (null == buff) {
            return null;
        } else if (end-start == 0) {
            return "";
        }
        return StringCache.toString(this);
    }

    public String toStringInternal() {
        return new String(buff, start, end-start);
    }

    public int getInt()
    {
    return Ascii.parseInt(buff, start,
                end-start);
    }

    // -------------------- equals --------------------

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(final String s) {
    final char[] c = buff;
    final int len = end-start;
    if (c == null || len != s.length()) {
        return false;
    }
    int off = start;
    for (int i = 0; i < len; i++) {
        if (c[off++] != s.charAt(i)) {
        return false;
        }
    }
    return true;
    }

    /**
     * Compares the message bytes to the specified String object.
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(final String s) {
    final char[] c = buff;
    final int len = end-start;
    if (c == null || len != s.length()) {
        return false;
    }
    int off = start;
    for (int i = 0; i < len; i++) {
        if (Ascii.toLower( c[off++] ) != Ascii.toLower( s.charAt(i))) {
        return false;
        }
    }
    return true;
    }

    public boolean equals(final CharChunk cc) {
    return equals( cc.getChars(), cc.getOffset(), cc.getLength());
    }

    public boolean equals(final char b2[], int off2, final int len2) {
    final char b1[]=buff;
    if( b1==null && b2==null ) {
        return true;
    }

    if (b1== null || b2==null || end-start != len2) {
        return false;
    }
    int off1 = start;
    int len=end-start;
    while ( len-- > 0) {
        if (b1[off1++] != b2[off2++]) {
        return false;
        }
    }
    return true;
    }

    public boolean equals(final byte b2[], int off2, final int len2) {
    final char b1[]=buff;
    if( b2==null && b1==null ) {
        return true;
    }

    if (b1== null || b2==null || end-start != len2) {
        return false;
    }
    int off1 = start;
    int len=end-start;

    while ( len-- > 0) {
        if ( b1[off1++] != (char)b2[off2++]) {
        return false;
        }
    }
    return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public boolean startsWith(final String s) {
    final char[] c = buff;
    final int len = s.length();
    if (c == null || len > end-start) {
        return false;
    }
    int off = start;
    for (int i = 0; i < len; i++) {
        if (c[off++] != s.charAt(i)) {
        return false;
        }
    }
    return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param s the string
     */
    public boolean startsWithIgnoreCase(final String s, final int pos) {
    final char[] c = buff;
    final int len = s.length();
    if (c == null || len+pos > end-start) {
        return false;
    }
    int off = start+pos;
    for (int i = 0; i < len; i++) {
        if (Ascii.toLower( c[off++] ) != Ascii.toLower( s.charAt(i))) {
        return false;
        }
    }
    return true;
    }


    // -------------------- Hash code  --------------------

    // normal hash.
    public int hash() {
    int code=0;
    for (int i = start; i < start + end-start; i++) {
        code = code * 37 + buff[i];
    }
    return code;
    }

    // hash ignoring case
    public int hashIgnoreCase() {
    int code=0;
    for (int i = start; i < end; i++) {
        code = code * 37 + Ascii.toLower(buff[i]);
    }
    return code;
    }

    public int indexOf(final char c) {
    return indexOf( c, start);
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     * @param c the character
     */
    public int indexOf(final char c, final int starting) {
    final int ret = indexOf( buff, start+starting, end, c );
    return (ret >= start) ? ret - start : -1;
    }

    public static int indexOf( final char chars[], int off, final int cend, final char qq )
    {
    while( off < cend ) {
        final char b=chars[off];
        if( b==qq ) {
            return off;
        }
        off++;
    }
    return -1;
    }


    public int indexOf( final String src, final int srcOff, final int srcLen, final int myOff ) {
    final char first=src.charAt( srcOff );

    // Look for first char
    final int srcEnd = srcOff + srcLen;

    for( int i=myOff+start; i <= (end - srcLen); i++ ) {
        if( buff[i] != first ) {
            continue;
        }
        // found first char, now look for a match
            int myPos=i+1;
        for( int srcPos=srcOff + 1; srcPos< srcEnd; ) {
                if( buff[myPos++] != src.charAt( srcPos++ )) {
                    break;
                }
                if( srcPos==srcEnd )
                 {
                    return i-start; // found it
                }
        }
    }
    return -1;
    }

    // -------------------- utils
    private int min(final int a, final int b) {
    if (a < b) {
        return a;
    }
    return b;
    }

    // Char sequence impl

    @Override
    public char charAt(final int index) {
        return buff[index + start];
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        try {
            final CharChunk result = (CharChunk) this.clone();
            result.setOffset(this.start + start);
            result.setEnd(this.start + end);
            return result;
        } catch (final CloneNotSupportedException e) {
            // Cannot happen
            return null;
        }
    }

    @Override
    public int length() {
        return end - start;
    }

}
