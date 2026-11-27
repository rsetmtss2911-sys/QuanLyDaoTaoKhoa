/**
 * QLDT Khoa - Core Client-Side Utilities
 * Không phụ thuộc thư viện ngoài, tuân thủ tuyệt đối Content-Security-Policy (no inline JS/styles)
 */
(function () {
  'use strict';

  function resetSubmitButton(button) {
    if (!button) return;
    if (button.dataset.originalHtml) {
      button.innerHTML = button.dataset.originalHtml;
    }
    button.disabled = false;
    button.removeAttribute('aria-busy');
  }

  // 1. Xác nhận thao tác hệ trọng: <form data-confirm="...">
  document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!form || !form.dataset) return;
    var message = form.dataset.confirm;
    if (message && !window.confirm(message)) {
      event.preventDefault();
    }
  });

  // 2. Chống nhấp đúp (Double Submit) chỉ trên nút được bấm
  document.addEventListener('submit', function (event) {
    if (event.defaultPrevented) return;
    var form = event.target;
    if (!form) return;
    var submitter = event.submitter || form.querySelector('[data-submit-once]');
    if (submitter && submitter.hasAttribute('data-submit-once')) {
      if (!submitter.dataset.originalHtml) {
        submitter.dataset.originalHtml = submitter.innerHTML;
      }
      submitter.disabled = true;
      submitter.setAttribute('aria-busy', 'true');
      submitter.innerHTML =
        '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Đang xử lý...';
    }
  });

  // Khôi phục nút submit khi người dùng nhấn nút Back trên trình duyệt (bfcache)
  window.addEventListener('pageshow', function () {
    document.querySelectorAll('[data-submit-once]').forEach(function (btn) {
      if (btn.disabled || btn.getAttribute('aria-busy') === 'true') {
        resetSubmitButton(btn);
      }
    });
  });

  // 3. Tự động submit form khi đổi bộ lọc: [data-auto-submit]
  document.addEventListener('change', function (event) {
    var target = event.target;
    if (!target) return;
    if (target.hasAttribute('data-auto-submit') && target.form) {
      target.form.submit();
      return;
    }
    var navPrefix = target.getAttribute('data-navigate-url');
    if (navPrefix) {
      window.location.href = navPrefix + encodeURIComponent(target.value || '');
    }
  });

  // 4. Delegated Click Handlers: [data-copy], [data-history-back], [data-print]
  document.addEventListener('click', function (event) {
    var copyEl = event.target.closest('[data-copy]');
    if (copyEl) {
      var textToCopy = copyEl.getAttribute('data-copy');
      if (textToCopy && navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textToCopy).then(function () {
          var originalTitle = copyEl.getAttribute('title') || '';
          copyEl.setAttribute('title', 'Đã sao chép!');
          copyEl.classList.add('fw-bold');
          setTimeout(function () {
            copyEl.setAttribute('title', originalTitle);
            copyEl.classList.remove('fw-bold');
          }, 1500);
        }).catch(function () {});
      }
      return;
    }

    if (event.target.closest('[data-history-back]')) {
      window.history.back();
      return;
    }

    if (event.target.closest('[data-print]')) {
      window.print();
      return;
    }

    var fillBtn = event.target.closest('[data-fill-email]');
    if (fillBtn) {
      var emailInput = document.getElementById('email');
      var passwordInput = document.getElementById('matKhau');
      if (emailInput) {
        emailInput.value = fillBtn.getAttribute('data-fill-email') || '';
      }
      if (passwordInput) {
        passwordInput.value = fillBtn.getAttribute('data-fill-password') || '';
      }
      if (passwordInput && passwordInput.form) {
        var submitBtn = passwordInput.form.querySelector('button[type="submit"]');
        if (submitBtn) {
          submitBtn.focus();
        }
      }
    }
  });

  // 5. DOMContentLoaded: Auto-dismiss chỉ cho success/info, tiến độ thanh bar & auto-open modal
  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.qd-flash-alert.alert-success, .qd-flash-alert.alert-info').forEach(function (alert) {
      setTimeout(function () {
        if (window.bootstrap && window.bootstrap.Alert) {
          bootstrap.Alert.getOrCreateInstance(alert).close();
        } else {
          alert.classList.add('d-none');
        }
      }, 5000);
    });

    document.querySelectorAll('[data-progress-width]').forEach(function (bar) {
      var pct = parseFloat(bar.getAttribute('data-progress-width'));
      if (Number.isFinite(pct)) {
        var clamped = Math.max(0, Math.min(100, pct));
        bar.style.width = clamped + '%';
      }
    });

    var autoModalHost = document.querySelector('[data-auto-open-modal]');
    if (autoModalHost && window.bootstrap) {
      var modalId = autoModalHost.getAttribute('data-auto-open-modal');
      var modalEl = modalId ? document.getElementById(modalId) : null;
      if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
      }
    }
  });

  // 6. Trích xuất CSRF Token cho Fetch API
  var tokenMeta = document.querySelector('meta[name="_csrf"]');
  var headerMeta = document.querySelector('meta[name="_csrf_header"]');
  if (tokenMeta && headerMeta) {
    window.qdCsrf = {
      header: headerMeta.content,
      token: tokenMeta.content
    };
  }
})();
