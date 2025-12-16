#!/usr/bin/env node
/**
 * DX helper for backend tasks.
 * Usage: node tools/run-backend.mjs <start|test|build|build-skip|format>
 */
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

const [, , cmd] = process.argv;
const isWindows = process.platform === 'win32';
const root = resolve(fileURLToPath(new URL('.', import.meta.url)), '..');
const backendDir = resolve(root, 'backend');
const gradle = isWindows ? 'gradlew.bat' : './gradlew';

const tasks = {
  start: [':api:bootRun'],
  test: ['test'],
  build: ['build'],
  'build-skip': ['build', '-x', 'test'],
  format: ['spotlessApply'],
};

if (!tasks[cmd]) {
  console.log('Usage: node tools/run-backend.mjs <start|test|build|build-skip|format>');
  process.exit(1);
}

const result = spawnSync(gradle, tasks[cmd], {
  cwd: backendDir,
  stdio: 'inherit',
  shell: true,
});

process.exit(result.status ?? 0);
