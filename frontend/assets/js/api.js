/**
 * CivicAI API Client
 * Centralized HTTP service with JWT injection, error handling, and file upload support.
 */

const API_BASE_URL = "http://localhost:8080/api";

const Api = {
  getToken() {
    return localStorage.getItem("civicai_token");
  },

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = this.getToken();

    const headers = {
      ...(options.headers || {})
    };

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData) && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      if (response.status === 401) {
        // Unauthorized
        console.warn("Session expired or unauthorized. Clearing session.");
        localStorage.removeItem("civicai_token");
        localStorage.removeItem("civicai_user");
        if (!window.location.pathname.endsWith("login.html") && !window.location.pathname.endsWith("index.html")) {
          window.location.href = "login.html?expired=true";
        }
        throw new Error("Session expired. Please log in again.");
      }

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errorMsg = data?.message || `Request failed with status ${response.status}`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err.message);
      throw err;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: "POST",
      body: JSON.stringify(body)
    });
  },

  patch(endpoint, body) {
    return this.request(endpoint, {
      method: "PATCH",
      body: JSON.stringify(body)
    });
  },

  upload(endpoint, formData) {
    return this.request(endpoint, {
      method: "POST",
      body: formData
    });
  }
};
