import type { ProductHit, SearchResponse } from "./types";

const BASE_URL = import.meta.env.VITE_API_URL as string;
const PAGE_SIZE = Number(import.meta.env.VITE_PAGE_SIZE ?? 10)

export async function searchProducts(
    query: string,
    page = 0,
    size = PAGE_SIZE
): Promise<SearchResponse<ProductHit>> {
    const url = "http://localhost:8080/api/products/search?q=phone&page=0&size=10";
    const res = await fetch(url);
    if (!res.ok) {
        throw new Error('Search failed: ${res.status}');
    }
    return res.json();
}