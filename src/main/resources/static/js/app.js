(function () {
    const openBtn = document.querySelector('[data-open-chat]');
    if (!openBtn) return;

    let panel = document.querySelector('.chatpanel');
    if (!panel) {
        panel = document.createElement('div');
        panel.className = 'chatpanel';
        panel.innerHTML = `
      <div class="chatpanel__head">
        <div>
          <div style="font-weight:800; letter-spacing:-.2px;">AI Travel Assistant</div>
          <div class="muted" style="font-size:12px;">Always ready to help</div>
        </div>
        <button class="btn btn--ghost" type="button" data-close-chat>Close</button>
      </div>
      <div class="chatpanel__body">
        <p class="muted" style="margin:0 0 10px; font-size:13px;">
          Ask anything about travel planning, destinations, or deals.
        </p>
        <div class="chatrow">
          <input class="input" id="chatInput" placeholder="Ask me anything about travel..." />
          <button class="btn btn--primary" type="button" id="chatSend">Send</button>
        </div>
        <div id="chatOut" class="muted" style="margin-top:10px; font-size:13px;"></div>
      </div>
    `;
        document.body.appendChild(panel);
    }

    const closeBtn = panel.querySelector('[data-close-chat]');
    const chatSend = panel.querySelector('#chatSend');
    const chatInput = panel.querySelector('#chatInput');
    const chatOut = panel.querySelector('#chatOut');

    function toggle(open) {
        panel.classList.toggle('is-open', open);
    }

    openBtn.addEventListener('click', () => toggle(!panel.classList.contains('is-open')));
    closeBtn.addEventListener('click', () => toggle(false));
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') toggle(false);
    });

    chatSend.addEventListener('click', () => {
        const q = (chatInput.value || '').trim();
        if (!q) return;
        chatOut.textContent = 'Sending…';
        // replace with your real endpoint later (ex: /api/chat)
        setTimeout(() => {
            chatOut.textContent = 'Demo reply: I can help you plan that. Want a budget and dates?';
        }, 450);
    });
})();
