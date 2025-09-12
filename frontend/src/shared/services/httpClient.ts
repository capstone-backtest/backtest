/**
 * 공통 HTTP 클라이언트
 * 모든 API 호출의 기본이 되는 HTTP 클라이언트
 */

import { ApiError, ApiResponse, HttpMethod } from '../../types/shared';

export interface RequestConfig {
  method: HttpMethod;
  headers?: Record<string, string>;
  body?: any;
  timeout?: number;
  retries?: number;
  signal?: AbortSignal;
}

export interface HttpClientConfig {
  baseURL?: string;
  defaultHeaders?: Record<string, string>;
  timeout?: number;
  retries?: number;
  interceptors?: {
    request?: (config: RequestConfig) => RequestConfig | Promise<RequestConfig>;
    response?: (response: Response) => Response | Promise<Response>;
    error?: (error: ApiError) => ApiError | Promise<ApiError>;
  };
}

export class HttpClient {
  private config: HttpClientConfig;

  constructor(config: HttpClientConfig = {}) {
    this.config = {
      baseURL: '',
      defaultHeaders: {
        'Content-Type': 'application/json',
      },
      timeout: 30000,
      retries: 3,
      ...config,
    };

    // 기본 baseURL 설정
    if (!this.config.baseURL) {
      const envBase = (import.meta as any)?.env?.VITE_API_BASE_URL as string | undefined;
      this.config.baseURL = envBase?.replace(/\/$/, '') || '';
    }
  }

  private createApiError(error: any, status: number = 500): ApiError {
    if (error.name === 'AbortError') {
      return {
        code: 'REQUEST_TIMEOUT',
        message: '요청 시간이 초과되었습니다.',
        details: { timeout: this.config.timeout },
      };
    }

    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      return {
        code: 'NETWORK_ERROR',
        message: '네트워크 연결에 문제가 발생했습니다.',
        details: { originalError: error.message },
      };
    }

    return {
      code: 'UNKNOWN_ERROR',
      message: error.message || '알 수 없는 오류가 발생했습니다.',
      details: { status, originalError: error },
    };
  }

  private async handleResponse(response: Response): Promise<any> {
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}`;
      let errorCode = `HTTP_${response.status}`;
      let details: Record<string, any> = {
        status: response.status,
        statusText: response.statusText,
      };

      try {
        const errorData = await response.json();
        errorMessage = errorData.detail || errorData.message || errorMessage;
        errorCode = errorData.code || errorCode;
        details = { ...details, ...errorData };
      } catch (parseError) {
        // JSON 파싱 실패 시 기본 메시지 사용
        console.warn('Error parsing response:', parseError);
      }

      const apiError: ApiError = {
        code: errorCode,
        message: errorMessage,
        details,
      };

      // 인터셉터가 있으면 적용
      if (this.config.interceptors?.error) {
        throw await this.config.interceptors.error(apiError);
      }

      throw apiError;
    }

    // 응답 인터셉터 적용
    const processedResponse = this.config.interceptors?.response
      ? await this.config.interceptors.response(response)
      : response;

    // JSON 응답 파싱
    const contentType = processedResponse.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await processedResponse.json();
    }

    return await processedResponse.text();
  }

  private async executeRequest(
    url: string, 
    config: RequestConfig
  ): Promise<any> {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      // 요청 인터셉터 적용
      const processedConfig = this.config.interceptors?.request
        ? await this.config.interceptors.request(config)
        : config;

      const fullUrl = url.startsWith('http') 
        ? url 
        : `${this.config.baseURL}${url}`;

      const requestInit: RequestInit = {
        method: processedConfig.method,
        headers: {
          ...this.config.defaultHeaders,
          ...processedConfig.headers,
        },
        signal: processedConfig.signal || controller.signal,
      };

      // Body 설정 (GET/HEAD가 아닌 경우만)
      if (processedConfig.body && !['GET', 'HEAD'].includes(processedConfig.method)) {
        requestInit.body = typeof processedConfig.body === 'string'
          ? processedConfig.body
          : JSON.stringify(processedConfig.body);
      }

      const response = await fetch(fullUrl, requestInit);
      clearTimeout(timeoutId);

      return await this.handleResponse(response);
    } catch (error) {
      clearTimeout(timeoutId);
      throw this.createApiError(error, 0);
    }
  }

  private async retryRequest<T>(
    url: string,
    config: RequestConfig,
    attempt: number = 1
  ): Promise<T> {
    try {
      return await this.executeRequest(url, config);
    } catch (error) {
      const maxRetries = config.retries ?? this.config.retries ?? 3;
      
      // 에러가 ApiError 타입인지 확인
      const apiError = error as ApiError;
      if (attempt < maxRetries && this.shouldRetry(apiError)) {
        const delay = Math.pow(2, attempt) * 1000; // Exponential backoff
        await new Promise(resolve => setTimeout(resolve, delay));
        return this.retryRequest<T>(url, config, attempt + 1);
      }
      
      throw error;
    }
  }

  private shouldRetry(error: ApiError): boolean {
    // 네트워크 오류나 서버 오류의 경우만 재시도
    return (
      error.code === 'NETWORK_ERROR' ||
      error.code === 'REQUEST_TIMEOUT' ||
      (error.details?.status >= 500 && error.details?.status < 600)
    );
  }

  // Public API methods
  
  async get<T = any>(url: string, config: Partial<RequestConfig> = {}): Promise<T> {
    return this.retryRequest<T>(url, {
      method: 'GET',
      ...config,
    });
  }

  async post<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
    return this.retryRequest<T>(url, {
      method: 'POST',
      body: data,
      ...config,
    });
  }

  async put<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
    return this.retryRequest<T>(url, {
      method: 'PUT',
      body: data,
      ...config,
    });
  }

  async patch<T = any>(url: string, data?: any, config: Partial<RequestConfig> = {}): Promise<T> {
    return this.retryRequest<T>(url, {
      method: 'PATCH',
      body: data,
      ...config,
    });
  }

  async delete<T = any>(url: string, config: Partial<RequestConfig> = {}): Promise<T> {
    return this.retryRequest<T>(url, {
      method: 'DELETE',
      ...config,
    });
  }

  // 헬퍼 메서드
  
  setAuthToken(token: string) {
    this.config.defaultHeaders = {
      ...this.config.defaultHeaders,
      'Authorization': `Bearer ${token}`,
    };
  }

  removeAuthToken() {
    if (this.config.defaultHeaders?.Authorization) {
      delete this.config.defaultHeaders.Authorization;
    }
  }

  setBaseURL(baseURL: string) {
    this.config.baseURL = baseURL.replace(/\/$/, '');
  }

  getConfig(): Readonly<HttpClientConfig> {
    return { ...this.config };
  }
}

// 기본 HTTP 클라이언트 인스턴스 생성
export const httpClient = new HttpClient();

// API 응답을 표준화하는 헬퍼 함수
export function createApiResponse<T>(data: T, success: boolean = true, message?: string): ApiResponse<T> {
  return {
    success,
    data,
    message,
    timestamp: new Date().toISOString(),
  };
}

// 에러 응답을 생성하는 헬퍼 함수
export function createErrorResponse(error: ApiError): ApiResponse<null> {
  return {
    success: false,
    data: null,
    error,
    timestamp: new Date().toISOString(),
  };
}