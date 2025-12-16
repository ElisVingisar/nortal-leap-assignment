#!/usr/bin/env node
/**
 * DX helper for frontend tasks.
 * Usage: node tools/run-frontend.mjs <install|start|build>
 */
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const [, , cmd] = process.argv;
const root = resolve(fileURLToPath(new URL('.', import.meta.url)), '..');
const frontendDir = resolve(root, 'frontend');

const tasks = {
  install: ['npm', 'install'],
  start: ['npm', 'start'],
  build: ['npm', 'run', 'build'],
};

if (!tasks[cmd]) {
  console.log('Usage: node tools/run-frontend.mjs <install|start|build>');
  process.exit(1);
}

const [bin, ...args] = tasks[cmd];
const result = spawnSync(bin, args, {
  cwd: frontendDir,
  stdio: 'inherit',
  shell: true,
});

process.exit(result.status ?? 0);
