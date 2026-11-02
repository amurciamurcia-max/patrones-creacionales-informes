# Sistema de Procesamiento de Informes - Abstract Factory Avanzado (Java 21)

Repositorio formal con la resolución de la actividad práctica de **Patrones de Creación**, modelado y optimizado bajo estándares contemporáneos de arquitectura de software.

## 🚀 Arquitectura y Patrones Aplicados

El sistema implementa el patrón **Abstract Factory (Fábrica Abstracta)** para aislar la lógica cliente de la instanciación de familias de objetos relacionados (Categorías vs. Tipos de Informe), mitigando la obsesión por primitivos y garantizando la coherencia estructural del dominio.

### Características de Alta Ingeniería Incluidas:
* **Programación Defensiva (Parse, Don't Validate):** Validación y tipado estático estricto en la frontera del sistema a través de expresiones regulares (`Pattern` inmutable) y encapsulamiento seguro en el record `InformeId`.
* **Tipos de Datos Algebraicos (ADT):** Jerarquías cerradas mediante `sealed interface` y la cláusula `permits` para control exhaustivo en tiempo de compilación.
* **Preservación de LSP con Mónadas:** Sustitución honesta de retornos `null` mediante el uso de `Optional<T>`, gestionando la asimetría de productos de forma segura.
* **Despacho Exhaustivo:** Uso de expresiones `switch` funcionales (Java 21) que garantizan la cobertura total de variantes operacionales en la fase de compilación.

---

## 📊 Diagramas de Arquitectura UML (Sintaxis Mermaid)

### 1. Estado Inicial del Sistema (Antes de la Evolución)
```mermaid
classDiagram
    class InformeFactory {
        <<interface>>
        +createFactura() Factura
        +createCompra() Compra
    }
    class EntradaFactory {
        +createFactura() Factura
        +createCompra() Compra
    }
    class SalidaFactory {
        +createFactura() Factura
        +createCompra() Compra
    }
    InformeFactory <|.. EntradaFactory
    InformeFactory <|.. SalidaFactory

    class Factura { <<interface>> +procesar(String name) }
    class Compra { <<interface>> +procesar(String name) }

    class FacturaEntrada { +procesar(String name) }
    class CompraEntrada { +procesar(String name) }
    Factura <|.. FacturaEntrada
    Compra <|.. CompraEntrada

    class FacturaSalida { +procesar(String name) }
    class CompraSalida { +procesar(String name) }
    Factura <|.. FacturaSalida
    Compra <|.. CompraSalida

    EntradaFactory ..> FacturaEntrada : instantiates
    EntradaFactory ..> CompraEntrada : instantiates
    SalidaFactory ..> FacturaSalida : instantiates
    SalidaFactory ..> CompraSalida : instantiates
```

### 2. Estado Final del Sistema (Después de las Evoluciones A y B)
```mermaid
classDiagram
    class InformeFactory {
        <<interface>>
        +createFactura() Factura
        +createCompra() Compra
        +createPedido() Optional~Pedido~
    }
    class EntradaFactory {
        +createFactura() Factura
        +createCompra() Compra
        +createPedido() Optional~Pedido~
    }
    class SalidaFactory {
        +createFactura() Factura
        +createCompra() Compra
        +createPedido() Optional~Pedido~
    }
    class MixtaFactory {
        +createFactura() Factura
        +createCompra() Compra
        +createPedido() Optional~Pedido~
    }
    InformeFactory <|.. EntradaFactory
    InformeFactory <|.. SalidaFactory
    InformeFactory <|.. MixtaFactory

    class Factura { <<interface>> }
    class Compra { <<interface>> }
    class Pedido { <<interface>> }

    Factura <|.. FacturaEntrada
    Factura <|.. FacturaSalida
    Factura <|.. FacturaMixta

    Compra <|.. CompraEntrada
    Compra <|.. CompraSalida
    Compra <|.. CompraMixta

    Pedido <|.. PedidoEntrada

    EntradaFactory ..> FacturaEntrada : instantiates
    EntradaFactory ..> CompraEntrada : instantiates
    EntradaFactory ..> PedidoEntrada : instantiates

    SalidaFactory ..> FacturaSalida : instantiates
    SalidaFactory ..> CompraSalida : instantiates

    MixtaFactory ..> FacturaMixta : instantiates
    MixtaFactory ..> CompraMixta : instantiates
```

---

## 🛠️ Requisitos e Instrucciones de Ejecución

* **Java Development Kit (JDK):** Versión 21 o superior de forma mandatoria.

Para compilar y ejecutar el ecosistema de pruebas defensivas desde la terminal, ejecute:

```bash
# Compilación de la unidad estructural
javac SistemaInformes.java

# Ejecución del motor general
java SistemaInformes
```
