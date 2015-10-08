
package com.openexchange.file.storage.onedrive.rest.file;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.openexchange.file.storage.onedrive.rest.Image;
import com.openexchange.file.storage.onedrive.rest.SharedInfo;
import com.openexchange.file.storage.onedrive.rest.User;

/**
 * {@link RestFile} - The <code>File</code> object contains info about a user's files in Microsoft OneDrive.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "from",
    "name",
    "description",
    "parent_id",
    "size",
    "upload_location",
    "comments_count",
    "comments_enabled",
    "is_embeddable",
    "source",
    "picture",
    "link",
    "type",
    "shared_with",
    "created_time",
    "updated_time",
    "images"
})
public class RestFile {

    @JsonProperty("id")
    private String id;
    @JsonProperty("from")
    private User from;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("size")
    private long size;
    @JsonProperty("upload_location")
    private String uploadLocation;
    @JsonProperty("comments_count")
    private long commentsCount;
    @JsonProperty("comments_enabled")
    private boolean commentsEnabled;
    @JsonProperty("is_embeddable")
    private boolean isEmbeddable;
    @JsonProperty("source")
    private String source;
    @JsonProperty("picture")
    private String picture;
    @JsonProperty("link")
    private String link;
    @JsonProperty("type")
    private String type;
    @JsonProperty("shared_with")
    private SharedInfo sharedWith;
    @JsonProperty("created_time")
    private String createdTime;
    @JsonProperty("updated_time")
    private String updatedTime;
    @JsonProperty("images")
    private Image[] images;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Initializes a new {@link RestFile}.
     */
    public RestFile() {
        super();
        size = -1L;
        sharedWith = new SharedInfo();
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("from")
    public User getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(User from) {
        this.from = from;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("parent_id")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonProperty("size")
    public long getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(long size) {
        this.size = size;
    }

    @JsonProperty("upload_location")
    public String getUploadLocation() {
        return uploadLocation;
    }

    @JsonProperty("upload_location")
    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }

    @JsonProperty("comments_count")
    public long getCommentsCount() {
        return commentsCount;
    }

    @JsonProperty("comments_count")
    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    @JsonProperty("comments_enabled")
    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    @JsonProperty("comments_enabled")
    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }

    @JsonProperty("is_embeddable")
    public boolean isIsEmbeddable() {
        return isEmbeddable;
    }

    @JsonProperty("is_embeddable")
    public void setIsEmbeddable(boolean isEmbeddable) {
        this.isEmbeddable = isEmbeddable;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("picture")
    public String getPicture() {
        return picture;
    }

    @JsonProperty("picture")
    public void setPicture(String picture) {
        this.picture = picture;
    }

    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("shared_with")
    public SharedInfo getSharedWith() {
        return sharedWith;
    }

    @JsonProperty("shared_with")
    public void setSharedWith(SharedInfo sharedWith) {
        this.sharedWith = sharedWith;
    }

    @JsonProperty("created_time")
    public String getCreatedTime() {
        return createdTime;
    }

    @JsonProperty("created_time")
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    @JsonProperty("updated_time")
    public String getUpdatedTime() {
        return updatedTime;
    }

    @JsonProperty("updated_time")
    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    @JsonProperty("images")
    public Image[] getImages() {
        return images;
    }

    @JsonProperty("images")
    public void setImages(Image[] images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
