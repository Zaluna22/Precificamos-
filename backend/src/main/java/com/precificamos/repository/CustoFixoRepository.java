package com.precificamos.repository;

import com.precificamos.model.CustoFixo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Costos fijos: siempre se consultan filtrando por el usuario dueño. */
public interface CustoFixoRepository extends JpaRepository<CustoFixo, Long> {
    List<CustoFixo> findByUsuarioIdOrderByIdAsc(Long usuarioId);
    Optional<CustoFixo> findByIdAndUsuarioId(Long id, Long usuarioId);
}
