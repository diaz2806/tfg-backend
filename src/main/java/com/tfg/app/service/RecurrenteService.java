package com.tfg.app.service;

import com.tfg.app.model.Gasto;
import com.tfg.app.repository.GastoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RecurrenteService {

    private final GastoRepository gastoRepository;

    public RecurrenteService(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    // SE EJECUTA TODOS LOS DÍAS A LAS 00:05
    @Scheduled(cron = "0 5 0 * * ?")
    public void procesarGastosRecurrentes() {
        System.out.println("Procesando gastos recurrentes automáticos...");

        List<Gasto> recurrentes = gastoRepository.findByRecurrenteTrue();

        for (Gasto original : recurrentes) {
            LocalDate ultima = original.getFechaUltimaEjecucion() != null 
                ? original.getFechaUltimaEjecucion() 
                : original.getFecha();

            if (debeEjecutarseHoy(ultima, original.getFrecuencia())) {

                // CREAR GASTO NUEVO
                Gasto nuevo = new Gasto();
                nuevo.setNombre(original.getNombre());
                nuevo.setCantidad(original.getCantidad());
                nuevo.setCategoria(original.getCategoria());
                nuevo.setDescripcion(original.getDescripcion() + " (recurrente automático)");
                nuevo.setFecha(LocalDate.now());
                nuevo.setUsuario(original.getUsuario());
                nuevo.setRecurrente(false);
                nuevo.setFrecuencia(null);

                gastoRepository.save(nuevo);

                // Actualizar última ejecución
                original.setFechaUltimaEjecucion(LocalDate.now());
                gastoRepository.save(original);

                System.out.println("Gasto recurrente creado: " + original.getNombre());
            }
        }
    }

    private boolean debeEjecutarseHoy(LocalDate ultima, String frecuencia) {
        LocalDate hoy = LocalDate.now();
        return switch (frecuencia) {
            case "SEMANAL" -> ultima.plusWeeks(1).isBefore(hoy) || ultima.plusWeeks(1).isEqual(hoy);
            case "MENSUAL" -> ultima.plusMonths(1).isBefore(hoy) || ultima.plusMonths(1).isEqual(hoy);
            case "ANUAL"  -> ultima.plusYears(1).isBefore(hoy) || ultima.plusYears(1).isEqual(hoy);
            default -> false;
        };
    }
}