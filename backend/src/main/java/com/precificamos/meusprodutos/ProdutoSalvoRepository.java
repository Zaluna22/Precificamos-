package com.precificamos.meusprodutos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Productos guardados: siempre se consultan filtrando por el usuario dueño. */
public interface ProdutoSalvoRepository extends JpaRepository<ProdutoSalvo, Long> {
    List<ProdutoSalvo> findByUsuarioIdOrderByActualizadoEnDescIdDesc(Long usuarioId);
    Optional<ProdutoSalvo> findByIdAndUsuarioId(Long id, Long usuarioId);
}
