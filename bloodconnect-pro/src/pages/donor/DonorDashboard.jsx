import { useState, useEffect } from "react";
import { X } from "lucide-react";
import API from "../../services/api";

export default function DonorDashboard() {

  const [donor, setDonor] = useState(null);
  const [notifications, setNotifications] = useState([]);

  const [showEdit, setShowEdit] = useState(false);
  const [formData, setFormData] = useState({});

  // ✅ TRACK ORIGINAL EMAIL
  const [originalEmail, setOriginalEmail] = useState("");

  // ✅ EMAIL OTP
  const [otpType, setOtpType] = useState(null);
  const [otpValue, setOtpValue] = useState("");
  const [emailVerified, setEmailVerified] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const res = await API.get("/donor-dashboard/overview");
      const profile = res.data.profile;

      const mappedDonor = {
  name: profile.fullName,
  bloodGroup: profile.bloodGroup,
  email: profile.email,
  phone: profile.phone,
  city: profile.location,
  age: profile.age,
  pincode: profile.pincode || "",
  available: profile.available,

  // ✅ TAKE FROM BACKEND
  totalDonations: res.data.totalDonations,
  lastDonation: res.data.lastDonation,
  nextEligibleDate: res.data.nextEligibleDate,
};

      setDonor(mappedDonor);
      setFormData(mappedDonor);
      setOriginalEmail(profile.email); // ✅ STORE ORIGINAL

      const mappedNotifications = res.data.notifications.map((n, i) => ({
        id: i,
        title: n.type,
        message: n.message,
        time: n.time,
      }));

      setNotifications(mappedNotifications);

    } catch {
      alert("Failed to load dashboard");
    }
  };

  // ✅ TOGGLE AVAILABILITY
  const toggleAvailability = async () => {
    try {
      const newStatus = !donor.available;
      await API.put(`/donor/availability?status=${newStatus}`);
      setDonor({ ...donor, available: newStatus });
    } catch {
      alert("Failed to update availability");
    }
  };

  // ✅ REAL OTP SEND
  // ✅ ROBUST OTP SEND (handles multiple backend formats)
const sendOtp = async () => {
  try {
    if (!formData.email) {
      alert("Enter email first");
      return;
    }

    const res = await API.post(
      `/auth/send-otp?email=${encodeURIComponent(formData.email)}`
    );

    setOtpType("email");
    setEmailVerified(false);

    alert(res.data); // ✅ shows "OTP sent successfully"

  } catch (err) {
    console.error(err);
    alert(err.response?.data || "Failed to send OTP");
  }
};

  // ✅ REAL OTP VERIFY
  // ✅ ROBUST OTP VERIFY
const verifyOtp = async () => {
  try {
    const res = await API.post(
      `/auth/verify-otp?email=${encodeURIComponent(formData.email)}&otp=${otpValue}`
    );

    if (res.data === "OTP verified") {
      setEmailVerified(true);
      setOtpType(null);
      setOtpValue("");

      alert("Email verified successfully");
    } else {
      alert(res.data); // shows "Invalid OTP" or "Expired"
    }

  } catch (err) {
    console.error(err);
    alert(err.response?.data || "Verification failed");
  }
};

  // ✅ SAVE (STRICT + EMAIL CHANGE CHECK)
  const handleSave = async () => {

    const isEmailChanged = formData.email !== originalEmail;

    if (isEmailChanged && !emailVerified) {
      alert("Please verify your email before updating profile");
      return;
    }

    try {
      await API.put("/donor-dashboard/update", { // ✅ FIXED URL
        fullName: formData.name,
        location: formData.city,
        phone: formData.phone,
        age: formData.age,
        pincode: formData.pincode,

        emailVerified: emailVerified, // ✅ IMPORTANT

        user: {
          email: formData.email,
          phone: formData.phone,
        },
      });

      setDonor(formData);
      setOriginalEmail(formData.email); // ✅ UPDATE ORIGINAL
      setShowEdit(false);

      alert("Profile updated successfully");

    } catch {
      alert("Update failed");
    }
  };

  if (!donor) {
    return <p className="text-center mt-10">Loading...</p>;
  }

  return (
    <div className="min-h-screen bg-gray-50 pt-10 px-4 pb-10">
      <div className="max-w-7xl mx-auto space-y-8">

        {/* Header */}
        <div className="bg-white p-6 rounded-2xl shadow-md flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">
              Welcome, {donor.name}
            </h1>
            <p className="text-gray-500 mt-1">
              {donor.bloodGroup} | {donor.city}
            </p>
          </div>

          <div className="flex items-center gap-4">
            <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
              donor.available
                ? "bg-green-100 text-green-700"
                : "bg-gray-200 text-gray-600"
            }`}>
              {donor.available ? "Available" : "Not Available"}
            </span>

            <button
              onClick={toggleAvailability}
              className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-700"
            >
              Toggle
            </button>
          </div>
        </div>

        {/* Stats */}
        <div className="grid md:grid-cols-3 gap-6">
          <StatCard title="Total Donations" value={donor.totalDonations} />
          <StatCard title="Last Donation" value={donor.lastDonation} />
          <StatCard title="Next Eligible" value={donor.nextEligibleDate} />
        </div>

        {/* Notifications */}
        <div className="bg-white p-6 rounded-2xl shadow-md">
          <h2 className="text-lg font-bold mb-4 text-gray-800">Notifications</h2>
          <div className="divide-y">
            {notifications.map((note) => (
              <div key={note.id} className="py-4">
                <p className="font-semibold text-gray-800">{note.title}</p>
                <p className="text-sm text-gray-600 mt-1">{note.message}</p>
                <p className="text-xs text-gray-400 mt-2">{note.time}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Profile */}
        <div className="bg-white p-6 rounded-2xl shadow-md">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold">Profile Information</h2>
            <button onClick={() => setShowEdit(true)} className="text-red-600 font-semibold">
              Edit Profile
            </button>
          </div>

          <div className="grid md:grid-cols-2 gap-4 text-sm text-gray-700">
            <p><strong>Name:</strong> {donor.name}</p>
            <p><strong>Age:</strong> {donor.age}</p>
            <p><strong>Email:</strong> {donor.email}</p>
            <p><strong>Mobile:</strong> {donor.phone}</p>
            <p><strong>City:</strong> {donor.city}</p>
            <p><strong>Pincode:</strong> {donor.pincode}</p>
          </div>
        </div>
      </div>

      {/* Edit Modal */}
      {showEdit && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50">
          <div className="bg-white w-full max-w-lg p-6 rounded-2xl shadow-xl relative">
            <button onClick={() => setShowEdit(false)} className="absolute top-4 right-4">
              <X size={20} />
            </button>

            <h2 className="text-xl font-bold mb-4">Edit Profile</h2>

            <div className="space-y-4">

              <Input label="Name" value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />

              <Input label="City" value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              />

              <Input label="Pincode" value={formData.pincode}
                onChange={(e) => setFormData({ ...formData, pincode: e.target.value })}
              />

              {/* EMAIL WITH SMART OTP */}
              <Input label="Email" value={formData.email}
                onChange={(e) => {
                  const newEmail = e.target.value;

                  setFormData({ ...formData, email: newEmail });

                  if (newEmail !== originalEmail) {
                    setEmailVerified(false);
                  } else {
                    setEmailVerified(true);
                  }
                }}
              />

              {formData.email !== originalEmail && !emailVerified && (
                <button onClick={sendOtp} className="text-sm text-red-600">
                  Verify Email via OTP
                </button>
              )}

              {otpType === "email" && (
                <div className="border p-3 rounded-lg bg-gray-50">
                  <input
                    value={otpValue}
                    onChange={(e) => setOtpValue(e.target.value)}
                    className="w-full border p-2 rounded-lg mb-2"
                  />
                  <button
                    onClick={verifyOtp}
                    className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm"
                  >
                    Verify OTP
                  </button>
                </div>
              )}

              <Input label="Mobile" value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              />

              <button
                onClick={handleSave}
                className="bg-red-600 text-white w-full py-2 rounded-lg mt-4"
              >
                Save Changes
              </button>

            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({ title, value }) {
  return (
    <div className="bg-white p-6 rounded-2xl shadow-md text-center">
      <p className="text-gray-500 text-sm">{title}</p>
      <h3 className="text-xl font-bold mt-2 text-gray-800">{value}</h3>
    </div>
  );
}

function Input({ label, value, onChange }) {
  return (
    <div>
      <label className="text-sm font-semibold">{label}</label>
      <input
        value={value}
        onChange={onChange}
        className="w-full border p-2 rounded-lg mt-1"
      />
    </div>
  );
}