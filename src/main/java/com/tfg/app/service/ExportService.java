package com.tfg.app.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.tfg.app.model.Gasto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    public byte[] exportarGastosPDF(List<Gasto> gastos) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph title = new Paragraph("Informe de Gastos")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Fecha del informe
            Paragraph fecha = new Paragraph("Generado el: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(fecha);

            // Estadísticas resumidas
            double totalGastos = gastos.stream()
                    .mapToDouble(g -> g.getCantidad() != null ? g.getCantidad() : 0.0)
                    .sum();

            Paragraph resumen = new Paragraph(String.format("Total de gastos: %d | Total: %.2f €",
                    gastos.size(), totalGastos))
                    .setFontSize(12)
                    .setBold()
                    .setMarginBottom(15);
            document.add(resumen);

            // Tabla de gastos
            float[] columnWidths = { 1, 3, 3, 2, 2, 2 };
            Table table = new Table(columnWidths);
            table.setWidth(550);

            // Encabezados
            String[] headers = { "ID", "Nombre", "Categoría", "Cantidad", "Fecha", "Recurrente" };
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Gasto gasto : gastos) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(gasto.getId()))));
                table.addCell(new Cell().add(new Paragraph(gasto.getNombre() != null ? gasto.getNombre() : "")));
                table.addCell(new Cell().add(new Paragraph(gasto.getCategoria() != null ? gasto.getCategoria().getNombre() : "Sin categoría")));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", gasto.getCantidad()))));
                table.addCell(new Cell().add(new Paragraph(gasto.getFecha() != null ? gasto.getFecha().format(formatter) : "")));
                table.addCell(new Cell().add(new Paragraph(gasto.isRecurrente() ? "Sí" : "No")));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IOException("Error al generar PDF", e);
        }
    }

    public byte[] exportarGastosExcel(List<Gasto> gastos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Gastos");

        // Estilo para el encabezado
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Crear encabezado
        Row headerRow = sheet.createRow(0);
        String[] columnas = { "ID", "Nombre", "Descripción", "Categoría", "Cantidad (€)", "Fecha", "Recurrente",
                "Frecuencia" };

        for (int i = 0; i < columnas.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        // Llenar datos
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Gasto gasto : gastos) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(gasto.getId() != null ? gasto.getId() : 0);
            row.createCell(1).setCellValue(gasto.getNombre() != null ? gasto.getNombre() : "");
            row.createCell(2).setCellValue(gasto.getDescripcion() != null ? gasto.getDescripcion() : "");
            row.createCell(3)
                    .setCellValue(gasto.getCategoria() != null ? gasto.getCategoria().getNombre() : "Sin categoría");
            row.createCell(4).setCellValue(gasto.getCantidad() != null ? gasto.getCantidad() : 0.0);
            row.createCell(5).setCellValue(gasto.getFecha() != null ? gasto.getFecha().format(formatter) : "");
            row.createCell(6).setCellValue(gasto.isRecurrente() != null && gasto.isRecurrente() ? "Sí" : "No");
            row.createCell(7).setCellValue(gasto.getFrecuencia() != null ? gasto.getFrecuencia() : "");
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convertir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}