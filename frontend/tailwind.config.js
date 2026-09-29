/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cadastral: {
          dark: "#0a0f1d",
          surface: "#111827",
          card: "#1f2937",
          border: "#374151",
          cyan: "#00f2fe",
          blue: "#4facfe",
          emerald: "#10b981",
          amber: "#f59e0b",
          coral: "#ef4444"
        }
      }
    },
  },
  plugins: [],
}
