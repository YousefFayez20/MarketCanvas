// Helper to generate consistent, realistic financial telemetry for demo/dashboard visualization
export function getAssetMarketMetrics(ticker: string): {
  price: number;
  change24h: number;
  sparkline: number[];
  high24h: number;
  low24h: number;
  marketCap: string;
} {
  // Deterministic seed based on ticker characters
  let seed = 0;
  for (let i = 0; i < ticker.length; i++) {
    seed = (seed << 5) - seed + ticker.charCodeAt(i);
    seed |= 0;
  }
  const absSeed = Math.abs(seed);

  // Base price between $45 and $480
  const basePrice = 45 + (absSeed % 435) + ((absSeed % 99) / 100);
  
  // 24h change between -4.5% and +6.5%
  const change24h = Number((((absSeed % 110) - 45) / 10).toFixed(2));
  
  // 7-day sparkline
  const sparkline: number[] = [];
  let curr = basePrice * (1 - change24h / 100);
  sparkline.push(Number(curr.toFixed(2)));
  for (let i = 1; i <= 6; i++) {
    const variation = (((absSeed * (i + 1)) % 21) - 10) / 100;
    curr = curr * (1 + variation);
    sparkline.push(Number(curr.toFixed(2)));
  }
  sparkline.push(Number(basePrice.toFixed(2)));

  const high24h = Number((basePrice * 1.025).toFixed(2));
  const low24h = Number((basePrice * 0.978).toFixed(2));
  
  const capValues = ["$48.2B", "$184.6B", "$720.5B", "$1.24T", "$2.89T", "$3.42T"];
  const marketCap = capValues[absSeed % capValues.length];

  return {
    price: basePrice,
    change24h,
    sparkline,
    high24h,
    low24h,
    marketCap,
  };
}
