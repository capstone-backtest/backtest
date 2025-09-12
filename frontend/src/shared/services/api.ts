/**
 * API Service Layer
 * 
 * This module provides a high-level API service that wraps the httpClient
 * for common API operations with proper error handling and response processing.
 */

import { httpClient } from './httpClient';
import type { ApiError } from '../../types';

export interface ApiRequestConfig {
  timeout?: number;
  retries?: number;
  headers?: Record<string, string>;
}

/**
 * Main API service class
 */
export class ApiService {
  private baseUrl: string;
  
  constructor(baseUrl: string = '/api/v1') {
    this.baseUrl = baseUrl;
  }

  /**
   * GET request wrapper
   */
  async get<T>(endpoint: string, _config?: ApiRequestConfig): Promise<T> {
    try {
      const response = await httpClient.get(`${this.baseUrl}${endpoint}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * POST request wrapper
   */
  async post<T>(endpoint: string, data?: any, _config?: ApiRequestConfig): Promise<T> {
    try {
      const response = await httpClient.post(`${this.baseUrl}${endpoint}`, data);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * PUT request wrapper
   */
  async put<T>(endpoint: string, data?: any, _config?: ApiRequestConfig): Promise<T> {
    try {
      const response = await httpClient.put(`${this.baseUrl}${endpoint}`, data);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * DELETE request wrapper
   */
  async delete<T>(endpoint: string, _config?: ApiRequestConfig): Promise<T> {
    try {
      const response = await httpClient.delete(`${this.baseUrl}${endpoint}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Handle and transform errors
   */
  private handleError(error: any): ApiError {
    if (error.name === 'ApiError') {
      return error;
    }

    // Transform generic errors to ApiError format
    return {
      name: 'ApiError',
      message: error.message || 'An unexpected error occurred',
      code: 'GENERIC_ERROR',
      status: error.status || 500,
      details: error.details || {}
    } as ApiError;
  }
}

// Default API service instance
export const apiService = new ApiService();

// Export for testing
export { ApiService as ApiServiceClass };