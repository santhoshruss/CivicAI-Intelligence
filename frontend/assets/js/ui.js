/**
 * CivicAI UI Utilities & Safe Rendering
 * Prevents XSS injection and builds common navigation and modal dialogs.
 */

const UI = {
  escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  },

  showToast(message, type = "success") {
    let container = document.getElementById("toast-container");
    if (!container) {
      container = document.createElement("div");
      container.id = "toast-container";
      document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transition = "opacity 0.3s";
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  renderSidebar(activePage = "") {
    const user = Auth.getUser();
    if (!user) return;

    const sidebarEl = document.querySelector(".sidebar");
    if (!sidebarEl) return;

    let menuItems = [];

    if (user.role === "CITIZEN") {
      menuItems = [
        { name: "My Dashboard", href: "citizen-dashboard.html", icon: "📊", key: "citizen-dashboard" },
        { name: "Report Damage", href: "report-incident.html", icon: "🚨", key: "report-incident" },
        { name: "City Map", href: "map.html", icon: "🗺️", key: "map" }
      ];
    } else if (user.role === "MUNICIPAL_OFFICER") {
      menuItems = [
        { name: "Officer Command", href: "officer-dashboard.html", icon: "🏛️", key: "officer-dashboard" },
        { name: "Incident Triage", href: "incidents.html", icon: "📋", key: "incidents" },
        { name: "Work Orders", href: "work-orders.html", icon: "🛠️", key: "work-orders" },
        { name: "City Map", href: "map.html", icon: "🗺️", key: "map" },
        { name: "RAG Assistant", href: "rag-assistant.html", icon: "📚", key: "rag-assistant" }
      ];
    } else if (user.role === "FIELD_INSPECTOR") {
      menuItems = [
        { name: "Assigned Work", href: "work-orders.html", icon: "🛠️", key: "work-orders" },
        { name: "City Map", href: "map.html", icon: "🗺️", key: "map" },
        { name: "RAG Guidelines", href: "rag-assistant.html", icon: "📚", key: "rag-assistant" }
      ];
    } else if (user.role === "ADMIN") {
      menuItems = [
        { name: "Admin Console", href: "admin-dashboard.html", icon: "⚙️", key: "admin-dashboard" },
        { name: "Officer Command", href: "officer-dashboard.html", icon: "🏛️", key: "officer-dashboard" },
        { name: "All Incidents", href: "incidents.html", icon: "📋", key: "incidents" },
        { name: "Work Orders", href: "work-orders.html", icon: "🛠️", key: "work-orders" },
        { name: "City Map", href: "map.html", icon: "🗺️", key: "map" },
        { name: "RAG Assistant", href: "rag-assistant.html", icon: "📚", key: "rag-assistant" }
      ];
    }

    const menuHtml = menuItems.map(item => `
      <li>
        <a href="${item.href}" class="${activePage === item.key ? 'active' : ''}">
          <span>${item.icon}</span>
          <span>${item.name}</span>
        </a>
      </li>
    `).join("");

    sidebarEl.innerHTML = `
      <div class="sidebar-brand">
        <div class="logo-icon">C</div>
        <div>
          <span class="logo-text">CivicAI</span>
          <span class="logo-sub">Infrastructure Intelligence</span>
        </div>
      </div>
      <ul class="sidebar-menu">
        ${menuHtml}
        <li style="margin-top: 1.5rem; border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 0.5rem;">
          <a href="javascript:void(0)" onclick="Auth.logout()" style="color: #f87171;">
            <span>🚪</span>
            <span>Sign Out</span>
          </a>
        </li>
      </ul>
      <div class="sidebar-footer">
        <div class="user-snippet">
          <div class="user-avatar">${user.name.charAt(0).toUpperCase()}</div>
          <div class="user-info">
            <div class="user-name">${UI.escapeHtml(user.name)}</div>
            <div class="user-role">${UI.escapeHtml(user.role.replace('_', ' '))}</div>
          </div>
        </div>
        <button class="btn btn-danger btn-sm" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" title="Sign out" onclick="Auth.logout()">Sign Out</button>
      </div>
    `;

    // Automatically ensure a prominent Sign Out button is in the topbar
    const topbarActions = document.querySelector(".topbar-actions");
    if (topbarActions && !document.getElementById("btn-topbar-logout")) {
      const topLogout = document.createElement("button");
      topLogout.id = "btn-topbar-logout";
      topLogout.className = "btn btn-secondary btn-sm";
      topLogout.style.color = "#ef4444";
      topLogout.style.borderColor = "#fecaca";
      topLogout.innerHTML = "🚪 Sign Out";
      topLogout.onclick = () => Auth.logout();
      topbarActions.appendChild(topLogout);
    }
  },

  formatDate(dateStr) {
    if (!dateStr) return "-";
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString() + " " + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return dateStr;
    }
  },

  getPriorityBadge(priority) {
    const p = (priority || "MEDIUM").toUpperCase();
    return `<span class="badge badge-${p.toLowerCase()}">${UI.escapeHtml(p)}</span>`;
  },

  getStatusBadge(status) {
    const s = (status || "REPORTED").toUpperCase();
    const cls = s.toLowerCase();
    return `<span class="badge badge-${cls}">${UI.escapeHtml(s.replace(/_/g, " "))}</span>`;
  }
};

// Automatically mount Voice Assistant widget across all pages
if (!document.getElementById("script-voice-assistant")) {
  const vaScript = document.createElement("script");
  vaScript.id = "script-voice-assistant";
  vaScript.src = "assets/js/voice-assistant.js";
  document.head.appendChild(vaScript);
}
