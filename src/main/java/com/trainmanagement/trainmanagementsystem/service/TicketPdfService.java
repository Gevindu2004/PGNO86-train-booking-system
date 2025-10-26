package com.trainmanagement.trainmanagementsystem.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.element.Div;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.trainmanagement.trainmanagementsystem.entity.Booking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class TicketPdfService {

    public byte[] generateTicketPdf(Booking booking) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        try {
            // Header
            Paragraph header = new Paragraph("CEYLON RAIL EASE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setMarginBottom(20);
            document.add(header);

            Paragraph subtitle = new Paragraph("TRAIN TICKET")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold()
                    .setMarginBottom(30);
            document.add(subtitle);

            // Ticket Information Table
            Table ticketTable = new Table(2).useAllAvailableWidth();
            
            // Ticket Number
            addTableRow(ticketTable, "Ticket Number", "#" + booking.getId());
            
            // Passenger Name
            addTableRow(ticketTable, "Passenger Name", booking.getPassengerName());
            
            // Train Information
            if (booking.getSchedule() != null && booking.getSchedule().getTrain() != null) {
                addTableRow(ticketTable, "Train", booking.getSchedule().getTrain().getName());
            } else {
                addTableRow(ticketTable, "Train", "N/A");
            }
            
            // Route
            if (booking.getSchedule() != null) {
                String route = booking.getSchedule().getFromStation() + " → " + booking.getSchedule().getToStation();
                addTableRow(ticketTable, "Route", route);
            } else {
                addTableRow(ticketTable, "Route", "N/A");
            }
            
            // Journey Date
            if (booking.getSchedule() != null && booking.getSchedule().getDate() != null) {
                String date = booking.getSchedule().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                addTableRow(ticketTable, "Journey Date", date);
            } else {
                addTableRow(ticketTable, "Journey Date", "N/A");
            }
            
            // Departure Time
            if (booking.getSchedule() != null && booking.getSchedule().getDepartureTime() != null) {
                addTableRow(ticketTable, "Departure Time", booking.getSchedule().getDepartureTime().toString());
            } else {
                addTableRow(ticketTable, "Departure Time", "N/A");
            }
            
            // Arrival Time
            if (booking.getSchedule() != null && booking.getSchedule().getArrivalTime() != null) {
                addTableRow(ticketTable, "Arrival Time", booking.getSchedule().getArrivalTime().toString());
            } else {
                addTableRow(ticketTable, "Arrival Time", "N/A");
            }
            
            // Seats
            if (booking.getSeats() != null && !booking.getSeats().isEmpty()) {
                StringBuilder seats = new StringBuilder();
                for (int i = 0; i < booking.getSeats().size(); i++) {
                    if (i > 0) seats.append(", ");
                    seats.append(booking.getSeats().get(i).getSeatNumber());
                }
                addTableRow(ticketTable, "Seats", seats.toString());
            } else {
                addTableRow(ticketTable, "Seats", "N/A");
            }
            
            // Booking Date
            if (booking.getBookingTime() != null) {
                String bookingDate = booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                addTableRow(ticketTable, "Booking Date", bookingDate);
            } else {
                addTableRow(ticketTable, "Booking Date", "N/A");
            }
            
            // Status
            addTableRow(ticketTable, "Status", booking.getStatus());

            document.add(ticketTable);

            // Footer
            Paragraph footer = new Paragraph("Thank you for choosing Ceylon RailEase!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginTop(30)
                    .setItalic();
            document.add(footer);

            Paragraph contact = new Paragraph("For inquiries, contact: support@ceylonrailease.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setMarginTop(10);
            document.add(contact);

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPadding(8);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "N/A"))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                .setPadding(8);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
