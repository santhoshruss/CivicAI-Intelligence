/**
 * CivicAI Intelligent Voice & Text Assistant
 * Features Web Speech Recognition (Voice Input) & Speech Synthesis (Voice Output).
 * Accessible across all dashboards and user roles.
 */

const VoiceAssistant = {
  isOpen: false,
  isListening: false,
  autoSpeak: false,
  recognition: null,

  init() {
    this.setupSpeechRecognition();
    this.renderWidget();
    this.bindEvents();
  },

  setupSpeechRecognition() {
    const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRec) {
      this.recognition = new SpeechRec();
      this.recognition.continuous = false;
      this.recognition.interimResults = true;
      this.recognition.lang = "en-US";

      this.recognition.onstart = () => {
        this.isListening = true;
        const mic = document.getElementById("va-mic-btn");
        if (mic) mic.classList.add("listening");
        const input = document.getElementById("va-input");
        if (input) input.placeholder = "Listening... Speak now";
      };

      this.recognition.onresult = (event) => {
        let transcript = "";
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          transcript += event.results[i][0].transcript;
        }
        const input = document.getElementById("va-input");
        if (input) input.value = transcript;
      };

      this.recognition.onend = () => {
        this.isListening = false;
        const mic = document.getElementById("va-mic-btn");
        if (mic) mic.classList.remove("listening");
        const input = document.getElementById("va-input");
        if (input) {
          input.placeholder = "Ask AI Assistant or use voice...";
          const val = input.value.trim();
          if (val.length > 2) {
            this.sendMessage(val);
          }
        }
      };

      this.recognition.onerror = (event) => {
        console.warn("Speech recognition notice:", event.error);
        this.isListening = false;
        const mic = document.getElementById("va-mic-btn");
        if (mic) mic.classList.remove("listening");
        const input = document.getElementById("va-input");
        if (input) input.placeholder = "Ask AI Assistant or use voice...";
      };
    }
  },

  toggleSpeechRecognition() {
    if (!this.recognition) {
      alert("Voice input is not supported in this browser. Please use Chrome, Edge, or Safari.");
      return;
    }

    if (this.isListening) {
      this.recognition.stop();
    } else {
      try {
        this.recognition.start();
      } catch (e) {
        console.warn("Recognition start error:", e);
      }
    }
  },

  speakText(text) {
    if (!("speechSynthesis" in window)) return;
    window.speechSynthesis.cancel();

    // Strip markdown formatting for natural reading
    const cleanText = text
      .replace(/[*_#>`~]/g, "")
      .replace(/\[([^\]]+)\]\([^)]+\)/g, "$1")
      .trim();

    const utterance = new SpeechSynthesisUtterance(cleanText);
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    window.speechSynthesis.speak(utterance);
  },

  renderWidget() {
    if (document.getElementById("voice-assistant-fab")) return;

    // Floating Button
    const fab = document.createElement("button");
    fab.id = "voice-assistant-fab";
    fab.className = "voice-assistant-fab";
    fab.title = "CivicAI Assistant (Voice & Text)";
    fab.innerHTML = `
      <div class="voice-assistant-pulse"></div>
      <span>🤖</span>
    `;
    document.body.appendChild(fab);

    // Assistant Window Panel
    const panel = document.createElement("div");
    panel.id = "voice-assistant-panel";
    panel.className = "voice-assistant-panel";
    panel.innerHTML = `
      <div class="voice-assistant-header">
        <h3>
          <span class="header-status-dot"></span>
          CivicAI Assistant
        </h3>
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <button id="va-speak-toggle" style="background: none; border: 1px solid rgba(255,255,255,0.3); color: white; border-radius: 4px; padding: 0.2rem 0.4rem; font-size: 0.72rem; cursor: pointer;">
            🔊 Audio: Off
          </button>
          <button id="va-close-btn" style="background: none; border: none; color: white; font-size: 1.25rem; cursor: pointer;">✕</button>
        </div>
      </div>

      <div id="va-body" class="voice-assistant-body">
        <div class="chat-bubble-assistant">
          <strong>Hello!</strong> I am your CivicAI Intelligent Assistant.
          <p style="margin-top: 0.35rem; font-size: 0.8rem; color: #475569;">
            You can type or speak with me using the microphone button below. Ask about reporting damage, SOP repair protocols, priority scoring, or work order progress.
          </p>
        </div>
      </div>

      <div class="voice-assistant-chips">
        <span class="quick-chip" onclick="VoiceAssistant.sendMessage('How to report damage?')">🚨 How to report?</span>
        <span class="quick-chip" onclick="VoiceAssistant.sendMessage('What is the safety perimeter for exposed electrical wiring?')">⚡ Electrical safety</span>
        <span class="quick-chip" onclick="VoiceAssistant.sendMessage('What are the asphalt compaction specs for pothole repair?')">🛣️ Pothole specs</span>
        <span class="quick-chip" onclick="VoiceAssistant.sendMessage('Explain priority levels')">🔥 Priority levels</span>
        <span class="quick-chip" onclick="VoiceAssistant.sendMessage('Where is the city map?')">🗺️ City Map</span>
      </div>

      <div class="voice-assistant-footer">
        <button id="va-mic-btn" class="mic-btn" title="Click to speak (Voice Recognition)">🎙️</button>
        <input type="text" id="va-input" class="voice-input" placeholder="Ask AI Assistant or use voice..." autocomplete="off">
        <button id="va-send-btn" class="send-btn" title="Send message">➤</button>
      </div>
    `;
    document.body.appendChild(panel);
  },

  bindEvents() {
    const fab = document.getElementById("voice-assistant-fab");
    const panel = document.getElementById("voice-assistant-panel");
    const closeBtn = document.getElementById("va-close-btn");
    const micBtn = document.getElementById("va-mic-btn");
    const sendBtn = document.getElementById("va-send-btn");
    const input = document.getElementById("va-input");
    const speakToggle = document.getElementById("va-speak-toggle");

    fab.addEventListener("click", () => this.togglePanel());
    closeBtn.addEventListener("click", () => this.togglePanel());

    micBtn.addEventListener("click", () => this.toggleSpeechRecognition());

    sendBtn.addEventListener("click", () => {
      const text = input.value.trim();
      if (text) this.sendMessage(text);
    });

    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        const text = input.value.trim();
        if (text) this.sendMessage(text);
      }
    });

    speakToggle.addEventListener("click", () => {
      this.autoSpeak = !this.autoSpeak;
      speakToggle.textContent = this.autoSpeak ? "🔊 Audio: On" : "🔇 Audio: Off";
      speakToggle.style.background = this.autoSpeak ? "rgba(16, 185, 129, 0.3)" : "none";
    });
  },

  togglePanel() {
    const panel = document.getElementById("voice-assistant-panel");
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      panel.classList.add("active");
      document.getElementById("va-input").focus();
    } else {
      panel.classList.remove("active");
      if (this.isListening && this.recognition) {
        this.recognition.stop();
      }
      if ("speechSynthesis" in window) {
        window.speechSynthesis.cancel();
      }
    }
  },

  async sendMessage(text) {
    const body = document.getElementById("va-body");
    const input = document.getElementById("va-input");
    input.value = "";

    // 1. Append User Bubble
    const userBubble = document.createElement("div");
    userBubble.className = "chat-bubble-user";
    userBubble.textContent = text;
    body.appendChild(userBubble);

    // 2. Append Typing Indicator
    const typingBubble = document.createElement("div");
    typingBubble.className = "chat-bubble-assistant";
    typingBubble.id = "va-typing";
    typingBubble.innerHTML = `<em>CivicAI Assistant is thinking...</em>`;
    body.appendChild(typingBubble);
    body.scrollTop = body.scrollHeight;

    try {
      const res = await Api.post("/assistant/chat", { message: text });
      typingBubble.remove();

      if (res.success && res.data) {
        const d = res.data;
        const aiBubble = document.createElement("div");
        aiBubble.className = "chat-bubble-assistant";

        let citationsHtml = "";
        if (d.sources && d.sources.length > 0) {
          citationsHtml = d.sources.map(s => `
            <div style="margin-top: 0.4rem; padding: 0.35rem 0.5rem; background: #f1f5f9; border-left: 2px solid #0284c7; border-radius: 2px; font-size: 0.72rem;">
              <strong>📑 ${UI.escapeHtml(s.documentName)} (p. ${s.pageNumber})</strong>: "${UI.escapeHtml(s.excerpt)}"
            </div>
          `).join("");
        }

        let actionHtml = "";
        if (d.suggestedAction && d.actionUrl) {
          actionHtml = `
            <div style="margin-top: 0.5rem;">
              <a href="${d.actionUrl}" class="btn btn-primary btn-sm" style="font-size: 0.75rem; padding: 0.25rem 0.6rem;">${UI.escapeHtml(d.suggestedAction)} →</a>
            </div>
          `;
        }

        aiBubble.innerHTML = `
          <div style="white-space: pre-line;">${UI.escapeHtml(d.reply)}</div>
          ${citationsHtml}
          ${actionHtml}
          <button class="chat-audio-btn" onclick="VoiceAssistant.speakText(${JSON.stringify(d.reply)})">
            🔊 Listen
          </button>
        `;
        body.appendChild(aiBubble);

        if (this.autoSpeak) {
          this.speakText(d.reply);
        }
      } else {
        const errBubble = document.createElement("div");
        errBubble.className = "chat-bubble-assistant";
        errBubble.textContent = "Could not process request at this moment.";
        body.appendChild(errBubble);
      }
    } catch (err) {
      if (document.getElementById("va-typing")) {
        document.getElementById("va-typing").remove();
      }
      const errBubble = document.createElement("div");
      errBubble.className = "chat-bubble-assistant";
      errBubble.textContent = "Error: " + err.message;
      body.appendChild(errBubble);
    }

    body.scrollTop = body.scrollHeight;
  }
};

// Automatically mount when DOM is ready
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => VoiceAssistant.init());
} else {
  VoiceAssistant.init();
}
