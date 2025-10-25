package com.tfg.app.service;

import com.tfg.app.model.Gasto;
import com.tfg.app.repository.GastoRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class GastoService {
    private final GastoRepository repository;

    public GastoService(GastoRepository repository) {
        this.repository = repository;
    }

    public List<Gasto> findAll() {
        return repository.findAll();
    }

    public Gasto save(Gasto gasto) {
        return repository.save(gasto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gasto> update(@PathVariable Long id, @RequestBody Gasto gasto) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(gasto.getNombre());
                    existing.setCategoria(gasto.getCategoria());
                    existing.setDescripcion(gasto.getDescripcion());
                    existing.setCantidad(gasto.getCantidad());
                    existing.setFecha(gasto.getFecha());
                    existing.setRecurrente(gasto.isRecurrente());
                    existing.setFrecuencia(gasto.getFrecuencia());
                    return ResponseEntity.ok(repository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
