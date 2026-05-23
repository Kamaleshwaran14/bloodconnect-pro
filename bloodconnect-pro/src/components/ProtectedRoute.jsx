import { Navigate, useLocation } from "react-router-dom";

export default function ProtectedRoute({ role, children }) {
  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("role");
  const hasStorageRaw = localStorage.getItem("hasStorage");

  const location = useLocation(); // ✅ FIX

  // ✅ Normalize roles
  const normalize = (r) => r?.replace("_", "").toUpperCase();

  // ✅ Convert storage value safely
  const hasStorage =
    hasStorageRaw === "true" ||
    hasStorageRaw === true ||
    hasStorageRaw === "TRUE";

  // ❌ Not logged in OR wrong role
  if (!token || normalize(userRole) !== normalize(role)) {
    return <Navigate to="/login" replace />;
  }

  // 🔥 FINAL FIX (USE location.pathname)
  if (
    normalize(role) === "HOSPITAL" &&
    location.pathname === "/hospital-management" &&
    !hasStorage
  ) {
    return <Navigate to="/hospital-dashboard" replace />;
  }

  return children;
}