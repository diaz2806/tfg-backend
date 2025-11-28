package com.tfg.app.repository;

import com.tfg.app.model.Gasto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Long> {
    List<Gasto> findByUsuarioId(Long idUsuario);
    List<Gasto> findByRecurrenteTrue();
}
