// Переменные состояния
let currentUser = null;
let stompClient = null;
const CHAT_ID = 1; // Пока захардкодим ID чата

// DOM Элементы
const authView = document.getElementById('auth-view');
const chatView = document.getElementById('chat-view');
const authError = document.getElementById('auth-error');

// --- ЛОГИКА РЕГИСТРАЦИИ (HTTP REST) ---
document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    authError.textContent = '';

    // Собираем данные из формы
    const userData = {
        name: document.getElementById('reg-name').value,
        userId: document.getElementById('reg-userId').value,
        phone: document.getElementById('reg-phone').value,
        password: document.getElementById('reg-password').value,
        description: document.getElementById('reg-description').value
    };

    try {
        // Отправляем POST запрос на бэкенд
        const response = await fetch('/api/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            currentUser = await response.json(); // Получаем созданного пользователя
            showChatView();
        } else {
            const errorMsg = await response.text();
            authError.textContent = errorMsg || 'Ошибка регистрации';
        }
    } catch (error) {
        authError.textContent = 'Ошибка сети';
    }
});

// --- ЛОГИКА ЧАТА (WebSocket) ---
function showChatView() {
    authView.classList.remove('active');
    chatView.classList.add('active');
    connectWebSocket();
}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);

        // Подписка на чат
        stompClient.subscribe(`/topic/chat/${CHAT_ID}`, (message) => {
            const msg = JSON.parse(message.body);
            renderMessage(msg);
        });

        // Уведомление о входе
        stompClient.send("/app/chat.addUser", {}, JSON.stringify({
            sender: currentUser.name,
            chatId: CHAT_ID
        }));
    });
}

document.getElementById('message-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const input = document.getElementById('message-input');
    const content = input.value.trim();

    if (content && stompClient) {
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
            sender: currentUser.name,
            recipient: "all",
            content: content,
            chatId: CHAT_ID,
            status: "SENT"
        }));
        input.value = '';
    }
});

function renderMessage(msg) {
    const chatWindow = document.getElementById('chat-window');
    const div = document.createElement('div');

    if (msg.sender === 'System') {
        div.className = 'message system';
        div.textContent = msg.content;
    } else {
        div.className = 'message user';
        div.innerHTML = `<div class="sender">${msg.sender}</div><div>${msg.content}</div>`;
    }

    chatWindow.appendChild(div);
    chatWindow.scrollTop = chatWindow.scrollHeight;
}

document.getElementById('logout-btn').addEventListener('click', () => {
    location.reload();
});