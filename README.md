# Laboratorio Tutorial: Calculadora Cientifica Graficadora con ANTLR 4
## Patron de Diseno Visitor y Construccion de un DSL Matematico

### Informacion Institucional y del Proyecto
- **Asignatura:** Lenguajes de Programacion y Traduccion
- **Programa:** Ciencias de la Computacion e Inteligencia Artificial
- **Universidad:** Universidad Sergio Arboleda
- **Docente:** Joaquin F. Sanchez
- **Autores:** Grupo 5: Andrés Sebastián Coral Vallejo, Carol Arenas Cardona y Johan Galeano

---

## 1. Proposito y Arquitectura del Sistema

El proposito de este laboratorio es construir progresivamente un lenguaje de dominio especifico (**DSL**) para el calculo matematico y la representacion grafica de funciones continuas y discontinuas. El interprete parte de operaciones aritmeticas elementales y evoluciona hacia una calculadora cientifica completa capaz de gestionar memoria dinamica para variables, constantes universales, operadores unarios, funciones trigonometricas y trascendentes, y comandos de graficacion interactiva con renderizado en 2D.

### Flujo de Procesamiento del Interprete

```text
Entrada del Usuario (Consola o Archivo)
                 |
                 v
        +-----------------+
        |  Lexer (ANTLR)  | ----> Transforma flujo de caracteres en Tokens
        +-----------------+
                 |
                 v
        +-----------------+
        | Parser (ANTLR)  | ----> Construye el Arbol Sintactico (Parse Tree)
        +-----------------+
                 |
                 v
        +-----------------+
        | Arbol Sintactico|
        +-----------------+
                 |
                 v
    +-------------------------+
    |  ScientificEvalVisitor  | ----> Recorre el arbol y ejecuta la semantica
    +-------------------------+
       /          |          \
      v           v           v
  Calculo      Memoria     Comandos / Graficacion
 Numerico     Variables         (PlotWindow)
```

---

## 2. Estructura del Repositorio

El proyecto mantiene una estructura modular y limpia, conteniendo exclusivamente el codigo fuente, las pruebas estructuradas y la documentacion tecnica:

```text
Calculadora - ANTLR/
|-- ScientificCalc.g4           # Especificacion de la gramatica y reglas lexicas
|-- ScientificEvalVisitor.java  # Evaluador semantico basado en el patron Visitor
|-- PlotWindow.java             # Motor de renderizado visual 2D con Java Swing
|-- Main.java                   # Punto de entrada principal y orquestador del interprete
|-- Calc.java                   # Alias de compatibilidad hacia Main
|-- ejemplos.txt                # Suite de prueba integral segun el tutorial
|-- README.md                   # Documentacion tecnica, respuestas y reporte de retos
|-- .gitignore                  # Exclusion de binarios y archivos generados
`-- pruebas/                    # Banco estructurado de casos de prueba
    |-- 01_aritmetica_reales.txt
    |-- 02_variables_memoria.txt
    |-- 03_potencias_unarios.txt
    |-- 04_funciones_cientificas.txt
    |-- 05_comandos_clear_vars.txt
    |-- 06_graficacion_plot.txt
    |-- 07_retos_extendidos.txt
    `-- todas.txt
```

---

## 3. Diseno de la Gramatica (`ScientificCalc.g4`)

La gramatica define de forma declarativa la sintaxis de las expresiones y comandos admitidos. Cada alternativa de produccion cuenta con su respectiva **etiqueta** (`# nombreRegla`), permitiendo que ANTLR genere metodos de visita desacoplados en el Visitor.

```antlr
grammar ScientificCalc;

// ==========================================
// Reglas Sintacticas
// ==========================================

prog
    : stat+ (EOF)?
    ;

stat
    : expr NEWLINE                                                    # printExpr
    | ID '=' expr NEWLINE                                             # assign
    | 'clear' NEWLINE                                                 # clear
    | 'vars' NEWLINE                                                  # showVars
    | 'plot' '(' expr ',' expr ',' expr ')' NEWLINE                   # plotExpr
    | 'plot' '(' expr ',' expr ',' expr ',' expr ',' expr ')' NEWLINE # plotRangeExpr
    | 'plot' '(' expr ',' expr ',' expr ',' expr ')' NEWLINE          # plotMultiExpr
    | NEWLINE                                                         # blank
    ;

expr
    : <assoc=right> expr '^' expr                                     # power
    | op=('+'|'-') expr                                               # unary
    | expr op=('*'|'/') expr                                          # mulDiv
    | expr op=('+'|'-') expr                                          # addSub
    | function '(' expr ')'                                           # functionCall
    | function2 '(' expr ',' expr ')'                                 # function2Call
    | constant                                                        # constantExpr
    | NUMBER                                                          # number
    | ID                                                              # id
    | '(' expr ')'                                                    # parens
    ;

function
    : 'sin' | 'cos' | 'tan' | 'asin' | 'acos' | 'atan'
    | 'sqrt' | 'log' | 'ln' | 'abs' | 'exp' | 'floor' | 'ceil'
    ;

function2
    : 'pow' | 'max' | 'min'
    ;

constant
    : 'pi' | 'e'
    ;

// ==========================================
// Reglas Lexicas (Tokens)
// ==========================================

MUL     : '*' ;
DIV     : '/' ;
ADD     : '+' ;
SUB     : '-' ;
POW     : '^' ;

NUMBER  : [0-9]+ ('.' [0-9]+)? ;
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
NEWLINE : '\r'? '\n' ;
WS      : [ \t]+ -> skip ;
```

---

## 4. Respuestas y Analisis de las Secciones del Tutorial

### Seccion 6: Analisis de Reconocimiento del Token `ID`
Dada la expresion regular `ID : [a-zA-Z_][a-zA-Z_0-9]* ;`:

| Texto | ¿Es reconocido? | Justificacion Tecnica |
|---|---|---|
| `variable` | **Si** | Inicia con letra y contiene unicamente caracteres alfabeticos validos. |
| `x2` | **Si** | Inicia con letra alfabética `x` seguida de un digito `2`. |
| `2x` | **No** | Comienza con un digito numérico (`2`), lo cual viola la restriccion de inicio `[a-zA-Z_]`. El lexer lo interpretaria inicialmente como parte de un token `NUMBER`. |
| `_resultado` | **Si** | El caracter de subrayado `_` es valido como caracter inicial y continuacion. |
| `variable-final` | **No** | El guion `-` no pertenece al conjunto `[a-zA-Z_0-9]`. El lexer reconoce el identificador `variable`, luego el token operador `SUB` (`-`), y finalmente el identificador `final`. |

---

### Seccion 7 y 8: Etiquetas de Produccion y Metodos del Visitor
**¿Por que resulta conveniente tener un metodo diferente para cada operacion y nodo?**  
Al etiquetar cada alternativa (por ejemplo `# mulDiv`, `# addSub`, `# power`), ANTLR genera interfaces fuertemente tipadas (`visitMulDiv`, `visitAddSub`, `visitPower`). Esto evita sentencias condicionales monoliticas (`if/else` o `switch` sobre operadores) en un unico metodo generico, permitiendo que la logica de cada operacion resida en su propia funcion modular, facilitando el mantenimiento y la extensibilidad del sistema.

---

### Seccion 14: Precedencia de Operadores
**¿Por que `2+3*4` produce `14.0` en lugar de `20.0`?**  
En ANTLR 4, el orden de definicion en la regla recursiva izquierda `expr` establece la jerarquia de precedencia. Al declarar `mulDiv` antes que `addSub`, las operaciones de multiplicacion y division se anidan a mayor profundidad en el arbol sintactico. Por ende, en la evaluacion post-orden del Visitor, el subarbol `3*4` (12.0) se computa primero, y su resultado es posteriormente sumado con `2.0`, obteniendo `14.0`.

---

### Seccion 16: Evaluacion de Variables no Definidas
**¿Por que emitir una advertencia y asumir 0.0 frente a lanzar una excepcion fatal?**  
En un entorno interactivo y continuo de tipo REPL o calculo de graficas, lanzar una excepcion no controlada detendria de forma abrupta la sesion del usuario o el muestreo de los 800 puntos de una funcion. Registrar el error en `System.err` y retornar un valor por defecto seguro (`0.0`) preserva la disponibilidad del interprete y ofrece retroalimentacion inmediata al usuario.

---

### Seccion 18 y 22: Potencias y Operadores Unarios
1. **Asociatividad de la Potencia:** Mediante la directiva `<assoc=right>`, la operacion `2^2^3` se agrupa como `2^(2^3) = 2^8 = 256.0`, respetando la convencion matematica internacional a diferencia de la asociatividad por izquierda convencional.
2. **Operadores Unarios:** La regla `op=('+'|'-') expr # unary` permite evaluar correctamente prefijos de signo como `-10`, `-2+5 = 3.0` y `abs(-10) = 10.0` sin requerir tokens especiales para numeros negativos en el lexer.

---

### Seccion 32: Muestreo de Funciones y Discontinuidades
**¿Que efecto tiene `Double.isFinite(y)` sobre la grafica de `plot(1/x, -5, 5)`?**  
Cuando $x = 0$, la division en precision real en Java produce `Infinity`, `-Infinity` o `NaN`. Al condicionar el almacenamiento de muestras mediante `if (Double.isFinite(y))`:
- Se descartan puntos infinitos que deformarian la escala vertical de la ventana.
- El trazador grafico (`PlotWindow`) detecta la discontinuidad y corta el trazo del `Path2D`, dibujando correctamente las dos ramas hiperbolicas independientes sin conectar artificialmente $-\infty$ con $+\infty$ con una linea vertical espuria.

---

### Seccion 35: Transformacion de Coordenadas y Orientacion del Eje Y
El espacio matematico se define en el intervalo real $[x_{min}, x_{max}] \times [y_{min}, y_{max}]$, mientras que el espacio grafico de Java Swing se mide en coordenadas de pantalla discretas $[0, \text{width}] \times [0, \text{height}]$ con el origen $(0,0)$ en la esquina superior izquierda.  
Las formulas de mapeo lineal implementadas son:
$$px = \text{padding} + \left( \frac{x - x_{min}}{x_{max} - x_{min}} \right) \cdot \text{plotWidth}$$
$$py = (\text{height} - \text{padding}) - \left( \frac{y - y_{min}}{y_{max} - y_{min}} \right) \cdot \text{plotHeight}$$
El termino `(height - padding) - ...` invierte el eje Y para reflejar el sentido matematico convencional donde los valores positivos crecen hacia arriba.

---

### Seccion 39: Analisis del Arbol Sintactico para `sin(x) + 2*x^2`
El parse tree construido presenta la siguiente jerarquia:
- **Raiz:** Nodo `addSub` correspondiente al operador `+`.
- **Hijo Izquierdo:** Nodo `functionCall` correspondiente a `sin(x)`, donde se invoca la funcion `sin` con el argumento `x`.
- **Hijo Derecho:** Nodo `mulDiv` con operador `*`:
  - Operando izquierdo: Nodo `number` con valor `2`.
  - Operando derecho: Nodo `power` con base `x` (nodo `id`) y exponente `2` (nodo `number`).

---

### Seccion 41: Preguntas Finales del Tutorial

1. **¿Cual es la responsabilidad del Lexer?**  
   Escanear la secuencia lineal de caracteres del archivo o consola y agruparlos en unidades minimas con significado llamadas **Tokens** (palabras clave, numeros, operadores, identificadores), ignorando espacios en blanco y comentarios.

2. **¿Cual es la responsabilidad del Parser?**  
   Verificar que la secuencia de tokens cumpla con las reglas sintacticas de la gramatica y construir el **Arbol de Sintaxis Abstracta / Parse Tree**, detectando errores de estructura.

3. **¿Que funcion cumplen las etiquetas como `# addSub` o `# functionCall`?**  
   Permiten a ANTLR generar clases de contexto diferenciadas y metodos de visita especificos en la clase base del Visitor, desacoplando la logica semantica por cada produccion.

4. **¿Que ventaja ofrece el patron Visitor?**  
   Permite separar completamente la gramatica de la ejecucion. Es posible implementar multiples analisis (evaluador aritmetico, optimizador de expresiones, generador de codigo C, formateador de arbol) sin modificar una sola linea de la gramatica.

5. **¿Que representa la tabla de simbolos?**  
   Una estructura de datos asociativa (`Map<String, Double>`) que almacena el estado de las variables del lenguaje, vinculando cada nombre de identificador con su valor numerico actual.

6. **¿Por que la variable `x` cambia continuamente durante una grafica?**  
   Porque para trazar una curva continua $y = f(x)$, el comando `plot` divide el dominio $[x_{min}, x_{max}]$ en 800 muestras discretas $x_i$, asignando sucesivamente cada $x_i$ en la tabla de simbolos y evaluando el arbol de la funcion.

7. **¿Por que podemos evaluar el mismo arbol sintactico varias veces?**  
   Porque el arbol sintactico es inmutable respecto al texto fuente. Al variar el contexto de la memoria (el valor de `x`), una nueva visita al arbol produce el valor $f(x)$ correspondiente sin necesidad de volver a tokenizar o re-parsear la expresion.

8. **¿Que sucede cuando se intenta graficar una funcion con una discontinuidad?**  
   Genera puntos indeterminados o infinitos. El interprete valida cada punto con `Double.isFinite(y)` y segmenta el trazo grafico para evitar deformaciones en el plano.

9. **¿Que modificaciones serian necesarias para funciones con dos argumentos?**  
   Crear una regla gramatical `function2 '(' expr ',' expr ')'` y en el Visitor implementar el metodo correspondiente evaluando ambos subarboles y delegando en metodos como `Math.pow(v1, v2)`, `Math.max(v1, v2)` o `Math.min(v1, v2)`.

10. **¿Por que la calculadora desarrollada puede considerarse un DSL (Domain-Specific Language)?**  
    Porque define una sintaxis concisa y optimizada especificamente para un dominio cerrado: el analisis, computo numerico y graficacion de funciones matematicas.

---

## 5. Implementacion de Retos de Extension (Seccion 42)

El proyecto incluye la implementacion completa de los retos propuestos en el tutorial:

### Reto 1: Funciones Cientificas Adicionales
Se agregaron las funciones trigonometricas inversas y de redondeo:
- `asin(x)` $\rightarrow$ `Math.asin(x)`
- `acos(x)` $\rightarrow$ `Math.acos(x)`
- `atan(x)` $\rightarrow$ `Math.atan(x)`
- `floor(x)` $\rightarrow$ `Math.floor(x)`
- `ceil(x)` $\rightarrow$ `Math.ceil(x)`

### Reto 2: Funciones con Dos Argumentos
Se incorporo en la gramatica la produccion:
```antlr
expr : function2 '(' expr ',' expr ')' # function2Call ;
function2 : 'pow' | 'max' | 'min' ;
```
Permitiendo ejecutar llamadas como `pow(2, 8) = 256.0`, `max(10, 25) = 25.0` y `min(10, 25) = 10.0`.

### Reto 3: Rango Vertical Explicito en Graficacion
Se extendio la sintaxis del comando `plot` para recibir limites verticales:
```antlr
stat : 'plot' '(' expr ',' expr ',' expr ',' expr ',' expr ')' NEWLINE # plotRangeExpr ;
```
Ejemplo de ejecucion: `plot(1/x, -5, 5, -10, 10)` graficando en $X \in [-5, 5]$ con corte en $Y \in [-10, 10]$.

### Reto 4: Graficacion Simultanea de Multiples Funciones
Se habilito la sintaxis para superposicion de curvas:
```antlr
stat : 'plot' '(' expr ',' expr ',' expr ',' expr ')' NEWLINE # plotMultiExpr ;
```
Ejemplo de ejecucion: `plot(sin(x), cos(x), -6.28, 6.28)` renderizando ambas curvas con colores y leyendas independientes.

### Reto 5: Diseno Sintactico para Definicion de Funciones en Tiempo de Ejecucion
Para soportar definiciones como `f(x) = x^2 + 2*x + 1`, la gramatica se extiende de la siguiente forma:
```antlr
stat
    : ID '(' ID ')' '=' expr NEWLINE  # defineFunction
    ;

expr
    : ID '(' expr ')'                 # customFunctionCall
    ;
```
**Estrategia semantica en Visitor:** Se almacena en una tabla de funciones (`Map<String, FunctionDef>`) el nombre del parametro formal y la referencia al subarbol `expr`. Cuando se invoque `f(5)`, se asigna `x = 5`, se visita el subarbol guardado y se retorna el resultado.

---

## 6. Lista de Comprobacion de Requerimientos (Seccion 43)

| Caracteristica Requerida | Estado | Archivo / Metodo Responsable |
|---|---|---|
| Numeros reales (`Double`) | **Implementado** | `ScientificCalc.g4`, `ScientificEvalVisitor.java (visitNumber)` |
| Suma (`+`) | **Implementado** | `ScientificEvalVisitor.java (visitAddSub)` |
| Resta (`-`) | **Implementado** | `ScientificEvalVisitor.java (visitAddSub)` |
| Multiplicacion (`*`) | **Implementado** | `ScientificEvalVisitor.java (visitMulDiv)` |
| Division (`/`) | **Implementado** | `ScientificEvalVisitor.java (visitMulDiv)` |
| Parentesis (`(...)`) | **Implementado** | `ScientificEvalVisitor.java (visitParens)` |
| Variables en memoria | **Implementado** | `ScientificEvalVisitor.java (visitAssign, visitId)` |
| Potencia (`^`) | **Implementado** | `ScientificEvalVisitor.java (visitPower)` |
| Operadores unarios (`+`, `-`) | **Implementado** | `ScientificEvalVisitor.java (visitUnary)` |
| Constantes matematicas (`pi`, `e`) | **Implementado** | `ScientificEvalVisitor.java (visitConstantExpr)` |
| Funciones cientificas | **Implementado** | `ScientificEvalVisitor.java (visitFunctionCall, visitFunction2Call)` |
| Comando `clear` | **Implementado** | `ScientificEvalVisitor.java (visitClear)` |
| Comando `vars` | **Implementado** | `ScientificEvalVisitor.java (visitShowVars)` |
| Comando `plot` | **Implementado** | `ScientificEvalVisitor.java (visitPlotExpr, visitPlotRangeExpr, visitPlotMultiExpr)` |
| Visualizacion grafica 2D | **Implementado** | `PlotWindow.java (paintComponent, transformacion de coordenadas)` |

---

## 7. Instrucciones de Compilacion y Ejecucion

### Requisitos
- Java JDK 11 o superior.
- Libreria `antlr-4.13.x-complete.jar` disponible en el sistema.

### Paso 1: Generar analizadores con ANTLR 4
```bash
# Linux / macOS
antlr4 -no-listener -visitor ScientificCalc.g4

# O con archivo JAR directo:
java -jar /ruta/antlr-4.13.2-complete.jar -no-listener -visitor ScientificCalc.g4
```

```powershell
# Windows PowerShell
java -jar "C:\ruta\antlr-4.13.2-complete.jar" -no-listener -visitor ScientificCalc.g4
```

### Paso 2: Compilar codigo Java
```bash
# Linux / macOS
javac -cp "$HOME/.local/lib/antlr-4.13.2-complete.jar:." *.java

# Windows PowerShell
javac -cp "C:\ruta\antlr-4.13.2-complete.jar;." *.java
```

### Paso 3: Ejecucion de Casos de Prueba y Ejemplos
```bash
# Ejecutar suite de ejemplos del tutorial
java -cp <RUTA_JAR>: Main ejemplos.txt

# Ejecutar pruebas individuales
java -cp <RUTA_JAR>: Main pruebas/01_aritmetica_reales.txt
java -cp <RUTA_JAR>: Main pruebas/02_variables_memoria.txt
java -cp <RUTA_JAR>: Main pruebas/03_potencias_unarios.txt
java -cp <RUTA_JAR>: Main pruebas/04_funciones_cientificas.txt
java -cp <RUTA_JAR>: Main pruebas/05_comandos_clear_vars.txt
java -cp <RUTA_JAR>: Main pruebas/06_graficacion_plot.txt
java -cp <RUTA_JAR>: Main pruebas/07_retos_extendidos.txt
java -cp <RUTA_JAR>: Main pruebas/todas.txt
```

---

## 8. Evidencias de Ejecucion de Casos de Prueba

### Caso 1: Aritmetica Real y Precedencia (`ejemplos.txt`)
- **Entrada:**
```text
2+2
2+3*4
(2+3)*4
```
- **Salida:**
```text
4.0
14.0
20.0
```

### Caso 2: Variables y Constantes
- **Entrada:**
```text
radio = 10
area = pi * radio^2
area
```
- **Salida:**
```text
314.1592653589793
```

### Caso 3: Funciones Trigonometricas y Trascendentes
- **Entrada:**
```text
sin(pi/2)
cos(0)
log(100)
ln(e)
sqrt(25)
2^8
```
- **Salida:**
```text
1.0
1.0
2.0
1.0
5.0
256.0
```

### Caso 4: Comandos `vars` y `clear`
- **Entrada:**
```text
a = 10
b = 20
vars
clear
vars
```
- **Salida:**
```text
a = 10.0
b = 20.0
Memoria eliminada.
No hay variables definidas.
```

### Caso 5: Graficacion Interactiva
- **Entrada:**
```text
plot(x^2, -10, 10)
plot(sin(x), -6.28, 6.28)
plot(sin(x), cos(x), -6.28, 6.28)
plot(1/x, -5, 5, -10, 10)
```
- **Salida en Consola:**
```text
Graficando funcion: f(x) = x^2 en [-10.0, 10.0]
Graficando funcion: f(x) = sin(x) en [-6.28, 6.28]
Graficando multiples curvas: sin(x) y cos(x)
Graficando funcion con rango: f(x) = 1/x en X: [-5.0, 5.0], Y: [-10.0, 10.0]
```
*(Se despliegan las ventanas graficas interactivas con renderizado antialiasing, ejes cartesianos y cuadricula de escala).*
