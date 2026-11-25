/**
 * QLDT Khoa - Core Client-Side Utilities
 * Không phụ thuộc thư viện ngoài, an toàn với CSP
 */
(function () {
  'use strict';

  // 1. Xác nhận thao tác hệ trọng: <form data-confirm="...">
  document.addEventListener('submit', function (event) {
    var form = event.target;
    var message = form.dataset && form.dataset.confirm;
    if (message && !window.confirm(message)) {
      event.preventDefault();
      return false;
    }
  });

  // 2. Chống nhấp đúp (Double Submit): <button type="submit" data-submit-once>
  document.addEventListener('submit', function (event) {
    if (event.defaultPrevented) return;
    var form = event.target;
    var button = form.querySelector('[data-submit-once]');
    if (button) {
      button.disabled = true;
      button.setAttribute('aria-busy', 'true');
      var originalHtml = button.innerHTML;
      button.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Đang xử lý...';
    }
  });

  // 3. Tự động ẩn thông báo Flash sau 5 giây
  document.addEventListener('DOMContentLoaded', function () {
    var flashAlerts = document.querySelectorAll('.qd-flash-alert');
    flashAlerts.forEach(function (alert) {
      setTimeout(function () {
        if (window.bootstrap && window.bootstrap.Alert) {
          var bsAlert = new bootstrap.Alert(alert);
          bsAlert.close();
        } else {
          alert.style.display = 'none';
        }
      }, 5000);
    });
  });

  // 4. Trích xuất CSRF Token cho Fetch API
  var tokenMeta = document.querySelector('meta[name="_csrf"]');
  var headerMeta = document.querySelector('meta[name="_csrf_header"]');
  if (tokenMeta && headerMeta) {
    window.qdCsrf = {
      header: headerMeta.content,
      token: tokenMeta.content
    };
  }
})();
