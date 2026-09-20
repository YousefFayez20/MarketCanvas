"use client";

import React, { useState, useRef, useEffect } from "react";
import { useUser } from "@/context/UserContext";
import { User, ChevronDown, Check } from "lucide-react";

export function UserSwitcher() {
  const { selectedUser, setSelectedUser, users, isLoadingUsers } = useUser();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const getAvatarColor = (name: string) => {
    switch (name.toLowerCase()) {
      case "alice":
        return "bg-purple-600 text-purple-100 border-purple-400";
      case "bob":
        return "bg-emerald-600 text-emerald-100 border-emerald-400";
      case "carol":
        return "bg-cyan-600 text-cyan-100 border-cyan-400";
      default:
        return "bg-slate-600 text-slate-100 border-slate-400";
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        disabled={isLoadingUsers}
        className="flex items-center gap-2.5 px-3 py-1.5 rounded-lg bg-canvas-subtle border border-canvas-border hover:border-canvas-borderHover transition-all text-sm"
      >
        <div
          className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold border ${
            selectedUser ? getAvatarColor(selectedUser.name) : "bg-slate-700"
          }`}
        >
          {selectedUser ? selectedUser.name.charAt(0).toUpperCase() : <User className="w-3.5 h-3.5" />}
        </div>
        <div className="text-left flex flex-col">
          <span className="font-medium text-canvas-text leading-tight">
            {selectedUser ? selectedUser.name : "Select User"}
          </span>
          <span className="text-[10px] text-canvas-dim leading-none">Mock Profile</span>
        </div>
        <ChevronDown className="w-3.5 h-3.5 text-canvas-muted ml-1" />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 rounded-xl glass-panel border border-canvas-border shadow-2xl z-50 py-1.5 overflow-hidden animate-in fade-in zoom-in-95 duration-100">
          <div className="px-3 py-1.5 border-b border-canvas-border text-[11px] font-semibold uppercase tracking-wider text-canvas-dim">
            Switch Mock Identity
          </div>
          <div className="py-1">
            {users.map((user) => {
              const isSelected = selectedUser?.id === user.id;
              return (
                <button
                  key={user.id}
                  onClick={() => {
                    setSelectedUser(user);
                    setIsOpen(false);
                  }}
                  className={`w-full flex items-center justify-between px-3 py-2 text-left hover:bg-canvas-card/80 transition-colors ${
                    isSelected ? "bg-canvas-card text-brand-cyan" : "text-canvas-text"
                  }`}
                >
                  <div className="flex items-center gap-2.5 overflow-hidden">
                    <div
                      className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold border ${getAvatarColor(
                        user.name
                      )}`}
                    >
                      {user.name.charAt(0).toUpperCase()}
                    </div>
                    <div className="flex flex-col truncate">
                      <span className="text-sm font-medium">{user.name}</span>
                      <span className="text-[10px] text-canvas-dim font-mono-num truncate">{user.id}</span>
                    </div>
                  </div>
                  {isSelected && <Check className="w-4 h-4 text-brand-cyan shrink-0 ml-2" />}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
