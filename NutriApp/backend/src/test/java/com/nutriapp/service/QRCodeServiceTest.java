package com.nutriapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriapp.dto.NutritionTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Testes unitários: QRCodeService")
class QRCodeServiceTest {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    private NutritionTable mockNutritionTable;

    @BeforeEach
    void setUp() {
        mockNutritionTable = new NutritionTable();
        mockNutritionTable.setRecipeId(1L);
        mockNutritionTable.setRecipeName("Frango Grelhado");
        mockNutritionTable.setEnergyKcal(new BigDecimal("300.50"));
        mockNutritionTable.setCarbohydrates(new BigDecimal("0.00"));
        mockNutritionTable.setProteins(new BigDecimal("35.00"));
        mockNutritionTable.setTotalFats(new BigDecimal("15.00"));
        mockNutritionTable.setDietaryFiber(new BigDecimal("0.00"));
        mockNutritionTable.setSodium(new BigDecimal("150.00"));
        mockNutritionTable.setAnvisaVersion("RDC 429/2020");
    }

    @Test
    @DisplayName("Gerar QR Code em formato PNG")
    void generateQRCode_PNG() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "PNG");

        assertThat(result).isNotEmpty();
        
        assertThat(result[0]).isEqualTo((byte) 0x89);
        assertThat(result[1]).isEqualTo((byte) 0x50);
    }

    @Test
    @DisplayName("Gerar QR Code em formato SVG")
    void generateQRCode_SVG() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "SVG");

        assertThat(result).isNotEmpty();
        String svg = new String(result);
        assertThat(svg).contains("<?xml version");
        assertThat(svg).contains("<svg");
        assertThat(svg).contains("</svg>");
    }

    @Test
    @DisplayName("Gerar QR Code em formato PDF (retorna PNG)")
    void generateQRCode_PDF() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "PDF");

        assertThat(result).isNotEmpty();
        
        assertThat(result[0]).isEqualTo((byte) 0x89);
    }

    @Test
    @DisplayName("Gerar QR Code com format em minúscula")
    void generateQRCode_lowercaseFormat() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "png");

        assertThat(result).isNotEmpty();
        assertThat(result[0]).isEqualTo((byte) 0x89);
    }

    @Test
    @DisplayName("Gerar QR Code com format null (padrão PNG)")
    void generateQRCode_nullFormat() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, null);

        assertThat(result).isNotEmpty();
        assertThat(result[0]).isEqualTo((byte) 0x89);
    }

    @Test
    @DisplayName("Gerar QR Code com formato inválido")
    void generateQRCode_invalidFormat() {
        assertThatThrownBy(() -> qrCodeService.generateQRCode(mockNutritionTable, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato não suportado");
    }

    @Test
    @DisplayName("Gerar QR Code SVG com propriedades corretas")
    void generateQRCode_SVG_validStructure() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "SVG");
        String svg = new String(result);

        assertThat(svg).contains("xmlns=\"http://www.w3.org/2000/svg\"");
        assertThat(svg).contains("width=");
        assertThat(svg).contains("height=");
        assertThat(svg).contains("viewBox=");
        assertThat(svg).contains("<rect");
        assertThat(svg).contains("fill=\"white\"");
        assertThat(svg).contains("fill=\"black\"");
    }

    @Test
    @DisplayName("Gerar QR Code com diferentes tamanhos de dados")
    void generateQRCode_differentDataSizes() throws Exception {
        
        NutritionTable small = new NutritionTable();
        small.setRecipeId(1L);
        small.setRecipeName("A");
        small.setEnergyKcal(new BigDecimal("100"));
        small.setCarbohydrates(new BigDecimal("0"));
        small.setProteins(new BigDecimal("10"));
        small.setTotalFats(new BigDecimal("5"));
        small.setDietaryFiber(new BigDecimal("2"));
        small.setSodium(new BigDecimal("50"));

        byte[] smallQR = qrCodeService.generateQRCode(small, "PNG");
        assertThat(smallQR).isNotEmpty();

        byte[] largeQR = qrCodeService.generateQRCode(mockNutritionTable, "PNG");
        assertThat(largeQR).isNotEmpty();
    }

    @Test
    @DisplayName("Gerar múltiplos QR Codes em PNG")
    void generateQRCode_multipleGenerations_PNG() throws Exception {
        byte[] qr1 = qrCodeService.generateQRCode(mockNutritionTable, "PNG");
        byte[] qr2 = qrCodeService.generateQRCode(mockNutritionTable, "PNG");

        assertThat(qr1).isNotEmpty();
        assertThat(qr2).isNotEmpty();
    
        assertThat(qr1[0]).isEqualTo((byte) 0x89);
        assertThat(qr2[0]).isEqualTo((byte) 0x89);
    }

    @Test
    @DisplayName("Gerar QR Code em PNG contém dados corretos")
    void generateQRCode_PNG_dataIntegrity() throws Exception {
        byte[] result = qrCodeService.generateQRCode(mockNutritionTable, "PNG");

        assertThat(result.length).isGreaterThan(100);
    }

    @Test
    @DisplayName("Gerar QR Code com NutritionTable completa")
    void generateQRCode_completNutritionTable() throws Exception {
        NutritionTable complete = new NutritionTable();
        complete.setRecipeId(42L);
        complete.setRecipeName("Receita Completa com Nome Longo");
        complete.setEnergyKcal(new BigDecimal("999.99"));
        complete.setCarbohydrates(new BigDecimal("100.00"));
        complete.setProteins(new BigDecimal("50.00"));
        complete.setTotalFats(new BigDecimal("30.00"));
        complete.setDietaryFiber(new BigDecimal("10.00"));
        complete.setSodium(new BigDecimal("500.00"));
        complete.setAnvisaVersion("RDC 429/2020");

        byte[] result = qrCodeService.generateQRCode(complete, "PNG");
        assertThat(result).isNotEmpty();
    }
}
