package com.precificamos.web;

import com.precificamos.auth.UsuarioActual;
import com.precificamos.model.ConfiguracaoNegocio;
import com.precificamos.service.PrecificacaoService;
import org.springframework.web.bind.annotation.*;

/** Lectura y guardado de la configuración general del negocio del usuario logueado. */
@RestController
@RequestMapping("/api/configuracao")
public class ConfiguracaoController {
    private final PrecificacaoService service;
    private final UsuarioActual actual;

    public ConfiguracaoController(PrecificacaoService service, UsuarioActual actual) {
        this.service = service;
        this.actual = actual;
    }

    @GetMapping
    public ConfiguracaoNegocio obter() { return service.configuracao(actual.requerido()); }

    @PutMapping
    public ConfiguracaoNegocio salvar(@RequestBody ConfiguracaoNegocio c) {
        return service.salvarConfiguracao(actual.requerido(), c);
    }
}
