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

## Declarations

1. All declarations are made with the reserved word `def`. Let's see the syntax rules for declarations depending on their type:

### Functions

1. If the function is global, then its first character must be `g` followed by `f`, which stands for _function_, and then the function name. For example: ```gfSum, gfSub, gfMul, etc.```

2. If the function is local, then its first character must be `f`, which stands for _function_, and then the function name. For example: ```fSum, fSub, fMul, etc.```

### Procedures

1. If the procedure is global, then its first character must be `g` followed by `p`, which stands for _procedure_, and then the procedure name. For example: ```gpShow, gpHide, gpDelete, etc.```

2. If the procedure is local, then its first character must be `p`, which stands for _procedure_, and then the procedure name. For example: ```pShow, pHide, pDelete, etc.```

### Classes

1. If the class is global, then its first character must be `g` followed by `c`, which stands for _class_, and then the class name. For example: ```gcPerson, gcCar, gcHouse, etc.```

2. If the class is local, then its first character must be `c`, which stands for _class_, and then the class name. For example: ```cPerson, cCar, cHouse, etc.```

### Methods and Properties of Classes

1. If the method is abstract _(has no body)_, then a `-` must be placed before the method name, followed by the method name and a pair of parentheses. For example: ```-getName(), -setName(), -getAge(), -setAge(), etc.```

2. If the method is concrete _(has a body)_, then the method name must be followed by a pair of parentheses _(if it has parameters)_ and the body of the method. If the method has no parameters then the parentheses are not requiered, eg: ```getName, setName(psName), getAge, setAge(pnAge), etc.```

3. Properties can be defined either inside the constructor _(pInit() procedure)_ or before the constructor. Only properties with their values are allowed eg: ```sName = "John", nAge = 25, etc.``` and the use of `let` is not allowed to define *properties*, this in order to speed up the reading of the source code.

4. The declaration of the methods also does not allow the use of `def` to define them, this in order to speed up the reading of the source code.

### Parameters of functions, procedures and methods

1. The parameters of functions, procedures and methods must begin with the letter `p` followed by the type of data it stores. ex: ```psName, pnAge, pbState, etc.```

### Modules:

1. Modules follow the same rules as *classes*, only that instead of `c` we use `l` which refers to `library`. ex: ```lPerson, lCar, lHouse, etc.```

### Letters defining data types

| Letter |  Type    |
| ------ | -------- |
|   d    | Date     |
|   t    | DateTime |
|   v    | Variant  |
|   s    | String   |
|   a    | Array    |
|   n    | Number   |
|   b    | Boolean  |
|   o    | Object   |
|   m    | Map      |


### Allowed rules

1. It is only allowed to associate the null value to variables of type `variant` and `object`.
2. If a variable is created without initializing then its value will depend on the letter that defines it. Eg: if the variable is of type `string` then its value will be an empty string '', etc.

### Sum of strings:

1. String addition is done with the `&` operator. Ex: ```"Hello " & "world"``` -> ```"Hello world"```. The only rule is that **the first operand must be a string** and the rest of the operands can be of any type.

### Logical operators:

1. The logical operators are the same as in other programming languages. Ex: ```or, and, not```

### Relational operators:

1. The relational operators are the same as in other programming languages. Ex: ```<, >, <=, >=, ==, !=```.

### Arithmetic operators:

1. The arithmetic operators are the same as in other programming languages. Ex: ```+, -, *, /, %, ^```.

### Assignment operators:

1. Assignment operators are the same as in other programming languages. Ex: ```=, +=, -=, *=, /=```.

## Syntax

### Expressions

```Python
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

## Examples

```Python
# this function add two numbers

def fSum(pnA, pnB)
    return pnA + pnB
end

# this procedure shows a message

def pShow(psMessage)
    println(psMessage)
end

# A person class

def cPerson
    sName = ""
    nAge = 0

    def pInit(psName, pnAge)
        poThis.sName = psName
        poThis.nAge = pnAge
    end

    def fGetName
        return poThis.sName
    end

    def pSetName(psName)
        poThis.sName = psName
    end

    def fGetAge
        return poThis.nAge
    end

    def pSetAge(pnAge)
        poThis.nAge = pnAge
    end
end
```
