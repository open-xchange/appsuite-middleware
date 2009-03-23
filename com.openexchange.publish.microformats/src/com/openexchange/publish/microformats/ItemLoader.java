package com.openexchange.publish.microformats;

import java.util.List;
import com.openexchange.publish.Path;


public interface ItemLoader<T> {
    public List<T> load(int folderId, Path path);
}
