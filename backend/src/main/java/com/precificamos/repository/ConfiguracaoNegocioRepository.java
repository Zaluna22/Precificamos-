package com.precificamos.repository;

import com.precificamos.model.ConfiguracaoNegocio;
import org.springframework.data.jpa.repository.JpaRepository;

/** La clave primaria de la configuración es el id del usuario dueño. */
public interface ConfiguracaoNegocioRepository extends JpaRepository<ConfiguracaoNegocio, Long> {}
