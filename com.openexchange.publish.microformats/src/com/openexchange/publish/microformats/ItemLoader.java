package com.openexchange.publish.microformats;

import com.openexchange.publish.Path;
import com.openexchange.tagging.Tagged;


public interface ItemLoader<T> {
    public T load(Tagged tagged, Path path);
}
