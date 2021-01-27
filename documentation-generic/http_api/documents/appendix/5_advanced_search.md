
# Advanced Search


This chapter describes the search module in more detail. Each search request contains a JSON object representing the search term. The search term is embedded
in a wrapping JSON object with the field `filter`, like `{"filter":[search term]}`. In general the structure of a search
term is in prefix notation, meaning the operator is written before its operands: `[">", 5, 2]` represents the condition
"5 > 2".

## Operators 

There are two kinds of search operators, comparison operators and logic operators.

### Comparison operators

Comparison operators compare two operands with one another. The exception is the "isNull" operator which has only one operand which must be a field name.
The following operators are available: 

| Operator | Description      |
|:---------|:-----------------|
| ">"      | greater          |
| "<"      | smaller          |
| "="      | equal            |
| "<="     | smaller or equal | 
| ">="     | greater or equal |
| "<>"     | unequal          |
| "isNull" | is NULL          |


### Logic operators

Logic operators combine search expressions. Whereby the logic operator "not" has exactly one operand, the other logic operators can have any number of operands. 
Each operand must be an array representing a nested search expression.

| Operator |
|:---------|
| "not"    |
| "and"    |
| "or"     |

## Operands

Each operator needs one or more operands. There are different types of operands:

* Constant
* Field
* Attachment
* Header

### Constant

Primitive JSON types are interpreted as constants, but arrays are not valid operands for comparison operators!

### Field

A field operand is a JSON object with the member `field` specifying the field name, e.g. `{"field":"first_name"}`. 
The available field names depend on the module that implements the search.

### Attachment

A attachment operand is a JSON object with the member `attachment` specifying the attachment name, e.g. `{"attachment":"somedocument.txt"}`. 
The attachment search is only available in case the `FILENAME_SEARCH` capability is included in the list of supported_capabilities of the folder. 

### Header

A header operand is a JSON object with the member `header` specifying the header name, e.g. `{"header":"From"}`. 


## Examples

Represents the expression `field_name1 = value1 AND NOT field_name2 > value2`.

```json
{
  "filter" : [
    "and",
      [ "=" , { "field" : "field_name1" }, "value1" ],
      ["not",
        [ ">", { "field" : "field_name2" }, "value2"]
      ]
    ]
}
```

## Infostore Advanced Search

### Operators

The following operators are allowed in search terms.

| Operators |
|:----------|
| "="       |
| ">"       |
| "<"       |

### Operands

The following operands and operators are allowed in search terms.

| Operands               | Valid operator |
|------------------------|:--------------:|
| "camera_aperture"      | ALL            |
| "camera_exposure_time" | ALL            |
| "camera_focal_length"  | AL             |
| "camera_iso_speed"     | ALL            |
| "camera_make"          | "="            |
| "camera_model"         | "="            |
| "capture_date"         | ALL            |
| "categories"           | "="            |
| "color_label"          | ALL            |
| "content"              | "="            |
| "creation_date"        | ALL            |
| "created_by"           | ALL            |
| "current_version"      | ALL            |
| "description"          | "="            |
| "filename"             | "="            |
| "file_md5sum"          | "="            |
| "file_mimetype"        | "="            |
| "file_size"            | ALL            |
| "height"               | ALL            |
| "last_modified"        | ALL            |
| "last_modified_utc"    | ALL            |
| "locked_until"         | ALL            |
| "media_date"           | ALL            |
| "meta"                 | "="            |
| "modified_by"          | ALL            |
| "number_of_versions"   | ALL            |
| "sequence_number"      | ALL            |
| "title"                | "="            |
| "url"                  | "="            |
| "version"              | "="            |
| "version_comment"      | "="            |
| "width"                | ALL            |

### Examples

Represents `Find files having a file_size < 1024`.

```json
{
  "filter" : [ "<" , { "field" : "file_size" }, "1024" ]
}
```

Represents `Find files with filename = "stuff"`.

```json
{
  "filter" : [ "=" ,  { "field" : "filename" }, "stuff" ]
}
```

Represents `Find files with filename "stuff" and file_size < 100 OR having filename = "changelog"`.

```json
{
  "filter" : 
    [ "or",
      [ "and",  
        [ "=" , { "field" : "filename" }, "stuff" ],
        [ "<" , { "field" : "file_size" }, "100" ]
      ],
      [ "=" , { "field" : "filename" }, "changelog" ]
    ]
}
```

## Chronos Advanced Search


### Operators

The following operators are allowed in search terms.

| Operators |
|:----------|
| "="       |
| ">"       |
| "<"       |

### Operands

The following operands and operators are allowed in search terms.

| Operands               | Valid operator |
|------------------------|:--------------:|
| "filename"             | "="            |
| "sequence"             | "="            |
| "timestamp"            | "ALL"          |
| "created"              | "ALL"          |
| "createdBy"            | "="            |
| "lastModified"         | "ALL"          |
| "modifiedBy"           | "="            |
| "calendarUser"         | "="            |
| "summary"              | "="            |
| "location"             | "="            |
| "description"          | "="            |
| "categories"           | "="            |
| "class"                | "="            |
| "color"                | "="            |
| "transp"               | "="            |
| "seriesId"             | "="            |
| "rrule"                | "="            |
| "modifiedBy"           | "="            |
| "recurrenceId"         | "="            |
| "recurrenceDates"      | "="            |
| "changeExceptionDates" | "="            |
| "deleteExceptionDates" | "="            |
| "status"               | "="            |
| "url"                  | "="            |
| "organizer"            | "="            |
| "comment"              | "="            |

### Examples

Find events that were modified after a certain date.

```json
{
  "filter" : [ ">" , { "field" : "lastModified" }, "1612526465493" ]
}
```

Find events with summary = "foobar" and created by user 3.

```json
{
  "filter" : [ 
  	"and", 
  	["=" ,  { "field" : "summary" }, "foobar" ],
  	["=" ,  { "field" : "createdBy" }, "3" ]
  ]
}
````

