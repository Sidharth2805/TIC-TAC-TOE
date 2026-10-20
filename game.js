/**
 * Tic-Tac-Toe Machine - Web Engine & Controller
 * Matches Java GameEngine & WinChecker specifications
 */

(function () {
  'use strict';

  // --- Sound Synthesizer (Web Audio API) ---
  const AudioCtx = window.AudioContext || window.webkitAudioContext;
  let audioCtx = null;
  let soundEnabled = true;

  function initAudio() {
    if (!audioCtx && AudioCtx) {
      audioCtx = new AudioCtx();
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
  }

  function playTone(freq, durationMs, type = 'sine', gainVal = 0.08) {
    if (!soundEnabled) return;
    try {
      initAudio();
      if (!audioCtx) return;
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
      gain.gain.setValueAtTime(gainVal, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + durationMs / 1000);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + durationMs / 1000);
    } catch (e) {
      // Audio autoplay policy fallback
    }
  }

  function playMoveSound(symbol) {
    if (symbol === 'X') playTone(540, 70, 'triangle', 0.1);
    else if (symbol === 'O') playTone(420, 70, 'sine', 0.1);
    else playTone(480, 70, 'square', 0.06);
  }

  function playWinSound() {
    if (!soundEnabled) return;
    playTone(523.25, 120, 'triangle', 0.12);
    setTimeout(() => playTone(659.25, 120, 'triangle', 0.12), 110);
    setTimeout(() => playTone(783.99, 240, 'triangle', 0.12), 220);
  }

  function playDrawSound() {
    playTone(280, 150, 'sawtooth', 0.08);
  }

  // --- State & Config ---
  const config = {
    gridSize: 3,
    winLength: 3,
    gameMode: 'vs_computer', // vs_computer, two_player, three_player, ai_vs_ai
    gameRule: 'classic', // classic, vanish, blitz, hazards
    difficulty: 'impossible', // rookie, tactical, impossible, chaos
    theme: 'obsidian',
    soundOn: true,
    power: true,
    animations: true,
    heatmap: true,
    player1Name: 'Player 1',
    player2Name: 'Player 2',
    player3Name: 'Player 3'
  };

  const state = {
    board: [],
    turnOrder: ['X', 'O'],
    currentPlayerIdx: 0,
    gameOver: false,
    winner: null,
    winningCells: [],
    moveHistory: [],
    redoHistory: [],
    vanishQueue: { X: [], O: [], 'Δ': [] },
    hazardCells: new Set(),
    blitzTimer: null,
    blitzSeconds: 10,
    isThinking: false
  };

  const stats = {
    p1Wins: 0,
    p2Wins: 0,
    draws: 0,
    totalGames: 0
  };

  // Load stats from localStorage
  try {
    const saved = localStorage.getItem('tictactoe_stats');
    if (saved) Object.assign(stats, JSON.parse(saved));
    const savedAnim = localStorage.getItem('tictactoe_animations');
    if (savedAnim !== null) config.animations = savedAnim === 'true';
    if (!config.animations) document.documentElement.setAttribute('data-animations', 'off');
    const savedHeat = localStorage.getItem('tictactoe_heatmap');
    if (savedHeat !== null) config.heatmap = savedHeat === 'true';
  } catch (e) {}

  function saveStats() {
    try {
      localStorage.setItem('tictactoe_stats', JSON.stringify(stats));
    } catch (e) {}
  }

  // --- Win Checker ---
  function getWinLength(size) {
    if (size === 3) return 3;
    if (size === 4 || size === 5) return 4;
    return 5;
  }

  function checkWin(board, size, winLen) {
    const directions = [
      [0, 1],   // horizontal
      [1, 0],   // vertical
      [1, 1],   // diagonal down-right
      [1, -1]   // diagonal down-left
    ];

    for (let r = 0; r < size; r++) {
      for (let c = 0; c < size; c++) {
        const startIdx = r * size + c;
        const sym = board[startIdx];
        if (!sym || sym === '■') continue;

        for (const [dr, dc] of directions) {
          const cells = [startIdx];
          let matched = true;

          for (let step = 1; step < winLen; step++) {
            const nr = r + dr * step;
            const nc = c + dc * step;
            if (nr < 0 || nr >= size || nc < 0 || nc >= size) {
              matched = false;
              break;
            }
            const nIdx = nr * size + nc;
            if (board[nIdx] !== sym) {
              matched = false;
              break;
            }
            cells.push(nIdx);
          }

          if (matched) {
            return { symbol: sym, cells };
          }
        }
      }
    }
    return null;
  }

  function isBoardFull(board) {
    return board.every(cell => cell !== null);
  }

  // --- HeatMap Prediction Engine (Matching Java BoardEvaluator) ---
  const linesCache = {};
  function getWinLines(size, winLen) {
    const key = `${size}_${winLen}`;
    if (linesCache[key]) return linesCache[key];

    const lines = [];
    const directions = [
      [0, 1],   // horizontal
      [1, 0],   // vertical
      [1, 1],   // diagonal down-right
      [1, -1]   // diagonal down-left
    ];

    for (let r = 0; r < size; r++) {
      for (let c = 0; c < size; c++) {
        for (const [dr, dc] of directions) {
          const line = [];
          let valid = true;
          for (let step = 0; step < winLen; step++) {
            const nr = r + dr * step;
            const nc = c + dc * step;
            if (nr < 0 || nr >= size || nc < 0 || nc >= size) {
              valid = false;
              break;
            }
            line.push(nr * size + nc);
          }
          if (valid) lines.push(line);
        }
      }
    }

    linesCache[key] = lines;
    return lines;
  }

  function evaluateHeatScore(board, size, winLen, activeSym, otherSym) {
    let score = 0;
    const lines = getWinLines(size, winLen);
    for (const line of lines) {
      let aiCount = 0;
      let humanCount = 0;
      let hasHazard = false;
      for (const idx of line) {
        const sym = board[idx];
        if (sym === activeSym) aiCount++;
        else if (sym === otherSym) humanCount++;
        else if (sym === '■') hasHazard = true;
      }
      if (hasHazard || (aiCount > 0 && humanCount > 0)) continue;
      if (aiCount > 0) score += Math.pow(10, aiCount);
      if (humanCount > 0) score -= Math.pow(10, humanCount);
    }
    return score;
  }

  function computeHeatmap() {
    if (!config.heatmap || state.gameOver || !config.power) return null;

    const curSym = state.turnOrder[state.currentPlayerIdx];
    let otherSym = null;
    for (const s of state.turnOrder) {
      if (s !== curSym) {
        otherSym = s;
        break;
      }
    }
    if (!otherSym) otherSym = (curSym === 'X' ? 'O' : 'X');

    const heatScores = {};
    let maxScore = -Infinity;
    let minScore = Infinity;

    for (let i = 0; i < state.board.length; i++) {
      if (!state.board[i]) {
        const copy = state.board.slice();
        copy[i] = curSym;
        const score = evaluateHeatScore(copy, config.gridSize, config.winLength, curSym, otherSym);
        heatScores[i] = score;
        if (score > maxScore) maxScore = score;
        if (score < minScore) minScore = score;
      }
    }

    const heatProbs = {};
    for (const idx in heatScores) {
      const score = heatScores[idx];
      const prob = 1.0 / (1.0 + Math.exp(-score / 150.0));
      heatProbs[idx] = {
        prob,
        pct: Math.round(prob * 100),
        isBest: score === maxScore,
        isWorst: score === minScore && minScore !== maxScore
      };
    }
    return heatProbs;
  }

  // --- Minimax AI ---
  function getAvailableCells(board) {
    const cells = [];
    for (let i = 0; i < board.length; i++) {
      if (board[i] === null) cells.push(i);
    }
    return cells;
  }

  function evaluateBoard(board, size, winLen, aiSym, oppSym) {
    const win = checkWin(board, size, winLen);
    if (win) {
      if (win.symbol === aiSym) return 1000;
      if (win.symbol === oppSym) return -1000;
    }
    return 0;
  }

  function minimax(board, depth, isMax, alpha, beta, size, winLen, aiSym, oppSym, maxDepth) {
    const win = checkWin(board, size, winLen);
    if (win) {
      if (win.symbol === aiSym) return 1000 - depth;
      if (win.symbol === oppSym) return -1000 + depth;
    }
    if (depth >= maxDepth || isBoardFull(board)) return 0;

    const available = getAvailableCells(board);
    if (available.length === 0) return 0;

    if (isMax) {
      let maxEval = -Infinity;
      for (const idx of available) {
        board[idx] = aiSym;
        const ev = minimax(board, depth + 1, false, alpha, beta, size, winLen, aiSym, oppSym, maxDepth);
        board[idx] = null;
        maxEval = Math.max(maxEval, ev);
        alpha = Math.max(alpha, ev);
        if (beta <= alpha) break;
      }
      return maxEval;
    } else {
      let minEval = Infinity;
      for (const idx of available) {
        board[idx] = oppSym;
        const ev = minimax(board, depth + 1, true, alpha, beta, size, winLen, aiSym, oppSym, maxDepth);
        board[idx] = null;
        minEval = Math.min(minEval, ev);
        beta = Math.min(beta, ev);
        if (beta <= alpha) break;
      }
      return minEval;
    }
  }

  function computeBestMove(aiSym) {
    const available = getAvailableCells(state.board);
    if (available.length === 0) return -1;

    const oppSym = (aiSym === 'X') ? 'O' : 'X';

    // Rookie: pure random
    if (config.difficulty === 'rookie') {
      return available[Math.floor(Math.random() * available.length)];
    }

    // Chaos: 50% random, 50% tactical
    if (config.difficulty === 'chaos' && Math.random() < 0.5) {
      return available[Math.floor(Math.random() * available.length)];
    }

    // Immediate win / block check
    for (const idx of available) {
      state.board[idx] = aiSym;
      if (checkWin(state.board, config.gridSize, config.winLength)) {
        state.board[idx] = null;
        return idx;
      }
      state.board[idx] = null;
    }
    for (const idx of available) {
      state.board[idx] = oppSym;
      if (checkWin(state.board, config.gridSize, config.winLength)) {
        state.board[idx] = null;
        return idx;
      }
      state.board[idx] = null;
    }

    // If Tactical difficulty, pick center or corner or random
    if (config.difficulty === 'tactical') {
      const center = Math.floor(state.board.length / 2);
      if (available.includes(center)) return center;
      return available[Math.floor(Math.random() * available.length)];
    }

    // Impossible: Depth-bounded Minimax
    const maxDepth = (config.gridSize === 3) ? 9 : (config.gridSize === 4 ? 3 : 2);
    let bestVal = -Infinity;
    let bestMove = available[0];

    for (const idx of available) {
      state.board[idx] = aiSym;
      const moveVal = minimax(state.board, 0, false, -Infinity, Infinity, config.gridSize, config.winLength, aiSym, oppSym, maxDepth);
      state.board[idx] = null;
      if (moveVal > bestVal) {
        bestVal = moveVal;
        bestMove = idx;
      }
    }
    return bestMove;
  }

  // --- Game Flow ---
  function getPlayerName(sym) {
    if (config.gameMode === 'vs_computer') {
      if (sym === 'X') return config.player1Name || 'Player 1';
      return 'COMPUTER';
    }
    if (config.gameMode === 'two_player') {
      if (sym === 'X') return config.player1Name || 'Player 1';
      return config.player2Name || 'Player 2';
    }
    if (config.gameMode === 'three_player') {
      if (sym === 'X') return config.player1Name || 'Player 1';
      if (sym === 'O') return config.player2Name || 'Player 2';
      return config.player3Name || 'Player 3';
    }
    if (config.gameMode === 'ai_vs_ai') {
      if (sym === 'X') return 'Computer 1';
      return 'Computer 2';
    }
    return sym;
  }

  function startBlitzTimer() {
    stopBlitzTimer();
    if (config.gameRule !== 'blitz' || state.gameOver) return;

    state.blitzSeconds = 10;
    updateBlitzUI(10);

    state.blitzTimer = setInterval(() => {
      state.blitzSeconds--;
      updateBlitzUI(state.blitzSeconds);
      if (state.blitzSeconds <= 0) {
        stopBlitzTimer();
        // Auto-play random available cell
        const available = getAvailableCells(state.board);
        if (available.length > 0 && !state.gameOver) {
          const move = available[Math.floor(Math.random() * available.length)];
          handleMove(move);
        }
      }
    }, 1000);
  }

  function stopBlitzTimer() {
    if (state.blitzTimer) {
      clearInterval(state.blitzTimer);
      state.blitzTimer = null;
    }
    const blitzBar = document.getElementById('blitzBar');
    if (blitzBar) blitzBar.style.display = 'none';
  }

  function updateBlitzUI(sec) {
    const blitzBar = document.getElementById('blitzBar');
    const blitzFill = document.getElementById('blitzFill');
    if (blitzBar && blitzFill) {
      blitzBar.style.display = 'block';
      blitzFill.style.width = (sec * 10) + '%';
    }
    const turnVal = document.getElementById('turnPlayer');
    if (turnVal && !state.gameOver) {
      turnVal.innerText = getPlayerName(state.turnOrder[state.currentPlayerIdx]).toUpperCase() + ' (' + sec + 's)';
    }
  }

  function resetMatch() {
    stopBlitzTimer();
    const totalCells = config.gridSize * config.gridSize;
    state.board = new Array(totalCells).fill(null);
    state.gameOver = false;
    state.winner = null;
    state.winningCells = [];
    state.moveHistory = [];
    state.redoHistory = [];
    state.vanishQueue = { X: [], O: [], 'Δ': [] };
    state.hazardCells = new Set();
    state.isThinking = false;

    // Turn order
    if (config.gameMode === 'three_player') {
      state.turnOrder = ['X', 'O', 'Δ'];
    } else {
      state.turnOrder = ['X', 'O'];
    }
    state.currentPlayerIdx = 0;

    // Hazards rule: randomly place 2-4 wall blocks
    if (config.gameRule === 'hazards') {
      const hazardCount = Math.min(Math.floor(totalCells * 0.15), 4) || 2;
      while (state.hazardCells.size < hazardCount) {
        const randIdx = Math.floor(Math.random() * totalCells);
        state.hazardCells.add(randIdx);
        state.board[randIdx] = '■';
      }
    }

    renderBoard();
    updateUI();

    if (config.gameRule === 'blitz') {
      startBlitzTimer();
    }

    // Trigger AI if first player is computer
    checkAITurn();
  }

  function handleMove(cellIdx) {
    if (!config.power || state.gameOver || state.isThinking) return;
    if (state.board[cellIdx] !== null) return;

    const currentSym = state.turnOrder[state.currentPlayerIdx];

    // Apply Vanish rule: max 3 pieces per player on 3x3
    if (config.gameRule === 'vanish') {
      const maxPieces = (config.gridSize === 3) ? 3 : 4;
      const q = state.vanishQueue[currentSym];
      if (q.length >= maxPieces) {
        const oldest = q.shift();
        state.board[oldest] = null;
        logHistory(`[VANISH]: ${getPlayerName(currentSym)}'s mark at cell ${oldest + 1} disappeared`);
      }
      q.push(cellIdx);
    }

    // Record move
    state.board[cellIdx] = currentSym;
    state.moveHistory.push({ symbol: currentSym, index: cellIdx });
    state.redoHistory = [];
    playMoveSound(currentSym);

    logHistory(`[${state.moveHistory.length.toString().padStart(2, '0')}] ${getPlayerName(currentSym)} -> cell ${cellIdx + 1}`);

    // Check Win or Draw
    const win = checkWin(state.board, config.gridSize, config.winLength);
    if (win) {
      state.gameOver = true;
      state.winner = win.symbol;
      state.winningCells = win.cells;
      stopBlitzTimer();
      playWinSound();

      if (win.symbol === 'X') stats.p1Wins++;
      else stats.p2Wins++;
      stats.totalGames++;
      saveStats();
    } else if (isBoardFull(state.board)) {
      state.gameOver = true;
      state.winner = null;
      stopBlitzTimer();
      playDrawSound();
      stats.draws++;
      stats.totalGames++;
      saveStats();
    } else {
      // Advance turn
      state.currentPlayerIdx = (state.currentPlayerIdx + 1) % state.turnOrder.length;
      if (config.gameRule === 'blitz') startBlitzTimer();
    }

    renderBoard();
    updateUI();

    if (!state.gameOver) {
      checkAITurn();
    }
  }

  function checkAITurn() {
    if (state.gameOver) return;
    const curSym = state.turnOrder[state.currentPlayerIdx];

    const isComputer = (config.gameMode === 'vs_computer' && curSym === 'O') ||
                       (config.gameMode === 'ai_vs_ai');

    if (isComputer) {
      state.isThinking = true;
      const turnVal = document.getElementById('turnPlayer');
      if (turnVal) turnVal.innerText = 'CALCULATING...';

      const delay = (config.gameMode === 'ai_vs_ai') ? 400 : 250;
      setTimeout(() => {
        const best = computeBestMove(curSym);
        state.isThinking = false;
        if (best !== -1) {
          handleMove(best);
        }
      }, delay);
    }
  }

  function undoMove() {
    if (state.moveHistory.length === 0 || state.isThinking) return;
    stopBlitzTimer();
    const lastMove = state.moveHistory.pop();
    state.redoHistory.push(lastMove);
    state.board[lastMove.index] = null;
    state.gameOver = false;
    state.winner = null;
    state.winningCells = [];

    // Step back turn
    state.currentPlayerIdx = (state.currentPlayerIdx - 1 + state.turnOrder.length) % state.turnOrder.length;

    // If vs computer, undo again so human isn't forced to play computer's turn
    if (config.gameMode === 'vs_computer' && state.turnOrder[state.currentPlayerIdx] === 'O' && state.moveHistory.length > 0) {
      const prevMove = state.moveHistory.pop();
      state.redoHistory.push(prevMove);
      state.board[prevMove.index] = null;
      state.currentPlayerIdx = 0;
    }

    renderBoard();
    updateUI();
    if (config.gameRule === 'blitz') startBlitzTimer();
  }

  function redoMove() {
    if (state.redoHistory.length === 0 || state.isThinking || state.gameOver) return;
    const nextMove = state.redoHistory.pop();
    handleMove(nextMove.index);
  }

  // --- UI Rendering ---
  function renderBoard() {
    const grid = document.getElementById('gridBoard');
    if (!grid) return;
    grid.innerHTML = '';
    grid.style.gridTemplateColumns = `repeat(${config.gridSize}, 1fr)`;
    grid.setAttribute('data-grid-size', config.gridSize);

    const heatData = computeHeatmap();

    for (let i = 0; i < state.board.length; i++) {
      const cell = document.createElement('div');
      cell.className = 'cell';
      cell.setAttribute('data-index', i);

      const val = state.board[i];
      if (val === 'X') {
        cell.classList.add('symbol-x', 'filled');
        cell.innerText = 'X';
      } else if (val === 'O') {
        cell.classList.add('symbol-o', 'filled');
        cell.innerText = 'O';
      } else if (val === 'Δ') {
        cell.classList.add('symbol-delta', 'filled');
        cell.innerText = 'Δ';
      } else if (val === '■') {
        cell.classList.add('hazard', 'filled');
        cell.innerText = '■';
        cell.title = 'HAZARD: Blocked wall cell (Cannot be played)';
      } else if (heatData && heatData[i]) {
        const heat = heatData[i];
        cell.classList.add('heat-cell');
        if (heat.isBest) cell.classList.add('heat-best');
        else if (heat.isWorst) cell.classList.add('heat-worst');

        const badge = document.createElement('span');
        badge.className = 'heat-val';
        badge.innerText = heat.pct + '%';
        cell.appendChild(badge);
        cell.title = `Evaluated Probability: ${heat.pct}%`;
        const alpha = (heat.prob * 0.16).toFixed(2);
        cell.style.setProperty('--heat-alpha', alpha);
      }

      if (state.winningCells.includes(i)) {
        cell.classList.add('winning-cell');
      }

      cell.addEventListener('click', () => {
        initAudio();
        handleMove(i);
      });

      grid.appendChild(cell);
    }
  }

  function updateUI() {
    // Turn Indicator
    const badge = document.getElementById('turnBadge');
    const header = document.getElementById('turnHeader');
    const playerLbl = document.getElementById('turnPlayer');

    if (!config.power) {
      badge.innerText = '-';
      header.innerText = 'STATUS';
      playerLbl.innerText = 'POWER LOCKED';
      return;
    }

    if (state.gameOver) {
      if (state.winner) {
        badge.innerText = state.winner;
        badge.className = 'turn-badge ' + (state.winner === 'X' ? 'badge-x' : (state.winner === 'O' ? 'badge-o' : 'badge-p3'));
        header.innerText = 'WINNER';
        playerLbl.innerText = getPlayerName(state.winner).toUpperCase();
      } else {
        badge.innerText = '=';
        badge.className = 'turn-badge';
        header.innerText = 'GAME OVER';
        playerLbl.innerText = 'DRAW: GRID FULL';
      }
    } else {
      const curSym = state.turnOrder[state.currentPlayerIdx];
      badge.innerText = curSym;
      badge.className = 'turn-badge ' + (curSym === 'X' ? 'badge-x' : (curSym === 'O' ? 'badge-o' : 'badge-p3'));
      header.innerText = 'TURN INDICATOR';
      if (!state.isThinking && config.gameRule !== 'blitz') {
        playerLbl.innerText = getPlayerName(curSym).toUpperCase();
      }
    }

    // Stats
    document.getElementById('statP1Name').innerText = (config.player1Name || 'P1').toUpperCase();
    document.getElementById('statP1Score').innerText = stats.p1Wins;
    document.getElementById('statP2Name').innerText = (config.gameMode === 'vs_computer' ? 'COMPUTER' : (config.player2Name || 'P2')).toUpperCase();
    document.getElementById('statP2Score').innerText = stats.p2Wins;
    document.getElementById('statDraws').innerText = stats.draws;
    document.getElementById('statTotal').innerText = stats.totalGames;

    // Status Bar
    document.getElementById('statusMoves').innerText = 'MOVES: ' + state.moveHistory.length;
    document.getElementById('statusMode').innerText = 'MODE: ' + config.gameMode.toUpperCase().replace('_', ' ');

    // Player inputs disabling & mode logic
    const p1In = document.getElementById('p1Input');
    const p2In = document.getElementById('p2Input');
    const p3Row = document.getElementById('p3Row');
    const p3In = document.getElementById('p3Input');

    if (config.gameMode === 'vs_computer') {
      p1In.disabled = false;
      p2In.value = 'Computer';
      p2In.disabled = true;
      p3Row.style.display = 'none';
    } else if (config.gameMode === 'two_player') {
      p1In.disabled = false;
      if (p2In.value === 'Computer') p2In.value = config.player2Name;
      p2In.disabled = false;
      p3Row.style.display = 'none';
    } else if (config.gameMode === 'three_player') {
      p1In.disabled = false;
      p2In.disabled = false;
      p3Row.style.display = 'flex';
      p3In.disabled = false;
    } else if (config.gameMode === 'ai_vs_ai') {
      p1In.value = 'Computer 1';
      p1In.disabled = true;
      p2In.value = 'Computer 2';
      p2In.disabled = true;
      p3Row.style.display = 'none';
    }

    // Button states
    document.getElementById('btnUndo').disabled = state.moveHistory.length === 0 || state.isThinking;
    document.getElementById('btnRedo').disabled = state.redoHistory.length === 0 || state.isThinking;
  }

  function logHistory(text) {
    const box = document.getElementById('historyList');
    if (!box) return;
    const entry = document.createElement('div');
    entry.innerText = text;
    box.appendChild(entry);
    box.scrollTop = box.scrollHeight;
  }

  // --- Attach Event Listeners ---
  function setupEvents() {
    // New Game, Undo, Redo
    document.getElementById('btnNewGame').addEventListener('click', resetMatch);
    document.getElementById('btnUndo').addEventListener('click', undoMove);
    document.getElementById('btnRedo').addEventListener('click', redoMove);

    // Audio & Power
    const btnAudio = document.getElementById('btnAudio');
    btnAudio.addEventListener('click', () => {
      soundEnabled = !soundEnabled;
      btnAudio.innerText = soundEnabled ? 'AUDIO: ON' : 'AUDIO: OFF';
    });

    const btnPower = document.getElementById('btnPower');
    btnPower.addEventListener('click', () => {
      config.power = !config.power;
      btnPower.innerText = config.power ? 'POWER: ON' : 'POWER: OFF';
      updateUI();
    });

    // Heatmap Toggle
    const btnHeatmap = document.getElementById('btnHeatmap');
    if (btnHeatmap) {
      btnHeatmap.innerText = config.heatmap ? 'HEATMAP: ON' : 'HEATMAP: OFF';
      if (config.heatmap) {
        btnHeatmap.classList.add('active');
      } else {
        btnHeatmap.classList.remove('active');
      }
      btnHeatmap.addEventListener('click', () => {
        config.heatmap = !config.heatmap;
        try { localStorage.setItem('tictactoe_heatmap', config.heatmap); } catch (e) {}
        btnHeatmap.innerText = config.heatmap ? 'HEATMAP: ON' : 'HEATMAP: OFF';
        if (config.heatmap) {
          btnHeatmap.classList.add('active');
        } else {
          btnHeatmap.classList.remove('active');
        }
        renderBoard();
      });
    }

    // Animations Toggle
    const btnAnimations = document.getElementById('btnAnimations');
    if (btnAnimations) {
      btnAnimations.innerText = config.animations ? 'ANIMATIONS: ON' : 'ANIMATIONS: OFF';
      if (config.animations) {
        btnAnimations.classList.add('active');
      } else {
        btnAnimations.classList.remove('active');
      }
      btnAnimations.addEventListener('click', () => {
        config.animations = !config.animations;
        try { localStorage.setItem('tictactoe_animations', config.animations); } catch (e) {}
        btnAnimations.innerText = config.animations ? 'ANIMATIONS: ON' : 'ANIMATIONS: OFF';
        if (config.animations) {
          btnAnimations.classList.add('active');
          document.documentElement.removeAttribute('data-animations');
        } else {
          btnAnimations.classList.remove('active');
          document.documentElement.setAttribute('data-animations', 'off');
        }
      });
    }

    // Theme buttons
    document.querySelectorAll('[data-theme-btn]').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('[data-theme-btn]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const theme = btn.getAttribute('data-theme-btn');
        document.documentElement.setAttribute('data-theme', theme);
      });
    });

    // Game Mode buttons
    document.querySelectorAll('[data-mode-btn]').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('[data-mode-btn]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        config.gameMode = btn.getAttribute('data-mode-btn');

        // Disable 3x3 if 3 players
        const btn3x3 = document.querySelector('[data-size-btn="3"]');
        if (config.gameMode === 'three_player') {
          if (btn3x3) btn3x3.disabled = true;
          if (config.gridSize === 3) {
            config.gridSize = 4;
            config.winLength = 4;
            document.querySelectorAll('[data-size-btn]').forEach(b => b.classList.remove('active'));
            const btn4x4 = document.querySelector('[data-size-btn="4"]');
            if (btn4x4) btn4x4.classList.add('active');
          }
        } else {
          if (btn3x3) btn3x3.disabled = false;
        }

        resetMatch();
      });
    });

    // Difficulty buttons
    document.querySelectorAll('[data-diff-btn]').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('[data-diff-btn]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        config.difficulty = btn.getAttribute('data-diff-btn');
      });
    });

    // Rule buttons
    const ruleDesc = document.getElementById('ruleDesc');
    const ruleDescs = {
      classic: 'Classic Tic-Tac-Toe: Standard rules. Line up marks to win.',
      vanish: 'Vanish Mode: Memory grid. Max 3 pieces per player; oldest piece disappears.',
      blitz: 'Blitz Mode: Speed match! 10-second timer per move before auto-play.',
      hazards: 'Hazards Mode: Obstacle grid. Red blocked wall cells cannot be played.'
    };

    document.querySelectorAll('[data-rule-btn]').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('[data-rule-btn]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const rule = btn.getAttribute('data-rule-btn');
        config.gameRule = rule;
        if (ruleDesc) ruleDesc.innerText = ruleDescs[rule] || '';
        resetMatch();
      });
    });

    // Board Size buttons
    document.querySelectorAll('[data-size-btn]').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('[data-size-btn]').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const sz = parseInt(btn.getAttribute('data-size-btn'), 10);
        config.gridSize = sz;
        config.winLength = getWinLength(sz);
        resetMatch();
      });
    });

    // Player name inputs
    const p1In = document.getElementById('p1Input');
    const p2In = document.getElementById('p2Input');
    const p3In = document.getElementById('p3Input');

    p1In.addEventListener('input', (e) => {
      config.player1Name = e.target.value.trim() || 'Player 1';
      updateUI();
    });
    p2In.addEventListener('input', (e) => {
      config.player2Name = e.target.value.trim() || 'Player 2';
      updateUI();
    });
    p3In.addEventListener('input', (e) => {
      config.player3Name = e.target.value.trim() || 'Player 3';
      updateUI();
    });

    // Settings Drawer / Modal Handlers
    const leftCol = document.getElementById('leftCol');
    const modalBackdrop = document.getElementById('modalBackdrop');
    const btnHeaderSettings = document.getElementById('btnHeaderSettings');
    const btnActionSettings = document.getElementById('btnActionSettings');
    const btnCloseSettings = document.getElementById('btnCloseSettings');
    const btnDoneSettings = document.getElementById('btnDoneSettings');

    function openSettingsModal() {
      if (leftCol) leftCol.classList.add('modal-open');
      if (modalBackdrop) modalBackdrop.classList.add('open');
    }

    function closeSettingsModal() {
      if (leftCol) leftCol.classList.remove('modal-open');
      if (modalBackdrop) modalBackdrop.classList.remove('open');
    }

    if (btnHeaderSettings) btnHeaderSettings.addEventListener('click', openSettingsModal);
    if (btnActionSettings) btnActionSettings.addEventListener('click', openSettingsModal);
    if (btnCloseSettings) btnCloseSettings.addEventListener('click', closeSettingsModal);
    if (btnDoneSettings) btnDoneSettings.addEventListener('click', closeSettingsModal);
    if (modalBackdrop) modalBackdrop.addEventListener('click', closeSettingsModal);

    // Accordion Sections (Game Rules, Statistics, Move History)
    document.querySelectorAll('.accordion-header').forEach(header => {
      header.addEventListener('click', () => {
        const item = header.closest('.accordion-item');
        if (!item) return;
        const isOpen = item.classList.contains('open');
        item.classList.toggle('open');
        header.setAttribute('aria-expanded', !isOpen);
      });
    });

    // Keyboard Shortcuts (Matching desktop app)
    window.addEventListener('keydown', (e) => {
      if (e.target.tagName === 'INPUT') return;
      if (e.ctrlKey && e.key === 'z') {
        undoMove();
        e.preventDefault();
      } else if (e.ctrlKey && e.key === 'y') {
        redoMove();
        e.preventDefault();
      } else if (e.ctrlKey && e.key === 'n') {
        resetMatch();
        e.preventDefault();
      }
    });

    // PWA Service Worker Registration
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').catch(() => {});
    }
  }

  // Initialization
  document.addEventListener('DOMContentLoaded', () => {
    setupEvents();
    resetMatch();
  });
})();
