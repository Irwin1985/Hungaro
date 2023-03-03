# Húngaro

A hungarian notation based programming language.

Hungarian is an interpreted programming language whose syntax is based on Hungarian notation with the aim of improving the readability of the source code and avoiding ambiguities in the declaration of variables and constants.

## Roadmap

- [x] Expressions
- [x] Variables
- [x] Functions
- [x] Procedures
- [x] First class functions and procedures
- [x] While loop
- [x] For loop
- [x] Repeat loop
- [x] If/else statement
- [x] Defer Statement
- [x] Simple declare statement
- [x] Commam separated declare statements
- [x] Block (declare - end) of declarations
- [x] Arrays
- [x] Maps
- [ ] Modules
- [x] Builtin type checker
- [x] Classes
- [x] Objects (Instances)
- [x] Variadic Functions
- [x] Optinal Parameters
- [x] Array builtins
- [x] Map builtins
- [x] String builtins
- [x] Function builtins
- [x] Native builtins
- [x] Class builtins

## Variable and Constant declarations

Both variables and constants are declared with the reserved word let. The rules for naming variables and constants are as follows:

### Variables

1. If the variable is global, then its first character must be `g`, and its second character must represent the data type it stores. For example: ```gsName, gnAge, gbState, etc.```

2. If the variable is local, then its first character must represent the data type it stores. For example: ```sName, nAge, bState, etc.```

### Constants
1. All constants must be in uppercase, and the use of underscores to separate words is allowed.

2. If the constant is global, then its first character must be `_` followed by the name of the constant. For example: ```_PI, _EULER, _GOLDEN_RATIO, etc.```

3. If the constant is local, then its first character must be a capital letter followed by the name of the constant. For example: ```PI, EULER, GOLDEN_RATIO, etc.```

## Declarations:
All declarations are made with the reserved word def. Let's see the syntax rules for declarations depending on their type:

### Syntax

#### Expressions

```Ruby
# Strings
"Hello World"

# Numbers (integers and floating points)
1
3.14

# Booleans
true
false

# Arrays
[1, 2, 3]

# Maps
{ "key": "value" }

# Variable declaration
let sLocalName = "John"
let gsGlobalName = "John"

# Variable assignment
sLocalName = "John" # assignments allows the same type.

# Constant declaration
let INTEREST = 0.05 # INTEREST is a local constant
let _CAPITAL = 1000 # _CAPITAL is a global constant

"""
 Local constants start with a capital letter
 Global constants start with an underscore
 Constants cannot be reassigned
"""

```
