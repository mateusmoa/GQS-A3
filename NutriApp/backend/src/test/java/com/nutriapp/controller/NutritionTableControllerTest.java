package com.nutriapp.controller;

import com.nutriapp.dto.NutritionTable;
import com.nutriapp.service.NutritionCalculationService;
import com.nutriapp.service.QRCodeService;
import com.nutriapp.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários: NutritionTableController")
class NutritionTableControllerTest {

    @Mock
    private NutritionCalculationService calculationService;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private NutritionTableController controller;

    private MockMvc mockMvc;
    private NutritionTable mockNutritionTable;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        mockNutritionTable = new NutritionTable();
        mockNutritionTable.setRecipeId(1L);
        mockNutritionTable.setRecipeName("Receita Teste");
        mockNutritionTable.setEnergyKcal(new BigDecimal("500.00"));
        mockNutritionTable.setCarbohydrates(new BigDecimal("50.00"));
        mockNutritionTable.setProteins(new BigDecimal("20.00"));
        mockNutritionTable.setTotalFats(new BigDecimal("15.00"));
        mockNutritionTable.setDietaryFiber(new BigDecimal("5.00"));
        mockNutritionTable.setSodium(new BigDecimal("200.00"));
        mockNutritionTable.setAnvisaVersion("RDC 429/2020");
    }

    @Test
    @DisplayName("GET /api/nutrition/recipe/{id} - calcular com sucesso")
    void getCalculateRecipeNutrition_success() throws Exception {
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);

        mockMvc.perform(get("/api/nutrition/recipe/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.recipeName").value("Receita Teste"))
                .andExpect(jsonPath("$.energyKcal").value(500.00));

        verify(recipeService, times(1)).findById(1L);
        verify(calculationService, times(1)).calculateRecipeNutrition(any());
    }

    @Test
    @DisplayName("GET /api/nutrition/recipe/{id} - receita não encontrada")
    void getCalculateRecipeNutrition_notFound() throws Exception {
        when(recipeService.findById(anyLong())).thenThrow(new RuntimeException("Receita não encontrada"));

        mockMvc.perform(get("/api/nutrition/recipe/999")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(recipeService, times(1)).findById(999L);
        verify(calculationService, never()).calculateRecipeNutrition(any());
    }



    @Test
    @DisplayName("POST /api/nutrition/qrcode/{id} - gerar QR Code PNG")
    void postGenerateQRCode_PNG() throws Exception {
        byte[] qrBytes = new byte[]{1, 2, 3, 4, 5};
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);
        when(qrCodeService.generateQRCode(any(), eq("PNG"))).thenReturn(qrBytes);

        mockMvc.perform(post("/api/nutrition/qrcode/1?format=PNG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().exists("Content-Disposition"));

        verify(qrCodeService, times(1)).generateQRCode(any(), eq("PNG"));
    }

    @Test
    @DisplayName("POST /api/nutrition/qrcode/{id} - gerar QR Code SVG")
    void postGenerateQRCode_SVG() throws Exception {
        byte[] qrBytes = "<svg></svg>".getBytes();
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);
        when(qrCodeService.generateQRCode(any(), eq("SVG"))).thenReturn(qrBytes);

        mockMvc.perform(post("/api/nutrition/qrcode/1?format=SVG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));

        verify(qrCodeService, times(1)).generateQRCode(any(), eq("SVG"));
    }

    @Test
    @DisplayName("POST /api/nutrition/qrcode/{id} - formato padrão PNG")
    void postGenerateQRCode_defaultFormat() throws Exception {
        byte[] qrBytes = new byte[]{1, 2, 3};
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);
        when(qrCodeService.generateQRCode(any(), eq("PNG"))).thenReturn(qrBytes);

        mockMvc.perform(post("/api/nutrition/qrcode/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        verify(qrCodeService, times(1)).generateQRCode(any(), eq("PNG"));
    }

    @Test
    @DisplayName("POST /api/nutrition/qrcode/{id} - erro ao gerar QR Code")
    void postGenerateQRCode_error() throws Exception {
        when(recipeService.findById(anyLong())).thenThrow(new RuntimeException("Erro ao gerar"));

        mockMvc.perform(post("/api/nutrition/qrcode/1?format=PNG")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/nutrition/export/{id} - exportar em PDF")
    void postExportNutritionTable_PDF() throws Exception {
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);

        mockMvc.perform(post("/api/nutrition/export/1?format=PDF")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("POST /api/nutrition/export/{id} - exportar em Excel")
    void postExportNutritionTable_EXCEL() throws Exception {
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);

        mockMvc.perform(post("/api/nutrition/export/1?format=EXCEL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("POST /api/nutrition/export/{id} - exportar em HTML")
    void postExportNutritionTable_HTML() throws Exception {
        when(calculationService.calculateRecipeNutrition(any())).thenReturn(mockNutritionTable);

        mockMvc.perform(post("/api/nutrition/export/1?format=HTML")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    @DisplayName("POST /api/nutrition/export/{id} - erro ao exportar")
    void postExportNutritionTable_error() throws Exception {
        when(recipeService.findById(anyLong())).thenThrow(new RuntimeException("Erro ao exportar"));

        mockMvc.perform(post("/api/nutrition/export/1?format=PDF")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/nutrition/health - health check")
    void getHealthCheck() throws Exception {
        mockMvc.perform(get("/api/nutrition/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("NutriApp Nutrition Service is running"));
    }
}
