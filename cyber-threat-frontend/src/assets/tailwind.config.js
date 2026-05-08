/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Primary brand colors
        primary: {
          50:  '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        },
        // Dark theme background colors
        dark: {
          50:  '#f8fafc',
          100: '#1e293b',
          200: '#172033',
          300: '#131929',
          400: '#0f1520',
          500: '#0a0f18',
          600: '#070b12',
          700: '#04070d',
          800: '#020408',
          900: '#010204',
        },
        // Cyber accent colors
        cyber: {
          blue:   '#00d4ff',
          green:  '#00ff88',
          red:    '#ff4757',
          yellow: '#ffd32a',
          purple: '#a55eea',
          orange: '#ff6b35',
        },
        // Risk level colors
        risk: {
          low:    '#00ff88',
          medium: '#ffd32a',
          high:   '#ff4757',
          critical: '#a55eea',
        }
      },
      // Background gradients
      backgroundImage: {
        'cyber-gradient':
          'linear-gradient(135deg, #0a0f18 0%, #131929 50%, #0a0f18 100%)',
        'card-gradient':
          'linear-gradient(135deg, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0.02) 100%)',
        'blue-gradient':
          'linear-gradient(135deg, #0ea5e9 0%, #0369a1 100%)',
        'green-gradient':
          'linear-gradient(135deg, #00ff88 0%, #00b359 100%)',
        'red-gradient':
          'linear-gradient(135deg, #ff4757 0%, #c0392b 100%)',
        'purple-gradient':
          'linear-gradient(135deg, #a55eea 0%, #6c3483 100%)',
      },
      // Font family
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      // Border radius
      borderRadius: {
        'xl':  '1rem',
        '2xl': '1.5rem',
        '3xl': '2rem',
      },
      // Box shadows for glassmorphism
      boxShadow: {
        'glass':
          '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
        'glass-hover':
          '0 8px 32px 0 rgba(0, 212, 255, 0.15)',
        'card':
          '0 4px 24px rgba(0, 0, 0, 0.4)',
        'card-hover':
          '0 8px 32px rgba(0, 212, 255, 0.2)',
        'neon-blue':
          '0 0 20px rgba(0, 212, 255, 0.5)',
        'neon-green':
          '0 0 20px rgba(0, 255, 136, 0.5)',
        'neon-red':
          '0 0 20px rgba(255, 71, 87, 0.5)',
      },
      // Animations
      animation: {
        'fade-in':
          'fadeIn 0.5s ease-in-out',
        'slide-in':
          'slideIn 0.3s ease-out',
        'slide-up':
          'slideUp 0.4s ease-out',
        'pulse-slow':
          'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'glow':
          'glow 2s ease-in-out infinite alternate',
        'float':
          'float 3s ease-in-out infinite',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%':   { transform: 'translateX(-10px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        slideUp: {
          '0%':   { transform: 'translateY(10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        glow: {
          '0%':   { boxShadow: '0 0 5px rgba(0, 212, 255, 0.2)' },
          '100%': { boxShadow: '0 0 20px rgba(0, 212, 255, 0.8)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%':      { transform: 'translateY(-10px)' },
        },
      },
      // Backdrop blur for glassmorphism
      backdropBlur: {
        xs: '2px',
        sm: '4px',
        md: '8px',
        lg: '12px',
        xl: '16px',
      },
    },
  },
  plugins: [],
}
