import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        canvas: {
          bg: "#080B11",
          subtle: "#0D131F",
          panel: "#121A2A",
          card: "#162034",
          border: "#1E2C44",
          borderHover: "#2C3E60",
          text: "#F1F5F9",
          muted: "#94A3B8",
          dim: "#64748B",
        },
        brand: {
          green: "#10B981",
          greenDim: "#064E3B",
          red: "#F43F5E",
          redDim: "#881337",
          cyan: "#06B6D4",
          cyanDim: "#164E63",
          purple: "#8B5CF6",
          purpleDim: "#4C1D95",
          amber: "#F59E0B",
          amberDim: "#78350F",
        },
      },
      boxShadow: {
        glowGreen: "0 0 25px -5px rgba(16, 185, 129, 0.25)",
        glowCyan: "0 0 25px -5px rgba(6, 182, 212, 0.25)",
        glowPurple: "0 0 25px -5px rgba(139, 92, 246, 0.25)",
      },
      animation: {
        "pulse-slow": "pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite",
      },
    },
  },
  plugins: [],
};

export default config;
