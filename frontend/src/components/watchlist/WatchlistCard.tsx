"use client";

import React, { useState } from "react";
import Link from "next/link";
import { WatchlistResponse } from "@/types";
import { deleteWatchlist } from "@/lib/api";
import {
  Folder,
  Trash2,
  ArrowRight,
  Loader2,
  Layers,
  AlertCircle,
} from "lucide-react";

interface WatchlistCardProps {
  watchlist: WatchlistResponse;
  onDeleted: (id: string) => void;
}

export function WatchlistCard({ watchlist, onDeleted }: WatchlistCardProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleDelete = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!showConfirm) {
      setShowConfirm(true);
      return;
    }

    setIsDeleting(true);
    try {
      await deleteWatchlist(watchlist.id);
      onDeleted(watchlist.id);
    } catch (err) {
      console.error("Failed to delete watchlist", err);
      setIsDeleting(false);
      setShowConfirm(false);
    }
  };

  const assetCount = watchlist.assetCount || watchlist.assetIds?.length || 0;
  const capacityPct = Math.min(100, Math.round((assetCount / 10) * 100));

  return (
    <div className="group relative rounded-2xl glass-panel glass-panel-hover p-5 border border-canvas-border flex flex-col justify-between transition-all duration-200 hover:shadow-xl hover:shadow-cyan-950/20">
      <div>
        {/* Header */}
        <div className="flex items-start justify-between gap-3 mb-3">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-canvas-card border border-canvas-border group-hover:border-brand-cyan/40 text-brand-cyan transition-colors">
              <Folder className="w-5 h-5" />
            </div>
            <div>
              <Link href={`/watchlists/${watchlist.id}`}>
                <h3 className="font-semibold text-base text-canvas-text group-hover:text-brand-cyan transition-colors line-clamp-1">
                  {watchlist.name}
                </h3>
              </Link>
              <span className="text-[11px] text-canvas-dim font-mono-num">
                ID: {watchlist.id.slice(0, 8)}...
              </span>
            </div>
          </div>

          {/* Delete Action */}
          <div className="relative">
            {showConfirm ? (
              <div
                className="flex items-center gap-1 bg-brand-redDim/60 border border-brand-red/40 rounded-lg p-1 animate-in fade-in"
                onClick={(e) => e.stopPropagation()}
              >
                <button
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className="px-2 py-1 text-[11px] font-bold bg-brand-red text-white rounded hover:bg-rose-600 transition-colors"
                >
                  {isDeleting ? <Loader2 className="w-3 h-3 animate-spin" /> : "Confirm"}
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setShowConfirm(false);
                  }}
                  className="px-1.5 py-1 text-[11px] text-canvas-muted hover:text-canvas-text"
                >
                  ✕
                </button>
              </div>
            ) : (
              <button
                onClick={handleDelete}
                title="Delete Watchlist"
                className="p-2 rounded-lg text-canvas-dim hover:text-brand-red hover:bg-canvas-card opacity-0 group-hover:opacity-100 transition-all"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>

        {/* Capacity Bar */}
        <div className="space-y-1.5 mt-4">
          <div className="flex items-center justify-between text-xs font-mono-num">
            <span className="text-canvas-dim">Tracked Assets</span>
            <span className="text-canvas-text font-medium">
              {assetCount} <span className="text-canvas-dim">/ 10 max</span>
            </span>
          </div>
          <div className="w-full h-1.5 rounded-full bg-canvas-card overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ${
                assetCount >= 10
                  ? "bg-brand-amber"
                  : assetCount > 0
                  ? "bg-brand-cyan"
                  : "bg-canvas-border"
              }`}
              style={{ width: `${capacityPct}%` }}
            />
          </div>
        </div>
      </div>

      {/* Footer Navigation */}
      <div className="pt-5 mt-4 border-t border-canvas-border/60 flex items-center justify-between">
        <span className="text-xs text-canvas-dim flex items-center gap-1.5">
          <Layers className="w-3.5 h-3.5" />
          {assetCount === 0 ? "Empty Portfolio" : `${assetCount} items`}
        </span>

        <Link
          href={`/watchlists/${watchlist.id}`}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand-cyan hover:text-cyan-300 transition-colors group/link"
        >
          <span>Open Watchlist</span>
          <ArrowRight className="w-3.5 h-3.5 group-hover/link:translate-x-0.5 transition-transform" />
        </Link>
      </div>
    </div>
  );
}
