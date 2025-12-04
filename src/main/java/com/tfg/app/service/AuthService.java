package com.tfg.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tfg.app.model.Usuario;
import com.tfg.app.repository.UsuarioRepository;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Patrón regex estricto para validar email
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Usuario login(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            System.out.println("No existe usuario con ese email");
            return null;
        }

        boolean coincide = passwordEncoder.matches(contrasena, usuario.getContrasena());
        System.out.println("Contraseña introducida: " + contrasena);
        System.out.println("Contraseña en BD: " + usuario.getContrasena());
        System.out.println("Coincide: " + coincide);

        return coincide ? usuario : null;
    }

    public void register(String nombre, String email, String contrasena) {
        // ✅ Validar que el nombre no esté vacío
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }

        // ✅ Validar que el nombre tenga al menos 2 caracteres
        if (nombre.trim().length() < 2) {
            throw new RuntimeException("El nombre debe tener al menos 2 caracteres");
        }

        // ✅ Validar que el email no esté vacío
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio");
        }

        // ✅ Validar formato de email
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("El email no es válido. Debe tener el formato: usuario@dominio.com");
        }

        // ✅ Validar que la contraseña no esté vacía
        if (contrasena == null || contrasena.isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        // ✅ Validar que la contraseña tenga al menos 6 caracteres
        if (contrasena.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }

        // ✅ Validar que el email no esté duplicado
        if (usuarioRepository.findByEmail(email.toLowerCase()) != null) {
            throw new RuntimeException("El email ya está registrado");
        }

        String hashedPassword = passwordEncoder.encode(contrasena);
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setEmail(email.toLowerCase().trim()); // ✅ Guardar en minúsculas y sin espacios
        usuario.setContrasena(hashedPassword);
        usuarioRepository.save(usuario);
    }
}