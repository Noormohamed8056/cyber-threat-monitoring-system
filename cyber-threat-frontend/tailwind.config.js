/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        dark: {
          500: '#0a0f18',
          400: '#0f1520',
          300: '#131929',
          200: '#172033',
          100: '#1e293b',
        },
        cyber: {
          blue:   '#00d4ff',
          green:  '#00ff88',
          red:    '#ff4757',
          yellow: '#ffd32a',
          purple: '#a55eea',
        },
      },
      backgroundImage: {
        'cyber-gradient':
          'linear-gradient(135deg, #0a0f18 0%, #131929 50%, #0a0f18 100%)',
      },
      animation: {
        'fade-in':    'fadeIn 0.5s ease-in-out',
        'slide-up':   'slideUp 0.4s ease-out',
        'slide-in':   'slideIn 0.3s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%':   { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)',    opacity: '1' },
        },
        slideIn: {
          '0%':   { transform: 'translateX(-10px)', opacity: '0' },
          '100%': { transform: 'translateX(0)',      opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}