import type { Metadata } from "next";
import "./globals.css";
import { UserProvider } from "@/context/UserContext";
import { QuoteProvider } from "@/context/QuoteContext";
import { Navbar } from "@/components/layout/Navbar";

export const metadata: Metadata = {
  title: "MarketCanvas — Event-Driven Investment Platform",
  description: "Next-generation watchlist management & market data streaming terminal",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen bg-canvas-bg text-canvas-text antialiased selection:bg-brand-cyan/20 selection:text-brand-cyan">
        <UserProvider>
          <QuoteProvider>
            <div className="min-h-screen flex flex-col">
              <Navbar />
              <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {children}
              </main>
            </div>
          </QuoteProvider>
        </UserProvider>
      </body>
    </html>
  );
}
