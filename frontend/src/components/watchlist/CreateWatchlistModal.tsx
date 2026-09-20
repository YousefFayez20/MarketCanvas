"use client";

import React, { useState } from "react";
import { useUser } from "@/context/UserContext";
import { createWatchlist } from "@/lib/api";
import { X, PlusCircle, Sparkles, Loader2, FolderPlus } from "lucide-react";

interface CreateWatchlistModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newWatchlistId: string) => void;
}

export function CreateWatchlistModal({
  isOpen,
  onClose,
  onSuccess,
}: CreateWatchlistModalProps) {
  const { selectedUser } = useUser();
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError("Please enter a watchlist name.");
      return;
    }
    if (!selectedUser) {
      setError("No mock user selected.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const newId = await createWatchlist(selectedUser.id, name.trim());
      setName("");
      onSuccess(newId);
      onClose();
    } catch (err: any) {
      setError(err.message || "Failed to create watchlist.");
    } finally {
      setLoading(false);
    }
  };

  const presetSuggestions = [
    "Tech Titans",
    "AI & Cloud Innovators",
    "Blue Chip Dividend",
    "Semiconductor Powerhouses",
    "Fintech & Crypto Ecosystem",
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-md rounded-2xl glass-panel border border-canvas-border shadow-2xl p-6 relative overflow-hidden">
        {/* Glow accent */}
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-brand-cyan via-brand-purple to-brand-green" />

        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-brand-cyanDim text-brand-cyan border border-brand-cyan/30">
              <FolderPlus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-canvas-text">Create New Watchlist</h3>
              <p className="text-xs text-canvas-dim">
                Assigning to owner: <span className="text-canvas-muted font-medium">{selectedUser?.name}</span>
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-canvas-dim hover:text-canvas-text hover:bg-canvas-card transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-canvas-muted mb-1.5">
              Watchlist Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Magnificent Seven"
              autoFocus
              className="w-full px-3.5 py-2.5 rounded-xl bg-canvas-card border border-canvas-border focus:border-brand-cyan focus:ring-1 focus:ring-brand-cyan outline-none text-sm text-canvas-text placeholder-canvas-dim transition-all"
            />
            {error && <p className="mt-1.5 text-xs text-brand-red font-medium">{error}</p>}
          </div>

          <div>
            <div className="flex items-center gap-1.5 mb-2 text-xs text-canvas-dim">
              <Sparkles className="w-3.5 h-3.5 text-brand-purple" />
              <span>Quick suggestions:</span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {presetSuggestions.map((preset) => (
                <button
                  key={preset}
                  type="button"
                  onClick={() => setName(preset)}
                  className="text-xs px-2.5 py-1 rounded-lg bg-canvas-card/70 border border-canvas-border hover:border-brand-purple/50 text-canvas-muted hover:text-canvas-text transition-colors"
                >
                  {preset}
                </button>
              ))}
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-canvas-border">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-sm font-medium text-canvas-muted hover:text-canvas-text hover:bg-canvas-card transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !name.trim()}
              className="flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-semibold bg-brand-cyan hover:bg-cyan-400 text-slate-950 shadow-lg shadow-brand-cyan/20 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Creating...</span>
                </>
              ) : (
                <>
                  <PlusCircle className="w-4 h-4" />
                  <span>Create Watchlist</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
