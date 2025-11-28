package com.tfg.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.tfg.app.model.Usuario;
import com.tfg.app.repository.UsuarioRepository;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(usuarioDetalles.getNombre());
                    usuario.setEmail(usuarioDetalles.getEmail());

                    // Solo actualizar contraseña si se proporciona una nueva
                    if (usuarioDetalles.getContrasena() != null && !usuarioDetalles.getContrasena().isEmpty()) {
                        // Encriptar la nueva contraseña
                        usuario.setContrasena(passwordEncoder.encode(usuarioDetalles.getContrasena()));
                    }

                    Usuario usuarioActualizado = usuarioRepository.save(usuario);
                    return ResponseEntity.ok(usuarioActualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/sueldo")
    public ResponseEntity<Usuario> actualizarSueldo(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        Double sueldo = body.get("sueldo");
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setSueldo(sueldo);
        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(guardado);
    }
}
