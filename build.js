const fs = require('fs');
const path = require('path');

const wwwDir = path.join(__dirname, 'www');
if (!fs.existsSync(wwwDir)) {
  fs.mkdirSync(wwwDir, { recursive: true });
}

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
  const dest = path.join(wwwDir, file);
  if (fs.existsSync(src)) {
    fs.copyFileSync(src, dest);
  }
});

console.log('Web assets successfully copied to www/');
