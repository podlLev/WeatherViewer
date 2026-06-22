document.addEventListener('DOMContentLoaded', () => {
    ['successToast', 'errorToast', 'errorsToast'].forEach(id => {
        const toastEl = document.getElementById(id);
        if (toastEl) {
            const toast = new bootstrap.Toast(toastEl);
            toast.show();
        }
    });
});
