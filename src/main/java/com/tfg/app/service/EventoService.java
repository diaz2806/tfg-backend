package com.tfg.app.service;

import com.tfg.app.model.Evento;
import com.tfg.app.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public List<Evento> getEventosUsuario(Long idUsuario) {
        return eventoRepository.findByUsuarioId(idUsuario);
    }

    public Evento guardarEvento(Evento evento) {
        return eventoRepository.save(evento);
    }

    public Optional<Evento> getEvento(Long id) {
        return eventoRepository.findById(id);
    }

    public void eliminarEvento(Long id) {
        eventoRepository.deleteById(id);
    }
}
