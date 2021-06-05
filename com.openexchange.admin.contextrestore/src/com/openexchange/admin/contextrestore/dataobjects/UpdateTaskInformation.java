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

package com.openexchange.admin.contextrestore.dataobjects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link UpdateTaskInformation} - Update task information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskInformation {

    /*-
     *
    private final static String REGEX_VALUE = "([^\\),]*)";
    private final static Pattern insertIntoUpdateTaskValues = Pattern.compile("\\((?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)" + REGEX_VALUE + "\\)");

    private static UpdateTaskInformation searchAndCheckUpdateTask(final BufferedReader in, final UpdateTaskInformation updateTaskInformation) throws IOException {
        StringBuilder insert = null;
        String line;
        boolean eoi = false;
        while (!eoi && (line = in.readLine()) != null && !line.startsWith("--")) {
            if (null != insert) {
                insert.append(line);
                if (line.endsWith(");")) {
                    eoi = true;
                }
            } else {
                if (line.startsWith("INSERT INTO `updateTask` VALUES ")) {
                    // Start collecting lines
                    insert = new StringBuilder(2048);
                    insert.append(line);
                }
            }
        }
        if (null != insert) {
            final Matcher matcher = insertIntoUpdateTaskValues.matcher(insert.substring(32));
            insert = null;
            while (matcher.find()) {
                final UpdateTaskEntry updateTaskEntry = new UpdateTaskEntry();
                updateTaskEntry.setContextId(Integer.parseInt(matcher.group(1)));
                updateTaskEntry.setTaskName(matcher.group(2));
                updateTaskEntry.setSuccessful((Integer.parseInt(matcher.group(3)) > 0));
                updateTaskEntry.setLastModified(Long.parseLong(matcher.group(4)));
                updateTaskInformation.add(updateTaskEntry);
            }
        }
        return updateTaskInformation;
    }
     *
     */

    private final Map<String, List<UpdateTaskEntry>> entries;

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation() {
        super();
        entries = new HashMap<String, List<UpdateTaskEntry>>();
    }

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation(final int capacity) {
        super();
        entries = new HashMap<String, List<UpdateTaskEntry>>(capacity);
    }

    /**
     * Gets the size of the update task collection.
     *
     * @return The size
     */
    public int size() {
        return entries.size();
    }

    /**
     * Checks whether the update task collection is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Checks if the update task collection contains specified element.
     *
     * @param e The element possibly contained
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(final UpdateTaskEntry e) {
        return entries.containsKey(e.getTaskName());
    }

    /**
     * Adds specified element.
     *
     * @param e The element to add
     */
    public void add(final UpdateTaskEntry e) {
        if (null == e) {
            return;
        }
        final String taskName = e.getTaskName();
        List<UpdateTaskEntry> list = entries.get(taskName);
        if (null == list) {
            list = new LinkedList<UpdateTaskEntry>();
            entries.put(taskName, list);
        }
        list.add(e);
    }

    /**
     * Removes specified element.
     *
     * @param e The element to remove
     */
    public void remove(final UpdateTaskEntry e) {
        if (null == e) {
            return;
        }
        final String taskName = e.getTaskName();
        List<UpdateTaskEntry> list = entries.get(taskName);
        if (null != list && list.remove(e) && list.isEmpty()) {
            entries.remove(taskName);
        }
    }

    /**
     * Clears the update task collection.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Checks equality to given update task collection.
     *
     * @param other The other update task collection.
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public boolean equalTo(final UpdateTaskInformation other) {
        if (null == other) {
            return false;
        }
        final Map<String, List<UpdateTaskEntry>> m1 = this.entries;
        final Map<String, List<UpdateTaskEntry>> m2 = other.entries;
        // Check by size
        {
            final int size1 = m1.size();
            final int size2 = m2.size();
            if (size1 != size2) {
                return false;
            }
        }
        for (final Entry<String, List<UpdateTaskEntry>> entry : m1.entrySet()) {
            final String taskName = entry.getKey();
            final List<UpdateTaskEntry> list2 = m2.get(taskName);
            if (null == list2) {
                return false;
            }
            final List<UpdateTaskEntry> list1 = entry.getValue();
            if (list1.size() != list2.size()) {
                return false;
            }
            for (final UpdateTaskEntry updateTaskEntry : list1) {
                if (!contained(updateTaskEntry, list1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean contained(final UpdateTaskEntry e, final List<UpdateTaskEntry> list) {
        final String taskName = e.getTaskName();
        final boolean successful = e.isSuccessful();
        for (final UpdateTaskEntry cur : list) {
            if (taskName.equals(cur.getTaskName()) && successful == cur.isSuccessful()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return entries.toString();
    }

}
