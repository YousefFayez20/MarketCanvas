import {
  AssetInfo,
  CreateWatchlistRequest,
  EndpointTestResult,
  MockUser,
  WatchlistResponse,
  StockQuote,
  AnalysisRequest,
  AnalysisResponse,
  WatchlistAnalysis,
} from "@/types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiError extends Error {
  status: number;
  data: any;

  constructor(status: number, message: string, data?: any) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE}${endpoint}`;
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorData = null;
    try {
      errorData = await response.json();
    } catch {
      // response might not be json
    }
    throw new ApiError(
      response.status,
      `API request failed: ${response.status} ${response.statusText}`,
      errorData
    );
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  // For plain text / UUID string responses
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    return (await response.json()) as T;
  }

  const text = await response.text();
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T;
  }
}

// -------------------------------------------------------------
// Core Domain API Operations
// -------------------------------------------------------------

// 1. Mock Users
export async function getMockUsers(): Promise<MockUser[]> {
  return request<MockUser[]>("/api/v1/users/mock");
}

// 2. Assets & Market Data Registry
export async function getAllAssets(): Promise<AssetInfo[]> {
  return request<AssetInfo[]>("/api/v1/assets");
}

export async function getAssetById(id: string): Promise<AssetInfo> {
  return request<AssetInfo>(`/api/v1/assets/${id}`);
}

export async function searchAssets(query: string): Promise<AssetInfo[]> {
  if (!query.trim()) {
    return getAllAssets();
  }
  return request<AssetInfo[]>(`/api/v1/assets/search?q=${encodeURIComponent(query)}`);
}

// 3. Market Data Quotes
export async function getQuote(ticker: string): Promise<StockQuote> {
  return request<StockQuote>(`/api/v1/marketdata/quotes/${ticker}`);
}

export async function getQuotes(tickers: string[]): Promise<StockQuote[]> {
  const results: StockQuote[] = [];
  for (const ticker of tickers) {
    try {
      const quote = await getQuote(ticker);
      results.push(quote);
    } catch {
      // Skip tickers that fail
    }
  }
  return results;
}

// 4. Watchlists
export async function getWatchlistsByOwner(ownerId: string): Promise<WatchlistResponse[]> {
  return request<WatchlistResponse[]>(`/api/v1/watchlists?ownerId=${ownerId}`);
}

export async function getWatchlist(id: string): Promise<WatchlistResponse> {
  return request<WatchlistResponse>(`/api/v1/watchlists/${id}`);
}

export async function createWatchlist(ownerId: string, name: string): Promise<string> {
  const payload: CreateWatchlistRequest = { ownerId, name };
  const res = await request<string>("/api/v1/watchlists", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  // If response is raw UUID or JSON string
  return typeof res === "string" ? res.replace(/^"|"$/g, "") : (res as any);
}

export async function deleteWatchlist(id: string): Promise<void> {
  await request<void>(`/api/v1/watchlists/${id}`, {
    method: "DELETE",
  });
}

export async function addAssetToWatchlist(watchlistId: string, assetId: string): Promise<void> {
  await request<void>(`/api/v1/watchlists/${watchlistId}/assets`, {
    method: "POST",
    body: JSON.stringify({ assetId }),
  });
}

export async function removeAssetFromWatchlist(watchlistId: string, assetId: string): Promise<void> {
  await request<void>(`/api/v1/watchlists/${watchlistId}/assets/${assetId}`, {
    method: "DELETE",
  });
}

// 5. AI Analysis
export async function analyzeStock(payload: AnalysisRequest): Promise<AnalysisResponse> {
  return request<AnalysisResponse>("/api/v1/ai/chat", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function analyzeWatchlist(watchlistId: string): Promise<WatchlistAnalysis> {
  return request<WatchlistAnalysis>(`/api/v1/ai/watchlists/${watchlistId}/analyze`, {
    method: "POST",
  });
}

// -------------------------------------------------------------
// Live Test Bench Diagnostic Runner
// -------------------------------------------------------------

export async function executeEndpointTest(
  id: string,
  title: string,
  endpoint: string,
  method: "GET" | "POST" | "DELETE",
  payload?: any
): Promise<EndpointTestResult> {
  const startTime = performance.now();
  const url = `${API_BASE}${endpoint}`;

  try {
    const response = await fetch(url, {
      method,
      headers: {
        "Content-Type": "application/json",
      },
      body: payload ? JSON.stringify(payload) : undefined,
    });

    const durationMs = Math.round(performance.now() - startTime);
    let data: any = null;
    if (response.status !== 204) {
      const text = await response.text();
      try {
        data = JSON.parse(text);
      } catch {
        data = text;
      }
    }

    return {
      id,
      title,
      endpoint,
      method,
      status: response.status,
      durationMs,
      success: response.ok,
      requestPayload: payload,
      responseData: data,
      timestamp: new Date().toLocaleTimeString(),
    };
  } catch (err: any) {
    const durationMs = Math.round(performance.now() - startTime);
    return {
      id,
      title,
      endpoint,
      method,
      status: 0,
      durationMs,
      success: false,
      requestPayload: payload,
      error: err.message || "Network error or backend unreachable",
      timestamp: new Date().toLocaleTimeString(),
    };
  }
}
