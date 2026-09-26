/** @type {import('next').NextConfig} */
const nextConfig = {
  // Standalone output is required for Docker builds (Linux/alpine), but causes NTFS
  // memory-mapped file lock collisions during Next.js builds on Windows.
  output:
    process.env.BUILD_STANDALONE === "true" || process.platform !== "win32"
      ? "standalone"
      : undefined,
  reactStrictMode: true,
};

export default nextConfig;
