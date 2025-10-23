import { useState } from 'react';
import reactLogo from './assets/react.svg';
import viteLogo from '/vite.svg';
import './App.css';
import { searchProducts } from './api';
import type {ProductHit} from "./types";

export default function App() {
  const [q, setQ] = useState('phone');
  const [items, setItems] = useState<ProductHit[]>([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const doSearch = async () => {
    setLoading(true);
    setErr(null);
    try {
      const data = await searchProducts(q);
      setItems(data.items ?? []);
    } catch (e: any) {
      setErr(e?.message ?? 'Search failed');
  } finally {
    setLoading(false);
  }
  };

  return (
    <main style={{ maxWidth: 900, margin: "2rem auto" }}>
      <h1>eazysearch</h1>
      <div style={{ display: 'flex', gap: 8}}>
        <input value={q} onChange={ e => setQ(e.target.value) } placeholder="Search..."/>
        <button onClick={doSearch} disabled={loading}>{loading ? 'Searching...' : 'Search'}</button>
      </div>
      {err && <p style={{color: "crimson"}}>Error: {err}</p>}
      <ul style={{ listStyle: 'none', padding: 0, marginTop: 16}}>
        {items.map((p) => (
          <li key={p.id} style={{ padding: 12, borderBottom: '1px solid #eee' }}>
            <strong>{p.title}</strong> - score {p.score.toFixed(3)}
            <div style= {{ fontSize: 14, color: '#555' }}>
            {p.brand && <>Brand: {p.brand} . </>}
            {p.category && <>Category: {p.category} . </>}
            {typeof p.price === 'number' && <>Price: ${p.price.toFixed(2)} . </>}
            {typeof p.rating === 'number' && <>Rating: {p.rating.toFixed(1)}</>}
            </div>
          </li>
        ))}
      </ul>
    </main>
  );
}
