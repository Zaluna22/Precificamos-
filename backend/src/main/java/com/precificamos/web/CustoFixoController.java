package com.precificamos.web;

import com.precificamos.auth.UsuarioActual;
import com.precificamos.model.CustoFixo;
import com.precificamos.repository.CustoFixoRepository;
import com.precificamos.service.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de costos fijos mensuales del usuario logueado. */
@RestController
@RequestMapping("/api/custos-fixos")
public class CustoFixoController {
    private final CustoFixoRepository repo;
    private final UsuarioActual actual;

    public CustoFixoController(CustoFixoRepository repo, UsuarioActual actual) {
        this.repo = repo;
        this.actual = actual;
    }

    @GetMapping
    public List<CustoFixo> listar() { return repo.findByUsuarioIdOrderByIdAsc(actual.requerido()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustoFixo criar(@RequestBody CustoFixo c) {
        c.setId(null);
        c.setUsuarioId(actual.requerido());
        return repo.save(c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        // Un costo de otro usuario responde 404, igual que uno inexistente.
        CustoFixo c = repo.findByIdAndUsuarioId(id, actual.requerido())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Custo fixo " + id + " no encontrado"));
        repo.delete(c);
    }
}
