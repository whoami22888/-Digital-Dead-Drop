import { useState } from "react";
import { AnimatePresence } from "framer-motion";
import { SplashScreen } from "@/components/SplashScreen";
import { RoleSelection } from "@/components/RoleSelection";
import { UserDashboard } from "@/components/user/UserDashboard";
import { DropNoteScreen } from "@/components/user/DropNoteScreen";
import { CheckNotesScreen } from "@/components/user/CheckNotesScreen";
import { AdminDashboard } from "@/components/admin/AdminDashboard";

type Screen = 
  | "splash" 
  | "role-selection" 
  | "user-dashboard" 
  | "drop-note" 
  | "check-notes" 
  | "how-it-works"
  | "admin-dashboard";

const Index = () => {
  const [currentScreen, setCurrentScreen] = useState<Screen>("splash");

  const handleEnterSystem = () => {
    setCurrentScreen("role-selection");
  };

  const handleSelectRole = (role: "user" | "admin") => {
    if (role === "user") {
      setCurrentScreen("user-dashboard");
    } else {
      setCurrentScreen("admin-dashboard");
    }
  };

  const handleUserNavigate = (screen: string) => {
    setCurrentScreen(screen as Screen);
  };

  const handleBackToRoleSelection = () => {
    setCurrentScreen("role-selection");
  };

  const handleBackToUserDashboard = () => {
    setCurrentScreen("user-dashboard");
  };

  return (
    <AnimatePresence mode="wait">
      {currentScreen === "splash" && (
        <SplashScreen key="splash" onEnter={handleEnterSystem} />
      )}
      
      {currentScreen === "role-selection" && (
        <RoleSelection key="role-selection" onSelectRole={handleSelectRole} />
      )}
      
      {currentScreen === "user-dashboard" && (
        <UserDashboard 
          key="user-dashboard" 
          onNavigate={handleUserNavigate}
          onBack={handleBackToRoleSelection}
        />
      )}
      
      {currentScreen === "drop-note" && (
        <DropNoteScreen key="drop-note" onBack={handleBackToUserDashboard} />
      )}
      
      {currentScreen === "check-notes" && (
        <CheckNotesScreen key="check-notes" onBack={handleBackToUserDashboard} />
      )}
      
      {currentScreen === "admin-dashboard" && (
        <AdminDashboard key="admin-dashboard" onBack={handleBackToRoleSelection} />
      )}
    </AnimatePresence>
  );
};

export default Index;
