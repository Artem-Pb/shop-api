export const TB = {
  BG:     '#ededea',
  CARD:   '#ffffff',
  INK:    '#0a0a0a',
  MUTED:  '#6a6a6a',
  ACCENT: '#d4ff00',
  RED:    '#ff3a2d',
  GREEN:  '#1fa674',
} as const

export const TBF = {
  display: "'Space Grotesk', sans-serif",
  mono:    "'JetBrains Mono', ui-monospace, 'SF Mono', monospace",
} as const

export const fmt = (n: number | string) =>
  Number(n).toLocaleString('ru-RU') + ' ₽'
