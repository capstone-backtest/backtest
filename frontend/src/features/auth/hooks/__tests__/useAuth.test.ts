import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useAuth } from '../useAuth';

// Mock the auth service
vi.mock('../../services', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  getAuthToken: vi.fn(),
}));

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('useAuth Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  it('should initialize with no user when no token is stored', () => {
    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toBeNull();
  });

  it('should set user when user data exists in localStorage', () => {
    const userData = JSON.stringify({ id: 1, username: 'testuser', email: 'test@example.com' });
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'auth_user') return userData;
      return null;
    });

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toEqual({
      id: 1,
      username: 'testuser',
      email: 'test@example.com'
    });
  });

  it('should handle logout correctly', async () => {
    const userData = JSON.stringify({ id: 1, username: 'testuser', email: 'test@example.com' });
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'auth_user') return userData;
      return null;
    });

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).not.toBeNull();

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(localStorageMock.removeItem).toHaveBeenCalledWith('auth_token');
  });

  it('should handle invalid token gracefully', () => {
    localStorageMock.getItem.mockReturnValue('invalid-token');

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toBeNull();
  });

  it('should handle invalid user data gracefully', () => {
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'auth_user') return 'invalid-json';
      return null;
    });

    const { result } = renderHook(() => useAuth());

    expect(result.current.user).toBeNull();
  });
});