/**
 * QLDT Khoa - Spreadsheet Grade Grid Interaction
 * Điều hướng bàn phím Excel-like & Tính nháp điểm tức thì (hỗ trợ trọng số 0%, Cấm thi, cảnh báo chưa lưu)
 */
document.addEventListener('DOMContentLoaded', function () {
  'use strict';

  var grid = document.getElementById('gradeGridTable');
  if (!grid) return;

  var form = document.getElementById('gradeEntryForm');
  var isDirty = false;

  var parsedQT = parseFloat(grid.dataset.wqt);
  var parsedGK = parseFloat(grid.dataset.wgk);
  var parsedCK = parseFloat(grid.dataset.wck);
  var wQT = Number.isFinite(parsedQT) ? parsedQT : 0.2;
  var wGK = Number.isFinite(parsedGK) ? parsedGK : 0.3;
  var wCK = Number.isFinite(parsedCK) ? parsedCK : 0.5;

  function toHe4(d10) {
    if (d10 === null || isNaN(d10)) return '—';
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
    if (d10 === null || isNaN(d10)) return '—';
    if (d10 >= 8.5) return 'A';
    if (d10 >= 8.0) return 'B+';
    if (d10 >= 7.0) return 'B';
    if (d10 >= 6.5) return 'C+';
    if (d10 >= 5.5) return 'C';
    if (d10 >= 5.0) return 'D+';
    if (d10 >= 4.0) return 'D';
    return 'F';
  }

  function setBadge(el, text, classList) {
    if (!el) return;
    el.textContent = text;
    el.className = classList;
  }

  function validateInput(input) {
    if (!input || input.disabled || input.value === '') {
      if (input) input.classList.remove('is-invalid');
      return { valid: true, value: null };
    }
    var val = parseFloat(input.value);
    var ok = Number.isFinite(val) && val >= 0.0 && val <= 10.0;
    input.classList.toggle('is-invalid', !ok);
    return { valid: ok, value: val };
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

    var resQT = validateInput(inQT);
    var resGK = validateInput(inGK);

    if (inCamThi && inCamThi.checked) {
      if (inCK) {
        if (inCK.value !== '') {
          inCK.dataset.prevVal = inCK.value;
        }
        inCK.value = '';
        inCK.disabled = true;
        inCK.classList.remove('is-invalid');
      }
      if (spanTong) {
        spanTong.textContent = '0.0';
        spanTong.className = 'fw-bold cell-tong text-danger';
      }
      if (spanHe4) spanHe4.textContent = '0.0';
      setBadge(spanChu, 'F', 'badge cell-chu bg-danger text-white');
      setBadge(spanKQ, 'Cấm thi', 'badge cell-kq bg-danger');
      return;
    } else if (inCK) {
      if (inCK.disabled) {
        inCK.disabled = false;
        if (inCK.value === '' && inCK.dataset.prevVal) {
          inCK.value = inCK.dataset.prevVal;
        }
      }
    }

    var resCK = validateInput(inCK);
    if (!resQT.valid || !resGK.valid || !resCK.valid) {
      if (spanTong) {
        spanTong.textContent = '!';
        spanTong.className = 'fw-bold cell-tong text-danger';
      }
      if (spanHe4) spanHe4.textContent = '—';
      setBadge(spanChu, '—', 'badge cell-chu bg-danger bg-opacity-10 text-danger');
      setBadge(spanKQ, 'Điểm 0-10', 'badge cell-kq bg-danger');
      return;
    }

    var readyQT = wQT <= 0 || resQT.value !== null;
    var readyGK = wGK <= 0 || resGK.value !== null;
    var readyCK = wCK <= 0 || resCK.value !== null;

    if (readyQT && readyGK && readyCK) {
      var rawTotal = (resQT.value || 0) * wQT + (resGK.value || 0) * wGK + (resCK.value || 0) * wCK;
      var total = Math.round(rawTotal * 10.0) / 10.0;
      var letter = toDiemChu(total);
      var passed = total >= 4.0;

      if (spanTong) {
        spanTong.textContent = total.toFixed(1);
        spanTong.className = 'fw-bold cell-tong ' + (passed ? 'text-dark' : 'text-danger');
      }
      if (spanHe4) spanHe4.textContent = toHe4(total);
      setBadge(
        spanChu,
        letter,
        'badge cell-chu ' + (letter === 'F' ? 'bg-danger text-white' : 'bg-success bg-opacity-10 text-success')
      );
      setBadge(spanKQ, passed ? 'Đạt' : 'Học lại', 'badge cell-kq ' + (passed ? 'bg-success' : 'bg-danger'));
    } else {
      if (spanTong) {
        spanTong.textContent = '—';
        spanTong.className = 'fw-bold cell-tong text-dark';
      }
      if (spanHe4) spanHe4.textContent = '—';
      setBadge(spanChu, '—', 'badge cell-chu bg-secondary bg-opacity-10 text-secondary');
      setBadge(spanKQ, 'Chưa đủ điểm', 'badge cell-kq bg-secondary');
    }
  }

  grid.addEventListener('input', function (e) {
    if (e.target.classList.contains('grade-input')) {
      isDirty = true;
      var row = e.target.closest('tr');
      if (row) recalcRow(row);
    }
  });

  grid.addEventListener('change', function (e) {
    if (e.target.classList.contains('input-camthi')) {
      isDirty = true;
      var row = e.target.closest('tr');
      if (row) recalcRow(row);
    }
  });

  if (form) {
    form.addEventListener('submit', function () {
      isDirty = false;
    });
  }

  window.addEventListener('beforeunload', function (e) {
    if (isDirty) {
      e.preventDefault();
      e.returnValue = '';
    }
  });

  // Điều hướng bàn phím (Phím mũi tên, Enter) — không dựa vào selectionStart vốn luôn null trên input[type=number]
  grid.addEventListener('keydown', function (e) {
    if (!e.target.classList.contains('grade-input')) return;

    var currentInput = e.target;
    var currentRow = currentInput.closest('tr');
    var colType = currentInput.dataset.col;

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
    } else if (e.key === 'ArrowRight') {
      e.preventDefault();
      if (colType === 'qt') {
        var gk = currentRow.querySelector('[data-col="gk"]');
        if (gk && !gk.disabled) gk.focus();
      } else if (colType === 'gk') {
        var ck = currentRow.querySelector('[data-col="ck"]');
        if (ck && !ck.disabled) ck.focus();
      }
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      if (colType === 'ck') {
        var gkLeft = currentRow.querySelector('[data-col="gk"]');
        if (gkLeft && !gkLeft.disabled) gkLeft.focus();
      } else if (colType === 'gk') {
        var qtLeft = currentRow.querySelector('[data-col="qt"]');
        if (qtLeft && !qtLeft.disabled) qtLeft.focus();
      }
    }
  });
});
