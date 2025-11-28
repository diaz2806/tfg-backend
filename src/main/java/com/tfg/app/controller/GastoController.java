package com.tfg.app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.tfg.app.model.Evento;
import com.tfg.app.model.Gasto;
import com.tfg.app.model.Usuario;
import com.tfg.app.repository.CategoriaRepository;
import com.tfg.app.repository.EventoRepository;
import com.tfg.app.repository.GastoRepository;
import com.tfg.app.service.ExportService;
import com.tfg.app.service.GastoService;
import com.tfg.app.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;
    private final GastoRepository gastoRepository;
    private final UsuarioService usuarioService;

    @Autowired
    private ExportService exportService;

    // AÑADE ESTOS DOS REPOSITORIOS
    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public GastoController(GastoService service, GastoRepository gastoRepository, 
                         UsuarioService usuarioService) {
        this.service = service;
        this.gastoRepository = gastoRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Gasto> getAll() {
        return service.findAll();
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Gasto> getByUsuario(@PathVariable Long idUsuario) {
        return gastoRepository.findByUsuarioId(idUsuario);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Gasto gasto) {
        try {
            // 1. Guardar el gasto
            Gasto gastoGuardado = gastoRepository.save(gasto);

            // 2. Si es recurrente → crear evento recurrente en calendario
            if (gasto.getRecurrente() != null && gasto.getRecurrente()) {

                Evento evento = new Evento();
                evento.setTitulo("Gasto recurrente: " + gasto.getNombre());
                evento.setDescripcion("Se repite " + gasto.getFrecuencia().toLowerCase());
                evento.setFechaInicio(LocalDateTime.now());
                evento.setFechaFin(LocalDateTime.now().plusHours(1));
                evento.setConGasto(true);
                evento.setCantidad(gasto.getCantidad());
                evento.setUsuario(gasto.getUsuario());

                // Si no tiene categoría, ponemos "Otros"
                if (gasto.getCategoria() == null) {
                    evento.setCategoria(categoriaRepository.findByNombre("Otros")
                            .orElseThrow(() -> new RuntimeException("Categoría 'Otros' no encontrada")));
                } else {
                    evento.setCategoria(gasto.getCategoria());
                }

                evento.setGasto(gastoGuardado);

                // CAMPOS RECURRENTES
                evento.setEsRecurrente(true);
                evento.setFrecuencia(gasto.getFrecuencia());
                evento.setFechaUltimaGeneracion(LocalDateTime.now());

                // GUARDAR CON EL REPOSITORIO CORRECTO
                eventoRepository.save(evento);
            }

            return ResponseEntity.ok(gastoGuardado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear gasto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gasto> updateGasto(@PathVariable Long id, @RequestBody Gasto gasto) {
        Optional<Gasto> gastoExistente = gastoRepository.findById(id);

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
            return ResponseEntity.ok(gastoRepository.save(g));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exportar/pdf/usuario/{idUsuario}")
    public ResponseEntity<byte[]> exportarPDF(@PathVariable Long idUsuario) {
        try {
            List<Gasto> gastos = gastoRepository.findByUsuarioId(idUsuario);
            byte[] pdfBytes = exportService.exportarGastosPDF(gastos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "gastos_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Error al exportar PDF: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/exportar/excel/usuario/{idUsuario}")
    public ResponseEntity<byte[]> exportarExcel(@PathVariable Long idUsuario) {
        try {
            List<Gasto> gastos = gastoRepository.findByUsuarioId(idUsuario);
            byte[] excelBytes = exportService.exportarGastosExcel(gastos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "gastos_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            System.err.println("Error al exportar Excel: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

}