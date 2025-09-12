import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTheme } from '../useTheme';

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

describe('useTheme Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  it('should initialize with default theme', () => {
    const { result } = renderHook(() => useTheme());

    expect(result.current.currentTheme).toBe('bubblegum');
    expect(result.current.isDarkMode).toBe(false);
  });

  it('should load theme from localStorage', () => {
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'selected-theme') return 'amethyst-haze';
      if (key === 'dark-mode') return 'true';
      return null;
    });

    const { result } = renderHook(() => useTheme());

    expect(result.current.currentTheme).toBe('amethyst-haze');
    expect(result.current.isDarkMode).toBe(true);
  });

  it('should change theme and save to localStorage', () => {
    const { result } = renderHook(() => useTheme());

    act(() => {
      result.current.changeTheme('claymorphism');
    });

    expect(result.current.currentTheme).toBe('claymorphism');
    expect(localStorageMock.setItem).toHaveBeenCalledWith('selected-theme', 'claymorphism');
  });

  it('should toggle dark mode and save to localStorage', () => {
    const { result } = renderHook(() => useTheme());

    expect(result.current.isDarkMode).toBe(false);

    act(() => {
      result.current.toggleDarkMode();
    });

    expect(result.current.isDarkMode).toBe(true);
    expect(localStorageMock.setItem).toHaveBeenCalledWith('dark-mode', 'true');

    act(() => {
      result.current.toggleDarkMode();
    });

    expect(result.current.isDarkMode).toBe(false);
    expect(localStorageMock.setItem).toHaveBeenCalledWith('dark-mode', 'false');
  });

  it('should handle invalid localStorage values gracefully', () => {
    localStorageMock.getItem.mockImplementation((key) => {
      if (key === 'selectedTheme') return 'invalid-theme';
      if (key === 'darkMode') return 'invalid-boolean';
      return null;
    });

    const { result } = renderHook(() => useTheme());

    // Should fallback to defaults
    expect(result.current.currentTheme).toBe('bubblegum');
    expect(result.current.isDarkMode).toBe(false);
  });
});