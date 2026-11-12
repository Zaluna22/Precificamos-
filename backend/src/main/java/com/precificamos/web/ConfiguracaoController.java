package com.precificamos.web;

import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.service.PrecificacaoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracao")
@CrossOrigin
public class ConfiguracaoController {
    private final PrecificacaoService service;

    public ConfiguracaoController(PrecificacaoService service) { this.service = service; }

    @GetMapping
    public ConfiguracaoNegocio obter() { return service.configuracao(); }

    @PutMapping
    public ConfiguracaoNegocio salvar(@RequestBody ConfiguracaoNegocio c) { return service.salvarConfiguracao(c); }
}
