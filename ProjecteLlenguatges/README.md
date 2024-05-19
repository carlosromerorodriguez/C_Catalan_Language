# Ç Language
<p align="center">
  <img src="resources/LOGO_ç.png" alt="Ç language logo" width="300" style="margin:auto;"/>
</p>

## Table of Contents
  - [Overview](#overview)
  - [Features](#features)
  - [Installation](#installation-and-setup)
  - [External dependecies](#external-dependencies)
  - [Usage](#usage)
  - [File Examples](#file-examples)
  - [Ç Basic Rules](#ç-basic-rules)
  - [Reserved Keywords](#reserved-keywords)
  - [Group members](#group-members)
  - [References](#references)


## Overview
**Ç** language introduces a revolutionary language that is poised to redefine programming paradigms. Inspired by the robustness of the C family, this language is designed to be both accessible and versatile. Embracing the essence of the Catalan language and culture, it represents a bold step towards linguistic diversity in the software industry.

This language is more than just a tool for coding; it's a gateway to a new era of programming excellence. With a syntax akin to pseudocode, it simplifies the coding experience for learners while offering the power and flexibility sought by seasoned developers. Whether you're a novice eager to explore the world of programming or a seasoned professional seeking innovation, our language promises to elevate your coding journey to new heights.
## Features
+ **Compiled language**
+ **Simplicity**: Pseudocode-like syntax for easy comprehension.
+ **Strong Typing**: Robust error-checking and code reliability.
+ **Multiple types allowed**:
    + Integer: enter
    + Float: decimal
    + Boolean: siono
+ **Error handling**: Proactive error management for smooth workflows.
+ **Dynamic generation**: Ability to dynamically compile based on imported grammar.

## External dependencies
The compiler relies on the following external dependency:

+ **Gson (v2.3.1)**: Is a Java library that can be used to convert Java Objects into their JSON representation and vice versa. It is used to read grammar.json.

## Installation and setup
#### 1. Download our project
#### 2. Navigate to directory:
```bash
cd ProjecteLlenguatges
```
#### 3. Select Ç input file path in main.java:
```java
private static final String FILE_PATH = "path/to/file.ç";
```

#### 4. Select Grammar JSON path:
```java
private static final String GRAMMAR_PATH = "path/to/grammar.json";
```

#### 5. Select MIPS output path:
```java
private static final String MIPS_FILE_PATH = "path/to/mips.asm";
```

## Usage
Once the setup is complete, follow these steps to use our compiler:

1. Execute main.java.
2. Copy MIPS output file to [MARS MIPS Compiler](https://courses.missouristate.edu/kenvollmar/mars/download.htm).
3. Check register to get expected output values.

## File Examples

### Multiplication table
```
proces enter mostraTaulaMultiplicar(enter: numero) fer:
    enter: iterador = 1 ç
    enter: resultat = 0 ç
    mentre (iterador <= 10) fer:
        resultat = numero * iterador ç
        mostra(numero + " * " + iterador + " = " + resultat + "\n") ç
        iterador = iterador + 1 ç
    fi

    retorna numero ç
fi

proces Calçot() fer:
    enter: n = 5 ç
    enter: ignorar = mostraTaulaMultiplicar(n) ç
fi
```
### Recursive Fibonacci
```
proces enter Fibonacci(enter: n) fer:
    enter: retornValue ç
    si(n < 2) fer:
        retornValue = n ç
    fisi
    sino fer:
        retornValue = Fibonacci(n - 1) + Fibonacci(n - 2) ç
    fisino

    retorna retornValue ç
fi

proces Calçot() fer:
    enter: valorFibonacci = Fibonacci(12) ç
fi
```

### Recursive Factorial
```
proces enter Factorial(enter: n) fer:
    enter: retornValue = 0 ç
    si(n igual 0) fer:
        retornValue = 1 ç
    fisi
    sino fer:
        retornValue = n * Factorial(n - 1) ç
    fisino

    retorna retornValue ç
fi

proces Calçot() fer:
    enter: valorFactorial = Factorial(5) ç
fi
```

### Recursive Power
```
proces enter potencia(enter: x, enter: n) fer:
    enter: resultat ç
    si(n igual 0) fer:
        resultat = 1 ç
    fisi
    sino fer:
        si(x igual 0) fer:
            resultat = 0 ç
        fisi
        sino fer:
            resultat = x * potencia(x, n - 1) ç
        fisino
    fisino

    retorna resultat ç
fi

proces Calçot () fer:
    enter: a = 3 ç
    enter: b = 3 ç
    enter: resultatPotencia = potencia(a, b) ç
fi
```

## Ç Basic Rules
+ You can use two types of comments:
  + One-line comment:
  ```
  proces Calçot() fer:
    xiuxiueja enter: valorFactorial = 5 ç
    enter: a = 10 ç
  fi
  ```
  + Multiline comment:
  ```
  proces Calçot() fer:
    comenta enter: valorFactorial = 5 ç
    enter: a = 10 ç
    ficomenta
  fi
  ```
+ Variables must be initialized before using them.
+ In 'mostra' operations there are special rule: 
  + You can concatenate custom strings with "enter" varibles:
  ```mostra("El número a és igual a " + a)```
  + You can not add '(' or ')' inside 'mostra': ~~mostra("El valor de (a) és " + a)~~
+ Returns are only allowed at the end of functions:
```
proces siono mesGran(enter: a, enter: b) fer:
  siono: resultat = fals ç
  
  si(a > b) fer:
    resultat = cert ç
  fisi
  
  sino fer:
    resultat = fals ç
  fisino
  
  retorna resultat ç
fi

proces Calçot() fer:
    enter: a = 3 ç
    enter: b = 5 ç

    siono: resultat = mesGran(a, b) ç
fi
```
+ In 'si' and 'mentre' statements all members of condition have to be the same type.

## Reserved Keywords:

### Comments 
+ ```xiuxiueja```
+ ```comenta```
+ ```ficomenta```

### Vartypes
+ ```enter```
+ ```decimal```
+ ```siono```
+ ```res```

### Functions
+ ```proces```
+ ```retorna```

### Inicializers and terminators
+ ```fer```
+ ```fi```
+ ```fisino```
+ ```ç```

### Conditional
+ ```si``` 
+ ```sino``` 

### Iterator
+ ```mentre``` 
+ ```per```
+ ```sumant```
+ ```restant```

### Matematical operators
+ ```+```
+ ```-```
+ ```*```
+ ```/```

### Boolean operators
+ ```i```
+ ```o```
+ ```no```
+ ```>```
+ ```>=```
+ ```<```
+ ```<=```
+ ```igual```
+ ```diferent```

### Misc
+ ```Calçot```
+ ```mostra```
+ ```=```
+ ```:```
+ ```(```
+ ```)```
+ Words containing ç or ñ



## Group members
Joaquim Angàs Jordana - joaquim.angas@students.salle.url.edu

Pol Cardenal Granell - pol.cardenal@students.salle.url.edu

Pol Guarch Bosom - pol.guarch@salle.url.edu

Oriol Rebordosa Cots - oriol.rebordosa@students.salle.url.edu

Carlos Romero Rodríguez  - c.romero@students.salle.url.edu

## References
+ [MIPS32 cheatsheet and instruction set.](https://uweb.engr.arizona.edu/~ece369/Resources/spim/MIPSReference.pdf)
+ [MIPS32 function handling.](https://courses.cs.washington.edu/courses/cse378/09wi/lectures/lec05.pdf)
+ [Aho, Alfred. Compilers: Principles, Techniques, and Tools (2006)](https://repository.unikom.ac.id/48769/1/Compilers%20-%20Principles%2C%20Techniques%2C%20and%20Tools%20%282006%29.pdf)