#!/usr/bin/env node
import { performance } from 'node:perf_hooks';
import crypto from 'node:crypto';

const baseUrl = process.env.PERF_BASE_URL ?? 'http://localhost:8080';
const iterations = Number(process.env.PERF_ITERATIONS ?? 100);

const labels = [
  'health',
  'listBooks',
  'listMembers',
  'borrowReturn',
  'reserveCancel',
  'borrowLimit',
];

const timings = Object.fromEntries(labels.map((l) => [l, []]));
let errors = 0;

async function main() {
  printIntro();
  await expectHealthy();

  for (let i = 0; i < iterations; i++) {
    const nonce = crypto.randomUUID().slice(0, 8);
    const bookId = `perf-book-${nonce}-${i}`;
    const memberId = `perf-m-${nonce}-${i}`;

    await timeRequest('health', () => fetchJson('/api/health'));
    await timeRequest('listBooks', () => fetchJson('/api/books'));
    await timeRequest('listMembers', () => fetchJson('/api/members'));
    await timeRequest('borrowReturn', () => borrowReturnOnce());
    await timeRequest('reserveCancel', () => reserveCancelOnce());
    await timeRequest('borrowLimit', () => borrowLimitScenario(nonce));
  }

  console.log('\nSummary (ms, lower is better):');
  for (const label of labels) {
    const stats = summarize(timings[label]);
    console.log(
      `${label.padEnd(14)} n=${stats.count.toString().padStart(3)}  p50=${stats.p50.toFixed(
        2
      )}  p95=${stats.p95.toFixed(2)}  max=${stats.max.toFixed(2)}`
    );
  }
  console.log(`\nErrors: ${errors}`);
  console.log('Done.');
}

function printIntro() {
  console.log(
    [
      'Perf smoke: quick happy-path API checks (list, borrow/return, reserve/cancel, borrow-limit sweep, health)',
      `Target: ${baseUrl}`,
      `Iterations per endpoint: ${iterations} (tune via PERF_ITERATIONS)`,
      'Metrics: p50/p95/max (ms). Lower is better; errors should be zero.',
    ].join('\n')
  );
}

async function borrowReturnOnce() {
  const headers = { 'Content-Type': 'application/json' };
  await fetchJson('/api/borrow', {
    method: 'POST',
    headers,
    body: JSON.stringify({ bookId: 'b1', memberId: 'm1' }),
  });
  await fetchJson('/api/return', {
    method: 'POST',
    headers,
    body: JSON.stringify({ bookId: 'b1' }),
  });
}

async function reserveCancelOnce() {
  const headers = { 'Content-Type': 'application/json' };
  await fetchJson('/api/reserve', {
    method: 'POST',
    headers,
    body: JSON.stringify({ bookId: 'b2', memberId: 'm2' }),
  });
  await fetchJson('/api/cancel-reservation', {
    method: 'POST',
    headers,
    body: JSON.stringify({ bookId: 'b2', memberId: 'm2' }),
  });
}

async function borrowLimitScenario(nonce) {
  const memberId = `perf-limit-${nonce}`;
  const books = Array.from({ length: 6 }, (_, idx) => `perf-limit-${nonce}-${idx}`);
  const headers = { 'Content-Type': 'application/json' };

  await fetchJson('/api/members', {
    method: 'POST',
    headers,
    body: JSON.stringify({ id: memberId, name: 'Perf Limit Member' }),
  });

  for (const id of books) {
    await fetchJson('/api/books', {
      method: 'POST',
      headers,
      body: JSON.stringify({ id, title: `Perf Limit Book ${id}` }),
    });
  }

  for (let i = 0; i < 5; i++) {
    await fetchJson('/api/borrow', {
      method: 'POST',
      headers,
      body: JSON.stringify({ bookId: books[i], memberId }),
    });
  }

  await fetchJson('/api/borrow', {
    method: 'POST',
    headers,
    body: JSON.stringify({ bookId: books[5], memberId }),
  });

  for (let i = 0; i < 5; i++) {
    await fetchJson('/api/return', {
      method: 'POST',
      headers,
      body: JSON.stringify({ bookId: books[i] }),
    });
  }

  for (const id of books) {
    await fetchJson('/api/books', {
      method: 'DELETE',
      headers,
      body: JSON.stringify({ id }),
    });
  }

  await fetchJson('/api/members', {
    method: 'DELETE',
    headers,
    body: JSON.stringify({ id: memberId }),
  });
}

async function expectHealthy() {
  const res = await fetchJson('/api/health');
  if (!res || res.status !== 'ok') {
    throw new Error(`Health check failed: ${JSON.stringify(res)}`);
  }
}

async function timeRequest(label, fn) {
  const start = performance.now();
  try {
    await fn();
  } catch (err) {
    errors++;
  } finally {
    timings[label].push(performance.now() - start);
  }
}

async function fetchJson(path, init = {}) {
  let res;
  try {
    res = await fetch(baseUrl + path, init);
  } catch (e) {
    throw new Error(
      `Request failed to ${baseUrl + path}. Is the backend running? (start with "./gradlew :api:bootRun" or "node tools/run-backend.mjs start"). ${e.message}`
    );
  }
  const text = await res.text();
  try {
    return text ? JSON.parse(text) : {};
  } catch {
    return {};
  }
}

function summarize(arr) {
  const sorted = [...arr].sort((a, b) => a - b);
  const pick = (p) => sorted[Math.min(sorted.length - 1, Math.floor(p * sorted.length))] ?? 0;
  return {
    count: sorted.length,
    p50: pick(0.5),
    p95: pick(0.95),
    max: sorted[sorted.length - 1] ?? 0,
  };
}

main().catch((err) => {
  console.error('Perf smoke failed:', err.message);
  process.exitCode = 1;
});
