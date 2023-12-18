
import React, { useState, useEffect } from 'react';
import { Routes, Route } from "react-router-dom";
import Topbar from "./scenes/global/Topbar";
import Sidebar from "./scenes/global/Sidebar";
import Dashboard from "./scenes/dashboard";
import Ingredients from "./Ingredients";
import Team from "./scenes/team";
import RecipeTable from "./scenes/RecipeTable";
import Contacts from "./scenes/contacts";
import Bar from "./scenes/bar";
import Form from "./scenes/form";
import Line from "./scenes/line";
import Pie from "./scenes/pie";
import FAQ from "./scenes/faq";
import Geography from "./scenes/geography";
import { CssBaseline, ThemeProvider } from "@mui/material";
import { ColorModeContext, useMode } from "./theme";
import Calendar from "./scenes/calendar/calendar";
import Parse from 'parse';
import axios from 'axios';

// const PARSE_APPLICATION_ID = 'e6Ll6TU4kwmISih6oezEZOsmnm32eGOhNKu7Z7VK';
// const PARSE_HOST_URL = 'https://parseapi.back4app.com/';
// const PARSE_JAVASCRIPT_KEY = 'zSbnGQd9vrnuKD5no5syGAsmQge8vkNIZZ1htN6y';

function App() {
  const [theme, colorMode] = useMode();
  const [isSidebar, setIsSidebar] = useState(true);

  useEffect(() => {
    const initializeParse = async () => {
      try {
        // Parse.initialize(PARSE_APPLICATION_ID, PARSE_JAVASCRIPT_KEY);
        // Parse.serverURL = PARSE_HOST_URL;
        // Other initialization logic can go here
      } catch (error) {
        console.error('Error initializing Parse:', error);
      }
    };

    initializeParse();
  }, []); // Empty dependency array ensures useEffect runs only once on mount

  // ... (existing render logic)

  
  

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <div className="app">
          <Sidebar isSidebar={isSidebar} />
          <main className="content">
            <Topbar setIsSidebar={setIsSidebar} />
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/Ingredients" element={<Ingredients />} />
              <Route path="/team" element={<Team />} />
              <Route path="/contacts" element={<Contacts />} />
              <Route path="/RecipeTable" element={<RecipeTable />} />
              <Route path="/form" element={<Form />} />
              <Route path="/bar" element={<Bar />} />
              <Route path="/pie" element={<Pie />} />
              <Route path="/line" element={<Line />} />
              <Route path="/faq" element={<FAQ />} />
              <Route path="/calendar" element={<Calendar />} />
              <Route path="/geography" element={<Geography />} />
            </Routes>
          </main>
        </div>
      </ThemeProvider>
    </ColorModeContext.Provider>
  );
}

export default App;
