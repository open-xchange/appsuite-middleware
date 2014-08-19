
package com.openexchange.file.storage.onedrive.rest.folder;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * {@link Folder} - The Folder object contains info about a user's folders in Microsoft OneDrive.
 * <p>
 * <table border="1">
<tbody><tr><th>Structure</th><th>Type</th><th>R/W</th><th>Description</th></tr>
<tr><td>
<p>data</p>
</td><td>
<p><strong>array</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>An array container for <strong>Folder</strong> objects, if a collection of objects is returned.</p>
</td></tr>
<tr><td>
<p>id</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The <strong>Folder</strong> object's ID.</p>
</td></tr>
<tr><td>
<p>from</p>
</td><td>
<p><strong>object</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>Info about the user who created the folder.</p>
</td></tr>
<tr><td>
<p>name (from object)</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The name of the user who created the folder.</p>
</td></tr>
<tr><td>
<p>id (from object)</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The ID of the user who created the folder.</p>
</td></tr>
<tr><td>
<p>name</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>RW</p>
</td><td>
<p>The name of the folder. Required.</p>
</td></tr>
<tr><td>
<p>description</p>
</td><td>
<p><strong>string</strong>/<strong>null</strong></p>
</td><td>
<p>RW</p>
</td><td>
<p>A description of the folder, or <strong>null</strong> if no description is specified.</p>
</td></tr>
<tr><td>
<p>count</p>
</td><td>
<p><strong>number</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The total number of items in the folder.</p>
</td></tr>
<tr><td>
<p>link</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The  URL of the folder, hosted in OneDrive.</p>
</td></tr>
<tr><td>
<p>parent_id</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The resource ID of the parent.</p>
</td></tr>
<tr><td>
<p>upload_location</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The URL to upload items to the folder hosted in OneDrive. Requires the <strong>wl.skydrive</strong> scope.</p>
</td></tr>
<tr><td>
<p>is_embeddable</p>
</td><td>
<p><strong>true/false</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>A  value that  indicates whether this folder can be embedded. If this folder can be embedded, this value is <strong>true</strong>; otherwise, it is <strong>false</strong>.</p>
</td></tr>
<tr><td>
<p>type</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The type of object; in this case, "folder".</p>
</td></tr>
<tr><td>
<p>created_time</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The time, in ISO 8601 format, at which the folder was created.</p>
</td></tr>
<tr><td>
<p>updated_time</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The time, in ISO 8601 format, that the system updated the file last.</p>
</td></tr>
<tr><td>
<p>client_updated_time</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>The time, in ISO 8601 format, that the client machine updated the file last.</p>
</td></tr>
<tr><td>
<p>shared_with</p>
</td><td>
<p><strong>object</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>Permissions info for the folder. Requires the <strong>wl.skydrive</strong> scope.</p>
</td></tr>
<tr><td>
<p>access (shared_with object)</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>R</p>
</td><td>
<p>Info about who can access the folder. The options  are:</p><ul>
<li><strong>People I selected</strong></li>
<li><strong>Just me</strong></li>
<li><strong>Everyone (public)</strong></li>
<li><strong>Friends</strong></li>
<li><strong>My friends and their friends</strong></li>
<li><strong>People with a link</strong></li>
</ul>The default is <strong>Just me</strong>.
</td></tr>
<tr><td>
<p>sort_by</p>
</td><td>
<p><strong>string</strong></p>
</td><td>
<p>RW</p>
</td><td>
<p>Sorts the items to specify the following criteria: updated, name, size, or default. </p>
</td></tr>
</tbody></table>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "from",
    "name",
    "description",
    "parent_id",
    "upload_location",
    "is_embeddable",
    "count",
    "link",
    "type",
    "shared_with",
    "created_time",
    "updated_time"
})
public class Folder {

    @JsonProperty("id")
    private String id;
    @JsonProperty("from")
    private From from;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("upload_location")
    private String uploadLocation;
    @JsonProperty("is_embeddable")
    private Boolean isEmbeddable;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("link")
    private String link;
    @JsonProperty("type")
    private String type;
    @JsonProperty("shared_with")
    private SharedWith sharedWith;
    @JsonProperty("created_time")
    private String createdTime;
    @JsonProperty("updated_time")
    private String updatedTime;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Folder withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("from")
    public From getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(From from) {
        this.from = from;
    }

    public Folder withFrom(From from) {
        this.from = from;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Folder withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Folder withDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("parent_id")
    public String getParentId() {
        return parentId;
    }

    @JsonProperty("parent_id")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Folder withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    @JsonProperty("upload_location")
    public String getUploadLocation() {
        return uploadLocation;
    }

    @JsonProperty("upload_location")
    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }

    public Folder withUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
        return this;
    }

    @JsonProperty("is_embeddable")
    public Boolean getIsEmbeddable() {
        return isEmbeddable;
    }

    @JsonProperty("is_embeddable")
    public void setIsEmbeddable(Boolean isEmbeddable) {
        this.isEmbeddable = isEmbeddable;
    }

    public Folder withIsEmbeddable(Boolean isEmbeddable) {
        this.isEmbeddable = isEmbeddable;
        return this;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    public Folder withCount(Integer count) {
        this.count = count;
        return this;
    }

    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
    }

    public Folder withLink(String link) {
        this.link = link;
        return this;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public Folder withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("shared_with")
    public SharedWith getSharedWith() {
        return sharedWith;
    }

    @JsonProperty("shared_with")
    public void setSharedWith(SharedWith sharedWith) {
        this.sharedWith = sharedWith;
    }

    public Folder withSharedWith(SharedWith sharedWith) {
        this.sharedWith = sharedWith;
        return this;
    }

    @JsonProperty("created_time")
    public String getCreatedTime() {
        return createdTime;
    }

    @JsonProperty("created_time")
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public Folder withCreatedTime(String createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    @JsonProperty("updated_time")
    public String getUpdatedTime() {
        return updatedTime;
    }

    @JsonProperty("updated_time")
    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Folder withUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Folder withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
