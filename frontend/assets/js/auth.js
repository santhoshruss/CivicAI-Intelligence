/**
 * CivicAI Authentication & Session Guard
 */

const Auth = {
  getUser() {
    try {
      const userStr = localStorage.getItem("civicai_user");
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  getToken() {
    return localStorage.getItem("civicai_token");
  },

  isAuthenticated() {
    return !!this.getToken() && !!this.getUser();
  },

  setSession(authData) {
    localStorage.setItem("civicai_token", authData.token);
    if (authData.refreshToken) {
      localStorage.setItem("civicai_refresh_token", authData.refreshToken);
    }
    const user = {
      id: authData.id,
      name: authData.name,
      email: authData.email,
      role: authData.role,
      departmentId: authData.departmentId
    };
    localStorage.setItem("civicai_user", JSON.stringify(user));
  },

  logout() {
    const refreshToken = localStorage.getItem("civicai_refresh_token");
    if (refreshToken) {
      Api.post("/auth/logout", { refreshToken }).catch(() => {});
    }
    localStorage.removeItem("civicai_token");
    localStorage.removeItem("civicai_refresh_token");
    localStorage.removeItem("civicai_user");
    window.location.href = "login.html";
  },

  requireAuth(allowedRoles = []) {
    if (!this.isAuthenticated()) {
      window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname);
      return false;
    }

    const user = this.getUser();
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
      alert("Access Denied: You do not possess necessary permissions for this page.");
      this.redirectToHome(user.role);
      return false;
    }

    return true;
  },

  redirectToHome(role) {
    switch (role) {
      case "CITIZEN":
        window.location.href = "citizen-dashboard.html";
        break;
      case "MUNICIPAL_OFFICER":
        window.location.href = "officer-dashboard.html";
        break;
      case "FIELD_INSPECTOR":
        window.location.href = "work-orders.html";
        break;
      case "ADMIN":
        window.location.href = "admin-dashboard.html";
        break;
      default:
        window.location.href = "index.html";
    }
  }
};
