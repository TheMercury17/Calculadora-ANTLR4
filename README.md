# Calculadora Aritmetica con ANTLR 4 - Patron de Diseno Visitor

## Informacion del Proyecto
- **Asignatura:** Lenguajes de Programacion
- **Programa:** Ciencias de la Computacion e Inteligencia Artificial
- **Universidad:** Universidad Sergio Arboleda
- **Docente:** Joaquin F. Sanchez
- **Autores:** Grupo 5: Andrés Sebastián Coral Vallejo, Carol Arenas Cardona y Johan Galeano

---

## 1. Introduccion y Fundamentos Teoricos

El presente proyecto implementa una calculadora aritmetica interactiva desarrollada en Java utilizando la herramienta de generacion de analizadores **ANTLR 4** (*ANother Tool for Language Recognition*), fundamentada en los principios de diseno expuestos en *The Definitive ANTLR 4 Reference* de Terence Parr.

El objetivo central de la implementacion es lograr un desacoplamiento estricto entre la especificacion sintactica del lenguaje (gramatica) y la logica de ejecucion y evaluacion de expresiones (semantica). A traves del patron de diseno **Visitor**, ANTLR 4 construye un arbol sintactico (*Parse Tree*) que es recorrido de manera estructurada y modular, permitiendo computar valores, administrar el estado de variables en memoria y manejar errores en tiempo de ejecucion sin contaminar la gramatica con acciones de codigo incrustadas.

---

## 2. Estructura del Proyecto

El repositorio contiene exclusivamente los archivos fuente, las pruebas automatizadas y la documentacion tecnica necesaria para su compilacion y ejecucion:

```text
Calculadora - ANTLR/
|-- LabeledExpr.g4              # Definicion de la gramatica con alternativas etiquetadas
|-- EvalVisitor.java            # Evaluador semantico basado en el patron Visitor
|-- Calc.java                   # Clase principal y orquestador del flujo de ejecucion
|-- README.md                   # Documentacion tecnica del proyecto
|-- .gitignore                  # Exclusion de artefactos binarios y generados
`-- pruebas/                    # Banco de casos de prueba organizados por categoria
    |-- 01_aritmetica_basica.txt
    |-- 02_precedencia_parentesis.txt
    |-- 03_variables_asignacion.txt
    |-- 04_division_por_cero.txt
    |-- 05_comando_clear.txt
    `-- todas.txt
```

---

## 3. Diseno de la Gramatica (`LabeledExpr.g4`)

La gramatica define la sintaxis del lenguaje aritmetico haciendo uso de **etiquetas de regla** (precedidas por `#`), lo cual instruye al generador de ANTLR 4 a producir metodos de visita especializados en la interfaz del Visitor para cada produccion.

```antlr
grammar LabeledExpr;

// ==========================================
// Reglas Sintacticas
// ==========================================

prog
    : stat+
    ;

stat
    : expr NEWLINE                # printExpr
    | ID '=' expr NEWLINE         # assign
    | 'clear' NEWLINE             # clear
    | NEWLINE                     # blank
    ;

expr
    : expr op=('*'|'/') expr      # MulDiv
    | expr op=('+'|'-') expr      # AddSub
    | INT                         # int
    | ID                          # id
    | '(' expr ')'                # parens
    ;

// ==========================================
// Reglas Lexicas (Tokens)
// ==========================================

MUL     : '*' ;
DIV     : '/' ;
ADD     : '+' ;
SUB     : '-' ;

ID      : [a-zA-Z]+ ;
INT     : [0-9]+ ;
NEWLINE : '\r'? '\n' ;
WS      : [ \t]+ -> skip ;
```

### Caracteristicas Principales de la Gramatica:
- **Precedencia de Operadores:** En ANTLR 4, el orden de declaracion de las alternativas en una regla recursiva izquierda define la jerarquia de precedencia. La regla `MulDiv` antecede a `AddSub`, garantizando que multiplicaciones y divisiones se evaluen con mayor prioridad que sumas y restas.
- **Asociatividad:** Por defecto, los operadores binarios en ANTLR 4 presentan asociatividad por la izquierda, respetando el estandar matematico usual.
- **Etiquetas de Alternativa:** Las etiquetas `# printExpr`, `# assign`, `# clear`, `# blank`, `# MulDiv`, `# AddSub`, `# int`, `# id` y `# parens` generan clases de contexto independientes, permitiendo implementar un metodo `visit` particular para cada caso.

---

## 4. Implementacion del Evaluador Semantico (`EvalVisitor.java`)

La clase `EvalVisitor` hereda de `LabeledExprBaseVisitor<Integer>` y encapsula la logica de computo, la gestion del entorno de memoria y el control de excepciones semanticas.

### Funcionalidades Destacadas:
1. **Memoria de Variables (Tabla de Simbolos):** Se emplea una estructura `Map<String, Integer>` para registrar y consultar las asignaciones de variables (`ID = expr`).
2. **Control de Division por Cero:** Ante una operacion de division donde el denominador sea igual a `0`, se captura la condicion, se emite una advertencia en el flujo de error estandar (`System.err`) y se retorna `null` para evitar interrupciones abruptas por excepciones no controladas.
3. **Tratamiento de Variables no Inicializadas:** Si se intenta referenciar un identificador que no ha sido previamente asignado en el mapa de memoria, se notifica el error semantico y se asigna el valor por defecto `0`.
4. **Comando `clear`:** Permite restablecer el entorno de ejecucion vaciando la tabla de variables mediante `memory.clear()`.

---

## 5. Modulo Principal (`Calc.java`)

La clase `Calc` inicializa y enlaza las fases del compilador:
1. Recibe la entrada desde un archivo de texto pasado como argumento por linea de comandos o, en su defecto, desde el flujo estandar `System.in`.
2. Instancia `LabeledExprLexer` sobre un `CharStream` para tokenizar los caracteres de entrada.
3. Genera un `CommonTokenStream` y construye el `LabeledExprParser`.
4. Genera el arbol sintactico invocando la regla raiz `prog()`.
5. Si no se detectan errores sintacticos (`getNumberOfSyntaxErrors() == 0`), invoca `EvalVisitor` para recorrer y evaluar el arbol.

---

## 6. Instrucciones de Compilacion y Ejecucion

### Requisitos Previos
- Java JDK 11 o superior instalado y configurado en el `PATH`.
- ANTLR 4 (version 4.13.x recomendada) disponible como archivo JAR o mediante la herramienta CLI de ANTLR.

---

### Paso 1: Generacion del Parser y Visitor con ANTLR 4
Desde la raiz del directorio del proyecto (`Calculadora - ANTLR`):

**Linux / macOS:**
```bash
antlr4 -no-listener -visitor LabeledExpr.g4
```
*O utilizando el JAR directamente:*
```bash
java -jar /ruta/a/antlr-4.13.2-complete.jar -no-listener -visitor LabeledExpr.g4
```

**Windows (PowerShell / CMD):**
```powershell
java -jar "C:\ruta\a\antlr-4.13.2-complete.jar" -no-listener -visitor LabeledExpr.g4
```

---

### Paso 2: Compilacion del Codigo Java

**Linux / macOS:**
```bash
javac -cp "$HOME/.local/lib/antlr-4.13.2-complete.jar:." *.java
```

**Windows (PowerShell):**
```powershell
javac -cp "C:\ruta\a\antlr-4.13.2-complete.jar;." *.java
```

---

### Paso 3: Ejecucion de Casos de Prueba

#### Suite Completa:
**Linux / macOS:**
```bash
java -cp "$HOME/.local/lib/antlr-4.13.2-complete.jar:." Calc pruebas/todas.txt
```
**Windows (PowerShell):**
```powershell
java -cp "C:\ruta\a\antlr-4.13.2-complete.jar;." Calc pruebas\todas.txt
```

#### Pruebas Individuales por Escenario:
```bash
# 1. Operaciones aritmeticas basicas
java -cp <RUTA_ANTLR_JAR>: Calc pruebas/01_aritmetica_basica.txt

# 2. Precedencia y parentesis
java -cp <RUTA_ANTLR_JAR>: Calc pruebas/02_precedencia_parentesis.txt

# 3. Asignacion y uso de variables
java -cp <RUTA_ANTLR_JAR>: Calc pruebas/03_variables_asignacion.txt

# 4. Manejo de division por cero y variables no inicializadas
java -cp <RUTA_ANTLR_JAR>: Calc pruebas/04_division_por_cero.txt

# 5. Comando clear para reinicio de memoria
java -cp <RUTA_ANTLR_JAR>: Calc pruebas/05_comando_clear.txt
```

---

## 7. Banco de Pruebas y Resultados de Ejecucion

### Escenario 1: Operaciones Aritmeticas Basicas (`01_aritmetica_basica.txt`)
Verifica la correcta resolucion de sumas, restas, multiplicaciones y divisiones sobre numeros enteros.

- **Entrada:**
```text
10 + 5
20 - 4
3 * 8
50 / 2
100 + 200 - 50
```

- **Salida en Consola:**
```text
15
16
24
25
250
```

---

### Escenario 2: Precedencia de Operadores y Parentesis (`02_precedencia_parentesis.txt`)
Valida la jerarquia de operaciones (`*` y `/` sobre `+` y `-`) y la alteracion de precedencia mediante parentesis.

- **Entrada:**
```text
1 + 2 * 3
(1 + 2) * 3
100 / 2 + 5 * 4
(50 - 10) / (2 + 2)
```

- **Salida en Consola:**
```text
7
9
70
10
```

---

### Escenario 3: Variables, Asignacion y Memoria (`03_variables_asignacion.txt`)
Comprueba el almacenamiento dinamico de valores en variables y su reutilizacion en expresiones posteriores.

- **Entrada:**
```text
a = 15
b = 5
c = a + b * 2
c
resultado = (a + b) / 2
resultado
```

- **Salida en Consola:**
```text
a = 15
b = 5
c = 25
25
resultado = 10
10
```

---

### Escenario 4: Manejo de Excepciones Semanticas (`04_division_por_cero.txt`)
Evalua la robustez del evaluador ante operaciones no permitidas (division por cero) y variables inexistentes.

- **Entrada:**
```text
10 / 0
denominador = 0
100 / denominador
z + 10
```

- **Salida en Consola:**
```text
Error semantico: Division por cero en la expresion '10/0'.
denominador = 0
Error semantico: Division por cero en la expresion '100/denominador'.
Error semantico: La variable 'z' no ha sido inicializada. Se asume valor 0.
10
```

---

### Escenario 5: Comando `clear` (`05_comando_clear.txt`)
Comprueba la limpieza de la memoria de variables y su efecto sobre expresiones subsiguientes.

- **Entrada:**
```text
x = 50
y = 20
x + y
clear
x + y
```

- **Salida en Consola:**
```text
x = 50
y = 20
70
Memoria de variables reiniciada.
Error semantico: La variable 'x' no ha sido inicializada. Se asume valor 0.
Error semantico: La variable 'y' no ha sido inicializada. Se asume valor 0.
0
```

---

### Escenario 6: Suite Completa Integrada (`todas.txt`)

- **Entrada:**
```text
10 + 5
20 - 4
3 * 8
50 / 2
1 + 2 * 3
(1 + 2) * 3
a = 10
b = 20
a + b * 2
(a + b) * 2
10 / 0
clear
```

- **Salida en Consola:**
```text
15
16
24
25
7
9
a = 10
b = 20
50
60
Error semantico: Division por cero en la expresion '10/0'.
Memoria de variables reiniciada.
```

---

## 8. Conclusiones

- La utilizacion del patron Visitor en ANTLR 4 proporciona una separacion limpia entre el analisis sintactico y la evaluacion semantica, facilitando el mantenimiento y la extension del lenguaje sin alterar la gramatica base.
- El mecanismo de etiquetas en alternativas gramaticales optimiza la generacion de codigo, permitiendo implementar un metodo especifico para cada regla en lugar de un despachador condicional monolitico.
- El manejo explicito de tablas de simbolos y el control defensivo sobre operaciones invalidas (division por cero y variables no inicializadas) garantiza la estabilidad y robustez del interprete aritmetico.
