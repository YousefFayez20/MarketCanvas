"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { MockUser } from "@/types";
import { getMockUsers } from "@/lib/api";

// Deterministic fallback users if backend is still starting up
const FALLBACK_USERS: MockUser[] = [
  { id: "4e074085-62be-3c97-9c97-09d0fe44d4dd", name: "Alice" },
  { id: "9f9d51bc-70ef-3037-bc0f-1dd40e53a35a", name: "Bob" },
  { id: "e037b583-bf08-3236-8e56-658b438cf38c", name: "Carol" },
];

interface UserContextType {
  selectedUser: MockUser | null;
  setSelectedUser: (user: MockUser) => void;
  users: MockUser[];
  isLoadingUsers: boolean;
  isBackendConnected: boolean;
  refreshUsers: () => Promise<void>;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

const STORAGE_KEY = "marketcanvas_active_user_id";

export function UserProvider({ children }: { children: React.ReactNode }) {
  const [users, setUsers] = useState<MockUser[]>(FALLBACK_USERS);
  const [selectedUser, setSelectedUserState] = useState<MockUser | null>(FALLBACK_USERS[0]);
  const [isLoadingUsers, setIsLoadingUsers] = useState<boolean>(true);
  const [isBackendConnected, setIsBackendConnected] = useState<boolean>(false);

  const fetchUsers = async () => {
    setIsLoadingUsers(true);
    try {
      const fetched = await getMockUsers();
      if (fetched && fetched.length > 0) {
        setUsers(fetched);
        setIsBackendConnected(true);

        // Restore persisted user if present
        const savedId = typeof window !== "undefined" ? localStorage.getItem(STORAGE_KEY) : null;
        const matching = savedId ? fetched.find((u) => u.id === savedId) : null;
        setSelectedUserState(matching || fetched[0]);
      }
    } catch (err) {
      console.warn("Backend user service not available yet, using fallback users", err);
      setIsBackendConnected(false);
    } finally {
      setIsLoadingUsers(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const setSelectedUser = (user: MockUser) => {
    setSelectedUserState(user);
    if (typeof window !== "undefined") {
      localStorage.setItem(STORAGE_KEY, user.id);
    }
  };

  return (
    <UserContext.Provider
      value={{
        selectedUser,
        setSelectedUser,
        users,
        isLoadingUsers,
        isBackendConnected,
        refreshUsers: fetchUsers,
      }}
    >
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
}
