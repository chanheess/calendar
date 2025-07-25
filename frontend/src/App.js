import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./components/pages/LoginPage";
import RegisterPage from "./components/pages/RegisterPage";
import HomePage from "./components/pages/HomePage";
import ProfilePage from "./components/pages/ProfilePage";
import MainLandingPage from "./components/pages/MainLandingPage";
import PrivacyPolicyPage from "./components/pages/PrivacyPolicyPage";
import ConditionalRoute from "./components/ConditionalRoute";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/user/profile" element={
          <ConditionalRoute>
            <ProfilePage />
          </ConditionalRoute>
        } />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/" element={<ConditionalRoute />} />
        <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
      </Routes>
    </Router>
  );
}

export default App;
