/**
 * QLDT Khoa - Tra cứu thông tin sinh viên & Modal xác nhận ban hành QĐHV (CSP-safe)
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('decisionForm');
    var maSvInput = document.getElementById('maSV');
    var lookupBtn = document.getElementById('btnLookupStudent');
    var previewCard = document.getElementById('studentPreviewCard');
    var previewError = document.getElementById('studentPreviewError');
    var typeSelect = document.getElementById('loaiQuyetDinh');
    var openConfirmBtn = document.getElementById('btnOpenDecisionConfirm');
    var confirmSubmitBtn = document.getElementById('btnConfirmDecisionSubmit');
    var modalEl = document.getElementById('confirmDecisionModal');
    var consequenceEl = document.getElementById('decisionConsequenceList');
    var summaryTargetEl = document.getElementById('decisionConfirmTarget');

    if (!form || !maSvInput) return;

    var allTypeOptions = typeSelect
      ? Array.from(typeSelect.options).map(function (o) {
          return { value: o.value, label: o.textContent };
        })
      : [];

    function renderAllowedOptions(allowedTypes) {
      if (!typeSelect) return;
      var currentVal = typeSelect.value;
      var items = Array.isArray(allowedTypes) && allowedTypes.length > 0 ? allowedTypes : null;

      typeSelect.innerHTML = '';
      if (!items) {
        allTypeOptions.forEach(function (opt) {
          var el = document.createElement('option');
          el.value = opt.value;
          el.textContent = opt.label;
          if (opt.value === currentVal) el.selected = true;
          typeSelect.appendChild(el);
        });
        return;
      }

      items.forEach(function (opt, idx) {
        var el = document.createElement('option');
        el.value = opt.code;
        el.textContent = opt.label;
        if (opt.code === currentVal || (idx === 0 && !items.some(function (i) { return i.code === currentVal; }))) {
          el.selected = true;
        }
        typeSelect.appendChild(el);
      });
    }

    function lookupStudent() {
      var maSV = (maSvInput.value || '').trim();
      if (!maSV) {
        if (previewCard) previewCard.classList.add('d-none');
        if (previewError) previewError.classList.add('d-none');
        renderAllowedOptions(null);
        return;
      }

      var baseUrl = maSvInput.dataset.previewUrl || '/admin/quyet-dinh/preview-student';
      fetch(baseUrl + '?maSV=' + encodeURIComponent(maSV), {
        headers: { Accept: 'application/json' }
      })
        .then(function (resp) {
          if (!resp.ok) throw new Error('HTTP ' + resp.status);
          return resp.json();
        })
        .then(function (data) {
          if (!data || !data.found) {
            if (previewCard) previewCard.classList.add('d-none');
            if (previewError) previewError.classList.remove('d-none');
            renderAllowedOptions(null);
            return;
          }
          if (previewError) previewError.classList.add('d-none');
          if (previewCard) {
            previewCard.classList.remove('d-none');
            document.getElementById('pvHoTen').textContent = data.hoTen || '—';
            document.getElementById('pvMaSV').textContent = data.maSV || '—';
            document.getElementById('pvLopHC').textContent = data.maLopHC || '—';
            document.getElementById('pvTrangThai').textContent = data.trangThaiLabel || '—';
          }
          if (Array.isArray(data.allowedTypes) && data.allowedTypes.length > 0) {
            renderAllowedOptions(data.allowedTypes);
          } else {
            renderAllowedOptions(null);
          }
        })
        .catch(function () {
          if (previewCard) previewCard.classList.add('d-none');
        });
    }

    if (lookupBtn) {
      lookupBtn.addEventListener('click', lookupStudent);
    }
    maSvInput.addEventListener('blur', lookupStudent);
    if ((maSvInput.value || '').trim()) {
      lookupStudent();
    }

    function buildConsequences(typeCode) {
      var items = ['Quyết định học vụ có tính bất biến pháp lý, không thể xóa hoặc sửa sau khi ban hành.'];
      if (typeCode === 'THOI_HOC') {
        items.push('Khóa tài khoản đăng nhập vĩnh viễn và thu hồi ngay các phiên hoạt động hiện tại (khi có hiệu lực).');
        items.push('Tự động rút toàn bộ các lớp học phần đang ở trạng thái Chờ mở.');
      } else if (typeCode === 'BAO_LUU' || typeCode === 'DINH_CHI' || typeCode === 'TOT_NGHIEP') {
        items.push('Tự động chuyển trạng thái sinh viên và rút khỏi các lớp học phần đang Chờ mở.');
      } else if (typeCode === 'TRO_LAI_HOC') {
        items.push('Khôi phục trạng thái Đang học và mở lại quyền truy cập cho sinh viên.');
      }
      return items;
    }

    if (openConfirmBtn && modalEl && window.bootstrap) {
      openConfirmBtn.addEventListener('click', function () {
        if (!form.checkValidity()) {
          form.classList.add('was-validated');
          form.reportValidity();
          return;
        }
        var selectedType = typeSelect ? typeSelect.value : '';
        var selectedLabel = typeSelect && typeSelect.selectedOptions[0]
          ? typeSelect.selectedOptions[0].textContent
          : selectedType;
        var maQD = (document.getElementById('maQuyetDinh')?.value || '').trim();
        var maSV = (maSvInput.value || '').trim();
        var ngayHL = (document.getElementById('ngayHieuLuc')?.value || '').trim();

        if (summaryTargetEl) {
          summaryTargetEl.textContent = selectedLabel + ' (' + maQD + ') cho SV ' + maSV + ' · Hiệu lực: ' + ngayHL;
        }
        if (consequenceEl) {
          consequenceEl.innerHTML = '';
          buildConsequences(selectedType).forEach(function (txt) {
            var li = document.createElement('li');
            li.textContent = txt;
            consequenceEl.appendChild(li);
          });
        }
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
      });
    }

    if (confirmSubmitBtn) {
      confirmSubmitBtn.addEventListener('click', function () {
        confirmSubmitBtn.disabled = true;
        form.submit();
      });
    }
  });
})();
