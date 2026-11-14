package com.precificamos.web;

import com.precificamos.auth.UsuarioActual;
import com.precificamos.dto.ResultadoPreco;
import com.precificamos.dto.ResultadoSimulacao;
import com.precificamos.model.MetodoPrecificacao;
import com.precificamos.model.Produto;
import com.precificamos.repository.ProdutoRepository;
import com.precificamos.service.PrecificacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * CRUD de productos y cálculo de precio/simulación (API original, se mantiene por compatibilidad).
 * Todo se limita a los productos del usuario logueado.
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoRepository repo;
    private final PrecificacaoService service;
    private final UsuarioActual actual;

    public ProdutoController(ProdutoRepository repo, PrecificacaoService service, UsuarioActual actual) {
        this.repo = repo;
        this.service = service;
        this.actual = actual;
    }

    @GetMapping
    public List<Produto> listar() { return repo.findByUsuarioIdOrderByIdAsc(actual.requerido()); }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id) { return service.buscar(actual.requerido(), id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto criar(@RequestBody Produto p) {
        p.setId(null);
        p.setUsuarioId(actual.requerido());
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @RequestBody Produto p) {
        Long usuario = actual.requerido();
        service.buscar(usuario, id);
        p.setId(id);
        p.setUsuarioId(usuario);
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) { repo.delete(service.buscar(actual.requerido(), id)); }

    @GetMapping("/{id}/preco")
    public ResultadoPreco preco(@PathVariable Long id,
                                @RequestParam(defaultValue = "MARGEM") MetodoPrecificacao metodo,
                                @RequestParam(defaultValue = "30") BigDecimal valor) {
        return service.precoDoProduto(actual.requerido(), id, metodo, valor);
    }

    @GetMapping("/{id}/simulacao")
    public ResultadoSimulacao simular(@PathVariable Long id, @RequestParam BigDecimal preco) {
        return service.simular(actual.requerido(), id, preco);
    }
}
