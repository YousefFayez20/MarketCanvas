import React from "react";

interface SparklineProps {
  data: number[];
  isPositive: boolean;
  width?: number;
  height?: number;
}

export function Sparkline({
  data,
  isPositive,
  width = 90,
  height = 28,
}: SparklineProps) {
  if (!data || data.length < 2) return null;

  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;

  const points = data
    .map((val, idx) => {
      const x = (idx / (data.length - 1)) * width;
      const y = height - ((val - min) / range) * (height - 6) - 3;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");

  const strokeColor = isPositive ? "#10B981" : "#F43F5E";
  const fillColor = isPositive ? "rgba(16, 185, 129, 0.12)" : "rgba(244, 63, 94, 0.12)";

  // Create area path
  const firstX = 0;
  const lastX = width;
  const areaPath = `M 0,${height} L ${points} L ${lastX},${height} Z`;

  return (
    <svg width={width} height={height} className="overflow-visible">
      <path d={areaPath} fill={fillColor} />
      <polyline
        fill="none"
        stroke={strokeColor}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        points={points}
      />
    </svg>
  );
}
