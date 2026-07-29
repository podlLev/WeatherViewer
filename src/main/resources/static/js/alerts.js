document.addEventListener('DOMContentLoaded', () => {
    ['successToast', 'errorToast', 'errorsToast'].forEach(id => {
        const toastEl = document.getElementById(id);
        if (toastEl) {
            const toast = new bootstrap.Toast(toastEl);
            toast.show();
        }
    });

    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', () => sortSelect.form.submit());
    }
});