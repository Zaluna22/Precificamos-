package com.precificamos.meusprodutos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.precificamos.auth.UsuarioActual;
import com.precificamos.segmento.Segmento;
import com.precificamos.service.DatosInvalidosException;
import com.precificamos.service.RecursoNaoEncontradoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.precificamos.i18n.Textos.t;

/**
 * "Meus produtos": productos calculados que el usuario guardó, con su precio final.
 * Todo exige sesión y se limita a los productos del usuario logueado (los de otro dan 404).
 */
@RestController
@RequestMapping("/api/meus-produtos")
public class ProdutoSalvoController {
    private final ProdutoSalvoRepository repo;
    private final UsuarioActual actual;
    private final ObjectMapper mapper;

    public ProdutoSalvoController(ProdutoSalvoRepository repo, UsuarioActual actual, ObjectMapper mapper) {
        this.repo = repo;
        this.actual = actual;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ProdutoSalvoDto> listar() {
        return repo.findByUsuarioIdOrderByActualizadoEnDescIdDesc(actual.requerido()).stream().map(this::dto).toList();
    }

    @GetMapping("/{id}")
    public ProdutoSalvoDto buscar(@PathVariable Long id) {
        return dto(propio(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoSalvoDto crear(@Valid @RequestBody SolicitudProdutoSalvo s) {
        ProdutoSalvo p = new ProdutoSalvo();
        p.setUsuarioId(actual.requerido());
        copiar(s, p);
        return dto(repo.save(p));
    }

    @PutMapping("/{id}")
    @Transactional
    public ProdutoSalvoDto actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudProdutoSalvo s) {
        ProdutoSalvo p = propio(id);
        copiar(s, p);
        return dto(repo.saveAndFlush(p));
    }

    @PatchMapping("/{id}")
    @Transactional
    public ProdutoSalvoDto cambiar(@PathVariable Long id, @Valid @RequestBody CambioProdutoSalvo c) {
        ProdutoSalvo p = propio(id);
        if (c.nombre() != null) {
            if (c.nombre().isBlank()) {
                throw new DatosInvalidosException(t("Poné un nombre al producto"), Map.of("nombre", t("Poné un nombre al producto")));
            }
            p.setNombre(c.nombre().trim());
        }
        if (c.precioFinal() != null) p.setPrecioFinal(c.precioFinal());
        return dto(repo.saveAndFlush(p));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable Long id) {
        repo.delete(propio(id));
    }

    /** Producto del usuario logueado; si no existe o es de otro usuario, 404. */
    private ProdutoSalvo propio(Long id) {
        return repo.findByIdAndUsuarioId(id, actual.requerido())
                .orElseThrow(() -> new RecursoNaoEncontradoException(t("Producto {0} no encontrado", id)));
    }

    private void copiar(SolicitudProdutoSalvo s, ProdutoSalvo p) {
        String segmento;
        try {
            segmento = Segmento.desdeCodigo(s.segmento()).name();
        } catch (RecursoNaoEncontradoException e) {
            throw new DatosInvalidosException(t("Indicá la categoría"), Map.of("segmento", t("Indicá la categoría")));
        }
        p.setSegmento(segmento);
        p.setNombre(s.nombre().trim());
        p.setComunJson(json(s.comun()));
        p.setDatosJson(json(s.datos()));
        p.setPrecioSugerido(s.precioSugerido());
        p.setCostoTotalUnitario(s.costoTotalUnitario());
        p.setMargenRealPct(s.margenRealPct());
        p.setLucroUnitario(s.lucroUnitario());
        p.setPrecioFinal(s.precioFinal() != null ? s.precioFinal() : s.precioSugerido());
    }

    private String json(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return "{}";
        try {
            return mapper.writeValueAsString(n);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private JsonNode leer(String json) {
        try {
            return mapper.readTree(json == null || json.isBlank() ? "{}" : json);
        } catch (JsonProcessingException e) {
            return mapper.createObjectNode();
        }
    }

    private ProdutoSalvoDto dto(ProdutoSalvo p) {
        return new ProdutoSalvoDto(p.getId(), p.getSegmento(), p.getNombre(), leer(p.getComunJson()), leer(p.getDatosJson()),
                p.getPrecioSugerido(), p.getCostoTotalUnitario(), p.getMargenRealPct(), p.getLucroUnitario(),
                p.getPrecioFinal(), p.getCreadoEn(), p.getActualizadoEn());
    }
}
