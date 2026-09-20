"use client";

import React, { useState, useEffect, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { AssetInfo, WatchlistResponse } from "@/types";
import {
  getWatchlist,
  getAssetById,
  getAllAssets,
  removeAssetFromWatchlist,
  deleteWatchlist,
} from "@/lib/api";
import { AssetTable } from "@/components/watchlist/AssetTable";
import { AssetSearchModal } from "@/components/watchlist/AssetSearchModal";
import {
  ArrowLeft,
  PlusCircle,
  Trash2,
  Layers,
  Loader2,
  Folder,
  AlertTriangle,
  Sparkles,
} from "lucide-react";

export default function WatchlistDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params?.id as string;

  const [watchlist, setWatchlist] = useState<WatchlistResponse | null>(null);
  const [assets, setAssets] = useState<AssetInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const loadData = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const wl = await getWatchlist(id);
      setWatchlist(wl);

      if (wl.assetIds && wl.assetIds.length > 0) {
        // Fetch asset details in parallel
        const assetPromises = wl.assetIds.map(async (assetId) => {
          try {
            return await getAssetById(assetId);
          } catch {
            // Fallback placeholder if asset lookup fails
            return {
              id: assetId,
              ticker: "UNKNOWN",
              name: "Asset Not Found",
              sector: "General",
            } as AssetInfo;
          }
        });
        const resolvedAssets = await Promise.all(assetPromises);
        setAssets(resolvedAssets);
      } else {
        setAssets([]);
      }
    } catch (err) {
      console.error("Failed to load watchlist details", err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleRemoveAsset = async (assetId: string) => {
    if (!watchlist) return;
    await removeAssetFromWatchlist(watchlist.id, assetId);
    setAssets((prev) => prev.filter((a) => a.id !== assetId));
    setWatchlist((prev) =>
      prev
        ? {
            ...prev,
            assetIds: prev.assetIds.filter((aid) => aid !== assetId),
            assetCount: Math.max(0, (prev.assetCount || prev.assetIds.length) - 1),
          }
        : null
    );
  };

  const handleAssetAdded = (newAsset: AssetInfo) => {
    setAssets((prev) => [...prev, newAsset]);
    setWatchlist((prev) =>
      prev
        ? {
            ...prev,
            assetIds: [...(prev.assetIds || []), newAsset.id],
            assetCount: (prev.assetCount || prev.assetIds.length) + 1,
          }
        : null
    );
  };

  const handleDeleteWatchlist = async () => {
    if (!watchlist) return;
    if (!confirm(`Are you sure you want to delete "${watchlist.name}"?`)) return;

    setIsDeleting(true);
    try {
      await deleteWatchlist(watchlist.id);
      router.push("/");
    } catch (err) {
      console.error("Failed to delete watchlist", err);
      setIsDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="py-24 flex flex-col items-center justify-center text-canvas-dim space-y-3">
        <Loader2 className="w-8 h-8 animate-spin text-brand-cyan" />
        <span className="text-sm font-medium">Loading watchlist data...</span>
      </div>
    );
  }

  if (!watchlist) {
    return (
      <div className="py-24 text-center">
        <h2 className="text-xl font-bold text-canvas-text mb-2">Watchlist Not Found</h2>
        <p className="text-xs text-canvas-dim mb-6">The requested watchlist does not exist or has been removed.</p>
        <Link
          href="/"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-canvas-card border border-canvas-border text-brand-cyan text-xs font-bold"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Return to Dashboard</span>
        </Link>
      </div>
    );
  }

  const assetCount = assets.length;
  const isFull = assetCount >= 10;

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* Navigation Breadcrumb */}
      <div>
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs text-canvas-dim hover:text-brand-cyan transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Portfolios</span>
        </Link>
      </div>

      {/* Header Banner */}
      <div className="rounded-2xl glass-panel border border-canvas-border p-6 shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-start gap-4">
            <div className="p-3 rounded-2xl bg-canvas-card border border-canvas-border text-brand-cyan shadow-md">
              <Folder className="w-7 h-7" />
            </div>
            <div>
              <div className="flex items-center gap-2.5">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-canvas-text tracking-tight">
                  {watchlist.name}
                </h1>
                <span className="text-[11px] font-mono-num px-2.5 py-0.5 rounded-full bg-brand-cyanDim text-brand-cyan border border-brand-cyan/30">
                  {assetCount}/10 Assets
                </span>
              </div>
              <p className="text-xs text-canvas-dim font-mono-num mt-1">
                Watchlist UUID: {watchlist.id}
              </p>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-3">
            <button
              onClick={handleDeleteWatchlist}
              disabled={isDeleting}
              className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-canvas-card border border-canvas-border hover:border-brand-red hover:text-brand-red text-canvas-muted text-xs font-semibold transition-all disabled:opacity-50"
            >
              {isDeleting ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin text-brand-red" />
              ) : (
                <Trash2 className="w-3.5 h-3.5" />
              )}
              <span>Delete</span>
            </button>

            <button
              onClick={() => setIsSearchOpen(true)}
              disabled={isFull}
              className="flex items-center gap-2 px-5 py-2 rounded-xl bg-brand-cyan hover:bg-cyan-400 text-slate-950 text-xs font-bold shadow-lg shadow-brand-cyan/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Add Assets</span>
            </button>
          </div>
        </div>

        {/* Capacity Indicator */}
        <div className="mt-6 pt-4 border-t border-canvas-border/60">
          <div className="flex items-center justify-between text-xs font-mono-num mb-1.5">
            <span className="text-canvas-dim flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5 text-brand-cyan" />
              Portfolio Capacity
            </span>
            <span className="text-canvas-text font-medium">
              {assetCount} <span className="text-canvas-dim">of 10 maximum (Domain Invariant)</span>
            </span>
          </div>
          <div className="w-full h-2 rounded-full bg-canvas-card overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ${
                isFull ? "bg-brand-amber" : "bg-gradient-to-r from-brand-cyan to-brand-green"
              }`}
              style={{ width: `${(assetCount / 10) * 100}%` }}
            />
          </div>
        </div>
      </div>

      {/* Assets Table */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-canvas-text">
            Tracked Assets & Market Metrics
          </h2>
          <span className="text-xs text-canvas-dim">
            Real US Tickers • Synchronized via REST & Kafka
          </span>
        </div>

        <AssetTable
          assets={assets}
          watchlistId={watchlist.id}
          onRemoveAsset={handleRemoveAsset}
          onOpenSearch={() => setIsSearchOpen(true)}
        />
      </div>

      {/* Asset Search Modal */}
      <AssetSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        watchlistId={watchlist.id}
        existingAssetIds={assets.map((a) => a.id)}
        currentAssetCount={assets.length}
        onAssetAdded={handleAssetAdded}
      />
    </div>
  );
}
