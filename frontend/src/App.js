import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./components/pages/LoginPage";
import RegisterPage from "./components/pages/RegisterPage";
import HomePage from "./components/pages/HomePage";
import ProfilePage from "./components/pages/ProfilePage";
import CheckLoginStatus from "./components/CheckLoginStatus";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/user/profile" element={
          <CheckLoginStatus>
            <ProfilePage />
          </CheckLoginStatus>
        } />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/" element={
          <CheckLoginStatus>
            <HomePage />
          </CheckLoginStatus>
        } />
      </Routes>
    </Router>
  );
}

export default App;
