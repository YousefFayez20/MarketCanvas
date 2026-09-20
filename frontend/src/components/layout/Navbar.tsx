"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { UserSwitcher } from "@/components/user/UserSwitcher";
import { EndpointTesterDrawer } from "@/components/api-inspector/EndpointTesterDrawer";
import { useUser } from "@/context/UserContext";
import {
  TrendingUp,
  Layers,
  Database,
  Terminal,
  Activity,
  Zap,
} from "lucide-react";

export function Navbar() {
  const pathname = usePathname();
  const { isBackendConnected } = useUser();
  const [isTesterOpen, setIsTesterOpen] = useState(false);

  const navLinks = [
    { name: "Watchlists", href: "/", icon: Layers },
    { name: "Market Directory", href: "/assets", icon: Database },
  ];

  return (
    <>
      <header className="sticky top-0 z-40 w-full glass-panel border-b border-canvas-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Logo & Brand */}
          <div className="flex items-center gap-8">
            <Link href="/" className="flex items-center gap-3 group">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-cyan via-brand-purple to-brand-green p-[1px] shadow-lg shadow-cyan-950/40">
                <div className="w-full h-full bg-canvas-bg rounded-[11px] flex items-center justify-center">
                  <TrendingUp className="w-5 h-5 text-brand-cyan group-hover:scale-110 transition-transform" />
                </div>
              </div>
              <div className="flex flex-col">
                <span className="font-extrabold text-base tracking-tight text-canvas-text flex items-center gap-1.5">
                  MarketCanvas
                  <span className="text-[10px] font-mono-num font-bold px-1.5 py-0.2 rounded bg-brand-cyanDim text-brand-cyan border border-brand-cyan/30">
                    MVP
                  </span>
                </span>
                <span className="text-[10px] text-canvas-dim leading-none">
                  Event-Driven Investment Platform
                </span>
              </div>
            </Link>

            {/* Navigation Links */}
            <nav className="hidden md:flex items-center gap-1">
              {navLinks.map((link) => {
                const isActive = pathname === link.href;
                const Icon = link.icon;
                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold transition-all ${
                      isActive
                        ? "bg-canvas-card text-brand-cyan border border-canvas-border shadow-sm"
                        : "text-canvas-muted hover:text-canvas-text hover:bg-canvas-card/50"
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span>{link.name}</span>
                  </Link>
                );
              })}
            </nav>
          </div>

          {/* Right Action Bar */}
          <div className="flex items-center gap-3">
            {/* Backend Health Badge */}
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-canvas-subtle border border-canvas-border text-xs">
              <span className="relative flex h-2 w-2">
                <span
                  className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${
                    isBackendConnected ? "bg-brand-green" : "bg-brand-amber"
                  }`}
                />
                <span
                  className={`relative inline-flex rounded-full h-2 w-2 ${
                    isBackendConnected ? "bg-brand-green" : "bg-brand-amber"
                  }`}
                />
              </span>
              <span className="text-canvas-dim font-mono-num text-[11px]">
                {isBackendConnected ? "Backend :8080" : "Connecting..."}
              </span>
            </div>

            {/* API Test Bench Trigger Button */}
            <button
              onClick={() => setIsTesterOpen(true)}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-canvas-subtle border border-canvas-border hover:border-brand-purple/50 text-canvas-muted hover:text-brand-purple text-xs font-medium transition-all"
              title="Open Live API Test Bench"
            >
              <Terminal className="w-3.5 h-3.5 text-brand-purple" />
              <span className="hidden sm:inline">API Inspector</span>
            </button>

            {/* Mock User Selector */}
            <UserSwitcher />
          </div>
        </div>
      </header>

      {/* Drawer */}
      <EndpointTesterDrawer
        isOpen={isTesterOpen}
        onClose={() => setIsTesterOpen(false)}
      />
    </>
  );
}
