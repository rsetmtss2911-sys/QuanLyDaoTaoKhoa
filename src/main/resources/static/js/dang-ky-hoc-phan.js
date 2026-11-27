/**
 * QLDT Khoa - Bộ lọc client-side & Tab chuyển đổi Mobile trên cổng ĐKHP (CSP-safe)
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var searchInput = document.getElementById('dkSearchInput');
    var dayFilter = document.getElementById('dkDayFilter');
    var seatOnlyCheck = document.getElementById('dkSeatOnly');
    var eligibleOnlyCheck = document.getElementById('dkEligibleOnly');
    var visibleCountEl = document.getElementById('dkVisibleCount');
    var tabButtons = document.querySelectorAll('[data-dk-tab]');
    var openPane = document.getElementById('dkOpenPane');
    var selectedPane = document.getElementById('dkSelectedPane');

    function applyFilter() {
      var query = (searchInput ? searchInput.value : '').trim().toLowerCase();
      var day = dayFilter ? dayFilter.value : '';
      var seatOnly = seatOnlyCheck ? seatOnlyCheck.checked : false;
      var eligibleOnly = eligibleOnlyCheck ? eligibleOnlyCheck.checked : false;

      var items = document.querySelectorAll('[data-dk-item]');
      var visibleDesktop = 0;

      items.forEach(function (el) {
        var text = (el.dataset.search || '').toLowerCase();
        var days = el.dataset.days || '';
        var hasSeat = el.dataset.hasSeat === 'true';
        var eligible = el.dataset.eligible === 'true';

        var matchesQuery = !query || text.indexOf(query) !== -1;
        var matchesDay = !day || days.indexOf(day) !== -1;
        var matchesSeat = !seatOnly || hasSeat;
        var matchesEligible = !eligibleOnly || eligible;

        var show = matchesQuery && matchesDay && matchesSeat && matchesEligible;
        el.classList.toggle('d-none', !show);
        if (show && el.tagName === 'TR') {
          visibleDesktop++;
        }
      });

      if (visibleCountEl) {
        visibleCountEl.textContent = visibleDesktop + ' lớp';
      }
    }

    if (searchInput) searchInput.addEventListener('input', applyFilter);
    if (dayFilter) dayFilter.addEventListener('change', applyFilter);
    if (seatOnlyCheck) seatOnlyCheck.addEventListener('change', applyFilter);
    if (eligibleOnlyCheck) eligibleOnlyCheck.addEventListener('change', applyFilter);

    tabButtons.forEach(function (btn) {
      btn.addEventListener('click', function () {
        var target = btn.dataset.dkTab;
        tabButtons.forEach(function (b) {
          var active = b === btn;
          b.classList.toggle('btn-primary', active);
          b.classList.toggle('btn-outline-secondary', !active);
        });
        if (openPane && selectedPane) {
          openPane.classList.toggle('d-none', target === 'selected');
          openPane.classList.toggle('d-md-block', target === 'selected');
          selectedPane.classList.toggle('d-none', target === 'open');
          selectedPane.classList.toggle('d-md-block', target === 'open');
        }
      });
    });
  });
})();
