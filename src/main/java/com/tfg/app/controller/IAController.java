package com.tfg.app.controller;

import com.tfg.app.model.Gasto;
import com.tfg.app.repository.GastoRepository;
import com.tfg.app.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ia")
@CrossOrigin(origins = "http://localhost:4200")
public class IAController {

        @Autowired
        private GroqService groqService;

        @Autowired
        private GastoRepository gastoRepository;

        @PostMapping("/analizar")
        public Map<String, String> analizarGastos(@RequestBody Map<String, Object> request) {
                try {
                        Long idUsuario = Long.valueOf(request.get("idUsuario").toString());
                        Double sueldo = request.containsKey("sueldo") && request.get("sueldo") != null
                                        ? Double.valueOf(request.get("sueldo").toString())
                                        : null;

                        System.out.println("👤 Analizando gastos para usuario ID: " + idUsuario);

                        // Obtener gastos del usuario
                        List<Gasto> gastosOriginales = gastoRepository.findByUsuarioId(idUsuario);
                        System.out.println("📊 Total de gastos obtenidos: " + gastosOriginales.size());

                        // ✅ FILTRAR GASTOS VÁLIDOS (con categoría no nula)
                        List<Gasto> gastos = gastosOriginales.stream()
                                        .filter(g -> g.getCategoria() != null)
                                        .filter(g -> g.getCantidad() != null && g.getCantidad() > 0)
                                        .filter(g -> g.getFecha() != null)
                                        .toList();

                        System.out.println("✅ Gastos válidos (con categoría): " + gastos.size());

                        if (gastos.isEmpty()) {
                                return Map.of(
                                                "analisis",
                                                "No tienes gastos registrados aún. Comienza a añadir gastos para obtener un análisis personalizado.",
                                                "totalMesActual", "0.00",
                                                "totalMesAnterior", "0.00");
                        }

                        // Calcular fechas
                        LocalDate mesActual = LocalDate.now().withDayOfMonth(1);
                        LocalDate mesAnterior = mesActual.minusMonths(1);

                        // ✅ TOTAL MES ACTUAL
                        double totalMesActual = gastos.stream()
                                        .filter(g -> g.getFecha().isAfter(mesActual.minusDays(1)))
                                        .mapToDouble(Gasto::getCantidad)
                                        .sum();

                        System.out.println("💰 Total mes actual: " + totalMesActual + "€");

                        // ✅ TOTAL MES ANTERIOR
                        double totalMesAnterior = gastos.stream()
                                        .filter(g -> g.getFecha().isAfter(mesAnterior.minusDays(1))
                                                        && g.getFecha().isBefore(mesActual))
                                        .mapToDouble(Gasto::getCantidad)
                                        .sum();

                        System.out.println("💰 Total mes anterior: " + totalMesAnterior + "€");

                        // ✅ GASTOS POR CATEGORÍA (MES ACTUAL) - MANEJO MANUAL PARA EVITAR NULL
                        Map<String, Double> gastosPorCategoria = new HashMap<>();

                        gastos.stream()
                                        .filter(g -> g.getFecha().isAfter(mesActual.minusDays(1)))
                                        .forEach(gasto -> {
                                                String categoria = gasto.getCategoria() != null
                                                                ? gasto.getCategoria().getNombre()
                                                                : "Sin categoría";
                                                if (categoria != null && !categoria.trim().isEmpty()) {
                                                        gastosPorCategoria.merge(categoria, gasto.getCantidad(),
                                                                        Double::sum);
                                                }
                                        });

                        System.out.println("📂 Gastos por categoría: " + gastosPorCategoria);

                        // Construir prompt para la IA
                        String prompt = construirPrompt(sueldo, totalMesActual, totalMesAnterior, gastosPorCategoria);

                        System.out.println("📤 Enviando prompt a Gemini...");

                        // Llamar a Groq
                        String analisis = groqService.analizarGastos(prompt);

                        System.out.println("📥 Respuesta recibida de Gemini");

                        return Map.of(
                                        "analisis", analisis,
                                        "totalMesActual", String.format("%.2f", totalMesActual),
                                        "totalMesAnterior", String.format("%.2f", totalMesAnterior));

                } catch (Exception e) {
                        System.err.println("❌ Error en analizarGastos: " + e.getMessage());
                        e.printStackTrace();
                        return Map.of(
                                        "analisis", "Error al procesar el análisis: " + e.getMessage(),
                                        "totalMesActual", "0.00",
                                        "totalMesAnterior", "0.00");
                }
        }

        private String construirPrompt(Double sueldo, double mesActual, double mesAnterior,
                        Map<String, Double> gastosPorCategoria) {
                StringBuilder p = new StringBuilder();

                p.append(
                                "Eres un asesor financiero experto, directo y muy amigable. Analiza los siguientes datos del usuario y responde ÚNICAMENTE con este formato exacto (nada más, ni saludos ni despedidas):\n\n");

                // Datos básicos
                if (sueldo != null && sueldo > 0) {
                        p.append("Sueldo mensual: ").append(String.format("%.2f€\n", sueldo));
                }
                p.append("Gasto mes actual: ").append(String.format("%.2f€\n", mesActual));
                p.append("Gasto mes anterior: ").append(String.format("%.2f€\n\n", mesAnterior));

                // Desglose categorías
                p.append("Desglose mes actual:\n");
                gastosPorCategoria.entrySet().stream()
                                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                .forEach(e -> p.append("• ").append(e.getKey()).append(": ")
                                                .append(String.format("%.2f€\n", e.getValue())));

                p.append("\n=== ANÁLISIS RÁPIDO ===\n");

                // Instrucciones estrictas (esto es lo que hace que quede perfecto)
                p.append("1. Variación respecto al mes anterior (porcentaje y si subió o bajó)\n");
                if (sueldo != null && sueldo > 0) {
                        p.append("2. Porcentaje del sueldo que lleva gastado este mes\n");
                }
                p.append("3. Categoría donde más gasta (la primera de la lista)\n");
                p.append("4. 3 recomendaciones concretas, reales y accionables para ahorrar este mes\n");
                p.append("5. Frase motivadora final\n\n");

                p.append("Usa emojis, sé breve, directo y muy positivo. ¡Quiero que el usuario sonría al leerlo!");

                return p.toString();
        }

        // NUEVO: Predicción del próximo mes
        @PostMapping("/prediccion")
        public Map<String, Object> predecirProximoMes(@RequestBody Map<String, Object> request) {
                Long idUsuario = Long.valueOf(request.get("idUsuario").toString());
                Double sueldo = request.containsKey("sueldo") ? Double.valueOf(request.get("sueldo").toString()) : null;

                List<Gasto> gastos = gastoRepository.findByUsuarioId(idUsuario);

                // Últimos 3 meses completos
                LocalDate hoy = LocalDate.now();
                LocalDate inicio = hoy.minusMonths(3).withDayOfMonth(1);
                List<Gasto> ultimos3Meses = gastos.stream()
                                .filter(g -> g.getFecha() != null && !g.getFecha().isBefore(inicio))
                                .toList();

                Map<String, Double> promedioPorCategoria = new HashMap<>();
                ultimos3Meses.stream()
                                .filter(g -> g.getCategoria() != null)
                                .forEach(g -> {
                                        String nombreCat = g.getCategoria().getNombre();
                                        promedioPorCategoria.merge(nombreCat, g.getCantidad(), Double::sum);
                                });
                promedioPorCategoria.replaceAll((k, v) -> v / 3.0);

                double totalPromedio = promedioPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();

                String prompt = """
                                Eres un predictor financiero muy preciso.
                                Promedio mensual últimos 3 meses: %.2f€
                                Promedio por categoría:
                                %s
                                Sueldo: %s

                                Predice cuánto gastará el usuario el próximo mes.
                                Di si hay riesgo de superar el sueldo.
                                Da 3 consejos concretos para ahorrar.
                                Sé directo, usa emojis y responde en español.
                                """.formatted(
                                totalPromedio,
                                promedioPorCategoria.entrySet().stream()
                                                .map(e -> "• " + e.getKey() + ": "
                                                                + String.format("%.2f€", e.getValue()))
                                                .collect(Collectors.joining("\n")),
                                sueldo != null ? String.format("%.2f€", sueldo) : "no indicado");

                String prediccion = groqService.analizarGastos(prompt);

                return Map.of(
                                "prediccion", prediccion,
                                "totalEstimado", String.format("%.2f", totalPromedio),
                                "riesgoSobregasto", totalPromedio > (sueldo != null ? sueldo : 0));
        }

        @PostMapping("/sugerir-eventos")
        public ResponseEntity<List<Map<String, String>>> sugerirEventos(@RequestBody Map<String, Object> request) {

                // FALLBACK SEGURO
                List<Map<String, String>> fallback = new ArrayList<>();
                Map<String, String> e1 = new HashMap<>();
                e1.put("titulo", "Revisar suscripciones");
                e1.put("fechaSugerida", "2025-06-05");
                e1.put("notas", "Netflix, Spotify, Disney+");
                fallback.add(e1);
                Map<String, String> e2 = new HashMap<>();
                e2.put("titulo", "Compra supermercado");
                e2.put("fechaSugerida", "2025-06-15");
                e2.put("notas", "Compra grande del mes");
                fallback.add(e2);
                Map<String, String> e3 = new HashMap<>();
                e3.put("titulo", "Revisión mensual");
                e3.put("fechaSugerida", "2025-06-30");
                e3.put("notas", "Cierra el mes");
                fallback.add(e3);
                Map<String, String> e4 = new HashMap<>();
                e4.put("titulo", "Pagar alquiler");
                e4.put("fechaSugerida", "2025-06-01");
                e4.put("notas", "No te olvides");
                fallback.add(e4);

                try {
                        Long idUsuario = Long.valueOf(request.get("idUsuario").toString());
                        List<Gasto> gastos = gastoRepository.findByUsuarioId(idUsuario);

                        // Lista para guardar los nombres recurrentes
                        List<String> nombresRecurrentes = new ArrayList<>();

                        // 1. Primero: gastos marcados como recurrentes (tu campo Boolean)
                        for (Gasto g : gastos) {
                                if (g.getRecurrente() != null && g.getRecurrente()) {
                                        String nombre = g.getNombre();
                                        if (nombre != null && !nombre.trim().isEmpty()) {
                                                nombresRecurrentes.add("• " + nombre.trim());
                                        }
                                }
                        }

                        // 2. Si no hay recurrentes marcados → detectar por repetición de nombre
                        if (nombresRecurrentes.isEmpty()) {
                                Map<String, Integer> contador = new HashMap<>();
                                for (Gasto g : gastos) {
                                        String nombre = g.getNombre();
                                        if (nombre != null && !nombre.trim().isEmpty()) {
                                                nombre = nombre.toLowerCase().trim();
                                                contador.put(nombre, contador.getOrDefault(nombre, 0) + 1);
                                        }
                                }

                                // Añadimos los que se repiten 2 o más veces
                                for (Map.Entry<String, Integer> entry : contador.entrySet()) {
                                        if (entry.getValue() >= 2) {
                                                String nombreBonito = entry.getKey().substring(0, 1).toUpperCase()
                                                                + entry.getKey().substring(1);
                                                nombresRecurrentes.add("• " + nombreBonito + " (" + entry.getValue()
                                                                + " veces)");
                                        }
                                }

                                // Ordenamos por número de veces (de más a menos)
                                nombresRecurrentes.sort((a, b) -> {
                                        int numA = Integer.parseInt(a.replaceAll(".*\\((\\d+) veces\\).*", "$1"));
                                        int numB = Integer.parseInt(b.replaceAll(".*\\((\\d+) veces\\).*", "$1"));
                                        return numB - numA;
                                });

                                // Máximo 8
                                if (nombresRecurrentes.size() > 8) {
                                        nombresRecurrentes = nombresRecurrentes.subList(0, 8);
                                }
                        }

                        String textoParaIA = nombresRecurrentes.isEmpty()
                                        ? "El usuario no tiene gastos recurrentes claros."
                                        : "Gastos recurrentes detectados:\n" + String.join("\n", nombresRecurrentes);

                        String prompt = """
                                        %s

                                        Sugiere 4 eventos útiles para el calendario basados en esos gastos.
                                        Responde SOLO con este JSON exacto:
                                        [{"titulo":"Revisar suscripciones","fechaSugerida":"2025-06-05","notas":"Netflix, Spotify..."},{"titulo":"Compra supermercado","fechaSugerida":"2025-06-15","notas":"Compra grande"},{"titulo":"Revisión mensual","fechaSugerida":"2025-06-30","notas":"Cierra el mes"},{"titulo":"Pagar alquiler","fechaSugerida":"2025-06-01","notas":"No te olvides"}]
                                        """
                                        .formatted(textoParaIA);

                        String respuesta = groqService.analizarGastos(prompt);
                        String json = respuesta.replaceAll("```json|```", "").trim();

                        try {
                                List<Map<String, String>> eventos = new ObjectMapper().readValue(json,
                                                new TypeReference<List<Map<String, String>>>() {
                                                });
                                if (eventos != null && eventos.size() >= 3) {
                                        return ResponseEntity.ok(eventos);
                                }
                        } catch (Exception e) {
                                System.out.println("IA falló, usando fallback");
                        }

                } catch (Exception e) {
                        // Silencio
                }

                return ResponseEntity.ok(fallback);
        }

        @PostMapping("/consejero")
        public ResponseEntity<Map<String, String>> consejero(@RequestBody Map<String, Object> request) {
                Long idUsuario = Long.valueOf(request.get("idUsuario").toString());
                String pregunta = (String) request.get("pregunta");

                List<Gasto> gastos = gastoRepository.findByUsuarioId(idUsuario);
                double totalUltimoMes = gastos.stream()
                                .filter(g -> g.getFecha() != null
                                                && g.getFecha().isAfter(LocalDate.now().minusMonths(1)))
                                .mapToDouble(Gasto::getCantidad)
                                .sum();

                String prompt = """
                                Eres un asesor financiero personal, directo y muy sabio.
                                El usuario ha gastado %.0f€ el último mes.
                                Sueldo aproximado: %s
                                Pregunta: "%s"

                                Responde en español, con empatía, claridad y 1 consejo práctico.
                                Usa emojis y máximo 4 líneas.
                                """.formatted(
                                totalUltimoMes,
                                request.containsKey("sueldo") ? request.get("sueldo") + "€" : "desconocido",
                                pregunta);

                String respuesta = groqService.analizarGastos(prompt);

                return ResponseEntity.ok(Map.of("respuesta", respuesta));
        }
}