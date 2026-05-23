import { useEffect, useState } from "react";
import API from "../../services/api";

export default function AdminApproval() {

  const [registrations, setRegistrations] = useState([]);

  useEffect(() => {
    fetchPendingUsers();
  }, []);

  const fetchPendingUsers = async () => {
    try {
      const res = await API.get("/admin/pending-users");
      setRegistrations(res.data);
    } catch (err) {
      console.error(err);
      alert("Failed to load users");
    }
  };

  const approveUser = async (id) => {
  try {
    const res = await API.put(`/admin/approve/${id}`);

    // ✅ Show backend message
    alert(res.data);

    // ✅ Remove approved user instantly
    setRegistrations(prev => prev.filter(user => user.id !== id));

  } catch {
    alert("Approval failed");
  }
};

const rejectUser = async (id) => {
  try {
    const res = await API.put(`/admin/reject/${id}`);

    // ✅ Show backend message
    alert(res.data);

    // ✅ Remove rejected user instantly
    setRegistrations(prev => prev.filter(user => user.id !== id));

  } catch {
    alert("Rejection failed");
  }
};

  const getFileUrl = (path) => {
    return `http://localhost:8080/api/admin/file?path=${encodeURIComponent(path)}`;
  };

  return (
    <div className="p-6 space-y-6 bg-gray-50 min-h-screen">

      <h1 className="text-2xl font-bold text-gray-800">
        Admin Approval Panel
      </h1>

      {registrations.length === 0 && (
        <p className="text-gray-500 text-sm">No pending users</p>
      )}

      {registrations.map((user) => {

        const details = user.details || {};

        // ✅ REMOVE unwanted fields
        const cleanDetails = Object.entries(details).filter(
          ([key, value]) =>
            value !== null &&
            value !== "" &&
            typeof value !== "object" &&
            !key.toLowerCase().includes("path")
        );

        // ✅ TITLE NAME (FIXED BASED ON BACKEND KEYS)
        const displayName =
          details["Full Name"] ||
          details["Hospital Name"] ||
          details["Bank Name"] ||
          "No Name";

        return (
          <div key={user.id} className="bg-white shadow-md rounded-2xl p-6">

            {/* HEADER */}
            <div className="flex justify-between items-start flex-wrap gap-4">

              <div>
                <h2 className="text-lg font-semibold text-gray-800">
                  {displayName}
                </h2>

                <p className="text-sm text-gray-500">Role: {user.role}</p>
                <p className="text-sm text-gray-500">Status: {user.status}</p>
              </div>

              {/* ACTION BUTTONS */}
              <div className="flex gap-3">
                <button
                  onClick={() => approveUser(user.id)}
                  className="bg-green-600 hover:bg-green-700 text-white px-5 py-2 rounded-lg text-sm shadow"
                >
                  Approve
                </button>

                <button
                  onClick={() => rejectUser(user.id)}
                  className="bg-red-600 hover:bg-red-700 text-white px-5 py-2 rounded-lg text-sm shadow"
                >
                  Reject
                </button>
              </div>
            </div>

            {/* BASIC INFO */}
            <div className="mt-4 text-sm text-gray-700 space-y-1">
              <p><b>Email:</b> {user.email || "N/A"}</p>
              <p><b>Phone:</b> {user.phone || "N/A"}</p>
            </div>

            {/* FULL DETAILS */}
            <div className="mt-4 grid md:grid-cols-2 gap-2 text-sm text-gray-700">

              {cleanDetails.map(([key, value]) => (
                <p key={key}>
                  <b>{key}:</b> {value}
                </p>
              ))}

            </div>

            {/* DOCUMENTS */}
            <div className="mt-5">
              <h3 className="font-semibold text-gray-800 mb-2">Documents</h3>

              <div className="flex flex-wrap gap-4">

                {Object.entries(user.documents || {}).map(([label, path]) => {

                  if (!path) return null;

                  const url = getFileUrl(path);

                  // ✅ IMAGE
                  if (/\.(jpg|jpeg|png)$/i.test(path)) {
                    return (
                      <div key={label} className="text-center">
                        <p className="text-xs mb-1">{label}</p>
                        <a href={url} target="_blank" rel="noreferrer">
                          <img
                            src={url}
                            alt={label}
                            className="w-24 h-24 object-cover rounded-lg border hover:scale-105 transition"
                          />
                        </a>
                      </div>
                    );
                  }

                  // ✅ PDF / OTHER
                  return (
                    <div key={label} className="text-center">
                      <p className="text-xs mb-1">{label}</p>
                      <a href={url} target="_blank" rel="noreferrer">
                        <button className="bg-gray-200 hover:bg-gray-300 px-3 py-1 rounded text-xs">
                          View File
                        </button>
                      </a>
                    </div>
                  );
                })}

              </div>
            </div>

          </div>
        );
      })}

    </div>
  );
}