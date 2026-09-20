export interface MockUser {
  id: string;
  name: string;
}

export interface AssetInfo {
  id: string;
  ticker: string;
  name: string;
  sector: string;
  price?: number;
  change24h?: number;
  sparkline?: number[];
}

export interface WatchlistResponse {
  id: string;
  ownerId: string;
  name: string;
  assetIds: string[];
  assetCount: number;
}

export interface CreateWatchlistRequest {
  ownerId: string;
  name: string;
}

export interface AddAssetRequest {
  assetId: string;
}

export interface EndpointTestResult {
  id: string;
  title: string;
  endpoint: string;
  method: "GET" | "POST" | "DELETE";
  status?: number;
  durationMs?: number;
  success?: boolean;
  requestPayload?: any;
  responseData?: any;
  error?: string;
  timestamp?: string;
}

export interface StockQuote {
  ticker: string;
  price: number;
  change24h: number | null;
  changePercent24h: number | null;
  high24h: number | null;
  low24h: number | null;
  openPrice: number | null;
  previousClose: number | null;
  volume: number | null;
  lastUpdated: string;
  providerSource: string;
}
