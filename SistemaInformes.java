import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ==========================================
// 0. VOCABULARIO DEL DOMINIO (ENUMS)
// ==========================================
enum Categoria { ENT, SAL, MIX }
enum TipoInforme { FAC, COM, PED }

// ==========================================
// 1. CAPA DE ABSTRACCIÓN DE PRODUCTOS (CONTRATOS SELLADOS)
// ==========================================
sealed interface Factura permits FacturaEntrada, FacturaSalida, FacturaMixta {
    void procesar(String filename);
}
sealed interface Compra permits CompraEntrada, CompraSalida, CompraMixta {
    void procesar(String filename);
}
sealed interface Pedido permits PedidoEntrada { 
    void procesar(String filename);
}

// ==========================================
// 2. PRODUCTOS CONCRETOS (RECORDS SIN ESTADO)
// ==========================================

// --- FAMILIA: ENTRADA ---
record FacturaEntrada() implements Factura {
    @Override public void procesar(String f) { System.out.println("Procesando informe de ENTRADA de tipo FACTURA -> " + f); }
}
record CompraEntrada() implements Compra {
    @Override public void procesar(String f) { System.out.println("Procesando informe de ENTRADA de tipo COMPRA -> " + f); }
}
record PedidoEntrada() implements Pedido { 
    @Override public void procesar(String f) { System.out.println("Procesando informe de ENTRADA de tipo PEDIDO -> " + f); }
}

// --- FAMILIA: SALIDA ---
record FacturaSalida() implements Factura {
    @Override public void procesar(String f) { System.out.println("Procesando informe de SALIDA de tipo FACTURA -> " + f); }
}
record CompraSalida() implements Compra {
    @Override public void procesar(String f) { System.out.println("Procesando informe de SALIDA de tipo COMPRA -> " + f); }
}

// --- FAMILIA: MIXTA (Evolución A) ---
record FacturaMixta() implements Factura {
    @Override public void procesar(String f) { System.out.println("Procesando informe de MIXTA de tipo FACTURA -> " + f); }
}
record CompraMixta() implements Compra {
    @Override public void procesar(String f) { System.out.println("Procesando informe de MIXTA de tipo COMPRA -> " + f); }
}

// ==========================================
// 3. CAPA DE FÁBRICAS (ABSTRACT FACTORY)
// ==========================================
sealed interface InformeFactory permits EntradaFactory, SalidaFactory, MixtaFactory {
    Factura createFactura();
    Compra createCompra();
    Optional<Pedido> createPedido(); 
}

record EntradaFactory() implements InformeFactory {
    @Override public Factura createFactura() { return new FacturaEntrada(); }
    @Override public Compra createCompra() { return new CompraEntrada(); }
    @Override public Optional<Pedido> createPedido() { return Optional.of(new PedidoEntrada()); }
}

record SalidaFactory() implements InformeFactory {
    @Override public Factura createFactura() { return new FacturaSalida(); }
    @Override public Compra createCompra() { return new CompraSalida(); }
    @Override public Optional<Pedido> createPedido() { return Optional.empty(); } 
}

record MixtaFactory() implements InformeFactory { 
    @Override public Factura createFactura() { return new FacturaMixta(); }
    @Override public Compra createCompra() { return new CompraMixta(); }
    @Override public Optional<Pedido> createPedido() { return Optional.empty(); } 
}

// ==========================================
// 4. PARSEO DEFENSIVO DEL NOMBRE DE ARCHIVO
// ==========================================
record InformeId(Categoria categoria, TipoInforme tipo, String secuencia, String archivo) {

    private static final Pattern NOMBRE_VALIDO =
        Pattern.compile("^(?<cat>ENT|SAL|MIX)_(?<tipo>FAC|COM|PED)_(?<sec>\\d{3})\\.txt$");

    static Optional<InformeId> parse(String raw) {
        if (raw == null) return Optional.empty();
        String nombre = raw.strip();
        Matcher m = NOMBRE_VALIDO.matcher(nombre);
        if (!m.matches()) return Optional.empty();
        return Optional.of(new InformeId(
            Categoria.valueOf(m.group("cat")),      
            TipoInforme.valueOf(m.group("tipo")),
            m.group("sec"),
            nombre));
    }
}

// ==========================================
// 5. MOTOR DE PROCESAMIENTO Y EJECUCIÓN (MAIN)
// ==========================================
public final class SistemaInformes {

    private static final Map<Categoria, InformeFactory> REGISTRO = construirRegistro();

    private static Map<Categoria, InformeFactory> construirRegistro() {
        var mapa = new EnumMap<Categoria, InformeFactory>(Categoria.class);
        mapa.put(Categoria.ENT, new EntradaFactory());
        mapa.put(Categoria.SAL, new SalidaFactory());
        mapa.put(Categoria.MIX, new MixtaFactory());
        return Collections.unmodifiableMap(mapa);
    }

    private SistemaInformes() { }

    public static void procesarInformes(String[] reports) {
        if (reports == null) {
            System.out.println("Error: el arreglo de informes es nulo");
            return;
        }
        for (String report : reports) {
            InformeId.parse(report).ifPresentOrElse(
                SistemaInformes::despachar,
                () -> System.out.println("Error: nombre de informe inválido -> " + report));
        }
    }

    /**
     * Despachador de lógica optimizado mediante "Switch Expression" (Java 21).
     * Nota de Diseño: Al convertir el switch en una asignación funcional (expresión),
     * el compilador de Java EXIGE exhaustividad estricta sobre el enum TipoInforme.
     * No requiere cláusula 'default', garantizando robustez matemática ante futuras expansiones.
     */
    private static void despachar(InformeId id) {
        InformeFactory factory = REGISTRO.get(id.categoria());
        if (factory == null) {
            System.out.println("Error: Fábrica no registrada para la categoría -> " + id.categoria());
            return;
        }

        // El switch actúa como EXPRESIÓN. Si falta un elemento del enum, el código NO COMPILES.
        Runnable accionProcesar = switch (id.tipo()) {
            case FAC -> () -> factory.createFactura().procesar(id.archivo());
            case COM -> () -> factory.createCompra().procesar(id.archivo());
            case PED -> () -> factory.createPedido().ifPresentOrElse(
                p -> p.procesar(id.archivo()),
                () -> System.out.println("Aviso de Diseño: El tipo PEDIDO no está soportado en la categoría " + id.categoria())
            );
        };

        // Ejecución diferida de la acción seleccionada de forma segura
        accionProcesar.run();
    }

    public static void main(String[] args) {
        String[] reports = {
            "ENT_FAC_001.txt",
            "SAL_COM_001.txt",
            "MIX_FAC_001.txt",
            "MIX_COM_001.txt",
            "ENT_PED_001.txt",
            "SAL_PED_002.txt",
            "XXX_FAC_001.txt",   // categoría inválida
            "ENT_FAC_1.txt",     // secuencia mal formada
            null                 // entrada nula
        };

        System.out.println("=== INICIANDO SISTEMA DE PROCESAMIENTO GENERAL DE INFORMES (JAVA 21) ===\n");
        procesarInformes(reports);
    }
}

