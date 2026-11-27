/**
 * QLDT Khoa - Hiển thị trường chuyên môn giảng viên theo vai trò (CSP-safe)
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var vaiTroSelect = document.getElementById('maVaiTro');
    var gvFields = document.getElementById('giangVienFields');
    if (!vaiTroSelect || !gvFields) return;

    function toggleGvFields() {
      gvFields.classList.toggle('d-none', vaiTroSelect.value !== 'GIANG_VIEN');
    }

    vaiTroSelect.addEventListener('change', toggleGvFields);
    toggleGvFields();
  });
})();
