let currentUser = null;
let stompClient = null;
const CHAT_ID = 1;
let editingMessageId = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('tab-login').addEventListener('click', () => switchTab('login'));
    document.getElementById('tab-register').addEventListener('click', () => switchTab('register'));

    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('message-form').addEventListener('submit', handleSendMessage);
    document.getElementById('logout-btn').addEventListener('click', handleLogout);
    document.getElementById('chat-window').addEventListener('click', handleMessageAction);
});

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`${tab}-form`).classList.add('active');
    clearErrors();
}

function validateField(input, regex, errorMsg) {
    const errorDiv = input.parentElement.querySelector('.error-text');
    if (!regex.test(input.value)) {
        input.classList.add('input-error');
        if (errorDiv) {
            errorDiv.textContent = errorMsg;
            errorDiv.classList.add('visible');
        }
        return false;
    }
    input.classList.remove('input-error');
    if (errorDiv) {
        errorDiv.classList.remove('visible');
    }
    return true;
}

function clearErrors() {
    document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
    document.querySelectorAll('.error-text.visible').forEach(el => el.classList.remove('visible'));
    document.querySelectorAll('.global-error').forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
}

function showGlobalError(formId, message) {
    const errorDiv = formId === 'login'
        ? document.getElementById('auth-error-global')
        : document.getElementById('auth-error-global-reg');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

async function handleLogin(e) {
    e.preventDefault();
    clearErrors();
    const userIdInput = document.getElementById('login-userId');
    const passwordInput = document.getElementById('login-password');

    if (!validateField(userIdInput, /^@[a-zA-Z0-9_]{1,30}$/, 'Invalid user ID format')) return;
    if (!validateField(passwordInput, /.{5,}/, 'Password must be at least 5 characters')) return;

    try {
        const response = await fetch('/api/users/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userIdInput.value.trim(), password: passwordInput.value })
        });

        if (response.ok) {
            currentUser = await response.json();
            enterChat();
        } else {
            const errText = await response.text();
            showGlobalError('login', errText || 'Ошибка входа');
        }
    } catch (error) {
        showGlobalError('login', 'Ошибка сети. Проверьте подключение.');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    clearErrors();

    const name = document.getElementById('reg-name');
    const userId = document.getElementById('reg-userId');
    const phone = document.getElementById('reg-phone');
    const password = document.getElementById('reg-password');
    const description = document.getElementById('reg-description');

    let isValid = true;
    isValid = validateField(name, /.{1,}/, 'Name cannot be empty') && isValid;
    isValid = validateField(userId, /^@[a-zA-Z0-9_]{1,30}$/, 'Invalid user ID format') && isValid;
    isValid = validateField(phone, /^\+?\d[\d\s\-\(\)]{6,14}\d$/, 'Invalid phone format') && isValid;
    isValid = validateField(password, /.{5,}/, 'Password must be more than 4 symbols') && isValid;

    if (!isValid) return;

    try {
        const response = await fetch('/api/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: name.value.trim(), userId: userId.value.trim(),
                phone: phone.value.trim(), password: password.value,
                description: description.value.trim()
            })
        });

        if (response.ok) {
            currentUser = await response.json();
            enterChat();
        } else {
            const errText = await response.text();
            showGlobalError('register', errText || 'Ошибка регистрации');
        }
    } catch (error) {
        showGlobalError('register', 'Ошибка сети. Проверьте подключение.');
    }
}

function enterChat() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('chat-screen').classList.remove('hidden');

    if (!currentUser || !currentUser.userId) {
        console.error("КРИТИЧЕСКАЯ ОШИБА: currentUser или userId не определен!", currentUser);
        return;
    }

    connectWebSocket();
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({},
        () => {
            stompClient.subscribe(`/topic/chat/${CHAT_ID}`, (payload) => {
                const msg = JSON.parse(payload.body);
                renderMessage(msg);
            });

            stompClient.send('/app/chat.addUser', {}, JSON.stringify({
                sender: currentUser.userId,
                chatId: CHAT_ID
            }));
        },
        (error) => {
            console.error("Ошибка WebSocket:", error);
        }
    );
}

function handleSendMessage(e) {
    e.preventDefault();
    const input = document.getElementById('message-input');
    const content = input.value.trim();
    if (!content || !stompClient) return;

    if (editingMessageId !== null) {
        // Редактирование существующего сообщения
        stompClient.send('/app/chat.editMessage', {}, JSON.stringify({
            id: editingMessageId,
            sender: currentUser.userId,
            content: content,
            chatId: CHAT_ID
        }));
        editingMessageId = null;
        input.placeholder = 'Сообщение...';
        input.classList.remove('editing');
    } else {
        // Отправка нового сообщения
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
            sender: currentUser.userId, recipient: 'all', content: content, chatId: CHAT_ID, status: 'SENT'
        }));
    }
    input.value = '';
}

function renderMessage(msg) {
    const chatWindow = document.getElementById('chat-window');
    const isOwn = msg.sender === currentUser.userId;

    // Обработка удаления
    if (msg.status === 'DELETED') {
        const el = document.querySelector(`[data-msg-id="${msg.id}"]`);
        if (el) el.remove();
        return;
    }

    // Системные сообщения
    if (msg.sender === 'System') {
        const div = document.createElement('div');
        div.className = 'message system';
        div.textContent = msg.content;
        chatWindow.appendChild(div);
        chatWindow.scrollTop = chatWindow.scrollHeight;
        return;
    }

    // Проверяем, существует ли уже сообщение с таким ID
    const existingEl = document.querySelector(`[data-msg-id="${msg.id}"]`);

    if (existingEl) {
        // ОБНОВЛЕНИЕ существующего сообщения (редактирование)
        const contentDiv = existingEl.querySelector('.msg-content');
        const timeDiv = existingEl.querySelector('.time');

        if (contentDiv) {
            contentDiv.textContent = msg.content;
        }

        if (timeDiv) {
            let timeStr = formatTime(msg.timeStamp);
            // Добавляем пометку "изменено"
            timeDiv.innerHTML = `${timeStr} <span class="edited-mark">(изменено)</span>`;
        }

        return;
    }

    // СОЗДАНИЕ нового сообщения
    const div = document.createElement('div');
    div.className = `message ${isOwn ? 'own' : 'other'}`;
    div.dataset.msgId = msg.id;

    let timeHtml = formatTime(msg.timeStamp);

    let actionsHtml = '';
    if (isOwn) {
        const safeContent = msg.content.replace(/"/g, '&quot;');
        actionsHtml = `
            <div class="message-actions">
                <button class="btn-edit" data-id="${msg.id}" data-content="${safeContent}" title="Редактировать">✏️</button>
                <button class="btn-delete" data-id="${msg.id}" title="Удалить">🗑️</button>
            </div>
        `;
    }

    const senderHtml = !isOwn ? `<div class="sender">${escapeHtml(msg.sender)}</div>` : '';

    div.innerHTML = `
        ${senderHtml}
        <div class="msg-content">${escapeHtml(msg.content)}</div>
        <div class="time">${timeHtml}</div>
        ${actionsHtml}
    `;

    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function handleMessageAction(e) {
    // Удаление сообщения
    if (e.target.classList.contains('btn-delete')) {
        const msgId = e.target.dataset.id;
        // Отправляем без подтверждения - удаление обратимо (просто статус меняется)
        stompClient.send('/app/chat.deleteMessage', {}, JSON.stringify({
            id: parseInt(msgId), sender: currentUser.userId, chatId: CHAT_ID
        }));
        return;
    }

    // Редактирование сообщения
    if (e.target.classList.contains('btn-edit')) {
        const msgId = e.target.dataset.id;
        const currentContent = e.target.dataset.content;

        // Активируем режим редактирования
        editingMessageId = parseInt(msgId);
        const input = document.getElementById('message-input');
        input.value = currentContent;
        input.placeholder = '✏️ Редактирование сообщения... (нажмите Enter)';
        input.classList.add('editing');
        input.focus();

        // Прокручиваем к полю ввода
        input.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

function handleLogout() {
    if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.deleteUser', {}, JSON.stringify({ sender: currentUser.userId, chatId: CHAT_ID }));
        stompClient.disconnect();
    }
    setTimeout(() => location.reload(), 300);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatTime(timeStamp) {
    if (!timeStamp) return '';
    try {
        return new Date(timeStamp).toLocaleTimeString('ru-RU', {
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return '';
    }
}

window.addEventListener('beforeunload', () => {
    if (stompClient && stompClient.connected) {
        try {
            stompClient.send('/app/chat.deleteUser', {}, JSON.stringify({ sender: currentUser.userId, chatId: CHAT_ID }));
        } catch (e) {}
    }
});

