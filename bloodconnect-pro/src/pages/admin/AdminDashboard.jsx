import { useEffect, useState } from "react";
import axios from "axios";

export default function AdminDashboard() {

  const [stats, setStats] = useState({});
  const [activeSection, setActiveSection] = useState(null);
  const [users, setUsers] = useState([]);

  /* ---------------- FETCH STATS ---------------- */
  useEffect(() => {
    axios.get("http://localhost:8080/api/admin/stats")
      .then(res => setStats(res.data))
      .catch(() => alert("Failed to load stats"));
  }, []);

  /* ---------------- LOAD USERS ---------------- */
  const loadUsers = (role) => {
    setActiveSection(role);

    axios.get(`http://localhost:8080/api/admin/users/${role}`)
      .then(res => setUsers(res.data))
      .catch(() => alert("Failed to load users"));
  };

  /* ---------------- BLOCK USER ---------------- */
  const blockUser = (id) => {
    axios.put(`http://localhost:8080/api/admin/block/${id}`)
      .then(() => {
        alert("User blocked");
        loadUsers(activeSection);
      });
  };

  /* ---------------- APPROVE USER ---------------- */
  const approveUser = (id) => {
    axios.put(`http://localhost:8080/api/admin/approve/${id}`)
      .then(() => {
        alert("User approved");
        loadUsers(activeSection);
      });
  };

  return (
    <div className="min-h-screen bg-gray-50 pt-6 px-4 pb-10">
      <div className="max-w-7xl mx-auto space-y-10">

        <h1 className="text-2xl font-bold text-gray-800">
          Admin Dashboard
        </h1>

        {/* ---------------- STATS ---------------- */}
        <div className="grid md:grid-cols-4 gap-6">
          <StatCard title="Total Donors" value={stats.donors} />
          <StatCard title="Hospitals" value={stats.hospitals} />
          <StatCard title="Blood Banks" value={stats.bloodBanks} />
          <StatCard title="Pending Approvals" value={stats.pendingApprovals} />
        </div>

        {/* ---------------- MANAGEMENT ---------------- */}
        <div className="grid md:grid-cols-3 gap-6">
          <ManageBox title="Manage Donors" onClick={() => loadUsers("DONOR")} />
          <ManageBox title="Manage Hospitals" onClick={() => loadUsers("HOSPITAL")} />
          <ManageBox title="Manage Blood Banks" onClick={() => loadUsers("BLOOD_BANK")} />
        </div>

        {/* ---------------- USERS LIST ---------------- */}
        {activeSection && (
          <div className="bg-white p-6 rounded-2xl shadow-sm border">
            <h2 className="text-lg font-semibold mb-6">
              {activeSection} List
            </h2>

            {users.length === 0 && (
              <p className="text-gray-500">No users found</p>
            )}

            {users.map((user) => (
              <div
                key={user.id}
                className="border rounded-xl p-4 mb-4 space-y-3"
              >
                {/* BASIC INFO */}
                <div className="flex justify-between">
                  <div>
                    <p className="font-semibold">
                      {user.email || user.phone}
                    </p>
                    <p className="text-sm text-gray-600">
                      Role: {user.role}
                    </p>
                    <p className="text-sm text-gray-500">
                      Status: {user.status}
                    </p>
                  </div>

                  {/* ACTION BUTTON */}
                  <div>
                    {user.status === "APPROVED" ? (
                      <button
                        onClick={() => blockUser(user.id)}
                        className="bg-red-500 text-white px-3 py-1 rounded"
                      >
                        Block
                      </button>
                    ) : (
                      <button
                        onClick={() => approveUser(user.id)}
                        className="bg-green-500 text-white px-3 py-1 rounded"
                      >
                        Approve
                      </button>
                    )}
                  </div>
                </div>

                {/* DETAILS */}
                <div className="grid md:grid-cols-2 gap-2 text-sm text-gray-700">
                  {user.details &&
                    Object.entries(user.details).map(([key, value]) => (
                      <p key={key}>
                        <span className="font-medium">{key}:</span> {value}
                      </p>
                    ))}
                </div>

                {/* DOCUMENTS */}
                <div className="flex flex-wrap gap-2 mt-2">
                  {user.documents &&
                    Object.entries(user.documents).map(([key, path]) => (
                      path && (
                        <a
                          key={key}
                          href={`http://localhost:8080/api/admin/file?path=${path}`}
                          target="_blank"
                          rel="noreferrer"
                          className="text-blue-600 text-sm underline"
                        >
                          {key}
                        </a>
                      )
                    ))}
                </div>

              </div>
            ))}
          </div>
        )}

      </div>
    </div>
  );
}

/* ---------------- REUSABLE COMPONENTS ---------------- */

function StatCard({ title, value }) {
  return (
    <div className="bg-white p-6 rounded-2xl shadow-sm border text-center">
      <p className="text-gray-500 text-sm">{title}</p>
      <h3 className="text-2xl font-bold mt-2 text-gray-800">
        {value || 0}
      </h3>
    </div>
  );
}

function ManageBox({ title, onClick }) {
  return (
    <div
      onClick={onClick}
      className="bg-white p-6 rounded-2xl shadow-sm border cursor-pointer hover:shadow-md transition"
    >
      <h3 className="text-lg font-semibold text-gray-800">
        {title}
      </h3>
      <p className="text-sm text-gray-500 mt-2">
        Click to view and manage
      </p>
    </div>
  );
}