package com.tfg.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tfg.app.model.Usuario;
import com.tfg.app.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario login(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            System.out.println("No existe usuario con ese email");
            return null;
        }

        boolean coincide = BCrypt.checkpw(contrasena, usuario.getContrasena());
        System.out.println("Contraseña introducida: " + contrasena);
        System.out.println("Contraseña en BD: " + usuario.getContrasena());
        System.out.println("Coincide: " + coincide);

        return coincide ? usuario : null;
    }

    public void register(String nombre, String email, String contrasena) {
        String hashedPassword = BCrypt.hashpw(contrasena, BCrypt.gensalt());
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(hashedPassword);
        usuarioRepository.save(usuario);
    }
}
