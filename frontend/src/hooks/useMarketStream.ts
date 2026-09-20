"use client";

import { useEffect, useRef } from "react";
import { StockQuote } from "@/types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export function useMarketStream(
  onQuoteUpdate: (quote: StockQuote) => void
) {
  const callbackRef = useRef(onQuoteUpdate);
  callbackRef.current = onQuoteUpdate;

  useEffect(() => {
    const eventSource = new EventSource(
      `${API_BASE}/api/v1/assets/stream`
    );

    eventSource.addEventListener("price-update", (event) => {
      try {
        const quote: StockQuote = JSON.parse(event.data);
        callbackRef.current(quote);
      } catch (err) {
        console.warn("Failed to parse SSE price-update event", err);
      }
    });

    eventSource.onerror = () => {
      console.warn("SSE connection error — EventSource will auto-reconnect");
    };

    return () => eventSource.close();
  }, []);
}
