const fs = require('fs');
const path = require('path');

const wwwDir = path.join(__dirname, 'www');
const androidAssetsDir = path.join(__dirname, 'android', 'app', 'src', 'main', 'assets', 'public');

[wwwDir, androidAssetsDir].forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

const files = [
  'index.html',
  'style.css',
  'game.js',
  'manifest.json',
  'favicon.svg',
  'icon-192.png',
  'icon-512.png',
  'privacy.html',
  'terms.html',
  'sw.js',
  'screenshot-mobile.png',
  'screenshot-desktop.png'
];

files.forEach(file => {
  const src = path.join(__dirname, file);
  if (fs.existsSync(src)) {
    fs.copyFileSync(src, path.join(wwwDir, file));
    fs.copyFileSync(src, path.join(androidAssetsDir, file));
  }
});

console.log('Web assets successfully copied to www/ and android/assets/public/');
