/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'build-success': '#10b981',
        'build-failure': '#ef4444',
        'build-pending': '#eab308',
        'build-progress': '#3b82f6',
        'build-cancelled': '#6b7280',
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      }
    },
  },
  plugins: [],
}
