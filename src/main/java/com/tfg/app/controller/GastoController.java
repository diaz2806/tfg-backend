package com.tfg.app.controller;

import com.tfg.app.model.Gasto;
import com.tfg.app.service.GastoService;
import com.tfg.app.repository.GastoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;
    private final GastoRepository repository;

    public GastoController(GastoService service, GastoRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Gasto> getAll() {
        return service.findAll();
    }

    @PostMapping
    public Gasto create(@RequestBody Gasto gasto) {
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
            return ResponseEntity.ok(repository.save(g));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
