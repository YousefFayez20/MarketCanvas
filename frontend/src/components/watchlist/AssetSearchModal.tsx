"use client";

import React, { useState, useEffect } from "react";
import { AssetInfo } from "@/types";
import { searchAssets, addAssetToWatchlist } from "@/lib/api";
import { getAssetMarketMetrics } from "@/lib/mockPrices";
import {
  Search,
  X,
  Plus,
  Check,
  Loader2,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
  Layers,
} from "lucide-react";

interface AssetSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  watchlistId: string;
  existingAssetIds: string[];
  currentAssetCount: number;
  onAssetAdded: (asset: AssetInfo) => void;
}

export function AssetSearchModal({
  isOpen,
  onClose,
  watchlistId,
  existingAssetIds,
  currentAssetCount,
  onAssetAdded,
}: AssetSearchModalProps) {
  const [query, setQuery] = useState("");
  const [assets, setAssets] = useState<AssetInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [addingAssetId, setAddingAssetId] = useState<string | null>(null);
  const [selectedSector, setSelectedSector] = useState<string>("ALL");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isAtCapacity = currentAssetCount >= 10;

  useEffect(() => {
    if (!isOpen) return;

    const timer = setTimeout(async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const results = await searchAssets(query);
        setAssets(results);
      } catch (err: any) {
        setErrorMessage("Failed to search assets. Please check backend connection.");
      } finally {
        setLoading(false);
      }
    }, 200);

    return () => clearTimeout(timer);
  }, [query, isOpen]);

  if (!isOpen) return null;

  const sectors = ["ALL", "Technology", "Financial Services", "Healthcare", "Consumer Cyclical", "Communication Services", "Energy"];

  const filteredAssets = selectedSector === "ALL" 
    ? assets 
    : assets.filter((a) => a.sector?.toLowerCase() === selectedSector.toLowerCase());

  const handleAdd = async (asset: AssetInfo) => {
    if (isAtCapacity) return;
    setAddingAssetId(asset.id);
    setErrorMessage(null);

    try {
      await addAssetToWatchlist(watchlistId, asset.id);
      onAssetAdded(asset);
    } catch (err: any) {
      setErrorMessage(err.message || "Failed to add asset to watchlist.");
    } finally {
      setAddingAssetId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-150">
      <div className="w-full max-w-2xl rounded-2xl glass-panel border border-canvas-border shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="p-5 border-b border-canvas-border flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-canvas-text flex items-center gap-2">
              <Search className="w-5 h-5 text-brand-cyan" />
              Add Assets to Watchlist
            </h3>
            <p className="text-xs text-canvas-dim mt-0.5">
              Search from 50 verified US public equities and market assets
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-canvas-dim hover:text-canvas-text hover:bg-canvas-card transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Capacity Warning Banner */}
        {isAtCapacity && (
          <div className="px-5 py-2.5 bg-brand-amberDim/40 border-b border-brand-amber/30 flex items-center gap-2 text-xs text-brand-amber">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>
              <strong>Watchlist capacity reached (10/10 assets).</strong> Remove an existing asset before adding more.
            </span>
          </div>
        )}

        {/* Search Input & Sector Filter */}
        <div className="p-4 border-b border-canvas-border space-y-3 bg-canvas-subtle/50">
          <div className="relative">
            <Search className="w-4 h-4 text-canvas-dim absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search by ticker (AAPL, NVDA, TSLA) or company name..."
              autoFocus
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-canvas-card border border-canvas-border focus:border-brand-cyan focus:ring-1 focus:ring-brand-cyan outline-none text-sm text-canvas-text placeholder-canvas-dim transition-all"
            />
            {query && (
              <button
                onClick={() => setQuery("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-canvas-dim hover:text-canvas-text text-xs"
              >
                Clear
              </button>
            )}
          </div>

          {/* Sector Chips */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none text-xs">
            <Layers className="w-3.5 h-3.5 text-canvas-dim shrink-0 mr-1" />
            {sectors.map((sec) => (
              <button
                key={sec}
                onClick={() => setSelectedSector(sec)}
                className={`px-2.5 py-1 rounded-lg shrink-0 transition-colors ${
                  selectedSector === sec
                    ? "bg-brand-cyan text-slate-950 font-semibold"
                    : "bg-canvas-card text-canvas-muted hover:text-canvas-text hover:bg-canvas-border/60 border border-canvas-border"
                }`}
              >
                {sec}
              </button>
            ))}
          </div>
        </div>

        {/* Results List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-2 divide-y divide-canvas-border/40">
          {errorMessage && (
            <div className="p-3 rounded-xl bg-brand-redDim/30 border border-brand-red/30 text-brand-red text-xs">
              {errorMessage}
            </div>
          )}

          {loading ? (
            <div className="py-12 flex flex-col items-center justify-center text-canvas-dim space-y-2">
              <Loader2 className="w-6 h-6 animate-spin text-brand-cyan" />
              <span className="text-xs">Searching Asset Registry...</span>
            </div>
          ) : filteredAssets.length === 0 ? (
            <div className="py-12 text-center text-canvas-dim text-sm">
              No matching assets found for "{query}". Try another ticker or sector filter.
            </div>
          ) : (
            filteredAssets.map((asset) => {
              const metrics = getAssetMarketMetrics(asset.ticker);
              const isAlreadyAdded = existingAssetIds.includes(asset.id);
              const isAddingThis = addingAssetId === asset.id;
              const isPositive = metrics.change24h >= 0;

              return (
                <div
                  key={asset.id}
                  className="pt-2 pb-2 first:pt-0 flex items-center justify-between hover:bg-canvas-card/50 px-2 rounded-xl transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-10 rounded-lg bg-canvas-card border border-canvas-border flex items-center justify-center font-mono-num font-bold text-sm text-brand-cyan">
                      {asset.ticker}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-canvas-text">{asset.name}</span>
                        <span className="text-[11px] px-2 py-0.5 rounded-full bg-canvas-border/50 text-canvas-muted">
                          {asset.sector}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 text-xs font-mono-num text-canvas-dim mt-0.5">
                        <span className="text-canvas-text font-medium">${metrics.price.toFixed(2)}</span>
                        <span
                          className={`flex items-center text-[11px] font-medium ${
                            isPositive ? "text-brand-green" : "text-brand-red"
                          }`}
                        >
                          {isPositive ? <TrendingUp className="w-3 h-3 mr-0.5 inline" /> : <TrendingDown className="w-3 h-3 mr-0.5 inline" />}
                          {isPositive ? "+" : ""}
                          {metrics.change24h}%
                        </span>
                        <span className="text-[10px] text-canvas-dim">Cap: {metrics.marketCap}</span>
                      </div>
                    </div>
                  </div>

                  <div>
                    {isAlreadyAdded ? (
                      <span className="inline-flex items-center gap-1 text-xs px-3 py-1.5 rounded-lg bg-canvas-card text-brand-green font-medium border border-brand-green/30">
                        <Check className="w-3.5 h-3.5" /> Added
                      </span>
                    ) : (
                      <button
                        onClick={() => handleAdd(asset)}
                        disabled={isAtCapacity || isAddingThis}
                        className="inline-flex items-center gap-1.5 text-xs font-semibold px-3.5 py-1.5 rounded-lg bg-canvas-card border border-canvas-border hover:border-brand-cyan hover:text-brand-cyan hover:bg-brand-cyanDim/20 text-canvas-text disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                      >
                        {isAddingThis ? (
                          <Loader2 className="w-3.5 h-3.5 animate-spin text-brand-cyan" />
                        ) : (
                          <Plus className="w-3.5 h-3.5" />
                        )}
                        <span>Add</span>
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-canvas-border bg-canvas-subtle/50 flex items-center justify-between text-xs text-canvas-dim">
          <span>
            Current Watchlist: <strong className="text-canvas-text">{currentAssetCount} / 10 assets</strong>
          </span>
          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-xl bg-canvas-card hover:bg-canvas-border/80 text-canvas-text font-medium transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
}
