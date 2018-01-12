
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

| Operator | Description |
|:---------|:------------|
| ">"      | greater |
| "<" | smaller |
| "=" | equal |
| "<=" | smaller or equal | 
| ">=" | greater or equal |
| "<>" | unequal |
| "isNull" | is NULL |


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

```json
{
  "filter":[
    "and",
    [
      "=",
      {
        "field":"field_name1"
      },
      "value1"
    ],
    [
      "not",
      [
        ">",
        {
          "field":"field_name2"
        },
        "value2"
      ]
    ]
  ]
}
```

Represents the expression `field_name1 = value1 AND NOT field_name2 > value2`.

