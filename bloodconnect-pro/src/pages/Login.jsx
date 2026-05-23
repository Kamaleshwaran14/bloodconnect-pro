import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import API from "../services/api";

export default function Login() {
  const [role, setRole] = useState("");
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!role) {
      alert("Please select a role");
      return;
    }

    try {
      const res = await API.post("/auth/login", {
        emailOrPhone: identifier,
        password: password,
      });

      if (typeof res.data === "string") {
        alert(res.data);
        return;
      }

      const {
        token,
        role: backendRole,
        userId,
        hasStorageFacility
      } = res.data;

      // ✅ NORMALIZE ROLE
      const normalize = (r) => r.replace("_", "").toUpperCase();

      if (normalize(role) !== normalize(backendRole)) {
        alert("Role mismatch! Please select correct role.");
        return;
      }

      // ✅ STORE BASIC DATA
      localStorage.setItem("token", token);
      localStorage.setItem("role", backendRole);
      localStorage.setItem("userId", userId);

      // 🔥 FINAL FIX (ONLY ONE PLACE, SAFE STORE)
      const storageValue =
        hasStorageFacility !== undefined && hasStorageFacility !== null
          ? hasStorageFacility
          : false;

      localStorage.setItem("hasStorage", String(storageValue));

      console.log("Backend hasStorageFacility:", hasStorageFacility);
      console.log("Stored hasStorage:", localStorage.getItem("hasStorage"));

      // 🔥 FORCE REFRESH (IMPORTANT)
      const roleMap = {
        ADMIN: "/admin-dashboard",
        DONOR: "/donor-dashboard",
        HOSPITAL: "/hospital-dashboard",
        BLOODBANK: "/bloodbank-dashboard",
        BLOOD_BANK: "/bloodbank-dashboard",
      };

      const path = roleMap[backendRole];

      if (path) {
        window.location.href = path; // ✅ changed from navigate()
      } else {
        alert("Unknown role: " + backendRole);
      }

    } catch (err) {
      console.error(err);
      alert("Login failed. Check server.");
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 via-white to-red-100 pt-24 px-4 pb-12">
      <div className="max-w-lg mx-auto">
        <div className="bg-white w-full p-10 rounded-3xl shadow-2xl border border-gray-100">

          <h2 className="text-3xl font-bold text-center text-red-600 mb-8">
            BloodConnect Pro Login
          </h2>

          {!role && (
            <div className="grid grid-cols-2 gap-4 mb-6">
              {[
                { id: "donor", label: "Donor" },
                { id: "hospital", label: "Hospital" },
                { id: "bloodbank", label: "Blood Bank" },
                { id: "admin", label: "Admin" },
              ].map((item) => (
                <div
                  key={item.id}
                  onClick={() => setRole(item.id)}
                  className="cursor-pointer border p-4 rounded-2xl text-center hover:border-red-500 hover:shadow-md transition"
                >
                  <h3 className="font-semibold">{item.label}</h3>
                </div>
              ))}
            </div>
          )}

          {role && (
            <form onSubmit={handleLogin} className="space-y-5">

              <div className="flex justify-between items-center mb-2">
                <p className="text-sm font-medium text-gray-500">
                  Logging in as:{" "}
                  <span className="text-red-600 capitalize">{role}</span>
                </p>
                <button
                  type="button"
                  onClick={() => setRole("")}
                  className="text-xs text-gray-400 hover:text-red-600"
                >
                  Change
                </button>
              </div>

              <input
                type="text"
                required
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                className="w-full border p-3 rounded-xl"
                placeholder="Email or Phone"
              />

              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full border p-3 rounded-xl"
                placeholder="Password"
              />

              <button className="w-full bg-red-600 text-white py-3 rounded-xl">
                Login
              </button>
            </form>
          )}

          <p className="text-center text-sm mt-8">
            New user?{" "}
            <Link to="/register" className="text-red-600 font-semibold">
              Register here
            </Link>
          </p>

        </div>
      </div>
    </div>
  );
}