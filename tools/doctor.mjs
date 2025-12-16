#!/usr/bin/env node
/**
 * Cross-OS setup helper.
 * Checks basic tooling and prints per-OS commands for frontend/backend.
 */
import { execSync } from 'node:child_process';

const platform = process.platform;
const isWindows = platform === 'win32';
const isMac = platform === 'darwin';
const isLinux = platform === 'linux';

function check(cmd, args = ['--version']) {
  try {
    const out = execSync([cmd, ...args].join(' '), { stdio: ['ignore', 'pipe', 'ignore'] }).toString().trim();
    return out.split('\n')[0];
  } catch {
    return null;
  }
}

const node = check('node');
const npm = check('npm');
const java = check(isWindows ? 'java.exe' : 'java');

function header(title) {
  console.log(`\n=== ${title} ===`);
}

header('Detected OS');
console.log(isWindows ? 'Windows' : isMac ? 'macOS' : isLinux ? 'Linux' : platform);

header('Tooling');
console.log(`node: ${node ?? 'missing'}`);
console.log(`npm:  ${npm ?? 'missing'}`);
console.log(`java: ${java ?? 'missing (need JDK 21+)'}\n`);

header('Frontend commands');
console.log('Install:   cd frontend && npm install');
console.log('Dev server: cd frontend && npm start');

header('Backend commands');
if (isWindows) {
  console.log('Tests:    cd backend && gradlew.bat test');
  console.log('Format:   cd backend && gradlew.bat spotlessApply');
  console.log('Run API:  cd backend && gradlew.bat :api:bootRun');
  console.log('Build:    cd backend && gradlew.bat build -x test');
} else {
  console.log('Tests:    cd backend && ./gradlew test');
  console.log('Format:   cd backend && ./gradlew spotlessApply');
  console.log('Run API:  cd backend && ./gradlew :api:bootRun');
  console.log('Build:    cd backend && ./gradlew build -x test');
}

if (!java) {
  header('Note');
  console.log('Install JDK 21+ (e.g., Temurin/Adoptium). Ensure java is on PATH.');
}

console.log('\nThis script is advisory only; install the missing tools before running the commands above.');

