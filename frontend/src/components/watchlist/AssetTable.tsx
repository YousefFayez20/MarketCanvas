"use client";

import React, { useState } from "react";
import { AssetInfo } from "@/types";
import { Sparkline } from "./Sparkline";
import { getAssetMarketMetrics } from "@/lib/mockPrices";
import { useQuotes } from "@/context/QuoteContext";
import {
  TrendingUp,
  TrendingDown,
  Trash2,
  Loader2,
  ExternalLink,
  PlusCircle,
  ShieldCheck,
} from "lucide-react";

interface AssetTableProps {
  assets: AssetInfo[];
  watchlistId: string;
  onRemoveAsset: (assetId: string) => Promise<void>;
  onOpenSearch: () => void;
}

export function AssetTable({
  assets,
  watchlistId,
  onRemoveAsset,
  onOpenSearch,
}: AssetTableProps) {
  const [removingId, setRemovingId] = useState<string | null>(null);
  const { quotes, previousPrices } = useQuotes();

  const handleRemove = async (assetId: string) => {
    setRemovingId(assetId);
    try {
      await onRemoveAsset(assetId);
    } finally {
      setRemovingId(null);
    }
  };

  if (assets.length === 0) {
    return (
      <div className="rounded-2xl glass-panel border border-canvas-border p-12 text-center flex flex-col items-center justify-center">
        <div className="w-14 h-14 rounded-2xl bg-canvas-card border border-canvas-border flex items-center justify-center text-canvas-muted mb-4">
          <PlusCircle className="w-7 h-7 text-brand-cyan" />
        </div>
        <h3 className="text-lg font-semibold text-canvas-text mb-1">No Assets in this Watchlist</h3>
        <p className="text-sm text-canvas-dim max-w-md mb-6">
          This watchlist is currently empty. Start tracking US stocks and market instruments by searching the asset registry.
        </p>
        <button
          onClick={onOpenSearch}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-cyan hover:bg-cyan-400 text-slate-950 text-sm font-semibold shadow-lg shadow-brand-cyan/20 transition-all"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Add Your First Asset</span>
        </button>
      </div>
    );
  }

  return (
    <div className="rounded-2xl glass-panel border border-canvas-border overflow-hidden shadow-xl">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-canvas-border bg-canvas-subtle/80 text-[11px] font-semibold uppercase tracking-wider text-canvas-dim">
              <th className="py-3.5 px-4">Asset / Symbol</th>
              <th className="py-3.5 px-4">Sector</th>
              <th className="py-3.5 px-4 text-right">Price</th>
              <th className="py-3.5 px-4 text-right">24h Change</th>
              <th className="py-3.5 px-4 text-center">7D Trend</th>
              <th className="py-3.5 px-4 text-right">Market Cap</th>
              <th className="py-3.5 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-canvas-border/50">
            {assets.map((asset) => {
              const metrics = getAssetMarketMetrics(asset.ticker);
              const quote = quotes[asset.ticker];
              const prevPrice = previousPrices[asset.ticker];
              const price = quote?.price ?? 0;
              const change = quote?.changePercent24h ?? 0;
              const isPositive = change >= 0;

              let flashClass = "";
              if (prevPrice !== undefined && quote) {
                if (quote.price > prevPrice) flashClass = "price-up";
                else if (quote.price < prevPrice) flashClass = "price-down";
              }

              const isRemoving = removingId === asset.id;

              return (
                <tr
                  key={asset.id}
                  className="hover:bg-canvas-card/60 transition-colors group"
                >
                  {/* Symbol & Name */}
                  <td className="py-4 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-canvas-card border border-canvas-border group-hover:border-brand-cyan/40 flex items-center justify-center font-mono-num font-bold text-sm text-brand-cyan shadow-sm transition-colors">
                        {asset.ticker}
                      </div>
                      <div className="flex flex-col">
                        <span className="font-semibold text-canvas-text group-hover:text-brand-cyan transition-colors">
                          {asset.name}
                        </span>
                        <span className="text-[11px] text-canvas-dim font-mono-num">
                          {asset.id.slice(0, 8)}...
                        </span>
                      </div>
                    </div>
                  </td>

                  {/* Sector */}
                  <td className="py-4 px-4">
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-canvas-card border border-canvas-border text-canvas-muted">
                      {asset.sector}
                    </span>
                  </td>

                  {/* Price */}
                  <td className={`py-4 px-4 text-right font-mono-num font-semibold text-canvas-text text-sm ${flashClass}`}>
                    ${price.toFixed(2)}
                  </td>

                  {/* 24h Change */}
                  <td className="py-4 px-4 text-right font-mono-num">
                    <span
                      className={`inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded-md ${
                        isPositive
                          ? "bg-brand-greenDim/40 text-brand-green border border-brand-green/30"
                          : "bg-brand-redDim/40 text-brand-red border border-brand-red/30"
                      }`}
                    >
                      {isPositive ? (
                        <TrendingUp className="w-3 h-3" />
                      ) : (
                        <TrendingDown className="w-3 h-3" />
                      )}
                      {isPositive ? "+" : ""}
                      {change}%
                    </span>
                  </td>

                  {/* 7D Trend Sparkline */}
                  <td className="py-4 px-4 text-center">
                    <div className="flex justify-center">
                      <Sparkline
                        data={metrics.sparkline}
                        isPositive={isPositive}
                        width={85}
                        height={26}
                      />
                    </div>
                  </td>

                  {/* Market Cap */}
                  <td className="py-4 px-4 text-right font-mono-num text-xs text-canvas-muted">
                    {metrics.marketCap}
                  </td>

                  {/* Remove Button */}
                  <td className="py-4 px-4 text-center">
                    <button
                      onClick={() => handleRemove(asset.id)}
                      disabled={isRemoving}
                      title="Remove from Watchlist"
                      className="p-2 rounded-lg text-canvas-dim hover:text-brand-red hover:bg-canvas-card transition-all disabled:opacity-50"
                    >
                      {isRemoving ? (
                        <Loader2 className="w-4 h-4 animate-spin text-brand-red" />
                      ) : (
                        <Trash2 className="w-4 h-4" />
                      )}
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
