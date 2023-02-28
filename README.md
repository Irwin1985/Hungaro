# Húngaro

A hungarian notation based programming language.


### Roadmap

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
- [ ] Class reflection

### Syntax

#### Expressions

```hungarian
# Strings
"Hello World"

# Numbers
1

# Booleans
true

# Arrays
[1, 2, 3]

# Maps
{ "key": "value" }

# Variable declaration
declare lsName = "John" # ls = local string
declare gsName = "John" # gs = global string

# Variable assignment
lsName = "John" # only strings can be assigned to strings
lsName = null # all variables can be assigned to null
lsName = 10 # this will throw an error because 10 is not a string

# Constant declaration
declare INTEREST = 0.05 # INTEREST is a local constant
declare _CAPITAL = 1000 # _CAPITAL is a global constant

"""
 Local constants start with a capital letter
 Global constants start with an underscore
 Constants cannot be reassigned
"""

```
