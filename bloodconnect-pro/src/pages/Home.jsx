import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import API from "../services/api";

export default function Home() {

  // ✅ EXISTING (unchanged)
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");

  // ✅ NEW STATE (for backend data)
  const [stats, setStats] = useState({
    hospitals: 0,
    bloodBanks: 0,
    donors: 0,
    responses: 0,
  });

  // ✅ FETCH DATA FROM BACKEND (NO LOGIC CHANGE ELSEWHERE)
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await API.get("/public/stats");
        setStats(res.data);
      } catch (err) {
        console.error("Error fetching stats:", err);
      }
    };

    fetchStats();
  }, []);

  return (
    <div className="bg-gray-50 min-h-screen flex flex-col">

      {/* ================= HERO ================= */}
      <section className="bg-gradient-to-r from-red-700 to-red-600 text-white py-20 px-6">
        <div className="max-w-6xl mx-auto text-center">
          <h1 className="text-4xl md:text-5xl font-bold leading-tight mb-6">
            Intelligent Blood Coordination & Emergency Response Platform
          </h1>

          <p className="max-w-3xl mx-auto text-lg opacity-90">
            BloodConnect Pro is a secure, role-based digital infrastructure
            designed to streamline emergency blood management between
            verified hospitals, licensed blood banks, and voluntary donors.
          </p>

          {/* ✅ BUTTON LOGIC (UNCHANGED) */}
          <div className="mt-8 flex justify-center gap-4 flex-wrap">
            {!token ? (
              <>
                <Link
                  to="/login"
                  className="bg-white text-red-600 px-6 py-3 rounded-lg font-semibold shadow hover:scale-105 transition"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="border border-white px-6 py-3 rounded-lg font-semibold hover:bg-white hover:text-red-600 transition"
                >
                  Register
                </Link>
              </>
            ) : (
              <Link
                to={
                  role === "DONOR"
                    ? "/donor-dashboard"
                    : role === "HOSPITAL"
                    ? "/hospital-dashboard"
                    : role === "BLOOD_BANK"
                    ? "/bloodbank-dashboard"
                    : role === "ADMIN"
                    ? "/admin-dashboard"
                    : "/"
                }
                className="bg-white text-red-600 px-6 py-3 rounded-lg font-semibold shadow hover:scale-105 transition"
              >
                Go to Dashboard
              </Link>
            )}
          </div>

        </div>
      </section>

      {/* ================= EMERGENCY STRIP ================= */}
      <section className="bg-red-50 border-y border-red-100 py-3 text-center text-sm text-red-700 font-medium">
        Real-time emergency blood request handling with verified institutional access.
      </section>

      {/* ================= PLATFORM SUMMARY ================= */}
      <section className="py-16 px-6">
        <div className="max-w-6xl mx-auto grid md:grid-cols-2 gap-12 items-center">

          <div>
            <h2 className="text-3xl font-bold mb-6">
              Built for Healthcare Infrastructure
            </h2>

            <p className="text-gray-600 mb-4">
              BloodConnect Pro is not just a donor listing system.
              It is a structured, admin-controlled digital coordination
              platform that ensures secure emergency communication,
              transparent verification, and controlled data visibility.
            </p>

            <p className="text-gray-600">
              Designed for real-world hospital workflows, the system reduces
              delays, prevents misuse of donor information, and enables
              accountability across institutions.
            </p>
          </div>

          {/* ✅ NOW USING BACKEND DATA */}
          <div className="grid grid-cols-2 gap-6">
            <StatBox number={stats.hospitals + "+"} label="Verified Hospitals" />
            <StatBox number={stats.bloodBanks + "+"} label="Licensed Blood Banks" />
            <StatBox number={stats.donors + "+"} label="Active Donors" />
            <StatBox number={stats.responses + "+"} label="Emergency Responses Managed" />
          </div>

        </div>
      </section>

      {/* ================= USER ROLES ================= */}
      <section className="bg-white py-16 px-6">
        <div className="max-w-6xl mx-auto text-center mb-12">
          <h2 className="text-3xl font-bold">Role-Based Access Architecture</h2>
        </div>

        <div className="grid md:grid-cols-4 gap-6 max-w-6xl mx-auto text-sm">

          <FeatureCard
            title="Donors"
            description="Maintain availability status, receive controlled emergency alerts, and respond securely without exposing personal data publicly."
          />

          <FeatureCard
            title="Hospitals"
            description="Raise emergency blood requests, search verified donors or blood banks by location and blood type, and manage inventory shortages."
          />

          <FeatureCard
            title="Blood Banks"
            description="Maintain real-time inventory, respond to hospital emergency requests, and manage supply transfers securely."
          />

          <FeatureCard
            title="System Admin"
            description="Approve registrations, monitor platform activity, verify institutions, and maintain compliance governance."
          />

        </div>
      </section>

      {/* ================= HOW SYSTEM WORKS ================= */}
      <section className="py-16 px-6 bg-gray-50">
        <div className="max-w-6xl mx-auto text-center mb-10">
          <h2 className="text-3xl font-bold">
            Operational Workflow
          </h2>
        </div>

        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto text-sm">

          <ProcessCard
            step="1"
            title="Verification & Approval"
            description="Institutions are verified by administrators before gaining access to platform features."
          />

          <ProcessCard
            step="2"
            title="Emergency Trigger"
            description="Hospitals initiate blood requests when inventory falls below safe threshold levels."
          />

          <ProcessCard
            step="3"
            title="Smart Controlled Matching"
            description="Nearby verified donors and blood banks are notified instantly based on blood group and location."
          />

        </div>
      </section>

      {/* ================= SECURITY & COMPLIANCE ================= */}
      <section className="bg-white py-16 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-6">
            Privacy, Security & Ethical Compliance
          </h2>

          <p className="text-gray-600 text-sm leading-relaxed">
            BloodConnect Pro enforces strict access control. Donor personal
            details are never publicly accessible. Information is only
            revealed to verified institutions during confirmed emergency matches.
            All system actions are traceable, ensuring accountability and preventing misuse.
          </p>
        </div>
      </section>

      {/* ================= TECHNOLOGY & FUTURE VISION ================= */}
      <section className="py-16 px-6 bg-gray-50">
        <div className="max-w-5xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-6">
            Designed for Scalability & Future Expansion
          </h2>

          <p className="text-gray-600 text-sm leading-relaxed">
            The system architecture supports multi-city expansion,
            real-time analytics integration, predictive shortage analysis,
            and centralized health authority reporting in future upgrades.
            BloodConnect Pro is built as a sustainable digital healthcare
            coordination infrastructure.
          </p>
        </div>
      </section>

      {/* ================= FINAL CTA ================= */}
      <section className="bg-red-700 text-white py-16 text-center px-6">
        <h2 className="text-3xl font-bold mb-4">
          Join a Verified Emergency Response Network
        </h2>
        <p className="mb-6 text-sm opacity-90">
          Register your institution or volunteer as a donor
          and become part of a secure, accountable life-saving system.
        </p>
        <Link
          to="/register"
          className="bg-white text-red-700 px-8 py-3 rounded-lg font-semibold"
        >
          Get Started
        </Link>
      </section>

      {/* ================= FOOTER ================= */}
      <footer className="bg-gray-900 text-gray-400 py-8 text-center text-xs">
        <p>© 2026 BloodConnect Pro. All Rights Reserved.</p>
        <p className="mt-2">
          Secure Digital Blood Coordination & Emergency Management Platform
        </p>

        {/* ✅ ADDED CONTACT EMAIL ONLY */}
        <p className="mt-2">
          Contact: bloodconnectpro@gmail.com
        </p>
      </footer>

    </div>
  );
}


/* ================= REUSABLE COMPONENTS ================= */

function StatBox({ number, label }) {
  return (
    <div className="bg-white p-6 rounded-xl shadow text-center">
      <h3 className="text-2xl font-bold text-red-600">{number}</h3>
      <p className="text-gray-500 text-sm mt-1">{label}</p>
    </div>
  );
}

function FeatureCard({ title, description }) {
  return (
    <div className="bg-gray-50 p-6 rounded-xl shadow text-center">
      <h3 className="font-semibold mb-3">{title}</h3>
      <p className="text-gray-600">{description}</p>
    </div>
  );
}

function ProcessCard({ step, title, description }) {
  return (
    <div className="bg-white p-6 rounded-xl shadow text-center">
      <div className="text-red-600 font-bold text-xl mb-3">{step}</div>
      <h3 className="font-semibold mb-2">{title}</h3>
      <p className="text-gray-600">{description}</p>
    </div>
  );
}