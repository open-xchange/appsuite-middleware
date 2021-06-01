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

package com.openexchange.appsuite.history.impl;

import com.openexchange.annotation.NonNull;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * {@link CopyFileVisitor} copies all files and folders from one folder to another
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class CopyFileVisitor extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path target;

    /**
     * Initializes a new {@link CopyFileVisitor}.
     *
     * @param source The source folder
     * @param target The target folder
     * @throw {@link NullPointerException} If either source or target is <code>null</code>
     */
    public CopyFileVisitor(@NonNull Path source, @NonNull Path target) {
        super();
        Objects.requireNonNull(source, "Source must not be null");
        Objects.requireNonNull(target, "Target must not be null");
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path newDirectory = target.resolve(source.relativize(dir));
        copy(dir, newDirectory);
        HistoryUtil.adjustPathOwner(newDirectory);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Copies the given path to the target path with attributes and it replaces an existing file/folder if it exists
     *
     * @param from The source {@link Path}
     * @param to The destination {@link Path}
     * @throws IOException in case of errors
     */
    private void copy(Path from, Path to) throws IOException {
        Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    }


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path newFile = target.resolve(source.relativize(file));
        copy(file, newFile);
        HistoryUtil.adjustPathOwner(newFile);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

}
