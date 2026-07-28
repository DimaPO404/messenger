let currentUser = null;
let stompClient = null;
let currentChatId = null;
let editingMessageId = null;
let searchTimeout = null;

document.addEventListener('DOMContentLoaded', () => {
    // Auth
    document.getElementById('tab-login').addEventListener('click', () => switchTab('login'));
    document.getElementById('tab-register').addEventListener('click', () => switchTab('register'));
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // Chat actions
    document.getElementById('chat-window').addEventListener('click', handleMessageAction);
    document.getElementById('message-form').addEventListener('submit', handleSendMessage);

    // Modals & Search
    document.getElementById('close-profile-modal').addEventListener('click', closeProfileModal);
    document.getElementById('user-search').addEventListener('input', handleSearchInput);
    document.getElementById('btn-new-personal').addEventListener('click', () => createPersonalChat());
    document.getElementById('btn-new-group').addEventListener('click', () => createGroupChat());

    checkLocalStorage();
});

function checkLocalStorage() {
    const saved = localStorage.getItem('currentUser');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            initApp();
        } catch (e) {
            localStorage.removeItem('currentUser');
        }
    }
}

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
    document.getElementById(`tab-${tab}`).classList.add('active');
    document.getElementById(`${tab}-form`).classList.add('active');
    clearErrors();
}

function clearErrors() {
    document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
    document.querySelectorAll('.error-text').forEach(el => el.classList.remove('visible'));
    document.querySelectorAll('.global-error').forEach(el => el.style.display = 'none');
}

function showGlobalError(formId, msg) {
    const el = document.getElementById(formId === 'login' ? 'auth-error-global' : 'auth-error-global-reg');
    el.textContent = msg; el.style.display = 'block';
    setTimeout(() => el.style.display = 'none', 5000);
}

async function handleLogin(e) {
    e.preventDefault(); clearErrors();
    const userId = document.getElementById('login-userId');
    const pass = document.getElementById('login-password');

    if (!/^[a-zA-Z0-9_]{1,30}$/.test(userId.value.replace('@', ''))) {
        userId.classList.add('input-error'); userId.parentElement.querySelector('.error-text').classList.add('visible'); return;
    }

    try {
        const res = await fetch('/api/users/login', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId.value.trim(), password: pass.value })
        });
        if (res.ok) {
            currentUser = await res.json();
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            initApp();
        } else {
            showGlobalError('login', await res.text());
        }
    } catch { showGlobalError('login', 'Ошибка сети'); }
}

async function handleRegister(e) {
    e.preventDefault(); clearErrors();
    const data = {
        name: document.getElementById('reg-name').value.trim(),
        userId: document.getElementById('reg-userId').value.trim(),
        phone: document.getElementById('reg-phone').value.trim(),
        password: document.getElementById('reg-password').value,
        description: document.getElementById('reg-description').value.trim()
    };

    // Простая валидация
    if (!data.userId.startsWith('@') || data.password.length < 5) {
        showGlobalError('register', 'Проверьте формат @username и длину пароля'); return;
    }

    try {
        const res = await fetch('/api/users/register', {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data)
        });
        if (res.ok) {
            currentUser = await res.json();
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            initApp();
        } else {
            showGlobalError('register', await res.text());
        }
    } catch { showGlobalError('register', 'Ошибка сети'); }
}

function initApp() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');

    document.getElementById('sidebar-name').textContent = currentUser.name;
    document.getElementById('sidebar-userId').textContent = currentUser.userId;
    document.getElementById('sidebar-avatar').textContent = currentUser.name.charAt(0).toUpperCase();

    loadChats();
    connectWebSocket();
}

async function loadChats() {
    const list = document.getElementById('chat-list');
    list.innerHTML = '<div class="empty-state" style="padding:20px; text-align:center; color:#707579;">Загрузка...</div>';

    try {
        const res = await fetch(`/api/chats?userId=${encodeURIComponent(currentUser.userId)}`);
        const chats = await res.json();

        list.innerHTML = '';
        if (chats.length === 0) {
            list.innerHTML = '<div class="empty-state" style="padding:20px; text-align:center; color:#707579;">Нет чатов. Создайте новый!</div>';
            return;
        }

        chats.forEach(chat => {
            const div = document.createElement('div');
            div.className = `chat-item ${currentChatId === chat.id ? 'active' : ''}`;
            div.dataset.chatId = chat.id;

            const isGroup = chat.type === 'GROUP';
            const displayName = isGroup ? chat.name : 'Личный чат'; // Можно улучшить, подтянув имя собеседника
            const icon = isGroup ? '👥' : '👤';
            const color = isGroup ? '#e17076' : '#707579';

            div.innerHTML = `
                <div class="chat-avatar" style="background:${color}">${icon}</div>
                <div class="chat-info-list">
                    <div class="chat-name">${displayName}</div>
                    <div class="chat-preview">Нажмите, чтобы открыть</div>
                </div>
            `;
            div.addEventListener('click', () => openChat(chat.id, displayName));
            list.appendChild(div);
        });
    } catch (e) {
        console.error(e);
        list.innerHTML = '<div class="empty-state" style="padding:20px; text-align:center; color:red;">Ошибка загрузки</div>';
    }
}

function openChat(chatId, chatName) {
    currentChatId = chatId;
    document.getElementById('no-chat-selected').classList.add('hidden');
    document.getElementById('active-chat-view').classList.remove('hidden');
    document.getElementById('chat-title').textContent = chatName;
    document.getElementById('chat-window').innerHTML = ''; // Очистка (история будет позже)

    // Обновляем активный класс в списке
    document.querySelectorAll('.chat-item').forEach(el => el.classList.remove('active'));
    const activeEl = document.querySelector(`.chat-item[data-chat-id="${chatId}"]`);
    if (activeEl) activeEl.classList.add('active');

    // Переподписка WebSocket
    if (stompClient && stompClient.connected) {
        // Отписываемся от старого (упрощенно: просто переподключаемся или управляем подписками)
        // Для простоты в этом примере мы полагаемся на то, что сообщения приходят,
        // но в идеале нужно хранить ссылку на subscription и делать subscription.unsubscribe()
    }
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        // Подписываемся на все чаты пользователя (упрощенно: пока подписываемся на текущий)
        if (currentChatId) {
            stompClient.subscribe(`/topic/chat/${currentChatId}`, (payload) => {
                renderMessage(JSON.parse(payload.body));
            });
            // Уведомление о входе в текущий чат
            stompClient.send('/app/chat.addUser', {}, JSON.stringify({ sender: currentUser.userId, chatId: currentChatId }));
        }
    });
}

// === Поиск пользователей ===
function handleSearchInput(e) {
    clearTimeout(searchTimeout);
    const query = e.target.value.trim();
    const resultsDiv = document.getElementById('search-results');

    if (query.length < 2) {
        resultsDiv.classList.add('hidden');
        return;
    }

    searchTimeout = setTimeout(async () => {
        try {
            const res = await fetch(`/api/users/search?query=${encodeURIComponent(query)}`);
            const users = await res.json();

            resultsDiv.innerHTML = '';
            if (users.length === 0) {
                resultsDiv.innerHTML = '<div class="search-item">Ничего не найдено</div>';
            } else {
                users.forEach(u => {
                    if (u.userId === currentUser.userId) return; // Не показывать себя
                    const div = document.createElement('div');
                    div.className = 'search-item';
                    div.textContent = `${u.name} (${u.userId})`;
                    div.addEventListener('click', () => startPersonalChatWith(u.userId));
                    resultsDiv.appendChild(div);
                });
            }
            resultsDiv.classList.remove('hidden');
        } catch (err) { console.error(err); }
    }, 300); // Debounce 300ms
}

// === Создание чатов ===
async function createPersonalChat() {
    const targetId = prompt('Введите @username пользователя:');
    if (!targetId || targetId === currentUser.userId) return;
    await createChatRequest('PERSONAL', null, [targetId]);
}

async function createGroupChat() {
    const name = prompt('Введите название группы:');
    if (!name) return;
    // Для простоты создаем группу только с собой, позже можно добавить выбор участников
    await createChatRequest('GROUP', name, []);
}

async function startPersonalChatWith(targetUserId) {
    document.getElementById('search-results').classList.add('hidden');
    document.getElementById('user-search').value = '';
    await createChatRequest('PERSONAL', null, [targetUserId]);
}

async function createChatRequest(type, name, userIds) {
    try {
        const res = await fetch(`/api/chats?creatorUserId=${encodeURIComponent(currentUser.userId)}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ type, name, userIds })
        });
        if (res.ok) {
            const newChat = await res.json();
            loadChats(); // Обновляем список
            openChat(newChat.id, type === 'GROUP' ? newChat.name : 'Личный чат');
        } else {
            alert('Ошибка: ' + await res.text());
        }
    } catch (e) { alert('Ошибка сети'); }
}

// === Сообщения ===
function handleSendMessage(e) {
    e.preventDefault();
    const input = document.getElementById('message-input');
    const content = input.value.trim();
    if (!content || !stompClient || !currentChatId) return;

    if (editingMessageId !== null) {
        stompClient.send('/app/chat.editMessage', {}, JSON.stringify({
            id: editingMessageId, sender: currentUser.userId, content, chatId: currentChatId
        }));
        editingMessageId = null;
        input.placeholder = 'Написать сообщение...';
        input.classList.remove('editing');
    } else {
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
            sender: currentUser.userId, recipient: 'all', content, chatId: currentChatId, status: 'SENT'
        }));
    }
    input.value = '';
}

function renderMessage(msg) {
    if (msg.chatId !== currentChatId) return; // Игнорируем сообщения из других чатов

    const chatWindow = document.getElementById('chat-window');
    const isOwn = msg.sender === currentUser.userId;

    if (msg.status === 'DELETED') {
        const el = document.querySelector(`[data-msg-id="${msg.id}"]`);
        if (el) el.remove();
        return;
    }

    if (msg.sender === 'System') {
        const div = document.createElement('div');
        div.className = 'message system';
        div.textContent = msg.content;
        chatWindow.appendChild(div);
        chatWindow.scrollTop = chatWindow.scrollHeight;
        return;
    }

    const existingEl = document.querySelector(`[data-msg-id="${msg.id}"]`);
    if (existingEl) {
        existingEl.querySelector('.msg-content').textContent = msg.content;
        existingEl.querySelector('.time').innerHTML = `${formatTime(msg.timeStamp)} <span class="edited-mark">(изменено)</span>`;
        return;
    }

    const div = document.createElement('div');
    div.className = `message ${isOwn ? 'own' : 'other'}`;
    div.dataset.msgId = msg.id;

    let actionsHtml = '';
    if (isOwn) {
        actionsHtml = `<div class="message-actions">
            <button class="btn-edit" data-id="${msg.id}" data-content="${msg.content.replace(/"/g, '&quot;')}">✏️</button>
            <button class="btn-delete" data-id="${msg.id}">🗑️</button>
        </div>`;
    }

    const senderHtml = !isOwn ? `<div class="sender clickable" data-user-id="${escapeHtml(msg.sender)}">${escapeHtml(msg.sender)}</div>` : '';

    div.innerHTML = `
        ${senderHtml}
        <div class="msg-content">${escapeHtml(msg.content)}</div>
        <div class="time">${formatTime(msg.timeStamp)}</div>
        ${actionsHtml}
    `;

    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

function handleMessageAction(e) {
    if (e.target.classList.contains('btn-delete')) {
        stompClient.send('/app/chat.deleteMessage', {}, JSON.stringify({
            id: parseInt(e.target.dataset.id), sender: currentUser.userId, chatId: currentChatId
        }));
    }
    if (e.target.classList.contains('btn-edit')) {
        editingMessageId = parseInt(e.target.dataset.id);
        const input = document.getElementById('message-input');
        input.value = e.target.dataset.content;
        input.placeholder = '✏️ Редактирование... (Enter для сохранения)';
        input.classList.add('editing');
        input.focus();
    }
    if (e.target.classList.contains('clickable')) {
        fetchAndShowUserProfile(e.target.dataset.userId);
    }
}

async function fetchAndShowUserProfile(userId) {
    try {
        const res = await fetch(`/api/users/profile?userId=${encodeURIComponent(userId)}`);
        if (res.ok) {
            const user = await res.json();
            document.getElementById('profile-avatar').src = `https://i.pravatar.cc/150?u=${encodeURIComponent(user.userId)}`;
            document.getElementById('profile-name').textContent = user.name;
            document.getElementById('profile-userId').textContent = user.userId;
            document.getElementById('profile-phone').textContent = user.phone || 'Не указан';
            document.getElementById('profile-description').textContent = user.description || 'Нет описания';
            document.getElementById('profile-modal').classList.remove('hidden');
        }
    } catch (e) { console.error(e); }
}

function closeProfileModal() {
    document.getElementById('profile-modal').classList.add('hidden');
}

function handleLogout() {
    if (stompClient?.connected) {
        stompClient.send('/app/chat.deleteUser', {}, JSON.stringify({ sender: currentUser.userId, chatId: currentChatId }));
        stompClient.disconnect();
    }
    localStorage.removeItem('currentUser');
    location.reload();
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatTime(ts) {
    if (!ts) return '';
    try { return new Date(ts).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' }); }
    catch { return ''; }
}

// Закрытие модалки по клику вне её
window.addEventListener('click', (e) => {
    if (e.target.id === 'profile-modal') closeProfileModal();
});