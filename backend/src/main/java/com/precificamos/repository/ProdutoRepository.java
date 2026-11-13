package com.precificamos.repository;

import com.precificamos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Productos: siempre se consultan filtrando por el usuario dueño. */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByUsuarioIdOrderByIdAsc(Long usuarioId);
    Optional<Produto> findByIdAndUsuarioId(Long id, Long usuarioId);
}
