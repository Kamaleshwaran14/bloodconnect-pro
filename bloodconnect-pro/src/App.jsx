import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useEffect } from "react"; // ✅ ADDED
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";

/* Hospital */
import HospitalDashboard from "./pages/hospital/HospitalDashboard";
import HospitalEmergency from "./pages/hospital/HospitalEmergency";
import HospitalManagement from "./pages/hospital/HospitalManagement";

/* Blood Bank */
import BloodBankDashboard from "./pages/bloodbank/BloodBankDashboard";
import BloodBankManagement from "./pages/bloodbank/BloodBankManagement";

/* Donor */
import DonorDashboard from "./pages/donor/DonorDashboard";
import DonorManagement from "./pages/donor/DonorManagement";

/* Admin */
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminApproval from "./pages/admin/AdminApproval";

export default function App() {

  // 🔥 AUTO LOGOUT AFTER INACTIVITY (ADDED ONLY THIS)
  useEffect(() => {
    let timer;

    const resetTimer = () => {
      clearTimeout(timer);

      timer = setTimeout(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("hasStorage");

        window.location.href = "/login";
      }, 15 * 60 * 1000); // ⏳ 15 minutes
    };

    window.onload = resetTimer;
    window.onmousemove = resetTimer;
    window.onkeypress = resetTimer;
    window.onclick = resetTimer;

    return () => clearTimeout(timer);
  }, []);

  return (
    <Router>
      <Navbar />

      <Routes>

        {/* PUBLIC */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* ================= HOSPITAL ================= */}
        <Route path="/hospital-dashboard"
          element={<ProtectedRoute role="HOSPITAL"><HospitalDashboard /></ProtectedRoute>}
        />
        <Route path="/hospital-emergency"
          element={<ProtectedRoute role="HOSPITAL"><HospitalEmergency /></ProtectedRoute>}
        />
        <Route path="/hospital-management"
          element={<ProtectedRoute role="HOSPITAL"><HospitalManagement /></ProtectedRoute>}
        />

        {/* ================= BLOOD BANK ================= */}
        <Route path="/bloodbank-dashboard"
          element={<ProtectedRoute role="BLOOD_BANK"><BloodBankDashboard /></ProtectedRoute>}
        />
        <Route path="/bloodbank-management"
          element={<ProtectedRoute role="BLOOD_BANK"><BloodBankManagement /></ProtectedRoute>}
        />

        {/* ================= DONOR ================= */}
        <Route path="/donor-dashboard"
          element={<ProtectedRoute role="DONOR"><DonorDashboard /></ProtectedRoute>}
        />
        <Route path="/donor-management"
          element={<ProtectedRoute role="DONOR"><DonorManagement /></ProtectedRoute>}
        />

        {/* ================= ADMIN ================= */}
        <Route path="/admin-dashboard"
          element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>}
        />
        <Route path="/admin-approval"
          element={<ProtectedRoute role="ADMIN"><AdminApproval /></ProtectedRoute>}
        />

      </Routes>
    </Router>
  );
}