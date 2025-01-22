import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./components/pages/LoginPage";
import RegisterPage from "./components/pages/RegisterPage";
import HomePage from "./components/pages/HomePage";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/" element={<HomePage />} />
      </Routes>
    </Router>
  );
}

export default App;
