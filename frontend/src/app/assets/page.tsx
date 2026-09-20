"use client";

import React, { useState, useEffect } from "react";
import { AssetInfo } from "@/types";
import { getAllAssets, searchAssets } from "@/lib/api";
import { getAssetMarketMetrics } from "@/lib/mockPrices";
import { useQuotes } from "@/context/QuoteContext";
import { Sparkline } from "@/components/watchlist/Sparkline";
import {
  Database,
  Search,
  TrendingUp,
  TrendingDown,
  Layers,
  Loader2,
  Building2,
  DollarSign,
  BarChart2,
} from "lucide-react";

export default function AssetDirectoryPage() {
  const [assets, setAssets] = useState<AssetInfo[]>([]);
  const [query, setQuery] = useState("");
  const [selectedSector, setSelectedSector] = useState<string>("ALL");
  const [loading, setLoading] = useState(true);
  const { quotes, previousPrices } = useQuotes();

  useEffect(() => {
    const fetchAssets = async () => {
      setLoading(true);
      try {
        const data = query.trim() ? await searchAssets(query) : await getAllAssets();
        setAssets(data || []);
      } catch (err) {
        console.error("Failed to load asset directory", err);
      } finally {
        setLoading(false);
      }
    };

    const timer = setTimeout(fetchAssets, 200);
    return () => clearTimeout(timer);
  }, [query]);

  const sectors = [
    "ALL",
    "Technology",
    "Financial Services",
    "Healthcare",
    "Consumer Cyclical",
    "Communication Services",
    "Energy",
    "Industrials",
  ];

  const filtered = selectedSector === "ALL"
    ? assets
    : assets.filter((a) => a.sector?.toLowerCase() === selectedSector.toLowerCase());

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Header */}
      <div className="pb-2 border-b border-canvas-border/60">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-brand-cyan mb-1">
          <Database className="w-3.5 h-3.5" />
          <span>Market Data & Security Directory</span>
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-canvas-text tracking-tight">
          Public Asset Catalog
        </h1>
        <p className="text-xs text-canvas-dim mt-1">
          Browsing 50 pre-registered US equities loaded from static JSON registry with deterministic UUIDs
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-canvas-dim absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search ticker (e.g. NVDA, MSFT, JPM) or company name..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-canvas-card border border-canvas-border focus:border-brand-cyan focus:ring-1 focus:ring-brand-cyan outline-none text-sm text-canvas-text placeholder-canvas-dim transition-all"
            />
          </div>
        </div>

        {/* Sector Tabs */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none text-xs">
          {sectors.map((sec) => (
            <button
              key={sec}
              onClick={() => setSelectedSector(sec)}
              className={`px-3 py-1.5 rounded-xl shrink-0 transition-all font-medium ${
                selectedSector === sec
                  ? "bg-brand-cyan text-slate-950 font-bold shadow-md shadow-brand-cyan/20"
                  : "bg-canvas-card border border-canvas-border text-canvas-muted hover:text-canvas-text hover:bg-canvas-card/80"
              }`}
            >
              {sec}
            </button>
          ))}
        </div>
      </div>

      {/* Directory Grid */}
      {loading ? (
        <div className="py-24 rounded-2xl glass-panel border border-canvas-border flex flex-col items-center justify-center text-canvas-dim space-y-2">
          <Loader2 className="w-8 h-8 animate-spin text-brand-cyan" />
          <span className="text-xs font-medium">Filtering asset registry...</span>
        </div>
      ) : filtered.length === 0 ? (
        <div className="py-24 text-center rounded-2xl glass-panel border border-canvas-border text-canvas-dim text-sm">
          No securities matched your search criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((asset) => {
            const metrics = getAssetMarketMetrics(asset.ticker);
            const quote = quotes[asset.ticker];
            const realPrice = quote?.price;
            const realChange = quote?.changePercent24h;
            
            const displayPrice = realPrice?.toFixed(2) ?? metrics.price.toFixed(2);
            const displayChange = realChange ?? metrics.change24h;
            const isPositive = displayChange >= 0;

            let flashClass = "";
            const prevPrice = previousPrices[asset.ticker];
            if (prevPrice !== undefined && quote) {
              if (quote.price > prevPrice) flashClass = "price-up";
              else if (quote.price < prevPrice) flashClass = "price-down";
            }

            return (
              <div
                key={asset.id}
                className="rounded-2xl glass-panel glass-panel-hover p-4 border border-canvas-border flex flex-col justify-between space-y-3"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-11 h-11 rounded-xl bg-canvas-card border border-canvas-border flex items-center justify-center font-mono-num font-bold text-sm text-brand-cyan shadow-inner">
                        {asset.ticker}
                      </div>
                      <div>
                        <h3 className="font-semibold text-sm text-canvas-text line-clamp-1">
                          {asset.name}
                        </h3>
                        <span className="text-[10px] text-canvas-dim font-mono-num">
                          {asset.id.slice(0, 13)}...
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="mt-3 flex items-center justify-between">
                    <span className="text-[11px] px-2.5 py-0.5 rounded-full bg-canvas-card border border-canvas-border text-canvas-muted">
                      {asset.sector}
                    </span>
                    <span className="text-xs font-mono-num text-canvas-dim">
                      Cap: {metrics.marketCap}
                    </span>
                  </div>
                </div>

                <div className="pt-3 border-t border-canvas-border/50 flex items-end justify-between">
                  <div>
                    <span className="text-[10px] uppercase tracking-wider text-canvas-dim block font-mono-num">
                      Current Price
                    </span>
                    <div className="flex items-baseline gap-2 mt-0.5">
                      <span className={`text-lg font-bold font-mono-num text-canvas-text ${flashClass}`}>
                        ${displayPrice}
                      </span>
                      <span
                        className={`inline-flex items-center text-xs font-mono-num font-semibold ${
                          isPositive ? "text-brand-green" : "text-brand-red"
                        }`}
                      >
                        {isPositive ? "+" : ""}
                        {displayChange}%
                      </span>
                    </div>
                  </div>

                  <div className="pb-1">
                    <Sparkline
                      data={metrics.sparkline}
                      isPositive={isPositive}
                      width={80}
                      height={24}
                    />
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
