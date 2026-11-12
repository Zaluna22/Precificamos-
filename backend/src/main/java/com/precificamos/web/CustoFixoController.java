package com.precificamos.web;

import com.precificamos.model.CustoFixo;
import com.precificamos.repository.CustoFixoRepository;
import com.precificamos.service.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/custos-fixos")
@CrossOrigin
public class CustoFixoController {
    private final CustoFixoRepository repo;

    public CustoFixoController(CustoFixoRepository repo) { this.repo = repo; }

    @GetMapping
    public List<CustoFixo> listar() { return repo.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustoFixo criar(@RequestBody CustoFixo c) { c.setId(null); return repo.save(c); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new RecursoNaoEncontradoException("Custo fixo " + id + " não encontrado");
        repo.deleteById(id);
    }
}
