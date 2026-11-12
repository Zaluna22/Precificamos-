package com.precificamos.web;

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

/** CRUD de productos y cálculo de precio/simulación (API original, se mantiene por compatibilidad). */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoRepository repo;
    private final PrecificacaoService service;

    public ProdutoController(ProdutoRepository repo, PrecificacaoService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public List<Produto> listar() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto criar(@RequestBody Produto p) { p.setId(null); return repo.save(p); }

    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @RequestBody Produto p) {
        service.buscar(id);
        p.setId(id);
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) { service.buscar(id); repo.deleteById(id); }

    @GetMapping("/{id}/preco")
    public ResultadoPreco preco(@PathVariable Long id,
                                @RequestParam(defaultValue = "MARGEM") MetodoPrecificacao metodo,
                                @RequestParam(defaultValue = "30") BigDecimal valor) {
        return service.precoDoProduto(id, metodo, valor);
    }

    @GetMapping("/{id}/simulacao")
    public ResultadoSimulacao simular(@PathVariable Long id, @RequestParam BigDecimal preco) {
        return service.simular(id, preco);
    }
}
