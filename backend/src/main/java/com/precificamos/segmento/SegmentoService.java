package com.precificamos.segmento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.segmento.dto.*;
import com.precificamos.service.DatosInvalidosException;
import com.precificamos.service.PrecificacaoService;
import com.precificamos.service.RecursoNaoEncontradoException;
import com.precificamos.service.RegraDeNegocioException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.precificamos.segmento.CalculadoraBase.*;

/**
 * Punto único de cálculo por segmento: elige la estrategia, completa los parámetros comunes
 * con la configuración guardada, valida los datos y arma el resultado o la simulación.
 */
@Service
public class SegmentoService {
    private final Map<Segmento, EstrategiaPrecificacion<?>> estrategias = new EnumMap<>(Segmento.class);
    private final PrecificacaoService precificacao;
    private final ObjectMapper mapper;
    /** Validador con mensajes en español. */
    private final Validator validador = Validation.byProvider(HibernateValidator.class).configure()
            .defaultLocale(Locale.forLanguageTag("es"))
            .buildValidatorFactory().getValidator();

    public SegmentoService(List<EstrategiaPrecificacion<?>> lista, PrecificacaoService precificacao, ObjectMapper mapper) {
        lista.forEach(e -> estrategias.put(e.segmento(), e));
        this.precificacao = precificacao;
        this.mapper = mapper;
    }

    /** Todas las categorías con ejemplos y pasos del formulario, en el orden del enum. */
    public List<DefinicionSegmento> definiciones() {
        return Arrays.stream(Segmento.values()).filter(estrategias::containsKey).map(this::definicion).toList();
    }

    /** Definición de una categoría (código insensible a mayúsculas). */
    public DefinicionSegmento definicion(String codigo) {
        return definicion(Segmento.desdeCodigo(codigo));
    }

    private DefinicionSegmento definicion(Segmento s) {
        List<PasoFormulario> pasos = new ArrayList<>(estrategia(s).pasos());
        pasos.add(pasoComun());
        return new DefinicionSegmento(s.name(), s.getNombre(), s.getDescripcion(), s.getIcono(), s.getEjemplos(), pasos);
    }

    /** Último paso de todos los formularios: ventas, costos fijos, impuestos y tasas. */
    private PasoFormulario pasoComun() {
        ConfiguracaoNegocio c = precificacao.configuracao();
        return new PasoFormulario("Ventas, costos fijos e impuestos",
                "Lo que comparte todo tu negocio. Se completa con tu Configuración y tus Costos fijos.", List.of(
                CampoFormulario.de("ventasMensualesProducto", "¿Cuántas unidades de esto vendés por mes?", "entero", true,
                        "Piezas, porciones o servicios. Si no sabés, poné una estimación", null).comun(),
                CampoFormulario.de("unidadesMensualesNegocio", "Total de unidades que vende todo el negocio por mes", "entero", false,
                        "Sirve para repartir los costos fijos entre todos tus productos. Vacío = igual al anterior",
                        c.getTotalUnidadesMes() != null && c.getTotalUnidadesMes() > 0 ? c.getTotalUnidadesMes() : null).comun(),
                CampoFormulario.de("costosFijosMensuales", "Costos fijos del mes", "moneda", false,
                        "Alquiler, luz, internet, contador, DAS del MEI…", r(precificacao.totalFixo())).comun(),
                CampoFormulario.de("impuestoPct", "Impuestos sobre la venta (%)", "porcentaje", false,
                        "Simples Nacional: tu alícuota (ej.: 6 %). MEI: 0 y sumá el DAS en costos fijos", c.getImpostoPct()).comun(),
                CampoFormulario.de("tarjetaPct", "Tasa de tarjeta / medio de pago (%)", "porcentaje", false,
                        "Débito ~1–2 %, crédito ~3–5 %", c.getTaxaCartaoPct()).comun(),
                CampoFormulario.de("marketplacePct", "Comisión de marketplace o app (%)", "porcentaje", false,
                        "Mercado Libre, Shopee, iFood… 0 si vendés directo", c.getTaxaMarketplacePct()).comun(),
                CampoFormulario.de("comisionPct", "Comisión de vendedores (%)", "porcentaje", false, null, c.getComissaoPct()).comun()));
    }

    /** Calcula el precio sugerido y todos los indicadores del segmento. */
    public ResultadoSegmento calcular(String codigo, SolicitudCalculo solicitud) {
        return calcular(estrategia(Segmento.desdeCodigo(codigo)), solicitud);
    }

    /** Simula la venta al precio indicado en {@code solicitud.precio()}. */
    public ResultadoSimulacionSegmento simular(String codigo, SolicitudCalculo solicitud) {
        if (solicitud == null || solicitud.precio() == null) {
            throw new DatosInvalidosException("Falta el precio a simular", Map.of("precio", "es obligatorio"));
        }
        return simular(estrategia(Segmento.desdeCodigo(codigo)), solicitud);
    }

    private <T> ResultadoSegmento calcular(EstrategiaPrecificacion<T> est, SolicitudCalculo s) {
        ComunesResueltos c = resolverComunes(s);
        T entrada = leerEntrada(est, s);
        CostoVariable costo = est.costoVariable(entrada, c);
        PropuestaPrecio p = est.proponerPrecio(entrada, costo, c);
        // Se analiza el precio ya redondeado a centavos, que es el que se cobra de verdad
        // (así el resultado coincide con el simulador usando el mismo precio).
        BigDecimal precio = r(p.precio());
        Analisis a = analizar(precio, costo.total(), c);
        List<String> avisos = new ArrayList<>(p.avisos());
        if (a.puntoEquilibrioUnidades() != null && a.puntoEquilibrioUnidades() > c.ventasMensualesProducto()
                && c.unidadesMensualesNegocio() == c.ventasMensualesProducto()) {
            avisos.add("Con " + c.ventasMensualesProducto() + " unidades por mes no llegás al punto de equilibrio ("
                    + a.puntoEquilibrioUnidades() + "). Vendé más, bajá costos o subí el precio.");
        }
        return new ResultadoSegmento(est.segmento(), costo.nombre(), r(costo.total()), r(a.rateo()), r(a.costoTotal()),
                precio, p.metodo(), r(a.tasas()), r(a.lucro()), r(a.margenPct()), a.puntoEquilibrioUnidades(),
                r(a.puntoEquilibrioFacturacion()), r(a.lucroMensual()), costo.desglose(), p.extras(), avisos);
    }

    private <T> ResultadoSimulacionSegmento simular(EstrategiaPrecificacion<T> est, SolicitudCalculo s) {
        ComunesResueltos c = resolverComunes(s);
        CostoVariable costo = est.costoVariable(leerEntrada(est, s), c);
        Analisis a = analizar(s.precio(), costo.total(), c);
        int ventas = c.ventasMensualesProducto();
        return new ResultadoSimulacionSegmento(r(a.precio()), r(a.costoTotal()), r(a.tasas()), r(a.contribucion()),
                r(a.lucro()), r(a.margenPct()), a.puntoEquilibrioUnidades(), ventas,
                r(a.precio().multiply(BigDecimal.valueOf(ventas))), r(a.lucroMensual()), a.lucro().signum() >= 0);
    }

    /** Convierte {@code datos} al DTO de la estrategia y lo valida. */
    private <T> T leerEntrada(EstrategiaPrecificacion<T> est, SolicitudCalculo s) {
        JsonNode datos = s == null ? null : s.datos();
        if (datos == null || datos.isNull() || datos.isEmpty()) {
            throw new DatosInvalidosException("Faltan los datos del segmento", Map.of("datos", "es obligatorio"));
        }
        T entrada;
        try {
            entrada = mapper.convertValue(datos, est.tipoEntrada());
        } catch (IllegalArgumentException e) {
            throw new DatosInvalidosException("Datos con formato inválido: " + causaRaiz(e), Map.of());
        }
        validar(entrada, "datos.");
        return entrada;
    }

    private ComunesResueltos resolverComunes(SolicitudCalculo s) {
        ParametrosComunes p = s == null || s.comun() == null ? ParametrosComunes.vacios() : s.comun();
        validar(p, "comun.");
        ConfiguracaoNegocio cfg = precificacao.configuracao();
        BigDecimal fijos = p.costosFijosMensuales() != null ? p.costosFijosMensuales() : precificacao.totalFixo();
        BigDecimal pct = fraccion(primero(p.impuestoPct(), cfg.getImpostoPct()))
                .add(fraccion(primero(p.tarjetaPct(), cfg.getTaxaCartaoPct())))
                .add(fraccion(primero(p.marketplacePct(), cfg.getTaxaMarketplacePct())))
                .add(fraccion(primero(p.comisionPct(), cfg.getComissaoPct())));
        Integer unidadesCfg = cfg.getTotalUnidadesMes() != null && cfg.getTotalUnidadesMes() > 0 ? cfg.getTotalUnidadesMes() : null;
        Integer ventas = p.ventasMensualesProducto() != null ? p.ventasMensualesProducto() : unidadesCfg;
        if (ventas == null) {
            throw new DatosInvalidosException("Indicá cuántas unidades vendés por mes",
                    Map.of("comun.ventasMensualesProducto", "es obligatorio"));
        }
        int unidades = p.unidadesMensualesNegocio() != null ? p.unidadesMensualesNegocio()
                : unidadesCfg != null ? unidadesCfg : ventas;
        if (pct.compareTo(BigDecimal.ONE) >= 0) {
            throw new RegraDeNegocioException("Impuestos y tasas suman 100 % o más del precio");
        }
        return new ComunesResueltos(fijos, pct, ventas, unidades, cero(cfg.getValorHora()));
    }

    private void validar(Object objeto, String prefijo) {
        Set<ConstraintViolation<Object>> errores = validador.validate(objeto);
        if (errores.isEmpty()) return;
        Map<String, String> campos = new TreeMap<>();
        errores.forEach(v -> campos.put(prefijo + v.getPropertyPath(), v.getMessage()));
        throw new DatosInvalidosException("Revisá los datos ingresados", campos);
    }

    private EstrategiaPrecificacion<?> estrategia(Segmento s) {
        EstrategiaPrecificacion<?> e = estrategias.get(s);
        if (e == null) throw new RecursoNaoEncontradoException("Segmento " + s + " sin estrategia");
        return e;
    }

    private static BigDecimal primero(BigDecimal a, BigDecimal b) { return a != null ? a : b; }

    private static String causaRaiz(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m.split("\n")[0];
    }
}
