
package com.copy.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "path",
    "name",
    "link_name",
    "token",
    "permissions",
    "public",
    "type",
    "size",
    "date_last_synced",
    "stub",
    "children",
    "counts",
    "recipient_confirmed",
    "object_available",
    "links",
    "url",
    "thumb"
})
public class Folder {

    @JsonProperty("id")
    private String id;
    @JsonProperty("path")
    private String path;
    @JsonProperty("name")
    private String name;
    @JsonProperty("link_name")
    private String linkName;
    @JsonProperty("token")
    private String token;
    @JsonProperty("permissions")
    private String permissions;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("type")
    private String type;
    @JsonProperty("size")
    private Object size;
    @JsonProperty("date_last_synced")
    private Integer dateLastSynced;
    @JsonProperty("stub")
    private Boolean stub;
    @JsonProperty("children")
    private List<Object> children = new LinkedList<Object>();
    @JsonProperty("counts")
    private List<Object> counts = new LinkedList<Object>();
    @JsonProperty("recipient_confirmed")
    private Boolean recipientConfirmed;
    @JsonProperty("object_available")
    private Boolean objectAvailable;
    @JsonProperty("links")
    private List<Link> links = new LinkedList<Link>();
    @JsonProperty("url")
    private String url;
    @JsonProperty("thumb")
    private Boolean thumb;
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

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("link_name")
    public String getLinkName() {
        return linkName;
    }

    @JsonProperty("link_name")
    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("permissions")
    public String getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("size")
    public Object getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Object size) {
        this.size = size;
    }

    @JsonProperty("date_last_synced")
    public Integer getDateLastSynced() {
        return dateLastSynced;
    }

    @JsonProperty("date_last_synced")
    public void setDateLastSynced(Integer dateLastSynced) {
        this.dateLastSynced = dateLastSynced;
    }

    @JsonProperty("stub")
    public Boolean getStub() {
        return stub;
    }

    @JsonProperty("stub")
    public void setStub(Boolean stub) {
        this.stub = stub;
    }

    @JsonProperty("children")
    public List<Object> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<Object> children) {
        this.children = children;
    }

    @JsonProperty("counts")
    public List<Object> getCounts() {
        return counts;
    }

    @JsonProperty("counts")
    public void setCounts(List<Object> counts) {
        this.counts = counts;
    }

    @JsonProperty("recipient_confirmed")
    public Boolean getRecipientConfirmed() {
        return recipientConfirmed;
    }

    @JsonProperty("recipient_confirmed")
    public void setRecipientConfirmed(Boolean recipientConfirmed) {
        this.recipientConfirmed = recipientConfirmed;
    }

    @JsonProperty("object_available")
    public Boolean getObjectAvailable() {
        return objectAvailable;
    }

    @JsonProperty("object_available")
    public void setObjectAvailable(Boolean objectAvailable) {
        this.objectAvailable = objectAvailable;
    }

    @JsonProperty("links")
    public List<Link> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("thumb")
    public Boolean getThumb() {
        return thumb;
    }

    @JsonProperty("thumb")
    public void setThumb(Boolean thumb) {
        this.thumb = thumb;
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
