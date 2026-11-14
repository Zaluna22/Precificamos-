package com.precificamos.segmento;

import com.precificamos.service.RecursoNaoEncontradoException;

import java.util.Arrays;
import java.util.List;

import static com.precificamos.i18n.Textos.t;

/**
 * Categorías amplias de negocio. Cualquier emprendimiento entra en alguna de ellas según
 * <em>cómo</em> obtiene lo que vende (lo elabora, lo fabrica, lo compra listo, lo vende en
 * volumen o lo presta como servicio), no según el rubro puntual.
 */
public enum Segmento {
    PRODUCCION_ALIMENTOS(
            "Producción de alimentos",
            "Gastronomía y alimentos o bebidas elaborados: trabajás con recetas, ingredientes y porciones.",
            "🍰",
            List.of("Confitería y tortas", "Panadería", "Viandas y comida casera", "Hamburguesería / food truck",
                    "Pizzería", "Restaurante o bar", "Heladería", "Cervecería artesanal", "Dulces y conservas",
                    "Brigadeiros y salgados para fiestas")),
    FABRICACION(
            "Fabricación propia",
            "Fabricás o confeccionás piezas con materias primas, insumos y mano de obra.",
            "🧵",
            List.of("Ropa y confección", "Artesanías", "Cosmética y jabones naturales", "Muebles y carpintería",
                    "Bijouterie y joyería", "Velas y aromas", "Impresión 3D", "Calzado y marroquinería",
                    "Lazos y accesorios para el pelo", "Estampado y sublimación")),
    REVENTA(
            "Reventa / comercio minorista",
            "Comprás productos listos a un proveedor y los revendés.",
            "🛍️",
            List.of("Tienda de ropa multimarca", "Electrónica y accesorios para celular", "Juguetería",
                    "Tienda online / dropshipping", "Revendedora de cosméticos por catálogo", "Librería y papelería",
                    "Pet shop (productos)", "Ferretería", "Importados", "Venta en Mercado Libre / Shopee")),
    VENTA_MASIVA(
            "Venta masiva / autoservicio",
            "Muchos productos de distintas categorías y alto giro, con margen por categoría.",
            "🛒",
            List.of("Supermercado", "Almacén / mercadito de barrio", "Autoservicio", "Kiosco y drugstore",
                    "Verdulería", "Distribuidora mayorista", "Farmacia (perfumería y venta libre)",
                    "Tienda de conveniencia", "Dietética a granel", "Minimercado de condominio")),
    SERVICIOS(
            "Servicios",
            "Vendés tu tiempo y conocimiento: cobrás por hora o por servicio realizado.",
            "🛠️",
            List.of("Peluquería y estética", "Manicura y uñas", "Electricista / plomero", "Diseño gráfico y web",
                    "Programación freelance", "Clases particulares", "Limpieza", "Fotografía",
                    "Mecánica", "Consultoría y contabilidad"));

    private final String nombre;
    private final String descripcion;
    private final String icono;
    private final List<String> ejemplos;

    Segmento(String nombre, String descripcion, String icono, List<String> ejemplos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.ejemplos = ejemplos;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getIcono() { return icono; }
    public List<String> getEjemplos() { return ejemplos; }

    /** Busca el segmento por su código sin distinguir mayúsculas ni guiones ({@code produccion-alimentos}). */
    public static Segmento desdeCodigo(String codigo) {
        String normalizado = codigo == null ? "" : codigo.trim().toUpperCase().replace('-', '_');
        return Arrays.stream(values()).filter(s -> s.name().equals(normalizado)).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        t("Segmento '{0}' no existe. Opciones: {1}", codigo, Arrays.toString(values()))));
    }
}
