package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.response.DetalleVentaResponse;
import com.example.proyecto.app.dto.response.VentaResponse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class PdfGeneratorService {

    public byte[] generarBoleta(VentaResponse venta, List<DetalleVentaResponse> detalles) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("BOLETA DE VENTA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        // Datos de la venta
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        document.add(new Paragraph("N° Venta: " + venta.getId(), normalFont));
        document.add(new Paragraph("Fecha: " + venta.getFecha(), normalFont));
        document.add(new Paragraph("Cliente: " + (venta.getClienteNombre() != null ? venta.getClienteNombre() : "Anónimo"), normalFont));
        document.add(new Paragraph("Vendedor: " + (venta.getUsuarioNombre() != null ? venta.getUsuarioNombre() : "Sistema"), normalFont));
        document.add(new Paragraph(" "));

        // Tabla de productos
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Encabezados
        String[] headers = {"Producto", "Cantidad", "Precio Unit.", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Filas
        for (DetalleVentaResponse detalle : detalles) {
            table.addCell(detalle.getProductoNombre() != null ? detalle.getProductoNombre() : "Producto");
            table.addCell(String.valueOf(detalle.getCantidad()));
            table.addCell("S/ " + String.format("%.2f", detalle.getPrecioUnitarioVenta()));
            table.addCell("S/ " + String.format("%.2f", detalle.getSubtotal()));
        }

        document.add(table);

        // Total
        Paragraph total = new Paragraph("TOTAL: S/ " + String.format("%.2f", venta.getTotal()),
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        // Pie
        document.add(new Paragraph(" "));
        document.add(new Paragraph("¡Gracias por su compra!",
                new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC)));

        document.close();
        return out.toByteArray();
    }
}