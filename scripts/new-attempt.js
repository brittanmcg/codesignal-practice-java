#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const readline = require('readline');

const ROOT = path.resolve(__dirname, '..');
const THEMES_DIR = path.join(ROOT, 'themes');
const ATTEMPTS_DIR = path.join(ROOT, 'attempts');

function loadThemes() {
  return fs
    .readdirSync(THEMES_DIR, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => {
      const themeDir = path.join(THEMES_DIR, entry.name);
      const meta = JSON.parse(fs.readFileSync(path.join(themeDir, 'META.json'), 'utf8'));
      return { dir: themeDir, ...meta };
    });
}

function printList(themes) {
  console.log('\nAvailable assessment themes:\n');
  themes.forEach((theme, i) => {
    console.log(`  ${i + 1}. ${theme.name}  (${theme.id})`);
    console.log(`     ${theme.description}`);
    console.log(`     ~${theme.estimatedMinutes} min | sections: ${theme.sections.join(' -> ')}\n`);
  });
}

function copyRecursive(src, dest) {
  fs.mkdirSync(dest, { recursive: true });
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyRecursive(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

function timestamp() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}-${pad(d.getHours())}${pad(
    d.getMinutes()
  )}`;
}

function scaffold(theme) {
  const dirName = `${theme.id}-${timestamp()}`;
  const dest = path.join(ATTEMPTS_DIR, dirName);
  if (fs.existsSync(dest)) {
    console.error(`Attempt directory already exists: ${dest}`);
    process.exit(1);
  }

  copyRecursive(path.join(theme.dir, 'starter'), dest);

  const startedAt = new Date().toISOString();
  fs.writeFileSync(
    path.join(dest, '.attempt-meta.json'),
    JSON.stringify({ theme: theme.id, startedAt }, null, 2)
  );

  console.log(`\nScaffolded "${theme.name}" into attempts/${dirName}\n`);
  console.log(`Read the spec:   themes/${theme.id}/README.md`);
  console.log(`Implement in:    attempts/${dirName}/src/main/java/`);
  console.log(`Run section 1:   cd attempts/${dirName} && mvn test -Dtest=Section1Test`);
  console.log(`Run everything:  cd attempts/${dirName} && mvn test`);
  console.log(`Started at:      ${startedAt}`);
  console.log(
    `\nDon't open themes/${theme.id}/reference-solution/ until you're done or truly stuck.\n`
  );
}

function main() {
  const themes = loadThemes();
  const args = process.argv.slice(2);

  if (args.includes('--list')) {
    printList(themes);
    return;
  }

  const requested = args[0];
  if (requested) {
    const theme = themes.find((t) => t.id === requested);
    if (!theme) {
      console.error(`Unknown theme "${requested}". Run with --list to see options.`);
      process.exit(1);
    }
    scaffold(theme);
    return;
  }

  printList(themes);
  const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
  rl.question('Pick a theme number (or Ctrl+C to cancel): ', (answer) => {
    const index = parseInt(answer, 10) - 1;
    rl.close();
    if (Number.isNaN(index) || index < 0 || index >= themes.length) {
      console.error('Invalid selection.');
      process.exit(1);
    }
    scaffold(themes[index]);
  });
}

main();
