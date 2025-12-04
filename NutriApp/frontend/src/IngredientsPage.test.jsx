import React from 'react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

const formatValue = (value, decimals = 1) => value ? Number(value).toFixed(decimals).replace('.', ',') : '0,0'

const IngredientsPage = ({ ingredients, onReload }) => {
  const [searchTerm, setSearchTerm] = React.useState('')
  const [filtered, setFiltered] = React.useState(ingredients)
  const [showCreate, setShowCreate] = React.useState(false)
  const [form, setForm] = React.useState({
    name: '', portionUnit: 'g', energyKcal: '', carbohydrates: '', proteins: '', totalFats: '',
    dietaryFiber: '', sodium: '', tbcaCode: '', category: ''
  })

  const handleSearch = async () => {
    if (searchTerm) {
      try {
        const res = await fetch(`http://localhost:8080/api/ingredients/search?q=${encodeURIComponent(searchTerm)}`)
        setFiltered(await res.json())
      } catch (e) { alert('Erro na pesquisa') }
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Excluir ingrediente?')) {
      try {
        await fetch(`http://localhost:8080/api/ingredients/${id}`, { method: 'DELETE' })
        onReload()
      } catch (e) { alert('Erro ao excluir') }
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-3xl font-bold">Ingredientes</h2>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">{ingredients.length} ingredientes</span>
        </div>
      </div>
      <div className="bg-white rounded-lg shadow overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nome</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Energia</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Carb</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Prot</th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase">Gord</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {ingredients.map((ing) => (
              <tr key={ing.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{ing.name}</div>
                </td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.energyKcal)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.carbohydrates)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.proteins)}</td>
                <td className="px-6 py-4 text-center text-sm">{formatValue(ing.totalFats)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

describe('Ingredients Page', () => {
  const mockIngredients = [
    { id: 1, name: 'Frango', energyKcal: 165, carbohydrates: 0, proteins: 31, totalFats: 3.6, portionUnit: 'g' },
    { id: 2, name: 'Arroz', energyKcal: 130, carbohydrates: 28, proteins: 2.7, totalFats: 0.3, portionUnit: 'g' },
    { id: 3, name: 'Feijão', energyKcal: 76, carbohydrates: 14, proteins: 5.2, totalFats: 0.2, portionUnit: 'g' }
  ]

  const mockOnReload = vi.fn()

  it('deve renderizar lista de ingredientes', () => {
    render(<IngredientsPage ingredients={mockIngredients} onReload={mockOnReload} />)
    
    expect(screen.getByText('Ingredientes')).toBeInTheDocument()
    expect(screen.getByText('3 ingredientes')).toBeInTheDocument()
  })

  it('deve exibir informações nutricionais dos ingredientes', () => {
    render(<IngredientsPage ingredients={mockIngredients} onReload={mockOnReload} />)
    
    expect(screen.getByText('Frango')).toBeInTheDocument()
    expect(screen.getByText('Arroz')).toBeInTheDocument()
    expect(screen.getByText('Feijão')).toBeInTheDocument()
  })

  it('deve formatar valores com separador de vírgula', () => {
    render(<IngredientsPage ingredients={mockIngredients} onReload={mockOnReload} />)
    
    const cells = screen.getAllByText('165,0')
    expect(cells.length).toBeGreaterThan(0)
  })

  it('deve exibir tabela com cabeçalhos corretos', () => {
    render(<IngredientsPage ingredients={mockIngredients} onReload={mockOnReload} />)
    
    expect(screen.getByText('Nome')).toBeInTheDocument()
    expect(screen.getByText('Energia')).toBeInTheDocument()
    expect(screen.getByText('Carb')).toBeInTheDocument()
    expect(screen.getByText('Prot')).toBeInTheDocument()
    expect(screen.getByText('Gord')).toBeInTheDocument()
  })

  it('deve renderizar vazio quando não há ingredientes', () => {
    render(<IngredientsPage ingredients={[]} onReload={mockOnReload} />)
    
    expect(screen.getByText('0 ingredientes')).toBeInTheDocument()
  })

  it('deve aplicar classes de hover aos ingredientes', () => {
    const { container } = render(<IngredientsPage ingredients={mockIngredients} onReload={mockOnReload} />)
    
    const rows = container.querySelectorAll('tbody tr')
    rows.forEach(row => {
      expect(row).toHaveClass('hover:bg-gray-50')
    })
  })
})

describe('formatValue utility', () => {
  it('deve formatar números com uma casa decimal', () => {
    expect(formatValue(165)).toBe('165,0')
    expect(formatValue(130.5)).toBe('130,5')
  })

  it('deve aceitar número de casas decimais customizado', () => {
    expect(formatValue(165, 2)).toBe('165,00')
    expect(formatValue(75, 0)).toBe('75')
  })

  it('deve retornar "0,0" para valores nulos ou undefined', () => {
    expect(formatValue(null)).toBe('0,0')
    expect(formatValue(undefined)).toBe('0,0')
  })

  it('deve converter ponto em vírgula para formato brasileiro', () => {
    expect(formatValue(123.45)).toBe('123,5')
    expect(formatValue(100.99, 2)).toBe('100,99')
  })

  it('deve funcionar com valores zero', () => {
    expect(formatValue(0)).toBe('0,0')
    expect(formatValue(0.5)).toBe('0,5')
  })
})
