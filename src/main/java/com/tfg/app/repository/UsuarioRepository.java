package com.tfg.app.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.app.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    Optional<Usuario> findById(Long id);
}

