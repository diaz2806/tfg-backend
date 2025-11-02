package com.tfg.app.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tfg.app.model.Gasto;
import com.tfg.app.model.Usuario;
import com.tfg.app.repository.GastoRepository;
import com.tfg.app.service.GastoService;
import com.tfg.app.service.UsuarioService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;
    private final GastoRepository repository;
    private final UsuarioService usuarioService; // ✅ AÑADIR

    public GastoController(GastoService service, GastoRepository repository, UsuarioService usuarioService) {
        this.service = service;
        this.repository = repository;
        this.usuarioService = usuarioService; // ✅ AÑADIR
    }

    @GetMapping
    public List<Gasto> getAll() {
        return service.findAll();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Gasto> getByUsuario(@PathVariable Long idUsuario) {
        return repository.findByUsuarioId(idUsuario);
    }

    @PostMapping
    public Gasto create(@RequestBody Gasto gasto) {
        // ✅ ASIGNAR USUARIO AUTOMÁTICAMENTE (usar usuario del localStorage o hardcoded por ahora)
        if (gasto.getUsuario() == null) {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(1L); // Por ahora hardcoded
            gasto.setUsuario(usuario);
        }
        return service.save(gasto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gasto> updateGasto(@PathVariable Long id, @RequestBody Gasto gasto) {
        Optional<Gasto> gastoExistente = repository.findById(id);

        if (gastoExistente.isPresent()) {
            Gasto g = gastoExistente.get();
            g.setNombre(gasto.getNombre());
            g.setDescripcion(gasto.getDescripcion());
            g.setCategoria(gasto.getCategoria());            
            g.setCantidad(gasto.getCantidad());
            g.setFecha(gasto.getFecha());
            g.setRecurrente(gasto.isRecurrente());
            g.setFrecuencia(gasto.getFrecuencia());
            // ✅ Mantener el usuario existente
            return ResponseEntity.ok(repository.save(g));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}