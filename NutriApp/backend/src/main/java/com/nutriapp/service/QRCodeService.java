package com.nutriapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nutriapp.dto.NutritionTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j

// Serviço para geração de QR Codes a partir de tabelas nutricionais.

public class QRCodeService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generateQRCode(NutritionTable nutritionTable, String format)
            throws JsonProcessingException, WriterException, IOException {
        String fmt = (format == null) ? "PNG" : format.trim().toUpperCase();
        log.info("Gerando QR Code para receita='{}' formato='{}'", nutritionTable.getRecipeName(), fmt);

        String jsonData = convertToJson(nutritionTable);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(jsonData, BarcodeFormat.QR_CODE, 300, 300);

        switch (fmt) {
            case "PNG":
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
                    return baos.toByteArray();
                }
            case "SVG":
                return generateSVG(bitMatrix);
            case "PDF":
                return generatePDFWithQR(bitMatrix);
            default:
                throw new IllegalArgumentException("Formato não suportado: " + format);
        }
    }

    // Converte tabela nutricional para JSON compactado.

    private String convertToJson(NutritionTable table) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();

        data.put("id", table.getRecipeId());
        data.put("name", table.getRecipeName());
        data.put("kcal", Math.round(table.getEnergyKcal().doubleValue()));
        data.put("carbs", Math.round(table.getCarbohydrates().doubleValue()));
        data.put("protein", Math.round(table.getProteins().doubleValue()));
        data.put("fat", Math.round(table.getTotalFats().doubleValue()));
        data.put("fiber", Math.round(table.getDietaryFiber().doubleValue()));
        data.put("sodium", Math.round(table.getSodium().doubleValue()));
        data.put("timestamp", System.currentTimeMillis());
        data.put("anvisa", table.getAnvisaVersion());

        return objectMapper.writeValueAsString(data);
    }

    // Gera QR Code em formato SVG.

    private byte[] generateSVG(BitMatrix bitMatrix) {
        final int pixelSize = 300; // tamanho em px do SVG
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        double cellSize = (double) pixelSize / Math.max(width, height);

        StringBuilder svg = new StringBuilder( (int)(pixelSize * pixelSize / 4) );
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(pixelSize).append("\" height=\"").append(pixelSize).append("\" viewBox=\"0 0 ").append(pixelSize).append(" ").append(pixelSize).append("\">\n");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n");

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    double rx = x * cellSize;
                    double ry = y * cellSize;
                    svg.append(String.format("<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" fill=\"black\"/>\n",
                            rx, ry, cellSize, cellSize));
                }
            }
        }

        svg.append("</svg>");
        log.debug("QR Code SVG gerado com sucesso ({}x{})", width, height);
        return svg.toString().getBytes();
    }

    // Gera PDF com QR Code (implementação simplificada: retorna PNG bytes).

    private byte[] generatePDFWithQR(BitMatrix bitMatrix) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        }
    }
}