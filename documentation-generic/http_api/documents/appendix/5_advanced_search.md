
# Advanced Search


This chapter describes the search module in more detail. Each search request contains a JSON object representing the search term. The search term is embedded
in a wrapping JSON object with the field `filter`, like `{"filter":[search term]}`. In general the structure of a search
term is in prefix notation, meaning the operator is written before its operands: `[">", 5, 2]` represents the condition
"5 > 2".

There are two kinds of search operators, comparison operators and logic operators.

## Comparison operators

Comparison operators have exactly two operands. Each operand can either be a field name or a constant. A field name is a JSON object
with the member `field` specifying the field name, e.g. `{"field":"first_name"}`. The available field names depend on the module
that implements the search. Primitive JSON types are interpreted as constants. Arrays are not valid operands for comparison operators!

| Operator | Description |
|:---------|:------------|
| ">"      | greater |
| "<" | smaller |
| "=" | equal |
| "<=" | smaller or equal | 
| ">=" | greater or equal |
| "<>" | unequal |


## Logic operators

The logic operator "not" has exactly one operand, the other logic operators can have any number of operands. Each operand must be an
array representing a nested search expression.

| Operator |
|:---------|
| "not"    |
| "and"    |
| "or"     |


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

