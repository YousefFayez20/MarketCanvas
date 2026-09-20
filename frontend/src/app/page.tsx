"use client";

import React, { useState, useEffect } from "react";
import { useUser } from "@/context/UserContext";
import { WatchlistResponse } from "@/types";
import { getWatchlistsByOwner } from "@/lib/api";
import { WatchlistCard } from "@/components/watchlist/WatchlistCard";
import { CreateWatchlistModal } from "@/components/watchlist/CreateWatchlistModal";
import {
  PlusCircle,
  Layers,
  Activity,
  TrendingUp,
  FolderPlus,
  Loader2,
  Database,
  Cpu,
  CheckCircle2,
} from "lucide-react";

export default function DashboardPage() {
  const { selectedUser, isBackendConnected } = useUser();
  const [watchlists, setWatchlists] = useState<WatchlistResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  const fetchWatchlists = async () => {
    if (!selectedUser) return;
    setLoading(true);
    try {
      const data = await getWatchlistsByOwner(selectedUser.id);
      setWatchlists(data || []);
    } catch (err) {
      console.error("Failed to load watchlists", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWatchlists();
  }, [selectedUser]);

  const handleDeleted = (deletedId: string) => {
    setWatchlists((prev) => prev.filter((w) => w.id !== deletedId));
  };

  const handleCreated = (newId: string) => {
    fetchWatchlists();
  };

  const totalAssetsCount = watchlists.reduce(
    (acc, curr) => acc + (curr.assetCount || curr.assetIds?.length || 0),
    0
  );

  return (
    <div className="space-y-8 animate-in fade-in duration-200">
      {/* Welcome & System Stats Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-2 border-b border-canvas-border/60">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-brand-cyan mb-1">
            <Activity className="w-3.5 h-3.5" />
            <span>Investment Terminal & Watchlists</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-canvas-text tracking-tight">
            Portfolio Hub — {selectedUser?.name || "Mock User"}
          </h1>
          <p className="text-xs text-canvas-dim font-mono-num mt-1">
            User ID: {selectedUser?.id}
          </p>
        </div>

        {/* Action button */}
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-brand-cyan hover:bg-cyan-400 text-slate-950 text-sm font-bold shadow-lg shadow-brand-cyan/20 transition-all self-start md:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Create Watchlist</span>
        </button>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="rounded-2xl glass-panel p-5 border border-canvas-border">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-canvas-dim">
              Active Watchlists
            </span>
            <div className="p-2 rounded-xl bg-canvas-card border border-canvas-border text-brand-cyan">
              <Layers className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold font-mono-num text-canvas-text">
              {watchlists.length}
            </span>
            <span className="text-xs text-canvas-dim">portfolios</span>
          </div>
        </div>

        <div className="rounded-2xl glass-panel p-5 border border-canvas-border">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-canvas-dim">
              Total Tracked Assets
            </span>
            <div className="p-2 rounded-xl bg-canvas-card border border-canvas-border text-brand-green">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold font-mono-num text-canvas-text">
              {totalAssetsCount}
            </span>
            <span className="text-xs text-canvas-dim">stocks / securities</span>
          </div>
        </div>

        <div className="rounded-2xl glass-panel p-5 border border-canvas-border">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-canvas-dim">
              Event Pipeline
            </span>
            <div className="p-2 rounded-xl bg-canvas-card border border-canvas-border text-brand-purple">
              <Cpu className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-center gap-2">
            <span className="text-sm font-bold text-canvas-text">
              Outbox + Kafka
            </span>
            <span className="inline-flex items-center gap-1 text-[10px] font-semibold px-2 py-0.5 rounded-full bg-brand-greenDim/50 text-brand-green border border-brand-green/30 font-mono-num">
              <CheckCircle2 className="w-3 h-3" /> KRaft Live
            </span>
          </div>
          <span className="text-[11px] text-canvas-dim block mt-1">
            Idempotent consumer recording to PostgreSQL
          </span>
        </div>
      </div>

      {/* Watchlist Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-canvas-text flex items-center gap-2">
            <FolderPlus className="w-4 h-4 text-brand-cyan" />
            {selectedUser?.name}'s Portfolios
          </h2>
          <span className="text-xs text-canvas-dim">
            Limit: Max 10 assets per watchlist aggregate
          </span>
        </div>

        {loading ? (
          <div className="py-16 rounded-2xl glass-panel border border-canvas-border flex flex-col items-center justify-center text-canvas-dim space-y-2">
            <Loader2 className="w-7 h-7 animate-spin text-brand-cyan" />
            <span className="text-xs font-medium">Loading user watchlists...</span>
          </div>
        ) : watchlists.length === 0 ? (
          <div className="py-16 rounded-2xl glass-panel border border-canvas-border flex flex-col items-center justify-center text-center p-6">
            <div className="w-14 h-14 rounded-2xl bg-canvas-card border border-canvas-border flex items-center justify-center text-canvas-dim mb-4">
              <Layers className="w-7 h-7 text-brand-cyan" />
            </div>
            <h3 className="text-base font-semibold text-canvas-text mb-1">
              No Watchlists Found
            </h3>
            <p className="text-xs text-canvas-dim max-w-sm mb-5">
              {selectedUser?.name} does not have any active watchlists yet. Create one now to start tracking US equities and market assets.
            </p>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-canvas-card border border-canvas-border hover:border-brand-cyan text-brand-cyan text-xs font-bold transition-all"
            >
              <PlusCircle className="w-4 h-4" />
              <span>Create Watchlist for {selectedUser?.name}</span>
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {watchlists.map((wl) => (
              <WatchlistCard
                key={wl.id}
                watchlist={wl}
                onDeleted={handleDeleted}
              />
            ))}
          </div>
        )}
      </div>

      {/* Create Modal */}
      <CreateWatchlistModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreated}
      />
    </div>
  );
}
