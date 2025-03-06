import React from "react";
import CheckLoginStatus from "../CheckLoginStatus";
import LayoutComponent from "../LayoutComponent";

const HomePage = () => {
  return (
    <CheckLoginStatus>
      <LayoutComponent />
    </CheckLoginStatus>
  )
};

export default HomePage;
