import { Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import { Menu, X } from "lucide-react";

export default function Navbar() {
  const normalize = (r) => r?.replace("_", "").toUpperCase();

  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);

  // 🔥 FIX: force re-render when localStorage changes
  const [refresh, setRefresh] = useState(false);

  useEffect(() => {
    const handleStorageChange = () => {
      setRefresh((prev) => !prev);
    };

    window.addEventListener("storage", handleStorageChange);

    // also trigger on load (same tab fix)
    handleStorageChange();

    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  const role = normalize(localStorage.getItem("role")) || "GUEST";

  const hasStorage = localStorage.getItem("hasStorage") === "true";

  const navItem = (path, label) => (
    <Link
      to={path}
      onClick={() => setIsOpen(false)}
      className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
        location.pathname === path
          ? "bg-red-600 text-white"
          : "text-gray-700 hover:bg-gray-100"
      }`}
    >
      {label}
    </Link>
  );

  const logout = () => {
    localStorage.clear();
    window.location.href = "/";
  };

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50 w-full">

      <div className="w-full px-6 py-4 flex items-center justify-between">

        <div className="flex-shrink-0">
          <Link to="/" className="text-3xl md:text-4xl font-bold text-red-600 tracking-tight">
            BloodConnect Pro
          </Link>
        </div>

        <div className="hidden md:flex items-center gap-3 flex-wrap justify-end">

          {navItem("/", "Home")}

          {role === "HOSPITAL" && (
            <>
              {navItem("/hospital-dashboard", "Dashboard")}
              {navItem("/hospital-emergency", "Emergency")}
              {hasStorage && navItem("/hospital-management", "Inventory")}
              <button onClick={logout} className="px-4 py-2 bg-gray-200 rounded-lg text-sm hover:bg-gray-300">
                Logout
              </button>
            </>
          )}

          {role === "BLOODBANK" && (
            <>
              {navItem("/bloodbank-dashboard", "Dashboard")}
              {navItem("/bloodbank-management", "Inventory")}
              <button onClick={logout} className="px-4 py-2 bg-gray-200 rounded-lg text-sm hover:bg-gray-300">
                Logout
              </button>
            </>
          )}

          {role === "DONOR" && (
            <>
              {navItem("/donor-dashboard", "Dashboard")}
              {navItem("/donor-management", "Donation")}
              <button onClick={logout} className="px-4 py-2 bg-gray-200 rounded-lg text-sm hover:bg-gray-300">
                Logout
              </button>
            </>
          )}

          {role === "ADMIN" && (
            <>
              {navItem("/admin-dashboard", "Dashboard")}
              {navItem("/admin-approval", "Approval")}
              <button onClick={logout} className="px-4 py-2 bg-gray-200 rounded-lg text-sm hover:bg-gray-300">
                Logout
              </button>
            </>
          )}

        </div>

        <div className="md:hidden">
          <button onClick={() => setIsOpen(!isOpen)}>
            {isOpen ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>

      </div>

      {isOpen && (
        <div className="md:hidden px-6 pb-4 flex flex-col gap-2 bg-white border-t">

          {navItem("/", "Home")}

          {role === "HOSPITAL" && (
            <>
              {navItem("/hospital-dashboard", "Dashboard")}
              {navItem("/hospital-emergency", "Emergency")}
              {hasStorage && navItem("/hospital-management", "Inventory")}
            </>
          )}

          {role === "BLOODBANK" && (
            <>
              {navItem("/bloodbank-dashboard", "Dashboard")}
              {navItem("/bloodbank-management", "Inventory")}
            </>
          )}

          {role === "DONOR" && (
            <>
              {navItem("/donor-dashboard", "Dashboard")}
              {navItem("/donor-management", "Donation")}
            </>
          )}

          {role === "ADMIN" && (
            <>
              {navItem("/admin-dashboard", "Dashboard")}
              {navItem("/admin-approval", "Approval")}
            </>
          )}

          {role !== "GUEST" && (
            <button onClick={logout} className="px-4 py-2 bg-gray-200 rounded-lg text-left">
              Logout
            </button>
          )}

        </div>
      )}

    </nav>
  );
}