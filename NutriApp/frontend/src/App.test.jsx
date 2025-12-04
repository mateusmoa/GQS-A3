import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import NutriApp from './App.jsx'

global.fetch = vi.fn()

describe('Dashboard Component', () => {
  beforeEach(() => {
    global.fetch.mockClear()
    global.fetch.mockImplementation((url) => {
      if (url.includes('/ingredients')) {
        return Promise.resolve({
          json: () => Promise.resolve([
            { id: 1, name: 'Frango', energyKcal: 165, carbohydrates: 0, proteins: 31, totalFats: 3.6, dietaryFiber: 0, sodium: 75, portionUnit: 'g' }
          ])
        })
      }
      if (url.includes('/recipes')) {
        return Promise.resolve({
          json: () => Promise.resolve([
            { id: 1, name: 'Frango Grelhado', preparationMethod: 'GRILLED', totalPortion: 200, portionUnit: 'g', servings: 2 }
          ])
        })
      }
      return Promise.reject(new Error('Unknown endpoint'))
    })
  })

  it('deve renderizar o Dashboard ao carregar', async () => {
    render(<NutriApp />)
    
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeInTheDocument()
      expect(screen.getByText('Bem-vindo ao NutriApp')).toBeInTheDocument()
    })
  })

  it('deve exibir contagem de ingredientes e receitas', async () => {
    render(<NutriApp />)
    
    await waitFor(() => {
      const ones = screen.getAllByText('1')
      expect(ones.length).toBeGreaterThan(0)
    })
  })

  it('deve exibir funcionalidades do NutriApp', async () => {
    render(<NutriApp />)
    
    await waitFor(() => {
      expect(screen.getByText('Cálculos automáticos ANVISA')).toBeInTheDocument()
      expect(screen.getByText('Base TBCA oficial')).toBeInTheDocument()
      expect(screen.getByText('Geração de QR Code')).toBeInTheDocument()
      expect(screen.getByText('Exportação múltipla')).toBeInTheDocument()
    })
  })

  it('deve permitir navegação para abas', async () => {
    render(<NutriApp />)
    
    const ingredientesBtn = screen.getByRole('button', { name: 'Ingredientes' })
    fireEvent.click(ingredientesBtn)
    
    await waitFor(() => {
      expect(ingredientesBtn).toHaveClass('bg-blue-600', 'text-white')
    })
  })
})

describe('Navigation', () => {
  beforeEach(() => {
    global.fetch.mockClear()
    global.fetch.mockImplementation((url) => {
      if (url.includes('/ingredients')) {
        return Promise.resolve({ json: () => Promise.resolve([]) })
      }
      if (url.includes('/recipes')) {
        return Promise.resolve({ json: () => Promise.resolve([]) })
      }
      return Promise.reject(new Error('Unknown endpoint'))
    })
  })

  it('deve navegar entre abas', async () => {
    render(<NutriApp />)
    
    expect(screen.getByText('Bem-vindo ao NutriApp')).toBeInTheDocument()
    
  
    fireEvent.click(screen.getByText('Ingredientes'))
    await waitFor(() => {
      expect(screen.getByText(/\d+ ingredientes/)).toBeInTheDocument()
    })
    
    fireEvent.click(screen.getByText('Receitas'))
    await waitFor(() => {
      expect(screen.getByText('Nova')).toBeInTheDocument()
    })
    
    fireEvent.click(screen.getByText('Tabela'))
    await waitFor(() => {
      expect(screen.getByText('Selecione uma receita')).toBeInTheDocument()
    })
  })

  it('deve manter classe ativa no botão da aba selecionada', () => {
    render(<NutriApp />)
    
    const dashboardBtn = screen.getByRole('button', { name: 'Dashboard' })
    expect(dashboardBtn).toHaveClass('bg-blue-600', 'text-white')
    
    const ingredientesBtn = screen.getByRole('button', { name: 'Ingredientes' })
    expect(ingredientesBtn).not.toHaveClass('bg-blue-600')
  })

  it('deve exibir header e footer', () => {
    render(<NutriApp />)
    
    const nutriAppTexts = screen.getAllByText('NutriApp')
    expect(nutriAppTexts.length).toBeGreaterThan(0)
    expect(screen.getByText('Sistema de Índice Nutricional')).toBeInTheDocument()
    expect(screen.getByText(/© 2024/)).toBeInTheDocument()
  })
})

describe('Header', () => {
  beforeEach(() => {
    global.fetch.mockImplementation((url) => {
      if (url.includes('/ingredients')) {
        return Promise.resolve({ json: () => Promise.resolve([]) })
      }
      if (url.includes('/recipes')) {
        return Promise.resolve({ json: () => Promise.resolve([]) })
      }
      return Promise.reject(new Error('Unknown endpoint'))
    })
  })

  it('deve ter logo e informações do app', () => {
    render(<NutriApp />)
    
    const nutriAppTexts = screen.getAllByText('NutriApp')
    expect(nutriAppTexts.length).toBeGreaterThan(0)
    expect(screen.getByText('Sistema de Índice Nutricional')).toBeInTheDocument()
  })

  it('deve ter todos os botões de navegação', () => {
    render(<NutriApp />)
    
    expect(screen.getByRole('button', { name: 'Dashboard' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ingredientes' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Receitas' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Tabela' })).toBeInTheDocument()
  })
})
