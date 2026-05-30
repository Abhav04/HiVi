import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  clearUser,
  getUser,
  isLoggedIn,
  reconcileAuthStorage,
  saveUser as persistUser,
  setToken,
} from '../utils/auth';

const AUTH_CHANGE_EVENT = 'hivi-auth-change';

export function notifyAuthChange() {
  window.dispatchEvent(new Event(AUTH_CHANGE_EVENT));
}

function readAuthState() {
  reconcileAuthStorage();
  const user = getUser();
  const authenticated = isLoggedIn();
  return { user: authenticated ? user : null, isAuthenticated: authenticated };
}

const AuthContext = createContext({
  ready: true,
  user: null,
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
  updateUser: () => {},
});

export function AuthProvider({ children }) {
  const [state, setState] = useState(() => ({
    ready: false,
    user: null,
    isAuthenticated: false,
  }));

  const sync = useCallback(() => {
    setState({ ready: true, ...readAuthState() });
  }, []);

  useEffect(() => {
    sync();
    const onStorage = (e) => {
      if (e.key === 'token' || e.key === 'hivi_user' || e.key === null) {
        sync();
      }
    };
    window.addEventListener(AUTH_CHANGE_EVENT, sync);
    window.addEventListener('storage', onStorage);
    return () => {
      window.removeEventListener(AUTH_CHANGE_EVENT, sync);
      window.removeEventListener('storage', onStorage);
    };
  }, [sync]);

  const login = useCallback((user, token) => {
    if (token) {
      setToken(token);
    }
    if (user) {
      persistUser(user);
    }
    notifyAuthChange();
  }, []);

  const logout = useCallback(() => {
    clearUser();
    notifyAuthChange();
  }, []);

  const updateUser = useCallback((user) => {
    persistUser(user);
    notifyAuthChange();
  }, []);

  const value = useMemo(
    () => ({
      ready: state.ready,
      user: state.user,
      isAuthenticated: state.isAuthenticated,
      login,
      logout,
      updateUser,
    }),
    [state, login, logout, updateUser]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
