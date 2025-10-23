export interface ProductHit {
    id: string;
    title: string;
    description: string;
    category: string;
    brand: string;
    price: number;
    rating: number;
    score: number;
};

export type Product = {
    id: string;
    title: string;
    description: string;
    category: string;
    brand: string;
    price: number;
    rating: number;
    searchableText: string;
    createdAt: string;
    updatedAt: string;
    searchableHash: string;
    embeddingReady: boolean;
}

export type SearchResponse<T> = {
    items: T[];
    page: number;
    size: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
};
