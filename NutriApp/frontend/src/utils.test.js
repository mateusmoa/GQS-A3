import { describe, it, expect } from 'vitest'

const formatValue = (value, decimals = 1) => {
  if (value === null || value === undefined) return '0,0'
  return Number(value).toFixed(decimals).replace('.', ',')
}

describe('formatValue - Utility Function', () => {
  describe('Basic Formatting', () => {
    it('deve formatar inteiros com uma casa decimal', () => {
      expect(formatValue(165)).toBe('165,0')
      expect(formatValue(130)).toBe('130,0')
      expect(formatValue(100)).toBe('100,0')
    })

    it('deve formatar decimais com arredondamento', () => {
      expect(formatValue(130.5)).toBe('130,5')
      expect(formatValue(165.7)).toBe('165,7')
      expect(formatValue(99.99)).toBe('100,0')
    })

    it('deve substituir ponto por vírgula', () => {
      expect(formatValue(123.45)).toBe('123,5')
      expect(formatValue(200.01)).toBe('200,0')
    })
  })

  describe('Custom Decimal Places', () => {
    it('deve aceitar número de casas decimais customizado', () => {
      expect(formatValue(165, 2)).toBe('165,00')
      expect(formatValue(130.5, 2)).toBe('130,50')
      expect(formatValue(100.999, 2)).toBe('101,00')
    })

    it('deve formatar com zero casas decimais', () => {
      expect(formatValue(165, 0)).toBe('165')
      expect(formatValue(130.7, 0)).toBe('131')
      expect(formatValue(99.4, 0)).toBe('99')
    })

    it('deve formatar com três casas decimais', () => {
      expect(formatValue(165.123, 3)).toBe('165,123')
      expect(formatValue(130, 3)).toBe('130,000')
    })
  })

  describe('Edge Cases', () => {
    it('deve retornar "0,0" para null', () => {
      expect(formatValue(null)).toBe('0,0')
    })

    it('deve retornar "0,0" para undefined', () => {
      expect(formatValue(undefined)).toBe('0,0')
    })

    it('deve funcionar com zero', () => {
      expect(formatValue(0)).toBe('0,0')
      expect(formatValue(0, 2)).toBe('0,00')
      expect(formatValue(0, 0)).toBe('0')
    })

    it('deve funcionar com números negativos', () => {
      expect(formatValue(-165)).toBe('-165,0')
      expect(formatValue(-0.5)).toBe('-0,5')
    })

    it('deve funcionar com números muito grandes', () => {
      expect(formatValue(999999.99)).toBe('1000000,0')
      expect(formatValue(1000000)).toBe('1000000,0')
    })

    it('deve funcionar com números muito pequenos', () => {
      expect(formatValue(0.001)).toBe('0,0')
      expect(formatValue(0.05)).toBe('0,1')
    })
  })

  describe('Nutritional Values', () => {
    it('deve formatar energia (kcal)', () => {
      expect(formatValue(165.0)).toBe('165,0')
      expect(formatValue(130.25)).toBe('130,3')
    })

    it('deve formatar macronutrientes (g)', () => {
      expect(formatValue(31.0)).toBe('31,0')
      expect(formatValue(28.5)).toBe('28,5')
      expect(formatValue(3.6)).toBe('3,6')
    })

    it('deve formatar sódio (mg)', () => {
      expect(formatValue(75, 0)).toBe('75')
      expect(formatValue(500.5, 0)).toBe('501')
    })

    it('deve formatar %VD', () => {
      expect(formatValue(8.25)).toBe('8,3')
      expect(formatValue(62.0)).toBe('62,0')
      expect(formatValue(6.5)).toBe('6,5')
    })
  })

  describe('Type Coercion', () => {
    it('deve converter string para número', () => {
      expect(formatValue('165')).toBe('165,0')
      expect(formatValue('130.5')).toBe('130,5')
    })

    it('deve tratar valores falsy corretamente', () => {
      expect(formatValue(false)).toBe('0,0')
      expect(formatValue('')).toBe('0,0')
      expect(formatValue(0)).toBe('0,0')
    })
  })

  describe('Brazilian Format Compliance', () => {
    it('deve usar vírgula como separador decimal (padrão brasileiro)', () => {
      const result = formatValue(123.456)
      expect(result).toContain(',')
      expect(result).not.toContain('.')
    })

    it('deve manter formato consistente', () => {
      const values = [1.5, 10.5, 100.5, 1000.5]
      const formatted = values.map(v => formatValue(v))
      
      formatted.forEach(f => {
        expect(f).toMatch(/^\d+,\d$/)
      })
    })
  })

  describe('Performance', () => {
    it('deve processar múltiplos valores rapidamente', () => {
      const values = Array.from({ length: 1000 }, (_, i) => i * 0.5)
      const start = performance.now()
      
      values.forEach(v => formatValue(v))
      
      const end = performance.now()
      expect(end - start).toBeLessThan(100) // Should be very fast
    })
  })
})
