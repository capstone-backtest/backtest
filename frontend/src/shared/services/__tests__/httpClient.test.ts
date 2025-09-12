import { describe, it, expect, beforeEach, vi } from 'vitest';
import { HttpClient } from '../httpClient';

// Mock fetch
global.fetch = vi.fn();

describe('HttpClient', () => {
  let httpClient: HttpClient;

  beforeEach(() => {
    vi.clearAllMocks();
    httpClient = new HttpClient({
      baseURL: 'http://localhost:8001'
    });
  });

  it('should make GET requests correctly', async () => {
    const mockResponse = { data: 'test' };
    (fetch as any).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    const result = await httpClient.get('/test');

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8001/test',
      expect.objectContaining({
        method: 'GET',
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
        }),
      })
    );
    expect(result).toEqual(mockResponse);
  });

  it('should make POST requests with body', async () => {
    const mockResponse = { success: true };
    const requestBody = { name: 'test' };

    (fetch as any).mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => mockResponse,
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    const result = await httpClient.post('/test', requestBody);

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8001/test',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
        }),
        body: JSON.stringify(requestBody),
      })
    );
    expect(result).toEqual(mockResponse);
  });

  it('should handle HTTP errors', async () => {
    (fetch as any).mockResolvedValueOnce({
      ok: false,
      status: 404,
      statusText: 'Not Found',
      json: async () => ({ message: 'Resource not found' }),
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    await expect(httpClient.get('/nonexistent')).rejects.toThrow('Resource not found');
  });

  it('should handle network errors', async () => {
    (fetch as any).mockRejectedValueOnce(new Error('Network error'));

    await expect(httpClient.get('/test')).rejects.toThrow('Network error');
  });

  it('should add authorization header when token exists', async () => {
    // Mock localStorage to return a token
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: vi.fn().mockReturnValue('test-token'),
      },
    });

    (fetch as any).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({}),
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    await httpClient.get('/protected');

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8001/protected',
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer test-token',
        }),
      })
    );
  });

  it('should retry failed requests', async () => {
    // First call fails, second succeeds
    (fetch as any)
      .mockRejectedValueOnce(new Error('Network error'))
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ success: true }),
      });

    const result = await httpClient.get('/retry-test');

    expect(fetch).toHaveBeenCalledTimes(2);
    expect(result).toEqual({ success: true });
  });

  it('should respect timeout', async () => {
    vi.useFakeTimers();

    (fetch as any).mockImplementation(() => 
      new Promise((resolve) => {
        setTimeout(() => resolve({
          ok: true,
          json: async () => ({}),
        }), 35000); // 35 seconds - longer than default timeout
      })
    );

    const promise = httpClient.get('/slow');

    // Fast-forward time to trigger timeout
    vi.advanceTimersByTime(30000);

    await expect(promise).rejects.toThrow();

    vi.useRealTimers();
  });

  it('should handle PUT and DELETE requests', async () => {
    const mockResponse = { updated: true };

    (fetch as any).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    const result = await httpClient.put('/test/1', { name: 'updated' });

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8001/test/1',
      expect.objectContaining({
        method: 'PUT',
      })
    );
    expect(result).toEqual(mockResponse);

    (fetch as any).mockResolvedValueOnce({
      ok: true,
      status: 204,
      json: async () => ({}),
      headers: {
        get: vi.fn().mockReturnValue('application/json')
      }
    });

    await httpClient.delete('/test/1');

    expect(fetch).toHaveBeenCalledWith(
      'http://localhost:8001/test/1',
      expect.objectContaining({
        method: 'DELETE',
      })
    );
  });
});