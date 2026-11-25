/**
 * QLDT Khoa - Spreadsheet Grade Grid Interaction
 * Điều hướng bàn phím Excel-like & Tính nháp điểm tức thì
 */
document.addEventListener('DOMContentLoaded', function () {
  'use strict';

  var grid = document.getElementById('gradeGridTable');
  if (!grid) return;

  var wQT = parseFloat(grid.dataset.wqt) || 0.2;
  var wGK = parseFloat(grid.dataset.wgk) || 0.3;
  var wCK = parseFloat(grid.dataset.wck) || 0.5;

  function toHe4(d10) {
    if (d10 === null || isNaN(d10)) return '-';
    if (d10 >= 8.5) return '4.0';
    if (d10 >= 8.0) return '3.5';
    if (d10 >= 7.0) return '3.0';
    if (d10 >= 6.5) return '2.5';
    if (d10 >= 5.5) return '2.0';
    if (d10 >= 5.0) return '1.5';
    if (d10 >= 4.0) return '1.0';
    return '0.0';
  }

  function toDiemChu(d10) {
    if (d10 === null || isNaN(d10)) return '-';
    if (d10 >= 8.5) return 'A';
    if (d10 >= 8.0) return 'B+';
    if (d10 >= 7.0) return 'B';
    if (d10 >= 6.5) return 'C+';
    if (d10 >= 5.5) return 'C';
    if (d10 >= 5.0) return 'D+';
    if (d10 >= 4.0) return 'D';
    return 'F';
  }

  function recalcRow(row) {
    var inCamThi = row.querySelector('.input-camthi');
    var inQT = row.querySelector('.input-qt');
    var inGK = row.querySelector('.input-gk');
    var inCK = row.querySelector('.input-ck');

    var spanTong = row.querySelector('.cell-tong');
    var spanHe4 = row.querySelector('.cell-he4');
    var spanChu = row.querySelector('.cell-chu');
    var spanKQ = row.querySelector('.cell-kq');

    if (inCamThi && inCamThi.checked) {
      if (inCK) {
        inCK.value = '';
        inCK.disabled = true;
      }
      if (spanTong) spanTong.textContent = '—';
      if (spanHe4) spanHe4.textContent = '—';
      if (spanChu) spanChu.textContent = '—';
      if (spanKQ) spanKQ.innerHTML = '<span class="badge bg-danger">Cấm thi</span>';
      return;
    } else {
      if (inCK) {
        inCK.disabled = false;
      }
    }

    var valQT = inQT && inQT.value !== '' ? parseFloat(inQT.value) : null;
    var valGK = inGK && inGK.value !== '' ? parseFloat(inGK.value) : null;
    var valCK = inCK && inCK.value !== '' ? parseFloat(inCK.value) : null;

    if (valQT !== null && valGK !== null && valCK !== null &&
        !isNaN(valQT) && !isNaN(valGK) && !isNaN(valCK)) {
      var total = Math.round((valQT * wQT + valGK * wGK + valCK * wCK) * 10.0) / 10.0;
      if (spanTong) spanTong.textContent = total.toFixed(1);
      if (spanHe4) spanHe4.textContent = toHe4(total);
      if (spanChu) spanChu.textContent = toDiemChu(total);
      if (spanKQ) {
        if (total >= 4.0) {
          spanKQ.innerHTML = '<span class="badge bg-success">Đạt</span>';
        } else {
          spanKQ.innerHTML = '<span class="badge bg-danger">Học lại</span>';
        }
      }
    } else {
      if (spanTong) spanTong.textContent = '—';
      if (spanHe4) spanHe4.textContent = '—';
      if (spanChu) spanChu.textContent = '—';
      if (spanKQ) spanKQ.innerHTML = '<span class="badge bg-secondary">Chưa đủ điểm</span>';
    }
  }

  // Lắng nghe sự kiện gõ điểm
  grid.addEventListener('input', function (e) {
    if (e.target.classList.contains('grade-input')) {
      var row = e.target.closest('tr');
      if (row) recalcRow(row);
    }
  });

  // Lắng nghe sự kiện checkbox Cấm thi
  grid.addEventListener('change', function (e) {
    if (e.target.classList.contains('input-camthi')) {
      var row = e.target.closest('tr');
      if (row) recalcRow(row);
    }
  });

  // Điều hướng bàn phím (Phím mũi tên, Enter)
  grid.addEventListener('keydown', function (e) {
    if (!e.target.classList.contains('grade-input')) return;

    var currentInput = e.target;
    var currentRow = currentInput.closest('tr');
    var colType = currentInput.dataset.col; // 'qt', 'gk', 'ck'

    if (e.key === 'ArrowDown' || e.key === 'Enter') {
      e.preventDefault();
      var nextRow = currentRow.nextElementSibling;
      if (nextRow) {
        var targetInput = nextRow.querySelector('[data-col="' + colType + '"]');
        if (targetInput && !targetInput.disabled) targetInput.focus();
      }
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      var prevRow = currentRow.previousElementSibling;
      if (prevRow) {
        var targetInput = prevRow.querySelector('[data-col="' + colType + '"]');
        if (targetInput && !targetInput.disabled) targetInput.focus();
      }
    } else if (e.key === 'ArrowRight' && currentInput.selectionStart === currentInput.value.length) {
      if (colType === 'qt') {
        var gk = currentRow.querySelector('[data-col="gk"]');
        if (gk && !gk.disabled) gk.focus();
      } else if (colType === 'gk') {
        var ck = currentRow.querySelector('[data-col="ck"]');
        if (ck && !ck.disabled) ck.focus();
      }
    } else if (e.key === 'ArrowLeft' && currentInput.selectionStart === 0) {
      if (colType === 'ck') {
        var gk = currentRow.querySelector('[data-col="gk"]');
        if (gk && !gk.disabled) gk.focus();
      } else if (colType === 'gk') {
        var qt = currentRow.querySelector('[data-col="qt"]');
        if (qt && !qt.disabled) qt.focus();
      }
    }
  });
});
