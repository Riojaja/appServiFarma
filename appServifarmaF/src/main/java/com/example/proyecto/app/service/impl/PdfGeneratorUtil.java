package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.example.proyecto.app.dto.response.DetalleVentaResponse;


@Service
public class PdfGeneratorUtil {

    public byte[] generarBoleta(Venta venta, List<DetalleVentaResponse> detalles) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("BOLETA DE VENTA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Espacio
        document.add(new Paragraph(" "));

        // Datos de la venta
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        document.add(new Paragraph("N° Venta: " + venta.getId(), normalFont));
        document.add(new Paragraph("Fecha: " + venta.getFecha().toString(), normalFont));
        document.add(new Paragraph("Cliente: " + (venta.getCliente() != null ? venta.getCliente().getNombre() : "Anónimo"), normalFont));
        document.add(new Paragraph("Vendedor: " + venta.getUsuario().getNombreCompleto(), normalFont));
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
            table.addCell(detalle.getProductoNombre());
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
        document.add(new Paragraph("Esta boleta es un comprobante de pago.", 
                                   new Font(Font.FontFamily.HELVETICA, 10)));

        document.close();
        return out.toByteArray();
    }
}