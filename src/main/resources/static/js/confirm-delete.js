function confirmDelete(event, message) {
    if (!confirm(message)) {
        event.preventDefault(); // Отмена отправки формы
    }
}