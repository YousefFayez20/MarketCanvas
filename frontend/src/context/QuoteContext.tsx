"use client";

import React, { createContext, useContext, useState, useCallback, useRef } from "react";
import { StockQuote } from "@/types";
import { useMarketStream } from "@/hooks/useMarketStream";

interface QuoteContextType {
  quotes: Record<string, StockQuote>;
  getQuote: (ticker: string) => StockQuote | undefined;
  previousPrices: Record<string, number>;
}

const QuoteContext = createContext<QuoteContextType>({
  quotes: {},
  getQuote: () => undefined,
  previousPrices: {},
});

export function QuoteProvider({ children }: { children: React.ReactNode }) {
  const [quotes, setQuotes] = useState<Record<string, StockQuote>>({});
  const previousPricesRef = useRef<Record<string, number>>({});
  const [previousPrices, setPreviousPrices] = useState<Record<string, number>>({});

  const handleQuoteUpdate = useCallback((quote: StockQuote) => {
    setQuotes((prev) => {
      const oldQuote = prev[quote.ticker];
      if (oldQuote) {
        previousPricesRef.current[quote.ticker] = oldQuote.price;
        setPreviousPrices({ ...previousPricesRef.current });
      }
      return { ...prev, [quote.ticker]: quote };
    });
  }, []);

  useMarketStream(handleQuoteUpdate);

  const getQuoteForTicker = useCallback(
    (ticker: string) => quotes[ticker],
    [quotes]
  );

  return (
    <QuoteContext.Provider value={{ quotes, getQuote: getQuoteForTicker, previousPrices }}>
      {children}
    </QuoteContext.Provider>
  );
}

export function useQuotes() {
  return useContext(QuoteContext);
}
