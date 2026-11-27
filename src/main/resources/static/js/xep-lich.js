/**
 * QLDT Khoa - Tương tác lưới Xếp lịch giảng dạy (CSP-safe)
 */
(function () {
  'use strict';

  function openAddScheduleModal(cellEl) {
    var thuVal = cellEl.getAttribute('data-thu');
    var tietVal = cellEl.getAttribute('data-tiet');
    if (thuVal) {
      var thuSelect = document.getElementById('modalThu');
      if (thuSelect) thuSelect.value = thuVal;
    }
    if (tietVal) {
      var startInput = document.getElementById('modalTietBatDau');
      var endInput = document.getElementById('modalTietKetThuc');
      if (startInput) startInput.value = tietVal;
      if (endInput) {
        var end = parseInt(tietVal, 10) + 2;
        if (end > 15) end = 15;
        endInput.value = end;
      }
    }
    var modalEl = document.getElementById('addScheduleModal');
    if (modalEl && window.bootstrap) {
      bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.timetable-cell-interactive').forEach(function (cell) {
      cell.addEventListener('click', function (e) {
        if (e.target.closest('.timetable-slot') || e.target.closest('form') || e.target.closest('button')) {
          return;
        }
        openAddScheduleModal(cell);
      });
    });
  });
})();
