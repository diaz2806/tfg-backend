package com.tfg.app.controller;

import com.tfg.app.model.Evento;
import com.tfg.app.model.Gasto;
import com.tfg.app.model.Usuario;
import com.tfg.app.model.Categoria;
import com.tfg.app.repository.EventoRepository;
import com.tfg.app.repository.CategoriaRepository;
import com.tfg.app.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "http://localhost:4200")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @GetMapping("/usuario/{idUsuario}")
    public List<Evento> obtenerEventosPorUsuario(@PathVariable Long idUsuario) {
        return eventoRepository.findByUsuarioId(idUsuario);
    }

    @PostMapping("/usuario/{idUsuario}")
    public Evento crearEvento(@PathVariable Long idUsuario, @RequestBody Evento evento) {
        System.out.println("📥 ===== EVENTO RECIBIDO =====");
        System.out.println("   - Titulo: " + evento.getTitulo());
        System.out.println("   - Categoria objeto: " + evento.getCategoria());
        System.out.println(
                "   - Categoria ID: " + (evento.getCategoria() != null ? evento.getCategoria().getId() : "NULL"));
        System.out.println("   - Categoria Nombre ANTES: "
                + (evento.getCategoria() != null ? evento.getCategoria().getNombre() : "NULL"));

        Usuario usuario = usuarioService.obtenerUsuarioPorId(idUsuario);
        evento.setUsuario(usuario);

        // ✅ CARGAR LA CATEGORÍA COMPLETA
        if (evento.getCategoria() != null && evento.getCategoria().getId() != null) {
            Categoria categoriaCompleta = categoriaRepository.findById(evento.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            evento.setCategoria(categoriaCompleta);
            System.out.println("✅ Categoría cargada DESPUÉS: ID=" + categoriaCompleta.getId() + ", Nombre="
                    + categoriaCompleta.getNombre());
        }

        System.out.println("🔍 ConGasto: " + evento.isConGasto());
        System.out.println("🔍 Cantidad: " + evento.getCantidad());

        if (evento.isConGasto() && evento.getCantidad() != null && evento.getCantidad() > 0) {
            String nombreCategoria = evento.getCategoria() != null ? evento.getCategoria().getNombre()
                    : "Sin categoría";
            System.out.println("🏷️ NOMBRE DE CATEGORÍA PARA GASTO: '" + nombreCategoria + "'");

            Gasto gasto = new Gasto();
            gasto.setNombre(evento.getTitulo());
            gasto.setDescripcion(evento.getDescripcion());
            Categoria categoria = categoriaRepository.findByNombre(nombreCategoria)
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(nombreCategoria);
                        nueva.setColor("#808080");
                        return categoriaRepository.save(nueva);
                    });
            gasto.setCategoria(categoria);
            gasto.setCantidad(evento.getCantidad());
            gasto.setFecha(evento.getFechaInicio().toLocalDate());
            gasto.setRecurrente(false);
            gasto.setUsuario(usuario);

            evento.setGasto(gasto);
            System.out.println("✅ Gasto creado y asociado al evento");
            System.out.println("   - Nombre gasto: " + gasto.getNombre());
            System.out.println("   - Categoría gasto: '" + gasto.getCategoria() + "'");
        }

        Evento savedEvento = eventoRepository.save(evento);
        System.out.println("💾 Evento guardado con ID: " + savedEvento.getId());
        System.out.println("================================");

        return savedEvento;
    }

    @PutMapping("/{id}")
    public Evento actualizarEvento(@PathVariable Long id, @RequestBody Evento eventoDetalles) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // ✅ CARGAR LA CATEGORÍA COMPLETA
        if (eventoDetalles.getCategoria() != null && eventoDetalles.getCategoria().getId() != null) {
            Categoria categoriaCompleta = categoriaRepository.findById(eventoDetalles.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            eventoDetalles.setCategoria(categoriaCompleta);
        }

        evento.setTitulo(eventoDetalles.getTitulo());
        evento.setDescripcion(eventoDetalles.getDescripcion());
        evento.setFechaInicio(eventoDetalles.getFechaInicio());
        evento.setFechaFin(eventoDetalles.getFechaFin());
        evento.setConGasto(eventoDetalles.isConGasto());
        evento.setCantidad(eventoDetalles.getCantidad());
        evento.setCategoria(eventoDetalles.getCategoria());

        if (eventoDetalles.isConGasto() && eventoDetalles.getCantidad() != null && eventoDetalles.getCantidad() > 0) {
            if (evento.getGasto() != null) {
                Gasto gasto = evento.getGasto();
                gasto.setNombre(eventoDetalles.getTitulo());
                gasto.setDescripcion(eventoDetalles.getDescripcion());
                gasto.setCategoria(eventoDetalles.getCategoria()); 
                gasto.setCantidad(eventoDetalles.getCantidad());
                gasto.setFecha(eventoDetalles.getFechaInicio().toLocalDate());
            } else {
                Gasto gasto = new Gasto();
                gasto.setNombre(eventoDetalles.getTitulo());
                gasto.setDescripcion(eventoDetalles.getDescripcion());
                gasto.setCategoria(eventoDetalles.getCategoria()); 
                gasto.setCantidad(eventoDetalles.getCantidad());
                gasto.setFecha(eventoDetalles.getFechaInicio().toLocalDate());
                gasto.setRecurrente(false);
                gasto.setUsuario(evento.getUsuario());
                evento.setGasto(gasto);
            }
        } else {
            evento.setGasto(null);
        }

        return eventoRepository.save(evento);
    }

    @DeleteMapping("/{id}")
    public void eliminarEvento(@PathVariable Long id) {
        eventoRepository.deleteById(id);
    }
}