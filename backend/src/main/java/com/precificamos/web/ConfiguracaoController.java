package com.precificamos.web;

import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.service.PrecificacaoService;
import org.springframework.web.bind.annotation.*;

/** Lectura y guardado de la configuración general del negocio. */
@RestController
@RequestMapping("/api/configuracao")
public class ConfiguracaoController {
    private final PrecificacaoService service;

    public ConfiguracaoController(PrecificacaoService service) { this.service = service; }

    @GetMapping
    public ConfiguracaoNegocio obter() { return service.configuracao(); }

    @PutMapping
    public ConfiguracaoNegocio salvar(@RequestBody ConfiguracaoNegocio c) { return service.salvarConfiguracao(c); }
}
