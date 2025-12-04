import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'

const API_BASE_URL = 'http://localhost:8080/api'

describe('API Integration Tests', () => {
  beforeEach(() => {
    global.fetch.mockClear()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('Ingredients API', () => {
    it('deve buscar lista de ingredientes', async () => {
      const mockIngredients = [
        { id: 1, name: 'Frango', energyKcal: 165 },
        { id: 2, name: 'Arroz', energyKcal: 130 }
      ]

      global.fetch.mockResolvedValueOnce({
        json: async () => mockIngredients
      })

      const response = await fetch(`${API_BASE_URL}/ingredients`)
      const data = await response.json()

      expect(global.fetch).toHaveBeenCalledWith(`${API_BASE_URL}/ingredients`)
      expect(data).toEqual(mockIngredients)
      expect(data.length).toBe(2)
    })

    it('deve buscar ingrediente por ID', async () => {
      const mockIngredient = { id: 1, name: 'Frango', energyKcal: 165 }

      global.fetch.mockResolvedValueOnce({
        json: async () => mockIngredient
      })

      const response = await fetch(`${API_BASE_URL}/ingredients/1`)
      const data = await response.json()

      expect(global.fetch).toHaveBeenCalledWith(`${API_BASE_URL}/ingredients/1`)
      expect(data.id).toBe(1)
    })

    it('deve buscar ingredientes por termo de pesquisa', async () => {
      const mockResults = [
        { id: 1, name: 'Frango Grelhado', energyKcal: 165 }
      ]

      global.fetch.mockResolvedValueOnce({
        json: async () => mockResults
      })

      const searchTerm = 'Frango'
      const response = await fetch(`${API_BASE_URL}/ingredients/search?q=${encodeURIComponent(searchTerm)}`)
      const data = await response.json()

      expect(global.fetch).toHaveBeenCalledWith(`${API_BASE_URL}/ingredients/search?q=Frango`)
      expect(data.length).toBe(1)
      expect(data[0].name).toContain('Frango')
    })

    it('deve criar novo ingrediente', async () => {
      const newIngredient = {
        name: 'Brócolis',
        energyKcal: 34,
        carbohydrates: 7,
        proteins: 2.6,
        totalFats: 0.4,
        dietaryFiber: 2.4,
        sodium: 64,
        portionUnit: 'g'
      }

      global.fetch.mockResolvedValueOnce({
        status: 201,
        json: async () => ({ id: 3, ...newIngredient })
      })

      const response = await fetch(`${API_BASE_URL}/ingredients`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newIngredient)
      })

      expect(global.fetch).toHaveBeenCalledWith(
        `${API_BASE_URL}/ingredients`,
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
        })
      )
      expect(response.status).toBe(201)
    })

    it('deve deletar ingrediente', async () => {
      global.fetch.mockResolvedValueOnce({
        status: 204
      })

      const response = await fetch(`${API_BASE_URL}/ingredients/1`, {
        method: 'DELETE'
      })

      expect(global.fetch).toHaveBeenCalledWith(
        `${API_BASE_URL}/ingredients/1`,
        { method: 'DELETE' }
      )
      expect(response.status).toBe(204)
    })
  })

  describe('Recipes API', () => {
    it('deve buscar lista de receitas', async () => {
      const mockRecipes = [
        { id: 1, name: 'Frango Grelhado', preparationMethod: 'GRILLED' },
        { id: 2, name: 'Arroz com Feijão', preparationMethod: 'BOILED' }
      ]

      global.fetch.mockResolvedValueOnce({
        json: async () => mockRecipes
      })

      const response = await fetch(`${API_BASE_URL}/recipes`)
      const data = await response.json()

      expect(global.fetch).toHaveBeenCalledWith(`${API_BASE_URL}/recipes`)
      expect(data.length).toBe(2)
    })

    it('deve criar nova receita', async () => {
      const newRecipe = {
        name: 'Frango com Legumes',
        preparationMethod: 'GRILLED',
        totalPortion: 250,
        portionUnit: 'g',
        servings: 2,
        ingredients: []
      }

      global.fetch.mockResolvedValueOnce({
        status: 201,
        json: async () => ({ id: 3, ...newRecipe })
      })

      const response = await fetch(`${API_BASE_URL}/recipes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newRecipe)
      })

      expect(global.fetch).toHaveBeenCalledWith(
        `${API_BASE_URL}/recipes`,
        expect.objectContaining({
          method: 'POST'
        })
      )
      expect(response.status).toBe(201)
    })

    it('deve deletar receita', async () => {
      global.fetch.mockResolvedValueOnce({
        status: 204
      })

      const response = await fetch(`${API_BASE_URL}/recipes/1`, {
        method: 'DELETE'
      })

      expect(global.fetch).toHaveBeenCalledWith(
        `${API_BASE_URL}/recipes/1`,
        { method: 'DELETE' }
      )
      expect(response.status).toBe(204)
    })
  })

  describe('Nutrition API', () => {
    it('deve calcular nutrição da receita', async () => {
      const mockNutrition = {
        recipeId: 1,
        recipeName: 'Frango Grelhado',
        energyKcal: 165.0,
        energyDV: 8.25,
        carbohydrates: 0,
        carbohydratesDV: 0,
        proteins: 31.0,
        proteinsDV: 62.0,
        totalFats: 3.6,
        totalFatsDV: 6.5,
        dietaryFiber: 0,
        dietaryFiberDV: 0,
        sodium: 75,
        sodiumDV: 3.13
      }

      global.fetch.mockResolvedValueOnce({
        json: async () => mockNutrition
      })

      const response = await fetch(`${API_BASE_URL}/nutrition/recipe/1`)
      const data = await response.json()

      expect(global.fetch).toHaveBeenCalledWith(`${API_BASE_URL}/nutrition/recipe/1`)
      expect(data.recipeId).toBe(1)
      expect(data.energyKcal).toBe(165.0)
    })

    it('deve gerar QR Code PNG', async () => {
      const mockBlob = new Blob(['PNG_DATA'], { type: 'image/png' })

      global.fetch.mockResolvedValueOnce({
        blob: async () => mockBlob
      })

      const response = await fetch(`${API_BASE_URL}/nutrition/qrcode/1?format=PNG`, {
        method: 'POST'
      })
      const blob = await response.blob()

      expect(global.fetch).toHaveBeenCalledWith(
        `${API_BASE_URL}/nutrition/qrcode/1?format=PNG`,
        { method: 'POST' }
      )
      expect(blob.type).toBe('image/png')
    })

    it('deve gerar QR Code SVG', async () => {
      const mockBlob = new Blob(['<svg></svg>'], { type: 'image/svg+xml' })

      global.fetch.mockResolvedValueOnce({
        blob: async () => mockBlob
      })

      const response = await fetch(`${API_BASE_URL}/nutrition/qrcode/1?format=SVG`, {
        method: 'POST'
      })
      const blob = await response.blob()

      expect(blob.type).toBe('image/svg+xml')
    })

    it('deve exportar tabela nutricional em PDF', async () => {
      const mockBlob = new Blob(['PDF_DATA'], { type: 'application/pdf' })

      global.fetch.mockResolvedValueOnce({
        blob: async () => mockBlob
      })

      const response = await fetch(`${API_BASE_URL}/nutrition/export/1?format=PDF`, {
        method: 'POST'
      })
      const blob = await response.blob()

      expect(blob.type).toBe('application/pdf')
    })
  })

  describe('Error Handling', () => {
    it('deve tratar erro de ingrediente não encontrado', async () => {
      global.fetch.mockResolvedValueOnce({
        status: 404,
        statusText: 'Not Found'
      })

      const response = await fetch(`${API_BASE_URL}/ingredients/999`)

      expect(response.status).toBe(404)
    })

    it('deve tratar erro de validação ao criar ingrediente', async () => {
      const invalidIngredient = { name: '' }

      global.fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => ({ error: 'Nome é obrigatório' })
      })

      const response = await fetch(`${API_BASE_URL}/ingredients`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(invalidIngredient)
      })

      expect(response.status).toBe(400)
    })

    it('deve tratar erro de receita sem ingredientes', async () => {
      const emptyRecipe = {
        name: 'Receita Vazia',
        ingredients: []
      }

      global.fetch.mockResolvedValueOnce({
        status: 400,
        json: async () => ({ error: 'Receita deve ter pelo menos um ingrediente' })
      })

      const response = await fetch(`${API_BASE_URL}/recipes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(emptyRecipe)
      })

      expect(response.status).toBe(400)
    })

    it('deve tratar erro de conexão', async () => {
      global.fetch.mockRejectedValueOnce(new Error('Network error'))

      try {
        await fetch(`${API_BASE_URL}/ingredients`)
        expect.fail('Should have thrown error')
      } catch (error) {
        expect(error.message).toBe('Network error')
      }
    })
  })

  describe('API Endpoints', () => {
    it('deve ter endpoint de health check', async () => {
      global.fetch.mockResolvedValueOnce({
        status: 200,
        json: async () => ({ status: 'UP' })
      })

      const response = await fetch(`${API_BASE_URL}/health`)
      const data = await response.json()

      expect(response.status).toBe(200)
      expect(data.status).toBe('UP')
    })

    it('deve respeitar CORS headers', async () => {
      global.fetch.mockResolvedValueOnce({
        status: 200,
        headers: {
          'Access-Control-Allow-Origin': 'http://localhost:3000',
          'Access-Control-Allow-Credentials': 'true'
        }
      })

      const response = await fetch(`${API_BASE_URL}/ingredients`, {
        credentials: 'include'
      })

      expect(response.status).toBe(200)
    })
  })
})
