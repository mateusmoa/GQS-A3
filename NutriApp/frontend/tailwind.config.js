/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        nutriapp: {
          primary: '#2563eb',
          secondary: '#10b981',
          accent: '#f59e0b',
        }
      }
    },
  },
  plugins: [],
}