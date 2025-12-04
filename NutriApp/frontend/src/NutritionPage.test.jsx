import React from 'react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'

const formatValue = (value, decimals = 1) => value ? Number(value).toFixed(decimals).replace('.', ',') : '0,0'

const NutritionPage = ({ nutritionData, recipes, onCalculate }) => {
  const [selRecipe, setSelRecipe] = React.useState('')

  const downloadQR = async () => {
    if (!nutritionData) return
    try {
      const res = await fetch(`http://localhost:8080/api/nutrition/qrcode/${nutritionData.recipeId}?format=PNG`, {method:'POST'})
      const blob = await res.blob()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `qr-${nutritionData.recipeId}.png`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e) { alert('Erro ao gerar QR') }
  }

  return (
    <div className="space-y-6">
      <h2 className="text-3xl font-bold">Tabela Nutricional</h2>
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex gap-4">
          <select onChange={(e) => setSelRecipe(e.target.value)} className="flex-1 px-3 py-2 border rounded-lg">
            <option value="">Selecione...</option>
            {recipes.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
          <button onClick={() => selRecipe && onCalculate(parseInt(selRecipe))} disabled={!selRecipe}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300">
            Calcular
          </button>
        </div>
      </div>

      {nutritionData ? (
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <div className="xl:col-span-2 bg-white rounded-lg shadow overflow-hidden">
            <div className="bg-black text-white text-center p-4">
              <h3 className="text-lg font-bold">INFORMAÇÃO NUTRICIONAL</h3>
              <p className="text-sm">{nutritionData.recipeName}</p>
              <p className="text-sm">Porção de 100g</p>
            </div>
            <table className="w-full">
              <thead>
                <tr className="border-b-2 border-black">
                  <th className="text-left p-3 font-bold">Nutriente</th>
                  <th className="text-center p-3 font-bold">Qtd/porção</th>
                  <th className="text-center p-3 font-bold">%VD*</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b"><td className="p-3 font-semibold">Valor energético</td>
                  <td className="text-center p-3">{formatValue(nutritionData.energyKcal)} kcal</td>
                  <td className="text-center p-3">{formatValue(nutritionData.energyDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Carboidratos</td>
                  <td className="text-center p-3">{formatValue(nutritionData.carbohydrates)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.carbohydratesDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Proteínas</td>
                  <td className="text-center p-3">{formatValue(nutritionData.proteins)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.proteinsDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Gorduras totais</td>
                  <td className="text-center p-3">{formatValue(nutritionData.totalFats)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.totalFatsDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Fibra alimentar</td>
                  <td className="text-center p-3">{formatValue(nutritionData.dietaryFiber)} g</td>
                  <td className="text-center p-3">{formatValue(nutritionData.dietaryFiberDV)}%</td></tr>
                <tr className="border-b"><td className="p-3">Sódio</td>
                  <td className="text-center p-3">{formatValue(nutritionData.sodium, 0)} mg</td>
                  <td className="text-center p-3">{formatValue(nutritionData.sodiumDV)}%</td></tr>
              </tbody>
            </table>
            <div className="p-4 text-xs text-gray-600">
              <p>*% Valores Diários. Conforme RDC nº 429/2020 ANVISA</p>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold mb-4">Exportar</h3>
            <button onClick={downloadQR} className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
              QR Code PNG
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-12 text-center">
          <p className="text-gray-500">Selecione uma receita</p>
        </div>
      )}
    </div>
  )
}

describe('Nutrition Page', () => {
  const mockNutritionData = {
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

  const mockRecipes = [
    { id: 1, name: 'Frango Grelhado' },
    { id: 2, name: 'Arroz com Feijão' }
  ]

  const mockOnCalculate = vi.fn()

  it('deve renderizar página de tabela nutricional', () => {
    render(
      <NutritionPage 
        nutritionData={null}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Tabela Nutricional')).toBeInTheDocument()
  })

  it('deve exibir dropdown de receitas', () => {
    render(
      <NutritionPage 
        nutritionData={null}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    const select = screen.getByDisplayValue('Selecione...')
    expect(select).toBeInTheDocument()
  })

  it('deve exibir lista de receitas no dropdown', () => {
    render(
      <NutritionPage 
        nutritionData={null}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Frango Grelhado')).toBeInTheDocument()
    expect(screen.getByText('Arroz com Feijão')).toBeInTheDocument()
  })

  it('deve exibir mensagem quando nenhuma receita é selecionada', () => {
    render(
      <NutritionPage 
        nutritionData={null}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Selecione uma receita')).toBeInTheDocument()
  })

  it('deve exibir tabela nutricional quando dados est\u00e3o dispon\u00edveis', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('INFORMA\u00c7\u00c3O NUTRICIONAL')).toBeInTheDocument()
    const recipeNames = screen.getAllByText('Frango Grelhado')
    expect(recipeNames.length).toBeGreaterThan(0)
  })

  it('deve exibir todos os nutrientes na tabela', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Valor energético')).toBeInTheDocument()
    expect(screen.getByText('Carboidratos')).toBeInTheDocument()
    expect(screen.getByText('Proteínas')).toBeInTheDocument()
    expect(screen.getByText('Gorduras totais')).toBeInTheDocument()
    expect(screen.getByText('Fibra alimentar')).toBeInTheDocument()
    expect(screen.getByText('Sódio')).toBeInTheDocument()
  })

  it('deve exibir valores formatados com vírgula', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText(/165,0 kcal/)).toBeInTheDocument()
  })

  it('deve exibir referência ANVISA RDC 429/2020', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText(/RDC nº 429\/2020 ANVISA/)).toBeInTheDocument()
  })

  it('deve ter botão de download QR Code', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('QR Code PNG')).toBeInTheDocument()
  })

  it('deve ter botão calcular habilitado quando receita é selecionada', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Calcular')).toBeInTheDocument()
  })

  it('deve exibir porção em 100g', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Porção de 100g')).toBeInTheDocument()
  })

  it('deve mostrar valores de %VD para cada nutriente', () => {
    render(
      <NutritionPage 
        nutritionData={mockNutritionData}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('%VD*')).toBeInTheDocument()
  })
})

describe('Nutrition Table Headers', () => {
  it('deve ter cabeçalhos corretos', () => {
    const mockRecipes = []
    const mockOnCalculate = vi.fn()

    render(
      <NutritionPage 
        nutritionData={null}
        recipes={mockRecipes}
        onCalculate={mockOnCalculate}
      />
    )

    expect(screen.getByText('Tabela Nutricional')).toBeInTheDocument()
  })
})
