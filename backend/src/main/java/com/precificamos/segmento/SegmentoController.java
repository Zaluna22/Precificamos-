package com.precificamos.segmento;

import com.precificamos.segmento.dto.DefinicionSegmento;
import com.precificamos.segmento.dto.ResultadoSegmento;
import com.precificamos.segmento.dto.ResultadoSimulacionSegmento;
import com.precificamos.segmento.dto.SolicitudCalculo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Precificación por categoría de negocio.
 *
 * <ul>
 *   <li>{@code GET /api/segmentos}: categorías, negocios de ejemplo y campos del formulario.</li>
 *   <li>{@code GET /api/segmentos/{segmento}}: una categoría.</li>
 *   <li>{@code POST /api/segmentos/{segmento}/calcular}: precio sugerido, margen y punto de equilibrio.</li>
 *   <li>{@code POST /api/segmentos/{segmento}/simular}: qué pasa si vendo a un precio dado.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/segmentos")
public class SegmentoController {
    private final SegmentoService service;

    public SegmentoController(SegmentoService service) { this.service = service; }

    @GetMapping
    public List<DefinicionSegmento> listar() { return service.definiciones(); }

    @GetMapping("/{segmento}")
    public DefinicionSegmento obtener(@PathVariable String segmento) { return service.definicion(segmento); }

    @PostMapping("/{segmento}/calcular")
    public ResultadoSegmento calcular(@PathVariable String segmento, @RequestBody SolicitudCalculo solicitud) {
        return service.calcular(segmento, solicitud);
    }

    @PostMapping("/{segmento}/simular")
    public ResultadoSimulacionSegmento simular(@PathVariable String segmento, @RequestBody SolicitudCalculo solicitud) {
        return service.simular(segmento, solicitud);
    }
}
