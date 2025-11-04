package com.nutriapp.controller;

import com.nutriapp.dto.NutritionTable;
import com.nutriapp.entity.Recipe;
import com.nutriapp.service.NutritionCalculationService;
import com.nutriapp.service.QRCodeService;
import com.nutriapp.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nutrition")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tabela Nutricional", description = "API para cálculo e exportação de tabelas nutricionais")
public class NutritionTableController {
    
    private final NutritionCalculationService calculationService;
    private final QRCodeService qrCodeService;
    private final RecipeService recipeService;
    
    @GetMapping("/recipe/{recipeId}")
    @Operation(summary = "Calcular tabela nutricional", 
               description = "Calcula valores nutricionais e %VD conforme ANVISA RDC 429/2020")
    public ResponseEntity<NutritionTable> calculateRecipeNutrition(@PathVariable Long recipeId) {
        log.info("GET /api/nutrition/recipe/{} - Calcular tabela nutricional", recipeId);
        
        try {
            Recipe recipe = recipeService.findById(recipeId);
            NutritionTable table = calculationService.calculateRecipeNutrition(recipe);
            return ResponseEntity.ok(table);
        } catch (RuntimeException e) {
            log.error("Erro ao calcular tabela nutricional: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro interno ao calcular tabela nutricional", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/qrcode/{recipeId}")
    @Operation(summary = "Gerar QR Code", 
               description = "Gera QR Code com dados nutricionais em formato PNG, SVG ou PDF")
    public ResponseEntity<byte[]> generateQRCode(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "PNG") String format) {
        
        log.info("POST /api/nutrition/qrcode/{}?format={} - Gerar QR Code", recipeId, format);
        
        try {
            Recipe recipe = recipeService.findById(recipeId);
            NutritionTable table = calculationService.calculateRecipeNutrition(recipe);
            byte[] qrCode = qrCodeService.generateQRCode(table, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                String.format("attachment; filename=nutriapp-qrcode-%d.%s", recipeId, format.toLowerCase()));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(qrCode);
                
        } catch (RuntimeException e) {
            log.error("Erro ao gerar QR Code: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro interno ao gerar QR Code", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/export/{recipeId}")
    @Operation(summary = "Exportar tabela nutricional", 
               description = "Exporta tabela em formato PDF, Excel ou HTML")
    public ResponseEntity<byte[]> exportNutritionTable(
            @PathVariable Long recipeId,
            @RequestParam String format) {
        
        log.info("POST /api/nutrition/export/{}?format={} - Exportar tabela", recipeId, format);
        
        try {
            Recipe recipe = recipeService.findById(recipeId);
            NutritionTable table = calculationService.calculateRecipeNutrition(recipe);
            
            // Placeholder - implementar geração real de PDF/Excel/HTML
            byte[] exportData = generateExport(table, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(format));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                String.format("attachment; filename=nutriapp-tabela-%d.%s", 
                    recipeId, getExtension(format)));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(exportData);
                
        } catch (RuntimeException e) {
            log.error("Erro ao exportar tabela: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro interno ao exportar tabela", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check do serviço de nutrição")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("NutriApp Nutrition Service is running");
    }
    
    // Helper methods
    
    private MediaType getMediaType(String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> MediaType.APPLICATION_PDF;
            case "PNG" -> MediaType.IMAGE_PNG;
            case "SVG" -> MediaType.valueOf("image/svg+xml");
            case "HTML" -> MediaType.TEXT_HTML;
            case "EXCEL", "XLSX" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
    
    private String getExtension(String format) {
        return switch (format.toUpperCase()) {
            case "EXCEL" -> "xlsx";
            default -> format.toLowerCase();
        };
    }
    
    private byte[] generateExport(NutritionTable table, String format) {
        // Placeholder - implementar geração real
        String content = String.format("Tabela Nutricional - %s\nFormato: %s\n", 
            table.getRecipeName(), format);
        return content.getBytes();
    }
}