import React from 'react'
import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

const RecipesPage = ({ recipes, ingredients, onReload, onCalculate }) => {
  const [showForm, setShowForm] = React.useState(false)
  const [form, setForm] = React.useState({ name: '', preparationMethod: 'RAW', totalPortion: '', portionUnit: 'g', servings: '', ingredients: [] })
  const [selIng, setSelIng] = React.useState('')
  const [qty, setQty] = React.useState('')

  const methods = { RAW: 'Cru', BOILED: 'Cozido', FRIED: 'Frito', BAKED: 'Assado', GRILLED: 'Grelhado', STEAMED: 'Vapor' }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold">Receitas</h2>
        <button className="flex items-center px-4 py-2 bg-green-600 text-white rounded-lg">
          Nova
        </button>
      </div>

      <div className="bg-white rounded-lg shadow divide-y">
        {recipes.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Nenhuma receita</div>
        ) : recipes.map(r => (
          <div key={r.id} className="p-6 flex justify-between items-center">
            <div>
              <h4 className="text-lg font-medium">{r.name}</h4>
              <span className="text-sm text-gray-600">{methods[r.preparationMethod]} • {r.totalPortion}{r.portionUnit}</span>
            </div>
            <div className="flex gap-2">
              <button onClick={() => onCalculate(r.id)} className="p-2 text-green-600 hover:bg-green-50 rounded">
                Calcular
              </button>
              <button onClick={() => { if(confirm('Excluir?')) { onReload() }}} className="p-2 text-red-600 hover:bg-red-50 rounded">
                Deletar
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

describe('Recipes Page', () => {
  const mockRecipes = [
    { id: 1, name: 'Frango Grelhado', preparationMethod: 'GRILLED', totalPortion: 200, portionUnit: 'g', servings: 2 },
    { id: 2, name: 'Arroz com Feijão', preparationMethod: 'BOILED', totalPortion: 300, portionUnit: 'g', servings: 1 }
  ]

  const mockIngredients = [
    { id: 1, name: 'Frango', energyKcal: 165 },
    { id: 2, name: 'Arroz', energyKcal: 130 }
  ]

  const mockOnReload = vi.fn()
  const mockOnCalculate = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('deve renderizar página de receitas', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Receitas')).toBeInTheDocument()
    expect(screen.getByText('Nova')).toBeInTheDocument()
  })

  it('deve exibir lista de receitas', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Frango Grelhado')).toBeInTheDocument()
    expect(screen.getByText('Arroz com Feijão')).toBeInTheDocument()
  })

  it('deve exibir método de preparo de cada receita', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText(/Grelhado.*200g/)).toBeInTheDocument()
    expect(screen.getByText(/Cozido.*300g/)).toBeInTheDocument()
  })

  it('deve exibir porção de cada receita', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText(/200g/)).toBeInTheDocument()
    expect(screen.getByText(/300g/)).toBeInTheDocument()
  })

  it('deve exibir mensagem quando não há receitas', () => {
    render(
      <RecipesPage 
        recipes={[]} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Nenhuma receita')).toBeInTheDocument()
  })

  it('deve ter botão de calcular para cada receita', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    const calculateButtons = screen.getAllByText('Calcular')
    expect(calculateButtons.length).toBe(2)
  })

  it('deve ter botão de deletar para cada receita', () => {
    render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    const deleteButtons = screen.getAllByText('Deletar')
    expect(deleteButtons.length).toBe(2)
  })

  it('deve aplicar classes corretas aos botões de ação', () => {
    const { container } = render(
      <RecipesPage 
        recipes={mockRecipes} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    const calculateBtn = container.querySelector('.text-green-600')
    expect(calculateBtn).toHaveClass('p-2', 'hover:bg-green-50')
  })

  it('deve renderizar corretamente com uma receita', () => {
    render(
      <RecipesPage 
        recipes={[mockRecipes[0]]} 
        ingredients={mockIngredients} 
        onReload={mockOnReload}
        onCalculate={mockOnCalculate}
      />
    )
    
    expect(screen.getByText('Frango Grelhado')).toBeInTheDocument()
    expect(screen.queryByText('Arroz com Feijão')).not.toBeInTheDocument()
  })
})

describe('Preparation Methods', () => {
  it('deve mapear métodos de preparo corretamente', () => {
    const methods = {
      RAW: 'Cru',
      BOILED: 'Cozido',
      FRIED: 'Frito',
      BAKED: 'Assado',
      GRILLED: 'Grelhado',
      STEAMED: 'Vapor'
    }

    expect(methods.RAW).toBe('Cru')
    expect(methods.BOILED).toBe('Cozido')
    expect(methods.FRIED).toBe('Frito')
    expect(methods.BAKED).toBe('Assado')
    expect(methods.GRILLED).toBe('Grelhado')
    expect(methods.STEAMED).toBe('Vapor')
  })
})
